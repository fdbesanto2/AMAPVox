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
import fr.ird.voxelidar.util.TimeCounter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

public class VoxelAnalysis implements Runnable {

    private final static Logger logger = Logger.getLogger(VoxelAnalysis.class);

    private VoxelSpace voxSpace;
    private Voxel voxels[][][];
    private VoxelManager voxelManager;

    private final double laserBeamDivergence = 0.0005f;
    private static final float MAX_PAD = 3;

    private int nbShotsTreated;
    private File outputFile;
    private static int compteur = 1;
    private static int compteur2 = 1;
    private Point3d offset;

    private float[][] weighting;
    //private GroundEnergy[][] groundEnergy;
    
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int nbEchosSol = 0;
    private VoxelParameters parameters;
    private boolean isTLS;
    private final LinkedBlockingQueue<Shot> arrayBlockingQueue;

    //private Mat4D transfMatrix;
    //private Mat3D rotation;
    private AtomicBoolean isFinished;

    private final EventListenerList listeners;
    private Dtm terrain;

    Point3i lastIndices = new Point3i();
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
    
    public class GroundEnergy{
        
        public int groundEnergyPotential;
        public float groundEnergyActual;
        
        public GroundEnergy(){
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

                weighting = new float[][]{{1.00f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                {0.62f, 0.38f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                {0.40f, 0.35f, 0.25f, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                {0.28f, 0.29f, 0.24f, 0.19f, Float.NaN, Float.NaN, Float.NaN},
                {0.21f, 0.24f, 0.21f, 0.19f, 0.15f, Float.NaN, Float.NaN},
                {0.16f, 0.21f, 0.19f, 0.18f, 0.14f, 0.12f, Float.NaN},
                {0.15f, 0.17f, 0.15f, 0.16f, 0.12f, 0.19f, 0.06f}};

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
        isTLS = parameters.isTLS();
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

                        //Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
                        //Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));
                        //shot.origin = new Point3d((double)locVector.x, (double)locVector.y, (double)locVector.z);
                        //shot.direction = new Vector3f((double)uVector.x, (double)uVector.y, (double)uVector.z);
                        //String line = "\""+time+"\""+" "+shot.nbEchos+" "+shot.origin.x+" "+shot.origin.y+" "+shot.origin.z+" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z;
                        //String line = shot.nbEchos+" "+shot.origin.x+" "+shot.origin.y+" "+shot.origin.z+" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z;
                        //for(int i=0;i<shot.ranges.length;i++){
                        //    line+=" " + shot.ranges[i];
                        //}
                        //writer.write(line+"\n");
                        if (nbShotsTreated % 1000000 == 0 && nbShotsTreated != 0) {
                            logger.info("Shots treated: " + nbShotsTreated);
                        }

                        shot.direction.normalize();
                        Point3d origin = new Point3d(shot.origin);
                        
                        double angle = Math.toDegrees(Math.acos(Math.abs(shot.direction.z)));
                        
                        //System.out.println((float)Math.toDegrees(angle));
                        if (shot.nbEchos == 0) {
                            logger.info("test");
                            LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);

                            Point3d echo = seg.getEnd();
                            propagate(origin, echo, (short) 0, 0, 1, shot.origin, false, angle);

                        } else {

                            double beamFraction = 1;
                            int sumIntensities = 0;

                            if (!isTLS) {
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
                                /*
                                count2++;
        
                                if(count2==1254){
                                    System.out.println("test");
                                }
                                */
                                LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[i]);

                                Point3d echo = seg.getEnd();

                        //this.voxSpace.getBoundingBox().update(echo);
                                //calcul de la fraction de section de faisceau intercepté
                                if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {
                                    beamFraction = bfIntercepted;
                                }

                                boolean lastEcho;

                                
                                lastEcho = i == shot.nbEchos - 1;
                                
                                
        
                                // propagate
                                if (isTLS) {
                                    propagate(origin, echo, (short) 0, beamFraction, residualEnergy, shot.origin, lastEcho, angle);
                                } else {
                                    propagate(origin, echo, shot.classifications[i], beamFraction, residualEnergy, shot.origin, lastEcho, angle);
                                }

                                if (parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE) {

                                    residualEnergy -= bfIntercepted;
                                }

                                /*
                                 double distance = lastEcho.distance(echo);
                                 if(distance<Math.sqrt(3) && !shotChanged){
                                 System.out.println(distance);
                                 //count1++;
                                 }
                                 */
                        //lastEcho = new Point3d(echo);
                                //lastShot = nbShotsTreated;
                                //shotChanged = false;
                                origin = new Point3d(echo);
                            }
                        }

                        if (first) {
                            logger.info(compteur2);
                            compteur2++;
                            first = false;
                        }

                        nbShotsTreated++;
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }

        //writer.close();
            //} catch (IOException ex) {
            //  java.util.logging.Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
            //}
            logger.info("Shots treated: " + nbShotsTreated);
            logger.info("voxelisation is finished ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

            calculatePADAndWrite(0);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }

    }

