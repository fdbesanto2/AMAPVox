/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.als;

import fr.amap.commons.util.Filter;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.lidar.amapvox.voxelisation.EchoFilter;
import java.util.List;

/**
 *
 * @author calcul
 */
public class LasEchoFilter implements EchoFilter{
    
    private final List<Filter> echoFilters;
    private final List<Integer> classifiedPointsToDiscard;
        
        
    public LasEchoFilter(List<Filter> echoFilters, List<Integer> classifiedPointsToDiscard) {

        this.echoFilters = echoFilters;
        this.classifiedPointsToDiscard = classifiedPointsToDiscard;
    }
    
    @Override
    public boolean doFiltering(Shot shot, int echoID) {
        
        if(shot.classifications != null && !classifiedPointsToDiscard.contains(shot.classifications[echoID])/*shot.classifications[echoID] != 2*/){
            return true;
        }else{
            return false;
        }
    }
    
}
