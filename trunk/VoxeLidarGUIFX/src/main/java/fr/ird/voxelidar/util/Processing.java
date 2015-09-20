/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import java.io.File;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class Processing {
    
    private final EventListenerList listeners= new EventListenerList();
    private String progress;
    private boolean finished;
    private int stepNumber = 1;
    private int currentStep = 0;

    public void setStepNumber(int stepNumber) {
        currentStep = 0;
        this.stepNumber = stepNumber;
    }
    
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
    
    protected int getProgression(){
        return (currentStep*100)/stepNumber;
    }
    
    public void fireProgress(String progress, int ratio){
        
        currentStep ++;
        for(ProcessingListener listener :listeners.getListeners(ProcessingListener.class)){
            
            listener.processingStepProgress(progress, ratio);
        }
    }
    
    public void fireFinished(){
        
        for(ProcessingListener listener :listeners.getListeners(ProcessingListener.class)){
            
            listener.processingFinished();
        }
    }
    
    public void addProcessingListener(ProcessingListener listener){
        listeners.add(ProcessingListener.class, listener);
    }
    
    public abstract File process();
}
