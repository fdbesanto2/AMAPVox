/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Julien Heurtebize
 */
public class DateUtil {
    
    private final static ArrayList<String> DEFAULT_PATTERNS;
    
    static{
        
        DEFAULT_PATTERNS = new ArrayList<>();
        
        DEFAULT_PATTERNS.add("dd/MM/yyyy");
        DEFAULT_PATTERNS.add("dd/MM/yy");
        DEFAULT_PATTERNS.add("EEEE dd MMMM yyyy");
        DEFAULT_PATTERNS.add("dd MMM yyyy");
        DEFAULT_PATTERNS.add("dd MMM yy");
        DEFAULT_PATTERNS.add("dd/MM/yy hh:mm");
        DEFAULT_PATTERNS.add("dd/MM/yyyy hh:mm");
        DEFAULT_PATTERNS.add("dd/MM/yyyy hh:mm:ss");
        
        DEFAULT_PATTERNS.trimToSize();
    }

    public static List<String> getDefaultPatterns() {
        
        return Collections.unmodifiableList(DEFAULT_PATTERNS);
    }
}
