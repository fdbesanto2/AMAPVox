/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dart;

/**
 *
 * @author Julien
 */
public class Turbid {
    
    public float LAI;
    public int leafPhaseFunction;

    public Turbid(float LAI, int leafPhaseFunction) {
        this.LAI = LAI;
        this.leafPhaseFunction = leafPhaseFunction;
    }
    
}
