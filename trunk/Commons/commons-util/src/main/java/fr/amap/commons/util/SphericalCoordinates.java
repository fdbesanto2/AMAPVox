/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.amap.commons.util;

import javax.vecmath.Point3d;
import javax.vecmath.Tuple3d;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.apache.commons.math3.util.FastMath;

/**
 *
 * @author calcul
 */


public class SphericalCoordinates {
    
    private double azimut;
    private double zenith;
    private double radius = 1;
    
    private Tuple3d cartesianCoordinates;
    
    public SphericalCoordinates() {
    }

    public SphericalCoordinates(double azimut, double zenith) {
        this.azimut = azimut;
        this.zenith = zenith;
    }
    
    public SphericalCoordinates(double azimut, double zenith, double radius) {
        this.azimut = azimut;
        this.zenith = zenith;
        this.radius = radius;
    }

    public double getAzimut() {
        return azimut;
    }

    public double getZenith() {
        return zenith;
    }

    public double getRadius() {
        return radius;
    }
    
    public Point3d toCartesian(){
        
        Point3d point = new Point3d(radius * FastMath.sin(zenith) * FastMath.cos(azimut), 
                            radius * FastMath.sin(zenith) * FastMath.sin(azimut), 
                            radius * FastMath.cos(zenith));
        
        cartesianCoordinates = point;
        
        return point;
    }
    
    public void toSpherical(Tuple3d point) {
        
        radius = FastMath.sqrt((point.x * point.x) + (point.y * point.y)+ (point.z * point.z));
        azimut = FastMath.atan2(point.y, point.x);
        
        zenith = FastMath.acos(point.z/radius);
        //elevation = Math.atan(Math.sqrt((point.x*point.x) + (point.y*point.y))/point.z);
        
        
        cartesianCoordinates = point;
    }
}
