/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class SceneObjectFactory {
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, int width, int height, Texture texture, int shaderId){
        
        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, width, height), shaderId, true);
        
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, Texture texture, int shaderId){
        
        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, texture.getWidth(), texture.getHeight()), shaderId, true);
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
}
