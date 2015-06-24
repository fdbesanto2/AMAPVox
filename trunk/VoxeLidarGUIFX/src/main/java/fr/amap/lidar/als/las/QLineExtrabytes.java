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
public class QLineExtrabytes{
    
    private int amplitude;
    private int pulseWidth;
    
    public QLineExtrabytes(byte[] bytes) {
        
        if(bytes.length == 2){
            amplitude = bytes[0];
            pulseWidth = bytes[1];
        }
    }

    public int getAmplitude() {
        return amplitude;
    }

    public int getPulseWidth() {
        return pulseWidth;
    }
}
