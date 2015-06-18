/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dtm;

import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import fr.ird.voxelidar.util.delaunay.Delaunay_Triangulation;
import fr.ird.voxelidar.util.delaunay.Point_dt;
import fr.ird.voxelidar.util.delaunay.Triangle_dt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class DtmLoader {

    final static Logger logger = Logger.getLogger(DtmLoader.class);
    
    public static RegularDtm readFromAscFile(File ascFile) throws Exception{
        
        final String pathFile = ascFile.getAbsolutePath();
        
        
        String line;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ascFile))) {
            
            int nbCols = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            int nbRows = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float xLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float yLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float step = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float noDataValue = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            
            float[][] zArray = new float[nbCols][nbRows];
            
            float z;
            
            int yIndex = 0;
            
            while((line = reader.readLine()) != null){
                
                line = line.trim();
                String[] values = line.split(" ", -1);
                if(values.length != nbCols){
                    throw new Exception("nb columns different from ncols header value");
                }
                for(int xIndex=0;xIndex<values.length;xIndex++){
                    
                    z = Float.valueOf(values[xIndex]);
                    if(z == noDataValue){
                        z = Float.NaN;
                    }
                    zArray[xIndex][yIndex] = z;
                    
                }
                
                yIndex++;
            }
            
            RegularDtm terrain = new RegularDtm(pathFile, zArray, xLeftCorner, yLeftCorner, step, nbCols, nbRows);
        
            return terrain;
            
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        
        return null;
    }
    
    public static ArrayList<Face> delaunaytriangulate(ArrayList<Vec3F> points){
        
        ArrayList<Face> faces = new ArrayList<>();
        Delaunay_Triangulation triangulation = new Delaunay_Triangulation();
        
        int count = 0;
        for(Vec3F pt : points){
            Point_dt point_dt = new Point_dt(pt.x, pt.z);
            point_dt.index = count;
            triangulation.insertPoint(point_dt);
            
            count++;
        }
        
        Iterator<Triangle_dt> trianglesIterator = triangulation.trianglesIterator();
        
        while(trianglesIterator.hasNext()){
            Triangle_dt triangle = trianglesIterator.next();
            if(triangle.p1() != null && triangle.p2() != null && triangle.p3() != null && !triangle.isHalfplane()){
                faces.add(new Face(triangle.p1().index, triangle.p2().index, triangle.p3().index));
            }
        }
        
        return faces;
    }
}
