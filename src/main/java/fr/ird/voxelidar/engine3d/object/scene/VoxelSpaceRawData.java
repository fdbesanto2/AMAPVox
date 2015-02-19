/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import fr.ird.voxelidar.io.file.FileManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien
 */
public class VoxelSpaceRawData {
            
    public Point3i split;
    public Point3d resolution;
    public Point3d bottomCorner;
    public Point3d topCorner;
    
    
    public ArrayList<RawVoxel> voxels;
    public ArrayList<String> attributsNames;
    
    public VoxelSpaceRawData(){
        
        voxels = new ArrayList<>();
        attributsNames = new ArrayList<>();
        
        split = new Point3i();
        resolution = new Point3d();
        bottomCorner = new Point3d();
        topCorner = new Point3d();
    }
    
    public VoxelSpaceRawData(VoxelSpaceRawData copy){
        
        voxels = new ArrayList<>();
        attributsNames = new ArrayList<>();
        
        split = copy.split;
        resolution = copy.resolution;
        bottomCorner = copy.bottomCorner;
        topCorner = copy.topCorner;
    }
    
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
    
    public void setVoxelsFromMap(Map<String, Float[]> voxelsMap){
        
        voxels = new ArrayList<>();
        attributsNames = new ArrayList<>();
                
        int count=0;
        for(Entry entry : voxelsMap.entrySet()){
            
            attributsNames.add((String) entry.getKey());
            Float[] attributs = (Float[]) entry.getValue();
            
            for(int i=0;i<attributs.length;i++){
                
                if(count == 0){
                    voxels.add(new Voxel(null, null, new Float[voxelsMap.size()], 1.0f));
                }
                
                voxels.get(i).attributs[count] = attributs[i];
            }
            
            count++;
        }
    }
    
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
    
    public static String[] readAttributs(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        if(header.split(" ").length == 10){
            
            return readAttributs1(f);
            
        }else{
            return readAttributs2(f);
        }
        
    }
    
    public static String[] readAttributs1(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        if(header.split(" ").length == 10){
            
            return FileManager.readHeader(f.getAbsolutePath()).split(" ");
        }
        
        return null;
        
    }
    
    public static String[] readAttributs2(File f){
        
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
            Logger.getLogger(VoxelSpaceData.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(VoxelSpaceData.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return header;
        
    }
}
