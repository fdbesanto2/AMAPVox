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
import fr.ird.voxelidar.util.SimpleFilter;
import fr.ird.voxelidar.util.TimeCounter;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
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
    private static final float MAX_PAD = 3.0f;

    private int nbShotsTreated;
    private File outputFile;
    private static int compteur = 1;
    private static int compteur2 = 1;
    private Point3d offset;

    private static float[][] weighting;
    //private GroundEnergy[][] groundEnergy;

    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int nbEchosSol = 0;
    private VoxelParameters parameters;
    private final LinkedBlockingQueue<Shot> arrayBlockingQueue;

    //private Mat4D transfMatrix;
    //private Mat3D rotation;
    private AtomicBoolean isFinished;

    private final EventListenerList listeners;
    private Dtm terrain;

    //variables temporaires
    private Point3i lastIndices = new Point3i();
    private double sumBeamFraction = 0;
    private double lastBeamFraction = 0;
    private boolean wasEqual = false;
    private double lastLongueur = 0;
    private int shotID = 0;
    private double lastDistanceToHit = 0;
    private int lastNbEchos = 0;
    private double lastD1 = 0;
    private double lastD2 = 0;
    private int nbEchosInsideVoxel = 1;
    private double lastEntering;
    private int lastShotID;
    private double lastSurface = 0;

    private ArrayList<Integer> indicesEchosTemp = new ArrayList<>();
    private double meanLongTemp = 0;
    private int nbLongTemp = 0;
    private int lastIndice = 0;

    int lastShot = 0;
    boolean shotChanged = false;
    Point3d lastEchotemp = new Point3d();
    /*
     public void setTransfMatrix(Mat4D transfMatrix) {
     this.transfMatrix = transfMatrix;
     }

     public void setRotation(Mat3D rotation) {
     this.rotation = rotation;
     }
     */

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

        //test
        /*
         float test = terrain.getSimpleHeight(-12.0f, 260.0f);
         float test2 = terrain.getSimpleHeight(-12.5f, 260.5f);
         float test3 = terrain.getSimpleHeight(-11.5f, 260.5f);
         float test4 = terrain.getSimpleHeight(-12.5f, 259.5f);
         float test5 = terrain.getSimpleHeight(-11.5f, 259.5f);
         float test6 = terrain.getSimpleHeight(-12.6f, 260.49f);
         */
        if (terrain != null && parameters.useDTMCorrection()) {
            distance = z - (float) (terrain.getSimpleHeight(x, y));
            //System.out.println(distance);
        }

        return distance;
    }

    public VoxelAnalysis(LinkedBlockingQueue<Shot> arrayBlockingQueue, Dtm terrain) {
        // = new BoundingBox3d();
        nbShotsTreated = 0;
        isFinished = new AtomicBoolean(false);
        this.arrayBlockingQueue = arrayBlockingQueue;
        listeners = new EventListenerList();
        this.terrain = terrain;
    }

    public Point3d getPosition(Point3i indices, Point3i splitting, Point3d minCorner, Point3d maxCorner) {

        Point3d resolution = new Point3d();
        resolution.x = (maxCorner.x - minCorner.x) / splitting.x;
        resolution.y = (maxCorner.y - minCorner.y) / splitting.y;
        resolution.z = (maxCorner.z - minCorner.z) / splitting.z;

        double posX = offset.x + (resolution.x / 2.0d) + (indices.x * resolution.x);
        double posY = offset.y + (resolution.y / 2.0d) + (indices.y * resolution.y);
        double posZ = offset.z + (resolution.z / 2.0d) + (indices.z * resolution.z);

        //System.out.println(posZ);
        return new Point3d(posX, posY, posZ);
    }

    public void init(VoxelParameters parameters, File outputFile) {

        this.parameters = parameters;
        this.outputFile = outputFile;

        switch (parameters.getWeighting()) {

            case VoxelParameters.WEIGHTING_ECHOS_NUMBER:

                if (parameters.isTLS()) {
                    weighting = new float[][]{
                        {1.00f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.50f, 0.50f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.33f, 0.33f, 0.33f, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.25f, 0.25f, 0.25f, 0.25f, Float.NaN, Float.NaN, Float.NaN},
                        {0.20f, 0.20f, 0.20f, 0.20f, 0.20f, Float.NaN, Float.NaN},
                        {0.16f, 0.16f, 0.16f, 0.16f, 0.16f, 0.16f, Float.NaN},
                        {0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f}};
                } else {
                    weighting = new float[][]{
                        {1.00f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.62f, 0.38f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.40f, 0.35f, 0.25f, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.28f, 0.29f, 0.24f, 0.19f, Float.NaN, Float.NaN, Float.NaN},
                        {0.21f, 0.24f, 0.21f, 0.19f, 0.15f, Float.NaN, Float.NaN},
                        {0.16f, 0.21f, 0.19f, 0.18f, 0.14f, 0.12f, Float.NaN},
                        {0.15f, 0.17f, 0.15f, 0.16f, 0.12f, 0.19f, 0.06f}};
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

        offset = new Point3d(parameters.bottomCorner);
        //groundEnergy = new GroundEnergy[parameters.split.x][parameters.split.y];

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

                    if (shot != null) {

                        shotID = nbShotsTreated;

                        //if(angle > 9.564216){
                        if (nbShotsTreated % 1000000 == 0 && nbShotsTreated != 0) {
                            logger.info("Shots treated: " + nbShotsTreated);
                        }

                        shot.direction.normalize();
                        Point3d origin = new Point3d(shot.origin);

                        if (shot.nbEchos == 0) {

                            LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);

                            Point3d echo = seg.getEnd();
                            propagate(origin, echo, (short) 0, 0, 1, shot.origin, false, shot.angle, shot.nbEchos, 0);

                        } else {

                            double beamFraction = 1;
                            int sumIntensities = 0;

                            if (!parameters.isTLS()) {
                                for (int i = 0; i < shot.nbEchos; i++) {
                                    sumIntensities += shot.intensities[i];
                                }
                            }

                            double bfIntercepted = 1;

                            shotChanged = true;
                            double residualEnergy = 1;

                            for (int i = 0; i < shot.nbEchos; i++) {

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

                                        bfIntercepted = weighting[shot.nbEchos - 1][i];

                                        break;

                                    default:
                                        bfIntercepted = 1;
                                        break;

                                }

                                LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[i]);

                                Point3d echo = seg.getEnd();

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
                                    residualEnergy -= (bfIntercepted / shot.nbEchos);
                                }

                                origin = new Point3d(echo);
                            }
                        }

                        nbShotsTreated++;
                    }

                    //}
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }

            logger.info("Shots treated: " + nbShotsTreated);
            logger.info("voxelisation is finished ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

            calculatePADAndWrite(0);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

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

                //vox.lgNoInterception += longueur;
                //vox.lgOutgoing += longueur;
                vox.lgTotal += longueur;

                vox.nbSampling++;
                //vox.nbOutgoing++;
                nbSamplingTotal++;
                
                

                //vox.bfEntering += residualEnergy;
                //vox.bsEntering += (surface * residualEnergy);
                vox.angleMean += angle;

                if (!parameters.isTLS()) {
                    ((ALSVoxel) vox).bvEntering += (surface * residualEnergy * longueur);
                    //((ALSVoxel)vox).bvOutgoing += (surface * residualEnergy * longueur);
                } else {
                    ((TLSVoxel) vox).bflEntering += (residualEnergy * longueur);
                    //((TLSVoxel)vox).bflOutgoing += (residualEnergy * longueur);
                }

                /*
                 Si l'écho est sur la face sortante du voxel, 
                 on n'incrémente pas le compteur d'échos
                 */
                //lastIntercepted = false;
            } /*
             Poursuite du trajet optique jusqu'à sortie de la bounding box
             */ else if (d1 > distanceToHit) {

                /*
                 la distance actuelle d1 est supérieure à la distance à l'écho
                 ce qui veut dire que l'écho a déjà été trouvé
                 */
                //on peut chercher ici la distance jusqu'au prochain voxel "sol"
                if (shotChanged) {

                    if (vox.ground_distance < 1) {
                        //groundEnergy[vox.i][vox.j].groundEnergyPotential ++;

                        shotChanged = false;
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
                if ((echoDistance >= parameters.minDTMDistance && parameters.useDTMCorrection()) || !parameters.useDTMCorrection()) {
                    if ((classification != 2 && !parameters.isTLS()) || parameters.isTLS()) { // if not ground

                        /*test pour savoir si l'écho précédent du tir était dans cette maille
                        cette condition est vérifiée si l'écho précédent du même tir était dans ce voxel*/
                        if (lastIndices.equals(indices) && lastShotID == shotID) {
                            
                            indicesEchosTemp.add(indiceEcho);
                            
                            
                            lastDistanceToHit = distanceToHit;
                            lastD1 = d1;
                            lastD2 = d2;
                            
                            //on incrémente les fractions de faisceau 
                            
                            //cas où il y a déjà un écho dans le voxel
                            if(nbEchosInsideVoxel == 1){
                                sumBeamFraction += lastBeamFraction;
                            }
                            
                            sumBeamFraction += beamFraction;
                            
                            
                            wasEqual = true;

                            nbEchosInsideVoxel++;
                            
                            lastSurface = surface;

                        } /* cas où précédemment plusieurs échos étaient dans le même voxel
                        Attention: le contexte a changé, nous ne sommes plus dans le même voxel
                         étape 1: on doit décrémenter les valeurs attribuées lors du premier écho 
                         (on ne savait pas alors que les échos suivants seraient dans le même voxel)
                        
                         étape 2:on traite les échos du voxel comme un seul écho,
                         on incrémente donc les valeurs dans le voxel comme si un seul écho 
                         avait été à l'intérieur*/ 
                        else if (wasEqual) {
                            
                            count2++;
                            
                            
                            //sumBeamFraction += lastBeamFraction;
                            //assert sumBeamFraction <= 1.0 : "sumBeamFraction cannot be greater than 1, value: "+sumBeamFraction;
                            
                            assert nbEchosInsideVoxel <= lastNbEchos : "le nombre d'échos du même tir dans le voxel ne peut pas être supérieur au nombre d'échos du tir";
                            /*
                            indicesEchosTemp.add(lastIndice);
                            double testTemp = 0;
                            for(Integer t:indicesEchosTemp){
                                testTemp += weighting[lastNbEchos-1][t];
                            }
                            
                            assert Math.abs(sumBeamFraction - testTemp)<0.01 : "error add sum beam fraction";
                            */
                            Voxel lastVox = voxels[lastIndices.x][lastIndices.y][lastIndices.z];
                            
                            //lastVox.lgOutgoing -= lastLongueur;
                            double longueur;
                            
                            longueur = (lastD2 - lastD1);
                            
                            //if(parameters.isTLS()){
                                
                            if(!lastEcho){
                                    
                            }else{
                                longueur = (lastDistanceToHit - lastD1);
                            }
                                
                            //}

                            //if(!lastEcho){
                            
                                //lastVox.lgOutgoing += longueur;
                            //lastVox.nbOutgoing ++; // ?? vraiment sortant, je ne crois pas
                            /*}else{
                             longueur = (lastDistanceToHit - lastD1);
                             //lastVox.Lg_Exiting += longueur;
                             }*/
                            lastVox.lgTotal -= lastLongueur;
                            lastVox.lgTotal += longueur;

                            if (parameters.isTLS()) {
                                //ne rien faire, a déjà été incrémenté une fois
                                ((TLSVoxel) lastVox).bflEntering -= lastEntering;
                                ((TLSVoxel) lastVox).bflEntering += (lastBeamFraction * longueur);
                                ((TLSVoxel) lastVox).bflIntercepted -= lastEntering;
                                ((TLSVoxel) lastVox).bflIntercepted += (lastBeamFraction * longueur);
                                
                            } else {
                                ((ALSVoxel) lastVox).bvEntering -= lastEntering;
                                ((ALSVoxel) lastVox).bvEntering += (lastSurface * sumBeamFraction * longueur);
                                ((ALSVoxel) lastVox).bvIntercepted -= lastEntering;
                                ((ALSVoxel) lastVox).bvIntercepted += (lastSurface * sumBeamFraction * longueur);
                            }

                            //on réinitialise les variables temporaires
                            sumBeamFraction = 0;
                            nbEchosInsideVoxel = 1;
                            wasEqual = false;
                            indicesEchosTemp = new ArrayList<>();

                        } else if (!wasEqual) {
                            
                            wasEqual = false;
                            sumBeamFraction = 0;
                            /*
                             * Si distanceToHit == d1,on incrémente le compteur d'échos
                             */

                            vox.nbSampling++;

                            nbSamplingTotal++;

                            double longueur;
                            longueur = (d2 - d1);
                            
                            
                            //if(parameters.isTLS()){
                                
                                if(!lastEcho){
                                
                                //vox.lgOutgoing += longueur;
                                /*
                                if(!parameters.isTLS()){
                                ((ALSVoxel)vox).bvOutgoing += (surface * residualEnergy * longueur);
                                }else{
                                ((TLSVoxel)vox).bflOutgoing += (residualEnergy * longueur);
                                }

                                vox.nbOutgoing ++;
                                */
                               }else{
                                   longueur = (distanceToHit - d1);
                               }
                                
                            //}
                            

                            if(indices.x == 0 && indices.y == 33 && indices.z == 5){
                                System.out.println("test");
                            }
                            
                            //lastSurface = surface;
                            lastLongueur = longueur;

                            vox.lgTotal += longueur;

                            vox.nbEchos++;
                            vox.angleMean += angle;

                            //vox.bfIntercepted += beamFraction;
                            //vox.bsIntercepted += (surface * beamFraction);
                            lastShotID = shotID;
                            lastIndices = new Point3i(indices);

                            //??? utilisation de residualEnergy ??
                            //vox.bfEntering += beamFraction;
                            //vox.bsEntering += (surface * beamFraction);
                            double entering;

                            if (!parameters.isTLS()) {
                                entering = (surface * beamFraction * longueur);
                                ((ALSVoxel) vox).bvEntering += entering;
                                ((ALSVoxel) vox).bvIntercepted += entering;
                            } else {
                                entering = (beamFraction * longueur);
                                ((TLSVoxel) vox).bflEntering += entering;
                                ((TLSVoxel) vox).bflIntercepted += entering;
                            }

                            lastEntering = entering;
                            lastIndice = indiceEcho;
                            lastBeamFraction = beamFraction;
                            lastNbEchos = nbEchos;

                        }
                    } else {
                        //groundEnergy[vox.i][vox.j].groundEnergyActual += beamFraction;
                    }

                }

                lastEchotemp = echo;
            }
        }

    
    }
    
    public void calculatePADAndWrite(double threshold) {

        long start_time = System.currentTimeMillis();

        logger.info("writing file: " + outputFile.getAbsolutePath());
        /*
         BufferedWriter writerTemp;
         try {
         writerTemp = new BufferedWriter(new FileWriter("c:\\Users\\Julien\\Desktop\\groundEnergy.txt"));
         writerTemp.write("i j groundEnergyActual groundEnergyPotential\n");
            
         for (int i = 0; i < parameters.split.x; i++) {
         for (int j = 0; j < parameters.split.y; j++) {
         writerTemp.write(i+" "+j+" "+groundEnergy[i][j].groundEnergyActual+ " "+groundEnergy[i][j].groundEnergyPotential+"\n");
         }
         }
            
         writerTemp.close();
         } catch (IOException ex) {
         logger.error(ex);
         }
         */

        //2097152 octets = 2 mo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + voxSpace.getBoundingBox().min.x + " " + voxSpace.getBoundingBox().min.y + " " + voxSpace.getBoundingBox().min.z + "\n");
            writer.write("#max_corner: " + voxSpace.getBoundingBox().max.x + " " + voxSpace.getBoundingBox().max.y + " " + voxSpace.getBoundingBox().max.z + "\n");
            writer.write("#split: " + voxSpace.getSplitting().x + " " + voxSpace.getSplitting().y + " " + voxSpace.getSplitting().z + "\n");

            writer.write("#offset: " + (float) offset.x + " " + (float) offset.y + " " + (float) offset.z + "\n");

            //writer.write("i j k shots path_length BFintercepted BFentering BSintercepted BSentering PAD"+"\n");
            //writer.write("i j k nbSampling interceptions path_length lgTraversant lgInterception PAD PAD2 BFIntercepted BFEntering BSIntercepted BSEntering dist"+"\n");
            
            if (parameters.isTLS()) {
                writer.write(TLSVoxel.getHeader() + "\n");
            }else{
                writer.write(ALSVoxel.getHeader() + "\n");
            }
            
            //writer.write("i j k BFEntering BFIntercepted BSEntering BSIntercepted path_length lgTraversant PadBF PadBS dist nbSampling nbEchos nbOutgoing lMean angleMean" + "\n");

            System.out.println("mean: " + meanLongTemp / nbLongTemp);

            for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel vox = voxels[i][j][k];

                        float /*pad1, pad2, */ pad3;
                        
                        vox.angleMean = vox.angleMean / vox.nbSampling;

                        if (vox.nbSampling >= vox.nbEchos) {

                            //vox.lMeanOutgoing = vox.lgOutgoing / (vox.nbOutgoing);
                            //vox.LMean_NoInterception = vox.lgNoInterception / (vox.nbSampling - vox.nbEchos);
                            vox.lMeanTotal = vox.lgTotal / (vox.nbSampling);

                        }

                        if (parameters.isTLS()) {

                            TLSVoxel tlsVox = (TLSVoxel) vox;

                            /**
                             * *PADBV**
                             */
                            if (tlsVox.bflEntering <= threshold) {

                                //pad1 = Float.NaN;
                                //pad2 = Float.NaN;
                                pad3 = Float.NaN;

                            } else if (tlsVox.bflIntercepted > tlsVox.bflEntering) {

                                logger.error("Voxel : " + tlsVox.$i + " " + tlsVox.$j + " " + tlsVox.$k + " -> bflInterceptes > bflEntering, NaN assigné");
                                //pad1 = Float.NaN;
                                //pad2 = Float.NaN;
                                pad3 = Float.NaN;

                            } else {

                                tlsVox.transmittance = (tlsVox.bflEntering - tlsVox.bflIntercepted) / tlsVox.bflEntering;

                                if (tlsVox.nbSampling > 1 && tlsVox.transmittance == 0 && tlsVox.nbSampling == tlsVox.nbEchos) {

                                    //pad1 = MAX_PAD;
                                    //pad2 = MAX_PAD;
                                    pad3 = MAX_PAD;

                                } else if (tlsVox.nbSampling <= 2 && tlsVox.transmittance == 0 && tlsVox.nbSampling == tlsVox.nbEchos) {

                                    //pad1 = Float.NaN;
                                    //pad2 = Float.NaN;
                                    pad3 = Float.NaN;

                                } else {

                                   //pad1 = (float) (Math.log(tlsVox.transmittance) / (-0.5 * vox.lMeanOutgoing));
                                    //pad2 = (float) (Math.log(tlsVox.transmittance) / (-0.5 * vox.LMean_NoInterception));
                                    pad3 = (float) (Math.log(tlsVox.transmittance) / (-0.5 * tlsVox.lMeanTotal));
                                    /*
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
                                     */
                                    if (Float.isNaN(pad3)) {
                                        pad3 = Float.NaN;
                                    } else if (pad3 > MAX_PAD || Float.isInfinite(pad3)) {
                                        pad3 = MAX_PAD;
                                    }
                                }

                            }

                            //vox.PadBVOutgoing = pad1 + 0.0f; //set +0.0f to avoid -0.0f
                            //vox.PadBVNoInterceptions = pad2 + 0.0f; //set +0.0f to avoid -0.0f
                            tlsVox.PadBflTotal = pad3 + 0.0f; //set +0.0f to avoid -0.0f

                            writer.write(tlsVox.toString() + "\n");

                        } else {

                            ALSVoxel alsVox = (ALSVoxel) vox;

                            /**
                             * *PADBV**
                             */
                            if (alsVox.bvEntering <= threshold) {

                                //pad1 = Float.NaN;
                                //pad2 = Float.NaN;
                                pad3 = Float.NaN;

                            } else if (alsVox.bvIntercepted > alsVox.bvEntering) {

                                logger.error("Voxel : " + alsVox.$i + " " + alsVox.$j + " " + alsVox.$k + " -> bvInterceptes > bvEntering, NaN assigné, difference: "+ (alsVox.bvEntering-alsVox.bvIntercepted));
                                //pad1 = Float.NaN;
                                //pad2 = Float.NaN;
                                pad3 = Float.NaN;

                            } else {

                                alsVox.transmittance = (alsVox.bvEntering - alsVox.bvIntercepted) / alsVox.bvEntering;

                                if (alsVox.nbSampling > 1 && alsVox.transmittance == 0 && alsVox.nbSampling == alsVox.nbEchos) {

                                    //pad1 = MAX_PAD;
                                    //pad2 = MAX_PAD;
                                    //pad3 = MAX_PAD;
                                    pad3 = Float.NaN;

                                } else if (alsVox.nbSampling <= 2 && alsVox.transmittance == 0 && alsVox.nbSampling == alsVox.nbEchos) {

                                    //pad1 = Float.NaN;
                                    //pad2 = Float.NaN;
                                    pad3 = Float.NaN;

                                } else {

                                   //pad1 = (float) (Math.log(alsVox.transmittance) / (-0.5 * vox.lMeanOutgoing));
                                    //pad2 = (float) (Math.log(alsVox.transmittance) / (-0.5 * vox.LMean_NoInterception));
                                    pad3 = (float) (Math.log(alsVox.transmittance) / (-0.5 * alsVox.lMeanTotal));
                                    /*
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
                                     */
                                    if (Float.isNaN(pad3)) {
                                        pad3 = Float.NaN;
                                    } else if (pad3 > MAX_PAD || Float.isInfinite(pad3)) {
                                        pad3 = MAX_PAD;
                                    }
                                }

                            }

                            //vox.PadBVOutgoing = pad1 + 0.0f; //set +0.0f to avoid -0.0f
                            //vox.PadBVNoInterceptions = pad2 + 0.0f; //set +0.0f to avoid -0.0f
                            alsVox.PadBVTotal = pad3 + 0.0f; //set +0.0f to avoid -0.0f

                            writer.write(alsVox.toString() + "\n");
                        }

                    }
                }
            }

            writer.close();

            logger.info("file writed ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

        } catch (FileNotFoundException e) {
            logger.error("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Error: " + e.getMessage());
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
                        
                        Point3d position = getPosition(new Point3i(voxels[x][y][z].$i, voxels[x][y][z].$j, voxels[x][y][z].$k),
                                parameters.split, parameters.bottomCorner, parameters.topCorner);

                        float dist;
                        if (terrain != null && parameters.useDTMCorrection()) {
                            dist = (float) (position.z - terrain.getSimpleHeight((float) position.x, (float) position.y));
                        } else {
                            dist = (float) (position.z);
                        }
                        voxels[x][y][z].setDist(dist);
                        voxels[x][y][z].setPosition(position);
                    }
                }
            }
            /*
             for (int i = 0; i < parameters.split.x; i++) {
             for (int j = 0; j < parameters.split.y; j++) {
             groundEnergy[i][j] = new GroundEnergy();
             }
             }
             */
            Scene scene = new Scene();
            scene.setBoundingBox(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner));

            voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

            voxelManager.showInformations();

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }
}
