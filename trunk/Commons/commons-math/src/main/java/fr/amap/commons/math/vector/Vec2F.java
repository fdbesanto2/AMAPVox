/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.vector;

import fr.amap.commons.math.point.Point2F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Vec2F extends Point2F{
    
    public Vec2F(){
        
        this.x = 0;
        this.y = 0;
    }
    
    public Vec2F(float x, float y){
        this.x = x;
        this.y = y;
    }
    
    public static Vec2F createVec2FromPoints(Vec2F point1, Vec2F point2){
        
        Vec2F result = new Vec2F();
        
        result.x = point2.x - point1.x;
        result.y = point2.y - point1.y;
        
        return result;
    }
    
    public static float determinant(Vec2F vec1, Vec2F vec2){
        
        float result = (vec1.x*vec2.y) - (vec1.y*vec2.x);
        
        return result;
    }
}
