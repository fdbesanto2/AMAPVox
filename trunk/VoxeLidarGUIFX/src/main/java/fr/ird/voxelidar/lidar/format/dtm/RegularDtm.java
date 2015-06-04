/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dtm;

import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec2F;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
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
    private String path;
    private MultiKeyMap map;
    
    private float[][] zArray;
    float xLeftLowerCorner;
    float yLeftLowerCorner;
    float cellSize;
    int rowNumber;
    
    private Map m;
    
    private Point2d minCorner = new Point2d();
    private Point2d maxCorner = new Point2d();
    private Point2d resolution;
    private float splitting;
    private Mat4D transformationMatrix;
    
    public Point3d getNearest(Point3d position){
        
        
        float x = (float) (minCorner.x+position.x /(resolution.x));
        float y = (float) (position.y /(resolution.y));
        
        return new Point3d(x, y, 0);
    }
    
    public String getPath() {
        return path;
    }
    
    public ArrayList<DTMPoint> getPoints() {
        return points;
    }

    public ArrayList<Face> getFaces() {
        return faces;
    }    
        
    public RegularDtm(String path, ArrayList<DTMPoint> points, ArrayList<Face> faces){
        
        this.transformationMatrix = Mat4D.identity();
        
        this.points = points;
        this.faces = faces;
        this.path = path;
    }
    
    public RegularDtm(String path, float[][] zArray, float xLeftLowerCorner, float yLeftLowerCorner, float cellSize){
        
        this.transformationMatrix = Mat4D.identity();
        
        this.path = path;
        this.zArray = zArray;
        this.xLeftLowerCorner = xLeftLowerCorner;
        this.yLeftLowerCorner = yLeftLowerCorner;
        this.cellSize = cellSize;
        this.rowNumber = zArray[0].length;
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
        
        Vec4D multiply = Mat4D.multiply(Mat4D.inverse(transformationMatrix), new Vec4D(posX, posY, 1, 1));
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
    
    public void buildMesh(){
        
        if(zArray != null){
            
            int width = zArray.length;
            if(width > 0){
                
                int height = zArray[0].length;
                faces = new ArrayList<>();
                points = new ArrayList<>();
                
                for(int i=0;i<width;i++){
                    for(int j=0;j<height;j++){
                        
                        if(!Float.isNaN(zArray[i][j])){
                            points.add(new DTMPoint(i*cellSize, j*cellSize, zArray[i][j]));
                        }else{
                            points.add(new DTMPoint(i*cellSize, j*cellSize, -10.0f));
                        }
                    }
                }
                
                for(int i=0;i<width;i++){
                    for(int j=0;j<height;j++){
                        /*
                        if(!Float.isNaN(zArray[i][j])){
                            points.add(new DTMPoint(i*cellSize, j*cellSize, zArray[i][j]));
                        }else{
                            points.add(new DTMPoint(i*cellSize, j*cellSize, -10.0f));
                        }*/
                        
                        
                        int point1, point2, point3, point4;
                        
                        point1 = get1dFrom2d(i, j);
                        
                        if(i+1 < width){
                            point2 = get1dFrom2d(i+1, j);
                            
                            if(j+1 < height){
                                point3 = get1dFrom2d(i+1, j+1);
                                point4 = get1dFrom2d(i, j+1);
                                
                                if(!Float.isNaN(zArray[i][j]) && !Float.isNaN(zArray[i+1][j+1]) ){
                                    if(!Float.isNaN(zArray[i+1][j])){
                                        faces.add(new Face(point1, point2, point3));
                                        
                                        int faceID = faces.size()-1;
                                        points.get(point1).faces.add(faceID);
                                        points.get(point2).faces.add(faceID);
                                        points.get(point3).faces.add(faceID);
                                    }
                                    if(!Float.isNaN(zArray[i][j+1])){
                                        faces.add(new Face(point1, point3, point4));
                                        int faceID = faces.size()-1;
                                        points.get(point1).faces.add(faceID);
                                        points.get(point2).faces.add(faceID);
                                        points.get(point3).faces.add(faceID);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    private int get1dFrom2d(int i, int j){
        return (zArray[0].length*i) + j;
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
    }

    public Mat4D getTransformationMatrix() {
        return transformationMatrix;
    }
    
}
