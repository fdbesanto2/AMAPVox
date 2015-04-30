/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.multires;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.ALSVoxel;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ExtendedALSVoxel extends ALSVoxel {

    public double resolution;
    
    public ExtendedALSVoxel(int i, int j, int k) {
        
        super(i, j, k);
    }
    
    public ExtendedALSVoxel(ALSVoxel voxel){
        
               
        super(voxel.$i, voxel.$j, voxel.$k);
        
        this.PadBVTotal = voxel.PadBVTotal;
        this._position = voxel._position;
        this._sum_li = voxel._sum_li;
        this.angleMean = voxel.angleMean;
        this.bvEntering = voxel.bvEntering;
        this.bvIntercepted = voxel.bvIntercepted;
        this.ground_distance = voxel.ground_distance;
        this.lMeanTotal = voxel.lMeanTotal;
        this.lgTotal = voxel.lgTotal;
        this.nbEchos = voxel.nbEchos;
        this.nbSampling = voxel.nbSampling;
        this.transmittance = voxel.transmittance;
        this._transmittance_v2 = voxel._transmittance_v2;
    }

}
