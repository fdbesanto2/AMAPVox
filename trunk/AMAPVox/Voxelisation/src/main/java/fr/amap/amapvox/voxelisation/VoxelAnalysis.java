package fr.amap.amapvox.voxelisation;

import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.math.point.Point3I;
import fr.amap.amapvox.commons.util.Filter;
import fr.amap.amapvox.commons.util.TimeCounter;
import fr.amap.amapvox.datastructure.octree.Octree;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jeeb.raytracing.geometry.Intersection;
import fr.amap.amapvox.jeeb.raytracing.geometry.LineElement;
import fr.amap.amapvox.jeeb.raytracing.geometry.LineSegment;
import fr.amap.amapvox.jeeb.raytracing.util.BoundingBox3d;
import fr.amap.amapvox.jeeb.raytracing.voxel.Scene;
import fr.amap.amapvox.jeeb.raytracing.voxel.VoxelManager;
import fr.amap.amapvox.jeeb.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.amapvox.jeeb.raytracing.voxel.VoxelManagerSettings;
import fr.amap.amapvox.jeeb.raytracing.voxel.VoxelSpace;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.jraster.braster.BCommon;
import fr.amap.amapvox.jraster.braster.BHeader;
import fr.amap.amapvox.jraster.braster.BSQ;
import fr.amap.amapvox.voxcommons.VoxTool;
import fr.amap.amapvox.voxelisation.configuration.VoxCfg;
import fr.amap.amapvox.voxelisation.configuration.VoxelParameters;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

public class VoxelAnalysis {
    
    final static boolean DEBUG = false;

    public enum LaserSpecification{
        
        DEFAULT_ALS(0.0003, 0.0005),
        VZ_400(0.007, 0.00035),
        LEICA_SCANSTATION_P30_40(0.0035, 0.00023);
    
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
    
    private final static Logger logger = Logger.getLogger(VoxelAnalysis.class);

    private VoxelSpace voxSpace;
    private Voxel voxels[][][];
    private VoxelManager voxelManager;

    private float MAX_PAD = 3;

    private int nbShotsProcessed;
    private File outputFile;

    private float[][] weighting;
    private float[][] residualEnergyTable;

    private GroundEnergy[][] groundEnergy;
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int nbEchosSol = 0;
    public VoxelParameters parameters;
    private VoxCfg cfg;

    private boolean isSet = false;

    private RegularDtm terrain;
    private List<Octree> pointcloudList;

    private int lastShot = 0;
    private boolean shotChanged = false;
    private Point3d lastEchotemp = new Point3d();
    private Voxel lastVoxelSampled;
    private int lastShotId;
    
    private LaserSpecification laserSpec;
    private static final double ONE_THIRD_OF_PI = Math.PI/3.0;
    

    private boolean padWasCalculated;

    int nbSamplingTotal = 0;
    
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

        if (terrain != null && parameters.useDTMCorrection()) {
            distance = z - (float) (terrain.getSimpleHeight(x, y));
        }

        return distance;
    }
            
    public VoxelAnalysis(RegularDtm terrain, List<Octree> pointcloud, VoxCfg cfg) {

        nbShotsProcessed = 0;
        this.terrain = terrain;
        this.pointcloudList = pointcloud;
        Shot.setFilters(cfg.getFilters());
        this.cfg = cfg;
    }

    /**
     * Get position of center of a voxel
     * @param indices
     * @param splitting
     * @param minCorner
     * @return
     */
    public Point3d getPosition(Point3i indices, Point3i splitting, Point3d minCorner) {
        /*
         Point3d resolution = new Point3d();
         resolution.x = (maxCorner.x - minCorner.x) / splitting.x;
         resolution.y = (maxCorner.y - minCorner.y) / splitting.y;
         resolution.z = (maxCorner.z - minCorner.z) / splitting.z;
         */
        
        double posX = minCorner.x + (parameters.resolution / 2.0d) + (indices.x * parameters.resolution);
        double posY = minCorner.y + (parameters.resolution / 2.0d) + (indices.y * parameters.resolution);
        double posZ = minCorner.z + (parameters.resolution / 2.0d) + (indices.z * parameters.resolution);

        return new Point3d(posX, posY, posZ);
    }

