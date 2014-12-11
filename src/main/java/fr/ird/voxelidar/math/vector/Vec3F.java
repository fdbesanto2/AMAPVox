/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.math.vector;

/**
 *
 * @author Julien
 */
public class Vec3F{
    
    public float x;
    public float y;
    public float z;
    
    public Vec3F(){
        
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }
    
    public Vec3F(float x, float y, float z){
        
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static Vec3F createVec3FromPoints(Vec3F point1, Vec3F point2){
        
        Vec3F result = new Vec3F();
        
        result.x = point2.x - point1.x;
        result.y = point2.y - point1.y;
        result.z = point2.z - point1.z;
        
        return result;
    }
    
    public static Vec3F cross(Vec3F vec, Vec3F vec2){
        
        Vec3F dest = new Vec3F();
        
        float x = vec.x, y = vec.y, z = vec.z;
        float x2 = vec2.x, y2 = vec2.y, z2 = vec2.z;
        
        dest.x = y*z2 - z*y2;
        dest.y = z*x2 - x*z2;
        dest.z = x*y2 - y*x2;
        
        return dest;
    }
    
    public static Vec3F normalize(Vec3F vec){
        
        Vec3F dest = new Vec3F();
        
        float x = vec.x, y = vec.y, z = vec.z;
        double len = Math.sqrt(x*x + y*y + z*z);
        
        if (len == 0) {
                dest.x = 0;
                dest.y = 0;
                dest.z = 0;
                return dest;
        } else if (len == 1) {
                dest.x = x;
                dest.y = y;
                dest.z = z;
                return dest;
        }
        
        len = 1 / len;
        dest.x = (float) (x*len);
        dest.y = (float) (y*len);
        dest.z = (float) (z*len);
        
        return dest;
    }
    
    public static Vec3F add(Vec3F vec1, Vec3F vec2){
        
        Vec3F result = new Vec3F();
        
        result.x = vec1.x + vec2.x;
        result.y = vec1.y + vec2.y;
        result.z = vec1.z + vec2.z;
        
        return result;
    }
    
    public static Vec3F substract(Vec3F vec1, Vec3F vec2){
        
        Vec3F result = new Vec3F();
        
        result.x = vec1.x - vec2.x;
        result.y = vec1.y - vec2.y;
        result.z = vec1.z - vec2.z;
        
        return result;
    }
    
    public static Vec3F multiply(Vec3F vec1, Vec3F vec2){
        
        Vec3F result = new Vec3F();
        
        result.x = vec1.x * vec2.x;
        result.y = vec1.y * vec2.y;
        result.z = vec1.z * vec2.z;
        
        return result;
    }
    
    public static Vec3F multiply(Vec3F vec, float multiplier){
        
        Vec3F result = Vec3F.multiply(vec, new Vec3F(multiplier, multiplier, multiplier));
        
        return result;
    }
    
    public static float length(Vec3F vec){
        
        float result = (float) Math.sqrt((vec.x * vec.x)+(vec.y * vec.y)+(vec.z * vec.z));
        
        return result;
    }
    
    public static float dot(Vec3F vec, Vec3F vec2){
        
        float result = vec.x*vec2.x + vec.y*vec2.y + vec.z*vec2.z;
        
        return result;
    }
    
    public static float[] toArray(Vec3F vec){
        
        float array[] = new float[3];
        
        array[0] = vec.x;
        array[1] = vec.y;
        array[2] = vec.z;
        
        return array;
    }

    
}
