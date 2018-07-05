/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.postproc;

import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpace;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien Heurtebize
 */
public class VoxelSpaceUtil {
    
     public static VoxelSpace fitVoxelSpaceToContent(File voxelFile) throws Exception{
        
        VoxelFileReader reader = new VoxelFileReader(voxelFile, true);
                
        Iterator<Voxel> iterator = reader.iterator();
        while(iterator.hasNext()){
            iterator.next();
        }
        
        return fitVoxelSpaceToContent(reader.voxelSpace);
    }
    
    public static VoxelSpace fitVoxelSpaceToContent(VoxelSpace voxelSpace){
        
        VoxelSpaceInfos infos = voxelSpace.getVoxelSpaceInfos();
        
        
        //i first side
        int i;
        for (i = 0; i < infos.getSplit().x; i++) {

            Voxel[][] layer = voxelSpace.getLayerX(i);
            
            
            int nbEmptyVoxels = 0;
            for(int j=0;j<layer.length;j++){
                for(int k=0;k<layer[0].length;k++){
                    if(layer[j][k].nbEchos > 0){
                        j = layer.length-1;
                        k = layer[0].length-1;
                    }else{
                        nbEmptyVoxels++;
                    }
                }
            }
            
            if(nbEmptyVoxels < layer.length * layer[0].length){
                break;
            }
        }
        
        int iMin = i;
        
        //i second side
        for (i = infos.getSplit().x-1; i >= 0; i--) {

            Voxel[][] layer = voxelSpace.getLayerX(i);
            
            int nbEmptyVoxels = 0;
            for(int j=0;j<layer.length;j++){
                for(int k=0;k<layer[0].length;k++){
                    if(layer[j][k].nbEchos > 0){
                        j = layer.length-1;
                        k = layer[0].length-1;
                    }else{
                        nbEmptyVoxels++;
                    }
                }
            }
            
            if(nbEmptyVoxels < layer.length * layer[0].length){
                break;
            }
        }
        
        int iMax = i;
        
        //j first side
        int j;
        for (j = 0; j < infos.getSplit().y; j++) {

            Voxel[][] layer = voxelSpace.getLayerY(j);
            
            
            int nbEmptyVoxels = 0;
            for(i=0;i<layer.length;i++){
                for(int k=0;k<layer[0].length;k++){
                    if(layer[i][k].nbEchos > 0){
                        i = layer.length-1;
                        k = layer[0].length-1;
                    }else{
                        nbEmptyVoxels++;
                    }
                }
            }
            
            if(nbEmptyVoxels < layer.length * layer[0].length){
                break;
            }
        }
        
        int jMin = j;
        
        //j second side
        for (j = infos.getSplit().y-1; j >= 0; j--) {

            Voxel[][] layer = voxelSpace.getLayerY(j);
            
            int nbEmptyVoxels = 0;
            for(i=0;i<layer.length;i++){
                for(int k=0;k<layer[0].length;k++){
                    if(layer[i][k].nbEchos > 0){
                        i = layer.length-1;
                        k = layer[0].length-1;
                    }else{
                        nbEmptyVoxels++;
                    }
                }
            }
            
            if(nbEmptyVoxels < layer.length * layer[0].length){
                break;
            }
        }
        
        int jMax = j;
        
        //k first side
        int k;
        for (k = 0; k < infos.getSplit().z; k++) {

            Voxel[][] layer = voxelSpace.getLayerZ(k);
            
            
            int nbEmptyVoxels = 0;
            for(i=0;i<layer.length;i++){
                for(j=0;j<layer[0].length;j++){
                    if(layer[i][j].nbEchos > 0){
                        i = layer.length-1;
                        j = layer[0].length-1;
                    }else{
                        nbEmptyVoxels++;
                    }
                }
            }
            
            if(nbEmptyVoxels < layer.length * layer[0].length){
                break;
            }
        }
        
        int kMin = k;
        
        //k second side
        for (k = infos.getSplit().z-1; k >= 0; k--) {

            Voxel[][] layer = voxelSpace.getLayerZ(k);
            
            int nbEmptyVoxels = 0;
            for(i=0;i<layer.length;i++){
                for(j=0;j<layer[0].length;j++){
                    if(layer[i][j].nbEchos > 0){
                        i = layer.length-1;
                        j = layer[0].length-1;
                    }else{
                        nbEmptyVoxels++;
                    }
                }
            }
            
            if(nbEmptyVoxels < layer.length * layer[0].length){
                break;
            }
        }
        
        int kMax = k;
        
        
        return cropVoxelSpace(voxelSpace, iMin, iMax, jMin, jMax, kMin, kMax);
    }
    
    public static VoxelSpace cropVoxelSpace(VoxelSpace voxelSpace, int iMin, int iMax, int jMin, int jMax, int kMin, int kMax){
        
        VoxelSpaceInfos infos = voxelSpace.getVoxelSpaceInfos();
        
        int iSplit = iMax - iMin + 1;
        int jSplit = jMax - jMin + 1;
        int kSplit = kMax - kMin + 1;

        infos.setMinCorner(new Point3d(
                infos.getMinCorner().x + iMin * infos.getResolution(),
                infos.getMinCorner().y + jMin * infos.getResolution(),
                infos.getMinCorner().z + kMin * infos.getResolution()));

        infos.setMaxCorner(new Point3d(
                infos.getMaxCorner().x - ((infos.getSplit().x - iMax - 1)*infos.getResolution()),
                infos.getMaxCorner().y - ((infos.getSplit().y - jMax - 1)*infos.getResolution()),
                infos.getMaxCorner().z - ((infos.getSplit().z - kMax - 1)*infos.getResolution())));
        
        
        infos.setSplit(new Point3i(iSplit, jSplit, kSplit));
        
        VoxelSpace vs = new VoxelSpace(infos);
        vs.voxels = new ArrayList();
        
        for(int i=0;i<voxelSpace.voxels.size();i++){
            
            Voxel voxel = (Voxel) voxelSpace.voxels.get(i);
            
            if(voxel.i >= iMin && voxel.i <= iMax && 
                    voxel.j >= jMin && voxel.j <= jMax && 
                    voxel.k >= kMin && voxel.k <= kMax){

                    voxel.i -= iMin;
                    voxel.j -= jMin;
                    voxel.k -= kMin;
                    
                    vs.voxels.add(voxel);
            }
        }
        
        return vs;
    }
}
