/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.chart;

import fr.ird.voxelidar.engine3d.object.scene.RawVoxel;
import fr.ird.voxelidar.engine3d.object.scene.Voxel;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.util.Processing;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import javax.vecmath.Point2f;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author Julien
 */
public class ChartFactory extends Processing{
    
    public XYSeries generateChartWithFilters(VoxelSpace voxelSpace, String horizontalAxis, String verticalAxis, VoxelFilter filter){
        
        ArrayList<Voxel> voxels = voxelSpace.data.voxels;
        
        XYSeries data = new XYSeries("ALS");
        ArrayList<String> attributsNames = voxelSpace.data.attributsNames;
        
        int iterationsNumber = voxels.size();
        int step = iterationsNumber/10;
        
        int iterationIndex = 0;
        for (Voxel voxel : voxels) {
            
            if(iterationIndex % step == 0){
                fireProgress("", (int)(((iterationIndex/(float)iterationsNumber)*100)));
            }
            
            Float[] attributs = voxel.attributs;
            
            if(filter.doFilter(attributsNames, attributs)){
                
                float horizontal = attributs[attributsNames.indexOf(horizontalAxis)];
                float vertical = attributs[attributsNames.indexOf(verticalAxis)];
                
                data.add(vertical, horizontal);
            }
            
            iterationIndex++;
        }
        
        fireFinished();
        
        return data;
        
    }
    
    public XYSeries generateVegetationProfile(VoxelFilter filter, VoxelSpace voxelSpace){
        
        ArrayList<Voxel> voxels = voxelSpace.data.voxels;
        
        Map<String, Point2f> minMax = voxelSpace.data.getMinMax();
        Point2f get = minMax.get("dist");
        
        float minHeight = 0;
        float maxHeight;
        if(get == null){
            maxHeight = voxelSpace.data.maxY;
        }else{
            maxHeight = get.y;
        }
        
        XYSeries data = new XYSeries("ALS");
        
        
        //float minHeight = format.minY;
        //float maxHeight = format.maxY;
        
        float heightIntervall = (maxHeight - minHeight)/100.0f;
        
        //compute heights
        float[] heights = new float[100];
        
        float temp = minHeight;
        for(int i=0;i<heights.length;i++){
            
            heights[i] = temp + heightIntervall;
            temp = heights[i];
        }
        
        int iterationsNumber = heights.length;
        int step = iterationsNumber/10;
        
        for(int i=0;i<heights.length-1;i++){
            
            if(i % step == 0){
                fireProgress("", (int)(((i/(float)iterationsNumber)*100)));
            }
            
            float averagePAD = 0;
            int count = 0;
            ArrayList<String> attributsNames = voxelSpace.data.attributsNames;
            
            for (Voxel voxel : voxels) {
                
                
                Float[] attributs = voxel.attributs;
                
                float dist = 0;
                try{
                    dist = attributs[attributsNames.indexOf("dist")];
                }catch(Exception e){
                    //dist = voxel.position.y;
                }
                
                if(dist >= 0 && dist >= heights[i] && dist < heights[i+1] && filter.doFilter(attributsNames, attributs)){
                    
                    float pad = attributs[attributsNames.indexOf("PAD")];
                    
                    
                    if(dist>=0 && pad>=0){
                        averagePAD += pad;
                        count++;
                    }
                }
            }
            
            averagePAD/=count;
            
            if(!Float.isNaN(averagePAD)){
                data.add(heights[i], averagePAD);
            }
        }
        
        fireFinished();
        
        return data;
        
    }

    @Override
    public File process() {
        return null;
    }
}
