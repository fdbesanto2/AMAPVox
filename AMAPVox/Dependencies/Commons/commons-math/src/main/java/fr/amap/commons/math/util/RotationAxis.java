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
public class RotationAxis {
    
    private double rotationAxisAngle;
    private double xRotationAxis;
    private double yRotationAxis;
    private double zRotationAxis;

    public RotationAxis(double rotationAxisAngle, double xRotationAxis, double yRotationAxis, double zRotationAxis) {
        this.rotationAxisAngle = rotationAxisAngle;
        this.xRotationAxis = xRotationAxis;
        this.yRotationAxis = yRotationAxis;
        this.zRotationAxis = zRotationAxis;
    }

    public double getRotationAxisAngle() {
        return rotationAxisAngle;
    }

    public void setRotationAxisAngle(double rotationAxisAngle) {
        this.rotationAxisAngle = rotationAxisAngle;
    }

    public double getXRotationAxis() {
        return xRotationAxis;
    }

    public void setxRotationAxis(double xRotationAxis) {
        this.xRotationAxis = xRotationAxis;
    }

    public double getYRotationAxis() {
        return yRotationAxis;
    }

    public void setyRotationAxis(double yRotationAxis) {
        this.yRotationAxis = yRotationAxis;
    }

    public double getZRotationAxis() {
        return zRotationAxis;
    }

    public void setzRotationAxis(double zRotationAxis) {
        this.zRotationAxis = zRotationAxis;
    }
}
