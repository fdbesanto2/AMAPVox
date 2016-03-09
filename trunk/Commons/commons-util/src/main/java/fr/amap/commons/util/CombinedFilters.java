
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.amap.commons.util;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class CombinedFilters {
    
    public Set<CombinedFilter> filters;
    
    public CombinedFilters(){
        filters = new HashSet<>();
    }
    
    public void addFilter(CombinedFilter filter) {
        filters.add(filter);
    }
    
    public void setFilters(Set<CombinedFilter> newFilters){
        
        if(newFilters != null){
            filters.clear();

            for(CombinedFilter f : newFilters){
                filters.add(f);
            }
        }
        
    }
    
    /**
     * Do filtering
     * @param value
     * @return true if the value is filtered, false otherwise
     */
    public boolean doFilter(float value){
        
        for(CombinedFilter filter : filters){
            
            if(filter.doFilter(value)){
                return true;
            }
        }
        
        return false;
    }
    
}
