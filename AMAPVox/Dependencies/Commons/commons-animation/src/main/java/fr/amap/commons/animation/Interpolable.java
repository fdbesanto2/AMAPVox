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
public interface Interpolable <T>{
    
    public abstract T interpolate(Interpolator interpolator, T startValue, T endValue, double fraction);
}
