/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.voxelidar.math.vector.Vec3D;

/**
 *
 * @author Julien
 */
public class LasPoint {
    
    public final static int CLASSIFICATION_GROUND = 2;
    
    public Vec3D location;
    public int r;
    public int n;
    public double t;
    public int i;
    public short classification;
    
    public LasPoint(Vec3D location, int r, int n, int i, short classification, double t) {
        this.location = location;
        this.r = r;
        this.n = n;
        this.classification = classification;
        this.i = i;
        this.t = t;
    }    
}
