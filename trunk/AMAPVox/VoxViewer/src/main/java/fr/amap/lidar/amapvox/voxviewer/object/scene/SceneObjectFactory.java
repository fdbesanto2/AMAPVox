/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;
import java.io.File;
import org.apache.log4j.Logger;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class SceneObjectFactory {
    
    private final static Logger logger = Logger.getLogger(SceneObjectFactory.class);
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, int width, int height, Texture texture){
        
        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, width, height), true);
        
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, Texture texture, int shaderId){
        
        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, texture.getWidth(), texture.getHeight()), true);
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
    
    public static VoxelSpaceSceneObject createVoxelSpace(File voxelSpaceFile){
        
        VoxelSpaceSceneObject voxelSpace = new VoxelSpaceSceneObject(voxelSpaceFile);
        
        try {
            voxelSpace.load(null);
            
            return voxelSpace;
            
        } catch (Exception ex) {
            logger.error("Cannot load voxel space", ex);
        }
        
        return null;
    }
    
}
