/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

/**
 *
 * @author Julien
 */
public abstract class VoxelisationToolAdapter implements VoxelisationToolListener{

    @Override
    public void voxelisationProgress(String progress, int ratio) {}

    @Override
    public void voxelisationFinished(float duration) {}
    
}
