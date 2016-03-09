package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.util.TimeCounter;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.raytracing.util.BoundingBox3d;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.Scene;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManager;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManagerSettings;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelSpace;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.util.Cancellable;
import fr.amap.commons.util.Process;
import fr.amap.commons.util.vegetation.DirectionalTransmittance;
import fr.amap.commons.util.vegetation.LADParams;
import fr.amap.commons.util.vegetation.LeafAngleDistribution;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos.Type;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.GroundEnergyParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightParams;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.vecmath.Point3i;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

public class VoxelAnalysis extends Process implements Cancellable{

    public enum LaserSpecification{
        
        DEFAULT_ALS(0.0003, 0.0005),
        VZ_400(0.007, 0.00035),
        LEICA_SCANSTATION_P30_40(0.0035, 0.00023),
        LEICA_SCANSTATION_C10(0.004, 0.0001);
    
        private final double beamDiameterAtExit;
        private final double beamDivergence;
    
        private LaserSpecification(double beamDiameterAtExit, double beamDivergence){
            this.beamDiameterAtExit = beamDiameterAtExit;
            this.beamDivergence = beamDivergence;
        }

        public double getBeamDiameterAtExit() {
            return beamDiameterAtExit;
        }

        public double getBeamDivergence() {
            return beamDivergence;
        }
        
    }
    
    private final static Logger LOGGER = Logger.getLogger(VoxelAnalysis.class);
    
    private boolean cancelled;

    private Voxel voxels[][][];
    private VoxelManager voxelManager;

    private float MAX_PAD = 3;

    private int nbShotsProcessed;
    private int tmpNbShotsProcessed = 0;
    private File outputFile;

    private float[][] weighting;
    private float[][] residualEnergyTable;
    
    private final boolean volumeWeighting = true;
    
    private int transMode = 1;

    private GroundEnergy[][] groundEnergy;
    private VoxelParameters parameters;

    private boolean isSet = false;

    private Raster dtm;

    private boolean shotChanged = false;
    private Voxel lastVoxelSampled;
    private int lastShotId;
    
    private LaserSpecification laserSpec;
    
    private ShotFilter shotFilter;
    private EchoFilter echoFilter;
    private List<PointcloudFilter> pointcloudFilters;
    
    private boolean padComputed;
    
    //directional transmittance (GTheta)
    private DirectionalTransmittance direcTransmittance;
    /**
     *
     */
    public class GroundEnergy {

        /*ATTENTION : le calcul de l'énergie arrivant au sol est incorrect actuellement,
         en effet, la prolongation d'un tir après son dernier écho jusqu'au sol ne se fait
         pas pour les tirs ayant le dernier écho en dehors de l'espace voxel.
         En correction, il faudrait faire un pré-calcul pour déterminer la taille de la 
        bounding-box pour inclure les tirs correspondants.
        Donc créer deux espaces voxels en l'occurence.*/
        
        public int groundEnergyPotential;
        public float groundEnergyActual;

        public GroundEnergy() {
            groundEnergyPotential = 0;
            groundEnergyActual = 0;
        }
    }
    
    private void generateResidualEnergyTable(){
        
        residualEnergyTable = new float[weighting.length][weighting[0].length];
        
        for(int i=0;i<weighting.length;i++){
            
            float startEnergy = 1;
            
            for(int j=0;j<i+1;j++){
                residualEnergyTable[i][j] = startEnergy;
                startEnergy -= weighting[i][j];
            }
        }
    }
    
    private void generateNoPonderationTables(){
        
        weighting = new float[7][7];
        
        for(int i=0;i<weighting.length;i++){
                        
            for(int j=0;j<i+1;j++){
                weighting[i][j] = 1;
            }
            
            for(int j=i+1;j<7;j++){
                weighting[i][j] = Float.NaN;
            }
        }
        
        residualEnergyTable = new float[7][7];
        
        for(int i=0;i<weighting.length;i++){
                        
            for(int j=0;j<i+1;j++){
                residualEnergyTable[i][j] = 1;
            }
            
            for(int j=i+1;j<7;j++){
                residualEnergyTable[i][j] = Float.NaN;
            }
        }
    }

