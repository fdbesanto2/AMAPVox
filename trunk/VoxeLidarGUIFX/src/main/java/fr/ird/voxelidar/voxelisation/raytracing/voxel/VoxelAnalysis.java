package fr.ird.voxelidar.voxelisation.raytracing.voxel;

import fr.ird.voxelidar.voxelisation.VoxelParameters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.vecmath.Point3i;

import fr.ird.voxelidar.voxelisation.raytracing.geometry.LineElement;
import fr.ird.voxelidar.voxelisation.raytracing.geometry.LineSegment;
import fr.ird.voxelidar.voxelisation.raytracing.util.BoundingBox3d;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.SimpleFilter;
import fr.ird.voxelidar.util.TimeCounter;
import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.imageio.ImageIO;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

public class VoxelAnalysis implements Runnable {

    private final static Logger logger = Logger.getLogger(VoxelAnalysis.class);

    private SimpleFilter filter;
    private VoxelSpace voxSpace;
    private Voxel voxels[][][];
    private VoxelManager voxelManager;

    private final double laserBeamDivergence = 0.0005f;
    private float MAX_PAD = 3;

    private int nbShotsTreated;
    private File outputFile;
    private static int compteur = 1;
    private static int compteur2 = 1;
    private Point3d offset;

    private static float[][] weighting;
    private GroundEnergy[][] groundEnergy;

    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int nbEchosSol = 0;
    private VoxelParameters parameters;
    private final LinkedBlockingQueue<Shot> arrayBlockingQueue;

    //private Mat4D transfMatrix;
    //private Mat3D rotation;
    private AtomicBoolean isFinished;
    private boolean isSet = false;

    private final EventListenerList listeners;
    private Dtm terrain;

    private int shotID = 0;

    int lastShot = 0;
    boolean shotChanged = false;
    Point3d lastEchotemp = new Point3d();

    public void setIsFinished(boolean isFinished) {
        this.isFinished.set(isFinished);
    }

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

    public VoxelAnalysis(LinkedBlockingQueue<Shot> arrayBlockingQueue, Dtm terrain, List<Filter> filters) {

        nbShotsTreated = 0;
        isFinished = new AtomicBoolean(false);
        this.arrayBlockingQueue = arrayBlockingQueue;
        listeners = new EventListenerList();
        this.terrain = terrain;
        Shot.setFilters(filters);
    }

    public Point3d getPosition(Point3i indices, Point3i splitting, Point3d minCorner, Point3d maxCorner) {

        Point3d resolution = new Point3d();
        resolution.x = (maxCorner.x - minCorner.x) / splitting.x;
        resolution.y = (maxCorner.y - minCorner.y) / splitting.y;
        resolution.z = (maxCorner.z - minCorner.z) / splitting.z;

        double posX = offset.x + (resolution.x / 2.0d) + (indices.x * resolution.x);
        double posY = offset.y + (resolution.y / 2.0d) + (indices.y * resolution.y);
        double posZ = offset.z + (resolution.z / 2.0d) + (indices.z * resolution.z);

        return new Point3d(posX, posY, posZ);
    }

    public void init(VoxelParameters parameters, File outputFile) {

        this.parameters = parameters;
        this.outputFile = outputFile;
        weighting = parameters.getWeightingData();
        /*
        switch (parameters.getWeighting()) {

            case VoxelParameters.WEIGHTING_ECHOS_NUMBER:

                if (parameters.isTLS()) {
                    weighting = VoxelParameters.DEFAULT_TLS_WEIGHTING;
                } else {
                    weighting = VoxelParameters.DEFAULT_ALS_WEIGHTING;
                }

                break;

            case VoxelParameters.WEIGHTING_NONE:

                break;

            case VoxelParameters.WEIGHTING_FILE:

                //read file
                File weightingFile = parameters.getWeightingFile();

                break;

            case VoxelParameters.WEIGHTING_FRACTIONING:

                break;

        }
        */
        MAX_PAD = parameters.getMaxPAD();

        offset = new Point3d(parameters.bottomCorner);
        groundEnergy = new GroundEnergy[parameters.split.x][parameters.split.y];

    }

