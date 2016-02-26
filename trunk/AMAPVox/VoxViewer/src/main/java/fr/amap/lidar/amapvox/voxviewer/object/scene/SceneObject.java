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
import fr.amap.lidar.amapvox.voxviewer.loading.shader.UniformMat4F;
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
    protected DrawType drawType;
    public boolean isAlphaRequired;
    public boolean depthTest = true;
    public Texture texture;
    protected Shader shader;
    protected Point3F gravityCenter;
    protected Mat4F transformation;
    private UniformMat4F transfoUniform = new UniformMat4F("transformation");
    private int id;
    protected boolean colorNeedUpdate = false;
    
    protected boolean mousePickable;
    private boolean selected;
    private boolean visible = true;
    
    private final EventListenerList listeners;

    public SceneObject(){
        vaoId = -1;
        transformation = Mat4F.identity();
        //setPosition(new Point3F());
        listeners = new EventListenerList();
        visible = true;
    }
    
    public SceneObject(GLMesh mesh, boolean isAlphaRequired){
        
        this.mesh = mesh;
        this.drawType = DrawType.TRIANGLES;
        this.isAlphaRequired = isAlphaRequired;
        this.gravityCenter = mesh.getGravityCenter();
        vaoId = -1;
        transformation = Mat4F.identity();
        //setPosition(new Point3F());
        listeners = new EventListenerList();
        visible = true;
    }
    
    public void resetIds(){
        vaoId = -1;
        mesh.resetIds();
    }
    
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

    public int getVaoId() {
        return vaoId;
    }

    public void setDrawType(DrawType drawType) {
        this.drawType = drawType;
    }

    public DrawType getDrawType() {
        return drawType;
    }

    public Shader getShader() {
        return shader;
    }

    public void setShader(Shader shader) {
        this.shader = shader;
        transfoUniform.addOwner(this.shader);
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
    
    public void setMousePickable(boolean isPickable){
        
        this.mousePickable = isPickable;
    }

    public boolean isMousePickable() {
        return mousePickable;
    }

    public GLMesh getMesh() {
        return mesh;
    }
    
    public void setMesh(GLMesh mesh){
        this.mesh = mesh;
        this.resetIds();
    }
    
    public void addSceneObjectListener(SceneObjectListener listener){
        listeners.add(SceneObjectListener.class, listener);
    }
    
    public void removeSceneObjectListener(SceneObjectListener listener){
        listeners.remove(SceneObjectListener.class, listener);
    }
    
    public void fireClicked(MousePicker mousePicker){
        
        for(SceneObjectListener listener : listeners.getListeners(SceneObjectListener.class)){
            
            listener.clicked(this, mousePicker);
        }
    }
    
    public void setPosition(Point3F position){
        
        transformation.mat[3] = position.x;
        transformation.mat[7] = position.y;
        transformation.mat[11] = position.z;
        
        transfoUniform.setValue(Mat4F.transpose(transformation));
    }
    
    public BoundingBox3D getBoundingBox(){
        
        Vec4F min = Mat4F.multiply(transformation, new Vec4F(
                (float)mesh.getxValues().getMinValue(),
                (float)mesh.getyValues().getMinValue(),
                (float)mesh.getzValues().getMinValue(),
                1));
        
        Vec4F max = Mat4F.multiply(transformation, new Vec4F(
                (float)mesh.getxValues().getMaxValue(),
                (float)mesh.getyValues().getMaxValue(),
                (float)mesh.getzValues().getMaxValue(),
                1));
        
        
        BoundingBox3D bb = new BoundingBox3D(
                new Point3D(min.x,min.y,min.z),
                new Point3D(max.x,max.y,max.z));
        
        return bb;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
    
    public abstract void initBuffers(GL3 gl);
    
    public abstract void updateBuffers(GL3 gl, int index, FloatBuffer buffer);
    
    public abstract void initVao(GL3 gl);
    
    public abstract void draw(GL3 gl);
    
    public abstract Object doPicking(MousePicker mousePicker);
}
