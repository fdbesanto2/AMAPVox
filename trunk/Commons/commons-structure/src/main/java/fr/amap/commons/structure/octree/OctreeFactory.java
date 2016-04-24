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
import fr.amap.commons.util.io.file.CSVFile;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class OctreeFactory {
    
    public final static int DEFAULT_MAXIMUM_POINT_NUMBER = 50;
    
    
    public static Octree createOctreeFromPointFile(CSVFile file, int maximumPointNumber, boolean sortPoints, Mat4D transfMatrix) throws Exception{
        
        List<Point3D> pointList = new ArrayList<>();
        
        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            
            String line;
            
            if(file.containsHeader()){
                reader.readLine();
            }
                
            for(int i=0;i<file.getNbOfLinesToSkip();i++){
                reader.readLine();
            }

            Map<String, Integer> columnAssignment = file.getColumnAssignment();

            while((line = reader.readLine()) != null){

                String[] split = line.split(file.getColumnSeparator());

                Vec4D transformedPoint = Mat4D.multiply(transfMatrix, 
                        new Vec4D(Float.valueOf(split[columnAssignment.get("X")]),
                                Float.valueOf(split[columnAssignment.get("Y")]),
                                Float.valueOf(split[columnAssignment.get("Z")]),
                                1));

                pointList.add(new Point3D((float) transformedPoint.x, (float) transformedPoint.y, (float) transformedPoint.z));
            }
            
            reader.close();
            
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
