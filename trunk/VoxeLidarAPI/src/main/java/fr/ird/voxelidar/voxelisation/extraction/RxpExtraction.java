package fr.ird.voxelidar.voxelisation.extraction;


import fr.ird.voxelidar.engine3d.math.matrix.Mat3D;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec3D;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;






/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Julien
 */
public class RxpExtraction implements Runnable{
    
    public final static Logger logger = Logger.getLogger(RxpExtraction.class);
    private final EventListenerList listeners;
    
    private final BlockingQueue<Shot> arrayBlockingQueue;
    private final File rxpFile;
    private final Mat4D transfMatrix;
    private final Mat3D rotation;
    
    public native void afficherBonjour();
    private native boolean simpleExtraction(String file, Shots shots);
    
    
    public RxpExtraction(File rxpFile, BlockingQueue<Shot> arrayBlockingQueue, Mat4D transfMatrix, Mat3D rotation){
        
        listeners = new EventListenerList();
        
        this.transfMatrix = transfMatrix;
        this.rotation = rotation;
        this.arrayBlockingQueue = arrayBlockingQueue;
        this.rxpFile = rxpFile;
    }
    
    static {
        System.loadLibrary("RivLibJNI");
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
    
    public void extract(File rxpFile) throws Exception{
        
        
        Shots shots = new Shots();
        /*
        shots.addShotsListener(new ShotsListener() {

            @Override
            public void shotExtracted(Shot shot) {
                fireShotAdded(shot);
            }
        });
        */
        boolean success = simpleExtraction(rxpFile.getAbsolutePath(), shots);
        
        
        if(!success){
            throw new Exception("Extraction failed");
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

    @Override
    public void run() {
        
        Shots shots = new Shots();
        
        shots.addShotsListener(new ShotsListener() {

            @Override
            public void shotExtracted(Shot shot) {
                try {
                    
                    Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
                    Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

                    shot.origin = new Point3d(locVector.x, locVector.y, locVector.z);
                    shot.direction = new Vector3d(uVector.x, uVector.y, uVector.z);

                    arrayBlockingQueue.put(shot);
                    
                } catch (InterruptedException ex) {
                    logger.error(ex);
                }
            }
        });
        
        boolean success = simpleExtraction(rxpFile.getAbsolutePath(), shots);
        
        fireIsFinished();
        
        if(!success){
            logger.error("extraction failed");
        }  
    }
    
}
