/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.math.basis;

import fr.ird.voxelidar.engine3d.math.vector.Vec3F;

/**
 *
 * @author Julien
 */
public class Basis {
    
    public Vec3F vectorX;
    public Vec3F vectorY;
    public Vec3F vectorZ;
    
    public static Basis createDefaultBasis(){
        
        Basis basis = new Basis (new Vec3F(1.0f, 0.0f, 0.0f), 
                                    new Vec3F(0.0f, 1.0f, 0.0f), 
                                    new Vec3F(0.0f, 0.0f, 1.0f));
        
        return basis;
    }
    
    public Basis(Vec3F vectorX, Vec3F vectorY, Vec3F vectorZ) {
        this.vectorX = vectorX;
        this.vectorY = vectorY;
        this.vectorZ = vectorZ;
    }
    /*
    public Vec3 changeBasisPoint(Basis basis, Vec3 point){
        
        Mat3 matrix = new Mat3();
        
        matrix.mat = new float[]{
            
            basis.vectorX.x, basis.vectorY.x, basis.vectorZ.x,
            basis.vectorX.y, basis.vectorY.y, basis.vectorZ.y,
            basis.vectorX.z, basis.vectorY.z, basis.vectorZ.z
        };
        
        float[] mat4x3 = new float[]{
            
            matrix.mat[0], matrix.mat[1], matrix.mat[2], point.x,
            matrix.mat[3], matrix.mat[4], matrix.mat[5], point.y,
            matrix.mat[6], matrix.mat[7], matrix.mat[8], point.z
        };
        
        //gaussian elimination
        
        //add -1 times the 1st row to the 3rd row
        mat4x3[8] = mat4x3[8] + (-1*mat4x3[0]);
        mat4x3[9] = mat4x3[9] + (-1*mat4x3[1]);
        mat4x3[10] = mat4x3[10] + (-1*mat4x3[2]);
        mat4x3[11] = mat4x3[11] + (-1*mat4x3[3]);
        
        //multiply the 2nd row by -1
        mat4x3[4] = mat4x3[4] * -1;
        mat4x3[5] = mat4x3[5] * -1;
        mat4x3[6] = mat4x3[6] * -1;
        mat4x3[7] = mat4x3[7] * -1;
        
        //add 1 times the 2nd row to the 3rd row
        mat4x3[8] = mat4x3[8] + mat4x3[4];
        mat4x3[9] = mat4x3[9] + mat4x3[5];
        mat4x3[10] = mat4x3[10] + mat4x3[6];
        mat4x3[11] = mat4x3[11] + mat4x3[7];
        
        //multiply the 3rd row by -1/4
        mat4x3[8] = mat4x3[8] * -0.25f;
        mat4x3[9] = mat4x3[9]  * -0.25f;
        mat4x3[10] = mat4x3[10]  * -0.25f;
        mat4x3[11] = mat4x3[11]  * -0.25f;
        
        //add 1 times the 3rd row to the 2nd row
        mat4x3[4] = mat4x3[4] + mat4x3[8];
        mat4x3[5] = mat4x3[5] + mat4x3[9];
        mat4x3[6] = mat4x3[6] + mat4x3[10];
        mat4x3[7] = mat4x3[7] + mat4x3[11];
        
        //add -3 times the 3rd row to the 1st row
        mat4x3[0] = mat4x3[0] + (-3*mat4x3[8]);
        mat4x3[1] = mat4x3[1] + (-3*mat4x3[9]);
        mat4x3[2] = mat4x3[2] + (-3*mat4x3[10]);
        mat4x3[3] = mat4x3[3] + (-3*mat4x3[11]);
        
        //add -2 times the 2nd row to the 1st row
        mat4x3[0] = mat4x3[0] + (-2*mat4x3[4]);
        mat4x3[1] = mat4x3[1] + (-2*mat4x3[5]);
        mat4x3[2] = mat4x3[2] + (-2*mat4x3[6]);
        mat4x3[3] = mat4x3[3] + (-2*mat4x3[7]);
        
        Vec3 basisChangePoint = new Vec3(mat4x3[3], mat4x3[7], mat4x3[11]);
        
        return basisChangePoint;
    
    }
    */
    public void changeBasisMatrix(Basis basis1, Basis basis2){
        
        //gaussian elimination
        
    }
    
}