    public void init(VoxelParameters parameters, File outputFile) {

        this.parameters = parameters;
        this.outputFile = outputFile;
        
        if(parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE){
            weighting = parameters.getWeightingData();
            generateResidualEnergyTable();
        }else{
            
            //pas de pondération
            generateNoPonderationTables();
        }
        
        MAX_PAD = parameters.getMaxPAD();
        
        if(parameters.isTLS()){
            laserSpec = LaserSpecification.VZ_400;
        }else{
            laserSpec = LaserSpecification.DEFAULT_ALS;
        }

        LeafAngleDistribution distribution = new LeafAngleDistribution(parameters.getLadType(), 
                                                                        parameters.getLadBetaFunctionAlphaParameter(),
                                                                        parameters.getLadBetaFunctionBetaParameter());
        
        direcTransmittance = new DirectionalTransmittance(distribution);
        
        logger.info("Building transmittance functions table");
        direcTransmittance.buildTable(DirectionalTransmittance.DEFAULT_STEP_NUMBER);
        logger.info("Transmittance functions table is built");
    }

    public void processOneShot(final Shot shot) {

        if (voxelManager == null) {
            logger.error("VoxelManager not initialized, what happened??");
            return;
        }

        if (shot != null && shot.doFilter()/* && shot.ranges.length > 1*/) {

            if (nbShotsProcessed % 1000000 == 0 && nbShotsProcessed != 0) {
                logger.info("Shots processed: " + nbShotsProcessed);
            }
            
            count1 += shot.nbEchos;

            shot.direction.normalize();
            Point3d origin = new Point3d(shot.origin);

            if (shot.nbEchos == 0) {

                LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);
                Point3d echo = new Point3d(seg.getEnd());
                propagate(origin, echo, 1, 1, shot.origin, false, shot.angle, nbShotsProcessed, shot, -1);

            } else {

                double beamFraction = 1;

                /*if (!parameters.isTLS()) {
                    for (int i = 0; i < shot.nbEchos; i++) {
                        sumIntensities += shot.intensities[i];
                    }
                }*/
                
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

                    /*switch (parameters.getWeighting()) {

                        case VoxelParameters.WEIGHTING_FRACTIONING:
                            if (shot.nbEchos == 1) {
                                //bfIntercepted = 1;
                            } else {
                                //bfIntercepted = (shot.intensities[i]) / (double) sumIntensities;
                            }

                            break;

                        case VoxelParameters.WEIGHTING_ECHOS_NUMBER:
                        case VoxelParameters.WEIGHTING_FILE:

                            //beamFraction = weighting[shot.nbEchos - 1][i];

                            break;

                        default:
                            //bfIntercepted = 1;
                            break;

                    }*/
                    
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

                        if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {
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
                        if (parameters.isTLS()) {
                            propagate(origin, echo, beamFraction, residualEnergy, shot.origin, lastEcho, shot.angle, nbShotsProcessed, shot, i);
                        } else {
                            propagate(origin, echo, beamFraction, residualEnergy, shot.origin, lastEcho, shot.angle, nbShotsProcessed, shot, i);
                        }

                        origin = new Point3d(echo);
                    }

                    

                }
            }

