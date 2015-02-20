/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.extraction;

/**
 *
 * @author Julien
 */
public class LasPoint {
    
    public double x, y, z;
    public short returnNumber;
    public short numberOfReturns;
    public double gpsTime;

    public LasPoint(double x, double y, double z, short returnNumber, short numberOfReturns, double gpsTime) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.returnNumber = returnNumber;
        this.numberOfReturns = numberOfReturns;
        this.gpsTime = gpsTime;
    }
    
}
