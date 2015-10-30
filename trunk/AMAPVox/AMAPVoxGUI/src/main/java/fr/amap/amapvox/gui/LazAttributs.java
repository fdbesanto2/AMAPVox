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
public class LazAttributs{
        
    private final static String classification = "classification";
    private final static String intensity = "intensity";
    private final static String numberOfReturns = "Number of returns";
    private final static String returnNumber = "Return number";
    private final static String time = "GPS time";

    private boolean exportClassification;
    private boolean exportIntensity;
    private boolean exportNumberOfReturns;
    private boolean exportReturnNumber;
    private boolean exportTime;
    
    private final Statistic classificationStatistic = new Statistic();
    private final Statistic intensityStatistic = new Statistic();
    private final Statistic returnNumberStatistic = new Statistic();
    private final Statistic numberOfReturnsStatistic = new Statistic();
    private final Statistic timeStatistic = new Statistic();
    
    private final static List<String> list = new ArrayList<>();
    
    static{
        list.add(classification);
        list.add(intensity);
        list.add(numberOfReturns);
        list.add(returnNumber);
        list.add(time);
    }


    public LazAttributs(){

        exportClassification = false;
        exportIntensity = false;
        exportNumberOfReturns = false;
        exportReturnNumber = false;
        exportTime = false;
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
            }
        }
    }

    public List<String> getAttributsNames(){

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

    public Statistic getClassificationStatistic() {
        return classificationStatistic;
    }

    public Statistic getIntensityStatistic() {
        return intensityStatistic;
    }

    public Statistic getReturnNumberStatistic() {
        return returnNumberStatistic;
    }

    public Statistic getNumberOfReturnsStatistic() {
        return numberOfReturnsStatistic;
    }

    public Statistic getTimeStatistic() {
        return timeStatistic;
    }

}
