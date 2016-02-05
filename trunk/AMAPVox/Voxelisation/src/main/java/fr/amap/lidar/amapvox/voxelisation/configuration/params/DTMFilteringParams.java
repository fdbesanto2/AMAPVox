/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

import java.io.File;

/**
 * DTM filtering parameters. A dtm filter doesn't take into account lidar echoes'shot
 * who are below the ground level, plus an offset.
 */
public class DTMFilteringParams {

    private boolean activate;
    private File dtmFile;
    private float minDTMDistance = 1;

    public DTMFilteringParams() {
        this.activate = false;
        this.minDTMDistance = 1;
    }
    
    public DTMFilteringParams(File dtmFile, float minDTMDistance) {
     
        this.activate = true;
        this.dtmFile = dtmFile;
        this.minDTMDistance = minDTMDistance;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public boolean useDTMCorrection() {
        return activate;
    }

    public File getDtmFile() {
        return dtmFile;
    }

    public void setDtmFile(File dtmFile) {
        this.dtmFile = dtmFile;
    }

    public float getMinDTMDistance() {
        return minDTMDistance;
    }

    public void setMinDTMDistance(float minDTMDistance) {
        this.minDTMDistance = minDTMDistance;
    }
    
}
