/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import javax.vecmath.Point3f;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien
 */
public class Shot {
    
    public int nbEchos;
    public Point3f origin;
    public Vector3f direction;
    public float ranges[];

    public Shot(int nbEchos, Point3f origin, Vector3f direction, float[] ranges) {
        
        this.origin = origin;
        this.nbEchos = nbEchos;
        this.direction = direction;
        this.ranges = ranges;
    }
    
    
}
