/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.datastructure.octree;

import fr.amap.amapvox.math.matrix.Mat4D;
import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.math.vector.Vec4D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class OctreeFactory {
    
    public final static int DEFAULT_MAXIMUM_POINT_NUMBER = 50;
    
    
    public static Octree createOctreeFromPointFile(File file, int maximumPointNumber, boolean sortPoints, Mat4D transfMatrix) throws Exception{
        
        List<Point3F> pointList = new ArrayList<>();
        
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            
            String line;
            boolean isInit = false;
            String separator = " ";
            int count = 1;
            
            while((line = reader.readLine()) != null){
                
                if(!isInit){
                    if(line.contains(",") && line.contains(".")){
                        separator = ",";
                    }
                    
                    String[] split = line.split(separator);
                    if(split.length > 3){
                        //logger.info("Point file contains more columns than necessary, parsing the three first");
                    }else if(split.length < 3){
                        reader.close();
                        throw new Exception("Point file doesn't contains valid columns!");
                    }
                    
                    isInit = true;
                }
                
                String[] split = line.split(separator);
                if(split.length < 3){
                    reader.close();
                    throw new Exception("Error parsing line "+count);
                }
                
                Vec4D transformedPoint = Mat4D.multiply(transfMatrix, new Vec4D(Float.valueOf(split[0]), Float.valueOf(split[1]), Float.valueOf(split[2]), 1));
                
                pointList.add(new Point3F((float) transformedPoint.x, (float) transformedPoint.y, (float) transformedPoint.z));
                count++;
            }
            
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        Point3F[] points = new Point3F[pointList.size()];
        
        float minPointX = 0, minPointY = 0, minPointZ = 0;
        float maxPointX = 0, maxPointY = 0, maxPointZ = 0;
        
        boolean init = false;
        for(Point3F point : pointList){
            
            if(!init){
                minPointX = point.x;
                minPointY = point.y;
                minPointZ = point.z;
                
                maxPointX = point.x;
                maxPointY = point.y;
                maxPointZ = point.z;
                
                init = true;
                
            }else{
                
                if(point.x > maxPointX){
                    maxPointX = point.x;
                }else if(point.x < minPointX){
                    minPointX = point.x;
                }
                
                if(point.y > maxPointY){
                    maxPointY = point.y;
                }else if(point.y < minPointY){
                    minPointY = point.y;
                }
                
                if(point.z > maxPointZ){
                    maxPointZ = point.z;
                }else if(point.z < minPointZ){
                    minPointZ = point.z;
                }
            }
        }
        
        Point3F minPoint = new Point3F(minPointX, minPointY, minPointZ);
        Point3F maxPoint = new Point3F(maxPointX, maxPointY, maxPointZ);
        
        if(sortPoints){
            Collections.sort(pointList);
        }
        
        pointList.toArray(points);
        
        Octree octree = new Octree(maximumPointNumber);
        
        octree.setPoints(points);
        octree.setMinPoint(minPoint);
        octree.setMaxPoint(maxPoint);
        
        return octree;
    }
}
