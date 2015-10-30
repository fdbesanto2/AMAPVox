/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.opengl.GL3;
import fr.amap.amapvox.math.matrix.Mat4F;
import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.math.vector.Vec3F;
import fr.amap.amapvox.math.vector.Vec4F;
import fr.amap.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.amapvox.voxviewer.mesh.GLMesh.DrawType;
import java.io.File;
import java.nio.FloatBuffer;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class SceneObject{
    
    //public Mesh mesh ;
    protected GLMesh mesh;
    protected int vaoId = -1, textureId = -1;
    private DrawType drawType;
    public boolean isAlphaRequired;
    public boolean depthTest = true;
    public Texture texture;
    protected Shader shader;
    protected Point3F position;
    private int id;
    protected boolean colorNeedUpdate = false;

    public void setId(int id) {
        this.id = id;
    }
    
    public int getShaderId() {
        return shader.getProgramId();
    }

    public int getTextureId() {
        return textureId;
    }
    

    public int getId() {
        return id;
    }
    
    public SceneObject(){
        
    }
    
    public SceneObject(GLMesh mesh, boolean isAlphaRequired){
        
        this.mesh = mesh;
        this.drawType = DrawType.TRIANGLES;
        this.isAlphaRequired = isAlphaRequired;
    }

    public void setDrawType(DrawType drawType) {
        this.drawType = drawType;
        this.mesh.drawType = drawType;
    }

    public DrawType getDrawType() {
        return drawType;
    }

    public Shader getShader() {
        return shader;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
    }
    
    public void attachTexture(int textureId){
        
        this.textureId = textureId;
    }
    
    public void attachTexture(Texture texture){
        
        this.texture = texture;
        textureId = texture.getId();
    }

    public Point3F getPosition() {
        return position;
    }

    public void setPosition(Point3F position) {
        this.position = position;
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
            
                
            Vec4F result = Mat4F.multiply(rotation, new Vec4F(x, y, z, 1));
            mesh.vertexBuffer.put(j, result.x);
            mesh.vertexBuffer.put(j+1, result.y);
            mesh.vertexBuffer.put(j+2, result.z);
            
        }
    }
    
    public abstract void initBuffers(GL3 gl);
    
    public abstract void updateBuffers(GL3 gl, int index, FloatBuffer buffer);
    
    public abstract void initVao(GL3 gl);
    
    public abstract void draw(GL3 gl);

    public abstract void load(File file);
}
