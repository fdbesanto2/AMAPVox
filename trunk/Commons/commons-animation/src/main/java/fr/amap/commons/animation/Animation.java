/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.animation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javafx.animation.Interpolator;

/**
 *
 * @author Julien Heurtebize
 */
public abstract class Animation {
    
    protected KeyFrame[] keyFrames;
    
    //contains for each property the occurrences in the keyFrames
    protected final Map<String, TreeSet<KeyValuePosition>> states = new HashMap<>();
    
    protected List<Event>[] events;

    protected void addPropertyValue(KeyValue keyValue, int timelineIndex, int keyframeIndex){
        
        if(states.containsKey(keyValue.getName())){
            states.get(keyValue.getName()).add(new KeyValuePosition(timelineIndex, keyframeIndex));
        }else{
            TreeSet<KeyValuePosition> values = new TreeSet<>();
            values.add(new KeyValuePosition(timelineIndex, keyframeIndex));
            states.put(keyValue.getName(), values);
        }
    }
    
    protected void insertKeyValue(int index, KeyValue keyValue){
                
        if(keyFrames[index] == null){
            keyFrames[index] = new KeyFrame(keyValue);
        }else{
            keyFrames[index].addKeyValue(keyValue);
        }
        
        addPropertyValue(keyValue, index, keyFrames[index].getKeyValues().size()-1);
    }
    
    protected void insertKeyFrame(int index, KeyFrame keyFrame){
                
        keyFrames[index] = keyFrame;
        
        int currentIndex = 0;
        for(KeyValue keyValue : keyFrames[index].getKeyValues()){
            addPropertyValue(keyValue, index, currentIndex);
            currentIndex++;
        }
    }
    
    protected Object getValue(String name, int index, Interpolator interpolator) throws Exception{
        
        TreeSet<KeyValuePosition> indices = states.get(name);
        
        if(indices == null){
            throw new Exception("The key '"+name+"'"+" doesn't exist!");
        }
        
        if(indices.isEmpty()){
            return null;
        }
        
        if(index >= indices.first().timelineIndex && index <= indices.last().timelineIndex){
            
            KeyValuePosition minTmp = indices.floor(new KeyValuePosition(index, 0));
            KeyValuePosition maxTmp = indices.ceiling(new KeyValuePosition(index, 0));
            
            if(minTmp == null){
                minTmp = indices.first();
            }
            
            if(maxTmp == null){
                maxTmp = indices.last();
            }
            
            Integer minIndex = minTmp.timelineIndex;
            Integer maxIndex = maxTmp.timelineIndex;
            
            if(minIndex != null && maxIndex != null){
                
                if(minIndex.equals(maxIndex)){
                    return keyFrames[minIndex].getKeyValues().get(minTmp.keyframeIndex).getValue().value;
                }
                
                double fraction = (index-minIndex)/(double)(maxIndex-minIndex);
                //System.out.println(fraction);
                return (keyFrames[minIndex].getKeyValues().get(minTmp.keyframeIndex).getValue()).interpolate(interpolator,
                        keyFrames[minIndex].getKeyValues().get(minTmp.keyframeIndex).getValue().value, keyFrames[maxIndex].getKeyValues().get(maxTmp.keyframeIndex).getValue().value, fraction);

            }
            
        }else if(index < indices.first().timelineIndex){ //set the value to the min defined value
            return keyFrames[indices.first().timelineIndex].getKeyValues().get(indices.first().keyframeIndex).getValue().value;
        }else if(index > indices.last().timelineIndex){ //set the value to the max defined value
            return keyFrames[indices.last().timelineIndex].getKeyValues().get(indices.last().keyframeIndex).getValue().value;
        }
        
        return null;
    }
    
    protected List<Event> getEvents(int index){
        
        return events[index];
    }
    
    protected void insertEvent(int index, Event event){
        
        if(events[index] == null){
            events[index] = new ArrayList<>();
        }
        
        events[index].add(event);
    }
}
