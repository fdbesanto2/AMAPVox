/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.math.matrix;

/**
 *
 * @author Julien
 */
public class Mat3D {
    
    /**
     *
     */
    public double[] mat;
    
    /**
     *
     */
    public Mat3D(){
        
        mat = new double[9];
        
    }
    
    /**
     *
     * @return
     */
    public Mat toMat(){
        Mat result = new Mat(3,3);
        
        result.setData(mat);
        
        return result;
    }
    
    /**
     *
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
    
    
}
