/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.geometry;

import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;

/**
 *
 * @author calcul
 */
public class Plane {
    
    private final float a;
    private final float b;
    private final float c;
    private final float d;
    
    private final Point3F point;
    
    public Plane(Vec3F u, Vec3F v, Point3F A){
        
        Vec3F normal = Vec3F.cross(u, v);
        Vec3F normalizedNormal = Vec3F.normalize(normal);
        a = normalizedNormal.x;
        b = normalizedNormal.y;
        c = normalizedNormal.z;
        d = -Vec3F.dot(new Vec3F(A.x, A.y, A.z), normalizedNormal);
        this.point = A;
    }
    
    public float getZFromXY(float x, float y){
        
        float z = ((-a*x)-(b*y)-d) / c;
        
        if(Float.isInfinite(z)){
            z = Float.NaN;
        }else if(Float.isNaN(z)){
            z = Float.POSITIVE_INFINITY;
        }
        
        return z;
    }
    
    public float getXFromYZ(float y, float z){
        return ((-b*y)-(c*z)-d) / a;
    }
    
    public float getYFromXZ(float x, float z){
        return ((-a*x)-(c*z)-d) / b;
    }
    
    public Vec3F getNormale(){
        return new Vec3F(a, b, c);
    }

    public Point3F getPoint() {
        return point;
    }
    
    public static void main(String[] args){
        
        Plane p = new Plane(new Vec3F(-17.21f, 8.95f, 0f), new Vec3F(-17.73f, -13.51f, 0f), new Point3F(0, 0, 4));
        float x = p.getXFromYZ(3, 4);
        float z = p.getZFromXY(3, 4);
        System.out.println(x);
    }
}
