/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.chart;

import fr.ird.voxelidar.util.Filter;
import java.util.ArrayList;
import java.util.Map;

/**
 * This class represent a set of filters
 * @author Julien
 */
public class VoxelFilter {
    
    private final ArrayList<Filter> filters;
    
    /**
     *
     */
    public VoxelFilter(){
        filters = new ArrayList<>();
    }
    
    /**
     *
     * @param filter the filter to add
     */
    public void addFilter(Filter filter){
        filters.add(filter);
    }
    
    /**
     *
     * @param attributs Map which associate the attribute name (the key) 
     * and the attribute value
     * @return return true if all conditions ({@link fr.ird.voxelidar.util.Filter}) are respected and false 
     * if one condition is not
     */
    public boolean doFilter(ArrayList<String> attributsNames, Float[] attributs){
        
        for(Filter filter : filters){
            
            float value = attributs[attributsNames.indexOf(filter.getVariable())];
            
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
