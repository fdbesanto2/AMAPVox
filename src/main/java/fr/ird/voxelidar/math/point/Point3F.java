/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.math.point;

/**
 *
 * @author Julien
 */
public class Point3F {
    
    public float x, y, z;

    public Point3F() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public Point3F(float x, float y, float z) {
        
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
