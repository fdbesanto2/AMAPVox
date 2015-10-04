package fr.ird.voxelidar.voxelisation.extraction;


import fr.ird.voxelidar.util.NativeLoader;
import fr.ird.voxelidar.engine3d.math.matrix.Mat3D;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec3D;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelAnalysis;
import java.io.File;
import java.util.Iterator;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;






/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RxpExtraction implements Runnable{
    
    public final static Logger logger = Logger.getLogger(RxpExtraction.class);
    private final static String NATIVE_LIBRARY_NAME = "RivLibJNI";
    private final EventListenerList listeners;
    
    private final LinkedBlockingQueue<Shot> arrayBlockingQueue;
    private final File rxpFile;
    private final Mat4D transfMatrix;
    private final Mat3D rotation;
    
    private final Shots shots;
    
    public native void afficherBonjour();
    private native boolean simpleExtraction(String file, Shots shots);
    
    
    public RxpExtraction(File rxpFile, LinkedBlockingQueue<Shot> arrayBlockingQueue, Mat4D transfMatrix, Mat3D rotation){
        
        listeners = new EventListenerList();
        
        this.transfMatrix = transfMatrix;
        this.rotation = rotation;
        this.arrayBlockingQueue = arrayBlockingQueue;
        this.rxpFile = rxpFile;
        shots = new Shots();
        
        final RxpExtraction parent = this;
        /*
        shots.addShotsListener(new ShotsListener() {

            @Override
            public void shotExtracted(final Shot shot) {
                try {
                    
                    Vec4D locVector = Mat4D.multiply(parent.transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
                    
                    Vec3D uVector = Mat3D.multiply(parent.rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));
                    
                    shot.setOriginAndDirection(new Point3d(locVector.x, locVector.y, locVector.z), new Vector3d(uVector.x, uVector.y, uVector.z));
                    //shot.origin = new Point3d(locVector.x, locVector.y, locVector.z);
                    //System.out.println(shot.origin.x + " "+shot.origin.y+" "+shot.origin.z+"\n");
                    
                    //shot.direction = new Vector3d(uVector.x, uVector.y, uVector.z);
                    
                    
                    
                    parent.arrayBlockingQueue.put(shot);
                    /*
                    while(parent.arrayBlockingQueue.size() >5000000){
                        Thread.sleep(3000);
                    }
                    *//*
                }catch (InterruptedException ex) {
                    logger.error(ex+" "+this.getClass().getName());
                }catch(Exception e){
                    logger.error(e.getMessage()+" "+this.getClass().getName());
                }
            }
        });*/

    }
    
    static {
        
        NativeLoader loader = new NativeLoader();
        loader.loadLibrary(NATIVE_LIBRARY_NAME);
        
    }
    
    public void addRxpExtractionListener(RxpExtractionListener listener){
        listeners.add(RxpExtractionListener.class, listener);
    }
    /*
    public void fireShotAdded(Shot shot){
        
        for(RxpExtractionListener listener :listeners.getListeners(RxpExtractionListener.class)){
            
            listener.shotExtracted(shot);
        }
    }
    */
    
    public void fireIsFinished(){
        
        for(RxpExtractionListener listener :listeners.getListeners(RxpExtractionListener.class)){
            
            listener.isFinished();
        }
    }
    
    
    /*
    public static void main(String[] args) {
        RxpExtraction test = new RxpExtraction();
        
        
        test.afficherBonjour();
        Shots shots = new Shots();
        
        File rxpFile = new File("C:\\Users\\Julien\\Documents\\Visual Studio 2012\\Projects\\TLSRivLib\\testmtd.rxp");
        
        long start_time = System.nanoTime();        
        
        boolean success = test.simpleExtraction(rxpFile.getAbsolutePath(), shots);
        //boolean success = test.simpleConnection("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.rxp", 10000, shots);
        
        long end_time = System.nanoTime();
        double difference = (end_time - start_time)*(Math.pow(10, -9));
        
        System.out.println("test");
    }
    */
    /*
    public void extract(VoxelAnalysis voxelAnalysis){
        
        long startTime = System.currentTimeMillis();
        
        voxelAnalysis.createVoxelSpace();
        fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction rxpExtraction = new fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction();
        rxpExtraction.openRxpFile(rxpFile, fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction.SHOT_WITH_REFLECTANCE);
        
        Iterator<fr.ird.voxelidar.voxelisation.extraction.Shot> iterator = rxpExtraction.iterator();
        
        fr.ird.voxelidar.voxelisation.extraction.Shot shot;
        while(iterator.hasNext()){
            
            shot = iterator.next();
            if(shot != null){
                
                
                Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));

                Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

                shot.setOriginAndDirection(new Point3d(locVector.x, locVector.y, locVector.z), new Vector3d(uVector.x, uVector.y, uVector.z));
                //shot.origin = new Point3d(locVector.x, locVector.y, locVector.z);
                //System.out.println(shot.origin.x + " "+shot.origin.y+" "+shot.origin.z+"\n");

                //shot.direction = new Vector3d(uVector.x, uVector.y, uVector.z);
                //try {
                    voxelAnalysis.processOneShot(new Shot(shot.nbEchos, shot.origin, shot.direction, shot.ranges));
                    //arrayBlockingQueue.put(new Shot(shot.nbEchos, shot.origin, shot.direction, shot.ranges));
               // } catch (InterruptedException ex) {
                   // java.util.logging.Logger.getLogger(RxpExtraction.class.getName()).log(Level.SEVERE, null, ex);
                //}
                
            }
            
        }
        
        rxpExtraction.close();
        
        logger.info("Shots processed: " + voxelAnalysis.nbShotsTreated);
        logger.info("voxelisation is finished ( " + TimeCounter.getElapsedStringTimeInSeconds(startTime) + " )");

        voxelAnalysis.calculatePADAndWrite(0);

        if(voxelAnalysis.parameters.isCalculateGroundEnergy() && !voxelAnalysis.parameters.isTLS()){
            voxelAnalysis.writeGroundEnergy();
        }
        
        
    }*/
    

    @Override
    public void run() {
        
        
        long startTime = System.currentTimeMillis();
        
        fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction rxpExtraction = new fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction();
        rxpExtraction.openRxpFile(rxpFile, fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction.SHOT_WITH_REFLECTANCE);
        
        Iterator<fr.ird.voxelidar.voxelisation.extraction.Shot> iterator = rxpExtraction.iterator();
        
        int count = 0;
       // Shot shot = iterator.next();
        fr.ird.voxelidar.voxelisation.extraction.Shot shot;
        while(iterator.hasNext()){
            
            shot = iterator.next();
            if(shot != null){
                if(count == 174649){
                    System.out.println(shot.nbEchos+" "+shot.origin+" "+shot.direction);
                }
                
                
                Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));

                Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

                shot.setOriginAndDirection(new Point3d(locVector.x, locVector.y, locVector.z), new Vector3d(uVector.x, uVector.y, uVector.z));
                //shot.origin = new Point3d(locVector.x, locVector.y, locVector.z);
                //System.out.println(shot.origin.x + " "+shot.origin.y+" "+shot.origin.z+"\n");

                //shot.direction = new Vector3d(uVector.x, uVector.y, uVector.z);
                try {
                    arrayBlockingQueue.put(new Shot(shot.nbEchos, shot.origin, shot.direction, shot.ranges));
                } catch (InterruptedException ex) {
                    java.util.logging.Logger.getLogger(RxpExtraction.class.getName()).log(Level.SEVERE, null, ex);
                }
                count++;
            }
            
        }
        
        rxpExtraction.close();
        
        System.out.println(TimeCounter.getElapsedTimeInSeconds(startTime)); 
        
        /*
        try{
            boolean success = simpleExtraction(rxpFile.getAbsolutePath(), shots);
                    
            fireIsFinished();
            
            if(!success){
                logger.error("extraction failed");
            } 
            
        }catch(Exception e){
            logger.error(e+" "+this.getClass().getName());
        }
                */
    }
    
}
