/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dtm;

import fr.ird.voxelidar.io.file.FileAdapter;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import fr.ird.voxelidar.util.delaunay.Delaunay_Triangulation;
import fr.ird.voxelidar.util.delaunay.Point_dt;
import fr.ird.voxelidar.util.delaunay.Triangle_dt;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            
            
            //ArrayList<Vec3F> points = new ArrayList<>();
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
    /*
    public static Dtm readFromAscFile(File ascFile, Mat4D transfMatrix) throws Exception{
        
        final String pathFile = ascFile.getAbsolutePath();
        
        
        String line;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ascFile))) {
            
            int nbCols = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            int nbRows = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float xLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float yLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float step = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float noDataValue = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            
            
            ArrayList<Vec3F> points = new ArrayList<>();
            
            float x, y, z;
            
            int yIndex = 0;
            float maxY = (nbRows-1)*step + yLeftCorner+ step/2.0f;
            
            while((line = reader.readLine()) != null){
                
                y = maxY - (yIndex-1)*step + yLeftCorner + step/2.0f;
                
                String[] values = line.split(" ");
                if(values.length != nbCols){
                    throw new Exception("nb columns different from ncols header value");
                }
                for(int xIndex=0;xIndex<values.length;xIndex++){
                    
                    x = (xIndex*step) + xLeftCorner+ step/2.0f;
                    z = Float.valueOf(values[xIndex]);
                    
                    if(z != noDataValue){
                        //Vec4D vec = new Vec4D(x, y, z, 1);
                        //Vec4D multiply = Mat4D.multiply(transfMatrix, vec);
                        
                        points.add(new Vec3F(x, z, y));
                        //points.add(new Vec3F((float)multiply.x, (float)multiply.y, (float)multiply.z));
                    }
                    
                }
                
                yIndex++;
            }
            
            ArrayList<Face> faces = DtmLoader.delaunaytriangulate(points);
            //ArrayList<Face> faces = TerrainLoader.triangulate(points);
            Dtm terrain = new Dtm(pathFile, points, faces);
        
            return terrain;
            
        } catch (IOException ex) {
            logger.error(ex.getMessage(), ex);
        }
        
        return null;
    }
    */
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
    
    //old triangulation method
    private static ArrayList<Face> triangulate(ArrayList<Vec3F> points){
        
        
        Collections.sort(points, new Comparator<Vec3F>() {

            @Override
            public int compare(Vec3F pt1, Vec3F pt2) {
                if (pt1.y < pt2.y) {
                    return 1;
                } else if (pt2.y > pt1.y) {
                    return -1;
                } else {
                    if (pt1.x < pt2.x) {
                        return 1;
                    } else if (pt2.x > pt1.x) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            }
        });
        
        float min, max;
        //on cherche la largeur d'une bande de terrain
        min = max = points.get(0).x;
        
        for(int i=1;i<points.size();i++){

            if(min > points.get(i).x){
                min = points.get(i).x;
            }
            
            if(max < points.get(i).x){
                max = points.get(i).x;
            }
        }
        
        //create faces list
        ArrayList<Face> faces = new ArrayList<>();
        
        //on regarde sur quel axe se fait l'ordre de parcours des points
        Vec3F point1 = points.get(0);
        Vec3F point2 = points.get(1);
        
        int largeur = 0;
        int longueur;
        
        if(point1.x != point2.x){
            
            //on cherche la largeur d'une bande de terrain
            /*
            for(int i=0;i<points.size();i++){
                
                if((points.get(i).x == points.get(0).x) && (i != 0)){
                    
                    largeur = i;
                    
                     //i = points.size()-1;
                }
            }*/
            largeur  = (int) Math.abs(max - min);
            
            longueur = points.size()/largeur;
            
            int nbTrianglesLargeur = largeur * 2 - 2;
            int nbTrianglesTotal = longueur * nbTrianglesLargeur - nbTrianglesLargeur;
            
            //on crée les faces
            int line = 1;
            for(int i=0;i<points.size();i++){
            
                if((i % (largeur)) == 0 && i>0){
                    line++;
                }
                
                int pt1 = i;
                int pt2 = i+1;
                if(pt2 < (largeur*line) && line<longueur){
                    
                    int pt3 = pt1+largeur;
                    int pt4 = pt2+largeur;
                    
                    Face triangle1 = new Face(pt1, pt2, pt3);
                    faces.add(triangle1);
                    
                    if(pt4 <= points.size() -1){
                        
                        Face triangle2 = new Face(pt2, pt3, pt4);
                        faces.add(triangle2);
                    } 
                }
                
            }
            
            if(nbTrianglesTotal != faces.size()){
                logger.info("dtm loader cannot create all triangles");
            }
        
            
        }else if(point1.y != point2.y){
            
        }else{
            logger.error("cannot reconstruct terrain from points, must be rectangular");
        }
        
        
        return faces;
    }
}