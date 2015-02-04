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
public class TimeCounter {
    
    public static String getElapsedTimeInSeconds(long startTime){
        
        return String.valueOf((float)(Math.round((System.currentTimeMillis() - startTime)*(Math.pow(10, -3))*100)/100))+" s";
    }
}
