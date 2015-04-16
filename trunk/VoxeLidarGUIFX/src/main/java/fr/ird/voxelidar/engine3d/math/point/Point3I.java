/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.math.point;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Point3I {
    
    public int x, y, z;

    public Point3I() {
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }
    
    public Point3I(int x, int y, int z) {
        
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
