/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import java.io.File;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien
 */
public class VoxelParameters {
    
    public static final int WEIGHTING_NONE = 0;
    public static final int WEIGHTING_ECHOS_NUMBER = 1;
    public static final int WEIGHTING_FILE = 2;
    
    Point3f bottomCorner;
    Point3f topCorner;
    Point3i split;
    float resolution;
    
    private int weighting;
    private File weightingFile;
    
    
    public VoxelParameters() {
        
    }
    
    public VoxelParameters(Point3f bottomCorner, Point3f topCorner, Point3i split) {
        
        this.bottomCorner = bottomCorner;
        this.topCorner = topCorner;
        this.split = split;
    }

    public Point3f getBottomCorner() {
        return bottomCorner;
    }

    public void setBottomCorner(Point3f bottomCorner) {
        this.bottomCorner = bottomCorner;
    }

    public Point3f getTopCorner() {
        return topCorner;
    }

    public void setTopCorner(Point3f topCorner) {
        this.topCorner = topCorner;
    }

    public Point3i getSplit() {
        return split;
    }

    public void setSplit(Point3i split) {
        this.split = split;
    }

    public int getWeighting() {
        return weighting;
    }

    public void setWeighting(int weighting) {
        this.weighting = weighting;
    }

    public File getWeightingFile() {
        return weightingFile;
    }

    public void setWeightingFile(File weightingFile) {
        this.weightingFile = weightingFile;
    }

    public float getResolution() {
        return resolution;
    }

    public void setResolution(float resolution) {
        this.resolution = resolution;
    }
}
