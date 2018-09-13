/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;

/**
 *
 * @author pverley
 */
public class WOSectionCurrentVoxelAnalysis extends CurrentVoxelAnalysis {
    
    public WOSectionCurrentVoxelAnalysis(Raster terrain, VoxelAnalysisCfg cfg) throws Exception {
        super(terrain, cfg, false);
    }
    
}
