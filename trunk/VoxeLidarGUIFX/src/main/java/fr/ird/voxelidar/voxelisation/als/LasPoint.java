/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.voxelidar.util.ByteConverter;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasPoint implements Comparable<LasPoint>{
    
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
    
    public double x;
    public double y;
    public double z;

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
     * @param x
     * @param y
     * @param z
     * @param returnNumber
     * @param numberOfReturns
     * @param intensity
     * @param gpsTime
     */
    
    public LasPoint(int x, int y, int z, byte returnNumber, byte numberOfReturns, int intensity, double gpsTime){

        this.x = x;
        this.y = y;
        this.z = z;
        
        this.i = ByteConverter.unsignedShortToInteger(intensity);
        this.r = ByteConverter.unsignedByteToShort(returnNumber);
        this.n = ByteConverter.unsignedByteToShort(numberOfReturns);
        this.t = gpsTime;
    }    
        
    public LasPoint(double x, double y, double z, int r, int n, int i, short classification, double t) {
        
        this.x = x;
        this.y = y;
        this.z = z;
        
        this.r = r;
        this.n = n;
        this.classification = classification;
        this.i = i;
        this.t = t;
    }    

    @Override
    public int compareTo(LasPoint o) {
        
        if(o.t > this.t){
            return -1;
        }else if(o.t < this.t){
            return 1;
        }else{
            return 0;
        }
    }
}
