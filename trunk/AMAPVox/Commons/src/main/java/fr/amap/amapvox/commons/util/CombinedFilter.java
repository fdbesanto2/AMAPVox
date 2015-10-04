
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

package fr.amap.amapvox.commons.util;

import java.util.ArrayList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class CombinedFilter{
    
    public final static int AND = 0;
    public final static int OR = 1;
    
    private Filter filter1;
    private Filter filter2;
    private int type;
    
    public CombinedFilter(Filter filter1, Filter filter2, int type){
        
        this.filter1 = filter1;
        this.filter2 = filter2;
        this.type = type;
    }

    public boolean doFilter(float attribut) {
        
        if(filter2 == null){
            return filter1.doFilter(attribut);
        }
        
        switch(type){
            
            case CombinedFilter.AND:
                
                return filter1.doFilter(attribut) && filter2.doFilter(attribut);
                
            case CombinedFilter.OR:
                
                return filter1.doFilter(attribut) || filter2.doFilter(attribut);
                
            default:
                return false;
                
        }
        
    }
    
}
