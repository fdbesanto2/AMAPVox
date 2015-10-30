/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.math.point;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Point3F  implements Comparable<Point3F>{
    
    public float x, y, z;

    public Point3F() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public Point3F(float x, float y, float z) {
        
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
    public static Point3F middle(Point3F point1, Point3F point2){
        
        Point3F middle = new Point3F((point1.x+point2.x)/2.0f,
                                    (point1.y+point2.y)/2.0f,
                                    (point1.z+point2.z)/2.0f);
        
        return middle;
    }
    
    /**
     *
     * @param point
     * @return
     */
    public float distanceTo(Point3F point){
        
        return (float) Math.sqrt(Math.pow(point.x-x, 2)+Math.pow(point.y-y, 2)+Math.pow(point.z-z, 2));
    }
    
    /**
     * Order points in the following order: x, y, z
     * @param point {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public int compareTo(Point3F point) {
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
