/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

import fr.amap.amapvox.commons.util.Statistic;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author calcul
 */
public class LasAttributs{
        
    private final static String classification = "classification";
    private final static String intensity = "intensity";
    private final static String numberOfReturns = "Number of returns";
    private final static String returnNumber = "Return number";
    private final static String time = "GPS time";
    private final static String red = "red";
    private final static String green = "green";
    private final static String blue = "blue";

    private boolean exportClassification;
    private boolean exportIntensity;
    private boolean exportNumberOfReturns;
    private boolean exportReturnNumber;
    private boolean exportTime;
    private boolean exportRed;
    private boolean exportGreen;
    private boolean exportBlue;
    
    private final int pointDataFormatID;
    

    public LasAttributs(int pointDataFormatID){

        exportClassification = false;
        exportIntensity = false;
        exportNumberOfReturns = false;
        exportReturnNumber = false;
        exportTime = false;
        exportRed = false;
        exportGreen = false;
        exportBlue = false;
        this.pointDataFormatID = pointDataFormatID;
    }
    
    public void processList(List<String> attributsToExport){
        
        for(String s : attributsToExport){
            switch(s){
                case classification:
                    exportClassification = true;
                    break;
                 case intensity:
                     exportIntensity = true;
                    break;
                case numberOfReturns:
                    exportNumberOfReturns = true;
                    break;
                case returnNumber:
                   exportReturnNumber = true;
                    break;
                case time:
                    exportTime = true;
                    break;
                case red:
                    exportRed = true;
                    break;
                case green:
                    exportGreen = true;
                    break;
                case blue:
                    exportBlue = true;
                    break;
            }
        }
    }

    public List<String> getAttributsNames(){

        List<String> list = new ArrayList<>();
        
        if(pointDataFormatID <= 3){
            
            
            list.add(classification);
            list.add(intensity);
            list.add(numberOfReturns);
            list.add(returnNumber);
            list.add(time);
            
            if(pointDataFormatID >= 2){
                list.add(red);
                list.add(green);
                list.add(blue);
            }
        }
        return list;
    }

    public boolean isExportClassification() {
        return exportClassification;
    }

    public boolean isExportIntensity() {
        return exportIntensity;
    }

    public boolean isExportNumberOfReturns() {
        return exportNumberOfReturns;
    }

    public boolean isExportReturnNumber() {
        return exportReturnNumber;
    }

    public boolean isExportTime() {
        return exportTime;
    }

    public boolean isExportRed() {
        return exportRed;
    }

    public boolean isExportGreen() {
        return exportGreen;
    }

    public boolean isExportBlue() {
        return exportBlue;
    }
    
}