    private float getGroundDistance(float x, float y, float z) {

        float distance = 0;

        if (dtm != null && parameters.getDtmFilteringParams().useDTMCorrection()) {
            distance = z - (float) (dtm.getSimpleHeight(x, y));
        }

        return distance;
    }
            
    public VoxelAnalysis(Raster terrain, List<PointcloudFilter> pointcloudFilters, VoxelAnalysisCfg cfg) {

        nbShotsProcessed = 0;
        tmpNbShotsProcessed = 0;
        this.dtm = terrain;
        this.pointcloudFilters = pointcloudFilters;
        
        shotFilter = cfg.getShotFilter();
        echoFilter = cfg.getEchoFilter();
    }
    
    public VoxelAnalysis() {

        nbShotsProcessed = 0;
        tmpNbShotsProcessed = 0;
    }

    /**
     * Get position of center of a voxel
     * @param indices
     * @param splitting
     * @param minCorner
     * @return
     */
    public Point3d getPosition(Point3i indices, Point3i splitting, Point3d minCorner) {
        
        double posX = minCorner.x + (voxelManager.getVoxelSpace().getVoxelSize().x / 2.0d) + (indices.x * voxelManager.getVoxelSpace().getVoxelSize().x);
        double posY = minCorner.y + (voxelManager.getVoxelSpace().getVoxelSize().y / 2.0d) + (indices.y * voxelManager.getVoxelSpace().getVoxelSize().y);
        double posZ = minCorner.z + (voxelManager.getVoxelSpace().getVoxelSize().z / 2.0d) + (indices.z * voxelManager.getVoxelSpace().getVoxelSize().z);

        return new Point3d(posX, posY, posZ);
    }

    public void init(VoxelParameters parameters, File outputFile) {

        this.parameters = parameters;
        this.outputFile = outputFile;
        
        if(parameters.getEchoesWeightParams().getWeightingMode() != EchoesWeightParams.WEIGHTING_NONE){
            weighting = parameters.getEchoesWeightParams().getWeightingData();
            generateResidualEnergyTable();
        }else{
            
            //pas de pondération
            generateNoPonderationTables();
        }
        
        MAX_PAD = parameters.infos.getMaxPAD();
        
        laserSpec = parameters.getLaserSpecification();
        
        if(laserSpec == null){
            if(parameters.infos.getType() == VoxelSpaceInfos.Type.TLS){
                laserSpec = LaserSpecification.VZ_400;
            }else{
                laserSpec = LaserSpecification.DEFAULT_ALS;
            }
        }
        
        LADParams ladParameters = parameters.getLadParams();
        if(ladParameters == null){
            ladParameters = new LADParams();
        }

        LeafAngleDistribution distribution = new LeafAngleDistribution(ladParameters.getLadType(), 
                                                                        ladParameters.getLadBetaFunctionAlphaParameter(),
                                                                        ladParameters.getLadBetaFunctionBetaParameter());
        
        direcTransmittance = new DirectionalTransmittance(distribution);
        
        LOGGER.info("Building transmittance functions table");
        direcTransmittance.buildTable(DirectionalTransmittance.DEFAULT_STEP_NUMBER);
        LOGGER.info("Transmittance functions table is built");
    }

