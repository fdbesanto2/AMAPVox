/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.util.ColorGradient;
import fr.amap.commons.util.Statistic;
import gnu.trove.list.array.TFloatArrayList;
import java.awt.Color;
import java.util.Comparator;
import org.apache.commons.math3.stat.Frequency;

/**
 *
 * @author calcul
 */
public class ScalarField {
    
    private final Statistic statistic;
    private ColorGradient colorGradient;
    private final TFloatArrayList values;
    private final String name;
    
    private final Frequency f;
    public long[] histogramFrequencyCount;
    public double[] histogramValue;
    
    public boolean hasColorGradient;
            
    public ScalarField(String name) {
        
        this.name = name;
        values = new TFloatArrayList();
        statistic = new Statistic();
        colorGradient = new ColorGradient(0, 0);
        hasColorGradient = true;
        f = new Frequency();
    }
    
    public void addValue(float value){
        values.add(value);
        statistic.addValue(value);
    }
    
    public float getValue(int index){
        
        return values.get(index);
    }
    
    public Color getColor(int index){
        
        colorGradient.setMinValue((float) statistic.getMinValue());
        colorGradient.setMaxValue((float) statistic.getMaxValue());
        
        return colorGradient.getColor(values.get(index));
    }

    public void setColorGradient(ColorGradient colorGradient) {
        this.colorGradient = colorGradient;
    }
    
    public void setGradientColor(Color[] color) {
        this.colorGradient.setGradientColor(color);
    }
    
    public int getNbValues(){
        return values.size();
    }

    public String getName() {
        return name;
    }

    public Statistic getStatistic() {
        return statistic;
    }
    
    public void buildHistogram(){
        
        for(int i=0;i< values.size();i++){
            float value = values.get(i);
            f.addValue(new Double(value).longValue());
        }
        
        double minValue = statistic.getMinValue();
        double maxValue = statistic.getMaxValue();
        
        double width = maxValue - minValue;
        double step = width / 18.0d;
        
        histogramValue = new double[19];
        histogramFrequencyCount = new long[19];
        
        int i=0;
        
        try{
            for(double d = minValue ; d <= maxValue ; d += step){
            
            if(i < histogramValue.length){
                    histogramFrequencyCount[i] = f.getCumFreq(new Double(d + step).longValue()) - f.getCumFreq(new Double(d).longValue());
                    histogramValue[i] = d;
                }

                i++;
            }
        }catch(Exception e){
            
        }
        
    }
    
    
}
