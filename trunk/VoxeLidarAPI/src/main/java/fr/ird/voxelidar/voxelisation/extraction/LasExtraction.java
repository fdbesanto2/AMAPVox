/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.extraction;

import java.util.concurrent.BlockingQueue;
import javax.swing.event.EventListenerList;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class LasExtraction implements Runnable{
    
    public final static Logger logger = Logger.getLogger(LasExtraction.class);
    
    public native void afficherBonjour();
    private native boolean simpleExtract(String filePath, LasPoints lasPoints);
    
    private final EventListenerList listeners;
    
    private final BlockingQueue<LasPoint> arrayBlockingQueue;
    private String filePath;
    
    static {
        System.loadLibrary("Laslib");
    }
    
    public void fireIsFinished(){
        
        for(LasExtractionListener listener :listeners.getListeners(LasExtractionListener.class)){
            
            listener.isFinished();
        }
    }
    
    public void addLasExtractionListener(LasExtractionListener listener){
        listeners.add(LasExtractionListener.class, listener);
    }
    
    public LasExtraction(BlockingQueue<LasPoint> arrayBlockingQueue, String filePath){
        this.arrayBlockingQueue = arrayBlockingQueue;
        this.listeners = new EventListenerList();
        this.filePath = filePath;
    }

    @Override
    public void run() {
        
        LasPoints points = new LasPoints();
        afficherBonjour();
        
        points.addLasPointListener(new LasPointListener() {

            @Override
            public void pointExtracted(LasPoint point) {
                try {
                    arrayBlockingQueue.put(point);
                } catch (InterruptedException ex) {
                    logger.error(ex.getMessage());
                }
            }
        });
        
        boolean success = simpleExtract(filePath, points);
        
        if(!success){
            logger.error("Cannot extract las file");
        }
        
        fireIsFinished();   
    }
    
}
