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
public class Intersection {
    
    //(algorithme de smit)
    public static Point3D getIntersectionLineBoundingBox(Point3D startPoint, Point3D endPoint, BoundingBox3D boundingBox3D) {

        double tmin, tmax, tymin, tymax, tzmin, tzmax;

        Point3D[] bounds = new Point3D[]{boundingBox3D.min, boundingBox3D.max};

        Vec3D direction = Vec3D.createVec3DFromPoints(startPoint, endPoint);
        direction = Vec3D.normalize(direction);
        Vec3D invDirection = new Vec3D(1.0/direction.x, 1.0/direction.y, 1.0/direction.z);
        int sign[] = new int[]{(invDirection.x < 0)? 1 : 0, (invDirection.y < 0)? 1 : 0 , (invDirection.z < 0)? 1 : 0};

        tmin = (bounds[sign[0]].x - startPoint.x) * invDirection.x;
        tmax = (bounds[1 - sign[0]].x - startPoint.x) * invDirection.x;
        tymin = (bounds[sign[1]].y - startPoint.y) * invDirection.y;
        tymax = (bounds[1 - sign[1]].y - startPoint.y) * invDirection.y;

        if ((tmin > tymax) || (tymin > tmax)) {
            return null;
        }
        if (tymin > tmin || Double.isNaN(tmin)) {
            tmin = tymin;
        }
        if (tymax < tmax || Double.isNaN(tmax)) {
            tmax = tymax;
        }

        tzmin = (bounds[sign[2]].z - startPoint.z) * invDirection.z;
        tzmax = (bounds[1 - sign[2]].z - startPoint.z) * invDirection.z;

        if ((tmin > tzmax) || (tzmin > tmax)) {
            return null;
        }
        if (tzmin > tmin || Double.isNaN(tmin)) {
            tmin = tzmin;
        }
        if (tzmax < tmax || Double.isNaN(tmax)) {
            tmax = tzmax;
        }

        if(tmax<tmin){
            System.out.println("test");
        }

        
        
        Point3D point = new Point3D(
                startPoint.x * direction.x + (float)tmin,
                startPoint.y * direction.y + (float)tmin,
                startPoint.z * direction.z + (float)tmin);

        return point;
    }
}
