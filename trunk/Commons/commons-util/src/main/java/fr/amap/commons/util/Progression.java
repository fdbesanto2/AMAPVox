/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util;

import java.io.File;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class Progression {
    
    private final EventListenerList listeners= new EventListenerList();
    private String progress;
    private boolean finished;
    private int stepNumber = 1;
    private int currentStep = 0;
    private double progressStepPercentage;
    private long count;

    public void setStepNumber(int stepNumber) {
        currentStep = 0;
        this.stepNumber = stepNumber;
    }
    
    public void setFinished(boolean isFinished) {
        this.finished = isFinished;
        
        if(isFinished){
            fireFinished(0);
        }
    }
    
    public void setProgressionStep(float percentage){
        this.progressStepPercentage = percentage;
    }
    
    protected int getProgression(){
        return (currentStep*100)/stepNumber;
    }
    
    public void fireProgress(String progressMsg, long progress, long max){
        
        count++;
        
        int progressStep = (int) ((max/100) * progressStepPercentage);
        
        if(progressStep == count){
            
            for(ProcessingListener listener :listeners.getListeners(ProcessingListener.class)){

                listener.processingStepProgress(progressMsg, progress, max);
            }
            
            count = 0;
        }
    }
    
    public void fireFinished(float duration){
        
        for(ProcessingListener listener :listeners.getListeners(ProcessingListener.class)){
            
            listener.processingFinished(duration);
        }
        
        count = 0;
    }
    
    public void addProcessingListener(ProcessingListener listener){
        listeners.add(ProcessingListener.class, listener);
    }
}
