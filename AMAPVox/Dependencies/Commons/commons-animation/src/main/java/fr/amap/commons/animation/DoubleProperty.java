/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.animation;

import javafx.animation.Interpolator;

/**
 *
 * @author Julien Heurtebize
 */
public class DoubleProperty extends Property<Double>{

    public DoubleProperty(Double value) {
        super(value);
    }

    @Override
    public Double interpolate(Interpolator interpolator, Double startValue, Double endValue, double fraction) {
        
        return interpolator.interpolate((double)startValue, (double)endValue, fraction);
    }
}
