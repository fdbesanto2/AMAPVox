/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.multires;


/**
 *
 * @author calcul
 */
public class ExtendedTLSVoxel extends TLSVoxel {

    public double resolution;

    public ExtendedTLSVoxel(int i, int j, int k) {
        
        super(i, j, k);
        
        _fields = Voxel.getFields(this.getClass());
    }

}
