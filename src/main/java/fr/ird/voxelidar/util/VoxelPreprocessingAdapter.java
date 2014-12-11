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
public abstract class VoxelPreprocessingAdapter implements VoxelPreprocessingListener{

    @Override
    public void voxelPreprocessingStepProgress(String progress, int ratio) {}

    @Override
    public void voxelPreprocessingFinished() {}
    
}
