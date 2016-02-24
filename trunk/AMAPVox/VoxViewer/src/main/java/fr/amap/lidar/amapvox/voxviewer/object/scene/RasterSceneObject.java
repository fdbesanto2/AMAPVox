/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import java.nio.FloatBuffer;

/**
 *
 * @author calcul
 */
public class RasterSceneObject extends ScalarSceneObject{
    
    public RasterSceneObject(GLMesh mesh, boolean isAlphaRequired) {
        
        super(mesh, isAlphaRequired);
        
        getElevationsFromMesh(mesh);
        init();
    }
    
    private void getElevationsFromMesh(GLMesh mesh){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        if(vertexBuffer != null){
            
            for(int j = 0 ; j<vertexBuffer.capacity(); j+=3){
                float z = vertexBuffer.get(j+2);
                addValue("Elevation", z, true);
            }
        }
    }
    
    
}
