/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util;

import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Julien Heurtebize
 */
public class LocaleUtil {
    
    public final static TreeMap<String, TreeSet<LocaleRepresentation>> AVAILABLE_LOCALES;    
    
    static{
        
        Locale[] availableLocales = Locale.getAvailableLocales();
        
        AVAILABLE_LOCALES = new TreeMap<>();
        
        for(Locale locale : availableLocales){
            String displayLanguage = locale.getDisplayCountry();
            if(!displayLanguage.isEmpty()){
                
                if(!AVAILABLE_LOCALES.containsKey(locale.getDisplayLanguage())){
                    AVAILABLE_LOCALES.put(locale.getDisplayLanguage(), new TreeSet<>());
                }
                
                AVAILABLE_LOCALES.get(locale.getDisplayLanguage()).add(new LocaleRepresentation(locale.getDisplayCountry(), locale.getDisplayLanguage()));
            }
        }
    }    
    
    public static Map<String, TreeSet<LocaleRepresentation>> getAvailableLocales() {
        return Collections.unmodifiableMap(AVAILABLE_LOCALES);
    }
    
    
}
