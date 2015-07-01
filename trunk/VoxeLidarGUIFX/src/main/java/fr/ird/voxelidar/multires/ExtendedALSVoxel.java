/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.multires;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.ALSVoxel;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ExtendedALSVoxel extends ALSVoxel {

    public float resolution;
    public float transmittanceNorm;
    
    public ExtendedALSVoxel(int i, int j, int k, Class c) {
        
        super(i, j, k, c);
    }

}
