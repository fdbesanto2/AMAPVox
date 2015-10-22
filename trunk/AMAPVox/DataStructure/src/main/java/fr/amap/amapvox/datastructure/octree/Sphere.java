/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.datastructure.octree;

import fr.amap.amapvox.math.point.Point3F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Sphere {
    
    private Point3F center;
    private float radius;
    
    public Sphere(){
        
        this.radius = 1.0f;
        this.center = new Point3F();
    }
    
    public Sphere(float radius){
        
        this.radius = radius;
        this.center = new Point3F();
    }
    
    public Sphere(Point3F center){
        
        this.radius = 1.0f;
        this.center = center;
    }
    
    public Sphere(Point3F center, float radius){
        
        this.radius = radius;
        this.center = center;
    }

    public Point3F getCenter() {
        return center;
    }

    public void setCenter(Point3F center) {
        this.center = center;
    }

    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public float distanceTo(Point3F target){
        return center.distanceTo(target);
    }
}
