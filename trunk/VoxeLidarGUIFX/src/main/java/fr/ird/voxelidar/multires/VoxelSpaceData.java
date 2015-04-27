/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.multires;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceData{
    
    public enum Type{
        ALS(1),
        TLS(2);
        
        private final int type;
        Type(int type){
            this.type = type;
        }
    }
    
    private Map<String, Point2f> minMax;
    
    public float minY, maxY;
    public Point3i split;
    public Point3d resolution;
    public float res;
    public Point3d bottomCorner;
    public Point3d topCorner;
    public Type type;
    
    public float maxPad = 5.0f;
    
    public ArrayList<Voxel> voxels;
    
    public ArrayList<String> attributsNames;
    
    public VoxelSpaceData(){
        
        voxels = new ArrayList<>();
        minMax = new HashMap<>();
        attributsNames = new ArrayList<>();
        split = new Point3i();
        resolution = new Point3d();
        bottomCorner = new Point3d();
        topCorner = new Point3d();
    }

    public Map<String, Point2f> getMinMax() {
        return minMax;
    }

    public void setMinMax(Map<String, Point2f> minMax) {
        this.minMax = minMax;
    }
    
    public Voxel getVoxel(int i, int j, int k){
        
        int index = get1DFrom3D(i, j, k);
        
        if(index>voxels.size()-1){
            return null;
        }
        
        return voxels.get(index);
    }
    
    public void setVoxel(int i, int j, int k, Voxel voxel){
        
        int index = get1DFrom3D(i, j, k);
        
        if(index>voxels.size()-1){
            
        }else{
            voxels.set(index, voxel);
        }
    }
    
    private int get1DFrom3D(int i, int j, int k){
        
        return (i*split.y*split.z) + (j*split.z) +  k;
    }
    
    /*
    @Override
    public Map<String, Float[]> getVoxelMap() {
        
        Map<String, Float[]> voxelMap = new LinkedHashMap<>();
        
        for (String attribut : attributsNames) {
            
            voxelMap.put(attribut, new Float[voxels.size()]);
        }
        
        for (int j=0;j<voxels.size();j++) {
            
            float[] attributsValues = voxels.get(j).attributs;
            
            for(int i=0;i<attributsValues.length;i++){
                voxelMap.get(attributsNames.get(i))[j] = attributsValues[i];
            }
        }
        
        return voxelMap;
    }
    */
    /*
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
                        
                        attributsLine += (int)voxel.attributs[i] + " ";
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
    */
    /*
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
    */
}
