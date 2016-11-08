package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.util.TimeCounter;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.raytracing.util.BoundingBox3d;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.Scene;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManager;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManagerSettings;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.raster.multiband.BCommon;
import fr.amap.commons.raster.multiband.BHeader;
import fr.amap.commons.raster.multiband.BSQ;
import fr.amap.commons.util.Cancellable;
import fr.amap.commons.util.Process;
import fr.amap.lidar.amapvox.commons.DirectionalTransmittance;
import fr.amap.lidar.amapvox.commons.LADParams;
import fr.amap.lidar.amapvox.commons.LeafAngleDistribution;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos.Type;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg.VoxelsFormat;
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
import java.math.BigInteger;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

public class VoxelAnalysis extends Process implements Cancellable{

    /*public enum LaserSpecification{
        
        LMS_Q560(0.0003, 0.0005),
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
        
    }*/
    
    private final static Logger LOGGER = Logger.getLogger(VoxelAnalysis.class);
    
    private boolean cancelled;

    private Voxel voxels[][][];
    private VoxelManager voxelManager;

    private float MAX_PAD = 3;

    private int nbShotsProcessed;
    private int tmpNbShotsProcessed = 0;

    private float[][] weighting;
    private float[][] residualEnergyTable;
    
    private final boolean volumeWeighting = true;
    
    private int pathLengthMode = 1; //1 = mode A, 2 = mode B
    private int transMode = 1;

    private GroundEnergy[][] groundEnergy;
    private VoxelParameters parameters;
    private VoxelAnalysisCfg cfg;

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
    
    private BufferedWriter shotSegmentWriter;
    
    //directional transmittance (GTheta)
    private DirectionalTransmittance direcTransmittance;
    
