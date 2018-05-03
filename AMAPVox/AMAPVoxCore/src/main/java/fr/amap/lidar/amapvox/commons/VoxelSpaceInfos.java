package fr.amap.lidar.amapvox.commons;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author calcul
 */
public class VoxelSpaceInfos {
        
    private Point3d minCorner;
    private Point3d maxCorner;
    private Point3i split;
    private Type type; //ALS ou TLS
    private final Point3d voxelSize;
    private float resolution;
    private float maxPAD = 5.0f;
    private LeafAngleDistribution.Type ladType = LeafAngleDistribution.Type.SPHERIC;
    private double[] ladParams;
    private String[] columnNames;
    private List<String> columnNamesList;
    private int transmittanceMode;
    private String pathLengthMode;
    
    public enum Type{
        ALS(1),
        TLS(2);
        
        private final int type;
        Type(int type){
            this.type = type;
        }
    }

    public VoxelSpaceInfos(){
        this.pathLengthMode = "A";
        this.transmittanceMode = 1;
        this.voxelSize = new Point3d();
    }
    
    public VoxelSpaceInfos(Point3d minCorner, Point3d maxCorner, float resolution) {
        this.pathLengthMode = "A";
        this.transmittanceMode = 1;
        
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        
        setResolution((double)resolution);
        
        this.voxelSize = new Point3d();
        
        updateVoxelSize();
    }
    
    public VoxelSpaceInfos(Point3d minCorner, Point3d maxCorner, Point3i split) {
        this.pathLengthMode = "A";
        this.transmittanceMode = 1;
        this.minCorner = minCorner;
        this.maxCorner = maxCorner;
        this.split = split;
        this.voxelSize = new Point3d();
        updateVoxelSize();
    }
    
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
                
                String typeStr = otherValuesArray[1];
                
                if(typeStr.contains("/")){ //workaround for the new parameters
                    String[] split1 = typeStr.split("/");
                    if(split1[0].equals("ALS")){
                        type = Type.ALS;
                    }else{
                        type = Type.TLS;
                    }
                    
                    transmittanceMode = Integer.valueOf(split1[1]);
                    pathLengthMode = split1[2];
                    
                }else{
                    if(typeStr.equals("ALS")){
                        type = Type.ALS;
                    }else{
                        type = Type.TLS;
                    }
                }
                
                resolution = Float.valueOf(otherValuesArray[3]);
                maxPAD = Float.valueOf(otherValuesArray[5]);
                
