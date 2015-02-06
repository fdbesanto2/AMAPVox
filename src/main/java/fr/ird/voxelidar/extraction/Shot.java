package fr.ird.voxelidar.extraction;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Julien
 */
public class Shot{
    
    public int nbEchos;
    
    public Point3d origin;
    public Vector3d direction;
    public double ranges[];
    
    public Shot() {
        
    }

    public Shot(int nbEchos, Point3d origin, Vector3d direction, double[] ranges) {
        
        this.origin = origin;
        this.nbEchos = nbEchos;
        this.direction = direction;
        this.ranges = ranges;
    }
    
    public Shot(int nbShots, double beam_origin_x, double beam_origin_y, double beam_origin_z, double beam_direction_x, double beam_direction_y, double beam_direction_z, double[] echos) {
        
        this.nbEchos = nbShots;
        this.origin = new Point3d((double)beam_origin_x, (double)beam_origin_y, (double)beam_origin_z);
        this.direction = new Vector3d((double)beam_direction_x, (double)beam_direction_y, (double)beam_direction_z);
        
        this.ranges = new double[echos.length];
        
        for(int i=0;i<echos.length;i++){
            this.ranges[i] = (double)echos[i];
        }
    }
    
}
