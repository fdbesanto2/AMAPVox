/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation;

import fr.amap.amapvox.io.tls.rxp.Shot;

/**
 *
 * @author calcul
 */
public interface EchoFilter {
    
    public boolean doFiltering(Shot shot, int echoID);
}
