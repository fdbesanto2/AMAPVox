/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.point;

/**
 * A double precision 2d point
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Point2D {
    
    public double x, y;

    public Point2D() {
        this.x = 0;
        this.y = 0;
    }
    
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }
    
}
