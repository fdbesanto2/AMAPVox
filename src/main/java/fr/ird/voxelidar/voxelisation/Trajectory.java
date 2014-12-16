/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

/**
 *
 * @author Julien
 */
public class Trajectory {
    
    public double x;
    public double y;
    public double z;
    public double T;

    public Trajectory(double x, double y, double z, double t) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.T = t;
    }
    
    
}