            nbShotsProcessed++;
            
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
     * @param beamFraction
     * @param source shot origin
     */
    private void propagate(Point3d origin, Point3d echo, double beamFraction, double residualEnergy, Point3d source, boolean lastEcho, double angle, int shotID, Shot shot, int echoID) {

        //get shot line
        LineElement lineElement = new LineSegment(origin, echo);

        //get the first voxel cross by the line
        VoxelCrossingContext context = voxelManager.getFirstVoxelV2(lineElement);

        double distanceToHit = lineElement.getLength();

        //calculate ground distance
        float echoDistance = getGroundDistance((float) echo.x, (float) echo.y, (float) echo.z);
        boolean keepEcho = true;

        if (parameters.isUsePointCloudFilter() && pointcloudList != null) {

            Point3F point = new Point3F((float) echo.x, (float) echo.y, (float) echo.z);

            int count = 0;
            boolean test;

            for (Octree octree : pointcloudList) {

                test = octree.isPointBelongsToPointcloud(point, parameters.getPointcloudFilters().get(count).getPointcloudErrorMargin(), Octree.INCREMENTAL_SEARCH);

                if (parameters.getPointcloudFilters().get(count).isKeep()) {
                    keepEcho = test;
                } else {
                    if (test) {
                        keepEcho = false;
                    }
                }
                count++;
            }
        }

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
            double distance = getPosition(new Point3i(indices.x, indices.y, indices.z), parameters.split, parameters.bottomCorner).distance(source);
            
            //surface de la section du faisceau à la distance de la source
            if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {
                surface = Math.pow((Math.tan(laserSpec.getBeamDivergence() / 2.0) * distance) + laserSpec.getBeamDiameterAtExit(), 2) * Math.PI; //TODO: récupérer taille faisceau sortie
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
            /*
            
                
             /*
             * Si d2 < distanceToHit le voxel est traversé sans interceptions
             */
            if (d2 < distanceToHit) {

                if(shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox){
                    //pour n'échantillonner qu'une fois le voxel pour un tir
                }else{
                    double longueur = d2 - d1;
                    
                    vox.lgTotal += longueur;
                
                    vox.nbSampling++;
                    nbSamplingTotal++;

                    vox.angleMean += angle;
                    
                    //double volume = longueur * ONE_THIRD_OF_PI * ((r*r)+(R*R)+(r*R));
                    //vox.bvEntering += volume * (Math.round(residualEnergy*10000)/10000.0);
                    
                    vox.bvEntering += surface * (Math.round(residualEnergy*10000)/10000.0) * longueur;

                    lastVoxelSampled = vox;
                    lastShotId = shotID;
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

                    if (parameters.isCalculateGroundEnergy() && !parameters.isTLS()) {

                        if (vox.ground_distance < parameters.minDTMDistance) {
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
                 
                double longueur;

                if (lastEcho) {
                    longueur = (distanceToHit - d1);
                } else {
                    longueur = (d2 - d1);
                }
                    
                if(shotID == lastShotId && lastVoxelSampled != null && lastVoxelSampled == vox){
                    //pour n'échantillonner qu'une fois le voxel pour un tir
                }else{
                    vox.nbSampling++;

                    nbSamplingTotal++;

                    vox.lgTotal += longueur;

                    vox.angleMean += angle;

                    double entering;
                    //entering = (surface/(Math.round(residualEnergy*10000)/10000.0)) * longueur;
                    entering = surface * (Math.round(residualEnergy*10000)/10000.0) * longueur;
                    vox.bvEntering += entering;
                    
                    lastVoxelSampled = vox;
                    lastShotId = shotID;
                }
                

                double intercepted = 0;

                if ((keepEchoOfShot(shot, echoID))
                        && ((echoDistance >= parameters.minDTMDistance && echoDistance != Float.NaN && parameters.useDTMCorrection()) || !parameters.useDTMCorrection())
                        && keepEcho) {

                    if(!lastEcho){
                        vox._lastEcho = false;
                    }
                    vox.nbEchos++;

                    intercepted = surface * (Math.round(beamFraction*10000)/10000.0) * longueur;
                    vox.bvIntercepted += intercepted;

                } else {

                    if (parameters.isCalculateGroundEnergy() && !parameters.isTLS() && !isSet) {
                        groundEnergy[vox.$i][vox.$j].groundEnergyActual += residualEnergy;
                        groundEnergy[vox.$i][vox.$j].groundEnergyPotential++;

                        isSet = true;
                    }
                }
                
                lastEchotemp = echo;
            }
        }

    }
    
    private boolean keepEchoOfShot(Shot shot, int echoID){
        
        if(shot.classifications != null && shot.classifications[echoID] != 2 && !parameters.isTLS()){
            return true;
        }else if(parameters.isTLS() && cfg.getEchoFilters() != null){
            
            List<Filter> echoFilters = cfg.getEchoFilters();
            
            for(Filter filter : echoFilters){
                
                switch(filter.getVariable()){
                    case "Reflectance":
                        
                        if(shot.reflectances != null){
                            return filter.doFilter(shot.reflectances[echoID]);
                        }
                        
                    case "Amplitude":
                        
                        if(shot.amplitudes != null){
                            return filter.doFilter(shot.amplitudes[echoID]);
                        }
                        
                    case "Deviation":
                        
                        if(shot.deviations != null){
                            return filter.doFilter(shot.deviations[echoID]);
                        }
                }
            }
            
        }
        
        return true;
    }

    private class Mean {

        public float sum;
        public int count;

        public Mean() {
            sum = 0;
            count = 0;
        }
    }

    public void generateMultiBandsRaster(File outputFile, float startingHeight, float step, int bandNumber, int resolution) {

        float[] altitudes = new float[bandNumber];
        for (int i = 0; i < bandNumber; i++) {
            altitudes[i] = startingHeight + (i * step);
        }

        float scale = (float) (resolution / parameters.resolution);

        int rasterXSize = (int) (Math.ceil(voxSpace.getSplitting().x / scale));
        int rasterYSize = (int) (Math.ceil(voxSpace.getSplitting().y / scale));

        BHeader header = new BHeader(rasterXSize, rasterYSize, altitudes.length, BCommon.NumberOfBits.N_BITS_32);
        header.setUlxmap(voxSpace.getBoundingBox().getMin().x + (resolution / 2.0f));
        header.setUlymap(voxSpace.getBoundingBox().getMin().y - (parameters.resolution / 2.0f) + (voxSpace.getSplitting().y * parameters.resolution));
        header.setXdim(resolution);
        header.setYdim(resolution);

        BSQ raster = new BSQ(outputFile, header);

        Mean[][][] padMean = new Mean[rasterXSize][rasterYSize][altitudes.length];

        if (terrain != null) {

            if (altitudes.length > 0) {

                float altitudeMin = altitudes[0];

                for (int i = 0; i < parameters.split.x; i++) {
                    for (int j = parameters.split.y - 1; j >= 0; j--) {
                        for (int k = 0; k < parameters.split.z; k++) {

                            Voxel vox;

                            if (!padWasCalculated) {
                                voxels[i][j][k] = calculatePAD(voxels[i][j][k], i, j, k);
                            }

                            vox = voxels[i][j][k];

                            //on calcule l'indice de la couche auquel appartient le voxel
                            if (vox != null && vox.ground_distance > altitudeMin) {
                                int layer = (int) ((vox.ground_distance - altitudeMin) / step);

                                if (layer < altitudes.length) {

                                    int indiceI = (int) (i / scale);
                                    int indiceJ = (int) (j / scale);

                                    if (padMean[indiceI][indiceJ][layer] == null) {
                                        padMean[indiceI][indiceJ][layer] = new Mean();
                                    }

                                    if (!Float.isNaN(vox.PadBVTotal)) {
                                        padMean[indiceI][indiceJ][layer].sum += vox.PadBVTotal;
                                        padMean[indiceI][indiceJ][layer].count++;
                                    }
                                }
                            }
                        }
                    }
                }

                padWasCalculated = true;

                long l = 4294967295L;

                try {
                    //on écrit la moyenne
                    for (int i = 0; i < rasterXSize; i++) {
                        for (int j = rasterYSize - 1, j2 = 0; j >= 0; j--, j2++) {
                            for (int k = 0; k < altitudes.length; k++) {

                                if (padMean[i][j][k] != null) {

                                    float meanOfPAD = padMean[i][j][k].sum / padMean[i][j][k].count;
                                    //float value = (meanOfPAD-0)/(MAX_PAD-0);

                                    long value = (long) (((double) meanOfPAD / (double) MAX_PAD) * (l));
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
                                    raster.setPixel(i, j2, k, b0, b1, b2, b3);

                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    logger.error(ex);
                }

                try {
                    raster.writeImage();
                    raster.writeHeader();
                } catch (IOException ex) {
                    logger.error("Cannot write raster file", ex);
                }
            }

        }
    }

    public Voxel calculatePAD(Voxel voxel, int i, int j, int k) {

        if (voxel == null) {

            voxel = initVoxel(i, j, k);
        }

        float pad1;

        voxel.angleMean = voxel.angleMean / voxel.nbSampling;

        if (voxel.nbSampling >= voxel.nbEchos) {

            voxel.lMeanTotal = voxel.lgTotal / (voxel.nbSampling);

        }

        /**
         * *PADBV**
         */
        if (voxel.nbSampling == 0) {

            pad1 = Float.NaN;
            voxel.transmittance = Float.NaN;
            
        }else if(voxel.bvIntercepted > voxel.bvEntering){
            
            logger.error("Voxel : " + voxel.$i + " " + voxel.$j + " " + voxel.$k + " -> bvInterceptes > bvEntering, NaN assigné, difference: " + (voxel.bvEntering - voxel.bvIntercepted));

            pad1 = Float.NaN;
            voxel.transmittance = Float.NaN;
            
        }else {
            
            voxel.transmittance = (voxel.bvEntering - voxel.bvIntercepted) / voxel.bvEntering;
            voxel.transmittance = (float) Math.pow(voxel.transmittance, 1 / voxel.lMeanTotal);

            if (/*voxel.nbSampling > 1 && */voxel.transmittance == 0) { // nbSampling == nbEchos

                pad1 = MAX_PAD;

            }/* else if (voxel.nbSampling == 1 && voxel.transmittance == 0 ) { // nbSampling == nbEchos

                pad1 = Float.NaN;

            } */else {

                float coefficientGTheta = (float) direcTransmittance.getTransmittanceFromAngle(voxel.angleMean, true);

                if(coefficientGTheta == 0){
                    logger.error("Voxel : " + voxel.$i + " " + voxel.$j + " " + voxel.$k + " -> coefficient GTheta nul, angle = "+voxel.angleMean);
                }
                
                pad1 = (float) (Math.log(voxel.transmittance) / (-coefficientGTheta));

                if (Float.isNaN(pad1)) {
                    pad1 = Float.NaN;
                } else if (pad1 > MAX_PAD || Float.isInfinite(pad1)) {
                    pad1 = MAX_PAD;
                }

            }

        }

        voxel.PadBVTotal = pad1 + 0.0f; //set +0.0f to avoid -0.0f

        return voxel;
    }
    
    public void computePADs(){
        
        for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel voxel = voxels[i][j][k];
                        voxels[i][j][k] = calculatePAD(voxel, i, j, k);
                    }
                }
            }
    }
    
    public void write(){
        
        long start_time = System.currentTimeMillis();

        logger.info("writing file: " + outputFile.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + voxSpace.getBoundingBox().min.x + " " + voxSpace.getBoundingBox().min.y + " " + voxSpace.getBoundingBox().min.z + "\n");
            writer.write("#max_corner: " + voxSpace.getBoundingBox().max.x + " " + voxSpace.getBoundingBox().max.y + " " + voxSpace.getBoundingBox().max.z + "\n");
            writer.write("#split: " + voxSpace.getSplitting().x + " " + voxSpace.getSplitting().y + " " + voxSpace.getSplitting().z + "\n");

            String metadata = "";
            String type = "";

            metadata += "#res: " + parameters.resolution + " ";
            metadata += "#MAX_PAD: " + parameters.getMaxPAD();

            if (parameters.isTLS()) {
                type += "#type: " + "TLS" + " ";
                type += metadata + "\n";
                writer.write(type);

                writer.write(Voxel.getHeader(Voxel.class) + "\n");
            } else {
                type += "#type: " + "ALS" + " ";
                type += metadata + "\n";
                writer.write(type);

                writer.write(Voxel.getHeader(Voxel.class) + "\n");
            }

            for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel voxel = voxels[i][j][k];
                        writer.write(voxel.toString() + "\n");

                    }
                }
            }

            writer.close();

            logger.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

        } catch (FileNotFoundException e) {
            logger.error("Error: " + e);
        } catch (Exception e) {
            logger.error("Error: " + e);
        }
    }

