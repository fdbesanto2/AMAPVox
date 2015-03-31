/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import fr.ird.voxelidar.util.image.ScaleGradient;
import fr.ird.voxelidar.engine3d.loading.texture.Texture;
import fr.ird.voxelidar.engine3d.object.camera.Camera;
import fr.ird.voxelidar.engine3d.loading.shader.Shader;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4F;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Julien
 */
public class Scene {
    
    /*
        contient une liste de shaders, une liste de buffers, une liste de lumières et un VoxelSpace
        ??optimisation 
    */
    
    private final ArrayList<SceneObject> objectsList;
    private final Map<Integer, Shader> shadersList;
    private VoxelSpace voxelSpace;
    public boolean canDraw;
    private SceneObject scalePlane;
    private int width;
    private int height;
    
    public Scene(){
        
        objectsList = new ArrayList<>();
        shadersList = new HashMap<>();
        canDraw = false;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }
    
    public VoxelSpace getVoxelSpace() {
        return voxelSpace;
    }

    public Map<Integer, Shader> getShadersList() {
        return shadersList;
    }

    public void setVoxelSpace(VoxelSpace voxelSpace) {
        this.voxelSpace = voxelSpace;
    }
    
    public void addObject(SceneObject sceneObject, GL3 gl){
        
        sceneObject.initBuffers(gl);
        sceneObject.initVao(gl, shadersList.get(sceneObject.getShaderId()));
        sceneObject.setId(objectsList.size());
        objectsList.add(sceneObject);
    }
    
    public void changeObjectTexture(int idObject, Texture texture){
        
        objectsList.get(idObject).attachTexture(texture);
    }
    
    public void addShader(Shader shader) {
        
        shadersList.put(shader.getProgramId(),shader);
    }
    
    public int getShaderByName(String name){
        
        for(Entry<Integer, Shader> shader : shadersList.entrySet()) {

            if(shader.getValue().name.equals(name)){
                
                return shader.getKey();
            }
        }
        
        return -1;
    }
    
    public void updateColorScale(){
        
        
    }
    
    public void draw(final GL3 gl, Camera camera){
        
        if(canDraw){
            
            if(!voxelSpace.arrayLoaded){
                
                Texture texture;
                if(voxelSpace.isStretched()){
                    texture = Texture.createColorScaleTexture(gl, ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.min, voxelSpace.max, width-80, (int)(height/20), ScaleGradient.HORIZONTAL), voxelSpace.attributValueMin, voxelSpace.attributValueMax);
                }else{
                    texture = Texture.createColorScaleTexture(gl, ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.attributValueMin, voxelSpace.attributValueMax, width-80, (int)(height/20), ScaleGradient.HORIZONTAL), voxelSpace.attributValueMin, voxelSpace.attributValueMax);
                }
                
                scalePlane = SceneObjectFactory.createTexturedPlane(new Vec3F(40, 20, 0), width-80, (int)(height/20), texture, getShaderByName("textureShader"));
                scalePlane.setDrawType(GL3.GL_TRIANGLES);
                
                this.addObject(scalePlane, gl);
        
                voxelSpace.initBuffers(gl);
                voxelSpace.initVao(gl, shadersList.get(voxelSpace.getShaderId()));
                voxelSpace.arrayLoaded = true;
                camera.location = new Vec3F(voxelSpace.centerX-voxelSpace.widthX, voxelSpace.centerY-voxelSpace.widthY, voxelSpace.centerZ-voxelSpace.widthZ);
                camera.target = new Vec3F(voxelSpace.centerX,voxelSpace.centerY,voxelSpace.centerZ);
                camera.updateViewMatrix();
                
                int textureShaderId = getShaderByName("textureShader");
                gl.glUseProgram(textureShaderId);
                    FloatBuffer projectionMatrix = Buffers.newDirectFloatBuffer(Mat4F.ortho(0, 640, 0, 480, -10, 1000).mat);
                    FloatBuffer viewMatrix = Buffers.newDirectFloatBuffer(Mat4F.lookAt(new Vec3F(0,0,0), new Vec3F(0,0,0), new Vec3F(0,1,0)).mat);
                    Shader shader = shadersList.get(textureShaderId);
                    gl.glUniformMatrix4fv(shader.uniformMap.get("viewMatrix"), 1, false, viewMatrix);
                    gl.glUniformMatrix4fv(shader.uniformMap.get("projMatrix"), 1, false, projectionMatrix);
                    
                    gl.glUniform1i(shader.uniformMap.get("texture"),0);
                gl.glUseProgram(0);

            }            
            
            if(!voxelSpace.isGradientUpdated()){
                Texture texture;
                
                if(voxelSpace.isStretched()){
                    texture = Texture.createColorScaleTexture(gl, ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.min, voxelSpace.max, width-80, (int)(height/20), ScaleGradient.HORIZONTAL), voxelSpace.attributValueMin, voxelSpace.attributValueMax);
                }else{
                    texture = Texture.createColorScaleTexture(gl, ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.attributValueMin, voxelSpace.attributValueMax, width-80, (int)(height/20), ScaleGradient.HORIZONTAL), voxelSpace.attributValueMin, voxelSpace.attributValueMax);
                }
                
                changeObjectTexture(scalePlane.getId(), texture);
            }
            
            /***draw voxel space***/
            gl.glUseProgram(voxelSpace.getShaderId());
                voxelSpace.render(gl);
            gl.glUseProgram(0);
            
            /***draw scene objects***/
            
            for(SceneObject object : objectsList){
                
                if(object.isAlphaRequired){
                    gl.glEnable(GL3.GL_BLEND);
                }else{
                    gl.glDisable(GL3.GL_BLEND);
                }
                
                gl.glUseProgram(object.getShaderId());
                    object.draw(gl, object.getDrawType());
                gl.glUseProgram(0);
                
            }
            
        }
        
    }
    
}
