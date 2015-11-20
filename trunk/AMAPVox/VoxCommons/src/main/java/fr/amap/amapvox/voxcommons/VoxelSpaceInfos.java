package fr.amap.amapvox.voxcommons;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


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
    
    public void readFromVoxelFile(File voxelFile) throws Exception{
        
        try (BufferedReader reader = new BufferedReader(new FileReader(voxelFile))){
            
            String identifier = reader.readLine();
            
            if(!identifier.equals("VOXEL SPACE")){
                reader.close();
                throw new Exception("Voxel file is invalid, VOXEL SPACE identifier is missing");
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
                
                reader.close();
                throw new Exception("Header is invalid",e);
            }
            
            columnNames = reader.readLine().split(" ");
            
            reader.close();
            
        } catch (FileNotFoundException ex) {
            throw new Exception("Cannot find voxel file",ex);
        } catch (IOException ex) {
            throw new Exception("Cannot read voxel file",ex);
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

    public void setMinCorner(Point3d minCorner) {
        this.minCorner = minCorner;
    }

    public void setMaxCorner(Point3d maxCorner) {
        this.maxCorner = maxCorner;
    }

    public void setSplit(Point3i split) {
        this.split = split;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setResolution(float resolution) {
        this.resolution = resolution;
    }

    public void setMaxPAD(float maxPAD) {
        this.maxPAD = maxPAD;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }
    
    @Override
    public String toString(){
        
        String columns = "";
        for(String column : columnNames){
            columns += column + " ";
        }
        
        columns = columns.substring(0, columns.length()-1);
        
        String result = "VOXEL SPACE\n"+
                        "#min_corner: "+minCorner.x+" "+minCorner.y+" "+minCorner.z+"\n"+
                        "#max_corner: "+maxCorner.x+" "+maxCorner.y+" "+maxCorner.z+"\n"+
                        "#split: "+split.x+" "+split.y+" "+split.z+"\n"+
                        "#type: "+type+" #res: "+resolution+" #MAX_PAD: "+maxPAD+"\n"+
                        columns;
        
        return result;
    }
}
