/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.als;

import fr.amap.amapvox.commons.util.ByteConverter;


/**
 * Represents the structure of a las point, with the following basics informations:<br>
 * x, y, z, echo range, echo number, recorded time (gps), intensity, classification
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
    public int classification;
    
    public LasPoint(int x, int y, int z, byte returnNumber, byte numberOfReturns, int intensity, byte classification, double gpsTime){

        this.x = x;
        this.y = y;
        this.z = z;
        
        this.i = ByteConverter.unsignedShortToInteger(intensity);
        this.r = ByteConverter.unsignedByteToShort(returnNumber);
        this.n = ByteConverter.unsignedByteToShort(numberOfReturns);
        
        this.classification = classification & 0x1f; //récupération des 5 lower bits
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

    /**
     * Compare gps time values
     * @param point point to compare
     */
    @Override
    public int compareTo(LasPoint point) {
        
        if(point.t > this.t){
            return -1;
        }else if(point.t < this.t){
            return 1;
        }else{
            return 0;
        }
    }
}