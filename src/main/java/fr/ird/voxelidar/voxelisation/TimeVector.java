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
public class TimeVector {
    
    public double x;
    public boolean isGpsTime;
    public double z;
    
    public TimeVector(double x, boolean isGpsTime, double z){
        
        this.x = x;
        this.isGpsTime = isGpsTime;
        this.z = z;
    }
}
