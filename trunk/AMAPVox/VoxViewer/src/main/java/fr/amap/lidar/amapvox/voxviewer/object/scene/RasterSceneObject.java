/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.structure.octree.Octree;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import java.nio.FloatBuffer;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class RasterSceneObject extends ScalarSceneObject{
    
    private final static Logger LOGGER = Logger.getLogger(RasterSceneObject.class);
    
    private Octree octree;
    
    public RasterSceneObject(GLMesh mesh, boolean isAlphaRequired) {
        
        super(mesh, isAlphaRequired);
        
        getElevationsFromMesh(mesh);
        init();
    }
    
    private void initOctree(){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        if(vertexBuffer != null){
            
            octree = new Octree(50);
            
            float[] array = new float[vertexBuffer.capacity()];

            for(int i = 0; i<vertexBuffer.capacity(); i++){
                
                array[i] = vertexBuffer.get(i);
            }

            octree.setPoints(array);
            try {
                octree.build();
            } catch (Exception ex) {
                LOGGER.error("The octree build failed.");
            }
        }
        
    }
    
    public Point3F getVertex(int index){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        if(vertexBuffer != null){
            
            int x = index * 3;
            int y = x+1;
            int z = x+2;
            
            if(z < vertexBuffer.capacity()){
                return new Point3F(vertexBuffer.get(x), vertexBuffer.get(y), vertexBuffer.get(z));
            }            
        }
        
        return null;
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
    
    /**
     * When picking a raster scene object, the element returned is the nearest point to the ray.
     * @param mousePicker The current mouse picker.
     * @return The nearest point or null if elements were not closed enough
     */
    @Override
    public Integer doPicking(MousePicker mousePicker) {
        
        
        Point3F startPoint = MousePicker.getPointOnray(mousePicker.getCamPosition(), mousePicker.getCurrentRay(), 0);
        Point3F endPoint = MousePicker.getPointOnray(mousePicker.getCamPosition(), mousePicker.getCurrentRay(), 600);
        
        int closestElement = octree.getClosestElement(new Point3D(startPoint.x, startPoint.y, startPoint.z),
                new Point3D(endPoint.x, endPoint.y, endPoint.z), 1.0f);
        
        if(closestElement > 0){
            
            return closestElement;
        }
        
        return null;
    }
    
    @Override
    public void setMousePickable(boolean isPickable){
        
        super.setMousePickable(isPickable);
        
        if(isPickable){
            initOctree();
        }
    }
    
    
}
