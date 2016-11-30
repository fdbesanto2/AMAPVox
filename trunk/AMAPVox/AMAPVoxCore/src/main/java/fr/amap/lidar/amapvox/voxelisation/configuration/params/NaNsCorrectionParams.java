/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

/**
 *
 * @author calcul
 */
public class NaNsCorrectionParams {
    
    private boolean activate;
    private float nbSamplingThreshold;

    public NaNsCorrectionParams(boolean activate) {
        this.activate = activate;
    }

    public NaNsCorrectionParams(float nbSamplingThreshold) {
        this.activate = true;
        this.nbSamplingThreshold = nbSamplingThreshold;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public float getNbSamplingThreshold() {
        return nbSamplingThreshold;
    }

    public void setNbSamplingThreshold(float nbSamplingThreshold) {
        this.nbSamplingThreshold = nbSamplingThreshold;
    }
    
}
