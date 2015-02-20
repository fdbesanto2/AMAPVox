/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Point2f;


/**
 *
 * @author Julien
 */
public class VoxelSpaceData extends VoxelSpaceRawData{
    
    private Map<String, Point2f> minMax;
    
    public float minY, maxY;
    
    public ArrayList<Voxel> voxels;
    
    public VoxelSpaceData(){
        
        super();
        
        voxels = new ArrayList<>();
        minMax = new HashMap<>();
    } 
    
    public float getVoxelValue(String attributName, int index){
        
        return voxels.get(index).attributs[attributsNames.indexOf(attributName)];
    }

    public Map<String, Point2f> getMinMax() {
        return minMax;
    }

    public void setMinMax(Map<String, Point2f> minMax) {
        this.minMax = minMax;
    }
    
    public void calculateAttributsLimits(){
        
        
        for(int i=0;i<attributsNames.size();i++){
            
            float min, max;
            
            if(!voxels.isEmpty()){
                min = voxels.get(0).attributs[i];
                max = voxels.get(0).attributs[i];
            }else{
                return;
            }
            
            for(int j=1;j<voxels.size();j++){
                
                if(min > voxels.get(j).attributs[i]){
                    min = voxels.get(j).attributs[i];
                }
                
                
                if(max < voxels.get(j).attributs[i]){
                    max = voxels.get(j).attributs[i];
                }

            }
            
            minMax.put(attributsNames.get(i), new Point2f(min, max));
        }
        
        
    }
}
