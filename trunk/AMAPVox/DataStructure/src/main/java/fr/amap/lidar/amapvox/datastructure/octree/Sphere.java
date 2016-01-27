/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.datastructure.octree;

import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Sphere {
    
    private Point3D center;
    private float radius;
    
    public Sphere(){
        
        this.radius = 1.0f;
        this.center = new Point3D();
    }
    
    public Sphere(float radius){
        
        this.radius = radius;
        this.center = new Point3D();
    }
    
    public Sphere(Point3D center){
        
        this.radius = 1.0f;
        this.center = center;
    }
    
    public Sphere(Point3D center, float radius){
        
        this.radius = radius;
        this.center = center;
    }

    public Point3D getCenter() {
        return center;
    }

    public void setCenter(Point3D center) {
        this.center = center;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public double distanceTo(Point3D target){
        return center.distanceTo(target);
    }
}
