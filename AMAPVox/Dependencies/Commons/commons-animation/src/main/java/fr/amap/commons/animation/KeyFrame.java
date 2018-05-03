/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Julien Heurtebize
 */
public class KeyFrame {
    
    private final List<KeyValue> keyValues;

    public KeyFrame() {
        
        keyValues = new ArrayList<>();
    }
    
    public KeyFrame(KeyValue... values) {
        
        keyValues = new ArrayList<>(Arrays.asList(values));
    }
    
    public void addKeyValue(KeyValue keyValue){
        keyValues.add(keyValue);
    }
    
    public void removeKeyValue(KeyValue keyValue){
        keyValues.remove(keyValue);
    }

    public List<KeyValue> getKeyValues() {
        return keyValues;
    }
}
