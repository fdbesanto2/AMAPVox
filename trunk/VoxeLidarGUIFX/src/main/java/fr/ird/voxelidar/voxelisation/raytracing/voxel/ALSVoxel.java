/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.raytracing.voxel;

/**
 *
 * @author calcul
 */
public class ALSVoxel extends Voxel{
    
    public double bvEntering = 0;
    public double bvIntercepted = 0;
    public double sumSurfaceMultiplyLength = 0;
    public double PadBVTotal = 0;
    public double PadBVTotal_V2 = 0;
        
    public ALSVoxel(int i, int j, int k) {
        super(i, j, k);
    }
}
