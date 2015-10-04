/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.math.geometry;

import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.util.BoundingBox3F;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author calcul
 */
public class AABB {
    
    private final Point3F[] points;
    private final int[][] faces;
    
    private final BoundingBox3F boundingBox3F;

    public AABB(BoundingBox3F boundingBox) {
        
        this.boundingBox3F = boundingBox;
        
        this.points = new Point3F[8];
        
        this.points[0] = new Point3F(boundingBox.min.x, boundingBox.min.y, boundingBox.min.z);
        this.points[1] = new Point3F(boundingBox.min.x, boundingBox.min.y, boundingBox.max.z);
        this.points[2] = new Point3F(boundingBox.min.x, boundingBox.max.y, boundingBox.min.z);
        this.points[3] = new Point3F(boundingBox.min.x, boundingBox.max.y, boundingBox.max.z);
        this.points[4] = new Point3F(boundingBox.max.x, boundingBox.min.y, boundingBox.min.z);
        this.points[5] = new Point3F(boundingBox.max.x, boundingBox.min.y, boundingBox.max.z);
        this.points[6] = new Point3F(boundingBox.max.x, boundingBox.max.y, boundingBox.min.z);
        this.points[7] = new Point3F(boundingBox.max.x, boundingBox.max.y, boundingBox.max.z);
        
        faces = new int[6][];
        
        faces[0] = new int[]{0, 1, 4, 5}; //front face
        faces[1] = new int[]{2, 3, 6, 7}; //back face
        faces[2] = new int[]{4, 5, 6, 7}; //right face
        faces[3] = new int[]{0, 1, 2, 3}; //left face
        faces[4] = new int[]{1, 3, 5, 7}; //top face
        faces[5] = new int[]{0, 2, 4, 6}; //bottom face
    }
    
    private boolean intoRange(float rangeElement1, float rangeElement2, float value){
        
        if(rangeElement1 > rangeElement2){
            return (value >= rangeElement2 && value <= rangeElement1);
        }else{
            return (value <= rangeElement2 && value >= rangeElement1);
        }
    }
    
    public List<Point3F> getIntersectionWithPlane(Plane plane){
        
        //on teste l'intersection du plan avec chaque face de AABB
        List<Point3F> intersections = new ArrayList<>();
        
        for(int[] face : faces){
            
            for(int i=0;i<4;i++){
                
                int indice = i;
                
                Point3F point1 = points[face[indice]];
                
                if(indice +1 == 4){
                    indice = 0;
                }
                
                Point3F point2 = points[face[indice+1]];

                if(point1.x != point2.x){ // x est la variable

                    float y = point1.y;
                    float z = point1.z;

                    float x = plane.getXFromYZ(y, z);
                    if(intoRange(point1.x, point2.x, x)){ //intersection
                        intersections.add(new Point3F(x, y, z));
                    }

                }else if(point1.y != point2.y){ // y est la variable
                    
                    float x = point1.x;
                    float z = point1.z;

                    float y = plane.getYFromXZ(x, z);
                    if(intoRange(point1.y, point2.y, y)){ //intersection
                        intersections.add(new Point3F(x, y, z));
                    }

                }else if(point1.z != point2.z){
                    
                    float x = point1.x;
                    float y = point1.y;

                    float z = plane.getZFromXY(x, y);
                    if(intoRange(point1.z, point2.z, z)){ //intersection
                        intersections.add(new Point3F(x, y, z));
                    }
                }
            }
        }
        
        return intersections;
    }
    
    public Point3F getNearestPoint(Point3F point){
        
        Point3F nearestPoint = new Point3F();
        float minDistance = 999999999;
        
        int indice = -1;
        
        for(int i=0;i<points.length;i++){
            
            Point3F corner = points[i];
            
            float currentDistance = corner.distanceTo(point);
            
            if(currentDistance < minDistance){
                minDistance = currentDistance;
                indice = i;
            }
        }
        
        return points[indice];
    }
}
