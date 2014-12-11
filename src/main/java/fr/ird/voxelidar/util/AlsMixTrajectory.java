/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

/**
 *
 * @author Julien
 */
public class AlsMixTrajectory {
    
    public Als als;
    
    public double xloc;
    public double yloc;
    public double zloc;
    
    public double xloc_s;
    public double yloc_s;
    public double zloc_s;
    
    public double x_u;
    public double y_u;
    public double z_u;
    
    public double range;

    public AlsMixTrajectory(Als als, double xloc_s, double yloc_s, double zloc_s) {
        this.als = als;
        this.xloc_s = xloc_s;
        this.yloc_s = yloc_s;
        this.zloc_s = zloc_s;
    }
    
    
    
}
