/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.extraction;

import java.util.EventListener;

/**
 *
 * @author Julien
 */
public interface LasExtractionListener extends EventListener{
    
    void isFinished();
}
