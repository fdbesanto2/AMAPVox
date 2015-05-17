
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.ird.voxelidar.voxelisation;

import fr.ird.voxelidar.engine3d.math.point.Point3F;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.vecmath.Point3f;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */


public class PointCloud {
    
    private final static Logger logger = Logger.getLogger(PointCloud.class);
    
    public List<Point3F> points;
    private Point3F min;
    private Point3F max;
    
     public PointCloud(){
        points = new ArrayList<>();
    }
    
    public boolean isPointInsidePointCloud(Point3F point, float maxDistance){
        
        int index = nearestPoint(point, maxDistance);
        if(index < 0){
            return false;
        }else{
            return true;
        }
        
    }
    
    public int nearestPoint(Point3F point, float maxDistance){
        int low = 0;
        int high = points.size()-1;

        while (low <= high) {
            int mid = (low + high) >>> 1;
            Comparable<Point3F> midVal = points.get(mid);
            int cmp = midVal.compareTo(point);

            if (cmp < 0){
                low = mid + 1;
            }else if (cmp > 0){
                high = mid - 1;
            }else{
                return mid; // key found
            }
        }
        
        if((low < points.size()) && (point.distanceTo(points.get(low)) < maxDistance)){
            return low;
        }else if(high >= 0 && (point.distanceTo(points.get(high)) < maxDistance)){
            return high;
        }
        
        return -(low + 1);  // key not found
        //return Collections.binarySearch(points, point);
    }
    
    public void readFromFile(File file){
        
        try {
            
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                points = new ArrayList<>();
                String line;
                while((line = reader.readLine()) != null){
                    String[] split = line.split(",");
                    /*
                    Point3f point = new Point3f(Math.round(Float.valueOf(split[0])*1000)/1000.0f,
                                                    Math.round(Float.valueOf(split[1])*1000)/1000.0f,
                                                    Math.round(Float.valueOf(split[2])*1000)/1000.0f);
                    */
                    Point3F point = new Point3F(Float.valueOf(split[0]), Float.valueOf(split[1]), Float.valueOf(split[2]));
                    points.add(point);
                }
            }
            
            long startTime = System.currentTimeMillis();
            Collections.sort(points);
            long endTime = System.currentTimeMillis();
            
            if(points != null && points.size()>0){
                min = points.get(0);
                max = points.get(points.size()-1);
            }
            
            System.out.println("temps de tri: "+((endTime-startTime)*Math.pow(10, -3)));
            
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
}
