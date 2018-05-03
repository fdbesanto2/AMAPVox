/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.matrix;

import fr.amap.commons.math.vector.Vec3D;


/**
 * A double precision 3x3 matrix
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Mat3D {
    
    /**
     * The matrix array
     */
    public double[] mat;
    
    /**
     * Constructs and initialize a new double precision 3x3 matrix filled with 0
     */
    public Mat3D(){
        
        mat = new double[9];
        
    }
    
    /**
     * Convert the 3x3 matrix to an undefine sized matrix
     * @return a new Mat object with 3x3 size
     */
    public Mat toMat(){
        
        Mat result = new Mat(3,3);
        result.setData(mat);
        
        return result;
    }
    
    /**
     * Constructs and initialize a new 3x3 matrix with an existing matrix
     * @param source matrix to copy
     */
    public Mat3D(Mat3D source){
        
        mat = new double[9];
        
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
    
    /**
     * Multiply a double precision 3x3 matrix by a double precision 3d vector in this order
     * @param mat3D the 3x3 double precision matrix
     * @param vec3D the 3x3 double precision vector
     * @return the result as a new double precision 3d vector
     */
    public static Vec3D multiply(Mat3D mat3D, Vec3D vec3D){
        
        Vec3D dest = new Vec3D();
        double[] mat = mat3D.mat;
        
        double a00 = mat[0], a01 = mat[1], a02 = mat[2];
        double a10 = mat[3], a11 = mat[4], a12 = mat[5];
        double a20 = mat[6], a21 = mat[7], a22 = mat[8];
        
        double v00 = vec3D.x;
        double v10 = vec3D.y;
        double v20 = vec3D.z;
        
        dest.x = a00 * v00 + a01 * v10 + a02 * v20;
        dest.y = a10 * v00 + a11 * v10 + a12 * v20;
        dest.z = a20 * v00 + a21 * v10 + a22 * v20;
        
        return dest;
    }
    
}
