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

package fr.ird.voxelidar.octree;

import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.point.Point3I;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class Octree {
    
    private final static Logger logger = Logger.getLogger(Octree.class);
    
    private Point3F[] points;
    private Point3F minPoint;
    private Point3F maxPoint;
    private int depth;
    private Node root;
    private List<Node> leafs;
    
    private final int maximumPoints;
    
    public final static short BINARY_SEARCH = 0;
    public final static short INCREMENTAL_SEARCH = 1;
    
    public Octree(int maximumPoints){
        this.maximumPoints = maximumPoints;
        depth = 0;
    }
    
    public void loadPointsFromFile(File file){
        
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
                        logger.info("Point file contains more columns than necessary, parsing the three first");
                    }else if(split.length < 3){
                        logger.error("Point file doesn't contains valid columns!");
                        reader.close();
                        return;
                    }
                    
                    isInit = true;
                }
                
                String[] split = line.split(separator);
                if(split.length < 3){
                    logger.error("Error parsing line "+count);
                    reader.close();
                    return;
                }
                
                pointList.add(new Point3F(Float.valueOf(split[0]), Float.valueOf(split[1]), Float.valueOf(split[2])));
                count++;
            }
            
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (IOException ex) {
            logger.error(ex);
        }
        
        points = new Point3F[pointList.size()];
        
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
        minPoint = new Point3F(minPointX, minPointY, minPointZ);
        maxPoint = new Point3F(maxPointX, maxPointY, maxPointZ);
        
        pointList.toArray(points);
    }
    
    public void build(){
        
        if(points != null){
            
            root = new Node(minPoint, maxPoint);
            
            for(int i=0;i<points.length;i++){
                
                root.insertPoint(this, i);
            }
            
        }else{
            logger.warn("Attempt to build octree but points array is null");
        }
    }
    
    public List<Node> getLeafs(){
        
        leafs = new ArrayList<>();
        
        if(root != null){
            getChilds(root);
        }
        
        return leafs;
    }
    
    private void getChilds(Node node){
        
        if(node.hasChilds()){
            
            for(short i=0;i<8;i++){
                Node child = node.getChild(i);
                if(child.isLeaf()){
                    leafs.add(child);
                }else{
                    getChilds(child);
                }
            }

        }
    }
    
    public Node traverse(Point3F point){
        
        Node node = null;
        
        if(root != null){
            
            node = root;
            
            while(node.hasChilds()){
                short indice = node.getIndiceFromPoint(point);
                
                Node tmp = node.getChild(indice);
                if(tmp != null){
                    node = tmp;
                }else{
                    return null;
                }
                
            }
        }
        
        return node;
    }
    
    public Point3F searchNearestPoint(Point3F point, short type){
        
        switch(type){
            case BINARY_SEARCH:
                break;
            case INCREMENTAL_SEARCH:
                return incrementalSearchNearestPoint(point);
        }
        
        return null;
    }
    
    public boolean isPointBelongToPointcloud(Point3F point, float errorMargin){
        
        Point3F incrementalSearchNearestPoint = searchNearestPoint(point, Octree.INCREMENTAL_SEARCH);
                            
                            
        boolean test = false;
        if(incrementalSearchNearestPoint != null){
            float distance = point.distanceTo(incrementalSearchNearestPoint);

            if(distance < errorMargin){
                test = true;
            }
        }
        
        return test;
    }
    
    private Point3F incrementalSearchNearestPoint(Point3F point){
        
        Point3F nearestPoint = null;
        
        if(root != null){
            
            Node leaf = traverse(point);
            
            if(leaf == null){
                return null;
            }
            
            float distance = 99999999;
            
            int[] nearestPoints = leaf.getPoints();
            
            if(nearestPoints != null){
                
                for (int pointToTest : nearestPoints) {
                    
                    float dist = point.distanceTo(points[pointToTest]);
                    
                    if(dist < distance){
                        distance = dist;
                        nearestPoint = points[pointToTest];
                    }
                }
            }
            
        }
        
        return nearestPoint;
    }

    public int getMaximumPoints() {
        return maximumPoints;
    }

    public Point3F[] getPoints() {
        return points;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
    
}
