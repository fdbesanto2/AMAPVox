/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import java.util.ArrayList;


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
    
    public VoxelObject getVoxel(int i, int j, int k){
        
        if(i > voxelSpaceInfos.getSplit().x -1 || j > voxelSpaceInfos.getSplit().y -1 || k > voxelSpaceInfos.getSplit().z -1){
            return null;
        }
        
        int index = get1DFrom3D(i, j, k);
        
        if(index>voxels.size()-1){
            return null;
        }
        
        return (VoxelObject) voxels.get(index);
    }    
    
}
