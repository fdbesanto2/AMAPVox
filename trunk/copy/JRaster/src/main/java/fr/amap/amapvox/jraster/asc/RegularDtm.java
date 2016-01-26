/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.jraster.asc;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.math.geometry.BoundingBox2F;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point3D;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RegularDtm {
        
    private ArrayList<DTMPoint> points;
    private ArrayList<Face> faces;
    
    private float zMin;
    private float zMax;
    
    private int indiceXMin = -1;
    private int indiceYMin = -1;
    private int indiceXMax = -1;
    private int indiceYMax = -1;
    
    private String path;
    
    private float[][] zArray;
    private float xLeftLowerCorner;
    private float yLeftLowerCorner;
    private float cellSize;
    private int rowNumber;
    private int colNumber;
    
    private Mat4D transformationMatrix;
    private Mat4D inverseTransfMat;
    
    /**
     *
     */
    public RegularDtm(){
        
    }
    
    /**
     *
     * @return
     */
    public String getPath() {
        return path;
    }
    
    /**
     *
     * @return
     */
    public List<DTMPoint> getPoints() {
        return points;
    }
    
    public Point3D getLowerCorner(){
        
        Vec4D result = Mat4D.multiply(transformationMatrix, new Vec4D(xLeftLowerCorner, yLeftLowerCorner, zMin, 1));
        
        return new Point3D(result.x, result.y, result.z);
    }
    
    public Point3D getUpperCorner(){
        
        Vec4D result = Mat4D.multiply(transformationMatrix, 
                new Vec4D(xLeftLowerCorner+colNumber*cellSize, yLeftLowerCorner+rowNumber*cellSize, zMax, 1));
        
        return new Point3D(result.x, result.y, result.z);
    }

    /**
     *
     * @return
     */
    public List<Face> getFaces() {
        return faces;
    }
    
    /**
     * Instantiate a grid raster type
     * @param path Absolute path of the file
     * @param zArray Two-dimensional array representing the 2d grid and containing z values
     * @param xLeftLowerCorner X left lower corner
     * @param yLeftLowerCorner Y left lower corner
     * @param cellSize Size of a cell, in meters (m)
     * @param nbCols Column number
     * @param nbRows Row number
     */
    public RegularDtm(String path, float[][] zArray, float xLeftLowerCorner, float yLeftLowerCorner, float cellSize, int nbCols, int nbRows){
        
        this.transformationMatrix = Mat4D.identity();
        this.inverseTransfMat = Mat4D.identity();
        
        this.path = path;
        this.zArray = zArray;
        this.xLeftLowerCorner = xLeftLowerCorner;
        this.yLeftLowerCorner = yLeftLowerCorner;
        this.cellSize = cellSize;
        this.rowNumber = nbRows;
        this.colNumber = nbCols;
    }
    
    /**
     * Get corresponding height from a x,y couple of points<br>
     * This method doesn't perform interpolation, it gets nearest cell and return corresponding value.<br>
     * @param posX position x
     * @param posY position y
     * @return height from x,y position
     */
    public float getSimpleHeight(float posX, float posY){
        
        Vec4D multiply = Mat4D.multiply(inverseTransfMat, new Vec4D(posX, posY, 1, 1));
        posX = (float) multiply.x;
        posY = (float) multiply.y;
        
        float z;
        
        int indiceX = (int)((posX-xLeftLowerCorner)/cellSize);
        int indiceY = (int)(rowNumber-(posY-yLeftLowerCorner)/cellSize);
        
        if(indiceX < 0 || indiceY < 0 || indiceY >= rowNumber){
            return Float.NaN;
        }
        
        if(indiceX<zArray.length && indiceY<zArray[0].length){
            z = zArray[indiceX][indiceY];
            
        }else{
            return Float.NaN;
        }        
        
        return z;
    }
    
    /**
     * Not implemented
     * @param posX
     * @param posY
     * @return
     */
    public float getInterpolatedHeight(float posX, float posY){
        
        return 0; // not implemented
    }
    
    private BoundingBox2F getLargestBoundingBox(BoundingBox2F boundingBox2F){
        
        //calculate the 4 corners
        Point2F min = boundingBox2F.min;
        Point2F max = boundingBox2F.max;
        
        Vec4D corner1 = Mat4D.multiply(inverseTransfMat, new Vec4D(min.x, min.y, 0, 1));
        Vec4D corner2 = Mat4D.multiply(inverseTransfMat,  new Vec4D(max.x, min.y, 0, 1));
        Vec4D corner3 = Mat4D.multiply(inverseTransfMat, new Vec4D(max.x, max.y, 0, 1));
        Vec4D corner4 = Mat4D.multiply(inverseTransfMat,  new Vec4D(min.x, max.y, 0, 1));
        
        float xMin = (float) corner1.x;
        float yMin = (float) corner1.y;
        float xMax = (float) corner3.x;
        float yMax = (float) corner3.y;
        
        xMin = Float.min(xMin, (float) Double.min(corner2.x, corner4.x));
        yMin = Float.min(yMin, (float) Double.min(corner2.y, corner4.y));
        
        xMax = Float.max(xMax, (float) Double.max(corner2.x, corner4.x));
        yMax = Float.max(yMax, (float) Double.max(corner2.y, corner4.y));
        
        return new BoundingBox2F(new Point2F(xMin, yMin), new Point2F(xMax, yMax));
    }
    
    /**
     *
     * @param boundingBox
     * @param offset
     */
    public void setLimits(BoundingBox2F boundingBox, int offset){
        
        //calculate the 4 corners
        
        boundingBox.min.x -= (offset*cellSize);
        boundingBox.min.y -= (offset*cellSize);
        
        boundingBox.max.x += (offset*cellSize);
        boundingBox.max.y += (offset*cellSize);
        
        BoundingBox2F largestBoundingBox = getLargestBoundingBox(boundingBox);
        
        indiceXMin = (int)((largestBoundingBox.min.x-xLeftLowerCorner)/cellSize);
        if(indiceXMin < 0){
            indiceXMin = -1;
        }
        
        indiceYMin = (int)(rowNumber-(largestBoundingBox.max.y-yLeftLowerCorner)/cellSize);
        if(indiceYMin < 0){
            indiceYMin = -1;
        }
        
        indiceXMax = (int)((largestBoundingBox.max.x-xLeftLowerCorner)/cellSize);
        if(indiceXMax > zArray.length){
            indiceXMax = -1;
        }
        
        indiceYMax = (int)(rowNumber-(largestBoundingBox.min.y-yLeftLowerCorner)/cellSize);
        if(indiceYMax > zArray[0].length){
            indiceYMax = -1;
        }
        
    }
    
    /**
     *
     * @param boundingBox2F
     * @param offset
     * @return
     */
    public RegularDtm subset(BoundingBox2F boundingBox2F, int offset){
        
        RegularDtm dtm = new RegularDtm();
        
        //calculate the 4 corners
        
        boundingBox2F.min.x -= (offset*cellSize);
        boundingBox2F.min.y -= (offset*cellSize);
        
        boundingBox2F.max.x += (offset*cellSize);
        boundingBox2F.max.y += (offset*cellSize);
        
        BoundingBox2F largestBoundingBox = getLargestBoundingBox(boundingBox2F);
        
        int minXId, minYId;
        int maxXId, maxYId;  
        
                
        minXId = (int)((largestBoundingBox.min.x-xLeftLowerCorner)/cellSize);
        if(minXId < 0){
            minXId = -1;
        }
        
        minYId = (int)(rowNumber-(largestBoundingBox.max.y-yLeftLowerCorner)/cellSize);
        if(minYId < 0){
            minYId = -1;
        }
        
        maxXId = (int)((largestBoundingBox.max.x-xLeftLowerCorner)/cellSize);
        if(maxXId > zArray.length){
            maxXId = -1;
        }
        
        maxYId = (int)(rowNumber-(largestBoundingBox.min.y-yLeftLowerCorner)/cellSize);
        if(maxYId > zArray[0].length){
            maxYId = -1;
        }
        
        dtm.xLeftLowerCorner = largestBoundingBox.min.x;
        dtm.yLeftLowerCorner = largestBoundingBox.min.y;
        dtm.cellSize = cellSize;
        dtm.rowNumber = maxYId-minYId;
        dtm.colNumber = maxXId-minXId;
        
        dtm.zArray = new float[dtm.colNumber][dtm.rowNumber];
        for(int i = minXId, i2=0 ; i<maxXId ; i++, i2++){
            for(int j = minYId, j2 = 0 ; j<maxYId ; j++, j2++){
                dtm.zArray[i2][j2] = zArray[i][j];
            }
        }
        
        return dtm;
    }
    
    /**
     * Write the raster in ascii grid format (*.asc)
     * @param output Output file
     * @throws IOException Throws an IOException when output path is invalid or other
     */
    public void write(File output) throws IOException{
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))){
            
            float noDataValue = -9999.000000f;
            writer.write("ncols "+colNumber+"\n");
            writer.write("nrows "+rowNumber+"\n");
            writer.write("xllcorner "+xLeftLowerCorner+"\n");
            writer.write("yllcorner "+yLeftLowerCorner+"\n");
            writer.write("cellsize "+cellSize+"\n");
            writer.write("nodata_value "+noDataValue+"\n");
            
            for(int j = 0;j<rowNumber;j++){
            
                StringBuilder stringBuilder = new StringBuilder();

                for(int i = 0 ; i<colNumber ; i++){

                    if(Float.isNaN(zArray[i][j])){
                        stringBuilder.append(noDataValue);
                    }else{
                        stringBuilder.append(zArray[i][j]);
                    }
                    
                    stringBuilder.append(" ");
                }

                writer.write(stringBuilder.toString()+"\n");
            }
            
            
        } catch (IOException ex) {
            throw new IOException("Cannot write dtm file "+output.getAbsolutePath(), ex);
        }
    }
    
    /**
     * Build a 3d mesh from the raster, creating points, faces (as indices)
     */
    public void buildMesh(){
        
        
        if(zArray != null){
            
            int width = zArray.length;
            if(width > 0){
                
                int height = zArray[0].length;
                faces = new ArrayList<>();
                points = new ArrayList<>();
                
                
                if(indiceXMin == -1){indiceXMin = 0;}
                
                if(indiceYMin == -1){indiceYMin = 0;}
                
                if(indiceXMax == -1){indiceXMax = width;}
                
                if(indiceYMax == -1){indiceYMax = height;}
                
                
                for(int i = indiceXMin;i<indiceXMax;i++){
                    for(int j = indiceYMin;j<indiceYMax;j++){
                        
                        float z ;
                        
                        if(!Float.isNaN(zArray[i][j])){
                            z = zArray[i][j];
                            
                        }else{
                            z = -10.0f;
                        }
                        
                        DTMPoint point = new DTMPoint((i*cellSize+xLeftLowerCorner),  (-j+rowNumber)*cellSize+yLeftLowerCorner, z);
                        Vec4D result = Mat4D.multiply(transformationMatrix, new Vec4D(point.x, point.y, point.z, 1));
                        
                        point.x = (float) result.x;
                        point.y = (float) result.y;


                        if(i == 0 && j == 0){
                            zMin = (float) result.z;
                            zMax = (float) result.z;
                        }else{
                            if(result.z < zMin){
                                zMin = (float) result.z;
                            }
                            if(result.z > zMax){
                                zMax = (float) result.z;
                            }
                        }

                        point.z = (float) result.z;
                        points.add(point);                     
                    }
                }
                
                for(int i = indiceXMin;i<indiceXMax;i++){
                    for(int j = indiceYMin;j<indiceYMax;j++){
                        
                        int point1, point2, point3, point4;
                        
                        point1 = get1dFrom2d(i, j);
                        
                        if(i+1 < (indiceXMax)){
                            point2 = get1dFrom2d(i+1, j);
                            
                            if(j+1 < (indiceYMax)){
                                point3 = get1dFrom2d(i+1, j+1);
                                point4 = get1dFrom2d(i, j+1);
                                
                                if(!Float.isNaN(zArray[i][j]) && !Float.isNaN(zArray[i+1][j+1]) ){
                                    
                                    if(point1 < points.size() && point2  < points.size() && point3  < points.size()){
                                        if(!Float.isNaN(zArray[i+1][j])){
                                            faces.add(new Face(point1, point2, point3));

                                            int faceID = faces.size()-1;
                                            points.get(point1).faces.add(faceID);
                                            points.get(point2).faces.add(faceID);
                                            points.get(point3).faces.add(faceID);
                                        }
                                        
                                    }
                                    
                                    if(point1 < points.size() && point3  < points.size() && point4  < points.size()){
                                        if(!Float.isNaN(zArray[i][j+1])){
                                            faces.add(new Face(point1, point3, point4));
                                            int faceID = faces.size()-1;
                                            points.get(point1).faces.add(faceID);
                                            points.get(point3).faces.add(faceID);
                                            points.get(point4).faces.add(faceID);
                                        }
                                    } 
                                }
                            }
                        }
                    }
                }
            }
        }
        
        zArray = null;
    }
    
    private int get1dFrom2d(int i, int j){
        
        return ((indiceYMax-indiceYMin)*(i-indiceXMin)) + (j-indiceYMin);
    }
    
    /**
     *
     * @param outputFile
     * @throws FileNotFoundException
     * @throws IOException
     */
    public void exportObj(File outputFile) throws FileNotFoundException, IOException{
        
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            
            writer.write("o terrain\n");
            
            for (DTMPoint point : points) {
                writer.write("v " + point.x + " " + point.y + " " + point.z + " " + "\n");
            }
            
            for (Face face : faces) {
                writer.write("f " + (face.getPoint1() + 1) + " " + (face.getPoint2() + 1) + " " + (face.getPoint3() + 1) + "\n");
            }
            
            
            writer.close();
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
    }

    /**
     *
     * @param transformationMatrix
     */
    public void setTransformationMatrix(Mat4D transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
        this.inverseTransfMat = Mat4D.inverse(transformationMatrix);
    }

    /**
     *
     * @return
     */
    public Mat4D getTransformationMatrix() {
        return transformationMatrix;
    }

    /**
     *
     * @return
     */
    public float getzMin() {
        return zMin;
    }

    /**
     *
     * @return
     */
    public float getzMax() {
        return zMax;
    }
}
