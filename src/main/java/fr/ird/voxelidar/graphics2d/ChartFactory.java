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
import java.util.Map;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author Julien
 */
public class ChartFactory {
    
    public static XYSeries generateChartWithFilters(VoxelSpaceFormat format, String horizontalAxis, String verticalAxis, VoxelFilter filter){
        
        ArrayList<Voxel> voxels = format.voxels;
        
        XYSeries data = new XYSeries("ALS");
        for (Voxel voxel : voxels) {

            Map<String, Float> attributs = voxel.getAttributs();
            
            if(filter.doFilter(attributs)){
                
                float horizontal = attributs.get(horizontalAxis);
                float vertical = attributs.get(verticalAxis);
                
                data.add(vertical, horizontal);
            }
        }
        
        return data;
        
    }
}
