/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.util.timeline;

/**
 *
 * @author Julien Heurtebize
 * @param <T>
 */
public abstract class Property<T> implements Interpolable<T>{
       
    protected T value;
    
    public Property(T value){
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}
