/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

/**
 *
 * @author calcul
 */
public interface ShotFilter {
    
    public boolean doFiltering(Shot shot);
}
