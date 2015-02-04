package fr.ird.voxelidar.extraction;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

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
    
    public Point3f origin;
    public Vector3f direction;
    public float ranges[];
    
    public Shot() {
        
    }

    public Shot(int nbEchos, Point3f origin, Vector3f direction, float[] ranges) {
        
        this.origin = origin;
        this.nbEchos = nbEchos;
        this.direction = direction;
        this.ranges = ranges;
    }
    
    public Shot(int nbShots, double beam_origin_x, double beam_origin_y, double beam_origin_z, double beam_direction_x, double beam_direction_y, double beam_direction_z, double[] echos) {
        
        this.nbEchos = nbShots;
        this.origin = new Point3f((float)beam_origin_x, (float)beam_origin_y, (float)beam_origin_z);
        this.direction = new Vector3f((float)beam_direction_x, (float)beam_direction_y, (float)beam_direction_z);
        
        this.ranges = new float[echos.length];
        
        for(int i=0;i<echos.length;i++){
            this.ranges[i] = (float)echos[i];
        }
    }
    
}
