/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import static java.lang.Double.NaN;

/**
 *
 * @author Julien
 */
public class Shoot {
    
    public int n;
    public double xloc_s;
    public double yloc_s;
    public double zloc_s;
    public double x_u;
    public double y_u;
    public double z_u;
    public double r1;
    public double r2;
    public double r3;
    public double r4;
    public double r5;
    public double r6;
    public double r7;

    public Shoot(int n, double xloc_s, double yloc_s, double zloc_s, double x_u, double y_u, double z_u) {
        this.n = n;
        this.xloc_s = xloc_s;
        this.yloc_s = yloc_s;
        this.zloc_s = zloc_s;
        this.x_u = x_u;
        this.y_u = y_u;
        this.z_u = z_u;
        
        this.r1 = NaN;
        this.r2 = NaN;
        this.r3 = NaN;
        this.r4 = NaN;
        this.r5 = NaN;
        this.r6 = NaN;
        this.r7 = NaN;
    }
    
    
}
