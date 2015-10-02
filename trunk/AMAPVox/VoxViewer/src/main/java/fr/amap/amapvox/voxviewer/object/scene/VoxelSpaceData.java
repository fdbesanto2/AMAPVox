/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.point.Point3I;
import fr.amap.amapvox.voxcommons.VoxelSpaceInfos;
import java.util.ArrayList;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceData extends fr.amap.amapvox.voxcommons.VoxelSpace{

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
    
    public Point3I getIndicesFromPoint(float x, float y ,float z){
        
        // shift to scene Min
        Point3F pt =new Point3F (x, y, z);
        pt.x -= voxelSpaceInfos.getMinCorner().x;
        pt.y -= voxelSpaceInfos.getMinCorner().y;
        pt.z -= voxelSpaceInfos.getMinCorner().z;

        if ((pt.z < 0) || (pt.z >= voxelSpaceInfos.getSplit().z)){
            return null;
        }
        if ((pt.x < 0) || (pt.x >= voxelSpaceInfos.getSplit().x)){
            return null;
        }
        if ((pt.y < 0) || (pt.y >= voxelSpaceInfos.getSplit().y)){
            return null;
        }
        pt.x /= voxelSpaceInfos.getResolution();
        pt.y /= voxelSpaceInfos.getResolution();
        pt.z /= voxelSpaceInfos.getResolution();

        Point3I indices = new Point3I();
        
        indices.x = (int)pt.x;
        indices.y = (int)pt.y;
        indices.z = (int)pt.z;
        /*
        indices.x = (int) Math.floor ((double) (pt.x % header.split.x)); if (indices.x<0) indices.x += header.split.x;
        indices.y = (int) Math.floor ((double) (pt.y % header.split.y)); if (indices.y<0) indices.y += header.split.y;
        indices.z = (int) Math.min (pt.z, header.split.z-1);*/
                
        return indices;
    }
    
    
}
