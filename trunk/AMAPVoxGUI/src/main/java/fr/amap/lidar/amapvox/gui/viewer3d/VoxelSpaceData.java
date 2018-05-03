/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import java.util.ArrayList;
import java.util.List;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceData extends fr.amap.lidar.amapvox.commons.VoxelSpace{

    public VoxelSpaceData(VoxelSpaceInfos voxelSpaceInfos) {
        
        super(voxelSpaceInfos);
        
        voxels = new ArrayList<>();
    }
    
    public VoxelObject getLastVoxel(){
        
        if(voxels != null && !voxels.isEmpty()){
            return (VoxelObject) voxels.get(voxels.size()-1);
        }
        
        return null;
    }
    
    public VoxelObject getFirstVoxel(){
        
        if(voxels != null && !voxels.isEmpty()){
            return (VoxelObject) voxels.get(0);
        }
        
        return null;
    }
    
    @Override
    public VoxelObject getVoxel(int i, int j, int k){
        
        return (VoxelObject) super.getVoxel(i, j, k);
    }
    
}
