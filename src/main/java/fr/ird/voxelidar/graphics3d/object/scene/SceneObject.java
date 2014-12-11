/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.scene;

import fr.ird.voxelidar.graphics2d.texture.Texture;
import fr.ird.voxelidar.graphics3d.mesh.Mesh;
import static fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace.FLOAT_SIZE;
import static fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace.SHORT_SIZE;
import fr.ird.voxelidar.graphics3d.shader.Shader;
import fr.ird.voxelidar.math.vector.Vec3F;
import java.nio.IntBuffer;
import javax.media.opengl.GL3;

/**
 *
 * @author Julien
 */
public class SceneObject {
    
    private final Mesh mesh;
    private int vboId, vaoId, iboId, shaderId, textureId;
    private int drawType;
    public boolean isAlphaRequired;
    public Texture texture;
    private int id;

    public void setId(int id) {
        this.id = id;
    }
    
    public int getShaderId() {
        return shaderId;
    }

    public int getTextureId() {
        return textureId;
    }
    
    

    public int getId() {
        return id;
    }
    public SceneObject(Mesh mesh, int shaderId, boolean isAlphaRequired){
        
        this.mesh = mesh;
        this.shaderId = shaderId;
        this.drawType = GL3.GL_TRIANGLES;
        this.isAlphaRequired = isAlphaRequired;
    }

    public void setDrawType(int drawType) {
        this.drawType = drawType;
    }

    public int getDrawType() {
        return drawType;
    }
    
    public void attachTexture(Texture texture){
        
        this.texture = texture;
        textureId = texture.getId();
    }
    
    public void translate(Vec3F position){
        
        for(int i=0;i<mesh.vertexBuffer.capacity();i++){
            
            float vertex = mesh.vertexBuffer.get(i);
            if(i%3 == 0){
                mesh.vertexBuffer.put(i, vertex+=position.y);
            }
            if(i%2 == 0){
                mesh.vertexBuffer.put(i, vertex+=position.z);
            }
            if(i%1 == 0){
                mesh.vertexBuffer.put(i, vertex+=position.x);
            }
        }
    }
    public void initBuffers(GL3 gl){
        
        IntBuffer tmp = IntBuffer.allocate(2);
        gl.glGenBuffers(2, tmp);
        vboId=tmp.get(0);
        iboId=tmp.get(1);
        
        
        long totalBuffersSize = 0;
        long offset = 0;
        
        if(mesh.colorBuffer != null){
            totalBuffersSize = mesh.vertexBuffer.capacity()*FLOAT_SIZE + mesh.colorBuffer.capacity()*FLOAT_SIZE;
        }else if(mesh.textureCoordinatesBuffer != null){
            totalBuffersSize = mesh.vertexBuffer.capacity()*FLOAT_SIZE + mesh.textureCoordinatesBuffer.capacity()*FLOAT_SIZE;
        }else{
            totalBuffersSize = mesh.vertexBuffer.capacity()*FLOAT_SIZE;
        }
        
        
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);
        
            //allocate total memory
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
            
            //vertex buffer
            gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, offset, mesh.vertexBuffer.capacity()*FLOAT_SIZE, mesh.vertexBuffer);
            
            //color buffer
            if(mesh.colorBuffer != null){
                offset += mesh.vertexBuffer.capacity()*FLOAT_SIZE;
                gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, offset, mesh.colorBuffer.capacity()*FLOAT_SIZE, mesh.colorBuffer);
                
            }else if(mesh.textureCoordinatesBuffer != null){
                offset += mesh.vertexBuffer.capacity()*FLOAT_SIZE;
                gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, offset, mesh.textureCoordinatesBuffer.capacity()*FLOAT_SIZE, mesh.textureCoordinatesBuffer);
            }
            
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
                gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.indexBuffer.capacity()*SHORT_SIZE, mesh.indexBuffer, GL3.GL_STATIC_DRAW);
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
    }
    
    public void initVao(GL3 gl, Shader shader){
        
        //generate vao
        IntBuffer tmp = IntBuffer.allocate(1);
        gl.glGenVertexArrays(1, tmp);
        vaoId = tmp.get(0);
        
        gl.glBindVertexArray(vaoId);
        
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);
                
                if(textureId != 0){
                    //gl.glActiveTexture(GL3.GL_TEXTURE0);
                    gl.glBindTexture(GL3.GL_TEXTURE_2D, textureId);
                }
            
                gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
                gl.glVertexAttribPointer(shader.attributeMap.get("position"), 3, GL3.GL_FLOAT, false, 0, 0);
                
                if(mesh.colorBuffer != null){
                    gl.glEnableVertexAttribArray(shader.attributeMap.get("color"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("color"), 3, GL3.GL_FLOAT, false, 0, mesh.vertexBuffer.capacity()*FLOAT_SIZE);
                }else if(mesh.textureCoordinatesBuffer != null){
                    gl.glEnableVertexAttribArray(shader.attributeMap.get("textureCoordinates"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("textureCoordinates"), 2, GL3.GL_FLOAT, false, 0, mesh.vertexBuffer.capacity()*FLOAT_SIZE);
                }
                
                if(textureId != -1){
                    gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
                }
                 
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
            
            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
            
        gl.glBindVertexArray(0);
    }
    
    public void draw(GL3 gl, int drawType){
        
        gl.glBindVertexArray(vaoId);
            if(texture != null){
                gl.glBindTexture(GL3.GL_TEXTURE_2D, textureId);
            }
                gl.glDrawElements(drawType, mesh.vertexCount, GL3.GL_UNSIGNED_SHORT, 0);

            if(texture != null){
                gl.glBindTexture(GL3.GL_TEXTURE_2D, 0);
            }
        gl.glBindVertexArray(0);
    }
}
