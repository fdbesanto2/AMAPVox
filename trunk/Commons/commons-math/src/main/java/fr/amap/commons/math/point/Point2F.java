/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.point;

/**
 * A single precision 2d point
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Point2F {
    
    public float x, y;

    public Point2F() {
        this.x = 0;
        this.y = 0;
    }
    
    public Point2F(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    /**
     * Get the distance between two points
     * @param point the point to compare the distance from
     * @return the distance between the two points
     */
    public float distanceTo(Point2F point){
        
        return (float) Math.sqrt(Math.pow(point.x-x, 2)+Math.pow(point.y-y, 2));
    }
}
