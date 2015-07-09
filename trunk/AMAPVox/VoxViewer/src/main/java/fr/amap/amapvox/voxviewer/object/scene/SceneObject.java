/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.opengl.GL3;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class SceneObject{
    
    //public Mesh mesh ;
    protected GLMesh mesh;
    protected int vaoId, shaderId, textureId;
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

    public void setShaderId(int shaderId) {
        this.shaderId = shaderId;
    }

    public int getTextureId() {
        return textureId;
    }
    

    public int getId() {
        return id;
    }
    
    public SceneObject(){
        
    }
    
    public SceneObject(GLMesh mesh, int shaderId, boolean isAlphaRequired){
        
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
    
    public abstract void initBuffers(GL3 gl);
    
    public abstract void initVao(GL3 gl, Shader shader);
    
    public abstract void draw(GL3 gl);

    
}
