/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.chart;

import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.io.File;

/**
 *
 * @author calcul
 */
public class VoxelFileChart{
        
    public File file;
    public VoxelFileReader reader;
    public String label;
    public boolean loaded;
    
    private SeriesParameters seriesParameters;

    public VoxelFileChart(File file, String label) {
        this.file = file;
        this.label = label;
        this.loaded = false;
        this.seriesParameters = new SeriesParameters(label);
    }

    public SeriesParameters getSeriesParameters() {
        return seriesParameters;
    }

    @Override
    public String toString(){
        return file.toString();
    }
}