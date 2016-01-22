/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.camera;

import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.vector.Vec3F;
import java.util.EventListener;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public interface CameraListener extends EventListener {
    
    void locationChanged(Vec3F location);
    void targetChanged(Vec3F target);
    void viewMatrixChanged(Mat4F viewMatrix);
    void projMatrixChanged(Mat4F projMatrix);
}
