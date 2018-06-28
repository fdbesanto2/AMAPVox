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
    private final double[][] weightingData;

    public final static double[][] DEFAULT_ALS_WEIGHTING = new double[][]{
        {1.00d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.62d, 0.38d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.40d, 0.35d, 0.25d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.28d, 0.29d, 0.24d, 0.19d, Double.NaN, Double.NaN, Double.NaN},
        {0.21d, 0.24d, 0.21d, 0.19d, 0.15d, Double.NaN, Double.NaN},
        {0.16d, 0.21d, 0.19d, 0.18d, 0.14d, 0.12d, Double.NaN},
        {0.15d, 0.17d, 0.15d, 0.16d, 0.12d, 0.19d, 0.06f}};

    public final static double[][] DEFAULT_TLS_WEIGHTING = new double[][]{
        {1.00d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.50d, 0.50d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {1 / 3.0d, 1 / 3.0d, 1 / 3.0d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.25d, 0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN, Double.NaN},
        {0.20d, 0.20d, 0.20d, 0.20d, 0.20d, Double.NaN, Double.NaN},
        {1 / 6.0d, 1 / 6.0d, 1 / 6.0d, 1 / 6.0d, 1 / 6.0d, 1 / 6.0d, Double.NaN},
        {1 / 7.0d, 1 / 7.0d, 1 / 7.0d, 1 / 7.0d, 1 / 7.0d, 1 / 7.0d, 1 / 7.0f}};

    public EchoesWeightByRankParams(double[][] weightingData) {
        this.weightingData = weightingData;
    }

    /**
     *
     * @return Weighting matrix as a 2d array, first by number of shot's echoes
     * then echo rank
     */
    public double[][] getWeightingData() {
        return weightingData;
    }
}