    public void processOneShot(final Shot shot) {

        if (voxelManager == null) {
            LOGGER.error("VoxelManager not initialized, what happened??");
            return;
        }
        
        if((shotFilter != null && shotFilter.doFiltering(shot)) || shotFilter == null ){
            
            if(tmpNbShotsProcessed == 1000000){
                LOGGER.info("Shots processed: " + nbShotsProcessed);
                tmpNbShotsProcessed = 0;
            }

            shot.direction.normalize();
            Point3d origin = new Point3d(shot.origin);

            if (shot.nbEchos == 0) {

                LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);
                Point3d echo = new Point3d(seg.getEnd());
                propagate(origin, echo, 1, 1, false, nbShotsProcessed, shot, -1);

            } else {

                double beamFraction = 1;

                shotChanged = true;
                isSet = false;

                double residualEnergy;
                float lastEchoBeamFraction = 0;
                int firstEchoOfVoxel = 0;

                for (int i = 0; i < shot.nbEchos; i++) {

                    Point3d nextEcho = null;

                    if (i < shot.nbEchos - 1) {
                        nextEcho = new Point3d(getEchoLocation(shot, i + 1));
                    }

                    Point3d echo = getEchoLocation(shot, i);

                    /*vérifie que le dernier écho n'était pas un écho "multiple",
                    c'est à dire le premier écho du tir dans le voxel*/
                    boolean wasMultiple = false;
                    if(lastEchoBeamFraction != 0){
                        wasMultiple = true;
                    }

                    if (echosAreInsideSameVoxel(echo, nextEcho)) {

                        /*ne rien faire dans ce cas
                         le beamFraction est incrémenté et l'opération se fera sur l'écho suivant*/
                        lastEchoBeamFraction += weighting[shot.nbEchos - 1][i];

                        if(!wasMultiple){
                            firstEchoOfVoxel = i;
                        }

                    } else {

                        if (parameters.getEchoesWeightParams().getWeightingMode() != EchoesWeightParams.WEIGHTING_NONE) {
                            beamFraction = weighting[shot.nbEchos - 1][i] + lastEchoBeamFraction;

                            if(wasMultiple){
                                residualEnergy = residualEnergyTable[shot.nbEchos - 1][firstEchoOfVoxel];
                            }else{
                                residualEnergy = residualEnergyTable[shot.nbEchos - 1][i];
                            }

                        }else{
                            beamFraction = 1;
                            residualEnergy = 1;
                        }

                        lastEchoBeamFraction = 0;

                        boolean lastEcho;

                        lastEcho = i == shot.nbEchos - 1;

                        // propagate
                        propagate(origin, echo, beamFraction, residualEnergy, lastEcho, nbShotsProcessed, shot, i);

                        origin = new Point3d(echo);
                    }
                }
            }

            nbShotsProcessed++;
            tmpNbShotsProcessed++;
        }
        
    }

    private Point3d getEchoLocation(Shot shot, int indice) {

        LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[indice]);
        return seg.getEnd();
    }

    private boolean isEchoInsideVoxel(Point3d point, Point3i searchedIndices) {

        Point3i realIndices = voxelManager.getVoxelIndicesFromPoint(point);

        return realIndices.equals(searchedIndices);
    }

    private boolean echosAreInsideSameVoxel(Point3d echo1, Point3d echo2) {

        if (echo1 == null || echo2 == null) {
            return false;
        }

        Point3i indices1 = voxelManager.getVoxelIndicesFromPoint(echo1);
        Point3i indices2 = voxelManager.getVoxelIndicesFromPoint(echo2);

        return indices1 != null && indices2 != null && indices1.equals(indices2);
    }

    /**
     *
     * @param origin current origin (origin start from the last echo)
     * @param echo current echo (position in voxel space)
     * @param beamFraction current beam fraction of laser shot
     * @param residualEnergy residual energy of laser beam
     * @param lastEcho is current echo the last or not
     * @param shotID current shot (id)
     * @param shot current shot processed
     * @param echoID current echo processed (id)
     */
    private void propagate(Point3d origin, Point3d echo, double beamFraction, double residualEnergy, boolean lastEcho, int shotID, Shot shot, int echoID) {

        //get shot line
        LineElement lineElement = new LineSegment(origin, echo);

        //get the first voxel cross by the line
        VoxelCrossingContext context = voxelManager.getFirstVoxelV2(lineElement);

        double distanceToHit = lineElement.getLength();

        //calculate ground distance
        float echoDistance = getGroundDistance((float) echo.x, (float) echo.y, (float) echo.z);
        boolean keepEcho, keepEchoPointCloudFiltering = true;
        
        //echo filtering
        if(pointcloudFilters != null){
            for(PointcloudFilter filter : pointcloudFilters){
                keepEchoPointCloudFiltering = keepEchoPointCloudFiltering && filter.doFiltering(echo);
            }
        }
        
        keepEcho = keepEchoOfShot(shot, echoID) &&
                (echoDistance >= parameters.getDtmFilteringParams().getMinDTMDistance() &&
                !Float.isNaN(echoDistance) && parameters.getDtmFilteringParams().useDTMCorrection()) ||
                !parameters.getDtmFilteringParams().useDTMCorrection() &&
                keepEchoPointCloudFiltering;

        while ((context != null) && (context.indices != null)) {

            //distance from the last origin to the point in which the ray enter the voxel
            double d1 = context.length;

            //current voxel
            Point3i indices = context.indices;

            context = voxelManager.CrossVoxel(lineElement, context.indices);

            //distance from the last origin to the point in which the ray exit the voxel
            double d2 = context.length;
            
            
            if (voxels[indices.x][indices.y][indices.z] == null) {
                voxels[indices.x][indices.y][indices.z] = initVoxel(indices.x, indices.y, indices.z);
            }

            Voxel vox = voxels[indices.x][indices.y][indices.z];
            
            double surface;

            //recalculé pour éviter le stockage de trois doubles (24 octets) par voxel.
            double distance = getPosition(new Point3i(indices.x, indices.y, indices.z), parameters.infos.getSplit(), parameters.infos.getMinCorner()).distance(shot.origin);
            
            //surface de la section du faisceau à la distance de la source
            if (parameters.getEchoesWeightParams().getWeightingMode() != EchoesWeightParams.WEIGHTING_NONE && volumeWeighting) {
                surface = Math.pow((Math.tan(laserSpec.getBeamDivergence() / 2.0) * distance) + laserSpec.getBeamDiameterAtExit(), 2) * Math.PI;
            } else {
                surface = 1;
            }
            
            /*double distance1 = 1;
            double distance2 = 2;
            double tanBeamDivergence = Math.tan(laserSpec.getBeamDivergence() / 2.0);
            double r = (tanBeamDivergence * distance1) + laserSpec.getBeamDiameterAtExit();
            double R = (tanBeamDivergence * distance2) + laserSpec.getBeamDiameterAtExit();*/

            
            // voxel sampled without hit
            /*Si un écho est positionné sur une face du voxel alors il est considéré
             comme étant à l'extérieur du dernier voxel traversé*/
            
            double surfMulLength = 0;
            double intercepted = 0;
            double entering = 0;
            double longueur = 0;
            
            boolean test = false;
                
             /*
             * Si d2 < distanceToHit le voxel est traversé sans interceptions
             */
            if (d2 < distanceToHit) {

                if(shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox){
                    //pour n'échantillonner qu'une fois le voxel pour un tir
                }else{
                    longueur = d2 - d1;
                    
                    vox.lgTotal += longueur;
                
                    vox.nbSampling++;

                    vox.angleMean += shot.angle;
                    
                    //double volume = longueur * ONE_THIRD_OF_PI * ((r*r)+(R*R)+(r*R));
                    //vox.bvEntering += volume * (Math.round(residualEnergy*10000)/10000.0);
                    
                    surfMulLength = surface * longueur;
                    entering = (Math.round(residualEnergy*10000)/10000.0);
                    vox.bvEntering += (entering * surfMulLength);

                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                    
                    test = true;
                }
                
                /*
                 Si l'écho est sur la face sortante du voxel, 
                 on n'incrémente pas le compteur d'échos
                 */
            } /*
             Poursuite du trajet optique jusqu'à sortie de la bounding box
             */ else if (d1 >= distanceToHit) {

                /*
                 la distance actuelle d1 est supérieure à la distance à l'écho
                 ce qui veut dire que l'écho a déjà été trouvé
                 */
                //on peut chercher ici la distance jusqu'au prochain voxel "sol"
                if (shotChanged) {

                    if (parameters.getGroundEnergyParams() != null && 
                            parameters.getGroundEnergyParams().isCalculateGroundEnergy() &&
                            parameters.infos.getType() != VoxelSpaceInfos.Type.TLS) {

                        if (vox.ground_distance < parameters.getDtmFilteringParams().getMinDTMDistance()) {
                            groundEnergy[vox.$i][vox.$j].groundEnergyPotential++;
                            shotChanged = false;
                            context = null;
                        }

                    } else {
                        context = null;
                    }

                }

            } /*
             Echo dans le voxel
             */ else {

                /*si plusieurs échos issus du même tir dans le voxel, 
                 on incrémente et le nombre de tirs entrants (nbsampling) 
                 et le nombre d'interception (interceptions) 
                 et la longueur parcourue(lgInterception)*/
                /*
                 * Si distanceToHit == d1,on incrémente le compteur d'échos
                 */
                 
                test = true;

                //si l'écho n'est pas un dernier écho mais au niveau distance ne sort pas du voxel courant, alors la longueur sera surestimé
                if (lastEcho) {
                    longueur = (distanceToHit - d1);
                } else {
                    longueur = (d2 - d1);
                }
                    
                if(shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox){
                    //pour n'échantillonner qu'une fois le voxel pour un tir
                }else{
                    vox.nbSampling++;

                    vox.lgTotal += longueur;

                    vox.angleMean += shot.angle;
                    
                    surfMulLength = surface * longueur;
                    
                    entering = (Math.round(residualEnergy*10000)/10000.0);
                    vox.bvEntering += (entering * surfMulLength);
                    
                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                }
                
                if (keepEcho){

                /*if ((keepEchoOfShot(shot, echoID))
                        && ((echoDistance >= parameters.minDTMDistance && echoDistance != Float.NaN && parameters.useDTMCorrection()) || !parameters.useDTMCorrection())
                        && keepEcho) {*/

                    if(!lastEcho){
                        vox._lastEcho = false;
                    }
                    vox.nbEchos++;

                    surfMulLength = surface * longueur;
                    intercepted = (Math.round(beamFraction*10000)/10000.0);
                    vox.bvIntercepted += (intercepted * surfMulLength);

                } else {

                    if (parameters.getGroundEnergyParams() != null && 
                            parameters.getGroundEnergyParams().isCalculateGroundEnergy() &&
                            parameters.infos.getType() != VoxelSpaceInfos.Type.TLS && !isSet) {
                        groundEnergy[vox.$i][vox.$j].groundEnergyActual += residualEnergy;
                        groundEnergy[vox.$i][vox.$j].groundEnergyPotential++;

                        isSet = true;
                    }
                }
            }
            
            if(test && (transMode == 2 || transMode == 3)){
                double transNorm;
                
                if(transMode == 2){
                    transNorm = ((entering - intercepted) / entering) * surfMulLength;
                }else{ //mode 3
                    transNorm = Math.pow(((entering - intercepted) / entering), 1/longueur) * surfMulLength;
                }
                
                vox._transmittance_norm += transNorm;

                vox._sumSurfMulLength += surfMulLength;
            }
            
            
        }

    }
    
    private boolean keepEchoOfShot(Shot shot, int echoID){
        
        if(echoFilter !=null){
            return echoFilter.doFiltering(shot, echoID);
        }else{
            return true;
        }
    }
    
    public float computeTransmittance(float bvEntering, float bvIntercepted){
        
        float transmittance;
        
        if (bvEntering == 0) {

            transmittance = Float.NaN;
            
        }else if(bvIntercepted > bvEntering){
            transmittance = Float.NaN;
            
        }else {
            transmittance = (bvEntering - bvIntercepted) / bvEntering;
        }
        
        return transmittance;
    }
    
    public float computeNormTransmittanceMode2(double transmittance, double _sumSurfMulLength, double lMeanTotal){
        float normalizedTransmittance = (float) Math.pow((transmittance / _sumSurfMulLength), 1 / lMeanTotal);
        return normalizedTransmittance;
    }
    
    public float computeNormTransmittanceV2(double transmittance, double _sumSurfMulLength){
        float normalizedTransmittance = (float) (transmittance / _sumSurfMulLength);
        return normalizedTransmittance;
    }
    
    public float computeNormTransmittance(double transmittance, double lMeanTotal){
        float normalizedTransmittance = (float) Math.pow(transmittance, 1 / lMeanTotal);
        return normalizedTransmittance;
    }
    
    public float computePADFromNormTransmittance(float transmittance, float angleMean){
        
        float pad;
        
        if (Float.isNaN(transmittance)) {

            pad = Float.NaN;
            
        }else {

            if (transmittance == 0) {

                pad = MAX_PAD;

            }else {

                float coefficientGTheta = (float) direcTransmittance.getTransmittanceFromAngle(angleMean, true);
                
                pad = (float) (Math.log(transmittance) / (-coefficientGTheta));

                if (Float.isNaN(pad)) {
                    pad = Float.NaN;
                } else if (pad > MAX_PAD || Float.isInfinite(pad)) {
                    pad = MAX_PAD;
                }

            }

        }

        return pad + 0.0f; //set +0.0f to avoid -0.0f
    }
    
    public Voxel computePADFromVoxel(Voxel voxel, int i, int j, int k) {
        
        if (voxel == null) {

            voxel = initVoxel(i, j, k);
        }
        
        voxel.angleMean = voxel.angleMean / voxel.nbSampling;

        if (voxel.nbSampling >= voxel.nbEchos) {

            voxel.lMeanTotal = voxel.lgTotal / (voxel.nbSampling);

        }
        
        float normalizedTransmittance;
        
        switch(transMode){
            case 2:
                normalizedTransmittance = computeNormTransmittanceMode2(voxel._transmittance_norm, voxel._sumSurfMulLength, voxel.lMeanTotal);
                break;
            case 3:
                normalizedTransmittance = computeNormTransmittanceV2(voxel._transmittance_norm, voxel._sumSurfMulLength);
                break;
                
            case 1:
            default:
                float transmittance = computeTransmittance(voxel.bvEntering, voxel.bvIntercepted);
                normalizedTransmittance = computeNormTransmittance(transmittance, voxel.lMeanTotal);
        }
        
        voxel.transmittance = normalizedTransmittance;
        voxel.PadBVTotal = computePADFromNormTransmittance(normalizedTransmittance, voxel.angleMean);
        
        return voxel;
    }

