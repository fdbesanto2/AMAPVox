/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import javax.vecmath.Matrix4d;

/**
 * Convert Matrix from vecmath to Mat
 * @author Julien
 */
public class MatrixConverter {
    
    public static Mat4D convertMatrix4dToMat4D(Matrix4d inputMatrix){
        
        double[] outputMatrix = new double[16];
        
        int count = 0;
        for(int i=0;i<4;i++){
            for(int j=0;j<4;j++){
                outputMatrix[count] = inputMatrix.getElement(i, j);
                count++;
            }
        }
        
        Mat4D matrix = new Mat4D();
        matrix.mat = outputMatrix;
               
        return matrix;
    }
    
    public static Matrix4d convertMat4DToMatrix4d(Mat4D inputMatrix){
        
        return new Matrix4d(inputMatrix.mat);
    }
}
