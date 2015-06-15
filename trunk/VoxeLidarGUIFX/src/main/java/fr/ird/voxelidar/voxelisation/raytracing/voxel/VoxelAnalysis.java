package fr.ird.voxelidar.voxelisation.raytracing.voxel;

import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.vecmath.Point3i;

import fr.ird.voxelidar.voxelisation.raytracing.geometry.LineElement;
import fr.ird.voxelidar.voxelisation.raytracing.geometry.LineSegment;
import fr.ird.voxelidar.voxelisation.raytracing.util.BoundingBox3d;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.ird.voxelidar.lidar.format.dtm.RegularDtm;
import fr.ird.voxelidar.octree.Octree;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import javax.imageio.ImageIO;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

public class VoxelAnalysis {

    private final static Logger logger = Logger.getLogger(VoxelAnalysis.class);

    private VoxelSpace voxSpace;
    private Voxel voxels[][][];
    private VoxelManager voxelManager;

    private static final float LASER_BEAM_DIVERGENCE_ALS = 0.0005f;
    private static final float LASER_BEAM_DIVERGENCE_TLS = 0.00035f;
    
    private float MAX_PAD = 3;

    public int nbShotsTreated;
    private File outputFile;
    private VoxelAnalysisData resultData;

    private float[][] weighting;
    
    private GroundEnergy[][] groundEnergy;
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int nbEchosSol = 0;
    public VoxelParameters parameters;
    private LinkedBlockingQueue<Shot> arrayBlockingQueue;

    private boolean isSet = false;

    private final EventListenerList listeners;
    private RegularDtm terrain;
    private List<Octree> pointcloudList;

    int lastShot = 0;
    boolean shotChanged = false;
    Point3d lastEchotemp = new Point3d();
    
    int nbSamplingTotal = 0;

    /**
     *
     */
    public class GroundEnergy {

        public int groundEnergyPotential;
        public float groundEnergyActual;

        public GroundEnergy() {
            groundEnergyPotential = 0;
            groundEnergyActual = 0;
        }
    }

    private float getGroundDistance(float x, float y, float z) {

        float distance = 0;
        
        if (terrain != null && parameters.useDTMCorrection()) {
            distance = z - (float) (terrain.getSimpleHeight(x, y));
        }

        return distance;
    }
    
    public VoxelAnalysis(RegularDtm terrain, List<Octree> pointcloud, List<Filter> filters) {

        nbShotsTreated = 0;
        listeners = new EventListenerList();
        this.terrain = terrain;
        this.pointcloudList = pointcloud;
        Shot.setFilters(filters);
        resultData = new VoxelAnalysisData();
    }

