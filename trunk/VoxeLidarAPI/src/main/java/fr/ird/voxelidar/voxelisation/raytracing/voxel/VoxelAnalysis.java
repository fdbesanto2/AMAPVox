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
import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

public class VoxelAnalysis implements Runnable{
    
    private final static Logger logger = Logger.getLogger(VoxelAnalysis.class);
    
    private VoxelSpace voxSpace;
    private Voxel voxels[][][];
    private VoxelManager voxelManager;
    
    private final double laserBeamDivergence = 0.0005f;
    private static final float MAX_PAD = 3;
    
    private int nbShotsTreated;
    private File outputFile;
    private static int compteur = 1;
    private Point3d offset;
    
    private float[][] weighting;
    int count1 = 0;
    int count2 = 0;
    int count3 = 0;
    int nbEchosSol = 0;
    private VoxelParameters parameters;
    private boolean isTLS;
    private LinkedBlockingQueue<Shot> arrayBlockingQueue;
    
    //private Mat4D transfMatrix;
    //private Mat3D rotation;
    
    private boolean isFinished;
    
    private final EventListenerList listeners;
    private Dtm terrain;
    
    Point3i lastIndices = new Point3i();
    int lastShot = 0;
    boolean shotChanged = false;
    /*
    public void setTransfMatrix(Mat4D transfMatrix) {
        this.transfMatrix = transfMatrix;
    }

    public void setRotation(Mat3D rotation) {
        this.rotation = rotation;
    }
    */
    public void setIsFinished(boolean isFinished) {
        this.isFinished = isFinished;
    }
    
    int nbSamplingTotal = 0;

    public class Voxel implements Serializable{
        
        int i;
        int j;
        int k;
        int nbSampling = 0;
        int interceptions = 0;
        float lgTraversant = 0;
        float pathLength = 0;
        float PAD = 0;
        float PAD2 = 0;
        float bfEntering=0;
        float bfIntercepted=0;
        float bsEntering = 0;
        float bsIntercepted = 0;
        float dist = 10;
        Point3d position;
        
        public Voxel(int i, int j, int k){
            
            this.i = i;
            this.j = j;
            this.k = k;
            
            position = getPosition(new Point3i(i, j, k), parameters.split, parameters.bottomCorner, parameters.topCorner);
            
            if(terrain != null && parameters.useDTMCorrection()){
                dist = (float) (position.z - terrain.getSimpleHeight((float)position.x, (float)position.y));
            }else{
                dist = (float) (position.z);
            }
        }
        
        @Override
        public String toString() {
            
            StringBuilder sb =  new StringBuilder() ;
            
            return sb.append(i).append(" ").
                    append(j).append(" ").
                    append(k).append(" ").
                    append(bfEntering).append(" ").
                    append(bfIntercepted).append(" ").
                    append(bsEntering).append(" ").
                    append(bsIntercepted).append(" ").
                    append(pathLength).append(" ").
                    append(lgTraversant).append(" ").
                    append(PAD).append(" ").
                    append(PAD2).append(" ").
                    append(dist).append(" ").toString();
            
        }
    }
    
    private float getGroundDistance(float x, float y, float z){
        
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
        if(terrain != null && parameters.useDTMCorrection()){
            distance = z - (float) (terrain.getSimpleHeight(x, y));
            //System.out.println(distance);
        }
        
        return distance;
    }
    
    public VoxelAnalysis(LinkedBlockingQueue<Shot> arrayBlockingQueue, Dtm terrain){
        // = new BoundingBox3d();
        nbShotsTreated = 0;
        isFinished = false;
        this.arrayBlockingQueue = arrayBlockingQueue;
        listeners = new EventListenerList();
        this.terrain = terrain;
    }
    
