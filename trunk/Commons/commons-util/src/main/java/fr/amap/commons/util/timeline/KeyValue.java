/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.timeline;

/**
 *
 * @author Julien Heurtebize
 */
public class KeyValue {
    
    private final String name;
    private Property value;

    public KeyValue(String name, Property value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public Property getValue() {
        return value;
    }

    public void setValue(Property value) {
        this.value = value;
    }
    
}
