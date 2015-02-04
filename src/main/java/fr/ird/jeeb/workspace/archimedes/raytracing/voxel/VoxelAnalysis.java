package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;
import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.ird.voxelidar.extraction.Shot;
import fr.ird.voxelidar.math.matrix.Mat3D;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
import fr.ird.voxelidar.math.vector.Vec4D;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.voxelisation.als.LasVoxelisation;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

public class VoxelAnalysis implements Runnable{
    
    private final static Logger logger = Logger.getLogger(VoxelAnalysis.class);
    
    private VoxelSpace voxSpace;
    private Voxel voxels[][][];
    private VoxelManager voxelManager;
    private final BoundingBox3f bbox;
    private final float laserBeamDivergence = 0.0005f;
    private int nbShotsTreated;
    private File outputFile;
    private static int compteur = 1;
    
    private float[][] weighting;
    
    private VoxelParameters parameters;
    private BlockingQueue<Shot> arrayBlockingQueue;
    
    //private Mat4D transfMatrix;
    //private Mat3D rotation;
    
    private boolean isFinished;
    
    private final EventListenerList listeners;
    
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

    public class Voxel {

        int nbSampling = 0;			// number of rays crossing the voxel
        float sampledVolume = 0;		// 
        float interceptions = 0;
        float hiddenVolume = 0;
        float lgTraversant = 0;
        float lgInterception = 0;
        float pathLength = 0;			// total path length of rays in voxel
        float PAD = 0;
        float PAD2 = 0;
        float bfEntering=0;
        float bfEnteringTemp=0;
        float bfIntercepted=0;
        float bsEntering = 0;
        float bsIntercepted = 0;
        
        public Point3f getPosition(Point3i indices, Point3i splitting, Point3f minCorner, Point3f maxCorner){
            
            float resolution = (maxCorner.x - minCorner.x) / splitting.x;
            
            float posX = indices.x+minCorner.x-(resolution/2.0f);
            float posY = indices.y+minCorner.y-(resolution/2.0f);
            float posZ = indices.z+minCorner.z-(resolution/2.0f);
            
            return new Point3f(posX, posY, posZ);
        }
    }
    
    public VoxelAnalysis(BlockingQueue<Shot> arrayBlockingQueue){
        bbox = new BoundingBox3f();
        nbShotsTreated = 0;
        isFinished = false;
        this.arrayBlockingQueue = arrayBlockingQueue;
        listeners = new EventListenerList();
        
        System.out.println(compteur);
        compteur++;
    }
    
    
    public void init(VoxelParameters parameters, File outputFile){
        
        this.parameters = parameters;
        this.outputFile = outputFile;
        
        switch(parameters.getWeighting()){
            
            case VoxelParameters.WEIGHTING_ECHOS_NUMBER:
                
                weighting = new float[][]{{1.00f,0.00f,0.00f,0.00f,0.00f,0.00f,0.00f},
                                        {0.62f,0.38f,0.00f,0.00f,0.00f,0.00f,0.00f},
                                        {0.40f,0.35f,0.25f,0.00f,0.00f,0.00f,0.00f},
                                        {0.28f,0.29f,0.24f,0.19f,0.00f,0.00f,0.00f},
                                        {0.21f,0.24f,0.21f,0.19f,0.15f,0.00f,0.00f},
                                        {0.16f,0.21f,0.19f,0.18f,0.14f,0.12f,0.00f},
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
            
        }
        
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

                //shot.origin = new Point3f((float)locVector.x, (float)locVector.y, (float)locVector.z);
                //shot.direction = new Vector3f((float)uVector.x, (float)uVector.y, (float)uVector.z);
                
                //String line = "\""+time+"\""+" "+shot.nbEchos+" "+shot.origin.x+" "+shot.origin.y+" "+shot.origin.z+" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z;
                String line = shot.nbEchos+" "+shot.origin.x+" "+shot.origin.y+" "+shot.origin.z+" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z;
                for(int i=0;i<shot.ranges.length;i++){
                    line+=" " + shot.ranges[i];
                }

                //writer.write(line+"\n");
                
                if(nbShotsTreated % 1000000 == 0){
                    System.out.println("Shots treated: "+nbShotsTreated);
                }

                shot.direction.normalize();
                Point3f origin = new Point3f(shot.origin);

                if (shot.nbEchos == 0) {

                    LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);

                    Point3f echo = seg.getEnd();
                    propagate(origin, echo, 0, shot.origin);

                } else {

                    float beamFraction = 1;

                    for (int i = 0; i < shot.nbEchos; i++) {

                        float bfIntercepted = weighting[shot.nbEchos-1][i];

                        LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[i]);

                        Point3f echo = seg.getEnd();

                        bbox.update(echo);

                        // propagate
                        propagate(origin, echo, beamFraction, shot.origin);

                        //calcul de la fraction de section de faisceau intercepté
                        if(parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE){
                            beamFraction = beamFraction - bfIntercepted;
                        }


                        origin = new Point3f(echo);
                    }
                }