    /**
     *
     * @param shot
     */
    /*
     public void voxelise(Shot shot){
        
     if(nbShotsTreated % 1000000 == 0){
     logger.debug("Shots treated: "+nbShotsTreated);
     //System.out.println("Shots treated: "+nbShotsTreated);
     }
        
     shot.direction.normalize();
     Point3d origin = new Point3d(shot.origin);
        
        
        
     if (shot.nbEchos == 0) {
                        
     LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);

     Point3d echo = seg.getEnd();
     propagate(origin, echo, 0, shot.origin);
            
     } else {
            
     double beamFraction = 1;
            
            
            
     for (int i = 0; i < shot.nbEchos; i++) {
                
                    
     double bfIntercepted = weighting[shot.nbEchos-1][i];

     LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[i]);

     //get echo position
     Point3d echo = seg.getEnd();

     bbox.update(echo);

     // propagate
     propagate(origin, echo, beamFraction, shot.origin);

     //calcul de la fraction de section de faisceau intercepté
     if(parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE){
     beamFraction = beamFraction - bfIntercepted;
     }


     origin = new Point3d(echo);
                
     }
     }
        
     nbShotsTreated++;
     }
     */
    /**
     *
     * @param origin current origin (origin start from the last echo)
     * @param echo current echo (position in voxel space)
     * @param beamFraction
     * @param source shot origin
     */
    private void propagate(Point3d origin, Point3d echo, short classification, double beamFraction, double residualEnergy, Point3d source, boolean lastEcho, double angle) {

        //get shot line
        LineElement lineElement = new LineSegment(origin, echo);

        //get the first voxel cross by the line
        VoxelCrossingContext context = voxelManager.getFirstVoxel(lineElement);
        
        count3++;
        
        if(count3 == 1326){
            System.out.println("test");
        }
        
        
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
            /******************A verifier si il vaut mieux prendre la distance 
             * du centre du voxel à la source ou de l'écho à la source**********************/
            double distance = vox._position.distance(source);

            //surface de la section du faisceau à la distance de la source
            double surface = Math.pow(Math.tan(laserBeamDivergence / 2) * distance, 2) * Math.PI;

            // voxel sampled without hit
            /*Si un écho est positionné sur une face du voxel alors il est considéré
             comme étant à l'extérieur du dernier voxel traversé*/
            /*
            if(d2 == d1){
                if(d1 == 0){
                    count1++;
                }
                /*
                count1++;
                double test = (int)lastEchotemp.z-lastEchotemp.z;
                if(test == 0){
                    count3++;
                }
                */
                //System.out.println("test");
            //}else{
                
                /*
                * Si d2 < distanceToHit le voxel est traversé sans interceptions
                */
                if (d2 <= distanceToHit) {
                
                    vox.Lg_NoInterception += (d2 - d1);
                    vox.Lg_Exiting += (d2 - d1);

                    vox.nbSampling++;
                    vox.nbOutgoing++;
                    nbSamplingTotal++;

                    vox.bfEntering += residualEnergy;
                    vox.bsEntering += (surface * residualEnergy);
                    
                    vox.angleMean += angle;
                    /*
                    Si l'écho est sur la face sortante du voxel, 
                    on n'incrémente pas le compteur d'échos
                    */
                    
                    //lastIntercepted = false;

                }
                /*
                * Poursuite du trajet optique jusqu'à sortie de la bounding box
                */
                else if (d1 > distanceToHit) {

                    /*
                        la distance actuelle d1 est supérieure à la distance à l'écho
                        ce qui veut dire que l'écho a déjà été trouvé
                    */
                    
                    //on peut chercher ici la distance jusqu'au prochain voxel "sol"
                    if(shotChanged){
                        
                        if(vox.ground_distance < 1){
                            //groundEnergy[vox.i][vox.j].groundEnergyPotential ++;
                            
                            shotChanged = false;
                            context = null;
                        }
                        
                    }
                    
                    
                } else {

                    /*si plusieurs échos issus du même tir dans le voxel, 
                     on incrémente et le nombre de tirs entrants (nbsampling) 
                     et le nombre d'interception (interceptions) 
                     et la longueur parcourue(lgInterception)*/
                    if ((echoDistance >= parameters.minDTMDistance && parameters.useDTMCorrection()) || !parameters.useDTMCorrection()) {
                        if ((classification != 2 && !isTLS) || isTLS) { // if not ground


                            /*
                            * Si distanceToHit == d1,on incrémente le compteur d'échos
                            */

                            vox.nbSampling++;

                            nbSamplingTotal++;
                            
                            if(!lastEcho){
                                vox.Lg_Exiting += (d2 - d1);
                                vox.nbOutgoing ++;
                            }
                            
                            vox.nbEchos++;
                            vox.angleMean += angle;
                            
                            vox.bfIntercepted += beamFraction;
                            vox.bsIntercepted += (surface * beamFraction);

                            vox.bfEntering += beamFraction;
                            vox.bsEntering += (surface * beamFraction);
                        }
                    }else{
                        //groundEnergy[vox.i][vox.j].groundEnergyActual += beamFraction;
                    }

                }
            //}
            
            
        }
        
