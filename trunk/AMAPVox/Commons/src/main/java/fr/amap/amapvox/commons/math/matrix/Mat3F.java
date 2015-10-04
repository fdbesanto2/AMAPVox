/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.math.matrix;

import fr.amap.amapvox.commons.math.vector.Vec3F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Mat3F {
    
    /**
     *
     */
    public float[] mat;
    
    /**
     *
     */
    public Mat3F(){
        
        mat = new float[9];
    }
    
    public Mat3F(float[] source){
        
        mat = source;
        
    }
    
    
    /**
     *
     * @param source matrix to copy
     */
    public Mat3F(Mat3F source){
        
        mat = new float[9];
        
        mat[0] = source.mat[0];
        mat[1] = source.mat[1];
        mat[2] = source.mat[2];
        mat[3] = source.mat[3];
        mat[4] = source.mat[4];
        mat[5] = source.mat[5];
        mat[6] = source.mat[6];
        mat[7] = source.mat[7];
        mat[8] = source.mat[8];
        mat[9] = source.mat[9];
    }
    
    public static Vec3F multiply(Mat3F mat3F, Vec3F vec3F){
        
        Vec3F dest = new Vec3F();
        float[] mat = mat3F.mat;
        
        float a00 = mat[0], a01 = mat[1], a02 = mat[2];
        float a10 = mat[3], a11 = mat[4], a12 = mat[5];
        float a20 = mat[6], a21 = mat[7], a22 = mat[8];
        
        float v00 = vec3F.x;
        float v10 = vec3F.y;
        float v20 = vec3F.z;
        
        dest.x = a00 * v00 + a01 * v10 + a02 * v20;
        dest.y = a10 * v00 + a11 * v10 + a12 * v20;
        dest.z = a20 * v00 + a21 * v10 + a22 * v20;
        
        return dest;
    }
    
}
