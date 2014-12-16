/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.math.vector;

import fr.ird.voxelidar.math.point.Point2D;

/**
 *
 * @author Julien
 */
public class Vec2D extends Point2D{
    
    public double x;
    public double y;
    
    public Vec2D(){
        
        this.x = 0;
        this.y = 0;
    }
    
    public Vec2D(double x, double y){
        this.x = x;
        this.y = y;
    }
    
    public static Vec2D createVec2DFromPoints(Vec2D point1, Vec2D point2){
        
        Vec2D result = new Vec2D();
        
        result.x = point2.x - point1.x;
        result.y = point2.y - point1.y;
        
        return result;
    }
    
    public static double determinant(Vec2D vec1, Vec2D vec2){
        
        double result = (vec1.x*vec2.y) - (vec1.y*vec2.x);
        
        return result;
    }
}