    public void calculatePADAndWrite(double threshold) {

        long start_time = System.currentTimeMillis();

        logger.info("writing file: " + outputFile.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + voxSpace.getBoundingBox().min.x + " " + voxSpace.getBoundingBox().min.y + " " + voxSpace.getBoundingBox().min.z + "\n");
            writer.write("#max_corner: " + voxSpace.getBoundingBox().max.x + " " + voxSpace.getBoundingBox().max.y + " " + voxSpace.getBoundingBox().max.z + "\n");
            writer.write("#split: " + voxSpace.getSplitting().x + " " + voxSpace.getSplitting().y + " " + voxSpace.getSplitting().z + "\n");

            String metadata = "";
            String type = "";

            metadata += "#res: " + parameters.resolution + " ";
            metadata += "#MAX_PAD: " + parameters.getMaxPAD();

            if (parameters.isTLS()) {
                type += "#type: " + "TLS" + " ";
                type += metadata + "\n";
                writer.write(type);

                writer.write(Voxel.getHeader(Voxel.class) + "\n");
            } else {
                type += "#type: " + "ALS" + " ";
                type += metadata + "\n";
                writer.write(type);

                writer.write(Voxel.getHeader(Voxel.class) + "\n");
            }

            for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel voxel = voxels[i][j][k];

                        if (!padWasCalculated) {
                            voxel = calculatePAD(voxel, i, j, k);
                        }

                        writer.write(voxel.toString() + "\n");

                    }
                }
            }

            writer.close();

            padWasCalculated = true;

            logger.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

        } catch (FileNotFoundException e) {
            logger.error("Error: " + e);
        } catch (Exception e) {
            logger.error("Error: " + e);
        }

    }
    
    //moore neighborhood
    private List<Point3I> getIndicesFromPassID(Point3I middleID, int passID, Point3I minLimit, Point3I maxLimit){
        
        
        List<Point3I> indices = new ArrayList<>();
        
        int minI = Math.max(middleID.x - passID, minLimit.x);
        int maxI = Math.min(middleID.x + passID, maxLimit.x);
        int minJ = Math.max(middleID.y - passID, minLimit.y);
        int maxJ = Math.min(middleID.y + passID, maxLimit.y);
        int minK = Math.max(middleID.z - passID, minLimit.z);
        int maxK = Math.min(middleID.z + passID, maxLimit.z);
        
        for (int i = minI; i <= maxI; i++) {
            for (int j = minJ; j <= maxJ; j++) {
                indices.add(new Point3I(i, j, minK));
                indices.add(new Point3I(i, j, maxK));
            }
        }

        for (int k = minK; k <= maxK; k++) {
            for (int j = minJ+1; j <= maxJ-1; j++) {

                indices.add(new Point3I(minI, j, k));
                indices.add(new Point3I(maxI, j, k));
            }
        }

        for (int k = minK+1; k <= maxK-1; k++) {
            for (int i = minI + 1; i <= maxI - 1; i++) {
                indices.add(new Point3I(i, minJ, k));
                indices.add(new Point3I(i, maxJ, k));
            }
        }
        
        for (int k = minK+1; k <= maxK-1; k++) {
            for (int i = minI; i <= maxI; i++) {
                for (int j = minJ+1; j <= maxJ-1; j++) {
                   indices.add(new Point3I(i, j, k));
                   indices.add(new Point3I(i, j, k));
                }
            }
        }
        
        return indices;
    }
    
    //moore neighborhood
    private List<Point3I> getIndicesFromPassIDV2(Point3I middleID, int passID, Point3I minLimit, Point3I maxLimit){
        
        
        List<Point3I> indices = new ArrayList<>();
        
        int minI = Math.max(middleID.x - passID, minLimit.x);
        int maxI = Math.min(middleID.x + passID, maxLimit.x);
        int minJ = Math.max(middleID.y - passID, minLimit.y);
        int maxJ = Math.min(middleID.y + passID, maxLimit.y);
        int minK = Math.max(middleID.z - passID, minLimit.z);
        int maxK = Math.min(middleID.z + passID, maxLimit.z);
                
        for (int x = -passID; x <= passID; ++x) {
            int r_x = passID - Math.abs(x);
            for (int y = -r_x; y <= r_x; ++y) {
                int r_y = r_x - Math.abs(y);
                for (int z = -r_y; z <= r_y; ++z) {
                    System.out.println(x + " " + y + " " + z);
                }
            }
        }
        
        return indices;
    }    
    
    public static void cleanIsolatedVoxels(File voxelFile, int nbEchosThreshold, int neighboursRange){
        
        //VoxelFileReader 
    }
    
    public void correctNaNs(){
        
        /**A faire : corriger de manière parallèle**/
        
        int[][] canopeeArray = new int[parameters.split.x][parameters.split.y];
        for (int x = 0; x < parameters.split.x; x++) {
            for (int y = 0; y <parameters.split.y; y++) {
                for (int z = parameters.split.z-1; z >= 0; z--) {
                    
                    Voxel voxel = voxels[x][y][z];
                    
                    if (voxel.nbSampling > 0 && voxel.nbEchos > 0) {
                        canopeeArray[x][y] = z;
                        break;
                    }
                }
            }
        }
        
        int passLimit = Integer.max(Integer.max(parameters.split.x, parameters.split.y), parameters.split.z);
        
        int passMax = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (int x = 0; x < parameters.split.x; x++) {
            for (int y = 0; y < parameters.split.y; y++) {
                for (int z = 0; z < parameters.split.z; z++) {
                    
                    Voxel voxel = voxels[x][y][z];
                    
                    if(voxel.ground_distance >= (parameters.resolution / 2.0f)){
                        
                        float currentNbSampling = voxel.nbSampling;
                        float currentTransmittance = voxel.transmittance;

                        //List<Voxel> neighboursNew = new ArrayList<>();
                        List<Voxel> neighbours = new ArrayList<>();
                        int nbRemovedNeighbors = 0;

                        int passID = 1;

                        //testloop:
                        while(currentNbSampling <= parameters.getCorrectNaNsNbSamplingThreshold() || currentTransmittance == 0 ){
                            
                                                        
                            if(passID > passLimit){
                                break;
                            }
                            
                            
                            int minX = Integer.max(x-passID, 0);
                            int minY = Integer.max(y-passID, 0);
                            int minZ = Integer.max(z-passID, 0);
                            
                            int maxX = Integer.min(x+passID, parameters.split.x-1);
                            int maxY = Integer.min(y+passID, parameters.split.y-1);
                            int maxZ = Integer.min(z+passID, parameters.split.z-1);
                            
                            //List<Point3I> neighboursList = getIndicesFromPassID(new Point3I(x, y, z), passID+1, new Point3I(0, 0, 0), new Point3I(parameters.split.x-1, parameters.split.y-1, parameters.split.z-1));
//                            List<Point3I> neighboursList = getNeighboursList(passID, x, y, z, parameters.split.x, parameters.split.y, parameters.split.z);
//                            
//                            for(Point3I neighbor : neighboursList){
//                                
//                                int i = neighbor.x;
//                                int j = neighbor.y;
//                                int k = neighbor.z;
//                                
//                                if (k <= canopeeArray[i][j]) {
//
//                                    Voxel neighbour = voxels[i][j][k];
//
//                                    if (neighbour.ground_distance >= -(parameters.resolution / 2.0f)) {
//                                        neighbours.add(neighbour);
//                                    }else{
//                                        nbRemovedNeighbors++;
//                                    }
//                                }else{
//                                    nbRemovedNeighbors++;
//                                }
//                            }
                            
                            //get neighbors
                            for(int i = minX ; i<= maxX ; i++){
                                for(int j = minY ; j<= maxY ; j++){
                                    for(int k = minZ ; k<= maxZ ; k++){

                                            //on n'ajoute pas les voxels de la passe précédente
                                        if(passID != 1 && (i >= x-(passID-1) && i <= x+(passID-1))
                                                && (j >= y-(passID-1) && j <= y+(passID-1))
                                                && (k >= z-(passID-1) && k <= z+(passID-1))){

                                        } else {

                                            if (i == x && j == y && k == z) {

                                            } else {
                                                if (k <= canopeeArray[i][j]) {

                                                    Voxel neighbour = voxels[i][j][k];

                                                    if (neighbour.ground_distance >= -(parameters.resolution / 2.0f)) {
                                                        neighbours.add(neighbour);
                                                    }else{
                                                        nbRemovedNeighbors++;
                                                    }
                                                }else{
                                                    nbRemovedNeighbors++;
                                                }
                                            }
                                        }
                                        //}
                                        
                                    }
                                }
                            }
                            
                            float meanEffectiveNbSampling = 0;
                            float meanTransmittance = 0;
                            
                            float sumBVEntering = 0;
                            float sumBVIntercepted = 0;
                            float sumLgTotal = 0;
                            
                            
                            int count = 0;

                            for(Voxel neighbour : neighbours){
                                
                                /*les voxels de transmittance nuls sont traités comme étant non échantillonné,
                                tous les voisins sont considérés indépendamment de l'échantillonnage*/
                                
                                sumBVEntering += neighbour.bvEntering;
                                sumBVIntercepted += neighbour.bvIntercepted;
                                sumLgTotal += neighbour.lgTotal;
                                meanEffectiveNbSampling += neighbour.nbSampling;
                                if(!Float.isNaN(neighbour.transmittance) && neighbour.transmittance != 0){
                                    //meanEffectiveNbSampling += neighbour.nbSampling;
                                    //meanTransmittance *= neighbour.transmittance;
                                    count++;
                                }
                            }

                            if(count > 0 && sumBVEntering > 0 && neighbours.size() > 0){
                                
                                meanTransmittance = (float) Math.pow((sumBVEntering-sumBVIntercepted)/sumBVEntering, sumBVEntering/sumLgTotal);
                                meanEffectiveNbSampling /= (float)neighbours.size();
                                
                                currentNbSampling = meanEffectiveNbSampling;
                                currentTransmittance = meanTransmittance;

                            
                            }else{
                                currentNbSampling = 0;
                            }
                            
                            passID++;
                            
                        }

                        if(passID > passMax){
                            passMax = passID;
                            System.out.println(passMax);
                        }

                        if(neighbours.size() > 0){

                            float meanPAD = 0;

                            int count = 0;
                            for(Voxel neighbour : neighbours){

                                if(!Float.isNaN(neighbour.transmittance) && neighbour.transmittance != 0){
                                    meanPAD += neighbour.PadBVTotal;
                                    count++;
                                }
                            }
                            
                            if(count != 0){
                                voxels[x][y][z].neighboursNumber = count;
                                voxels[x][y][z].passNumber = passID;
                                voxels[x][y][z].PadBVTotal = meanPAD /count/* /ponderationCoeffSum*/;
                                voxels[x][y][z].nbSampling = (int)currentNbSampling;
                                voxels[x][y][z].transmittance = currentTransmittance;
                            }
                            
                            
                        }
                    }
                    
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long time = endTime - startTime;
        System.out.println("Time : "+time+" ms");
    }

    public void writeGroundEnergy() {

        if (groundEnergy.length > 0 && groundEnergy[0].length > 0) {

            long start_time = System.currentTimeMillis();

            logger.info("writing file: " + parameters.getGroundEnergyFile().getAbsolutePath());

            try {

                if (parameters.getGroundEnergyFileFormat() == VoxelParameters.FILE_FORMAT_PNG) {

                    BufferedImage image = new BufferedImage(parameters.split.x, parameters.split.y, BufferedImage.TYPE_INT_ARGB);

                    for (int i = 0; i < parameters.split.x; i++) {
                        for (int j = 0; j < parameters.split.y; j++) {

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

                            image.setRGB(i, parameters.split.y - 1 - j, c.getRGB());

                        }
                    }

                    ImageIO.write(image, "png", new File(parameters.getGroundEnergyFile().getAbsolutePath()));

                } else {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(parameters.getGroundEnergyFile()))) {
                        writer.write("i j groundEnergyActual groundEnergyPotential transmittance\n");

                        for (int i = 0; i < parameters.split.x; i++) {
                            for (int j = 0; j < parameters.split.y; j++) {

                                float transmittance = groundEnergy[i][j].groundEnergyActual / groundEnergy[i][j].groundEnergyPotential;
                                writer.write(i + " " + j + " " + groundEnergy[i][j].groundEnergyActual + " " + groundEnergy[i][j].groundEnergyPotential + " " + transmittance + "\n");
                            }
                        }
                    }
                }

                logger.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

            } catch (IOException ex) {
                logger.error(ex);
            }

        } else {

            logger.warn("Ground energy is set up but no values are available");
        }

    }

    public void createVoxelSpace() {

        try {
            if (parameters.isCalculateGroundEnergy()) {
                groundEnergy = new GroundEnergy[parameters.split.x][parameters.split.y];
            }

            voxSpace = new VoxelSpace(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner), parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

            // allocate voxels
            logger.info("allocate!!!!!!!!");

            voxels = new Voxel[parameters.split.x][parameters.split.y][parameters.split.z];

            try {

                if (parameters.isCalculateGroundEnergy() && !parameters.isTLS()) {
                    for (int i = 0; i < parameters.split.x; i++) {
                        for (int j = 0; j < parameters.split.y; j++) {
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
            scene.setBoundingBox(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner));

            voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

            System.out.println(voxelManager.getInformations());

        } catch (Exception e) {
            logger.error(e + " " + this.getClass().getName());
        }

    }

    private Voxel initVoxel(int i, int j, int k) {

        Voxel vox = new Voxel(i, j, k);

        Point3d position = getPosition(new Point3i(i, j, k),
                parameters.split, parameters.bottomCorner);

        float dist;

        if (terrain != null && parameters.useDTMCorrection()) {

            float dtmHeightXY = terrain.getSimpleHeight((float) position.x, (float) position.y);
            if (Float.isNaN(dtmHeightXY)) {
                dist = (float) (position.z);
            } else {
                dist = (float) (position.z - dtmHeightXY);
            }

        } else {
            dist = (float) (position.z - parameters.getBottomCorner().z);
        }

        vox.ground_distance = dist;

        return vox;
    }

    public int getNbShotsProcessed() {
        return nbShotsProcessed;
    }
    
    public static void main(String[] args) {
        
        
//        VoxelAnalysis voxelAnalysis = new VoxelAnalysis(null, null, null);
//        
//        List<Point3I> neighboursList = voxelAnalysis.getNeighboursList(2, 2, 2, 2, 3, 3, 3);
//        
//        int neighboursNumber = VoxTool.getNeighboursNumber(2, 2, 2, 2, 3, 3, 3);
//        Collections.sort(neighboursList);
//        
//        if(neighboursNumber != neighboursList.size()){
//            System.out.println("erreur");
//        }else{
//            System.out.println("correct");
//        }
        
        
//        VoxelParameters parameters = new VoxelParameters(new Point3d(0, 0, 0),
//                                                        new Point3d(20, 20, 20),
//                                                        new Point3i(20, 20, 20));
//        
//        parameters.setResolution(1.0);
//        parameters.setMaxPAD(3.5f);
//        parameters.setTLS(true);
//        parameters.setWeighting(VoxelParameters.WEIGHTING_ECHOS_NUMBER);
//        parameters.setWeightingData(VoxelParameters.DEFAULT_ALS_WEIGHTING);
//        parameters.setLadType(LeafAngleDistribution.Type.SPHERIC);
//
//        
//        voxelAnalysis.init(parameters, new File("/home/calcul/Documents/Julien/test.vox"));
//        voxelAnalysis.createVoxelSpace();
//        
//        List<Shot> shots = new ArrayList<>();
//        //shots.add(new Shot(4, new Point3d(10, 10, 15), new Vector3d(0, 0, -1), new double[]{6, 14, 18, 21}, new int[4], new int[4]));
//        
//        //shots.add(new Shot(1, new Point3d(10.5, 10.5, 10.99), new Vector3d(0, 0, -1), new double[]{8}, new int[1], new int[1]));
//        //shots.add(new Shot(4, new Point3d(10.5, 10.5, 10.99), new Vector3d(0, 0, -1), new double[]{2, 4, 6, 8}, new int[4], new int[4]));
//        //shots.add(new Shot(4, new Point3d(10, 10, 24), new Vector3d(0, 0, -1), new double[]{6.1, 6.2, 6.3, 10}, new int[1], new int[1]));
//        shots.add(new Shot(1, new Point3d(0.5, 0.01, 19.9), new Vector3d(1, 0, -1), new double[]{100}, new int[1], new int[1]));
//        
//        for(Shot shot : shots){
//            voxelAnalysis.processOneShot(shot);
//        }
//        
//        voxelAnalysis.computePADs();
//        voxelAnalysis.write();
    }
    
}