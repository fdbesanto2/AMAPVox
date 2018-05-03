/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.commons.util.Filter;
import fr.amap.lidar.amapvox.voxelisation.EchoFilter;
import fr.amap.lidar.amapvox.shot.Shot;
import java.util.List;

/**
 *
 * @author calcul
 */
public class RxpEchoFilter implements EchoFilter{
    
    private final List<Filter> echoFilters;

    public RxpEchoFilter(List<Filter> echoFilters) {

        this.echoFilters = echoFilters;
    }

    @Override
    public boolean doFiltering(Shot shot, int echoID) {
        
        /*if(echoFilters != null){
            
            for(Filter filter : echoFilters){
                
                switch(filter.getVariable()){
                    case "Reflectance":
                        
                        if(shot.reflectances != null){
                            return filter.doFilter(shot.reflectances[echoID]);
                        }
                        
                    case "Amplitude":
                        
                        if(shot.amplitudes != null){
                            return filter.doFilter(shot.amplitudes[echoID]);
                        }
                        
                    case "Deviation":
                        
                        if(shot.deviations != null){
                            return filter.doFilter(shot.deviations[echoID]);
                        }
                }
            }
            
        }*/
        
        return true;
    }
}
