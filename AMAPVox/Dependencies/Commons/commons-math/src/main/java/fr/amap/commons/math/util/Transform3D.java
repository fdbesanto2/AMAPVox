/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.util;

import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class Transform3D {
    
    private final Matrix4d matrix = new Matrix4d();
    
    private double eulerXRotation;
    private double eulerYRotation;
    private double eulerZRotation;
    
    private double rotationAxisAngle;
    private double xRotationAxis;
    private double yRotationAxis;
    private double zRotationAxis;
    
    private boolean rotationAxisAngleDirty;
    private boolean eulerDirty;

    public Transform3D() {
        
        matrix.setIdentity();
    }
    
    private void updateRotationAxis(){
        
        double m00 = matrix.m00;
        double m11 = matrix.m11;
        double m22 = matrix.m22;
        double m21 = matrix.m21;
        double m12 = matrix.m12;
        double m02 = matrix.m02;
        double m20 = matrix.m20;
        double m10 = matrix.m10;
        double m01 = matrix.m01;

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

        rotationAxisAngle = Math.toDegrees(alpha_rad);

        xRotationAxis = axis.x;
        yRotationAxis = axis.y;
        zRotationAxis = axis.z;
    }
    
    private void updateEuler(){
        
        double m20 = matrix.m20;
        double m21 = matrix.m21;
        double m22 = matrix.m22;
        double m00 = matrix.m00;
        double m10 = matrix.m10;
        double m01 = matrix.m01;
        double m02 = matrix.m02;


        double theta_rad;
        double cos_theta;
        double psi_rad;
        double phi_rad;

        if (Math.abs(m20) != 1){

                theta_rad = -Math.asin(m20);
                cos_theta = Math.cos(theta_rad);
                psi_rad = Math.atan2(m21/cos_theta, m22/cos_theta);
                phi_rad = Math.atan2(m10/cos_theta, m00/cos_theta);
        }else{
                phi_rad = 0;

                if (m20 == -1)
                {
                    theta_rad = (Math.PI)/2.0d;
                    psi_rad = Math.atan2(m01,m02);
                }
                else
                {
                    theta_rad = -(Math.PI)/2.0d;
                    psi_rad = -Math.atan2(m01,m02);
                }
        }

        eulerXRotation = Math.toDegrees(psi_rad);
        eulerYRotation = Math.toDegrees(theta_rad);
        eulerZRotation = Math.toDegrees(phi_rad);
    }
    
    public EulerRotation getEulerRotation(){
        
        if(eulerDirty){
            updateEuler();
            eulerDirty = false;
        }
        
        return new EulerRotation(eulerXRotation, eulerYRotation, eulerZRotation);
    }
    
    public RotationAxis getRotationAxis(){
        
        if(rotationAxisAngleDirty){
            updateRotationAxis();
            rotationAxisAngleDirty = false;
        }
        
        return new RotationAxis(rotationAxisAngle, xRotationAxis, yRotationAxis, zRotationAxis);
    }
    
    public void setRotationAxis(RotationAxis rotationAxis){
        
        double uX = rotationAxis.getXRotationAxis();
        double uY = rotationAxis.getYRotationAxis();
        double uZ = rotationAxis.getZRotationAxis();
        
        Vector3d vec = new Vector3d(uX, uY, uZ);
        vec.normalize();
        
        if(!Double.isNaN(vec.length())){
            
            uX = vec.x;
            uY = vec.y;
            uZ = vec.z;

            double angle = rotationAxis.getRotationAxisAngle();

            double c = Math.cos(Math.toRadians(angle));
            double s = Math.sin(Math.toRadians(angle));

            matrix.m00 = (uX*uX)+(1-(uX*uX))*c;
            matrix.m01 = (uX*uY)*(1-c)-(uZ*s);
            matrix.m02 = (uX*uZ)*(1-c)+(uY*s);
            matrix.m10 = (uX*uY)*(1-c)+(uZ*s);
            matrix.m11 = (uY*uY)+(1-(uY*uY))*c;
            matrix.m12 = (uY*uZ)*(1-c)-(uX*s);
            matrix.m20 = (uX*uZ)*(1-c)-(uY*s);
            matrix.m21 = (uY*uZ)*(1-c)+(uX*s);
            matrix.m22 = (uZ*uZ)+(1-(uZ*uZ))*c;
            
            xRotationAxis = uX;
            yRotationAxis = uY;
            zRotationAxis = uZ;
            rotationAxisAngle = angle;
            
            eulerDirty = true;
        }
    }
    
    /**
     * 
     * @param eulerRotation in degrees
     */
    public void setEulerRotation(EulerRotation eulerRotation){
        
        matrix.m11 = Math.cos(Math.toRadians(eulerRotation.getXRotation()));
        matrix.m12 = -Math.sin(Math.toRadians(eulerRotation.getXRotation()));
        matrix.m21 = Math.sin(Math.toRadians(eulerRotation.getXRotation()));
        matrix.m22 = Math.cos(Math.toRadians(eulerRotation.getXRotation()));
        
        matrix.m00 = Math.cos(Math.toRadians(eulerRotation.getYRotation()));
        matrix.m02 = Math.sin(Math.toRadians(eulerRotation.getYRotation()));
        matrix.m20 = -Math.sin(Math.toRadians(eulerRotation.getYRotation()));
        matrix.m22 = Math.cos(Math.toRadians(eulerRotation.getYRotation()));
        
        matrix.m00 = Math.cos(Math.toRadians(eulerRotation.getZRotation()));
        matrix.m01 = -Math.sin(Math.toRadians(eulerRotation.getZRotation()));
        matrix.m10 = Math.sin(Math.toRadians(eulerRotation.getZRotation()));
        matrix.m11 = Math.cos(Math.toRadians(eulerRotation.getZRotation()));
        
        eulerXRotation = eulerRotation.getXRotation();
        eulerYRotation = eulerRotation.getYRotation();
        eulerZRotation = eulerRotation.getZRotation();
        
        rotationAxisAngleDirty = true;
    }
    
    public void setPosition(Point3d position){
        
        matrix.m03 = position.x;
        matrix.m13 = position.y;
        matrix.m23 = position.z;
    }
    
    public Point3d getPosition(Point3d position){
        
        return new Point3d(matrix.m03, matrix.m13, matrix.m23);
    }

    public Matrix4d getMatrix() {
        return new Matrix4d(matrix);
    }
    
    public void setMatrix(Matrix4d newMatrix){
        matrix.set(newMatrix);
        rotationAxisAngleDirty = true;
        eulerDirty = true;
    }
}