    public Point3d getPosition(Point3i indices, Point3i splitting, Point3d minCorner, Point3d maxCorner){
        
        Point3d resolution = new Point3d();
        resolution.x = (maxCorner.x - minCorner.x) / splitting.x;
        resolution.y = (maxCorner.y - minCorner.y) / splitting.y;
        resolution.z = (maxCorner.z - minCorner.z) / splitting.z;

        double posX = offset.x+(resolution.x/2.0d)+(indices.x*resolution.x);
        double posY = offset.y+(resolution.y/2.0d)+(indices.y*resolution.y);
        double posZ = offset.z+(resolution.z/2.0d)+(indices.z*resolution.z);
        
        //System.out.println(posZ);
        
        return new Point3d(posX, posY, posZ);
    }
    
    
    public void init(VoxelParameters parameters, File outputFile){
        
        this.parameters = parameters;
        this.outputFile = outputFile;
        
        switch(parameters.getWeighting()){
            
            case VoxelParameters.WEIGHTING_ECHOS_NUMBER:
                
                weighting = new float[][]{{1.00f,Float.NaN,Float.NaN,Float.NaN,Float.NaN,Float.NaN,Float.NaN},
                                        {0.62f,0.38f,Float.NaN,Float.NaN,Float.NaN,Float.NaN,Float.NaN},
                                        {0.40f,0.35f,0.25f,Float.NaN,Float.NaN,Float.NaN,Float.NaN},
                                        {0.28f,0.29f,0.24f,0.19f,Float.NaN,Float.NaN,Float.NaN},
                                        {0.21f,0.24f,0.21f,0.19f,0.15f,Float.NaN,Float.NaN},
                                        {0.16f,0.21f,0.19f,0.18f,0.14f,0.12f,Float.NaN},
                                        {0.15f,0.17f,0.15f,0.16f,0.12f,0.19f,0.06f}};
                
                break;
                
            case VoxelParameters.WEIGHTING_NONE:
                
                weighting = new float[][]{{1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f}};
                
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
        
        createVoxelSpace();
        
    }
    
    @Override
    public void run() {
        
        long start_time = System.currentTimeMillis();
        
        //try {
            /***TEST : write shots file****/
            //BufferedWriter writer = new BufferedWriter(new FileWriter("C:\\Users\\Julien\\Desktop\\test_rxp.txt"));
            //writer.write("\"key\" \"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");
            //writer.write("\"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\"\n");            
 
        while(!isFinished || !arrayBlockingQueue.isEmpty()) {

            try {
                Shot shot = arrayBlockingQueue.take();
                
                

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
                
                if(nbShotsTreated % 1000000 == 0 && nbShotsTreated != 0){
                    logger.info("Shots treated: "+nbShotsTreated);
                }

                shot.direction.normalize();
                Point3d origin = new Point3d(shot.origin);

                if (shot.nbEchos == 0) {

                    LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);

                    Point3d echo = seg.getEnd();
                    propagate(origin, echo, (short)0, 0, shot.origin, false);

                } else {

                    double beamFraction = 1;
                    int sumIntensities = 0;
                    
                    if(!isTLS){
                        for (int i = 0; i < shot.nbEchos; i++) {
                            sumIntensities+=shot.intensities[i];
                        }
                    }
                    
                    double bfIntercepted = 1;
                    
                    shotChanged = true;
                    
                    for (int i = 0; i < shot.nbEchos; i++) {
                        
                        
                        switch(parameters.getWeighting()){
            
                            case VoxelParameters.WEIGHTING_FRACTIONING:
                                if(shot.nbEchos == 1){
                                    bfIntercepted = 1;
                                }else{
                                    bfIntercepted = (shot.intensities[i])/(double)sumIntensities; 
                                }
                                
                                break;
                                
                            case VoxelParameters.WEIGHTING_ECHOS_NUMBER:
                            case VoxelParameters.WEIGHTING_FILE:   
                                
                                bfIntercepted = weighting[shot.nbEchos-1][i];
                                break;
                                
                            default: 
                                bfIntercepted = 1;
                                break;
                                
                        }

                        LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[i]);

                        Point3d echo = seg.getEnd();

                        //bbox.update(echo);
                        
                        //calcul de la fraction de section de faisceau intercepté
                        if(parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE){
                            beamFraction = bfIntercepted;
                        }
                        
                        boolean lastEcho;
                        if(i == shot.nbEchos -1){
                            lastEcho = true;
                        }else{
                            lastEcho = false;
                        }
                        // propagate
                        if(isTLS){
                            propagate(origin, echo, (short)0, beamFraction, shot.origin, lastEcho);
                        }else{
                            propagate(origin, echo, shot.classifications[i], beamFraction, shot.origin, lastEcho);
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
                
                nbShotsTreated++;
                
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        }
        
        //writer.close();
        
        //} catch (IOException ex) {
          //  java.util.logging.Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
        //}
        
        logger.info("Shots treated: "+nbShotsTreated);
        logger.info("voxelisation is finished ( "+TimeCounter.getElapsedStringTimeInSeconds(start_time)+" )");
                    
        
        calculatePADAndWrite(0);

        

        arrayBlockingQueue = null;
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
    private void propagate(Point3d origin, Point3d echo, short classification, double beamFraction, Point3d source, boolean lastEcho) {
                
        //get shot line
        LineElement lineElement = new LineSegment(origin, echo);
        
        //get the first voxel cross by the line
        VoxelCrossingContext context = voxelManager.getFirstVoxel(lineElement);
        
        double distanceToHit = lineElement.getLength();
        
        //calculate ground distance
        float echoDistance = 0;
        
        //if(lastEcho){
            echoDistance = getGroundDistance((float)echo.x, (float)echo.y, (float)echo.z);
        //}
        
        //double distanceToHit = echo.distance(origin); //must be the same as "lineElement.getLength();"
        
        //distance de l'écho à la source
        //double distance = echo.distance(source);
        
        //surface de la section du faisceau à la distance de la source
        //double surface = Math.pow(Math.tan(laserBeamDivergence/2)* distance, 2)  * Math.PI;
        
        while ((context != null) && (context.indices != null)) {
             
            
            //distance from the last origin to the point in which the ray enter the voxel
            double d1 = context.length;
            
            //current voxel
            Point3i indices = context.indices;
            
            /*
            if(lastIntercepted && !shotChanged && lastIndices.x == indices.x && lastIndices.y == indices.y && lastIndices.z == indices.z){
                
                double distance = lastEcho.distance(echo);
                //System.out.println(lastIndices.x+" "+lastIndices.y+" "+lastIndices.z+" "+distance);
                count1++;
            }
            */
            
            
            context = voxelManager.CrossVoxel(lineElement, context.indices);
            /*
            //même voxel
            if(context != null && context.indices != null){
                
                if(context.indices.x == indices.x && context.indices.y == indices.y && context.indices.z == indices.z){

                    System.out.println("test");
                }
            }
            */
            //distance from the last origin to the point in which the ray exit the voxel
            double d2 = context.length;
            
            Voxel vox = voxels[indices.x][indices.y][indices.z];
            //indices = context.indices;
            //Point3d voxelPosition = getPosition(indices, parameters.split, parameters.bottomCorner, parameters.topCorner);
            
            //distance de l'écho à la source
            double distance = vox.position.distance(source);

            //surface de la section du faisceau à la distance de la source
            double surface = Math.pow(Math.tan(laserBeamDivergence/2)* distance, 2)  * Math.PI;
            
            // voxel sampled without hit
            
            /*Si un écho est positionné sur une face du voxel alors il est considéré
            comme étant à l'extérieur du dernier voxel traversé*/
            if (d2 <= distanceToHit) {
                /*
                if(indices.x == 0 && indices.y == 208 && indices.z == 35){
                    System.out.println("test");
                }
                */
                vox.lgTraversant += (d2 - d1);
                vox.pathLength += (d2 - d1);
                
                vox.nbSampling++;
                nbSamplingTotal++;
                
                vox.bfEntering += beamFraction;
                vox.bsEntering += (surface*beamFraction);
                //lastIntercepted = false;
                
            }else if (d1>distanceToHit) {
                
                //no comprende
                //lastIntercepted = false;
                
            }else {
                
                /*si plusieurs échos issus du même tir dans le voxel, 
                on incrémente et le nombre de tirs entrants (nbsampling) 
                et le nombre d'interception (interceptions) 
                et la longueur parcourue(lgInterception)*/
                
                //vox.lgTraversant += (distanceToHit - d1);
                
                
                //lastIntercepted = true;
                //lastIndices = indices;
                //lastShot = nbShotsTreated;
                
                if((classification != 2 && !isTLS) || isTLS){ // if not ground
                    if((echoDistance >= 2 && parameters.useDTMCorrection()) || !parameters.useDTMCorrection()){
                        
                        vox.nbSampling++;
                
                        nbSamplingTotal++;

                        vox.pathLength +=  (distanceToHit - d1);
                        vox.interceptions ++;

                        vox.bfIntercepted += beamFraction;
                        vox.bsIntercepted += (surface*beamFraction);
                        
                        vox.bfEntering += beamFraction;
                        vox.bsEntering += (surface*beamFraction);
                    }
                }
                       
            }
        }
    }
    
    public void calculatePADAndWrite(double threshold){
        
        long start_time = System.currentTimeMillis();
        
        logger.info("writing file: "+outputFile.getAbsolutePath());
        
        //2097152 octets = 2 mo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("VOXEL SPACE"+"\n");
            writer.write("#min_corner: "+voxSpace.getBoundingBox().min.x+" "+voxSpace.getBoundingBox().min.y+" "+voxSpace.getBoundingBox().min.z+"\n");
            writer.write("#max_corner: "+voxSpace.getBoundingBox().max.x+" "+voxSpace.getBoundingBox().max.y+" "+voxSpace.getBoundingBox().max.z+"\n");
            writer.write("#split: "+voxSpace.getSplitting().x+" "+voxSpace.getSplitting().y+" "+voxSpace.getSplitting().z+"\n");

            writer.write("#offset: "+(float)offset.x+" "+(float)offset.y+" "+(float)offset.z+"\n");

            //writer.write("i j k shots path_length BFintercepted BFentering BSintercepted BSentering PAD"+"\n");
            //writer.write("i j k nbSampling interceptions path_length lgTraversant lgInterception PAD PAD2 BFIntercepted BFEntering BSIntercepted BSEntering dist"+"\n");
            writer.write("i j k BFEntering BFIntercepted BSEntering BSIntercepted path_length lgTraversant PAD PAD2 dist"+"\n");
            
            for (int i = 0; i < parameters.split.x; i++) {
                for (int j = 0; j < parameters.split.y; j++) {
                    for (int k = 0; k < parameters.split.z; k++) {

                        Voxel vox = voxels[i][j][k];
                        
                        float PAD, PAD2;
                        
                        if(vox.bfEntering <= threshold){
                            
                            PAD = Float.NaN;
                            
                        }else if(vox.bfIntercepted >= vox.bfEntering){
                            
                            PAD = MAX_PAD;
                            
                        }else{
                            
                            double transmittance = (vox.bfEntering - vox.bfIntercepted)/vox.bfEntering;
                            double l = vox.lgTraversant/(vox.nbSampling-vox.interceptions);
                            
                            PAD = (float) (Math.log(transmittance)/(-0.5*l));
                            
                            if(Float.isNaN(PAD)){
                                PAD = Float.NaN;
                            }else if(PAD > MAX_PAD || Float.isInfinite(PAD)){
                                PAD = MAX_PAD;
                            }
                        }
                        
                        
                        vox.PAD = PAD+0.0f; //set +0.0f to avoid -0.0f
                        
                        if(vox.bsEntering <= threshold){
                            
                            PAD2 = Float.NaN;
                            
                        }else if(vox.bsIntercepted >= vox.bsEntering){
                            
                            PAD2 = MAX_PAD;
                            
                        }else{
                            
                            double transmittance = (vox.bsEntering - vox.bsIntercepted)/vox.bsEntering;
                            double l = vox.lgTraversant/(vox.nbSampling-vox.interceptions);
                            
                            PAD2 = (float) (Math.log(transmittance)/(-0.5*l));
                            
                            if(Float.isNaN(PAD2)){
                                PAD2 = Float.NaN;
                            }else if(PAD2 > MAX_PAD || Float.isInfinite(PAD2)){
                                PAD2 = MAX_PAD;
                            }
                        }
                        
                        
                        vox.PAD2 = PAD2+0.0f; //set +0.0f to avoid -0.0f
                        
                        /*
                        float PAD;
                        float PAD2;
                        if(vox.nbSampling <= threshold){
                            
                            PAD = Float.NaN;
                            PAD2 = Float.NaN;
                            
                        }else if(vox.bfIntercepted >= vox.bfEntering){
                            
                            PAD = MAX_PAD;
                            PAD2 = MAX_PAD;
                            
                        }else{
                            
                            //PAD 1
                            double transmittance = (vox.nbSampling - vox.interceptions)/vox.nbSampling;
                            double l = vox.lgTraversant/(vox.nbSampling-vox.interceptions); //longueur du trajet optique
                            PAD = (float) (Math.log(transmittance)/(-0.5*l));
                            
                            if(PAD > MAX_PAD){
                                PAD = MAX_PAD;
                            }else if(Float.isNaN(PAD) || Float.isInfinite(PAD)){
                                PAD = Float.NaN;
                            }
                            
                            //PAD 2
                            transmittance = (vox.bfEntering - vox.bfIntercepted)/vox.bfEntering;
                            l = vox.lgTraversant/(vox.bfEntering-vox.bfIntercepted);
                            PAD2 = (float) (Math.log(transmittance)/(-0.5*l));
                            
                            if(Float.isNaN(PAD2) || Float.isInfinite(PAD2)){
                                PAD2 = Float.NaN;
                            }else if(PAD2 > MAX_PAD){
                                PAD2 = MAX_PAD;
                            }
                        }
                        
                        vox.PAD = PAD+0.0f; //set +0.0f to avoid -0.0f
                        vox.PAD2 = PAD2+0.0f;
                        */
                        //String voxLine = concatene(i, j, k, vox);
                        
                        writer.write(vox.toString()+"\n");

                    }
                }
            }
            
            writer.close();
            
            logger.info("file writed ( "+TimeCounter.getElapsedStringTimeInSeconds(start_time)+" )");
            
        } catch (FileNotFoundException e) {
            logger.error("Error: " + e.getMessage());
        }catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
        
    }
    
    private String concatene(int x, int y, int z, Voxel vox){
        
        //String voxLine = x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.interceptions+" "+vox.pathLength+" "+vox.lgTraversant+" "+vox.lgInterception+" "+vox.PAD+" "+vox.PAD2+" "+vox.bfIntercepted+" "+vox.bfEntering+" "+vox.bsIntercepted+" "+vox.bsEntering+" "+vox.dist+"\n";
        String voxLine = x+" "+y+" "+z+" "+vox.bfEntering+" "+vox.bfIntercepted+" "+vox.bsEntering+" "+vox.bsIntercepted+" "+vox.pathLength+" "+vox.lgTraversant+" "+vox.PAD+" "+vox.dist+"\n";
        return voxLine;
    }
    
    private void createVoxelSpace() {

        voxSpace = new VoxelSpace(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner), parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

        // allocate voxels
        logger.info("allocate!!!!!!!!");
        
        voxels = new Voxel[parameters.split.x][parameters.split.y][parameters.split.z];
        
        for (int x = 0; x < parameters.split.x; x++) {
            for (int y = 0; y < parameters.split.y; y++) {
                for (int z = 0; z < parameters.split.z; z++) {
                    
                    voxels[x][y][z] = new Voxel(x, y, z);
                }
            }
        }

        Scene scene = new Scene();
        scene.setBoundingBox(new BoundingBox3d(parameters.bottomCorner, parameters.topCorner));

        voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

        voxelManager.showInformations();
    }
}
