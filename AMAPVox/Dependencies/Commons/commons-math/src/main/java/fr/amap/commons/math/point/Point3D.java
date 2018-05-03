/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.point;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Point3D  implements Comparable<Point3D>{
    
    public double x, y, z;

    public Point3D() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public Point3D(double x, double y, double z) {
        
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    /**
     *
     * @param point1
     * @param point2
     * @return
     */
    public static Point3D middle(Point3D point1, Point3D point2){
        
        Point3D middle = new Point3D((point1.x+point2.x)/2.0f,
                                    (point1.y+point2.y)/2.0f,
                                    (point1.z+point2.z)/2.0f);
        
        return middle;
    }
    
    /**
     *
     * @param point
     * @return
     */
    public double distanceTo(Point3D point){
        
        return (double) Math.sqrt(Math.pow(point.x-x, 2)+Math.pow(point.y-y, 2)+Math.pow(point.z-z, 2));
    }
    
    /**
     * Order points in the following order: x, y, z
     * @param point {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Point3D point) {
        if(point.x > this.x){
            return -1;
        }else if(point.x < this.x){
            return 1;
        }else{
            if(point.y > this.y){
                return -1;
            }else if(point.y < this.y){
                return 1;
            }else{
                if(point.z > this.z){
                    return -1;
                }else if(point.z < this.z){
                    return 1;
                }else{
                    return 0;
                }
            }
        }
    }
}
