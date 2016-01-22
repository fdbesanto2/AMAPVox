/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.camera;

import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.vector.Vec3F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
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
