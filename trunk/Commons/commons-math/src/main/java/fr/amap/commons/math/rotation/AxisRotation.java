/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.rotation;

import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class AxisRotation {
    
    private double xAxisRotation;
    private double yAxisRotation;
    private double zAxisRotation;
    private double rotationAngle;

    public AxisRotation(double xAxisRotation, double yAxisRotation, double zAxisRotation, double rotationAngle) {
        this.xAxisRotation = xAxisRotation;
        this.yAxisRotation = yAxisRotation;
        this.zAxisRotation = zAxisRotation;
        this.rotationAngle = rotationAngle;
    }

    public double getxAxisRotation() {
        return xAxisRotation;
    }

    public void setxAxisRotation(double xAxisRotation) {
        this.xAxisRotation = xAxisRotation;
    }

    public double getyAxisRotation() {
        return yAxisRotation;
    }

    public void setyAxisRotation(double yAxisRotation) {
        this.yAxisRotation = yAxisRotation;
    }

    public double getzAxisRotation() {
        return zAxisRotation;
    }

    public void setzAxisRotation(double zAxisRotation) {
        this.zAxisRotation = zAxisRotation;
    }

    public double getRotationAngle() {
        return rotationAngle;
    }

    public void setRotationAngle(double rotationAngle) {
        this.rotationAngle = rotationAngle;
    }
    
    public Mat3D getRotationMatrix(){
        
        Mat3D rotationMatrix = new Mat3D();
        
        double uX = xAxisRotation, uY = yAxisRotation, uZ = zAxisRotation;
        
        Vector3d vec = new Vector3d(uX, uY, uZ);
        vec.normalize();
        
        if(!Double.isNaN(vec.length())){
            uX = vec.x;
            uY = vec.y;
            uZ = vec.z;

            double angle = rotationAngle;

            double c = Math.cos(Math.toRadians(angle));
            double s = Math.sin(Math.toRadians(angle));
            
            rotationMatrix.mat = new double[]{
                (uX*uX)+(1-(uX*uX))*c, (uX*uY)*(1-c)-(uZ*s), (uX*uZ)*(1-c)+(uY*s),
                (uX*uY)*(1-c)+(uZ*s), (uY*uY)+(1-(uY*uY))*c, (uY*uZ)*(1-c)-(uX*s),
                (uX*uZ)*(1-c)-(uY*s), (uY*uZ)*(1-c)+(uX*s), (uZ*uZ)+(1-(uZ*uZ))*c
            };
            
            return rotationMatrix;
            
        }else{
            return null;
        }
    }
    
    public static AxisRotation getRotationAxisAndAngle(Mat4D matrix){
        
        double m00 = matrix.mat[0];
        double m11 = matrix.mat[5];
        double m22 = matrix.mat[10];
        double m21 = matrix.mat[9];
        double m12 = matrix.mat[6];
        double m02 = matrix.mat[2];
        double m20 = matrix.mat[8];
        double m10 = matrix.mat[4];
        double m01 = matrix.mat[1];

        double trace = m00 + m11 + m22;
        double cos_t = (trace - 1)/2.0d;

        double alpha_rad;

        Vector3d axis = new Vector3d();

        if (Math.abs(cos_t) <= 1)
        {
            alpha_rad = Math.acos(cos_t); //result in [0;pi]
        }else{
            alpha_rad = 0;
        }

        axis.x = m21-m12;
        axis.y = m02-m20;
        axis.z = m10-m01;

        //normalize axis
        double n2 = axis.lengthSquared();

        if (n2 > 0)
        {
            axis.x /= Math.sqrt(n2);
            axis.y /= Math.sqrt(n2);
            axis.z /= Math.sqrt(n2);
        }
        else
        {
            //axis is too small!
            axis = new Vector3d(0, 0, 1);
        }

        return new AxisRotation(axis.x, axis.y, axis.z, Math.toDegrees(alpha_rad));
    }
    
}
