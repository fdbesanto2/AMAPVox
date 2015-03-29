/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

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
    
    public Point3d bottomCorner;
    public Point3d topCorner;
    public Point3i split;
    public double resolution;
    
    private int weighting;
    private File weightingFile;
    private float[][] weightingData;
    
    private boolean useDTMCorrection;
    private File dtmFile;
    public float minDTMDistance = 1;
    private boolean TLS;
    
    public static float[][] DEFAULT_ALS_WEIGHTING = new float[][]{
                        {1.00f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.62f, 0.38f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.40f, 0.35f, 0.25f, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.28f, 0.29f, 0.24f, 0.19f, Float.NaN, Float.NaN, Float.NaN},
                        {0.21f, 0.24f, 0.21f, 0.19f, 0.15f, Float.NaN, Float.NaN},
                        {0.16f, 0.21f, 0.19f, 0.18f, 0.14f, 0.12f, Float.NaN},
                        {0.15f, 0.17f, 0.15f, 0.16f, 0.12f, 0.19f, 0.06f}};
    
    public static float[][] DEFAULT_TLS_WEIGHTING = new float[][]{
                        {1.00f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.50f, 0.50f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.33f, 0.33f, 0.33f, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
                        {0.25f, 0.25f, 0.25f, 0.25f, Float.NaN, Float.NaN, Float.NaN},
                        {0.20f, 0.20f, 0.20f, 0.20f, 0.20f, Float.NaN, Float.NaN},
                        {0.16f, 0.16f, 0.16f, 0.16f, 0.16f, 0.16f, Float.NaN},
                        {0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f, 0.142857143f}};
    
    
    public VoxelParameters() {
        useDTMCorrection = false;
        TLS = false;
    }

    public void setUseDTMCorrection(boolean useDTMCorrection) {
        this.useDTMCorrection = useDTMCorrection;
    }

    public boolean useDTMCorrection() {
        return useDTMCorrection;
    }

    public File getDtmFile() {
        return dtmFile;
    }

    public void setDtmFile(File dtmFile) {
        this.dtmFile = dtmFile;
    }

    public float[][] getWeightingData() {
        return weightingData;
    }

    public void setWeightingData(float[][] weightingData) {
        this.weightingData = weightingData;
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

    public boolean isTLS() {
        return TLS;
    }

    public void setTLS(boolean TLS) {
        this.TLS = TLS;
    }
    
}
