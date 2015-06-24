/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.als.las;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VLineExtrabytes{
    
    private int amplitude;
    private int reflectance;
    private int deviation;
    
    public VLineExtrabytes(byte[] bytes) {
        
        if(bytes.length == 3){
            amplitude = bytes[0];
            reflectance = bytes[1];
            deviation = bytes[2];
        }
    }

    public int getAmplitude() {
        return amplitude;
    }

    public int getReflectance() {
        return reflectance;
    }

    public int getDeviation() {
        return deviation;
    }
}
