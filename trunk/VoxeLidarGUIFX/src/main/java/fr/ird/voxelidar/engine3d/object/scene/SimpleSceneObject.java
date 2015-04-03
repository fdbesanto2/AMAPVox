/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import com.jogamp.opengl.GL3;
import fr.ird.voxelidar.engine3d.buffer.MeshBuffer;
import fr.ird.voxelidar.engine3d.loading.shader.Shader;
import fr.ird.voxelidar.engine3d.object.mesh.InstancedMesh;
import fr.ird.voxelidar.engine3d.object.mesh.Mesh;
import fr.ird.voxelidar.engine3d.object.mesh.TexturedMesh;
import static fr.ird.voxelidar.engine3d.object.scene.VoxelSpace.FLOAT_SIZE;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

/**
 *
 * @author Julien
 */
public class SimpleSceneObject extends SceneObject{
    
    public SimpleSceneObject(Mesh mesh, int shaderId, boolean isAlphaRequired){
        
        super(mesh, shaderId, isAlphaRequired);
    }
    
    @Override
    public void initBuffers(GL3 gl){
        
        buffer = new MeshBuffer(gl);
        
        if(mesh instanceof InstancedMesh){
            buffer.initBuffers(gl, mesh.indexBuffer, new FloatBuffer[]{mesh.vertexBuffer, 
                                    ((InstancedMesh)mesh).instancePositionsBuffer, 
                                    ((InstancedMesh)mesh).instanceColorsBuffer});
            
        }else if(mesh instanceof TexturedMesh){
            
            if(((TexturedMesh)mesh).textureCoordinatesBuffer != null){
                
                buffer.initBuffers(gl, mesh.indexBuffer, new FloatBuffer[]{mesh.vertexBuffer, 
                                    ((TexturedMesh)mesh).textureCoordinatesBuffer});
            }
            
        }else if(mesh.colorBuffer != null){
            
            buffer.initBuffers(gl, mesh.indexBuffer, new FloatBuffer[]{mesh.vertexBuffer, 
                                                                    mesh.colorBuffer, mesh.normalBuffer});
        }else{
             buffer.initBuffers(gl, mesh.indexBuffer, new FloatBuffer[]{mesh.vertexBuffer});
        }
    }
    
    @Override
    public void initVao(GL3 gl, Shader shader){
        
        //generate vao
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenVertexArrays(1, tmp);
        vaoId = tmp.get(0);
        
        gl.glBindVertexArray(vaoId);
        
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, buffer.getVboId());
                
                if(textureId != 0){
                    //gl.glActiveTexture(GL3.GL_TEXTURE0);
                    gl.glBindTexture(GL3.GL_TEXTURE_2D, textureId);
                }
            
                gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);
                
                if(mesh.colorBuffer != null){
                    gl.glEnableVertexAttribArray(shader.attributeMap.get("color"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("color"), 3, GL3.GL_FLOAT, false, 0, mesh.vertexBuffer.capacity()*FLOAT_SIZE);
                    
                    gl.glEnableVertexAttribArray(shader.attributeMap.get("normal"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("normal"), 3, GL3.GL_FLOAT, false, 0, mesh.vertexBuffer.capacity()*FLOAT_SIZE+mesh.normalBuffer.capacity()*FLOAT_SIZE);
                }else if(mesh instanceof TexturedMesh){
                    gl.glEnableVertexAttribArray(shader.attributeMap.get("textureCoordinates"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("textureCoordinates"), 2, GL3.GL_FLOAT, false, 0, mesh.vertexBuffer.capacity()*FLOAT_SIZE);
                }
                
                if(textureId != -1){
                    gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
                }
                 
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, buffer.getIboId());
            
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
        gl.glBindVertexArray(0);
    }
    
    @Override
    public void draw(GL3 gl, int drawType){
        
        gl.glBindVertexArray(vaoId);
            if(texture != null){
                gl.glBindTexture(GL3.GL_TEXTURE_2D, textureId);
            }
            
            if(mesh instanceof InstancedMesh){
                gl.glDrawElementsInstanced(drawType, mesh.vertexCount, GL3.GL_UNSIGNED_SHORT, 0, ((InstancedMesh)mesh).instanceNumber);
            }else{
                gl.glDrawElements(drawType, mesh.vertexCount, GL3.GL_UNSIGNED_SHORT, 0);
            }
            

            if(texture != null){
                gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
            }
        gl.glBindVertexArray(0);
    }
}
