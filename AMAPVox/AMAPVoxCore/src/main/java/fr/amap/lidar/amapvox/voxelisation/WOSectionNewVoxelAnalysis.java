package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;

public class WOSectionNewVoxelAnalysis extends NewVoxelAnalysis {

    public WOSectionNewVoxelAnalysis(Raster terrain, VoxelAnalysisCfg cfg) throws Exception {
        super(terrain, cfg, false);
    }

}