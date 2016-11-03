/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.util;

/**
 *
 * @author Julien Heurtebize
 */
public class EulerRotation {
    
    private double xRotation;
    private double yRotation;
    private double zRotation;

    public EulerRotation(double xRotation, double yRotation, double zRotation) {
        this.xRotation = xRotation;
        this.yRotation = yRotation;
        this.zRotation = zRotation;
    }

    public void setxRotation(double xRotation) {
        this.xRotation = xRotation;
    }

    public void setyRotation(double yRotation) {
        this.yRotation = yRotation;
    }

    public void setzRotation(double zRotation) {
        this.zRotation = zRotation;
    }

    public double getXRotation() {
        return xRotation;
    }

    public double getYRotation() {
        return yRotation;
    }

    public double getZRotation() {
        return zRotation;
    }
}
