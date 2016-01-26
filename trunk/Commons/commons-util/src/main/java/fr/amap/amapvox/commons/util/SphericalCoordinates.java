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

package fr.amap.amapvox.commons.util;

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
    
    private double azimuth;
    private double elevation;
    private double radius = 1;
    
    private Tuple3d cartesianCoordinates;
    
    public SphericalCoordinates() {
    }

    public SphericalCoordinates(double azimuth, double elevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
    }
    
    public SphericalCoordinates(double azimuth, double elevation, double radius) {
        this.azimuth = azimuth;
        this.elevation = elevation;
        this.radius = radius;
    }

    public double getAzimuth() {
        return azimuth;
    }

    public double getElevation() {
        return elevation;
    }

    public double getRadius() {
        return radius;
    }
    
    public Point3d toCartesian(){
        
        Point3d point = new Point3d(radius * FastMath.sin(elevation) * FastMath.cos(azimuth), 
                            radius * FastMath.sin(elevation) * FastMath.sin(azimuth), 
                            radius * FastMath.cos(elevation));
        
        cartesianCoordinates = point;
        
        return point;
    }
    
    public void toSpherical(Tuple3d point) {
        
        radius = FastMath.sqrt((point.x * point.x) + (point.y * point.y)+ (point.z * point.z));
        azimuth = FastMath.atan2(point.y, point.x);
        
        elevation = FastMath.acos(point.z/radius);
        //elevation = Math.atan(Math.sqrt((point.x*point.x) + (point.y*point.y))/point.z);
        
        
        cartesianCoordinates = point;
    }
}
