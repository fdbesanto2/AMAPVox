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
import fr.amap.amapvox.voxviewer.loading.shader.AxisShader;
import fr.amap.amapvox.voxviewer.loading.shader.InstanceLightedShader;
import fr.amap.amapvox.voxviewer.loading.shader.InstanceShader;
import fr.amap.amapvox.voxviewer.loading.shader.LightedShader;
import fr.amap.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.amapvox.voxviewer.loading.shader.SimpleShader;
import fr.amap.amapvox.voxviewer.loading.shader.TextureShader;
import fr.amap.amapvox.voxviewer.loading.shader.Uniform;
import fr.amap.amapvox.voxviewer.loading.shader.Uniform1I;
import fr.amap.amapvox.voxviewer.loading.shader.Uniform3F;
import fr.amap.amapvox.voxviewer.loading.shader.UniformMat4F;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.amapvox.voxviewer.object.lighting.Light;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Scene {
    
    private final static Logger logger = Logger.getLogger(Scene.class);
    /*
        contient une liste de shaders, une liste de buffers, une liste de lumières et un VoxelSpace
        ??optimisation 
    */
    
    public final List<SceneObject> objectsList;
    public final Map<Integer, Shader> shadersList;
    public final List<Texture> textureList;
    
    public TrackballCamera camera;
    private Light light;
    
    public boolean canDraw;
    private SceneObject scalePlane;
    private int width;
    private int height;
    
    private boolean lightPositionChanged = true;
    private boolean lightAmbientColorChanged = true;
    private boolean lightDiffuseColorChanged = true;
    private boolean lightSpecularColorChanged = true;
    
    public Shader noTranslationShader;
    public Shader instanceLightedShader;
    public Shader instanceShader;
    public Shader texturedShader;
    public Shader labelShader;
    public Shader lightedShader;
    public Shader simpleShader;
    
    public UniformMat4F viewMatrixUniform;
    public UniformMat4F projMatrixUniform;
    public UniformMat4F normalMatrixUniform;
    public Uniform3F lightPositionUniform;
    public Uniform3F lambientUniform;
    public Uniform3F ldiffuseUniform;
    public Uniform3F lspecularUniform;
    
    public UniformMat4F projMatrixOrthoUniform;
    public UniformMat4F viewMatrixOrthoUniform;
    
    public Uniform1I textureUniform;
    
    private final Map<String, Uniform> uniforms = new HashMap<>();
    
    public Scene(){
        
        objectsList = new ArrayList<>();
        shadersList = new HashMap<>();
        textureList = new ArrayList<>();
        canDraw = false;
        light = new Light();
        
        noTranslationShader = new AxisShader("noTranslationShader");
        instanceLightedShader = new InstanceLightedShader("instanceLightedShader");
        instanceShader = new InstanceShader("instanceShader");
        texturedShader = new TextureShader("textureShader");
        texturedShader.isOrtho = true;
        labelShader = new TextureShader("labelShader");
        lightedShader = new LightedShader("lightShader");
        simpleShader = new SimpleShader("simpleShader");
        
        viewMatrixUniform = new UniformMat4F("viewMatrix");
        uniforms.put("viewMatrix", viewMatrixUniform);
        
        projMatrixUniform = new UniformMat4F("projMatrix");
        uniforms.put("projMatrix", projMatrixUniform);
        
        projMatrixOrthoUniform = new UniformMat4F("projMatrixOrtho");
        uniforms.put("projMatrixOrtho", projMatrixOrthoUniform);
        
        viewMatrixOrthoUniform = new UniformMat4F("viewMatrixOrtho");
        uniforms.put("viewMatrixOrtho", viewMatrixOrthoUniform);
        
        textureUniform = new Uniform1I("texture");
        uniforms.put("texture", textureUniform);
        
        lambientUniform = new Uniform3F("lambient");
        uniforms.put("lambient", lambientUniform);
        
        ldiffuseUniform = new Uniform3F("ldiffuse");
        uniforms.put("ldiffuse", ldiffuseUniform);
        
        lspecularUniform = new Uniform3F("lspecular");
        uniforms.put("lspecular", lspecularUniform);
        
        lightPositionUniform = new Uniform3F("lightPosition");
        uniforms.put("lightPosition", lightPositionUniform);
        
    }
    
    private void initUniforms(){
        
        Iterator<Entry<Integer, Shader>> iterator = shadersList.entrySet().iterator();
        
        while(iterator.hasNext()){ //pour tous les shaders
            Entry<Integer, Shader> shaderEntry = iterator.next();
            
            Iterator<Entry<String, Integer>> iterator2 = shaderEntry.getValue().uniformMap.entrySet().iterator();
            
            while(iterator2.hasNext()){ //pour chaque uniform d'un shader
                Entry<String, Integer> uniformEntry = iterator2.next();
                        
                //on ajout la variable uniform à la map uniforms
                if(uniforms.containsKey(uniformEntry.getKey())){
                    uniforms.get(uniformEntry.getKey()).addOwner(shaderEntry.getValue(), uniformEntry.getValue());
                }
            }
        }
    }
    
    public void init(GL3 gl){
        
        try {
            noTranslationShader.init(gl);
            instanceLightedShader.init(gl);
            instanceShader.init(gl);
            texturedShader.init(gl);
            labelShader.init(gl);
            lightedShader.init(gl);
            simpleShader.init(gl);
            
            addShader(noTranslationShader);
            addShader(instanceLightedShader);
            addShader(instanceShader);
            addShader(texturedShader);
            addShader(simpleShader);
            addShader(lightedShader);
            addShader(labelShader);
            
            initUniforms();
            
            projMatrixOrthoUniform.setValue(Mat4F.ortho(0, 640, 0, 480, -10, 1000));
            viewMatrixOrthoUniform.setValue(Mat4F.lookAt(new Vec3F(0,0,0), new Vec3F(0,0,0), new Vec3F(0,1,0)));
            
            textureUniform.setValue(0);
            
            //binding de la caméra avec les variables uniforms des shaders
            camera.addPropertyChangeListener("projMatrix", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    projMatrixUniform.setValue((Mat4F) evt.getNewValue());
                }
            });
            
            camera.addPropertyChangeListener("viewMatrix", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    viewMatrixUniform.setValue((Mat4F) evt.getNewValue());
                }
            });
            
        } catch (Exception ex) {
            logger.error("Cannot generate shader", ex);
        }
        
        for(Texture texture : textureList){
            try {
                texture.init(gl);
            } catch (Exception ex) {
                logger.error("Cannot generate texture", ex);
            }
        }
            
        for(SceneObject sceneObject : objectsList){
            sceneObject.initBuffers(gl);
            sceneObject.initVao(gl);
            sceneObject.setId(objectsList.size());
            
        }
        
        setLightAmbientValue(getLight().ambient);
        setLightDiffuseValue(getLight().diffuse);
        setLightSpecularValue(getLight().specular);
        
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Map<Integer, Shader> getShadersList() {
        return shadersList;
    }
    
    public void addSceneObject(SceneObject sceneObject){
        
        objectsList.add(sceneObject);
        if(sceneObject.texture != null){
            addTexture(sceneObject.texture);
        }
    }
    
    public void addObject(SceneObject sceneObject, GL3 gl){
        
        sceneObject.initBuffers(gl);
        sceneObject.initVao(gl);
        sceneObject.setId(objectsList.size());
        objectsList.add(sceneObject);
    }
    
    public void changeObjectTexture(int idObject, Texture texture){
        
        objectsList.get(idObject).attachTexture(texture);
    }
    
    public void addShader(Shader shader) {
        
        shadersList.put(shader.getProgramId(),shader);
    }
    
    public void addTexture(Texture texture){
        textureList.add(texture);
    }
    
    public int getShaderByName(String name){
        
        for(Entry<Integer, Shader> shader : shadersList.entrySet()) {

            if(shader.getValue().name.equals(name)){
                
                return shader.getKey();
            }
        }
        
        return -1;
    }
    
    public void draw(final GL3 gl){
        
        //update shader variables
        Iterator<Entry<Integer, Shader>> iterator = shadersList.entrySet().iterator();

        while(iterator.hasNext()){
            Entry<Integer, Shader> next = iterator.next();
            Shader shader = next.getValue();
            shader.updateProgram(gl);
        }
        
        //update textures
        for(Texture texture : textureList){
            if(texture.isDirty()){
                try {
                    texture.update(gl);
                } catch (Exception ex) {
                    logger.error("Failed to update texture", ex);
                }
            }
        }
        
        camera.updateViewMatrix();

        /***draw scene objects***/

        gl.glEnable(GL3.GL_BLEND);

        for(SceneObject object : objectsList){


            if(!object.depthTest){
                gl.glClear(GL3.GL_DEPTH_BUFFER_BIT);
            }

            gl.glUseProgram(object.getShaderId());
                object.draw(gl);
            gl.glUseProgram(0);

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
        lambientUniform.setValue(ambient);
    }
    
    public void setLightDiffuseValue(Vec3F diffuse){
        light.diffuse = diffuse;
        ldiffuseUniform.setValue(diffuse);
    }
    
    public void setLightSpecularValue(Vec3F specular){
        light.specular = specular;
        lspecularUniform.setValue(specular);
    }
    
    public Point3F getLightPosition() {
        return light.position;
    }

    public void setLightPosition(Point3F position) {
        this.light.position = position;
        lightPositionUniform.setValue(new Vec3F(position.x, position.y, position.z));
    }
    

    public void setCamera(TrackballCamera camera) {
        this.camera = camera;
    }

    public TrackballCamera getCamera() {
        return camera;
    }
    
}
