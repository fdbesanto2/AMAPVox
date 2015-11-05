/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import fr.amap.amapvox.commons.util.ColorGradient;
import fr.amap.amapvox.commons.util.Statistic;
import gnu.trove.list.array.TFloatArrayList;
import java.awt.Color;

/**
 *
 * @author calcul
 */
public class ScalarField {
    
    private final Statistic statistic;
    private ColorGradient colorGradient;
    private final TFloatArrayList values;
    private final String name;

    public ScalarField(String name) {
        
        this.name = name;
        values = new TFloatArrayList();
        statistic = new Statistic();
        colorGradient = new ColorGradient(0, 0);
    }
    
    public void addValue(float value){
        values.add(value);
        statistic.addValue(value);
    }
    
    public Color getColor(int index){
        
        colorGradient.setMinValue((float) statistic.getMinValue());
        colorGradient.setMaxValue((float) statistic.getMaxValue());
        
        return colorGradient.getColor(values.get(index));
    }

    public void setColorGradient(ColorGradient colorGradient) {
        this.colorGradient = colorGradient;
    }
    
    public int getNbValues(){
        return values.size();
    }

    public String getName() {
        return name;
    }
}
