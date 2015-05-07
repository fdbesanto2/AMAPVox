/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public interface FilterInterface {
    
    public static Set<Filter> filters = new HashSet<>();
    public void addFilter(Filter filter);
    public boolean doFilter(List<String> attributsNames, float[] attributs);
    public boolean doFilter(List<String> attributsNames, float attribut);
}