    @Override
    public void run() {

        try {
            long start_time = System.currentTimeMillis();

            createVoxelSpace();

            boolean first = true;

            //try {
            /**
             * *TEST : write shots file***
             */
            //BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Julien\\Desktop\\test_rxp.txt"));
            //writer.write("\"key\" \"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");
            //writer.write("\"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");            
            while (!isFinished.get() || !arrayBlockingQueue.isEmpty()) {

                try {

                    Shot shot = arrayBlockingQueue.poll();

                    if (shot != null && shot.doFilter()) {

                        shotID = nbShotsTreated;

                        if (nbShotsTreated % 1000000 == 0 && nbShotsTreated != 0) {
                            logger.info("Shots treated: " + nbShotsTreated);
                        }

                        shot.direction.normalize();
                        Point3d origin = new Point3d(shot.origin);

                        if (shot.nbEchos == 0) {

                            LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);
                            Point3d echo = new Point3d(seg.getEnd());
                            propagate(origin, echo, (short) 0, 1, 1, shot.origin, false, shot.angle, shot.nbEchos, 0);

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
                                        propagate(origin, echo, (short) 0, beamFraction, residualEnergy, shot.origin, lastEcho, shot.angle, shot.nbEchos, i);
                                    } else {
                                        propagate(origin, echo, shot.classifications[i], beamFraction, residualEnergy, shot.origin, lastEcho, shot.angle, shot.nbEchos, i);
                                    }

                                    if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {

                                        residualEnergy -= bfIntercepted;
                                    } else {
                                        //residualEnergy -= (bfIntercepted / shot.nbEchos);
                                    }

                                    bfIntercepted = 0;
                                }
                               
                               origin = new Point3d(echo);
                               
                            }
                        }

                        nbShotsTreated++;
                    }

                } catch (Exception e) {
                    logger.error(e);
                }
            }

            logger.info("Shots processed: " + nbShotsTreated);
            logger.info("voxelisation is finished ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

            calculatePADAndWrite(0);
            
            if(parameters.isCalculateGroundEnergy() && !parameters.isTLS()){
                writeGroundEnergy();
            }

        } catch (Exception e) {
            logger.error(e);
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
    
    private void propagate(Point3d origin, Point3d echo, short classification, double beamFraction, double residualEnergy, Point3d source, boolean lastEcho, double angle, int nbEchos, int indiceEcho) {

        //get shot line
        LineElement lineElement = new LineSegment(origin, echo);

        //get the first voxel cross by the line
        VoxelCrossingContext context = voxelManager.getFirstVoxel(lineElement);

        double distanceToHit = lineElement.getLength();
        
        //calculate ground distance
        float echoDistance = getGroundDistance((float) echo.x, (float) echo.y, (float) echo.z);

        while ((context != null) && (context.indices != null)) {

            //distance from the last origin to the point in which the ray enter the voxel
            double d1 = context.length;

            //current voxel
            Point3i indices = context.indices;
            
            

            context = voxelManager.CrossVoxel(lineElement, context.indices);

            //distance from the last origin to the point in which the ray exit the voxel
            double d2 = context.length;
            

            Voxel vox = voxels[indices.x][indices.y][indices.z];
            
            
            
            //distance de l'écho à la source
            /**
             * ****************A verifier si il vaut mieux prendre la distance
             * du centre du voxel à la source ou de l'écho à la source*********************
             */
            double distance = vox._position.distance(source);

            //surface de la section du faisceau à la distance de la source
            double surface = Math.pow(Math.tan(laserBeamDivergence / 2) * distance, 2) * Math.PI;

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
                
                if (!parameters.isTLS()) {
                    
                    ((ALSVoxel) vox).bvEntering += (surface * residualEnergy * longueur);
                    
                    vox._transBeforeNorm += (1 * surface * longueur);
                    ((ALSVoxel) vox).sumSurfaceMultiplyLength += (surface*longueur);
                    
                } else {
                    
                    ((TLSVoxel) vox).bflEntering += (1 * longueur);
                    
                    vox._transBeforeNorm += longueur;
                }
                
                
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
                //if ((echoDistance >= parameters.minDTMDistance && echoDistance!= Float.NaN && parameters.useDTMCorrection()) || !parameters.useDTMCorrection()) {
                    if ((classification != 2 && !parameters.isTLS()) || parameters.isTLS()) { // if not ground
                        
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
                        if (!parameters.isTLS()) {
                            
                            entering = (surface * beamFraction * longueur);
                            ((ALSVoxel) vox).bvEntering += entering;
                            
                        } else {
                            entering = (1 * longueur);
                            ((TLSVoxel) vox).bflEntering += entering;
                        }
                        
                        double intercepted = 0;
                        
                        
                        if ((echoDistance >= parameters.minDTMDistance && echoDistance!= Float.NaN && parameters.useDTMCorrection())|| !parameters.useDTMCorrection()){
                            
                            vox.nbEchos++;
                            
                            
                            if (!parameters.isTLS()) {
                                
                                intercepted = (surface * beamFraction * longueur);
                                
                                ((ALSVoxel) vox).bvIntercepted += intercepted;

                            } else {
                                intercepted = (1 * longueur);
                                ((TLSVoxel) vox).bflIntercepted += intercepted;
                            }
                            
                        }else{
                            
                            if(parameters.isCalculateGroundEnergy() && !parameters.isTLS() && !isSet){
                                groundEnergy[vox.$i][vox.$j].groundEnergyActual += beamFraction;
                                isSet = true;
                            }
                        }
                        
                        if (!parameters.isTLS()) {
                            
                            ((ALSVoxel) vox).sumSurfaceMultiplyLength += (surface*longueur);
                            ((ALSVoxel) vox)._transBeforeNorm += (((entering-intercepted)/entering) * surface * longueur);
                            
                        } else {
                            ((TLSVoxel) vox)._transBeforeNorm += (((entering-intercepted)/entering) * longueur);
                        }
                    }
                    
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

            

            //writer.write("i j k shots path_length BFintercepted BFentering BSintercepted BSentering PAD"+"\n");
            //writer.write("i j k nbSampling interceptions path_length lgTraversant lgInterception PAD PAD2 BFIntercepted BFEntering BSIntercepted BSEntering dist"+"\n");
            if (parameters.isTLS()) {
                writer.write("#type: " +"TLS"+ "\n");
                writer.write(Voxel.getHeader(TLSVoxel.class) + "\n");
            } else {
                writer.write("#type: " +"ALS"+ "\n");
                writer.write(Voxel.getHeader(ALSVoxel.class) + "\n");
            }

            for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel vox = voxels[i][j][k];

                        float pad1, pad2;

                        vox.angleMean = vox.angleMean / vox.nbSampling;

                        if (vox.nbSampling >= vox.nbEchos) {

                            vox.lMeanTotal = vox.lgTotal / (vox.nbSampling);

                        }

                        if (parameters.isTLS()) {

                            TLSVoxel tlsVox = (TLSVoxel) vox;

                            /**
                             * *PADBV**
                             */
                            if (tlsVox.bflEntering <= threshold) {

                                pad1 = Float.NaN;
                                pad2 = pad1;
                                tlsVox.transmittance = Float.NaN;
                                tlsVox.transmittance_v2 = Float.NaN;

                            } else if (tlsVox.bflIntercepted > tlsVox.bflEntering) {

                                logger.error("Voxel : " + tlsVox.$i + " " + tlsVox.$j + " " + tlsVox.$k + " -> bflInterceptes > bflEntering, NaN assigné");
                                
                                tlsVox.transmittance = Float.NaN;
                                tlsVox.transmittance_v2 = Float.NaN;
                                pad1 = Float.NaN;
                                pad2 = pad1;

                            } else {
                                
                                tlsVox.transmittance = (tlsVox.bflEntering - tlsVox.bflIntercepted) / tlsVox.bflEntering;
                                tlsVox.transmittance_v2 = (tlsVox._transBeforeNorm) / tlsVox.lgTotal;
                                
                                if(Math.abs(tlsVox.transmittance - tlsVox.transmittance_v2) > 0.001){
                                    System.out.println("test");
                                }
                                
                                if (tlsVox.nbSampling > 1 && tlsVox.transmittance == 0 && tlsVox.nbSampling == tlsVox.nbEchos) {

                                    pad1 = MAX_PAD;
                                    pad2 = pad1;

                                } else if (tlsVox.nbSampling <= 2 && tlsVox.transmittance == 0 && tlsVox.nbSampling == tlsVox.nbEchos) {

                                    pad1 = Float.NaN;
                                    pad2 = pad1;

                                } else {
                                    
                                    pad1 = (float) (Math.log(tlsVox.transmittance) / (-0.5 * tlsVox.lMeanTotal));
                                    pad2 = (float) (Math.log(tlsVox.transmittance_v2) / (-0.5 * tlsVox.lMeanTotal));
                                    
                                    if (Float.isNaN(pad1)) {
                                        pad1 = Float.NaN;
                                    } else if (pad1 > MAX_PAD || Float.isInfinite(pad1)) {
                                        pad1 = MAX_PAD;
                                    }
                                    
                                    if (Float.isNaN(pad2)) {
                                        pad2 = Float.NaN;
                                    } else if (pad2 > MAX_PAD || Float.isInfinite(pad2)) {
                                        pad2 = MAX_PAD;
                                    }
                                }

                            }
                            
                            tlsVox.PadBflTotal = pad1 + 0.0f; //set +0.0f to avoid -0.0f
                            tlsVox.PadBflTotal_V2 = pad2 + 0.0f; //set +0.0f to avoid -0.0f

                            writer.write(tlsVox.toString() + "\n");

                        } else {

                            ALSVoxel alsVox = (ALSVoxel) vox;


                            /**
                             * *PADBV**
                             */
                            if (alsVox.bvEntering <= threshold) {

                                pad1 = Float.NaN;
                                pad2 = pad1;
                                alsVox.transmittance = Float.NaN;
                                alsVox.transmittance_v2 = Float.NaN;

                            } else if (alsVox.bvIntercepted > alsVox.bvEntering) {

                                logger.error("Voxel : " + alsVox.$i + " " + alsVox.$j + " " + alsVox.$k + " -> bvInterceptes > bvEntering, NaN assigné, difference: " + (alsVox.bvEntering - alsVox.bvIntercepted));
                                
                                pad1 = Float.NaN;
                                pad2 = pad1;
                                alsVox.transmittance = Float.NaN;
                                alsVox.transmittance_v2 = Float.NaN;

                            } else {
                                
                                alsVox.transmittance = (alsVox.bvEntering - alsVox.bvIntercepted) / alsVox.bvEntering;
                                alsVox.transmittance_v2 = (alsVox._transBeforeNorm) / alsVox.sumSurfaceMultiplyLength ;

                                if (alsVox.nbSampling > 1 && alsVox.transmittance == 0 && alsVox.nbSampling == alsVox.nbEchos) {

                                    pad1 = MAX_PAD;
                                    pad2 = pad1;

                                } else if (alsVox.nbSampling <= 2 && alsVox.transmittance == 0 && alsVox.nbSampling == alsVox.nbEchos) {

                                    pad1 = Float.NaN;
                                    pad2 = pad1;

                                } else {

                                    pad1 = (float) (Math.log(alsVox.transmittance) / (-0.5 * alsVox.lMeanTotal));
                                    pad2 = (float) (Math.log(alsVox.transmittance_v2) / (-0.5 * alsVox.lMeanTotal));

                                    if (Float.isNaN(pad1)) {
                                        pad1 = Float.NaN;
                                    } else if (pad1 > MAX_PAD || Float.isInfinite(pad1)) {
                                        pad1 = MAX_PAD;
                                    }
                                    
                                    if (Float.isNaN(pad2)) {
                                        pad2 = Float.NaN;
                                    } else if (pad2 > MAX_PAD || Float.isInfinite(pad2)) {
                                        pad2 = MAX_PAD;
                                    }
                                    
                                }

                            }

                            alsVox.PadBVTotal = pad1 + 0.0f; //set +0.0f to avoid -0.0f
                            alsVox.PadBVTotal_V2 = pad2 + 0.0f; //set +0.0f to avoid -0.0f

                            writer.write(alsVox.toString() + "\n");
                        }

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

    private void writeGroundEnergy(){
        
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
                                
                            }else{
                                c = new Color(1.0f, 0.0f, 0.0f, 1.0f);
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

    private void createVoxelSpace() {

        try {
            voxSpace = new VoxelSpace(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner), parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

            // allocate voxels
            logger.info("allocate!!!!!!!!");

            if (parameters.isTLS()) {
                voxels = new TLSVoxel[parameters.split.x][parameters.split.y][parameters.split.z];
            } else {
                voxels = new ALSVoxel[parameters.split.x][parameters.split.y][parameters.split.z];
            }
            

            for (int x = 0; x < parameters.split.x; x++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int z = 0; z < parameters.split.z; z++) {

                        if (parameters.isTLS()) {
                            voxels[x][y][z] = new TLSVoxel(x, y, z);
                        } else {
                            voxels[x][y][z] = new ALSVoxel(x, y, z);
                        }

                        Point3d position = getPosition(new Point3i(x, y, z),
                                parameters.split, parameters.bottomCorner, parameters.topCorner);

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
                        voxels[x][y][z].setPosition(position);
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
            Scene scene = new Scene();
            scene.setBoundingBox(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner));

            voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

            voxelManager.showInformations();
            

        } catch (Exception e) {
            logger.error(e);
        }

    }
}
