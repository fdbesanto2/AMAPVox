package fr.amap.lidar.amapvox.commons;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */





/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ExtendedALSVoxel extends ALSVoxel {

    public float resolution;
    public float type; //0 = below ground, 1 = ground, 2 = below canopy, 3 = canopy, 4 = above canopy
    public float canopy_relative_layer;
    public float patch_type; //0 = none, 1 = mean value, 2 = higher resolution
    //public float transmittanceNorm;
    
    public ExtendedALSVoxel(int i, int j, int k, Class c) {
        
        super(i, j, k, c);
    }

}
