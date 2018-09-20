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

    public final static EchoesWeightByRankParams DEFAULT_ALS_WEIGHTING
            = new EchoesWeightByRankParams(new double[][]{
        {1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.62d, 0.38d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.40d, 0.35d, 0.25d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.28d, 0.29d, 0.24d, 0.19d, Double.NaN, Double.NaN, Double.NaN},
        {0.21d, 0.24d, 0.21d, 0.19d, 0.15d, Double.NaN, Double.NaN},
        {0.16d, 0.21d, 0.19d, 0.18d, 0.14d, 0.12d, Double.NaN},
        {0.15d, 0.17d, 0.15d, 0.16d, 0.12d, 0.19d, 0.06d}});

    public final static EchoesWeightByRankParams DEFAULT_TLS_WEIGHTING
            = new EchoesWeightByRankParams(new double[][]{
        {1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.5d, 0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {1 / 3.d, 1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
        {0.25d, 0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN, Double.NaN},
        {0.2d, 0.2d, 0.2d, 0.2d, 0.2d, Double.NaN, Double.NaN},
        {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, Double.NaN},
        {1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d}});

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
