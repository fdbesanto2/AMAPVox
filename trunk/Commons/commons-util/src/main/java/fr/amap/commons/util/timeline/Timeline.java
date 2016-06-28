/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.timeline;

import fr.amap.commons.util.Time;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import javafx.animation.Interpolator;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.joda.time.Period;

/**
 *
 * @author J. Heurtebize
 */
public class Timeline extends Animation{
    
    private final List<TimelineListener> listeners = new ArrayList<>();
    
    //private final List<KeyFrame> 
    
    private final DateTime startTime;
    private final DateTime endTime;
    private double timeStep = 0.1;
    private final Duration periodDuration;
    private final int nbSteps;

    /**
     * 
     * @param startTime
     * @param endTime
     * @param timeStep Time step in decimal hour.
     */
    public Timeline(DateTime startTime, DateTime endTime, double timeStep) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.timeStep = timeStep;
        
        periodDuration = new Duration(startTime, endTime);
        double hours = periodDuration.getStandardSeconds()/3600.0;
        
        nbSteps = (int) (hours / timeStep) + 1;
        keyFrames = new KeyFrame[nbSteps];
        events = new ArrayList[nbSteps];
    }
    
    public void insertKeyValue(DateTime timePosition, KeyValue keyValue){
        
        int index = getNearestIndex(timePosition);
        
        insertKeyValue(index, keyValue);
    }
    
    /**
     * Get the nearest index in the timeline from the DateTime.
     * @param timePosition
     * @return 
     */
    private int getNearestIndex(DateTime timePosition){
        
        //get array index from timePosition
        Duration durationFromStart = new Duration(startTime, timePosition);
        int index = (int) Math.ceil((durationFromStart.getMillis() / 1000.0 / 3600.0) / timeStep);
        
        if(index >= nbSteps){
            index = nbSteps -1;
        }else if(index < 0/* || index >= nbSteps*/){
            index = 0;
        }
        
        return index;
    }
    
    /**
     * 
     * @param timePosition
     * @param keyFrame
     * @return The nearest DateTime in the timeline from the given DateTime.
     */
    public DateTime insertKeyFrame(DateTime timePosition, KeyFrame keyFrame){
        
        int index = getNearestIndex(timePosition);
        
        insertKeyFrame(index, keyFrame);
        
        DateTime nearestDateTime = startTime.plus(Duration.standardSeconds((long) ((index*timeStep)*3600)));
        
        return nearestDateTime;
    }
    
    public DateTime insertEvent(DateTime timePosition, Event event){
        
        int index = getNearestIndex(timePosition);
        
        super.insertEvent(index, event);
        
        DateTime nearestDateTime = startTime.plus(Duration.standardSeconds((long) ((index*timeStep)*3600)));
        
        return nearestDateTime;
    }
    
    public List<Event> getEvents(DateTime dateTime){
        
        int nearestIndex = getNearestIndex(dateTime);
        return super.getEvents(nearestIndex);
    }
    
    public Object getValue(String name, DateTime dateTime, Interpolator interpolator) throws Exception{
        
        //closest timeline index corresponding to the given DateTime
        int nearestIndex = getNearestIndex(dateTime);
        return getValue(name, nearestIndex, interpolator);
    }
    
    public void start(){
        
        fireStarted();
        
        DateTime currentTime = new DateTime(startTime);

        double endDecimalHour = 24.0;
        
        while(currentTime.getYear() <= endTime.getYear() ){
            
            fireDoyChanged(currentTime);
            
            if(endTime.getYear() == currentTime.getYear() && endTime.getDayOfYear() == currentTime.getDayOfYear()){
                endDecimalHour = Time.getDecimalHour(endTime.getHourOfDay(), endTime.getMinuteOfHour(), endTime.getSecondOfMinute());
            }
            
            int currentDoy = currentTime.getDayOfYear();
            
            double h = 0;
            
            while(!(currentTime.getDayOfYear() != currentDoy || (h=Time.getDecimalHour(currentTime)) >= endDecimalHour)){
                                                
                fireTimeChanged(currentTime);
                
                currentTime = currentTime.plus(Period.seconds((int)(timeStep * 3600)));
            }
            
            if(endTime.getYear() <= currentTime.getYear() && endTime.getDayOfYear() <= currentTime.getDayOfYear() && h >= endDecimalHour){
                break;
            }
        }
        
        fireFinished();
    }
    
    private void fireStarted(){
        
        for(TimelineListener listener : listeners){
            listener.onStarted();
        }
    }
    
    private void fireFinished(){
        
        for(TimelineListener listener : listeners){
            listener.onFinished();
        }
    }
    
    private void fireDoyChanged(DateTime time){
        
        for(TimelineListener listener : listeners){
            listener.onDoyChanged(time);
        }
    }
    
    private void fireTimeChanged(DateTime time){
        
        for(TimelineListener listener : listeners){
            listener.onTimeChanged(time);
        }
    }
    
    public void addTimelineListener(TimelineListener listener){
        listeners.add(listener);
    }
    
    public void removeTimelineListener(TimelineListener listener){
        listeners.remove(listener);
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }
}
