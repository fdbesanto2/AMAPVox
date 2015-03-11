/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
    
    @Override
    public Map<String, Float[]> getVoxelMap() {
        
        Map<String, Float[]> voxelMap = new LinkedHashMap<>();
        
        for (String attribut : attributsNames) {
            
            voxelMap.put(attribut, new Float[voxels.size()]);
        }
        
        for (int j=0;j<voxels.size();j++) {
            
            Float[] attributsValues = voxels.get(j).attributs;
            
            for(int i=0;i<attributsValues.length;i++){
                voxelMap.get(attributsNames.get(i))[j] = attributsValues[i];
            }
        }
        
        return voxelMap;
    }
    
    @Override
    public void write(File outputFile){
        
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            writer.write("VOXEL SPACE"+"\n");
            writer.write("#min_corner: "+bottomCorner.x+" "+bottomCorner.y+" "+bottomCorner.z+"\n");
            writer.write("#max_corner: "+topCorner.x+" "+topCorner.y+" "+topCorner.z+"\n");
            writer.write("#split: "+split.x+" "+split.y+" "+split.z+"\n");

            writer.write("#offset: "+(float)bottomCorner.x+" "+(float)bottomCorner.y+" "+(float)bottomCorner.z+"\n");
            
            String header = "";
            for (String attributsName : attributsNames) {
                header += attributsName + " ";
            }
            header = header.trim();
            
            writer.write(header+"\n");
            
            for (RawVoxel voxel : voxels) {
                
                //writer.write(voxel.indice.x + " " + voxel.indice.y + " " + voxel.indice.z);
                
                String attributsLine = "";
                
                for (int i=0;i<voxel.attributs.length;i++) {
                    
                    if(i<3){
                        
                        attributsLine += voxel.attributs[i].intValue() + " ";
                    }else{
                        attributsLine += voxel.attributs[i] + " ";
                    }
                }
                writer.write(attributsLine.trim()+"\n");
            }
            
            writer.close();
            
        } catch (FileNotFoundException e) {
            
        }catch (Exception e) {
            
        }
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
