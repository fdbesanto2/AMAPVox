/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import java.util.List;
import java.util.Set;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class SimpleFilter implements FilterInterface{

    @Override
    public void addFilter(Filter filter) {
        filters.add(filter);
    }
    
    public void setFilters(Set<Filter> newFilters){
        
        if(newFilters != null){
            filters.clear();

            for(Filter f : newFilters){
                filters.add(f);
            }
        }
        
    }

    @Override
    public boolean doFilter(List<String> attributsNames, float[] attributs) {
        
        return false;
    }

    @Override
    public boolean doFilter(List<String> attributsNames, float attribut) {
        
        for(Filter filter : filters){
            
            float value = attribut;
            
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
    
    public boolean doOrFilter(float attribut) {
        
        for(Filter filter : filters){
            
            float value = attribut;
            
            switch(filter.getCondition()){
                case Filter.EQUAL:
                    if(value == filter.getValue())return true;
                    break;
                case Filter.GREATER_THAN:
                    if(value > filter.getValue())return true;
                    break;
                case Filter.GREATER_THAN_OR_EQUAL:
                    if(value >= filter.getValue())return true;
                    break;
                case Filter.LESS_THAN:
                    if(value < filter.getValue())return true;
                    break;
                case Filter.LESS_THAN_OR_EQUAL:
                    if(value <= filter.getValue())return true;
                    break;
                case Filter.NOT_EQUAL:
                    if(value != filter.getValue())return true;
                    break;
            }
        }
        
        return false;
    }
    
    
}
