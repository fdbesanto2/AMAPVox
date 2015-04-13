/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

/**
 *
 * @author Julien
 */
public class LasShot {
    
    public LasPoint lasPoint;
    
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

    public LasShot(LasPoint lasPoint, double xloc_s, double yloc_s, double zloc_s) {
        this.lasPoint = lasPoint;
        this.xloc_s = xloc_s;
        this.yloc_s = yloc_s;
        this.zloc_s = zloc_s;
    }
    
    
    
}