                nbShotsTreated++;
            } catch (Exception e) {
                logger.error(e.getMessage());
            }
        }
        
        //writer.close();
        
        //} catch (IOException ex) {
          //  java.util.logging.Logger.getLogger(LasVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
        //}
        
        logger.info("voxelisation is finished ( "+TimeCounter.getElapsedTimeInSeconds(start_time)+" )");
                    
        
        calculatePADAndWrite(0);

        

        arrayBlockingQueue = null;
    }
    
    /**
     *
     * @param shot
     */
    public void voxelise(Shot shot){
        
        if(nbShotsTreated % 1000000 == 0){
            logger.debug("Shots treated: "+nbShotsTreated);
            //System.out.println("Shots treated: "+nbShotsTreated);
        }
        
        shot.direction.normalize();
        Point3f origin = new Point3f(shot.origin);
        
        
        
        if (shot.nbEchos == 0) {
                        
            LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);

            Point3f echo = seg.getEnd();
            propagate(origin, echo, 0, shot.origin);
            
        } else {
            
            float beamFraction = 1;
            
            
            
            for (int i = 0; i < shot.nbEchos; i++) {
                
                    
                float bfIntercepted = weighting[shot.nbEchos-1][i];

                LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[i]);

                //get echo position
                Point3f echo = seg.getEnd();

                bbox.update(echo);

                // propagate
                propagate(origin, echo, beamFraction, shot.origin);

                //calcul de la fraction de section de faisceau intercepté
                if(parameters.getWeighting() != VoxelParameters.WEIGHTING_NONE){
                    beamFraction = beamFraction - bfIntercepted;
                }


                origin = new Point3f(echo);
                
            }
        }
        
        nbShotsTreated++;
    }
    
    /**
     *
     * @param origin current origin (origin start from the last echo)
     * @param echo current echo (position in voxel space)
     * @param beamFraction 
     * @param source shot origin
     */
    private void propagate(Point3f origin, Point3f echo, float beamFraction, Point3f source) {
        
        //get shot line
        LineElement lineElement = new LineSegment(origin, echo);
        
        //get the first voxel cross by the line
        VoxelCrossingContext context = voxelManager.getFirstVoxel(lineElement);
        
        float distanceToHit = lineElement.getLength();
        
        //float distanceToHit = echo.distance(origin); //must be the same as "lineElement.getLength();"
        
        //distance de l'écho à la source
        //float distance = echo.distance(source);
        
        //surface de la section du faisceau à la distance de la source
        //double surface = Math.pow(Math.tan(laserBeamDivergence/2)* distance, 2)  * Math.PI;
        
        while ((context != null) && (context.indices != null)) {
            
            //distance from the last origin to the point in which the ray enter the voxel
            float d1 = context.length;
            
            //current voxel
            Point3i indices = context.indices;
            
            context = voxelManager.CrossVoxel(lineElement, context.indices);
            
            //distance from the last origin to the point in which the ray exit the voxel
            float d2 = context.length;

            Voxel vox = voxels[indices.x][indices.y][indices.z];
            
            Point3f voxelPosition = vox.getPosition(indices, parameters.split, parameters.bottomCorner, parameters.topCorner);
            
            //distance de l'écho à la source
            float distance = voxelPosition.distance(source);

            //surface de la section du faisceau à la distance de la source
            double surface = Math.pow(Math.tan(laserBeamDivergence/2)* distance, 2)  * Math.PI;
            
            // voxel sampled without hit
            if (d2 < distanceToHit) {
                
                vox.lgTraversant += (d2 - d1);
                
                vox.pathLength += (d2 - d1);
                vox.nbSampling++;
                nbSamplingTotal++;
                
                vox.bfEntering += beamFraction;
                vox.bsEntering += (surface*beamFraction);
                
            }else if (d1>distanceToHit) {
                //no comprende
                //System.out.println("test");
            }else {
                
                /*si plusieurs échos issus du même tir dans le voxel, 
                on incrémente et le nombre de tirs entrants (nbsampling) 
                et le nombre d'interception (interceptions) 
                et la longueur parcourue(lgInterception)*/
                
                vox.lgTraversant += (d2 - d1);
                
                vox.nbSampling++;
                nbSamplingTotal++;
                
                vox.pathLength += (distanceToHit - d1);
                vox.interceptions ++;
                
                vox.lgInterception += (distanceToHit - d1);
                
                vox.bfIntercepted += beamFraction;
                vox.bfEntering += beamFraction;
                
                vox.bsIntercepted += (surface*beamFraction);
                vox.bsEntering += (surface*beamFraction);
                
                //Point3f echo2 = new Point3f(lineElement.getDirection());
                //echo2.scale(distanceToHit);
                       
            }
        }
    }
    
    /**
     *
     * @param threshold minimum sampling for PAD calculation
     */
    public void calculatePAD(float threshold){
        
        for (int x = 0; x < parameters.split.x; x++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int z = 0; z < parameters.split.z; z++) {
                        
                        Voxel vox = voxels[x][y][z];
                        
                        float PAD;
                        float PAD2;
                        
                        if(vox.nbSampling <= threshold){
                            
                            PAD = -1;
                            PAD2 = -1;
                            
                        }else if(vox.interceptions >= vox.nbSampling){
                            
                            PAD = 10;
                            PAD2 = 10;
                            
                        }else{
                            
                            float transmittance = (vox.nbSampling - vox.interceptions)/vox.nbSampling;
                            float l = vox.lgTraversant/(vox.nbSampling-vox.interceptions); //longueur du trajet optique
                            
                            PAD = (float) (Math.log(transmittance)/(-0.5*l));
                            PAD2 = (float) ((Math.log((vox.bfEntering - vox.bfIntercepted)/vox.bfEntering))/(-0.5*l));
                            
                            if(PAD > 10){
                                PAD = 10;
                            }
                            if(PAD2 > 10){
                                PAD2 = 10;
                            }
                            
                            if(Float.isNaN(PAD)){
                                PAD = -1;
                            }
                            
                            if(Float.isNaN(PAD2)){
                                PAD2 = -1;
                            }
                        }
                        
                        
                        vox.PAD = PAD+0.0f;
                        vox.PAD2 = PAD2+0.0f;
                    }
                }
        }
        
    }
    
    public void calculatePADAndWrite(float threshold){
        
        long start_time = System.currentTimeMillis();
        
        logger.info("writing file: "+outputFile.getAbsolutePath());
        
        //2097152 octets = 2 mo
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile),2097152)) {

            writer.write("VOXEL SPACE"+"\n");
            writer.write("#min_corner: "+voxSpace.getBoundingBox().min.x+" "+voxSpace.getBoundingBox().min.y+" "+voxSpace.getBoundingBox().min.z+"\n");
            writer.write("#max_corner: "+voxSpace.getBoundingBox().max.x+" "+voxSpace.getBoundingBox().max.y+" "+voxSpace.getBoundingBox().max.z+"\n");
            writer.write("#split: "+voxSpace.getSplitting().x+" "+voxSpace.getSplitting().y+" "+voxSpace.getSplitting().z+"\n");

            Point3f offset = new Point3f();
            writer.write("#offset: "+offset.x+" "+offset.y+" "+offset.z+"\n");

            //writer.write("i j k shots path_length BFintercepted BFentering BSintercepted BSentering PAD"+"\n");
            writer.write("i j k nbSampling interceptions path_length lgTraversant lgInterception PAD PAD2 BFIntercepted BFEntering BSIntercepted BSEntering"+"\n");

            for (int x = 0; x < parameters.split.x; x++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int z = 0; z < parameters.split.z; z++) {

                        Voxel vox = voxels[x][y][z];
                        
                        float PAD;
                        float PAD2;
                        
                        if(vox.nbSampling <= threshold){
                            
                            PAD = -1;
                            PAD2 = -1;
                            
                        }else if(vox.interceptions >= vox.nbSampling){
                            
                            PAD = 10;
                            PAD2 = 10;
                            
                        }else{
                            
                            float transmittance = (vox.nbSampling - vox.interceptions)/vox.nbSampling;
                            float l = vox.lgTraversant/(vox.nbSampling-vox.interceptions); //longueur du trajet optique
                            /*
                            if(l == vox.pathLength){
                                System.out.println("test");
                            }else{
                                System.out.println("test");
                            }
                            */
                            PAD = (float) (Math.log(transmittance)/(-0.5*l));
                            PAD2 = (float) ((Math.log((vox.bfEntering - vox.bfIntercepted)/vox.bfEntering))/(-0.5*vox.lgTraversant/(vox.nbSampling-vox.interceptions)));
                            if(PAD > 10){
                                PAD = 10;
                            }
                            if(PAD2 > 10){
                                PAD2 = 10;
                            }
                            
                            if(Float.isNaN(PAD)){
                                PAD = -1;
                            }
                            
                            if(Float.isNaN(PAD2)){
                                PAD2 = -1;
                            }
                        }
                        
                        
                        vox.PAD = PAD+0.0f;
                        vox.PAD2 = PAD2+0.0f;
                        
                        String voxLine = concatene(x, y, z, vox);
                        
                        //writer.write(x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.pathLength+" "+BFIntercepted+" "+BFentering+" "+BSintercepted+" "+BSentering+" "+PAD+"\n");
                        writer.write(voxLine);

                    }
                }
            }
            
            writer.close();
            
            logger.info("file writed ( "+TimeCounter.getElapsedTimeInSeconds(start_time)+" )");
            
        } catch (FileNotFoundException e) {
            logger.error("Error: " + e.getMessage());
        }catch (Exception e) {
            logger.error("Error: " + e.getMessage());
        }
        
    }
    
    private String concatene(int x, int y, int z, Voxel vox){
        String voxLine = x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.interceptions+" "+vox.pathLength+" "+vox.lgTraversant+" "+vox.lgInterception+" "+vox.PAD+" "+vox.PAD2+" "+vox.bfIntercepted+" "+vox.bfEntering+" "+vox.bsIntercepted+" "+vox.bsEntering+"\n";
        
        return voxLine;
    }
    
    private void createVoxelSpace() {

        voxSpace = new VoxelSpace(new BoundingBox3f(parameters.bottomCorner, parameters.topCorner), parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

        // allocate voxels
        System.out.println("allocate!!!!!!!!");
        
        voxels = new Voxel[parameters.split.x][parameters.split.y][parameters.split.z];
        
        for (int x = 0; x < parameters.split.x; x++) {
            for (int y = 0; y < parameters.split.y; y++) {
                for (int z = 0; z < parameters.split.z; z++) {
                    
                    voxels[x][y][z] = new Voxel();
                }
            }
        }

        Scene scene = new Scene();
        scene.setBoundingBox(new BoundingBox3f(parameters.bottomCorner, parameters.topCorner));

        voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

        voxelManager.showInformations();
    }

    public void writeOutput() {

        //Point3f dimVox = voxSpace.getVoxelSize();
        //float voxelVolume = (dimVox.x * dimVox.y * dimVox.z);
        System.out.println("write file: "+outputFile.getAbsolutePath());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("VOXEL SPACE"+"\n");
            writer.write("#min_corner: "+voxSpace.getBoundingBox().min.x+" "+voxSpace.getBoundingBox().min.y+" "+voxSpace.getBoundingBox().min.z+"\n");
            writer.write("#max_corner: "+voxSpace.getBoundingBox().max.x+" "+voxSpace.getBoundingBox().max.y+" "+voxSpace.getBoundingBox().max.z+"\n");
            writer.write("#split: "+voxSpace.getSplitting().x+" "+voxSpace.getSplitting().y+" "+voxSpace.getSplitting().z+"\n");

            Point3f offset = new Point3f();
            writer.write("#offset: "+offset.x+" "+offset.y+" "+offset.z+"\n");

            //writer.write("i j k shots path_length BFintercepted BFentering BSintercepted BSentering PAD"+"\n");
            writer.write("i j k nbSampling interceptions path_length lgTraversant lgInterception PAD PAD2 BFIntercepted BFEntering BSIntercepted BSEntering"+"\n");

            for (int z = 0; z < parameters.split.z; z++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int x = 0; x < parameters.split.x; x++) {

                        Voxel vox = voxels[x][y][z];
                        
                        //writer.write(x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.pathLength+" "+BFIntercepted+" "+BFentering+" "+BSintercepted+" "+BSentering+" "+PAD+"\n");
                        writer.write(x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.interceptions+" "+vox.pathLength+" "+vox.lgTraversant+" "+vox.lgInterception+" "+vox.PAD+" "+vox.PAD2+" "+vox.bfIntercepted+" "+vox.bfEntering+" "+vox.bsIntercepted+" "+vox.bsEntering+"\n");

                    }
                }
            }
            
            writer.close();
            
        } catch (Exception e) {	//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

}
