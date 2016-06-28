/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

import java.io.File;

/**
 *
 * @author calcul
 */
public class GroundEnergyParams {
    
    private boolean calculateGroundEnergy;
    private File groundEnergyFile;
    private short groundEnergyFileFormat;
    public static final short FILE_FORMAT_PNG = 1;
    public static final short FILE_FORMAT_TXT = 0;

    public GroundEnergyParams() {
        groundEnergyFileFormat = FILE_FORMAT_TXT;
    }

    public boolean isCalculateGroundEnergy() {
        return calculateGroundEnergy;
    }

    public void setCalculateGroundEnergy(boolean calculateGroundEnergy) {
        this.calculateGroundEnergy = calculateGroundEnergy;
    }

    public File getGroundEnergyFile() {
        return groundEnergyFile;
    }

    public void setGroundEnergyFile(File groundEnergyFile) {
        this.groundEnergyFile = groundEnergyFile;
    }

    public short getGroundEnergyFileFormat() {
        return groundEnergyFileFormat;
    }

    public void setGroundEnergyFileFormat(short groundEnergyFileFormat) {
        this.groundEnergyFileFormat = groundEnergyFileFormat;
    }
    
}
