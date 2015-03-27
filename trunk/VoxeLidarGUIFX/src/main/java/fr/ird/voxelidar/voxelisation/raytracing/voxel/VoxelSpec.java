/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.raytracing.voxel;

import javax.vecmath.Point3i;

/**
 *
 * @author calcul
 */
public class VoxelSpec {
    
    public Point3i indice;
    
    public double d2;
    public double d1;
    
    public VoxelSpec(int i, int j, int k){
        indice = new Point3i(i, j, k);
    }
}
