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
public class TLSVoxel extends Voxel{
    
    public double bflEntering = 0;
    public double bflIntercepted = 0;
    public double PadBflTotal = 0;
    public double PadBflTotal_V2 = 0;
    
    public TLSVoxel(int i, int j, int k) {
        super(i, j, k);
    }
    
}
