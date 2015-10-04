/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.math.point;

/**
 *
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
    
    public float distanceTo(Point2F point){
        
        return (float) Math.sqrt(Math.pow(point.x-x, 2)+Math.pow(point.y-y, 2));
    }
}
