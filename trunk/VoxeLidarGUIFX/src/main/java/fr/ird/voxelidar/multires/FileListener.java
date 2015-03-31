/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.multires;

import java.util.EventListener;

/**
 *
 * @author Julien
 */
public interface FileListener extends EventListener{
    
    void fileRead();
    
    
}
