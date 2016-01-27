/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.datastructure.voxel;

import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.point.Point3I;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.vecmath.Point2f;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceData{
    
    private Map<String, Point2f> minMax;
    
    public float minY, maxY;
    
    public ArrayList<VoxelObject> voxels;
    //public Type type;
    
    //public float res;
    //public float maxPad = 5.0f;
    
    public VoxelSpaceHeader header;
    /*
    public Point3i split;
    public Point3d resolution;
    public Point3d bottomCorner;
    public Point3d topCorner;
    
    public ArrayList<String> attributsNames;
    */
   
    
    public enum Type{
        ALS(1),
        TLS(2);
        
        private final int type;
        Type(int type){
            this.type = type;
        }
    }
    
    public VoxelSpaceData(){
        
        voxels = new ArrayList<>();
        /*
        attributsNames = new ArrayList<>();
        
        split = new Point3i();
        resolution = new Point3d();
        bottomCorner = new Point3d();
        topCorner = new Point3d();
        */
        header = new VoxelSpaceHeader();
        minMax = new HashMap<>();
        
    }

    public Map<String, Point2f> getMinMax() {
        return minMax;
    }

    public void setMinMax(Map<String, Point2f> minMax) {
        this.minMax = minMax;
    }
    
    public Map<String, Float[]> getVoxelMap() {
        
        Map<String, Float[]> voxelMap = new LinkedHashMap<>();
        
        for (String attribut : header.attributsNames) {
            
            voxelMap.put(attribut, new Float[voxels.size()]);
        }
        
        for (int j=0;j<voxels.size();j++) {
            
            float[] attributsValues = ((VoxelObject)voxels.get(j)).attributs;
            
            for(int i=0;i<attributsValues.length;i++){
                voxelMap.get(header.attributsNames.get(i))[j] = attributsValues[i];
            }
        }
        
        return voxelMap;
    }
    
    public VoxelObject getVoxel(int indice){
        return voxels.get(indice);
    }
    
    public VoxelObject getVoxel(int i, int j, int k){
        
        int index = get1DFrom3D(i, j, k);
        
        if(index>voxels.size()-1){
            return null;
        }
        
        return voxels.get(index);
    }
    
    private int get1DFrom3D(int i, int j, int k){
        return (i*header.split.y*header.split.z) + (j*header.split.z) +  k;
    }
    
    public VoxelObject getLastVoxel(){
        
        if(voxels != null && !voxels.isEmpty()){
            return voxels.get(voxels.size()-1);
        }
        
        return null;
    }
    
    public VoxelObject getFirstVoxel(){
        
        if(voxels != null && !voxels.isEmpty()){
            return voxels.get(0);
        }
        
        return null;
    }
    
    public Point3I getIndicesFromPoint(float x, float y ,float z){
        
        // shift to scene Min
        Point3F pt =new Point3F (x, y, z);
        pt.x -= header.bottomCorner.x;
        pt.y -= header.bottomCorner.y;
        pt.z -= header.bottomCorner.z;

        if ((pt.z < 0) || (pt.z >= header.split.z)){
            return null;
        }
        if ((pt.x < 0) || (pt.x >= header.split.x)){
            return null;
        }
        if ((pt.y < 0) || (pt.y >= header.split.y)){
            return null;
        }
        pt.x /= header.res;
        pt.y /= header.res;
        pt.z /= header.res;

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

    
    public static String[] readAttributs(File f) throws FileNotFoundException, IOException{
        
        String[] header = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(f));
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            reader.readLine();
            header = reader.readLine().split(" ");
            
            
            reader.close();
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        return header;
        
    }
    
    public void calculateAttributsLimits(){
        
        
        for(int i=0;i<header.attributsNames.size();i++){
            
            float min, max;
            
            if(!voxels.isEmpty()){
                min = ((VoxelObject)voxels.get(0)).attributs[i];
                max = ((VoxelObject)voxels.get(0)).attributs[i];
            }else{
                return;
            }
            
            for(int j=1;j<voxels.size();j++){
                
                if(min > ((VoxelObject)voxels.get(j)).attributs[i]){
                    min = ((VoxelObject)voxels.get(j)).attributs[i];
                }
                
                
                if(max < ((VoxelObject)voxels.get(j)).attributs[i]){
                    max = ((VoxelObject)voxels.get(j)).attributs[i];
                }

            }
            
            minMax.put(header.attributsNames.get(i), new Point2f(min, max));
        }
        
        
    }
}
