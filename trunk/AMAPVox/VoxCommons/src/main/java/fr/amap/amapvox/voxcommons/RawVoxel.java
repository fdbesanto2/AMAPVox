/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxcommons;

/**
 *
 * @author calcul
 */
public class RawVoxel {
        
    public int $i;
    public int $j;
    public int $k;
    
    public float[] attributs;

    public RawVoxel(){

    }
    
    public RawVoxel(int $i, int $j, int $k) {
        this.$i = $i;
        this.$j = $j;
        this.$k = $k;
    }
    
}
