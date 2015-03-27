/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import fr.ird.voxelidar.engine3d.object.mesh.Face;
import fr.ird.voxelidar.engine3d.math.vector.Vec2F;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class Dtm {
    
    private final Logger logger = Logger.getLogger(Dtm.class);
    private ArrayList<Vec3F> points;
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
    
    public Point3d getNearest(Point3d position){
        
        
        float x = (float) (minCorner.x+position.x /(resolution.x));
        float y = (float) (position.y /(resolution.y));
        
        return new Point3d(x, y, 0);
    }
    
    public String getPath() {
        return path;
    }
    
    public ArrayList<Vec3F> getPoints() {
        return points;
    }

    public ArrayList<Face> getFaces() {
        return faces;
    }
    
    public ArrayList<Short> getIndices(){
        
        ArrayList<Short> indices = new ArrayList<>();
        
        for (Face face : faces) {
            indices.add((short) face.getPoint1());
            indices.add((short) face.getPoint2());
            indices.add((short) face.getPoint3());
        }
        
        return indices;
    }
    
        
    public Dtm(String path, ArrayList<Vec3F> points, ArrayList<Face> faces){
        
        this.points = points;
        this.faces = faces;
        this.path = path;
    }
    
    public Dtm(String path, float[][] zArray, float xLeftLowerCorner, float yLeftLowerCorner, float cellSize){
        
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
       
        
        for (Vec3F point : points) {
            
            map.put(point.x+0.0f, point.z+0.0f, point.y+0.0f);
        }
        
        return map;
    }
    
    public void constructZArray(){
        
        for (Vec3F point : points) {
            
            
        }
    }
    
    public float getSimpleHeight(float posX, float posY){
        
        float z;
        
        int indiceX = (int)((posX-xLeftLowerCorner)/cellSize);
        int indiceY = (int)(rowNumber-(posY-yLeftLowerCorner)/cellSize);
        
        if(indiceX < 0 || indiceY < 0 || indiceY >= rowNumber){
            return Float.NaN;
        }
        
        if(indiceX<zArray.length && indiceY<zArray[0].length){
            z = zArray[indiceX][indiceY];
            
            if(z == -9999.0f){
                return Float.NaN;
            }
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
    /*
    public float getHeight(float posX, float posY){
        
        if(map == null){
            map = getXYStructure();
        }
        float height = 10;
        
        Point3d nearest = getNearest(new Point3d(posX, posY, 0));
        
        try{
            height = (float) map.get(posX, posY);
        }catch(Exception e){
            try{
                height = (float) map.get((int)posX, (int)posY);
            }catch(Exception e2){
                
                //logger.error("unable to get z distance from x,y coordinates: X= "+posX+" Y= "+posY);
            }
            
        }
        
        
        return height;
    }
    */
    public void exportObj(File outputFile){
        
        BufferedWriter writer;
        try {
            writer = new BufferedWriter(new FileWriter(outputFile));
            
            writer.write("o terrain\n");
            
            for (Vec3F point : points) {
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
            
            Vec3F pointA = points.get(triangle.getPoint1());
            Vec3F pointB = points.get(triangle.getPoint2());
            Vec3F pointC = points.get(triangle.getPoint3());
            
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
    
    public float getZFromXY(float x, float y){
        
        //détermination des 3 points formant la face qui contient x,y
        Face triangle = getFaceContainingPoint(x, y);
        
        Vec3F pointA = points.get(triangle.getPoint1());
        Vec3F pointB = points.get(triangle.getPoint2());
        Vec3F pointC = points.get(triangle.getPoint3());
        
        //calcul de l'équation du plan
        Vec3F vecAB = Vec3F.createVec3FromPoints(pointA, pointB);
        Vec3F vecAC = Vec3F.createVec3FromPoints(pointA, pointC);
        
        Vec3F vecNorm = Vec3F.cross(vecAB, vecAC);
        float a = vecNorm.x;
        float b = vecNorm.y;
        float c = vecNorm.z;
        float d = (-pointA.x * vecNorm.x) + (-pointA.y * vecNorm.y) + (-pointA.z * vecNorm.z);
        
        //on remplace dans l'équation pour trouver z
        //float z = ((a*x) + (b*y) + d)/(-c);
        float z = ((-a*x) - (c*y) - d)/(b);
        return z;
    }
}