                //test for leaf angle distribution parameter
                if(otherValuesArray.length >= 8){
                    String ladStr = otherValuesArray[7];
                    
                    if(ladStr.contains("=")){
                        
                        String[] split = ladStr.split("=");
                        String ladTypeName = split[0];
                        ladType = LeafAngleDistribution.Type.fromString(ladTypeName);
                        
                        split[1] = split[1].replace("[", "");
                        split[1] = split[1].replace("]", "");
                        
                        String[] params = split[1].split(";");
                        ladParams = new double[params.length];
                        
                        for(int i = 0;i<params.length;i++){
                            ladParams[i] = Double.valueOf(params[i]);
                        }
                        
                    }else{
                        ladType = LeafAngleDistribution.Type.fromString(ladStr);
                    }
                }
                
                
            }catch(IOException | NumberFormatException e){
                
                reader.close();
                throw new Exception("Header is invalid",e);
            }
            
            columnNames = reader.readLine().split(" ");
            
            columnNamesList = new ArrayList<>(columnNames.length);
            columnNamesList.addAll(Arrays.asList(columnNames));
            
            reader.close();
            
        } catch (FileNotFoundException ex) {
            throw new Exception("Cannot find voxel file",ex);
        } catch (IOException ex) {
            throw new Exception("Cannot read voxel file",ex);
        }
    }
    
    private void updateVoxelSize(){
        
        double boundingBoxSizeX = maxCorner.x - minCorner.x;
        double boundingBoxSizeY = maxCorner.y - minCorner.y;
        double boundingBoxSizeZ = maxCorner.z - minCorner.z;
        
        this.voxelSize.x = boundingBoxSizeX / split.x;
        this.voxelSize.y = boundingBoxSizeY / split.y;
        this.voxelSize.z = boundingBoxSizeZ / split.z;
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

    public Type getType() {
        return type;
    }

    public float getResolution() {
        return resolution;
    }

    public Point3d getVoxelSize() {
        
        if(voxelSize.x == 0 && voxelSize.y == 0 && voxelSize.z == 0){
            updateVoxelSize();
        }
        return voxelSize;
    }
    
    public float getMaxPAD() {
        return maxPAD;
    }

    public String[] getColumnNames() {
        return columnNames;
    }
    
    public List<String> getColumnNamesList() {
        
        return columnNamesList;
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

    public void setType(Type type) {
        this.type = type;
    }

    public void setResolution(float resolution) {
        this.resolution = resolution;
    }
    
    public void setResolution(double resolution) {
        
        this.resolution = (float) resolution;
        
        if(minCorner != null && maxCorner != null){
            
            split = new Point3i(
                    (int) Math.ceil((maxCorner.getX() - minCorner.getX()) / resolution),
                    (int) Math.ceil((maxCorner.getY() - minCorner.getY()) / resolution),
                    (int) Math.ceil((maxCorner.getZ() - minCorner.getZ()) / resolution));
        }
    }

    public void setMaxPAD(float maxPAD) {
        this.maxPAD = maxPAD;
    }

    public void setColumnNames(String[] columnNames) {
        this.columnNames = columnNames;
    }

    public LeafAngleDistribution.Type getLadType() {
        return ladType;
    }

    public void setLadType(LeafAngleDistribution.Type ladType) {
        this.ladType = ladType;
    }

    public double[] getLadParams() {
        return ladParams;
    }

    public void setLadParams(double[] ladParams) {
        this.ladParams = ladParams;
    }

    public int getTransmittanceMode() {
        return transmittanceMode;
    }

    public String getPathLengthMode() {
        return pathLengthMode;
    }

    public void setTransmittanceMode(int transmittanceMode) {
        this.transmittanceMode = transmittanceMode;
    }

    public void setPathLengthMode(String pathLengthMode) {
        this.pathLengthMode = pathLengthMode;
    }
    
    public String headerToString(){
        
        String metadata = "#type: "+type+"/"+transmittanceMode+"/"+pathLengthMode+" #res: "+resolution+" #MAX_PAD: "+maxPAD;
        
        metadata += " #LAD_TYPE: " + ladType.toString();
            
        if (ladType == LeafAngleDistribution.Type.TWO_PARAMETER_BETA || ladType == LeafAngleDistribution.Type.ELLIPSOIDAL) {
            
            metadata += "=[";

            for (int i = 0; i < ladParams.length; i++) {
                if (i != 0) {
                    metadata += ";";
                }
                metadata += ladParams[i];
            }

            metadata += "]";
        }
        
        String result = "VOXEL SPACE\n"+
                        "#min_corner: "+minCorner.x+" "+minCorner.y+" "+minCorner.z+"\n"+
                        "#max_corner: "+maxCorner.x+" "+maxCorner.y+" "+maxCorner.z+"\n"+
                        "#split: "+split.x+" "+split.y+" "+split.z+"\n"+
                        metadata;
        
        return result;
    }
    
    @Override
    public String toString(){
        
        String columns = "";
        for(String column : columnNames){
            columns += column + " ";
        }
        
        columns = columns.substring(0, columns.length()-1);
        
        String metadata = "#type: "+type+" #res: "+resolution+" #MAX_PAD: "+maxPAD;
        
        metadata += " #LAD_TYPE: " + ladType.toString();
            
        if (ladType == LeafAngleDistribution.Type.TWO_PARAMETER_BETA) {
            
            metadata += "=[";

            for (int i = 0; i < ladParams.length; i++) {
                if (i != 0) {
                    metadata += ";";
                }
                metadata += ladParams[i];
            }

            metadata += "]";
        }
        
        String result = "VOXEL SPACE\n"+
                        "#min_corner: "+minCorner.x+" "+minCorner.y+" "+minCorner.z+"\n"+
                        "#max_corner: "+maxCorner.x+" "+maxCorner.y+" "+maxCorner.z+"\n"+
                        "#split: "+split.x+" "+split.y+" "+split.z+"\n"+
                        metadata+"\n"+
                        columns;
        
        return result;
    }
}
