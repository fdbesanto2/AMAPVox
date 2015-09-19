/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.math.vector;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Vec3D{
    
    public double x;
    public double y;
    public double z;
    
    public Vec3D(){
        
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }
    
    public Vec3D(double x, double y, double z){
        
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static Vec3D createVec3DFromPoints(Vec3D point1, Vec3D point2){
        
        Vec3D result = new Vec3D();
        
        result.x = point2.x - point1.x;
        result.y = point2.y - point1.y;
        result.z = point2.z - point1.z;
        
        return result;
    }
    
    public static Vec3D cross(Vec3D vec, Vec3D vec2){
        
        Vec3D dest = new Vec3D();
        
        double x = vec.x, y = vec.y, z = vec.z;
        double x2 = vec2.x, y2 = vec2.y, z2 = vec2.z;
        
        dest.x = y*z2 - z*y2;
        dest.y = z*x2 - x*z2;
        dest.z = x*y2 - y*x2;
        
        return dest;
    }
    
    public static Vec3D normalize(Vec3D vec){
        
        Vec3D dest = new Vec3D();
        
        double x = vec.x, y = vec.y, z = vec.z;
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
        dest.x = (double) (x*len);
        dest.y = (double) (y*len);
        dest.z = (double) (z*len);
        
        return dest;
    }
    
    public static Vec3D add(Vec3D vec1, Vec3D vec2){
        
        Vec3D result = new Vec3D();
        
        result.x = vec1.x + vec2.x;
        result.y = vec1.y + vec2.y;
        result.z = vec1.z + vec2.z;
        
        return result;
    }
    
    public static Vec3D substract(Vec3D vec1, Vec3D vec2){
        
        Vec3D result = new Vec3D();
        
        result.x = vec1.x - vec2.x;
        result.y = vec1.y - vec2.y;
        result.z = vec1.z - vec2.z;
        
        return result;
    }
    
    public static Vec3D multiply(Vec3D vec1, Vec3D vec2){
        
        Vec3D result = new Vec3D();
        
        result.x = vec1.x * vec2.x;
        result.y = vec1.y * vec2.y;
        result.z = vec1.z * vec2.z;
        
        return result;
    }
    
    public static Vec3D multiply(Vec3D vec, double multiplier){
        
        Vec3D result = Vec3D.multiply(vec, new Vec3D(multiplier, multiplier, multiplier));
        
        return result;
    }
    
    public static double length(Vec3D vec){
        
        double result = (double) Math.sqrt((vec.x * vec.x)+(vec.y * vec.y)+(vec.z * vec.z));
        
        return result;
    }
    
    public static double dot(Vec3D vec, Vec3D vec2){
        
        double result = vec.x*vec2.x + vec.y*vec2.y + vec.z*vec2.z;
        
        return result;
    }
    
    public static double[] toArray(Vec3D vec){
        
        double array[] = new double[3];
        
        array[0] = vec.x;
        array[1] = vec.y;
        array[2] = vec.z;
        
        return array;
    }

    
}
