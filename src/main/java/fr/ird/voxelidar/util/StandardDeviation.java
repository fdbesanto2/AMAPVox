/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

/**
 *
 * @author Julien
 */
public class StandardDeviation {
    
    private float average;

    public float getAverage() {
        return average;
    }
    
    public float getFromFloatArray(float[] values){
        
        //average
        float sum = 0;
        for(int i=0;i<values.length;i++){
            
            sum+=values[i];
        }
        
        average = sum/(float)values.length;
        
        
        
        float sum2 = 0;
        for(int i=0;i<values.length;i++){
            
            sum2+=Math.pow(values[i]-average, 2);
        }
        
        
        
        return (float)(Math.sqrt((1/(float)values.length)* sum2));
    }
}
