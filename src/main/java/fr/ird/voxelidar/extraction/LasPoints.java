/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.extraction;

import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien
 */
public class LasPoints {
    
    private final EventListenerList listeners;
    
    public LasPoints(){
        listeners = new EventListenerList();
    }
    
    public void addLasPointListener(LasPointListener listener){
        listeners.add(LasPointListener.class, listener);
    }
    
    public void firePointAdded(LasPoint point){
        
        for(LasPointListener listener :listeners.getListeners(LasPointListener.class)){
            
            listener.pointExtracted(point);
        }
    }
    
    public void addPoint(double x, double y, double z, short returnNumber, short numberOfReturns, double gpsTime){
        
        LasPoint point = new LasPoint(x, y, z, returnNumber, numberOfReturns, gpsTime);
        
        firePointAdded(point);
    }
}
