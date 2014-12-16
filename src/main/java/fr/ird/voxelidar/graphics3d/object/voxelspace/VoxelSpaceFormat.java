/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.voxelspace;

import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.math.point.Point2F;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

/**
 *
 * @author Julien
 */
public class VoxelSpaceFormat {
    
    private int xIndex, yIndex, zIndex;
    private Map<String, Point2F> minMax;
    private Map<String, Integer> attributs;

    public Map<String, Integer> getAttributs() {
        return attributs;
    }

    public void setAttributs(Map<String, Integer> attributs) {
        this.attributs = attributs;
    }
    
    public static String[] readAttributs(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        
        if(header.equals("VOXEL SPACE")){
            
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(f));

                reader.readLine();
                reader.readLine();
                reader.readLine();
                reader.readLine();

                return reader.readLine().split("\t");

            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(VoxelSpace.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(VoxelSpace.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }else if(header.split(" ").length == 10){
            
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

    public int getxIndex() {
        return xIndex;
    }

    public void setxIndex(int xIndex) {
        this.xIndex = xIndex;
    }

    public int getyIndex() {
        return yIndex;
    }

    public void setyIndex(int yIndex) {
        this.yIndex = yIndex;
    }

    public int getzIndex() {
        return zIndex;
    }

    public void setzIndex(int zIndex) {
        this.zIndex = zIndex;
    }
}
