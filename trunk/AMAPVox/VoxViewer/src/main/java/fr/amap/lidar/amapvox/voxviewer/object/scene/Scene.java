/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import com.jogamp.opengl.GL3;
import fr.amap.commons.math.geometry.Intersection;
import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.AxisShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.ColorShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.InstanceLightedShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.InstanceShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.PhongShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.SimpleShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.TextureShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.Uniform;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.Uniform1I;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.Uniform3F;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.UniformMat4F;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.lidar.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.lidar.amapvox.voxviewer.object.lighting.Light;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;
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
    
    public final CopyOnWriteArrayList<SceneObject> objectsList;
    public final CopyOnWriteArrayList<SceneObject> hudList;
    public final Map<String, Shader> shadersList;
    public final List<Texture> textureList;
    
    private TrackballCamera camera;
    private Light light;
    
    private MousePicker mousePicker;
    private boolean mousePickerIsDirty; //the mouse picker has been updated
    
    public boolean canDraw;
    
    private int width;
    private int height;
    
    //default shaders
    public static AxisShader noTranslationShader = new AxisShader("noTranslationShader");
    public static Shader instanceLightedShader = new InstanceLightedShader("instanceLightedShader");
    public static InstanceShader instanceShader = new InstanceShader("instanceShader");
    public static TextureShader texturedShader = new TextureShader("textureShader");
    public static TextureShader labelShader = new TextureShader("labelShader");
    public static PhongShader phongShader = new PhongShader("lightShader");
    public static SimpleShader simpleShader = new SimpleShader("simpleShader");
    public static ColorShader colorShader = new ColorShader("colorShader");
    
    //global uniforms, can be used inside shaders files
    public UniformMat4F viewMatrixUniform = new UniformMat4F("viewMatrix");
    public UniformMat4F projMatrixUniform = new UniformMat4F("projMatrix");
    public UniformMat4F normalMatrixUniform;
    public UniformMat4F transformationUniform = new UniformMat4F("transformation");
    public Uniform3F lightPositionUniform = new Uniform3F("lightPosition");
    public Uniform3F lambientUniform = new Uniform3F("lambient");
    public Uniform3F ldiffuseUniform = new Uniform3F("ldiffuse");
    public Uniform3F lspecularUniform = new Uniform3F("lspecular");
    public UniformMat4F projMatrixOrthoUniform = new UniformMat4F("projMatrixOrtho");
    public UniformMat4F viewMatrixOrthoUniform = new UniformMat4F("viewMatrixOrtho");
    public Uniform1I textureUniform = new Uniform1I("texture");
    
    private final Map<String, Uniform> uniforms = new HashMap<>();
    
    public Scene(){
        
        objectsList = new CopyOnWriteArrayList<>();
        hudList = new CopyOnWriteArrayList<>();
        shadersList = new HashMap<>();
        textureList = new ArrayList<>();
        canDraw = false;
        light = new Light();
        camera = new TrackballCamera();
        mousePicker = new MousePicker(camera);
        
        uniforms.put(viewMatrixUniform.getName(), viewMatrixUniform);
        uniforms.put(projMatrixUniform.getName(), projMatrixUniform);
        uniforms.put(transformationUniform.getName(), transformationUniform);
        uniforms.put(projMatrixOrthoUniform.getName(), projMatrixOrthoUniform);
        uniforms.put(viewMatrixOrthoUniform.getName(), viewMatrixOrthoUniform);
        uniforms.put(textureUniform.getName(), textureUniform);
        uniforms.put(lambientUniform.getName(), lambientUniform);
        uniforms.put(ldiffuseUniform.getName(), ldiffuseUniform);
        uniforms.put(lspecularUniform.getName(), lspecularUniform);
        uniforms.put(lightPositionUniform.getName(), lightPositionUniform);
        
    }
    
    private void initUniforms(){
        
        Iterator<Entry<String, Shader>> iterator = shadersList.entrySet().iterator();
        
        while(iterator.hasNext()){ //pour tous les shaders
            Entry<String, Shader> shaderEntry = iterator.next();
            initUniforms(shaderEntry.getValue());
        }
    }
    
    private void initUniforms(Shader shader){
        
        Iterator<Entry<String, Integer>> iterator2 = shader.uniformMap.entrySet().iterator();
            
        while(iterator2.hasNext()){ //pour chaque uniform d'un shader
            Entry<String, Integer> uniformEntry = iterator2.next();

            //on ajoute la variable uniform à la map uniforms
            if(uniforms.containsKey(uniformEntry.getKey())){
                uniforms.get(uniformEntry.getKey()).addOwner(shader);

                //handle the case when uniform has been updated before shader has been initialized
                if(uniforms.get(uniformEntry.getKey()).isDirty()){
                    shader.notifyDirty(uniforms.get(uniformEntry.getKey()));
                }
            }
        }
    }
    
    private void notifyShaderDirtyUniforms(Shader shader){
        
        Iterator<Entry<String, Integer>> iterator2 = shader.uniformMap.entrySet().iterator();
            
        while(iterator2.hasNext()){ //pour chaque uniform d'un shader
            Entry<String, Integer> uniformEntry = iterator2.next();

            //on ajoute la variable uniform à la map uniforms
            if(uniforms.containsKey(uniformEntry.getKey())){
                shader.notifyDirty(uniforms.get(uniformEntry.getKey()));
            }
        }
    }
    
    public void init(GL3 gl){
        
        try {
            
            Iterator<Entry<String, Shader>> iterator = shadersList.entrySet().iterator();

            while(iterator.hasNext()){
                Entry<String, Shader> next = iterator.next();
                Shader shader = next.getValue();
                shader.init(gl);
            }
            
            noTranslationShader.init(gl);
            instanceLightedShader.init(gl);
            instanceShader.init(gl);
            texturedShader.init(gl);
            labelShader.init(gl);
            phongShader.init(gl);
            simpleShader.init(gl);
            colorShader.init(gl);
            
            addShader(noTranslationShader);
            addShader(instanceLightedShader);
            addShader(instanceShader);
            addShader(texturedShader);
            addShader(simpleShader);
            addShader(phongShader);
            addShader(labelShader);
            addShader(colorShader);
            
            initUniforms(); //assign owners to uniforms (shaders using the uniforms)
            
            projMatrixOrthoUniform.setValue(Mat4F.ortho(0, width, 0, height, -10, 1000));
            viewMatrixOrthoUniform.setValue(Mat4F.lookAt(new Vec3F(0,0,0), new Vec3F(0,0,0), new Vec3F(0,1,0)));
            transformationUniform.setValue(Mat4F.identity());
            
            textureUniform.setValue(0);
            
            //binding de la caméra avec les variables uniforms des shaders
            camera.addPropertyChangeListener("projMatrix", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    projMatrixUniform.setValue((Mat4F) evt.getNewValue());
                    mousePicker.setProjectionMatrix((Mat4F) evt.getNewValue());
                }
            });
            
            camera.addPropertyChangeListener("viewMatrix", new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    viewMatrixUniform.setValue((Mat4F) evt.getNewValue());
                    mousePicker.setViewMatrix((Mat4F) evt.getNewValue());
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
            
            if(sceneObject.getShaderId() != -1){
                
                if(sceneObject.getMesh().getVboId() == -1){
                    sceneObject.initBuffers(gl);
                }
                
                if(sceneObject.getVaoId() == -1){
                    sceneObject.initVao(gl);
                }
                
                sceneObject.setId(objectsList.size());
            }            
        }
        
        for(SceneObject sceneObject : hudList){
            sceneObject.initBuffers(gl);
            sceneObject.initVao(gl);
            sceneObject.setId(objectsList.size()+hudList.size());
            
        }
        
        setLightAmbientValue(getLight().ambient);
        setLightDiffuseValue(getLight().diffuse);
        setLightSpecularValue(getLight().specular);
        
    }

    public Map<String, Shader> getShadersList() {
        return shadersList;
    }
    
    public void addSceneObjectAsHud(SceneObject sceneObject){
        
        hudList.add(sceneObject);
        if(sceneObject.texture != null){
            addTexture(sceneObject.texture);
        }
    }
    
    public void addSceneObject(SceneObject sceneObject){
        
        objectsList.add(sceneObject);
        if(sceneObject.texture != null){
            addTexture(sceneObject.texture);
        }
    }
    
    public SceneObject getFirstSceneObject(){
        
        if(objectsList.size() > 0){
            return objectsList.get(0);
        }
        
        return null;
    }
    
    public void updateMousePicker(float mouseX, float mouseY, int startX, int startY, float viewportWidth, float viewportHeight){
        mousePicker.update(mouseX, mouseY, startX, startY, viewportWidth, viewportHeight);
        mousePickerIsDirty = true;
    }
    
    public void addShader(Shader shader) {
        
        shadersList.put(shader.name,shader);
    }
    
    public void addTexture(Texture texture){
        textureList.add(texture);
    }
    
    public void draw(final GL3 gl){
        
        camera.updateViewMatrix();
        
        //update picking
        if(mousePickerIsDirty){
            
            //test for intersection (simple method, need to use an octree later)
            for(SceneObject object : objectsList){
                
                
                //System.out.println(mousePicker.getCurrentRay().x+" "+mousePicker.getCurrentRay().y+" "+mousePicker.getCurrentRay().z);
                
                Point3F startPoint = mousePicker.getPointOnray(1);
                Point3F endPoint = mousePicker.getPointOnray(1000);
                
                Point3D intersection = Intersection.getIntersectionLineBoundingBox(new Point3D(startPoint.x, startPoint.y, startPoint.z),
                        new Point3D(endPoint.x, endPoint.y, endPoint.z),
                        object.getBoundingBox());
                
                if(intersection != null){
                    object.fireClicked(mousePicker.getCurrentRay());
                }
            }
            
            mousePickerIsDirty = false;
        }
        
        //add possible new shaders
        for(SceneObject object : objectsList){
            
            if(object.getShaderId() == -1){
                Shader shader = object.getShader();
                shader.init(gl);
                initUniforms(shader);
                addShader(shader);
                notifyShaderDirtyUniforms(shader);
            }
            
            if(object.vaoId == -1){
                object.initBuffers(gl);
                object.initVao(gl);
            }
        }
        
        //update shader variables
        Iterator<Entry<String, Shader>> iterator = shadersList.entrySet().iterator();

        while(iterator.hasNext()){
            Entry<String, Shader> next = iterator.next();
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
        
        for(SceneObject object : hudList){
            if(object.vaoId == -1){
                object.initBuffers(gl);
                object.initVao(gl);
            }

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
        mousePicker = new MousePicker(camera);
    }

    public TrackballCamera getCamera() {
        return camera;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public MousePicker getMousePicker() {
        return mousePicker;
    }
}