//    public Voxel computePADFromVoxel(Voxel voxel, int i, int j, int k) {
//
//        if (voxel == null) {
//
//            voxel = initVoxel(i, j, k);
//        }
//
//        float pad1;
//
//        voxel.angleMean = voxel.angleMean / voxel.nbSampling;
//
//        if (voxel.nbSampling >= voxel.nbEchos) {
//
//            voxel.lMeanTotal = voxel.lgTotal / (voxel.nbSampling);
//
//        }
//
//        /**
//         * *PADBV**
//         */
//        if (voxel.nbSampling == 0) {
//
//            pad1 = Float.NaN;
//            voxel.transmittance = Float.NaN;
//            
//        }else if(voxel.bvIntercepted > voxel.bvEntering){
//            
//            LOGGER.error("Voxel : " + voxel.$i + " " + voxel.$j + " " + voxel.$k + " -> bvInterceptes > bvEntering, NaN assigné, difference: " + (voxel.bvEntering - voxel.bvIntercepted));
//
//            pad1 = Float.NaN;
//            voxel.transmittance = Float.NaN;
//            
//        }else {
//            
//            voxel.transmittance = (voxel.bvEntering - voxel.bvIntercepted) / voxel.bvEntering;
//            voxel.transmittance = (float) Math.pow(voxel.transmittance, 1 / voxel.lMeanTotal);
//
//            if (/*voxel.nbSampling > 1 && */voxel.transmittance == 0) { // nbSampling == nbEchos
//
//                pad1 = MAX_PAD;
//
//            }/* else if (voxel.nbSampling == 1 && voxel.transmittance == 0 ) { // nbSampling == nbEchos
//
//                pad = Float.NaN;
//
//            } */else {
//
//                float coefficientGTheta = (float) direcTransmittance.getTransmittanceFromAngle(voxel.angleMean, true);
//
//                if(coefficientGTheta == 0){
//                    LOGGER.error("Voxel : " + voxel.$i + " " + voxel.$j + " " + voxel.$k + " -> coefficient GTheta nul, angle = "+voxel.angleMean);
//                }
//                
//                pad1 = (float) (Math.log(voxel.transmittance) / (-coefficientGTheta));
//
//                if (Float.isNaN(pad1)) {
//                    pad1 = Float.NaN;
//                } else if (pad1 > MAX_PAD || Float.isInfinite(pad1)) {
//                    pad1 = MAX_PAD;
//                }
//
//            }
//
//        }
//
//        voxel.PadBVTotal = pad1 + 0.0f; //set +0.0f to avoid -0.0f
//
//        return voxel;
//    }
    
    public void computePADs(){
        
        for (int i = 0; i < parameters.infos.getSplit().x; i++) {
            for (int j = 0; j < parameters.infos.getSplit().y; j++) {
                for (int k = 0; k < parameters.infos.getSplit().z; k++) {

                    Voxel voxel = voxels[i][j][k];
                    voxels[i][j][k] = computePADFromVoxel(voxel, i, j, k);
                }
            }
        }
        
        padComputed = true;
    }
    
    public void write() throws FileNotFoundException, Exception{
        
        long start_time = System.currentTimeMillis();

        LOGGER.info("writing file: " + outputFile.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write(parameters.infos.headerToString()+"\n");
            writer.write(Voxel.getHeader(Voxel.class) + "\n");
            
            int count = 0;
            int nbLines = parameters.infos.getSplit().x * parameters.infos.getSplit().y * parameters.infos.getSplit().z;

            for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                for (int j = 0; j < parameters.infos.getSplit().y; j++) {
                    for (int k = 0; k < parameters.infos.getSplit().z; k++) {
                        
                        if(isCancelled()){
                            return;
                        }
                        
                        fireProgress("Writing file", count, nbLines);

                        Voxel voxel = voxels[i][j][k];
                        
                        if (!padComputed) {
                            voxel = computePADFromVoxel(voxel, i, j, k);
                        }
                        
                        writer.write(voxel.toString() + "\n");
                        
                        count++;
                    }
                }
            }

            writer.close();
            
            padComputed = true;

            LOGGER.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

        } catch (FileNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw e;
        }
    }
    
      
    
    public static void cleanIsolatedVoxels(File voxelFile, int nbEchosThreshold, int neighboursRange){
        
        //VoxelFileReader 
    }

    public void writeGroundEnergy() throws IOException {

        if (groundEnergy.length > 0 && groundEnergy[0].length > 0) {

            long start_time = System.currentTimeMillis();

            LOGGER.info("writing file: " + parameters.getGroundEnergyParams().getGroundEnergyFile().getAbsolutePath());

            try {

                if (parameters.getGroundEnergyParams().getGroundEnergyFileFormat() == GroundEnergyParams.FILE_FORMAT_PNG) {

                    BufferedImage image = new BufferedImage(parameters.infos.getSplit().x, parameters.infos.getSplit().y, BufferedImage.TYPE_INT_ARGB);

                    for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                        for (int j = 0; j < parameters.infos.getSplit().y; j++) {

                            float transmittance = groundEnergy[i][j].groundEnergyActual / groundEnergy[i][j].groundEnergyPotential;

                            Color c;

                            if (transmittance <= 1.0 && transmittance >= 0.0) {
                                c = new Color(ColorSpace.getInstance(ColorSpace.CS_GRAY), new float[]{transmittance}, 1.0f);

                            } else if (transmittance > 1.0) {
                                c = new Color(1.0f, 0.0f, 0.0f, 1.0f);
                            } else if (transmittance < 0.0) {
                                c = new Color(0.0f, 1.0f, 0.0f, 1.0f);
                            } else {
                                c = new Color(0.0f, 0.0f, 1.0f, 1.0f);
                            }

                            image.setRGB(i, parameters.infos.getSplit().y - 1 - j, c.getRGB());

                        }
                    }

                    ImageIO.write(image, "png", new File(parameters.getGroundEnergyParams().getGroundEnergyFile().getAbsolutePath()));

                } else {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(parameters.getGroundEnergyParams().getGroundEnergyFile()))) {
                        writer.write("i j groundEnergyActual groundEnergyPotential transmittance\n");

                        for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                            for (int j = 0; j < parameters.infos.getSplit().y; j++) {

                                float transmittance = groundEnergy[i][j].groundEnergyActual / groundEnergy[i][j].groundEnergyPotential;
                                writer.write(i + " " + j + " " + groundEnergy[i][j].groundEnergyActual + " " + groundEnergy[i][j].groundEnergyPotential + " " + transmittance + "\n");
                            }
                        }
                    }
                }

                LOGGER.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

            } catch (IOException ex) {
                throw ex;
            }

        } else {

            LOGGER.warn("Ground energy is set up but no values are available");
        }

    }

    public void createVoxelSpace() {

        try {
            
            if (parameters.getGroundEnergyParams() != null && 
                    parameters.getGroundEnergyParams().isCalculateGroundEnergy()) {
                
                groundEnergy = new GroundEnergy[parameters.infos.getSplit().x][parameters.infos.getSplit().y];
            }
            
            // allocate voxels
            LOGGER.info("allocate!!!!!!!!");

            voxels = new Voxel[parameters.infos.getSplit().x][parameters.infos.getSplit().y][parameters.infos.getSplit().z];

            try {

                if (parameters.getGroundEnergyParams() != null && 
                        parameters.getGroundEnergyParams().isCalculateGroundEnergy() && parameters.infos.getType() != Type.TLS) {
                    
                    for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                        for (int j = 0; j < parameters.infos.getSplit().y; j++) {
                            groundEnergy[i][j] = new GroundEnergy();
                        }
                    }
                }

            } catch (OutOfMemoryError ex) {
                throw new Exception("Unsufficient memory, you need to allocate more, change the Xmx value!", ex);
            } catch (Exception ex) {
                throw new Exception("Error during instantiation of voxel space: ", ex);
            }

            Scene scene = new Scene();
            scene.setBoundingBox(new BoundingBox3d(parameters.infos.getMinCorner(), parameters.infos.getMaxCorner()));

            voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.infos.getSplit(), VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

            LOGGER.info(voxelManager.getInformations());

        } catch (Exception e) {
            LOGGER.error(e + " " + this.getClass().getName());
        }

    }

    private Voxel initVoxel(int i, int j, int k) {

        Voxel vox = new Voxel(i, j, k);

        Point3d position = getPosition(new Point3i(i, j, k),
                parameters.infos.getSplit(), parameters.infos.getMinCorner());

        float dist;

        if (dtm != null && parameters.getDtmFilteringParams().useDTMCorrection()) {

            float dtmHeightXY = dtm.getSimpleHeight((float) position.x, (float) position.y);
            if (Float.isNaN(dtmHeightXY)) {
                dist = (float) (position.z);
            } else {
                dist = (float) (position.z - dtmHeightXY);
            }

        } else {
            dist = (float) (position.z - parameters.infos.getMinCorner().z);
        }

        vox.ground_distance = dist;

        return vox;
    }

    public int getNbShotsProcessed() {
        return nbShotsProcessed;
    }

    public Voxel[][][] getVoxels() {
        return voxels;
    }

    public ShotFilter getShotFilter() {
        return shotFilter;
    }

    public void setShotFilter(ShotFilter shotFilter) {
        this.shotFilter = shotFilter;
    }

    public EchoFilter getEchoFilter() {
        return echoFilter;
    }

    public void setEchoFilter(EchoFilter echoFilter) {
        this.echoFilter = echoFilter;
    }

    public List<PointcloudFilter> getPointcloudFilters() {
        return pointcloudFilters;
    }

    public void setPointcloudFilters(List<PointcloudFilter> pointcloudFilters) {
        this.pointcloudFilters = pointcloudFilters;
    }

    public Raster getDtm() {
        return dtm;
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}