    //tmp
    /*boolean[][][] valid;
    BufferedWriter writer;*/
    
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
        }else{
            distance = z;
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
        
        this.cfg = cfg;
        
        init(cfg.getVoxelParameters());
    }


    private void init(VoxelParameters parameters) {

        this.parameters = parameters;
        this.parameters.infos.setTransmittanceMode(parameters.getTransmittanceMode());
        this.parameters.infos.setPathLengthMode(parameters.getPathLengthMode());
        
        if(parameters.getEchoesWeightParams().getWeightingMode() != EchoesWeightParams.WEIGHTING_NONE){
            weighting = parameters.getEchoesWeightParams().getWeightingData();
            generateResidualEnergyTable();
        }else{
            
            //pas de pondération
            generateNoPonderationTables();
        }
        
        MAX_PAD = parameters.infos.getMaxPAD();
        this.transMode = parameters.getTransmittanceMode();
        
        
        String pathLengthModeStr = parameters.getPathLengthMode();
        if(pathLengthModeStr.equals("A")){
            pathLengthMode = 1;
        }else{
            pathLengthMode = 2;
        }
        
        //test
        /*pathLengthMode = 2;
        this.transMode = 1;*/
        
        laserSpec = parameters.getLaserSpecification();
        
        if(laserSpec == null){
            if(parameters.infos.getType() == VoxelSpaceInfos.Type.TLS){
                laserSpec = LaserSpecification.VZ_400;
            }else{
                laserSpec = LaserSpecification.LMS_Q560;
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
        
        if(cfg != null && cfg.isExportShotSegment()){
            try {
                shotSegmentWriter = new BufferedWriter(new FileWriter(new File(cfg.getOutputFile().getAbsolutePath()+".segments")));
                shotSegmentWriter.write("i j k norm_transmittance weight\n");
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(VoxelAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * Get position of the center of a voxel
     * @param indices
     * @return
     */
    public Point3d getPosition(Point3i indices) {
        
        Point3d minCorner = parameters.infos.getMinCorner();
        Point3d voxSize = voxelManager.getVoxelSpace().getVoxelSize();
        
        double posX = minCorner.x + (voxSize.x / 2.0d) + (indices.x * voxSize.x);
        double posY = minCorner.y + (voxSize.y / 2.0d) + (indices.y * voxSize.y);
        double posZ = minCorner.z + (voxSize.z / 2.0d) + (indices.z * voxSize.z);

        return new Point3d(posX, posY, posZ);
    }

    public void processOneShot(final Shot shot) throws IOException {

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

            if (shot.getEchoesNumber() == 0) {

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

                for (int i = 0; i < shot.getEchoesNumber(); i++) {

                    Point3d nextEcho = null;

                    if (i < shot.getEchoesNumber() - 1) {
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
                        lastEchoBeamFraction += weighting[shot.getEchoesNumber() - 1][i];

                        if(!wasMultiple){
                            firstEchoOfVoxel = i;
                        }

                    } else {

                        if (parameters.getEchoesWeightParams().getWeightingMode() != EchoesWeightParams.WEIGHTING_NONE) {
                            beamFraction = weighting[shot.getEchoesNumber() - 1][i] + lastEchoBeamFraction;

                            if(wasMultiple){
                                residualEnergy = residualEnergyTable[shot.getEchoesNumber() - 1][firstEchoOfVoxel];
                            }else{
                                residualEnergy = residualEnergyTable[shot.getEchoesNumber() - 1][i];
                            }

                        }else{
                            beamFraction = 1;
                            residualEnergy = 1;
                        }

                        lastEchoBeamFraction = 0;

                        boolean lastEcho;

                        lastEcho = i == shot.getEchoesNumber() - 1;

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
            
            
            //instantiate on the fly, when the voxel is crossed
            if (voxels[indices.x][indices.y][indices.z] == null) {
                voxels[indices.x][indices.y][indices.z] = initVoxel(indices.x, indices.y, indices.z);
            }

            Voxel vox = voxels[indices.x][indices.y][indices.z];
            
            double surface;

            //recalculé pour éviter le stockage de trois doubles (24 octets) par voxel.
            Point3d voxelPosition = getPosition(new Point3i(indices.x, indices.y, indices.z));
            double distance = voxelPosition.distance(shot.origin);
            
            //don't continue the propagation if the current sampled voxel is below the ground
            if(parameters.getGroundEnergyParams() == null || 
                            !parameters.getGroundEnergyParams().isCalculateGroundEnergy()){
                if(vox.ground_distance < voxelManager.getVoxelSpace().getVoxelSize().z/2.0f){
                    break;
                }
            }
            
            //surface de la section du faisceau à la distance de la source
            if (parameters.getEchoesWeightParams().getWeightingMode() != EchoesWeightParams.WEIGHTING_NONE && volumeWeighting) {
                surface = Math.pow((Math.tan(laserSpec.getBeamDivergence() / 2.0) * distance) + laserSpec.getBeamDiameterAtExit(), 2) * Math.PI;
            } else {
                surface = 1;
            }
            
            /*Si un écho est positionné sur une face du voxel alors il est considéré
             comme étant à l'extérieur du dernier voxel traversé*/
            
            double surfMulLength = 0;
            double surfMulLengthMulEnt = 0; //CL
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

                    vox.angleMean += shot.getAngle();
                    
                    //double volume = longueur * ONE_THIRD_OF_PI * ((r*r)+(R*R)+(r*R));
                    //vox.bvEntering += volume * (Math.round(residualEnergy*10000)/10000.0);
                    
                    surfMulLength = surface * longueur;
                    entering = (Math.round(residualEnergy*10000)/10000.0);
                    surfMulLengthMulEnt = entering * surfMulLength; //CL
                    vox.bvEntering += (entering * surfMulLength);

                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                    
                    test = true;
                    vox._valid = false;
                }
                
                /*
                 Si l'écho est sur la face sortante du voxel, 
                 on n'incrémente pas le compteur d'échos
                 */
            } /*
             Poursuite du trajet optique jusqu'à sortie de la bounding box
             */ 
            else if (d1 >= distanceToHit) {

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
                            context = null; // sortie de la boucle 
                        }

                    } else {
                        context = null;// sortie de la boucle 
                    }

                }

            } /*
             Echo dans le voxel
             */ 
            else {

                /*si plusieurs échos issus du même tir dans le voxel, 
                 on incrémente et le nombre de tirs entrants (nbsampling) 
                 et le nombre d'interception (interceptions) 
                 et la longueur parcourue(lgInterception)*/
                /*
                 * Si distanceToHit == d1,on incrémente le compteur d'échos
                 */
                 
                

                //si l'écho n'est pas un dernier écho mais au niveau distance ne sort pas du voxel courant, alors la longueur sera surestimé
                if (lastEcho) {
                    if(pathLengthMode == 1){
                        longueur = (distanceToHit - d1);
                    }else{
                        longueur = (d2 - d1); //test
                    }
                    vox._valid = false;
                } else {
                    longueur = (d2 - d1);
                }
                    
                if(shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox){
                    //pour n'échantillonner qu'une fois le voxel pour un tir
                }else{
                    
                    test = true;
                    
                    vox.nbSampling++;

                    vox.lgTotal += longueur;

                    vox.angleMean += shot.getAngle();
                    
                    surfMulLength = surface * longueur;
                    entering = (Math.round(residualEnergy*10000)/10000.0);
                    surfMulLengthMulEnt = entering * surfMulLength; //CL
                    vox.bvEntering += (entering * surfMulLength);
                    
                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                }
                
                if (keepEcho){

                /*if ((keepEchoOfShot(shot, echoID))
                        && ((echoDistance >= parameters.minDTMDistance && echoDistance != Float.NaN && parameters.useDTMCorrection()) || !parameters.useDTMCorrection())
                        && keepEcho) {*/
                
                    test = true;

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
            
            /*if(test && valid[vox.$i][vox.$j][vox.$k]){
                try {
                    writer.write(vox.$i+"\t"+vox.$j+"\t"+vox.$k+"\t"+entering+"\t"+intercepted+"\t"+longueur+"\t"+surface+"\n");
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(VoxelAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                }
            }*/
            
            if(test && (transMode == 2 || transMode == 3)){
                double transNorm;
                
                if(transMode == 2){
                    transNorm = ((entering - intercepted) / entering) * surfMulLength;
                }else{ //mode 3
                    
                    if(longueur == 0){
                        transNorm = 0;
                    }else{
                        transNorm = Math.pow(((entering - intercepted) / entering), 1/longueur) * surfMulLengthMulEnt;
                    }
                }
                
                vox.transmittance_tmp += transNorm;
                
                vox.sumSurfMulLengthMulEnt += surfMulLengthMulEnt; //CL

                vox.sumSurfMulLength += surfMulLength;
                
                if(cfg.isExportShotSegment()){
                    double currentNormalizedTrans = transNorm/surfMulLengthMulEnt;
                    try {
                        shotSegmentWriter.write(vox.$i+" "+vox.$j+" "+vox.$k+" "+currentNormalizedTrans+" "+surfMulLengthMulEnt+"\n");
                    } catch (IOException ex) {
                        java.util.logging.Logger.getLogger(VoxelAnalysis.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            
        }

    }
    
    private boolean keepEchoOfShot(Shot shot, int echoID){
        
        if(shot.getMask() == null){
            return true;
        }
        
        return shot.getMask()[echoID];
    }
    
    public static float computeTransmittance(double bvEntering, double bvIntercepted){
        
        float transmittance;
        
        if (bvEntering == 0) {

            transmittance = Float.NaN;
            
        }else if(bvIntercepted > bvEntering){
            transmittance = Float.NaN;
            
        }else {
            transmittance = (float) ((bvEntering - bvIntercepted) / bvEntering);
        }
        
        return transmittance;
    }
    
    public static float computeNormTransmittanceMode2(double transmittance, double _sumSurfMulLength, double lMeanTotal){
        float normalizedTransmittance = (float) Math.pow((transmittance / _sumSurfMulLength), 1 / lMeanTotal);
        return normalizedTransmittance;
    }
    
    public static float computeNormTransmittanceMode3(double transmittance, double _sumSurfMulLength){
        float normalizedTransmittance = (float) (transmittance / _sumSurfMulLength);
        return normalizedTransmittance;
    }
    
    public static float computeNormTransmittance(double transmittance, double lMeanTotal){
        float normalizedTransmittance = (float) Math.pow(transmittance, 1 / lMeanTotal);
        return normalizedTransmittance;
    }
    
    public static float computePADFromNormTransmittance(float transmittance, float angleMean, float maxPAD, DirectionalTransmittance direcTransmittance){
        
        float pad;
        
        if (Float.isNaN(transmittance)) {

            pad = Float.NaN;
            
        }else {

            if (transmittance == 0) {

                pad = maxPAD;

            }else {

                float coefficientGTheta = (float) direcTransmittance.getTransmittanceFromAngle(angleMean, true);
                
                pad = (float) (Math.log(transmittance) / (-coefficientGTheta));

                if (Float.isNaN(pad)) {
                    pad = Float.NaN;
                } else if (pad > maxPAD || Float.isInfinite(pad)) {
                    pad = maxPAD;
                }

            }

        }

        return pad + 0.0f; //set +0.0f to avoid -0.0f
    }
    
    public float computePADFromNormTransmittance(float transmittance, float angleMean){
        
        return computePADFromNormTransmittance(transmittance, angleMean, MAX_PAD, direcTransmittance);
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
                normalizedTransmittance = computeNormTransmittanceMode2(voxel.transmittance_tmp, voxel.sumSurfMulLength, voxel.lMeanTotal);
                break;
            case 3:
                //normalizedTransmittance = computeNormTransmittanceV2(voxel.transmittance_tmp, voxel.sumSurfMulLength);
                normalizedTransmittance = computeNormTransmittanceMode3(voxel.transmittance_tmp, voxel.sumSurfMulLengthMulEnt); //CL
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
    
    public void write(VoxelsFormat format, File outputFile) throws FileNotFoundException, Exception{
        
        //tmp
        //writer.close();
        
        if(cfg.isExportShotSegment()){
            shotSegmentWriter.close();
        }
        
        long start_time = System.currentTimeMillis();

        LOGGER.info("writing file: " + outputFile.getAbsolutePath());
        
        if(format == VoxelsFormat.NONE){
            return;
        }else if(format == VoxelsFormat.VOXEL){
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
                            
                            /*if(voxel._valid && voxel.nbSampling > 0){
                                System.out.println(voxel.toString());
                            }*/

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
        }else if(format == VoxelsFormat.RASTER){

            float scale = 1.0f;
            float resolution = parameters.infos.getResolution();

            int rasterXSize = (int) (Math.ceil(parameters.infos.getSplit().x / scale));
            int rasterYSize = (int) (Math.ceil(parameters.infos.getSplit().y / scale));

            BHeader header = new BHeader(rasterXSize, rasterYSize, 1, BCommon.NumberOfBits.N_BITS_32);
            header.setUlxmap(parameters.infos.getMinCorner().x + (resolution / 2.0f));
            header.setUlymap(parameters.infos.getMaxCorner().y - (resolution / 2.0f));
            header.setXdim((float) parameters.infos.getVoxelSize().x);
            header.setYdim((float) parameters.infos.getVoxelSize().y);

            BSQ raster = new BSQ(outputFile, header);

            long valMax = 4294967294L;
            long valNoData = 4294967295L;
            
            double laiMax = 50000;
            
            header.addMetadata("NO_DATA", String.valueOf(valNoData));
            header.addMetadata("MAX_VAL", String.valueOf(valMax));
            header.addMetadata("MAX_VEG_M2", String.valueOf(laiMax));
            

            for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                for (int j = 0 ; j < parameters.infos.getSplit().y; j++) {
                    
                    double laiSum = 0;
                    
                    for (int k = parameters.infos.getSplit().z - 1; k >= 0; k--) {

                        Voxel vox = voxels[i][j][k];

                        if (vox != null && vox.ground_distance >= 0 && vox.PadBVTotal > 0 && !Float.isNaN(vox.PadBVTotal)) {
                            
                            //calcul de la position de départ
                            Point3d voxelPosition = getPosition(new Point3i(i, j, k));
                            
                            Point3d subVoxelSize = new Point3d(parameters.infos.getVoxelSize().x / parameters.infos.getResolution(),
                                                                parameters.infos.getVoxelSize().y / parameters.infos.getResolution(),
                                                                parameters.infos.getVoxelSize().z / parameters.infos.getResolution());
                            
                            Point3d startPosition = new Point3d(voxelPosition.x - (parameters.infos.getVoxelSize().x - subVoxelSize.x)/2.0,
                                                                voxelPosition.y - (parameters.infos.getVoxelSize().y - subVoxelSize.y)/2.0,
                                                                voxelPosition.z - (parameters.infos.getVoxelSize().z - subVoxelSize.z)/2.0);

                            Point3i nbSubVoxels = new Point3i((int)Math.ceil(parameters.infos.getVoxelSize().x),
                                                                (int)Math.ceil(parameters.infos.getVoxelSize().y),
                                                                (int)Math.ceil(parameters.infos.getVoxelSize().z));

                            //parcours des sous-voxels
                            int nbSubVoxelsAboveGround = 0;

                            for (int i2 = 0; i2 < nbSubVoxels.x; i2++) {
                                for (int j2 = 0; j2 < nbSubVoxels.y; j2++) {
                                    for (int k2 = nbSubVoxels.z - 1; k2 >= 0; k2--) {

                                        float groundDistance = getGroundDistance((float)(startPosition.x + (i2 * subVoxelSize.x)),
                                                                                (float)(startPosition.y + (j2 * subVoxelSize.y)),
                                                                                (float)(startPosition.z + (k2 * subVoxelSize.z)));

                                        if(groundDistance > 0){
                                            nbSubVoxelsAboveGround++;
                                        }
                                    }
                                }
                            }
                            
                            float volume = (float) (subVoxelSize.x * subVoxelSize.y * subVoxelSize.z * nbSubVoxelsAboveGround);
                            double lai = vox.PadBVTotal * volume;

                            laiSum += lai;
                        }
                    }
                    
                    long value = (long) ((laiSum / laiMax) * (valMax));
                    
                    String binaryString = Long.toBinaryString(value);
                    byte[] bval = new BigInteger(binaryString, 2).toByteArray();
                    ArrayUtils.reverse(bval);
                    byte b0 = 0x0, b1 = 0x0, b2 = 0x0, b3 = 0x0;
                    if (bval.length > 0) {
                        b0 = bval[0];
                    }
                    if (bval.length > 1) {
                        b1 = bval[1];
                    }
                    if (bval.length > 2) {
                        b2 = bval[2];
                    }
                    if (bval.length > 3) {
                        b3 = bval[3];
                    }

                    raster.setPixel(i, parameters.infos.getSplit().y - j - 1, 0, b0, b1, b2, b3);
                }
            }

            raster.writeHeader();
            raster.writeImage();
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

            if (parameters.getGroundEnergyParams() != null && 
                    parameters.getGroundEnergyParams().isCalculateGroundEnergy() && parameters.infos.getType() != Type.TLS) {

                for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                    for (int j = 0; j < parameters.infos.getSplit().y; j++) {
                        groundEnergy[i][j] = new GroundEnergy();
                    }
                }
            }

            Scene scene = new Scene();
            scene.setBoundingBox(new BoundingBox3d(parameters.infos.getMinCorner(), parameters.infos.getMaxCorner()));

            voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.infos.getSplit(), VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

            LOGGER.info(voxelManager.getInformations());

            //tmp, read valid voxels
        
            /*valid = new boolean[parameters.infos.getSplit().x][parameters.infos.getSplit().y][parameters.infos.getSplit().z];
            try {
                BufferedReader reader = new BufferedReader(new FileReader(new File("/home/julien/Documents/pour_greg/indices.txt")));

                String line;
                while((line = reader.readLine()) != null){
                    String[] split = line.split(" ");
                    valid[Integer.valueOf(split[0])][Integer.valueOf(split[1])][Integer.valueOf(split[2])] = true;
                }
                
                reader.close();
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(VoxelAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(VoxelAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            writer = new BufferedWriter(new FileWriter(new File("/home/julien/Documents/pour_greg/segments.txt")));*/

        } catch (Exception e) {
            LOGGER.error(e + " " + this.getClass().getName());
        }

    }

    private Voxel initVoxel(int i, int j, int k) {

        Voxel vox = new Voxel(i, j, k);

        Point3d position = getPosition(new Point3i(i, j, k));

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