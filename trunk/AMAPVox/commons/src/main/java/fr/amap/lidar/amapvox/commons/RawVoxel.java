/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.commons;

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
    
    @Override
    public String toString(){
        
        String result = $i + " " + $j + " " +$k;
        
        for(float f : attributs){
            result += " " + f;
        }
        
        return result;
    }
}
