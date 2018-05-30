/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

/**
 *
 * @author Julien Heurtebize
 */
public class EchoesWeightByRankParams {

    /**
     * Weighting matrix as a 2d array, first number of shot's echoes then echo
     * rank
     */
    private final float[][] weightingData;

    public final static float[][] DEFAULT_ALS_WEIGHTING = new float[][]{
        {1.00f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
        {0.62f, 0.38f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
        {0.40f, 0.35f, 0.25f, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
        {0.28f, 0.29f, 0.24f, 0.19f, Float.NaN, Float.NaN, Float.NaN},
        {0.21f, 0.24f, 0.21f, 0.19f, 0.15f, Float.NaN, Float.NaN},
        {0.16f, 0.21f, 0.19f, 0.18f, 0.14f, 0.12f, Float.NaN},
        {0.15f, 0.17f, 0.15f, 0.16f, 0.12f, 0.19f, 0.06f}};

    public final static float[][] DEFAULT_TLS_WEIGHTING = new float[][]{
        {1.00f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
        {0.50f, 0.50f, Float.NaN, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
        {1 / 3.0f, 1 / 3.0f, 1 / 3.0f, Float.NaN, Float.NaN, Float.NaN, Float.NaN},
        {0.25f, 0.25f, 0.25f, 0.25f, Float.NaN, Float.NaN, Float.NaN},
        {0.20f, 0.20f, 0.20f, 0.20f, 0.20f, Float.NaN, Float.NaN},
        {1 / 6.0f, 1 / 6.0f, 1 / 6.0f, 1 / 6.0f, 1 / 6.0f, 1 / 6.0f, Float.NaN},
        {1 / 7.0f, 1 / 7.0f, 1 / 7.0f, 1 / 7.0f, 1 / 7.0f, 1 / 7.0f, 1 / 7.0f}};

    public EchoesWeightByRankParams(float[][] weightingData) {
        this.weightingData = weightingData;
    }

    /**
     *
     * @return Weighting matrix as a 2d array, first by number of shot's echoes
     * then echo rank
     */
    public float[][] getWeightingData() {
        return weightingData;
    }
}
