/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.camera;

import fr.ird.voxelidar.engine3d.math.matrix.Mat4F;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
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
