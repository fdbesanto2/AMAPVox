/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import java.util.EventListener;

/**
 *
 * @author Julien
 */
public interface VoxelisationToolListener extends EventListener{
    
    void voxelisationProgress(String progress, int ratio);
    void voxelisationFinished(float duration);
}
