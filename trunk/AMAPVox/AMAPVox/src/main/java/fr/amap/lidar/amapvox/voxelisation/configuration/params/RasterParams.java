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
public class RasterParams {
    
    
    private boolean generateMultiBandRaster;
    private boolean shortcutVoxelFileWriting;
    private int rasterResolution; //en pixels
    private float rasterStartingHeight;
    private float rasterHeightStep;
    private int rasterBandNumber;

    public RasterParams() {
        generateMultiBandRaster = false;
    }
    
    public boolean isGenerateMultiBandRaster() {
        return generateMultiBandRaster;
    }

    public void setGenerateMultiBandRaster(boolean generateMultiBandRaster) {
        this.generateMultiBandRaster = generateMultiBandRaster;
    }

    public boolean isShortcutVoxelFileWriting() {
        return shortcutVoxelFileWriting;
    }

    public void setShortcutVoxelFileWriting(boolean shortcutVoxelFileWriting) {
        this.shortcutVoxelFileWriting = shortcutVoxelFileWriting;
    }

    public int getRasterResolution() {
        return rasterResolution;
    }

    public void setRasterResolution(int rasterResolution) {
        this.rasterResolution = rasterResolution;
    }

    public float getRasterStartingHeight() {
        return rasterStartingHeight;
    }

    public void setRasterStartingHeight(float rasterStartingHeight) {
        this.rasterStartingHeight = rasterStartingHeight;
    }

    public float getRasterHeightStep() {
        return rasterHeightStep;
    }

    public void setRasterHeightStep(float rasterHeightStep) {
        this.rasterHeightStep = rasterHeightStep;
    }

    public int getRasterBandNumber() {
        return rasterBandNumber;
    }

    public void setRasterBandNumber(int rasterBandNumber) {
        this.rasterBandNumber = rasterBandNumber;
    }
    
}