        lastEchotemp = echo;
        /*
        if(lastEcho){
            
        }
        */
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
            java.util.logging.Logger.getLogger(VoxelAnalysis.class.getName()).log(Level.SEVERE, null, ex);
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
            
            
            writer.write(Voxel.getHeader() + "\n");
            //writer.write("i j k BFEntering BFIntercepted BSEntering BSIntercepted path_length lgTraversant PadBF PadBS dist nbSampling nbEchos nbOutgoing lMean angleMean" + "\n");

            for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel vox = voxels[i][j][k];

                        float PAD, PAD2;
                        
                        vox.angleMean = vox.angleMean/vox.nbSampling;
                        
                        if(parameters.isTLS()){
                            
                            if (vox.nbSampling > vox.nbEchos) {
                                vox.LMean_Exiting = vox.Lg_NoInterception / (vox.nbSampling - vox.nbEchos);
                            }
                            
                        }else{
                            vox.LMean_NoInterception = vox.Lg_Exiting / (vox.nbSampling);
                            vox.LMean_Exiting = vox.Lg_Exiting / (vox.nbOutgoing);
                        }
                        //double l;

                        

                        /***PADBF***/
                        if (vox.bfEntering <= threshold) {

                            PAD = Float.NaN;

                        } else if (vox.bfIntercepted > vox.bfEntering) {
                            
                            logger.error("Voxel : "+vox.i+" "+vox.j+" "+vox.k+" -> BFInterceptes > BFEntering, NaN assigné");
                            PAD = Float.NaN;

                        } else {

                            double transmittance = (vox.bfEntering - vox.bfIntercepted) / vox.bfEntering;
                            //double l = vox.lgTraversant/(vox.nbSampling-vox.interceptions);
                            /*
                            if(!parameters.isTLS()){
                                logger.error("Voxel : "+vox.i+" "+vox.j+" "+vox.k+" -> transmittance et lMean nuls, NaN assigné");
                            }
                            */
                            
                            if(vox.nbSampling>1 && transmittance == 0 && vox.nbSampling == vox.nbEchos){
                                
                                PAD = MAX_PAD;
                                
                            }else if(vox.nbSampling<=2 && transmittance == 0 && vox.nbSampling == vox.nbEchos){
                                
                                PAD = Float.NaN;
                                
                            }else{
                                
                               PAD = (float) (Math.log(transmittance) / (-0.5 * vox.LMean_Exiting));
                               
                               if (Float.isNaN(PAD)) {
                                   PAD = Float.NaN;
                               } else if (PAD > MAX_PAD || Float.isInfinite(PAD)) {
                                   PAD = MAX_PAD;
                               }
                            }
                            
                        }

