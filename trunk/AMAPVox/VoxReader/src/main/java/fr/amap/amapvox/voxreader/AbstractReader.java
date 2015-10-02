/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxreader;

import fr.amap.amapvox.voxcommons.VoxelSpace;
import fr.amap.amapvox.voxcommons.VoxelSpaceInfos;
import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;

/**
 *
 * @author calcul
 */
public abstract class AbstractReader{

    public VoxelSpace voxelSpace = null;
    public File voxelFile = null;
    public BufferedReader reader = null;
    public String currentLine = null;
    public boolean keepInMemory = false;
    public int currentVoxelIndex = 0;
    public boolean wasRead = false;
    
    public AbstractReader(File voxelFile, boolean keepInMemory) throws Exception{
        
        this.voxelFile = voxelFile;
        this.keepInMemory = keepInMemory;
        
        VoxelSpaceInfos voxelSpaceInfos= new VoxelSpaceInfos();
        voxelSpace = new VoxelSpace(voxelSpaceInfos);
        
        voxelSpaceInfos.readFromVoxelFile(voxelFile);
        
        if(keepInMemory){
            voxelSpace.voxels = new ArrayList<>();
        }
    }
    
    public AbstractReader(File voxelFile) throws Exception{
        
        this.voxelFile = voxelFile;
        this.keepInMemory = true;
        
        VoxelSpaceInfos voxelSpaceInfos= new VoxelSpaceInfos();
        voxelSpace = new VoxelSpace(voxelSpaceInfos);
        
        voxelSpaceInfos.readFromVoxelFile(voxelFile);
        
        voxelSpace.voxels = new ArrayList<>();
    }
    
    public File getVoxelFile() {
        return voxelFile;
    }

    public VoxelSpaceInfos getVoxelSpaceInfos() {
        return voxelSpace.getVoxelSpaceInfos();
    }
 
}
