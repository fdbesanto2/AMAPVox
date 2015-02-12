/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.raytracing.voxel;

import java.io.File;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien
 */
public class VoxelParameters {
    
    public static final int WEIGHTING_NONE = 0;
    public static final int WEIGHTING_ECHOS_NUMBER = 1;
    public static final int WEIGHTING_FILE = 2;
    public static final int WEIGHTING_FRACTIONING = 3;
    
    Point3d bottomCorner;
    Point3d topCorner;
    Point3i split;
    double resolution;
    
    private int weighting;
    private File weightingFile;
    private boolean useDTMCorrection;
    
    
    public VoxelParameters() {
        useDTMCorrection = false;
    }

    public void setUseDTMCorrection(boolean useDTMCorrection) {
        this.useDTMCorrection = useDTMCorrection;
    }

    public boolean useDTMCorrection() {
        return useDTMCorrection;
    }
    
    public VoxelParameters(Point3d bottomCorner, Point3d topCorner, Point3i split) {
        
        this.bottomCorner = bottomCorner;
        this.topCorner = topCorner;
        this.split = split;
    }

    public Point3d getBottomCorner() {
        return bottomCorner;
    }

    public void setBottomCorner(Point3d bottomCorner) {
        this.bottomCorner = bottomCorner;
    }

    public Point3d getTopCorner() {
        return topCorner;
    }

    public void setTopCorner(Point3d topCorner) {
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

    public double getResolution() {
        return resolution;
    }

    public void setResolution(double resolution) {
        this.resolution = resolution;
    }
}
