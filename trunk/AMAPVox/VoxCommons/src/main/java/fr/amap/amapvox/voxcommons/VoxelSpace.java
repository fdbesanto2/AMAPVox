/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxcommons;

import java.util.List;

/**
 *
 * @author calcul
 */
public class VoxelSpace{
    
    protected final VoxelSpaceInfos voxelSpaceInfos;
    public List voxels;

    public VoxelSpace(VoxelSpaceInfos voxelSpaceInfos) {
        this.voxelSpaceInfos = voxelSpaceInfos;
    }

    public VoxelSpaceInfos getVoxelSpaceInfos() {
        return voxelSpaceInfos;
    }
    
}
