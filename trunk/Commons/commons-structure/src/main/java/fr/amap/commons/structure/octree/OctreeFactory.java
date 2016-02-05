/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.structure.octree;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec4D;
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
        
        List<Point3D> pointList = new ArrayList<>();
        
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
                
                pointList.add(new Point3D((float) transformedPoint.x, (float) transformedPoint.y, (float) transformedPoint.z));
                count++;
            }
            
        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        Point3D[] points = new Point3D[pointList.size()];
        
        if(sortPoints){
            Collections.sort(pointList);
        }
        
        pointList.toArray(points);
        
        Octree octree = new Octree(maximumPointNumber);
        
        octree.setPoints(points);
        
        return octree;
    }
}
