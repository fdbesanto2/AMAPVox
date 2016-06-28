/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.timeline;

import javafx.animation.Interpolator;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize
 */
public class Point3dProperty extends Property<Point3d>{

    public Point3dProperty(Point3d value) {
        super(value);
    }
    
    @Override
    public Point3d interpolate(Interpolator interpolator, Point3d startValue, Point3d endValue, double fraction) {
        return new Point3d(
                interpolator.interpolate(startValue.x, endValue.x, fraction),
                interpolator.interpolate(startValue.y, endValue.y, fraction),
                interpolator.interpolate(startValue.z, endValue.z, fraction));
    }
    
}
