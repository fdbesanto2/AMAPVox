/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.chart;

import fr.amap.amapvox.voxreader.VoxelFileReader;
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

    public VoxelFileChart(File file, String label) {
        this.file = file;
        this.label = label;
        this.loaded = false;
    }

    @Override
    public String toString(){
        return file.toString();
    }
}