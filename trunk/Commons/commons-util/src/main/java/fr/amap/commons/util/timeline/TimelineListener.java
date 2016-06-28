/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.timeline;

import java.util.EventListener;
import org.joda.time.DateTime;

/**
 *
 * @author Julien Heurtebize
 */
public interface TimelineListener extends EventListener{
    
    public void onStarted();
    public void onDoyChanged(DateTime time);
    public void onTimeChanged(DateTime time);
    public void onFinished();
}
