/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.extraction;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class JNIHelper {
    
    public static String getFullyQualifiedClassName(Object obj){
        
        return obj.getClass().getName().replace('.', '/');
    }
}
