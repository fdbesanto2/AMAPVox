/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import fr.amap.amapvox.voxviewer.mesh.PointCloudGLMesh;

/**
 *
 * @author calcul
 */
public class PointCloudSceneObject extends SimpleSceneObject{

    public PointCloudSceneObject(PointCloudGLMesh mesh, boolean isAlphaRequired){
        super(mesh, isAlphaRequired);
    }
    
}
