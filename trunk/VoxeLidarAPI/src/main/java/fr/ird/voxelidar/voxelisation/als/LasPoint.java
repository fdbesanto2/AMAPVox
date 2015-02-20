/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import javax.vecmath.Vector3d;

/**
 *
 * @author Julien
 */
public class LasPoint {
    
    public final static int CLASSIFICATION_CREATED_NEVER_CLASSIFIED = 0;
    public final static int CLASSIFICATION_UNCLASSIFIED = 1;
    public final static int CLASSIFICATION_GROUND = 2;
    public final static int CLASSIFICATION_LOW_VEGETATION = 3;
    public final static int CLASSIFICATION_MEDIUM_VEGETATION = 4;
    public final static int CLASSIFICATION_HIGH_VEGETATION = 5;
    public final static int CLASSIFICATION_BUILDING = 6;
    public final static int CLASSIFICATION_LOW_POINT = 7;
    public final static int CLASSIFICATION_MODEL_KEY_POINT = 8;
    public final static int CLASSIFICATION_WATER = 9;
    
    /**
     * las point location
     */
    public Vector3d location;

    /**
     * echo range
     */
    public int r;

    /**
     * echo number
     */
    public int n;

    /**
     * recorded time
     */
    public double t;

    /**
     * intensity
     */
    public int i;

    /**
     * classification (ground = 2, unclassified = 1)
     */
    public short classification;
    
    /**
     *
     * @param location
     * @param r
     * @param n
     * @param i
     * @param classification
     * @param t
     */
    public LasPoint(Vector3d location, int r, int n, int i, short classification, double t) {
        this.location = location;
        this.r = r;
        this.n = n;
        this.classification = classification;
        this.i = i;
        this.t = t;
    }    
}
