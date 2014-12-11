/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.scene;

import fr.ird.voxelidar.graphics2d.texture.Texture;
import fr.ird.voxelidar.graphics3d.mesh.MeshFactory;

/**
 *
 * @author Julien
 */
public class SceneObjectFactory {
    
    public static SceneObject createTexturedPlane(Texture texture, int shaderId){
        
        SceneObject sceneObject = new SceneObject(MeshFactory.createMeshFromTexture(texture), shaderId, true);
        
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
}
