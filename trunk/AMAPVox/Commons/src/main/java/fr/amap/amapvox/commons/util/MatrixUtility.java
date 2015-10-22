/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.util;

import fr.amap.amapvox.math.matrix.Mat4D;
import fr.amap.amapvox.math.vector.Vec2D;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;

/**
 * Convert Matrix from vecmath to Mat
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MatrixUtility {
    
    public static Mat4D convertMatrix4dToMat4D(Matrix4d inputMatrix){
        
        if(inputMatrix == null){
            return null;
        }
        
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
    
    public static Matrix4d getMatrixTransformation(Vector3d point1, Vector3d point2) {

        if ((point1.x == point2.x) && (point1.y == point2.y) && (point1.z == point2.z)) {

            return new Matrix4d();
        }
        Vec2D v = new Vec2D(point1.x - point2.x, point1.y - point2.y);
        double rho = (double) Math.atan(v.x / v.y);

        Vector3d trans = new Vector3d(-point2.x, -point2.y, -point2.z);
        trans.z = 0; //no vertical translation

        Matrix4d mat4x4Rotation = new Matrix4d();
        Matrix4d mat4x4Translation = new Matrix4d();

        //rotation autour de l'axe z
        mat4x4Rotation.set(new double[]{
            (double) Math.cos(rho), (double) -Math.sin(rho), 0, 0,
            (double) Math.sin(rho), (double) Math.cos(rho), 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        });

        mat4x4Translation.set(new double[]{
            1, 0, 0, trans.x,
            0, 1, 0, trans.y,
            0, 0, 1, trans.z,
            0, 0, 0, 1
        });

        mat4x4Rotation.mul(mat4x4Translation);
        return mat4x4Rotation;
    }
    
    public static Matrix4d convertMat4DToMatrix4d(Mat4D inputMatrix){
        
        return new Matrix4d(inputMatrix.mat);
    }
}
