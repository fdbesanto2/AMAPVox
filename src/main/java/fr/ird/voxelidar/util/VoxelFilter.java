/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Julien
 */
public class VoxelFilter {
    
    private ArrayList<Filter> filters;
    
    public VoxelFilter(){
        filters = new ArrayList<>();
    }
    
    public void addFilter(Filter filter){
        filters.add(filter);
    }
    
    public boolean doFilter(Map<String,Float> attributs){
        
        for(Filter filter : filters){
            
            float value = attributs.get(filter.getVariable());
            
            switch(filter.getCondition()){
                case Filter.EQUAL:
                    if(value != filter.getValue())return false;
                    break;
                case Filter.GREATER_THAN:
                    if(value <= filter.getValue())return false;
                    break;
                case Filter.GREATER_THAN_OR_EQUAL:
                    if(value < filter.getValue())return false;
                    break;
                case Filter.LESS_THAN:
                    if(value >= filter.getValue())return false;
                    break;
                case Filter.LESS_THAN_OR_EQUAL:
                    if(value > filter.getValue())return false;
                    break;
                case Filter.NOT_EQUAL:
                    if(value == filter.getValue())return false;
                    break;
            }
        }
        
        return true;
    }
}
