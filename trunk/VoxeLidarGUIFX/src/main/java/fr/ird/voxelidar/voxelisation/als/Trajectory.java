/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Trajectory {
    
    public double x;
    public double y;
    public double z;
    public double t;

    public Trajectory(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Trajectory(double x, double y, double z, double t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.t = t;
    }
    
    
}
