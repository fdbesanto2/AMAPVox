/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

/**
 *
 * @author Julien
 */
public class VoxelisationParameters {
    
    private double lowerCornerX, lowerCornerY, lowerCornerZ;
    private double topCornerX, topCornerY, topCornerZ;
    private int splitX, splitY, splitZ;
    private float resolution;

    public VoxelisationParameters(double lowerCornerX, double lowerCornerY, double lowerCornerZ, double topCornerX, double topCornerY, double topCornerZ, int splitX, int splitY, int splitZ, float resolution) {
        this.lowerCornerX = lowerCornerX;
        this.lowerCornerY = lowerCornerY;
        this.lowerCornerZ = lowerCornerZ;
        this.topCornerX = topCornerX;
        this.topCornerY = topCornerY;
        this.topCornerZ = topCornerZ;
        this.splitX = splitX;
        this.splitY = splitY;
        this.splitZ = splitZ;
        this.resolution = resolution;
    }

    public float getResolution() {
        return resolution;
    }

    public void setResolution(float resolution) {
        this.resolution = resolution;
    }

    
    public double getLowerCornerX() {
        return lowerCornerX;
    }

    public void setLowerCornerX(double lowerCornerX) {
        this.lowerCornerX = lowerCornerX;
    }

    public double getLowerCornerY() {
        return lowerCornerY;
    }

    public void setLowerCornerY(double lowerCornerY) {
        this.lowerCornerY = lowerCornerY;
    }

    public double getLowerCornerZ() {
        return lowerCornerZ;
    }

    public void setLowerCornerZ(double lowerCornerZ) {
        this.lowerCornerZ = lowerCornerZ;
    }

    public double getTopCornerX() {
        return topCornerX;
    }

    public void setTopCornerX(double topCornerX) {
        this.topCornerX = topCornerX;
    }

    public double getTopCornerY() {
        return topCornerY;
    }

    public void setTopCornerY(double topCornerY) {
        this.topCornerY = topCornerY;
    }

    public double getTopCornerZ() {
        return topCornerZ;
    }

    public void setTopCornerZ(double topCornerZ) {
        this.topCornerZ = topCornerZ;
    }

    public int getSplitX() {
        return splitX;
    }

    public void setSplitX(int splitX) {
        this.splitX = splitX;
    }

    public int getSplitY() {
        return splitY;
    }

    public void setSplitY(int splitY) {
        this.splitY = splitY;
    }

    public int getSplitZ() {
        return splitZ;
    }

    public void setSplitZ(int splitZ) {
        this.splitZ = splitZ;
    }
    
    
    
    
}
