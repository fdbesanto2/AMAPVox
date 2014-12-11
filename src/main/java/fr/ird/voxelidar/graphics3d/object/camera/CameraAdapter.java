/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.camera;

import fr.ird.voxelidar.math.matrix.Mat4F;
import fr.ird.voxelidar.math.vector.Vec3F;

/**
 *
 * @author Julien
 */
public abstract class CameraAdapter implements CameraListener{
    
    @Override
    public void locationChanged(Vec3F location){}
    
    @Override
    public void targetChanged(Vec3F target){}
    
    @Override
    public void viewMatrixChanged(Mat4F viewMatrix){}
    
    @Override
    public void projMatrixChanged(Mat4F projMatrix){}
}
