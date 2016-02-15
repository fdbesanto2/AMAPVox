/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import com.jogamp.opengl.GL3;
import fr.amap.commons.math.geometry.BoundingBox3D;
import fr.amap.commons.math.geometry.BoundingBox3F;
import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.math.vector.Vec4F;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh.DrawType;
import java.io.File;
import java.nio.FloatBuffer;
import javax.swing.event.EventListenerList;

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
    protected Point3F gravityCenter;
    private int id;
    protected boolean colorNeedUpdate = false;
    protected boolean mousePickable;
    protected MousePicker mousePicker;
    
    private final EventListenerList listeners = new EventListenerList();

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
        this.gravityCenter = mesh.getGravityCenter();
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

    public Point3F getGravityCenter() {
        return gravityCenter;
    }

    public void setGravityCenter(Point3F position) {
        this.gravityCenter = position;
    }
    
    public void translate(Vec3F translation){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        for(int j = 0 ; j<vertexBuffer.capacity(); j+=3){
            
            
            float x = vertexBuffer.get(j);
            float y = vertexBuffer.get(j+1);
            float z = vertexBuffer.get(j+2);
            
            vertexBuffer.put(j, x+translation.x);
            vertexBuffer.put(j+1, y+translation.y);
            vertexBuffer.put(j+2, z+translation.z);
            
        }
        
        mesh.setVertexBuffer(vertexBuffer);
    }
    
    public void scale(Vec3F scale){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        for(int j = 0 ; j<vertexBuffer.capacity(); j+=3){
            
            
            float x = vertexBuffer.get(j);
            float y = vertexBuffer.get(j+1);
            float z = vertexBuffer.get(j+2);
            
            vertexBuffer.put(j, x*scale.x);
            vertexBuffer.put(j+1, y*scale.y);
            vertexBuffer.put(j+2, z*scale.z);
        }
        
        mesh.setVertexBuffer(vertexBuffer);
        
    }
    
    public void rotate(Mat4F rotation){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        for(int j = 0 ; j<vertexBuffer.capacity(); j+=3){
            
            float x = vertexBuffer.get(j);
            float y = vertexBuffer.get(j+1);
            float z = vertexBuffer.get(j+2);
            
                
            Vec4F result = Mat4F.multiply(rotation, new Vec4F(x, y, z, 1));
            vertexBuffer.put(j, result.x);
            vertexBuffer.put(j+1, result.y);
            vertexBuffer.put(j+2, result.z);
        }
        
        mesh.setVertexBuffer(vertexBuffer);
    }
    
    public void setMousePickable(boolean isPickable){
        
        this.mousePickable = isPickable;
    }

    public boolean isMousePickable() {
        return mousePickable;
    }  
    
    public void updateMousePicker(MousePicker mousePicker){
        this.mousePicker = mousePicker;
    }

    public GLMesh getMesh() {
        return mesh;
    }
    
    public void addSceneObjectListener(SceneObjectListener listener){
        listeners.add(SceneObjectListener.class, listener);
    }
    
    public void removeSceneObjectListener(SceneObjectListener listener){
        listeners.remove(SceneObjectListener.class, listener);
    }
    
    public void fireClicked(Vec3F ray){
        
        for(SceneObjectListener listener : listeners.getListeners(SceneObjectListener.class)){
            
            listener.clicked(this, ray);
        }
    }
    
    public BoundingBox3D getBoundingBox(){
        
        BoundingBox3D bb = new BoundingBox3D(
                new Point3D(mesh.getxValues().getMinValue(),
                        mesh.getyValues().getMinValue(),
                        mesh.getzValues().getMinValue()),
                new Point3D(mesh.getxValues().getMaxValue(),
                        mesh.getyValues().getMaxValue(),
                        mesh.getzValues().getMaxValue()));
        
        return bb;
    }
    
    public abstract void initBuffers(GL3 gl);
    
    public abstract void updateBuffers(GL3 gl, int index, FloatBuffer buffer);
    
    public abstract void initVao(GL3 gl);
    
    public abstract void draw(GL3 gl);

    public abstract void load(File file);
    
    public abstract Object doPicking(Point3F camPosition, Vec3F ray);
}
