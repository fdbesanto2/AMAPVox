/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class VoxelSpaceInfos {
    
    private final static Logger logger = Logger.getLogger(VoxelSpaceInfos.class);
    
    private Point3d minCorner;
    private Point3d maxCorner;
    private Point3i split;
    private String type; //ALS ou TLS
    private float resolution;
    private float maxPAD;
    private String[] columnNames;
    
    public void readFromVoxelFile(File voxelFile){
        
        try (BufferedReader reader = new BufferedReader(new FileReader(voxelFile))){
            
            String identifier = reader.readLine();
            
            if(!identifier.equals("VOXEL SPACE")){
                logger.error("Voxel file is invalid, VOXEL SPACE identifier is missing");
                reader.close();
                return;
            }
            
            try{
                String minCornerLine = reader.readLine();
                String[] minCornerArray = minCornerLine.split(" ");
                minCorner = new Point3d(Double.valueOf(minCornerArray[1]), 
                                        Double.valueOf(minCornerArray[2]), 
                                        Double.valueOf(minCornerArray[3]));

                String maxCornerLine = reader.readLine();
                String[] maxCornerArray = maxCornerLine.split(" ");
                maxCorner = new Point3d(Double.valueOf(maxCornerArray[1]), 
                                        Double.valueOf(maxCornerArray[2]), 
                                        Double.valueOf(maxCornerArray[3]));

                String splitLine = reader.readLine();
                String[] splitArray = splitLine.split(" ");
                split = new Point3i(Integer.valueOf(splitArray[1]), 
                                        Integer.valueOf(splitArray[2]), 
                                        Integer.valueOf(splitArray[3]));

                String otherValuesLine = reader.readLine();
                String[] otherValuesArray = otherValuesLine.split(" ");
                
                type = otherValuesArray[1];
                resolution = Float.valueOf(otherValuesArray[3]);
                maxPAD = Float.valueOf(otherValuesArray[5]);
                
                
            }catch(IOException | NumberFormatException e){
                
                logger.error("Header is invalid",e);
                reader.close();
            }
            
            columnNames = reader.readLine().split(" ");
            
            reader.close();
            
        } catch (FileNotFoundException ex) {
            logger.error("Cannot find voxel file",ex);
        } catch (IOException ex) {
            logger.error("Cannot read voxel file",ex);
        }
    }

    public Point3d getMinCorner() {
        return minCorner;
    }

    public Point3d getMaxCorner() {
        return maxCorner;
    }

    public Point3i getSplit() {
        return split;
    }

    public String getType() {
        return type;
    }

    public float getResolution() {
        return resolution;
    }

    public float getMaxPAD() {
        return maxPAD;
    }

    public String[] getColumnNames() {
        return columnNames;
    }
}
