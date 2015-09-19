/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import fr.amap.amapvox.commons.math.matrix.Mat4F;
import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.commons.util.image.ScaleGradient;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.object.camera.Camera;
import fr.amap.amapvox.voxviewer.object.lighting.Light;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Scene {
    
    /*
        contient une liste de shaders, une liste de buffers, une liste de lumières et un VoxelSpace
        ??optimisation 
    */
    
    public final ArrayList<SceneObject> objectsList;
    public final ArrayList<Camera> cameraList;
    private Light light;
    public final Map<Integer, Shader> shadersList;
    private VoxelSpace voxelSpace;
    private RegularDtm dtm;
    public boolean canDraw;
    private SceneObject scalePlane;
    private int width;
    private int height;
    
    private boolean lightPositionChanged = true;
    private boolean lightAmbientColorChanged = true;
    private boolean lightDiffuseColorChanged = true;
    private boolean lightSpecularColorChanged = true;
    
    public Scene(){
        
        objectsList = new ArrayList<>();
        cameraList = new ArrayList<>();
        shadersList = new HashMap<>();
        canDraw = false;
        light = new Light();
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
    
    private Texture createColorScaleTexture(GL3 gl) throws Exception{
        
        Texture texture;
        
        if(voxelSpace.isStretched()){
            texture = Texture.createColorScaleTexture(gl, ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.min, voxelSpace.max, width-80, (int)(height/20), ScaleGradient.HORIZONTAL), voxelSpace.attributValueMin, voxelSpace.attributValueMax);
        }else{
            if(voxelSpace.isUseClippedRangeValue()){
                texture = Texture.createColorScaleTexture(gl, ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.attributValueMinClipped, voxelSpace.attributValueMaxClipped, width-80, (int)(height/20), ScaleGradient.HORIZONTAL), voxelSpace.attributValueMinClipped, voxelSpace.attributValueMaxClipped);
            }else{
                texture = Texture.createColorScaleTexture(gl, ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.attributValueMin, voxelSpace.attributValueMax, width-80, (int)(height/20), ScaleGradient.HORIZONTAL), voxelSpace.attributValueMin, voxelSpace.attributValueMax);
            }

        }
        
        return texture;
    }
    
    public void draw(final GL3 gl, Camera camera) throws Exception{
        
        if(canDraw){
            
            if(!voxelSpace.arrayLoaded){
                
                Texture texture= createColorScaleTexture(gl);
                
                scalePlane = SceneObjectFactory.createTexturedPlane(new Vec3F(40, 20, 0), width-80, (int)(height/20), texture, getShaderByName("textureShader"));
                scalePlane.setDrawType(GL3.GL_TRIANGLES);
                
                this.addObject(scalePlane, gl);
        
                voxelSpace.initBuffers(gl);
                voxelSpace.initVao(gl, shadersList.get(voxelSpace.getShaderId()));
                voxelSpace.arrayLoaded = true;
                //camera.location = new Vec3F(voxelSpace.centerX, voxelSpace.centerY, voxelSpace.centerZ+voxelSpace.widthZ);
                camera.setLocation(new Vec3F(voxelSpace.centerX+voxelSpace.widthX, voxelSpace.centerY+voxelSpace.widthY, voxelSpace.centerZ+voxelSpace.widthZ));
                camera.setTarget(new Vec3F(voxelSpace.centerX,voxelSpace.centerY,voxelSpace.centerZ));
                camera.updateViewMatrix();
                
                int textureShaderId = getShaderByName("textureShader");
                gl.glUseProgram(textureShaderId);
                    FloatBuffer projectionMatrix = Buffers.newDirectFloatBuffer(Mat4F.ortho(0, width, 0, height, -10, 1000).mat);
                    FloatBuffer viewMatrix = Buffers.newDirectFloatBuffer(Mat4F.lookAt(new Vec3F(0,0,0), new Vec3F(0,0,0), new Vec3F(0,1,0)).mat);
                    Shader shader = shadersList.get(textureShaderId);
                    gl.glUniformMatrix4fv(shader.uniformMap.get("viewMatrix"), 1, false, viewMatrix);
                    gl.glUniformMatrix4fv(shader.uniformMap.get("projMatrix"), 1, false, projectionMatrix);
                    
                    gl.glUniform1i(shader.uniformMap.get("texture"),0);
                gl.glUseProgram(0);

            }  
            
            boolean wasInstancesNotUpdated = !voxelSpace.isInstancesUpdated();
            
            if(!voxelSpace.isGradientUpdated()){
                
                Texture texture= createColorScaleTexture(gl);
                
                changeObjectTexture(scalePlane.getId(), texture);
            }
            
            /***draw voxel space***/
            //gl.glEnable(GL3.GL_BLEND);
            gl.glUseProgram(voxelSpace.getShaderId());
                voxelSpace.render(gl,shadersList.get(voxelSpace.getShaderId()));
            gl.glUseProgram(0);
            //gl.glDisable(GL3.GL_BLEND);
            
            if(!voxelSpace.isGradientUpdated() || wasInstancesNotUpdated){
                
                Texture texture= createColorScaleTexture(gl);
                changeObjectTexture(scalePlane.getId(), texture);
            }
            
            /***draw scene objects***/
            
            gl.glEnable(GL3.GL_BLEND);
            
            for(SceneObject object : objectsList){
                
                //if(object.isAlphaRequired){
                    
                //}else{
                    //gl.glDisable(GL3.GL_BLEND);
                //}
                    
                if(!object.depthTest){
                    gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);
                    //gl.glDisable(GL3.GL_DEPTH_TEST);
                    //gl.glEnable(GL3.GL_CULL_FACE);
                }
                
                gl.glUseProgram(object.getShaderId());
                    object.draw(gl);
                gl.glUseProgram(0);
                
                if(!object.depthTest){
                    //gl.glEnable(GL3.GL_DEPTH_TEST);
                    //gl.glDisable(GL3.GL_CULL_FACE);
                }
                
            }
            
        }
        
    }

    public Light getLight() {
        return light;
    }

    public void setLight(Light light) {
        this.light = light;
    }
    
    public void setLightAmbientValue(Vec3F ambient){
        light.ambient = ambient;
        lightAmbientColorChanged = true;
    }
    
    public void setLightDiffuseValue(Vec3F diffuse){
        light.diffuse = diffuse;
        lightDiffuseColorChanged = true;
    }
    
    public void setLightSpecularValue(Vec3F specular){
        light.specular = specular;
        lightSpecularColorChanged = true;
    }
    
    public Point3F getLightPosition() {
        return light.position;
    }

    public void setLightPosition(Point3F position) {
        this.light.position = position;
        lightPositionChanged = true;
        
    }

    public boolean isLightPositionChanged() {
        return lightPositionChanged;
    }

    public void setLightPositionChanged(boolean lightPositionChanged) {
        this.lightPositionChanged = lightPositionChanged;
    }

    public boolean isLightAmbientColorChanged() {
        return lightAmbientColorChanged;
    }

    public void setLightAmbientColorChanged(boolean lightAmbientColorChanged) {
        this.lightAmbientColorChanged = lightAmbientColorChanged;
    }

    public boolean isLightDiffuseColorChanged() {
        return lightDiffuseColorChanged;
    }

    public void setLightDiffuseColorChanged(boolean lightDiffuseColorChanged) {
        this.lightDiffuseColorChanged = lightDiffuseColorChanged;
    }

    public boolean isLightSpecularColorChanged() {
        return lightSpecularColorChanged;
    }

    public void setLightSpecularColorChanged(boolean lightSpecularColorChanged) {
        this.lightSpecularColorChanged = lightSpecularColorChanged;
    }

    public RegularDtm getDtm() {
        return dtm;
    }

    public void setDtm(RegularDtm dtm) {
        this.dtm = dtm;
    }
}
