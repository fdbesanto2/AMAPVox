/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceData{
    
    private Map<String, Point2f> minMax;
    
    public float minY, maxY;
    
    public ArrayList<Voxel> voxels;
    //public Type type;
    
    //public float res;
    //public float maxPad = 5.0f;
    
    private final static Logger logger = Logger.getLogger(VoxelSpaceData.class);
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
    
    public Voxel getVoxel(int indice){
        return voxels.get(indice);
    }
    
    public Voxel getVoxel(int i, int j, int k){
        
        int index = get1DFrom3D(i, j, k);
        
        if(index>voxels.size()-1){
            return null;
        }
        
        return voxels.get(index);
    }
    
    private int get1DFrom3D(int i, int j, int k){
        return (i*header.split.y*header.split.z) + (j*header.split.z) +  k;
    }
    
    public Voxel getLastVoxel(){
        
        if(voxels != null && !voxels.isEmpty()){
            return voxels.get(voxels.size()-1);
        }
        
        return null;
    }
    
    public Voxel getFirstVoxel(){
        
        if(voxels != null && !voxels.isEmpty()){
            return voxels.get(0);
        }
        
        return null;
    }

    
    public static String[] readAttributs(File f){
        
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
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
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
