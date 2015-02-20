/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import fr.ird.voxelidar.engine3d.loading.texture.Texture;
import fr.ird.voxelidar.engine3d.loading.mesh.MeshFactory;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;

/**
 *
 * @author Julien
 */
public class SceneObjectFactory {
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, int width, int height, Texture texture, int shaderId){
        
        SceneObject sceneObject = new SimpleSceneObject(MeshFactory.createPlaneFromTexture(startPoint, texture, width, height), shaderId, true);
        
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
}
