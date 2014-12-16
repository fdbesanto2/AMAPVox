/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.voxelspace;

/**
 *
 * @author Julien
 */
public class VoxelSpaceFormat1 extends VoxelSpaceFormat{
    
    double xMinCorner, yMinCorner, zMinCorner;
    double xMaxCorner, yMaxCorner, zMaxCorner;
    int xSplit, ySplit, zSplit;

    public double getxMinCorner() {
        return xMinCorner;
    }

    public void setxMinCorner(double xMinCorner) {
        this.xMinCorner = xMinCorner;
    }

    public double getyMinCorner() {
        return yMinCorner;
    }

    public void setyMinCorner(double yMinCorner) {
        this.yMinCorner = yMinCorner;
    }

    public double getzMinCorner() {
        return zMinCorner;
    }

    public void setzMinCorner(double zMinCorner) {
        this.zMinCorner = zMinCorner;
    }

    public double getxMaxCorner() {
        return xMaxCorner;
    }

    public void setxMaxCorner(double xMaxCorner) {
        this.xMaxCorner = xMaxCorner;
    }

    public double getyMaxCorner() {
        return yMaxCorner;
    }

    public void setyMaxCorner(double yMaxCorner) {
        this.yMaxCorner = yMaxCorner;
    }

    public double getzMaxCorner() {
        return zMaxCorner;
    }

    public void setzMaxCorner(double zMaxCorner) {
        this.zMaxCorner = zMaxCorner;
    }

    public int getxSplit() {
        return xSplit;
    }

    public void setxSplit(int xSplit) {
        this.xSplit = xSplit;
    }

    public int getySplit() {
        return ySplit;
    }

    public void setySplit(int ySplit) {
        this.ySplit = ySplit;
    }

    public int getzSplit() {
        return zSplit;
    }

    public void setzSplit(int zSplit) {
        this.zSplit = zSplit;
    }
    
}
