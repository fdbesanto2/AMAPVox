/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.voxelspace;

import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.math.point.Point2F;
import java.io.File;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Julien
 */
public class VoxelSpaceFormat {
    
    private Map<String, Point2F> minMax;
    private Map<String, Integer> attributs;
    public int xNumberVox, yNumberVox, zNumberVox;
    public float resolution;
    
    public ArrayList<Voxel> voxels;
    
    public VoxelSpaceFormat(){
        voxels = new ArrayList<>();
    }
    
    public Map<String, Integer> getAttributs() {
        return attributs;
    }

    public void setAttributs(Map<String, Integer> attributs) {
        this.attributs = attributs;
    }
    
    public static String[] readAttributs(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        if(header.split(" ").length == 10){
            
            return FileManager.readHeader(f.getAbsolutePath()).split(" ");
        }
        
        return null;
        
    }

    public Map<String, Point2F> getMinMax() {
        return minMax;
    }

    public void setMinMax(Map<String, Point2F> minMax) {
        this.minMax = minMax;
    }
}
