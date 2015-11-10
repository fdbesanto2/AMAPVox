/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.math.vector;

import fr.amap.amapvox.math.point.Point3F;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Vec3F{
    
    public float x;
    public float y;
    public float z;
    
    /**
     * Constructs and initialize a new 3d single precision vector filled with zeros
     */
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
    
    public Vec3F(Point3F point1, Point3F point2){
        
        this.x = point2.x - point1.x;
        this.y = point2.y - point1.y;
        this.z = point2.z - point1.z;
    }
    
    /**
     * Create a new single precision 3d vector from two 3d points
     * @param point1 The first point
     * @param point2 The second point
     * @return A 3d vector constructed with the two given points
     */
    public static Vec3F createVec3FromPoints(Vec3F point1, Vec3F point2){
        
        Vec3F result = new Vec3F();
        
        result.x = point2.x - point1.x;
        result.y = point2.y - point1.y;
        result.z = point2.z - point1.z;
        
        return result;
    }
    
    public static Vec3F createVec3FFromPoints(Point3F point1, Point3F point2){
        
        Vec3F result = new Vec3F();
        
        result.x = point2.x - point1.x;
        result.y = point2.y - point1.y;
        result.z = point2.z - point1.z;
        
        return result;
    }
    
    /**
     * Get angle between two vectors
     * @param vec1 The first vector
     * @param vec2 The second vector
     * @return The angle (in radians) as a single precision number
     */
    public static float angle(Vec3F vec1, Vec3F vec2){
        
        float n = Vec3F.dot(vec1, vec2);
        float d = Vec3F.length(vec1)*Vec3F.length(vec2);
        
        float angle = (float) Math.acos(n/d);
        
        return angle;
    }
    
    /**
     * Get the cross product of two 3d vectors
     * @param vec First vector
     * @param vec2 Second vector
     * @return The cross product as a 3d single precision vector
     */
    public static Vec3F cross(Vec3F vec, Vec3F vec2){
        
        Vec3F dest = new Vec3F();
        
        float x = vec.x, y = vec.y, z = vec.z;
        float x2 = vec2.x, y2 = vec2.y, z2 = vec2.z;
        
        dest.x = y*z2 - z*y2;
        dest.y = z*x2 - x*z2;
        dest.z = x*y2 - y*x2;
        
        return dest;
    }
    
    /**
     * Normalize a 3d vector
     * @param vec The vector to normalize
     * @return The normalized vector
     */
    public static Vec3F normalize(Vec3F vec){
        
        Vec3F dest = new Vec3F();
        
        float x = vec.x, y = vec.y, z = vec.z;
        double len = Math.sqrt(x*x + y*y + z*z);
        
        if (len == 0) {
                dest.x = 0;
                dest.y = 0;
                dest.z = 0;
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
        
        return dest;
    }
    
    /**
     * Add a 3d vector to another
     * @param vec1 The first vector
     * @param vec2 The second vector
     * @return The vector addition as a 3d single precision vector
     */
    public static Vec3F add(Vec3F vec1, Vec3F vec2){
        
        Vec3F result = new Vec3F();
        
        result.x = vec1.x + vec2.x;
        result.y = vec1.y + vec2.y;
        result.z = vec1.z + vec2.z;
        
        return result;
    }
    
    /**
     * Substract a 3d vector from another
     * @param vec1 The original vector
     * @param vec2 The second vector
     * @return The original 3d vector, substracted by the other
     */
    public static Vec3F substract(Vec3F vec1, Vec3F vec2){
        
        Vec3F result = new Vec3F();
        
        result.x = vec1.x - vec2.x;
        result.y = vec1.y - vec2.y;
        result.z = vec1.z - vec2.z;
        
        return result;
    }
    
    /**
     * Multiply a 3d vector by another
     * @param vec1 The first vector
     * @param vec2 The second vector
     * @return A 3d vector, result of the product of the first vector by the second one
     */
    public static Vec3F multiply(Vec3F vec1, Vec3F vec2){
        
        Vec3F result = new Vec3F();
        
        result.x = vec1.x * vec2.x;
        result.y = vec1.y * vec2.y;
        result.z = vec1.z * vec2.z;
        
        return result;
    }
    
    /**
     * Multiply a 3d vector by a single precision number
     * @param vec The 3d vector
     * @param multiplier The factor
     * @return A 3d vector, result of the product of the vector by the factor
     */
    public static Vec3F multiply(Vec3F vec, float multiplier){
        
        Vec3F result = Vec3F.multiply(vec, new Vec3F(multiplier, multiplier, multiplier));
        
        return result;
    }
    
    /**
     * Get the length of a single precision 3d vector
     * @param vec The vector
     * @return The length of the vector, as a single precision number
     */
    public static float length(Vec3F vec){
        
        float result = (float) Math.sqrt((vec.x * vec.x)+(vec.y * vec.y)+(vec.z * vec.z));
        
        return result;
    }
    
    /**
     * Get the dot product of two 3d vector
     * @param vec The first vector
     * @param vec2 The second vector
     * @return The result of the dot product as a single precision number
     */
    public static float dot(Vec3F vec, Vec3F vec2){
        
        float result = vec.x*vec2.x + vec.y*vec2.y + vec.z*vec2.z;
        
        return result;
    }
    
    /**
     * Convert the given vector to a 1 dimensional array with a size of 3
     * @param vec The 3d vector
     * @return a one dimensional single precision array with a length of 3
     */
    public static float[] toArray(Vec3F vec){
        
        float array[] = new float[3];
        
        array[0] = vec.x;
        array[1] = vec.y;
        array[2] = vec.z;
        
        return array;
    }
    
}
