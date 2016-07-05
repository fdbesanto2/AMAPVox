/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.animation;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;

/**
 *
 * @author Julien Heurtebize
 */
public class FrameAnimation extends Animation{
    
    private final int duration;
    private final int start;
    private final int end;
    
    /**
     * 
     * @param start Start frame index.
     * @param end End frame index.
     */
    public FrameAnimation(int start, int end) {
        
        this.start = start;
        this.end = end;
        
        duration = (end - start) + 1;
        keyFrames = new KeyFrame[duration];
        events = new ArrayList[duration];
    }
    
    @Override
    public void insertKeyValue(int frame, KeyValue keyValue){
        
        int index = getFrameIndex(frame);
        
        super.insertKeyValue(index, keyValue);
    }
    
    @Override
    public void insertKeyFrame(int frame, KeyFrame keyFrame){
        
        int index = getFrameIndex(frame);
        
        super.insertKeyFrame(index, keyFrame);
    }
    
    private int getFrameIndex(int frame){
        
        return frame - start;
    }
    
    private int getNearestFrameIndex(int frame){
        
        int frameIndex = getFrameIndex(frame);
        
        frameIndex = Integer.max(0, frameIndex);
        frameIndex = Integer.min(duration - 1, frameIndex);
        
        return frameIndex;
    }
    
    @Override
    public void insertEvent(int frame, Event event){
        
        int index = getFrameIndex(frame);
        
        super.insertEvent(index, event);
    }
    
    @Override
    public List<Event> getEvents(int frame){
        
        int index = getFrameIndex(frame);
        return super.getEvents(index);
    }
    
    @Override
    protected Object getValue(String name, int frame, Interpolator interpolator) throws Exception{
        
        int index = getFrameIndex(frame);
        return super.getValue(name, index, interpolator);
    }
}
