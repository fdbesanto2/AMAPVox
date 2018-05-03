/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.geometry;

import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.vector.Vec3D;

/**
 *
 * @author calcul
 */
public class Distance {
    
    public static float getPointLineDistance(Point3D point, Point3D lineStart, Point3D lineEnd){
        
        //compute direction vector of the line
        Vec3D direction = new Vec3D(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y, lineEnd.z - lineStart.z);
        
        //apply formula
        Vec3D BA = new Vec3D(lineStart.x - point.x, lineStart.y - point.y, lineStart.z - point.z);
        
        
        return (float) (Vec3D.length(Vec3D.cross(BA, direction)) / Vec3D.length(direction));
    }
}
