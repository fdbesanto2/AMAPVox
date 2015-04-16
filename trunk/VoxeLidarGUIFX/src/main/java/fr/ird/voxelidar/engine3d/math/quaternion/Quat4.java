/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.math.quaternion;

import fr.ird.voxelidar.engine3d.math.vector.Vec3F;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Quat4 {
    
    public float w;
    public float x;
    public float y;
    public float z;
    
    public Quat4(){
        
        this.w = 0.0f;
        this.x = 0.0f;
        this.y = 0.0f;
        this.z = 0.0f;
    }
    
    public Quat4(float w, float x, float y, float z){
        
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public static Quat4 fromAxisAngle(Vec3F axis, float angle){
        
        Quat4 dest = new Quat4();
        
        dest.w = (float) Math.cos(angle/2);
        dest.x = (float) (Math.sin(angle/2)*Math.cos(axis.x));
        dest.y = (float) (Math.sin(angle/2)*Math.cos(axis.y));
        dest.z = (float) (Math.sin(angle/2)*Math.cos(axis.z));
        
        
        
        return dest;
    }
    
    /*
    public static Quat4 fromEuler(float x, float y, float z){
        
        Quat4 dest = new Quat4();
        
        double c1 = Math.cos(x / 2 );
        double c2 = Math.cos(y / 2 );
        double c3 = Math.cos(z / 2 );
        double s1 = Math.sin(x / 2 );
        double s2 = Math.sin(y / 2 );
        double s3 = Math.sin(z / 2 );
        
        dest.x = (float) (s1 * c2 * c3 + c1 * s2 * s3);
        dest.y = (float) (c1 * s2 * c3 - s1 * c2 * s3);
        dest.z = (float) (c1 * c2 * s3 + s1 * s2 * c3);
        dest.w = (float) (c1 * c2 * c3 - s1 * s2 * s3);
        
        
        dest.w = (float) ((Math.cos(x/2.0f)*Math.cos(y/2.0f)*Math.cos(z/2.0f))+(Math.sin(x/2.0f)*Math.sin(y/2.0f)*Math.sin(z/2.0f)));
        dest.x = (float) ((Math.sin(x/2.0f)*Math.cos(y/2.0f)*Math.cos(z/2.0f))-(Math.cos(x/2.0f)*Math.sin(y/2.0f)*Math.sin(z/2.0f)));
        dest.y = (float) ((Math.cos(x/2.0f)*Math.sin(y/2.0f)*Math.cos(z/2.0f))+(Math.sin(x/2.0f)*Math.cos(y/2.0f)*Math.sin(z/2.0f)));
        dest.z = (float) ((Math.cos(x/2.0f)*Math.cos(y/2.0f)*Math.sin(z/2.0f))-(Math.sin(x/2.0f)*Math.sin(y/2.0f)*Math.cos(z/2.0f)));
        
        
        return dest;
    }
    */
    
    /*
    public static Vec3 toEuler(Quat4 quaternion){
        
        Vec3 dest = new Vec3();
        
        dest.x = (float) Math.atan2(2*((quaternion.w*quaternion.x)+(quaternion.y*quaternion.z)), 1-2*(Math.pow(quaternion.x, 2)+Math.pow(quaternion.y, 2)));
        dest.y = (float) Math.asin(2*((quaternion.w*quaternion.y)-(quaternion.z*quaternion.x)));
        dest.z = (float) Math.atan2(2*((quaternion.w*quaternion.z)+(quaternion.x*quaternion.y)), 1-2*(Math.pow(quaternion.y, 2)+Math.pow(quaternion.z, 2)));
        
        return dest;
    }
    */
}
