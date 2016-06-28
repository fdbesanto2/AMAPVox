/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.timeline;

import org.joda.time.DateTime;

/**
 *
 * @author Julien Heurtebize
 */
public abstract class TimelineAdapter implements TimelineListener{

    @Override
    public void onStarted() {}

    @Override
    public void onDoyChanged(DateTime time) {}
    
    @Override
    public void onTimeChanged(DateTime time) {}

    @Override
    public void onFinished() {}
    
}
