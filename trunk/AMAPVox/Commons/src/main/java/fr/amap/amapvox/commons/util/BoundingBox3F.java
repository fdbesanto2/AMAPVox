
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

import fr.amap.amapvox.commons.math.point.Point3F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class BoundingBox3F {
    
    public Point3F min;
    public Point3F max;
    
    public BoundingBox3F(){
        min = new Point3F();
        max = new Point3F();
    }

    public BoundingBox3F(Point3F min, Point3F max) {
        this.min = min;
        this.max = max;
    }
    
    public void keepLargest(BoundingBox3F boundingBox3F){
        
        float xMin = 0, yMin = 0, zMin = 0;
        float xMax = 0, yMax = 0, zMax = 0;
        
        if(boundingBox3F.min.x < min.x){
            xMin = boundingBox3F.min.x;
        }else{
            xMin = min.x;
        }
        
        if(boundingBox3F.min.y < min.y){
            yMin = boundingBox3F.min.y;
        }else{
            yMin = min.y;
        }
        
        if(boundingBox3F.min.z < min.z){
            zMin = boundingBox3F.min.z;
        }else{
            zMin = min.z;
        }
        
        if(boundingBox3F.max.x > max.x){
            xMax = boundingBox3F.max.x;
        }else{
            xMax = max.x;
        }
        
        if(boundingBox3F.max.y > max.y){
            yMax = boundingBox3F.max.y;
        }else{
            yMax = max.y;
        }
        
        if(boundingBox3F.max.z > max.z){
            zMax = boundingBox3F.max.z;
        }else{
            zMax = max.z;
        }
        
        this.min = new Point3F(xMin, yMin, zMin);
        this.max = new Point3F(xMax, yMax, zMax);
    }
}
