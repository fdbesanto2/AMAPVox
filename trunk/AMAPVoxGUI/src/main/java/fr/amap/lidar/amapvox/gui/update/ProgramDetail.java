/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.update;

import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Julien Heurtebize
 */
public class ProgramDetail {
    
    private final Calendar date;
    private final URL url;
    public final static NumberFormat NUMBER_FORMAT = NumberFormat.getInstance();
    private String changeLog;

    static{
        NUMBER_FORMAT.setMinimumIntegerDigits(2);
    }
    
    public ProgramDetail(Date date, URL url, String changeLog) {
        this.date = Calendar.getInstance();
        this.date.setTime(date);
        this.url = url;
        this.changeLog = changeLog;
        
    }

    public Calendar getDate() {
        return date;
    }

    public URL getUrl() {
        return url;
    }
    
    public String getChangeLog() {
        return changeLog;
    }

    public void setChangeLog(String changeLog) {
        this.changeLog = changeLog;
    }
    
}
