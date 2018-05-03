/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.vector;

import fr.amap.commons.math.point.Point4F;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Vec4F extends Point4F{
    
    public Vec4F(){
        
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }
    
    public Vec4F(float x, float y, float z, float w){
        
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    /**
     * Normalize a 3d vector
     * @param vec The vector to normalize
     * @return The normalized vector
     */
    public static Vec4F normalize(Vec4F vec){
        
        Vec4F dest = new Vec4F();
        
        float x = vec.x, y = vec.y, z = vec.z, w = vec.w;
        double len = Math.sqrt(x*x + y*y + z*z + w*w);
        
        if (len == 0) {
                dest.x = 0;
                dest.y = 0;
                dest.z = 0;
                dest.w = 0;
                return dest;
        } /*else if (len == 1) {
                dest.x = x;
                dest.y = y;
                dest.z = z;
                return dest;
        }*/
        
        len = 1 / len;
        dest.x = (float) (x*len);
        dest.y = (float) (y*len);
        dest.z = (float) (z*len);
        dest.w = (float) (w*len);
        
        return dest;
    }
}