                        vox.PadBF = PAD + 0.0f; //set +0.0f to avoid -0.0f
                        
                        if (vox.bsEntering <= threshold) {

                            PAD = Float.NaN;

                        } else if (vox.bsIntercepted > vox.bsEntering) {
                            
                            logger.error("Voxel : "+vox.i+" "+vox.j+" "+vox.k+" -> BSInterceptes > BSEntering, NaN assigné");
                            PAD = Float.NaN;

                        } else {

                            double transmittance = (vox.bsEntering - vox.bsIntercepted) / vox.bsEntering;
                            //double l = vox.lgTraversant/(vox.nbSampling-vox.interceptions);
                             
                            if(vox.LMean_Exiting == 0 && transmittance == 0){
                                PAD = Float.NaN;
                                
                                if(!parameters.isTLS()){
                                    logger.error("Voxel : "+vox.i+" "+vox.j+" "+vox.k+" -> transmittance et lMean nuls, NaN assigné");
                                }
                                
                            }else{
                                
                               PAD = (float) (Math.log(transmittance) / (-0.5 * vox.LMean_Exiting));

                               if (Float.isNaN(PAD)) {
                                   PAD = Float.NaN;
                               } else if (PAD > MAX_PAD || Float.isInfinite(PAD)) {
                                   PAD = MAX_PAD;
                               }
                            }
                            
                        }

                        vox.PadBS = PAD + 0.0f; //set +0.0f to avoid -0.0f
                        
                        writer.write(vox.toString() + "\n");

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

    private String concatene(int x, int y, int z, Voxel vox) {

        //String voxLine = x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.interceptions+" "+vox.pathLength+" "+vox.lgTraversant+" "+vox.lgInterception+" "+vox.PAD+" "+vox.PAD2+" "+vox.bfIntercepted+" "+vox.bfEntering+" "+vox.bsIntercepted+" "+vox.bsEntering+" "+vox.dist+"\n";
        String voxLine = x + " " + y + " " + z + " " + vox.bfEntering + " " + vox.bfIntercepted + " " + vox.bsEntering + " " + vox.bsIntercepted + " " + vox.Lg_Exiting + " " + vox.Lg_NoInterception + " " + vox.PadBF + " " + vox.ground_distance + "\n";
        return voxLine;
    }

    private void createVoxelSpace() {

        try {
            voxSpace = new VoxelSpace(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner), parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

            // allocate voxels
            logger.info("allocate!!!!!!!!");

            voxels = new Voxel[parameters.split.x][parameters.split.y][parameters.split.z];

            for (int x = 0; x < parameters.split.x; x++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int z = 0; z < parameters.split.z; z++) {

                        voxels[x][y][z] = new Voxel(x, y, z);
                        Point3d position = getPosition(new Point3i(voxels[x][y][z].i, voxels[x][y][z].j, voxels[x][y][z].k),
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
