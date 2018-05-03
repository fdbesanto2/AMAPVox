/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.rotation;

import fr.amap.commons.math.matrix.Mat4D;

/**
 *
 * @author Julien Heurtebize
 */
public class EulerRotation {
    
    private double psi;
    private double theta;
    private double phi;

    public EulerRotation(double psi, double theta, double phi) {
        this.psi = psi;
        this.theta = theta;
        this.phi = phi;
    }

    /**
     * Get roll
     * @return 
     */
    public double getPsi() {
        return psi;
    }

    public void setPsi(double psi) {
        this.psi = psi;
    }

    /**
     * Get pitch
     * @return 
     */
    public double getTheta() {
        return theta;
    }

    public void setTheta(double theta) {
        this.theta = theta;
    }

    /**
     * Get yaw
     * @return 
     */
    public double getPhi() {
        return phi;
    }

    public void setPhi(double phi) {
        this.phi = phi;
    }
    
    public static EulerRotation getEulerFromMatrix(Mat4D matrix){
                 
        double m20 = matrix.mat[8];
        double m21 = matrix.mat[9];
        double m22 = matrix.mat[10];
        double m00 = matrix.mat[0];
        double m10 = matrix.mat[4];
        double m01 = matrix.mat[1];
        double m02 = matrix.mat[2];


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

        return new EulerRotation(Math.toDegrees(psi_rad), Math.toDegrees(theta_rad), Math.toDegrees(phi_rad));
    }
    
}
