/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.math.geometry;

import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.math.vector.Vec3F;

/**
 *
 * @author calcul
 */
public class Intersection {
    
    //(algorithme de smit)
    public static Point3F getIntersectionLineBoundingBox(Point3F startPoint, Point3F endPoint, BoundingBox3F boundingBox3F) {

        double tmin, tmax, tymin, tymax, tzmin, tzmax;

        Point3F[] bounds = new Point3F[]{boundingBox3F.min, boundingBox3F.max};

        Vec3F direction = Vec3F.createVec3FFromPoints(startPoint, endPoint);
        direction = Vec3F.normalize(direction);
        Vec3F invDirection = new Vec3F(1.0f/direction.x, 1.0f/direction.y, 1.0f/direction.z);
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

        
        
        Point3F point = new Point3F(
                startPoint.x * direction.x + (float)tmin,
                startPoint.y * direction.y + (float)tmin,
                startPoint.z * direction.z + (float)tmin);

        return point;
    }
}
