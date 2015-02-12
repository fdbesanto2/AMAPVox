/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics2d;

import fr.ird.voxelidar.lidar.format.voxelspace.Voxel;
import fr.ird.voxelidar.lidar.format.voxelspace.VoxelSpaceFormat;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.VoxelFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author Julien
 */
public class VegetationProfile {
    
    
    public VegetationProfile(){
        
        
    }
    
    public static XYSeries getData(float height, VoxelFilter filter, VoxelSpaceFormat format){
        
        ArrayList<Voxel> voxels = format.voxels;
        
        XYSeries data = new XYSeries("ALS");
        
        float minHeight = format.minY;
        float maxHeight = format.maxY;
        
        int heightIntervall = (int) (maxHeight - minHeight);
        
        //compute heights
        float[] heights = new float[heightIntervall];
        float temp = minHeight;
        for(int i=0;i<heights.length;i++){
            
            heights[i] = temp + ((maxHeight - minHeight)/heightIntervall);
            temp = heights[i];
        }
        
        for(int i=0;i<heights.length;i++){
            
            float averagePAD = 0;
            int count = 0;
            
            for (Voxel voxel : voxels) {
                
                Map<String, Float> attributs = voxel.getAttributs();
                float dist;
                try{
                    dist = attributs.get("dist");
                }catch(Exception e){
                    dist = voxel.y;
                }
                
                if(dist >= heights[i]-1 && dist <= heights[i]+1 && filter.doFilter(attributs)){
                    
                    float pad = attributs.get("PAD2");
                    
                    
                    if(dist>=0 && pad>=0){
                        averagePAD += pad;
                        count++;
                    }
                }
            }
            
            averagePAD/=count;
            
            data.add(heights[i], averagePAD);
        }
        
        return data;
        
    }
}
