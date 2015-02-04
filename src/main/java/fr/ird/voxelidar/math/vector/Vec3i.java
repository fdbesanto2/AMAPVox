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
public class Vec3i{
    
    public int x;
    public int y;
    public int z;
    
    public Vec3i(){
        
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public Vec3i(int x, int y, int z){
        
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static Vec3i createVec3iromPoints(Vec3i point1, Vec3i point2){
        
        Vec3i result = new Vec3i();
        
        result.x = point2.x - point1.x;
        result.y = point2.y - point1.y;
        result.z = point2.z - point1.z;
        
        return result;
    }
    
    public static Vec3i cross(Vec3i vec, Vec3i vec2){
        
        Vec3i dest = new Vec3i();
        
        int x = vec.x, y = vec.y, z = vec.z;
        int x2 = vec2.x, y2 = vec2.y, z2 = vec2.z;
        
        dest.x = y*z2 - z*y2;
        dest.y = z*x2 - x*z2;
        dest.z = x*y2 - y*x2;
        
        return dest;
    }
    
    public static Vec3i normalize(Vec3i vec){
        
        Vec3i dest = new Vec3i();
        
        int x = vec.x, y = vec.y, z = vec.z;
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
        dest.x = (int) (x*len);
        dest.y = (int) (y*len);
        dest.z = (int) (z*len);
        
        return dest;
    }
    
    public static Vec3i add(Vec3i vec1, Vec3i vec2){
        
        Vec3i result = new Vec3i();
        
        result.x = vec1.x + vec2.x;
        result.y = vec1.y + vec2.y;
        result.z = vec1.z + vec2.z;
        
        return result;
    }
    
    public static Vec3i substract(Vec3i vec1, Vec3i vec2){
        
        Vec3i result = new Vec3i();
        
        result.x = vec1.x - vec2.x;
        result.y = vec1.y - vec2.y;
        result.z = vec1.z - vec2.z;
        
        return result;
    }
    
    public static Vec3i multiply(Vec3i vec1, Vec3i vec2){
        
        Vec3i result = new Vec3i();
        
        result.x = vec1.x * vec2.x;
        result.y = vec1.y * vec2.y;
        result.z = vec1.z * vec2.z;
        
        return result;
    }
    
    public static Vec3i multiply(Vec3i vec, int multiplier){
        
        Vec3i result = Vec3i.multiply(vec, new Vec3i(multiplier, multiplier, multiplier));
        
        return result;
    }
    
    public static int length(Vec3i vec){
        
        int result = (int) Math.sqrt((vec.x * vec.x)+(vec.y * vec.y)+(vec.z * vec.z));
        
        return result;
    }
    
    public static int dot(Vec3i vec, Vec3i vec2){
        
        int result = vec.x*vec2.x + vec.y*vec2.y + vec.z*vec2.z;
        
        return result;
    }
    
    public static int[] toArray(Vec3i vec){
        
        int array[] = new int[3];
        
        array[0] = vec.x;
        array[1] = vec.y;
        array[2] = vec.z;
        
        return array;
    }

    
}
