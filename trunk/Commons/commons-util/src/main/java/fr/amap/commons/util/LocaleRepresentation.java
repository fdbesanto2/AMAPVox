/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util;

/**
 *
 * @author Julien Heurtebize
 */
public class LocaleRepresentation implements Comparable<LocaleRepresentation>{
    
    private final String displayCountry;
    private final String displayLanguage;

    public LocaleRepresentation(String displayCountry, String displayLanguage) {
        this.displayCountry = displayCountry;
        this.displayLanguage = displayLanguage;
    }

    @Override
    public int compareTo(LocaleRepresentation o) {
        return displayCountry.compareTo(o.displayCountry);
    }

    public String getDisplayCountry() {
        return displayCountry;
    }

    public String getDisplayLanguage() {
        return displayLanguage;
    }
    
    
}
