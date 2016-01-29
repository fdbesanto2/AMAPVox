
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


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class BoundingBox3d {
    
    public Point3d min;
    public Point3d max;
    
    public BoundingBox3d(){
        min = new Point3d();
        max = new Point3d();
    }

    public BoundingBox3d(Point3d min, Point3d max) {
        this.min = min;
        this.max = max;
    }
    
    public void keepLargest(BoundingBox3d boundingBox){
        
        double xMin = 0, yMin = 0, zMin = 0;
        double xMax = 0, yMax = 0, zMax = 0;
        
        if(boundingBox.min.x < min.x){
            xMin = boundingBox.min.x;
        }else{
            xMin = min.x;
        }
        
        if(boundingBox.min.y < min.y){
            yMin = boundingBox.min.y;
        }else{
            yMin = min.y;
        }
        
        if(boundingBox.min.z < min.z){
            zMin = boundingBox.min.z;
        }else{
            zMin = min.z;
        }
        
        if(boundingBox.max.x > max.x){
            xMax = boundingBox.max.x;
        }else{
            xMax = max.x;
        }
        
        if(boundingBox.max.y > max.y){
            yMax = boundingBox.max.y;
        }else{
            yMax = max.y;
        }
        
        if(boundingBox.max.z > max.z){
            zMax = boundingBox.max.z;
        }else{
            zMax = max.z;
        }
        
        this.min = new Point3d(xMin, yMin, zMin);
        this.max = new Point3d(xMax, yMax, zMax);
    }
}
