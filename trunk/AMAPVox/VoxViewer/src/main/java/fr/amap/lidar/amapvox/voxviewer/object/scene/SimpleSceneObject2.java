/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import com.jogamp.opengl.GL3;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import java.io.File;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author calcul
 */
public class SimpleSceneObject2 extends SceneObject{
    
    public SimpleSceneObject2(GLMesh mesh, boolean isAlphaRequired){
        
        super(mesh, isAlphaRequired);
    }
    
    @Override
    public void initBuffers(GL3 gl){
        
        mesh.initBuffers(gl, GLMesh.DEFAULT_SIZE);
    }
    
    @Override
    public void initVao(GL3 gl){
        
        //generate vao
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenVertexArrays(1, tmp);
        vaoId = tmp.get(0);
        
        gl.glBindVertexArray(vaoId);
        
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, mesh.getVboId());
                gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);
                 
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.getIboId());
            
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
        gl.glBindVertexArray(0);
    }
    
    @Override
    public void draw(GL3 gl){
        
        gl.glBindVertexArray(vaoId);
        mesh.draw(gl, drawType);
        gl.glBindVertexArray(0);
    }

    @Override
    public void updateBuffers(GL3 gl, int index, FloatBuffer buffer) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void load(File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String doPicking(Point3F camPosition, Vec3F ray) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}