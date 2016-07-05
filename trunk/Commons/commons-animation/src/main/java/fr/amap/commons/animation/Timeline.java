/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.animation;

import java.util.ArrayList;
import java.util.List;
import javafx.animation.Interpolator;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
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
        long millisDuration = durationFromStart.getMillis();
        
        int index = (int) Math.round((millisDuration / 1000.0 / 3600.0) / timeStep);
        
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
    
    /**
     * Get the decimal hour from hour, minutes and seconds.
     * @param hour The hour
     * @param minutes The minutes
     * @param seconds The seconds
     * @return The decimal hour.
     */
    public static double getDecimalHour(int hour, int minutes, int seconds) {
        return hour + (minutes / 60.0) + (seconds / 3600.0);
    }
    
    public static double getDecimalHour(DateTime time){
        return getDecimalHour(time.getHourOfDay(), time.getMinuteOfHour(), time.getSecondOfMinute());
    }
    
    public void start(){
        
        fireStarted();
        
        DateTime currentTime = new DateTime(startTime);
        int lastDoy = currentTime.getDayOfYear();
        
        fireTimeChanged(currentTime);
        fireDoyChanged(currentTime);
        
        while(currentTime.compareTo(endTime) <= 0){
            currentTime = currentTime.plusMillis((int)(timeStep * 3600 * 1000));
            
            if(currentTime.compareTo(endTime) > 0){
                break;
            }
            
            if(currentTime.getDayOfYear() != lastDoy){
                
                lastDoy = currentTime.getDayOfYear();
                fireDoyChanged(currentTime);
            }
            
            fireTimeChanged(currentTime);
        }
        
        fireFinished();
    }
    
    public static void main(String[] args) {
        
        DateTimeZone zone = DateTimeZone.forID("Europe/Paris");
        
        Timeline timeline = new Timeline(new DateTime(2016, 10, 30, 0, 0, zone), new DateTime(2016, 10, 30, 23, 59, zone), 0.01);
        
        timeline.addTimelineListener(new TimelineAdapter() {

            @Override
            public void onTimeChanged(DateTime time) {
                System.out.println(time.toString("HH:mm:ss"));
            }
        });
        
        int nearestIndex = timeline.getNearestIndex(new DateTime(2016, 10, 30, 1, 2, zone));
        
        timeline.start();
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
