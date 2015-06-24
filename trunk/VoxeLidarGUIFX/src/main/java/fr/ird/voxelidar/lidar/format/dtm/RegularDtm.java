/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dtm;

import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.point.Point2F;
import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.vector.Vec2F;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import fr.ird.voxelidar.util.BoundingBox2F;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RegularDtm {
    
    private final Logger logger = Logger.getLogger(RegularDtm.class);
    
    private ArrayList<DTMPoint> points;
    private ArrayList<Face> faces;
    
    private float zMin;
    private float zMax;
    
    private int indiceXMin = -1;
    private int indiceYMin = -1;
    private int indiceXMax = -1;
    private int indiceYMax = -1;
    
    private String path;
    private MultiKeyMap map;
    
    private float[][] zArray;
    float xLeftLowerCorner;
    float yLeftLowerCorner;
    float cellSize;
    public int rowNumber;
    public int colNumber;
    
    private Map m;
    
    private Point2d minCorner = new Point2d();
    private Point2d maxCorner = new Point2d();
    private Point2d resolution;
    private float splitting;
    
    private Mat4D transformationMatrix;
    private Mat4D inverseTransfMat;
    
    public RegularDtm(){
        
    }
    
    public Point3d getNearest(Point3d position){
        
        
        float x = (float) (minCorner.x+position.x /(resolution.x));
        float y = (float) (position.y /(resolution.y));
        
        return new Point3d(x, y, 0);
    }
    
    public String getPath() {
        return path;
    }
    
    public List<DTMPoint> getPoints() {
        return points;
    }

    public List<Face> getFaces() {
        return faces;
    }    
        
    public RegularDtm(String path, ArrayList<DTMPoint> points, ArrayList<Face> faces){
        
        this.transformationMatrix = Mat4D.identity();
        this.inverseTransfMat = Mat4D.identity();
        
        this.points = points;
        this.faces = faces;
        this.path = path;
    }
    
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
    
    
    public MultiKeyMap getXYStructure(){
        
        
        map = new MultiKeyMap();
        
        if(points.size()>2){
            
            minCorner.x = points.get(0).x;
            minCorner.y = points.get(0).z;
            
            maxCorner.x = points.get(0).x;
            maxCorner.y = points.get(0).z;
            
            splitting = Math.abs(points.get(0).x - points.get(1).x);
        }
        
        for(int i=1;i<points.size();i++){
            
            float x = points.get(i).x;
            float y = points.get(i).z;
            
            if(minCorner.x > x){
                minCorner.x = x;
            }
            if(maxCorner.x < x){
                maxCorner.x = x;
            }
            
            if(minCorner.y > y){
                minCorner.y = y;
            }
            if(maxCorner.y < y){
                maxCorner.y = y;
            }
        }
        
        resolution = new Point2d(maxCorner.x-minCorner.x/splitting, maxCorner.y-minCorner.y);
       
        
        for (DTMPoint point : points) {
            
            map.put(point.x+0.0f, point.z+0.0f, point.y+0.0f);
        }
        
        return map;
    }
    
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
    
    public float getInterpolatedHeight(float posX, float posY){
        
        if(map == null){
            map = getXYStructure();
        }
        
        int x1 = (int)posX;
        int x2 = ((int)posX)+1;
        int y1 = (int)posY;
        int y2 = ((int)posY)+1;
        
        float z1 =0, z2=0, z3=0, z4=0;
        
        try{
            z3 = (float) map.get((float)x1, (float)y1);
        }catch(Exception e){}
        
        try{
            z4 = (float) map.get((float)x2, (float)y1);
        }catch(Exception e){}
        
        try{
            z2 = (float) map.get((float)x2, (float)y2);
        }catch(Exception e){}
        try{
            z1 = (float) map.get((float)x1, (float)y2);
        }catch(Exception e){}
        
        
        //interpolation
        float lambda = (x1-posX)/(x2 - x1);
        float mu = (y1-posY) / (y2 -y1);
        
        float z = (1-lambda)*((1-mu)*z1+mu*z3)+lambda*((1-mu)*z2+(mu*z4));
             
        return z;
    }
    
    public void setLimits(Point3F min, Point3F max, int offset){
        
        //calculate the 4 corners
        
        min.x -= (offset*cellSize);
        min.y -= (offset*cellSize);
        
        max.x += (offset*cellSize);
        max.y += (offset*cellSize);
        
        Vec4D corner1 = Mat4D.multiply(inverseTransfMat, new Vec4D(min.x, min.y, min.z, 1));
        Vec4D corner2 = Mat4D.multiply(inverseTransfMat,  new Vec4D(max.x, min.y, min.z, 1));
        Vec4D corner3 = Mat4D.multiply(inverseTransfMat, new Vec4D(max.x, max.y, max.z, 1));
        Vec4D corner4 = Mat4D.multiply(inverseTransfMat,  new Vec4D(min.x, max.y, min.z, 1));
        
        float xMin = (float) corner1.x;
        float yMin = (float) corner1.y;
        float xMax = (float) corner3.x;
        float yMax = (float) corner3.y;
        
        if(corner2.x < xMin){xMin = (float) corner2.x;}
        if(corner2.x > xMax){xMax = (float) corner2.x;}
        if(corner2.y < yMin){yMin = (float) corner2.y;}
        if(corner2.y > yMax){yMax = (float) corner2.y;}
        if(corner4.x < xMin){xMin = (float) corner4.x;}
        if(corner4.x > xMax){xMax = (float) corner4.x;}
        if(corner4.y < yMin){yMin = (float) corner4.y;}
        if(corner4.y > yMax){yMax = (float) corner4.y;}
        
        
        Point2F minPoint = new Point2F(xMin, yMin);
        Point2F maxPoint = new Point2F(xMax, yMax);
        
                
        indiceXMin = (int)((minPoint.x-xLeftLowerCorner)/cellSize);
        if(indiceXMin < 0){
            indiceXMin = -1;
        }
        
        indiceYMin = (int)(rowNumber-(maxPoint.y-yLeftLowerCorner)/cellSize);
        if(indiceYMin < 0){
            indiceYMin = -1;
        }
        
        indiceXMax = (int)((maxPoint.x-xLeftLowerCorner)/cellSize);
        if(indiceXMax > zArray.length){
            indiceXMax = -1;
        }
        
        indiceYMax = (int)(rowNumber-(minPoint.y-yLeftLowerCorner)/cellSize);
        if(indiceYMax > zArray[0].length){
            indiceYMax = -1;
        }
        
    }
    
    public RegularDtm subset(BoundingBox2F boundingBox2F, int offset){
        
        RegularDtm dtm = new RegularDtm();
        
        //calculate the 4 corners
        Point2F min = boundingBox2F.min;
        Point2F max = boundingBox2F.max;
        
        min.x -= (offset*cellSize);
        min.y -= (offset*cellSize);
        
        max.x += (offset*cellSize);
        max.y += (offset*cellSize);
        
        Vec4D corner1 = Mat4D.multiply(inverseTransfMat, new Vec4D(min.x, min.y, 0, 1));
        Vec4D corner2 = Mat4D.multiply(inverseTransfMat,  new Vec4D(max.x, min.y, 0, 1));
        Vec4D corner3 = Mat4D.multiply(inverseTransfMat, new Vec4D(max.x, max.y, 0, 1));
        Vec4D corner4 = Mat4D.multiply(inverseTransfMat,  new Vec4D(min.x, max.y, 0, 1));
        
        float xMin = (float) corner1.x;
        float yMin = (float) corner1.y;
        float xMax = (float) corner3.x;
        float yMax = (float) corner3.y;
        
        if(corner2.x < xMin){xMin = (float) corner2.x;}
        if(corner2.x > xMax){xMax = (float) corner2.x;}
        if(corner2.y < yMin){yMin = (float) corner2.y;}
        if(corner2.y > yMax){yMax = (float) corner2.y;}
        if(corner4.x < xMin){xMin = (float) corner4.x;}
        if(corner4.x > xMax){xMax = (float) corner4.x;}
        if(corner4.y < yMin){yMin = (float) corner4.y;}
        if(corner4.y > yMax){yMax = (float) corner4.y;}
        
        
        Point2F minPoint = new Point2F(xMin, yMin);
        Point2F maxPoint = new Point2F(xMax, yMax);
        
        int minXId, minYId;
        int maxXId, maxYId;  
        
                
        minXId = (int)((minPoint.x-xLeftLowerCorner)/cellSize);
        if(minXId < 0){
            minXId = -1;
        }
        
        minYId = (int)(rowNumber-(maxPoint.y-yLeftLowerCorner)/cellSize);
        if(minYId < 0){
            minYId = -1;
        }
        
        maxXId = (int)((maxPoint.x-xLeftLowerCorner)/cellSize);
        if(maxXId > zArray.length){
            maxXId = -1;
        }
        
        maxYId = (int)(rowNumber-(minPoint.y-yLeftLowerCorner)/cellSize);
        if(maxYId > zArray[0].length){
            maxYId = -1;
        }
        
        dtm.xLeftLowerCorner = minPoint.x;
        dtm.yLeftLowerCorner = minPoint.y;
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
    
    public void write(File output){
        
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
            logger.error("Cannot write dtm file "+output.getAbsolutePath(), ex);
        }
    }
    
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
    
    public void exportObj(File outputFile){
        
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
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }

        
    }
    
    public Face getFaceContainingPoint(float x, float y){
        
        for (Face triangle : faces) {
            
            DTMPoint pointA = points.get(triangle.getPoint1());
            DTMPoint pointB = points.get(triangle.getPoint2());
            DTMPoint pointC = points.get(triangle.getPoint3());
            
            Vec2F vecAB = Vec2F.createVec2FromPoints(new Vec2F(pointA.x, pointA.z), new Vec2F(pointB.x, pointB.z));
            Vec2F vecBC = Vec2F.createVec2FromPoints(new Vec2F(pointB.x, pointB.z), new Vec2F(pointC.x, pointC.z));
            Vec2F vecCA = Vec2F.createVec2FromPoints(new Vec2F(pointC.x, pointC.z), new Vec2F(pointA.x, pointA.z));
            
            
            Vec2F vecAM = Vec2F.createVec2FromPoints(new Vec2F(pointA.x, pointA.z), new Vec2F(x, y));
            Vec2F vecBM = Vec2F.createVec2FromPoints(new Vec2F(pointB.x, pointB.z), new Vec2F(x, y));
            Vec2F vecCM = Vec2F.createVec2FromPoints(new Vec2F(pointC.x, pointC.z), new Vec2F(x, y));
            
            float detABAM = Vec2F.determinant(vecAB, vecAM);
            float detBCBM = Vec2F.determinant(vecBC, vecBM);
            float detCACM = Vec2F.determinant(vecCA, vecCM);
            
            if(detABAM<=0 && detBCBM<=0 && detCACM<=0){
                return triangle;
            }else if(detABAM>0 && detBCBM>0 && detCACM>0){
                return triangle;
            }
        }
        
        return null;
    }

    public void setTransformationMatrix(Mat4D transformationMatrix) {
        this.transformationMatrix = transformationMatrix;
        this.inverseTransfMat = Mat4D.inverse(transformationMatrix);
    }

    public Mat4D getTransformationMatrix() {
        return transformationMatrix;
    }

    public float getzMin() {
        return zMin;
    }

    public float getzMax() {
        return zMax;
    }
}
