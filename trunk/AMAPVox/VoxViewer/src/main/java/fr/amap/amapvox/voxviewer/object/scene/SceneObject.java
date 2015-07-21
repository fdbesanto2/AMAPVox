/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.opengl.GL3;
import fr.amap.amapvox.commons.math.matrix.Mat4F;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.commons.math.vector.Vec4;
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
    public boolean depthTest;
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
        this.depthTest = true;
    }

    public void setDrawType(int drawType) {
        this.drawType = drawType;
        this.mesh.drawType = drawType;
    }

    public int getDrawType() {
        return drawType;
    }
    
    public void attachTexture(Texture texture){
        
        this.texture = texture;
        textureId = texture.getId();
    }
    
    public void translate(Vec3F translation){
        
        for(int j = 0 ; j<mesh.vertexBuffer.capacity(); j+=3){
            
            
            float x = mesh.vertexBuffer.get(j);
            float y = mesh.vertexBuffer.get(j+1);
            float z = mesh.vertexBuffer.get(j+2);
            
            mesh.vertexBuffer.put(j, x+translation.x);
            mesh.vertexBuffer.put(j+1, y+translation.y);
            mesh.vertexBuffer.put(j+2, z+translation.z);
            
        }
    }
    
    public void rotate(Mat4F rotation){
        
        for(int j = 0 ; j<mesh.vertexBuffer.capacity(); j+=3){
            
            
            float x = mesh.vertexBuffer.get(j);
            float y = mesh.vertexBuffer.get(j+1);
            float z = mesh.vertexBuffer.get(j+2);
            
                
            Vec4 result = Mat4F.multiply(rotation, new Vec4(x, y, z, 1));
            mesh.vertexBuffer.put(j, result.x);
            mesh.vertexBuffer.put(j+1, result.y);
            mesh.vertexBuffer.put(j+2, result.z);
            
        }
    }
    
    public abstract void initBuffers(GL3 gl);
    
    public abstract void initVao(GL3 gl, Shader shader);
    
    public abstract void draw(GL3 gl);

    
}
