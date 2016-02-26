/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.math.vector.Vec3F;
import java.util.EventListener;

/**
 *
 * @author calcul
 */
public interface SceneObjectListener extends EventListener{
    
    void clicked(SceneObject sceneObject, MousePicker mousePicker);
}
