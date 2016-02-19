/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.vegetation;

/**
 *
 * @author calcul
 */
public class LADParams {
    
    private int ladEstimationMode;
    private LeafAngleDistribution.Type ladType;
    private float ladBetaFunctionAlphaParameter;
    private float ladBetaFunctionBetaParameter;

    public LADParams() {
        ladType = LeafAngleDistribution.Type.SPHERIC;
        ladEstimationMode = 0;
    }

    public int getLadEstimationMode() {
        return ladEstimationMode;
    }

    /**
     * 0 for uniform LAD attribution, 1 for special (not available)
     * @param ladEstimationMode
     */
    public void setLadEstimationMode(int ladEstimationMode) {
        this.ladEstimationMode = ladEstimationMode;
    }

    public LeafAngleDistribution.Type getLadType() {
        return ladType;
    }

    public void setLadType(LeafAngleDistribution.Type ladType) {
        this.ladType = ladType;
    }

    public float getLadBetaFunctionAlphaParameter() {
        return ladBetaFunctionAlphaParameter;
    }

    public void setLadBetaFunctionAlphaParameter(float ladBetaFunctionAlphaParameter) {
        this.ladBetaFunctionAlphaParameter = ladBetaFunctionAlphaParameter;
    }

    public float getLadBetaFunctionBetaParameter() {
        return ladBetaFunctionBetaParameter;
    }

    public void setLadBetaFunctionBetaParameter(float ladBetaFunctionBetaParameter) {
        this.ladBetaFunctionBetaParameter = ladBetaFunctionBetaParameter;
    }
    
}
