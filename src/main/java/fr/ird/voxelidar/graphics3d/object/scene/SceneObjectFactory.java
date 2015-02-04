/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.scene;

import fr.ird.voxelidar.graphics2d.texture.Texture;
import fr.ird.voxelidar.graphics3d.mesh.MeshFactory;
import fr.ird.voxelidar.math.vector.Vec3F;

/**
 *
 * @author Julien
 */
public class SceneObjectFactory {
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, int width, int height, Texture texture, int shaderId){
        
        SceneObject sceneObject = new SceneObject(MeshFactory.createPlaneFromTexture(startPoint, texture, width, height), shaderId, true);
        
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
}
