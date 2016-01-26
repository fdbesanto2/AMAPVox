/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.quaternion;

import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class QuaternionUtility {
    
    
    public static Mat4F lookRotation(Point3F eye, Point3F target, Vec3F up){
                
        Vec3F eyeVec = new Vec3F(eye.x, eye.y, eye.z);
        Vec3F targetVec = new Vec3F(target.x, target.y, target.z);
        
        Vec3F f = Vec3F.normalize(Vec3F.substract(eyeVec, targetVec));
        Vec3F s = Vec3F.normalize(Vec3F.cross(up, f));
        Vec3F u = Vec3F.cross(f, s);
        
        Mat4F result = new Mat4F();
        
        result.mat = new float[]{
            s.x, u.x, f.x, 0,
            s.y, u.y, f.y, 0,
            s.z, u.z, f.z, 0,
            -Vec3F.dot(s, eyeVec), -Vec3F.dot(u, eyeVec), -Vec3F.dot(f, eyeVec), 1
        };
        /*
        Quaternion quaternion = new Quaternion();
        
        quaternion.setFromMatrix(s.x, u.x, f.x, s.y, u.y, f.y, s.z, u.z, f.z);
        
        float[] tab = new float[16];
        quaternion.toMatrix(tab, 0);
        
        if(tab[0] != s.x || tab[1] != u.x || tab[2] != f.x || 
            tab[3] != s.y || tab[4] != u.y || tab[5] != f.y ||
            tab[6] != s.z || tab[7] != u.z || tab[8] != f.z){
            
            //System.out.println("test");
        }*/
        /*
        result.mat = new float[]{
            tab[0], tab[1], tab[2], 0,
            tab[4], tab[5], tab[6], 0,
            tab[8], tab[9], tab[10], 0,
            -Vec3F.dot(s, eyeVec), -Vec3F.dot(u, eyeVec), -Vec3F.dot(f, eyeVec), 1
        };*/
        
        return result;
        
    }
    
}