    /**
     *
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
        weighting = parameters.getWeightingData();
        MAX_PAD = parameters.getMaxPAD();

    }
    
    public void processOneShot(Shot shot){
        
        if(voxelManager == null){
            logger.error("VoxelManager not initialized, what happened??");
            return;
        }
        
        if (shot != null && shot.doFilter()) {


            if (nbShotsTreated % 1000000 == 0 && nbShotsTreated != 0) {
                logger.info("Shots processed: " + nbShotsTreated);
            }

            shot.direction.normalize();
            Point3d origin = new Point3d(shot.origin);

            if (shot.nbEchos == 0) {

                LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);
                Point3d echo = new Point3d(seg.getEnd());
                propagate(origin, echo, (short) 0, 1, 1, shot.origin, false, shot.angle, shot.nbEchos, 0, -1);

            } else {

                double beamFraction = 1;
                int sumIntensities = 0;

                if (!parameters.isTLS()) {
                    for (int i = 0; i < shot.nbEchos; i++) {
                        sumIntensities += shot.intensities[i];
                    }
                }

                double bfIntercepted = 0;

                shotChanged = true;
                isSet = false;

                double residualEnergy = 1;

                for (int i = 0; i < shot.nbEchos; i++) {                               


                   Point3d nextEcho = null;

                   if(i < shot.nbEchos-1){
                       nextEcho = new Point3d(getEchoLocation(shot, i+1));
                   }

                   Point3d echo = getEchoLocation(shot, i);

                   switch (parameters.getWeighting()) {

                        case VoxelParameters.WEIGHTING_FRACTIONING:
                            if (shot.nbEchos == 1) {
                                bfIntercepted = 1;
                            } else {
                                bfIntercepted = (shot.intensities[i]) / (double) sumIntensities;
                            }

                            break;

                        case VoxelParameters.WEIGHTING_ECHOS_NUMBER:
                        case VoxelParameters.WEIGHTING_FILE:

                            bfIntercepted += weighting[shot.nbEchos - 1][i];

                            break;

                        default:
                            bfIntercepted = 1;
                            break;

                    }
                   
                    if(areEchoInsideSameVoxel(echo, nextEcho)){

                       /*ne rien faire dans ce cas
                       le beamFraction est incrémenté et l'opération se fera sur l'écho suivant*/
                       count2++;
                    }else{

                        if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {
                            beamFraction = bfIntercepted;
                        }

                        boolean lastEcho;

                        lastEcho = i == shot.nbEchos - 1;

                        // propagate
                        if (parameters.isTLS()) {
                            propagate(origin, echo, (short) 0, beamFraction, residualEnergy, shot.origin, lastEcho, shot.angle, shot.nbEchos, i, shot.reflectances[i]);
                        } else {
                            propagate(origin, echo, shot.classifications[i], beamFraction, residualEnergy, shot.origin, lastEcho, shot.angle, shot.nbEchos, i, -1.0f);
                        }

                        if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {

                            residualEnergy -= bfIntercepted;

                        }

                        bfIntercepted = 0;
                    }

                   origin = new Point3d(echo);

                }
            }

            nbShotsTreated++;
        }
    }
    
    private Point3d getEchoLocation(Shot shot, int indice){
        
        LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[indice]);
        return seg.getEnd();
    }

    private boolean isEchoInsideVoxel(Point3d point, Point3i searchedIndices) {

        Point3i realIndices = voxelManager.getVoxelIndicesFromPoint(point);

        return realIndices.equals(searchedIndices);
    }
    
    private boolean areEchoInsideSameVoxel(Point3d echo1, Point3d echo2){
        
        if(echo1 == null || echo2 == null){
            return false;
        }
        
        Point3i indices1 = voxelManager.getVoxelIndicesFromPoint(echo1);
        Point3i indices2 = voxelManager.getVoxelIndicesFromPoint(echo2);
        
        return indices1 !=null && indices2 != null && indices1.equals(indices2);
    }

    /**
     *
     * @param origin current origin (origin start from the last echo)
     * @param echo current echo (position in voxel space)
     * @param beamFraction
     * @param source shot origin
     */
    
    private void propagate(Point3d origin, Point3d echo, short classification, double beamFraction, double residualEnergy, Point3d source, boolean lastEcho, double angle, int nbEchos, int indiceEcho, float reflectance) {

        //get shot line
        LineElement lineElement = new LineSegment(origin, echo);
        

        //get the first voxel cross by the line
        VoxelCrossingContext context = voxelManager.getFirstVoxel(lineElement);

        double distanceToHit = lineElement.getLength();
        
        //calculate ground distance
        float echoDistance = getGroundDistance((float) echo.x, (float) echo.y, (float) echo.z);
        boolean keepEcho = true;
        
        if(parameters.isUsePointCloudFilter() && pointcloudList != null){
            
            Point3F point = new Point3F((float) echo.x, (float) echo.y, (float) echo.z);
            
            int count = 0;
            boolean test;
            
            for(Octree octree : pointcloudList){
                
                test = octree.isPointBelongsToPointcloud(point, parameters.getPointcloudFilters().get(count).getPointcloudErrorMargin(), Octree.INCREMENTAL_SEARCH);
                
                if(parameters.getPointcloudFilters().get(count).isKeep()){
                    keepEcho = test;
                }else{
                    if(test){
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
            

            if(voxels[indices.x][indices.y][indices.z] == null){
                voxels[indices.x][indices.y][indices.z] = initVoxel(indices.x, indices.y, indices.z);
            }
            
            Voxel vox = voxels[indices.x][indices.y][indices.z];
            
            
            //distance de l'écho à la source
            /**
             * ****************A verifier si il vaut mieux prendre la distance
             * du centre du voxel à la source ou de l'écho à la source*********************
             */
            double surface;
            
            //recalculé pour éviter le stockage de trois doubles (24 octets) par voxel
            double distance = getPosition(new Point3i(indices.x, indices.y, indices.z), parameters.split, parameters.bottomCorner).distance(source);

            //surface de la section du faisceau à la distance de la source
            if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {
                if(!parameters.isTLS()){
                    surface = Math.pow(Math.tan(LASER_BEAM_DIVERGENCE_ALS / 2) * distance, 2) * Math.PI;
                }else{
                    surface = Math.pow(Math.tan(LASER_BEAM_DIVERGENCE_TLS / 2) * distance, 2) * Math.PI;
                }
            }else{
                surface = 1;
            }
            

            // voxel sampled without hit
            /*Si un écho est positionné sur une face du voxel alors il est considéré
             comme étant à l'extérieur du dernier voxel traversé*/
            /*
            
                
             /*
             * Si d2 < distanceToHit le voxel est traversé sans interceptions
             */
            if (d2 <= distanceToHit) {

                double longueur = d2 - d1;
                
                vox.lgTotal += longueur;

                vox.nbSampling++;
                nbSamplingTotal++;
                
                vox.angleMean += angle;
                vox.bvEntering += (surface * residualEnergy * longueur);
                //vox._transBeforeNorm += (surface * longueur);
                //vox._sumSurfaceMultiplyLength += (surface*longueur);
                
                
                /*
                 Si l'écho est sur la face sortante du voxel, 
                 on n'incrémente pas le compteur d'échos
                 */
                
            } /*
             Poursuite du trajet optique jusqu'à sortie de la bounding box
             */ else if (d1 > distanceToHit) {

                /*
                 la distance actuelle d1 est supérieure à la distance à l'écho
                 ce qui veut dire que l'écho a déjà été trouvé
                 */
                //on peut chercher ici la distance jusqu'au prochain voxel "sol"
                if (shotChanged) {
                    
                    if(parameters.isCalculateGroundEnergy() && !parameters.isTLS()){
                        
                        if(vox.ground_distance < parameters.minDTMDistance){
                            groundEnergy[vox.$i][vox.$j].groundEnergyPotential ++;
                            shotChanged = false;
                            context = null;
                        }
                        
                    }else{
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

                    vox.nbSampling++;

                    nbSamplingTotal++;

                    double longueur;

                    if(lastEcho){
                        longueur = (distanceToHit - d1);
                    }else{
                        longueur = (d2 - d1);
                    }

                    vox.lgTotal += longueur;

                    vox.angleMean += angle;

                    double entering;
                    entering = (surface * beamFraction * longueur);
                    vox.bvEntering += entering;

                    double intercepted = 0;


                    if (((classification != 2 && !parameters.isTLS()) || parameters.isTLS()) && 
                            ((echoDistance >= parameters.minDTMDistance && echoDistance!= Float.NaN && parameters.useDTMCorrection())|| !parameters.useDTMCorrection()) &&
                            keepEcho){

                        vox.nbEchos++;

                        if(parameters.isUsePointCloudFilter()){
                            resultData.filteredPointsCount++;
                        }


                        intercepted = (surface * beamFraction * longueur);
                        vox.bvIntercepted += intercepted;

                    }else{


                        if(parameters.isCalculateGroundEnergy() && !parameters.isTLS() && !isSet){
                            groundEnergy[vox.$i][vox.$j].groundEnergyActual += residualEnergy;
                            groundEnergy[vox.$i][vox.$j].groundEnergyPotential++;

                            isSet = true;
                        }
                    }

                    //vox._sumSurfaceMultiplyLength += (surface*longueur);
                    //vox._transBeforeNorm += (((entering-intercepted)/entering) * surface * longueur);
                    
                lastEchotemp = echo;
            }
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
            
            metadata += "#res: "+parameters.resolution+" ";
            metadata += "#MAX_PAD: "+parameters.getMaxPAD();
            
            if (parameters.isTLS()) {
                type += "#type: " +"TLS"+ " ";
                type += metadata+"\n";
                writer.write(type);
                
                writer.write(Voxel.getHeader(Voxel.class) + "\n");
            } else {
                type += "#type: " +"ALS"+ " ";
                type += metadata+"\n";
                writer.write(type);
                
                writer.write(Voxel.getHeader(Voxel.class) + "\n");
            }

            for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel vox = voxels[i][j][k];
                        
                        if(vox == null){
                            
                            vox = initVoxel(i, j, k);
                        }

                        float pad1/*, pad2*/;

                        vox.angleMean = vox.angleMean / vox.nbSampling;

                        if (vox.nbSampling >= vox.nbEchos) {

                            vox.lMeanTotal = vox.lgTotal / (vox.nbSampling);

                        }
                        
                        Voxel voxel = vox;

                        /**
                         * *PADBV**
                         */
                        if (voxel.bvEntering <= threshold) {

                            pad1 = Float.NaN;
                            //pad2 = pad1;
                            voxel.transmittance = Float.NaN;
                            //voxel._transmittance_v2 = Float.NaN;

                        } else if (voxel.bvIntercepted > voxel.bvEntering) {

                            logger.error("Voxel : " + voxel.$i + " " + voxel.$j + " " + voxel.$k + " -> bvInterceptes > bvEntering, NaN assigné, difference: " + (voxel.bvEntering - voxel.bvIntercepted));

                            pad1 = Float.NaN;
                            //pad2 = pad1;
                            voxel.transmittance = Float.NaN;
                            //voxel._transmittance_v2 = Float.NaN;

                        } else {

                            voxel.transmittance = (voxel.bvEntering - voxel.bvIntercepted) / voxel.bvEntering;
                            //voxel._transmittance_v2 = (voxel._transBeforeNorm) / voxel._sumSurfaceMultiplyLength ;

                            if (voxel.nbSampling > 1 && voxel.transmittance == 0 && voxel.nbSampling == voxel.nbEchos) {

                                pad1 = MAX_PAD;
                                //pad2 = pad1;

                            } else if (voxel.nbSampling <= 2 && voxel.transmittance == 0 && voxel.nbSampling == voxel.nbEchos) {

                                pad1 = Float.NaN;
                                //pad2 = pad1;

                            } else {

                                pad1 = (float) (Math.log(voxel.transmittance) / (-0.5 * voxel.lMeanTotal));
                                //pad2 = (float) (Math.log(voxel._transmittance_v2) / (-0.5 * voxel.lMeanTotal));

                                if (Float.isNaN(pad1)) {
                                    pad1 = Float.NaN;
                                } else if (pad1 > MAX_PAD || Float.isInfinite(pad1)) {
                                    pad1 = MAX_PAD;
                                }
                                /*
                                if (Float.isNaN(pad2)) {
                                    pad2 = Float.NaN;
                                } else if (pad2 > MAX_PAD || Float.isInfinite(pad2)) {
                                    pad2 = MAX_PAD;
                                }*/

                            }

                        }

                        voxel.PadBVTotal = pad1 + 0.0f; //set +0.0f to avoid -0.0f
                        //voxel._PadBVTotal_V2 = pad2 + 0.0f; //set +0.0f to avoid -0.0f

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

    public void writeGroundEnergy(){
        
        if(groundEnergy.length > 0 && groundEnergy[0].length > 0){
            
            long start_time = System.currentTimeMillis();

            logger.info("writing file: " + parameters.getGroundEnergyFile().getAbsolutePath());
        
            try {
                
                if(parameters.getGroundEnergyFileFormat() == VoxelParameters.FILE_FORMAT_PNG){
                    
                    BufferedImage image = new BufferedImage(parameters.split.x, parameters.split.y, BufferedImage.TYPE_INT_ARGB);
                    
                    for (int i = 0; i < parameters.split.x; i++) {
                        for (int j = 0; j < parameters.split.y; j++) {
                            
                            float transmittance = groundEnergy[i][j].groundEnergyActual/groundEnergy[i][j].groundEnergyPotential;
                            
                            Color c;
                            
                            if(transmittance <= 1.0 && transmittance >= 0.0){
                                c = new Color(ColorSpace.getInstance(ColorSpace.CS_GRAY), new float[]{transmittance}, 1.0f);
                                
                            }else if(transmittance > 1.0){
                                c = new Color(1.0f, 0.0f, 0.0f, 1.0f);
                            }else if(transmittance < 0.0){
                                c = new Color(0.0f, 1.0f, 0.0f, 1.0f);
                            }else{
                                c = new Color(0.0f, 0.0f, 1.0f, 1.0f);
                            }
                            
                            image.setRGB(i, parameters.split.y-1-j, c.getRGB());
                            
                        }
                    }
                    
                    ImageIO.write(image, "png",new File(parameters.getGroundEnergyFile().getAbsolutePath()));
                    
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
            
        }else{
            
            logger.warn("Ground energy is set up but no values are available");
        }
        
    }

    public void createVoxelSpace() {

        try {
            if(parameters.isCalculateGroundEnergy()){
                groundEnergy = new GroundEnergy[parameters.split.x][parameters.split.y];
            }
            
            voxSpace = new VoxelSpace(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner), parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

            // allocate voxels
            logger.info("allocate!!!!!!!!");

            voxels = new Voxel[parameters.split.x][parameters.split.y][parameters.split.z];
            
            try{
                for (int x = 0; x < parameters.split.x; x++) {
                    for (int y = 0; y < parameters.split.y; y++) {
                        for (int z = 0; z < parameters.split.z; z++) {
                            /*
                            voxels[x][y][z] = new Voxel(x, y, z);

                            Point3d position = getPosition(new Point3i(x, y, z),
                                    parameters.split, parameters.bottomCorner);

                            float dist;
                            if (terrain != null && parameters.useDTMCorrection()) {
                                float dtmHeightXY = terrain.getSimpleHeight((float) position.x, (float) position.y);
                                if (dtmHeightXY == Float.NaN) {
                                    dist = (float) (position.z);
                                } else {
                                    dist = (float) (position.z - dtmHeightXY);
                                }

                            } else {
                                dist = (float) (position.z);
                            }
                            voxels[x][y][z].setDist(dist);
                            voxels[x][y][z].setPosition(position);*/
                        }
                    }
                }

                if(parameters.isCalculateGroundEnergy() && !parameters.isTLS()){
                    for (int i = 0; i < parameters.split.x; i++) {
                        for (int j = 0; j < parameters.split.y; j++) {
                            groundEnergy[i][j] = new GroundEnergy();
                        }
                    }
                }
                
            }catch(OutOfMemoryError ex){
                throw new Exception("Unsufficient memory, you need to allocation more, change the Xmx value!", ex);
            }catch(Exception ex){
                throw new Exception("Error during instantiation of voxel space: ", ex);
            }
            
            Scene scene = new Scene();
            scene.setBoundingBox(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner));

            voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

            voxelManager.showInformations();
            

        } catch (Exception e) {
            logger.error(e+" "+this.getClass().getName());
        }

    }

    public VoxelAnalysisData getResultData() {
        return resultData;
    }
    
    private Voxel initVoxel(int i, int j, int k){
        
        Voxel vox = new Voxel(i, j, k);
        
        Point3d position = getPosition(new Point3i(i, j, k),
        parameters.split, parameters.bottomCorner);

        float dist;

        if (terrain != null && parameters.useDTMCorrection()) {
            float dtmHeightXY = terrain.getSimpleHeight((float) position.x, (float) position.y);
            if (dtmHeightXY == Float.NaN) {
                dist = (float) (position.z);
            } else {
                dist = (float) (position.z - dtmHeightXY);
            }

        } else {
            dist = (float) (position.z);
        }

        vox.setDist(dist);
        //vox.setPosition(position);
        
        return vox;
    }
}
