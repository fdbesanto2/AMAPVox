/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxreader;

import java.util.List;

/**
 *
 * @author calcul
 */
public class VoxelSpace {
    
    public VoxelSpaceInfos voxelSpaceInfos;
    public List<Voxel> voxels = null;
    public int[][] canopeeArray = null;

    public VoxelSpace(VoxelSpaceInfos voxelSpaceInfos) {
        this.voxelSpaceInfos = voxelSpaceInfos;
    }
    
    public int[][] getCanopeeArray(){
        
        if(canopeeArray == null){
            
            canopeeArray = new int[voxelSpaceInfos.getSplit().x][voxelSpaceInfos.getSplit().y];
        
            for(Voxel voxel : voxels){
                if (voxel.nbSampling > 0 && voxel.nbEchos > 0) {

                    if(voxel.$k > canopeeArray[voxel.$i][voxel.$j]){
                        canopeeArray[voxel.$i][voxel.$j] = voxel.$k;
                    }
                    break;
                }
            }
        }
        
        return canopeeArray;
    }
    
}
