/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import java.io.File;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien
 */
public abstract class Processing {
    
    private final EventListenerList listeners= new EventListenerList();
    private String progress;
    private boolean finished;
    
    
    public Processing(){
    }
    
    public void setProgress(String progress, int ratio) {
        this.progress = progress;
        fireProgress(progress, ratio);
    }
    
    public void setFinished(boolean isFinished) {
        this.finished = isFinished;
        
        if(isFinished){
            fireFinished();
        }
    }
    
    public void fireProgress(String progress, int ratio){
        
        for(PreprocessingListener listener :listeners.getListeners(PreprocessingListener.class)){
            
            listener.preprocessingStepProgress(progress, ratio);
        }
    }
    
    public void fireFinished(){
        
        for(PreprocessingListener listener :listeners.getListeners(PreprocessingListener.class)){
            
            listener.preprocessingFinished();
        }
    }
    
    public void addVoxelPreprocessingListener(PreprocessingListener listener){
        listeners.add(PreprocessingListener.class, listener);
    }
    
    public abstract File process();
}
