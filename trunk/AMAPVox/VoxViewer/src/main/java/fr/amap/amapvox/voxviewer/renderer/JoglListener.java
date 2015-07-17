/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.renderer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.amapvox.commons.math.matrix.Mat4F;
import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxviewer.event.BasicEvent;
import fr.amap.amapvox.voxviewer.loading.shader.AxisShader;
import fr.amap.amapvox.voxviewer.loading.shader.InstanceLightedShader;
import fr.amap.amapvox.voxviewer.loading.shader.InstanceShader;
import fr.amap.amapvox.voxviewer.loading.shader.LightedShader;
import fr.amap.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.amapvox.voxviewer.loading.shader.SimpleShader;
import fr.amap.amapvox.voxviewer.loading.shader.TextureShader;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.object.camera.CameraAdapter;
import fr.amap.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.amapvox.voxviewer.object.scene.Scene;
import fr.amap.amapvox.voxviewer.object.scene.SceneManager;
import fr.amap.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.amapvox.voxviewer.object.scene.SceneObjectFactory;
import fr.amap.amapvox.voxviewer.object.scene.SimpleSceneObject;
import fr.amap.amapvox.voxviewer.object.scene.SimpleSceneObject2;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpace;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class JoglListener implements GLEventListener {
    
    private VoxelSpace voxelSpace;
    private BasicEvent eventListener;
    TrackballCamera camera;
    private Scene scene;
    private SceneManager sceneManager;
    private Vec3F worldColor;
    
    public int width;
    public int height;
    
    public int startX = 0;
    public int startY = 0;
    
    private RegularDtm terrain;
    private boolean isFpsInit = false;
    private boolean justOnce = false;
    private FPSAnimator animator;
    private boolean viewMatrixChanged = false;
    private boolean projectionMatrixChanged = false;
    private boolean isInit;
    
    
    
    private final EventListenerList listeners;


    public RegularDtm getTerrain() {
        return terrain;
    }
    
    public Scene getScene() {
        return scene;
    }

    public Vec3F getWorldColor() {
        return worldColor;
    }

    public void setWorldColor(Vec3F worldColor) {
        this.worldColor = worldColor;
    }

    public TrackballCamera getCamera() {
        return camera;
    }

    public BasicEvent getEventListener() {
        return eventListener;
    }

    
    
    public JoglListener(VoxelSpace voxelSpace, FPSAnimator animator){
        
        scene = new Scene();
        this.voxelSpace = voxelSpace;
        this.animator = animator;
        worldColor = new Vec3F(200.0f/255.0f, 200.0f/255.0f, 200.0f/255.0f);
        listeners = new EventListenerList();
    }
    /*
    public JoglListener(JFrameSettingUp parent, Dtm terrain, Settings settings, FPSAnimator animator){
        
        this.terrain = terrain;
        this.settings = settings;
        this.parent = parent;
        worldColor = new Vec3F(200.0f/255.0f, 200.0f/255.0f, 200.0f/255.0f);
        this.animator = animator;
    }
    */
    public void attachEventListener(BasicEvent eventListener){
        
        this.eventListener = eventListener;
    }
    
    public void addListener(JoglListenerListener listener){
        listeners.add(JoglListenerListener.class, listener);
    }
    
    public void fireSceneInitialized() {

        for (JoglListenerListener listener : listeners.getListeners(JoglListenerListener.class)) {
            listener.sceneInitialized();
        }
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        
        isInit = true;
                
        GL3 gl = drawable.getGL().getGL3();
        
        String extensions = gl.glGetString(GL3.GL_EXTENSIONS);
        /*
        Vec3F eye = new Vec3F(80.0f, 72.25f, 200f);
        Vec3F target = new Vec3F(0.0f, 0.0f, 0.0f);
        Vec3F up = new Vec3F(0.0f, 0.0f, 1.0f);
        */
        
        Vec3F eye = new Vec3F(80.0f, 72.25f, 200f);
        Vec3F target = new Vec3F(0.0f, 0.0f, 0.0f);
        Vec3F up = new Vec3F(0.0f, 0.0f, 1.0f);
        
        camera = new TrackballCamera();
        camera.init(eye, target, up);
        camera.initOrtho(-((this.width-startX)/100), (this.width-startX)/100, this.height/100, -(this.height)/100, camera.getNearOrtho(), camera.getFarOrtho());
        
        try {
            initScene(gl);
        } catch (Exception ex) {
            Logger.getLogger(JoglListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA );
                
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_LEQUAL);
                
        gl.glClearDepthf(1.0f);
        
        //gl.glEnable(GL3.GL_LINE_SMOOTH);
        //gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        
        //gl.glPolygonMode(GL3.GL_FRONT, GL3.GL_LINE);
        //gl.glPolygonMode(GL3.GL_BACK, GL3.GL_LINE);
        //gl.glPolygonMode(GL3.GL_FRONT_AND_BACK, GL3.GL_LINE);
        //gl.glEnable(GL3.GL_CULL_FACE);
        //gl.glCullFace(GL3.GL_FRONT_AND_BACK);
        
        //gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
        //gl.glHint(GL3.GL_GENERATE_MIPMAP_HINT, GL3.GL_NICEST);
        //gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        //gl.glHint(GL3.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL3.GL_NICEST);
        
        //drawable.setAutoSwapBufferMode(true);
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        
        
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        
        update();
        try {
            render(drawable);
        } catch (Exception ex) {
            Logger.getLogger(JoglListener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void update() {
        
    }
    
    public void drawNextFrame(){
        justOnce = true;
        animator.resume();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        
        this.width = width;
        this.height = height;
        
        GL3 gl=drawable.getGL().getGL3();
        gl.glViewport(startX, startY, this.width-startX, this.height);        
        
        scene.setWidth(width-startX);
        scene.setHeight(height);
        
        if(isInit){
            camera.setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);
        }
        
        updateCamera();
    }
    
    public void updateCamera(){
        
        if(camera.isIsPerspective()){
            camera.setPerspective(60.0f, (1.0f*this.width-startX)/height, camera.getNearPersp(), camera.getFarPersp());
        }else{
            camera.initOrtho(-((this.width-startX)/100), (this.width-startX)/100, this.height/100, -(this.height)/100, camera.getNearOrtho(), camera.getFarOrtho());
            camera.setOrthographic(camera.getNearOrtho(), camera.getFarOrtho());
        }
        
    }
    
    private void render(GLAutoDrawable drawable) throws Exception {
        
        GL3 gl = drawable.getGL().getGL3();
        
        gl.glViewport(startX, startY, width-startX, height);
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT|GL3.GL_COLOR_BUFFER_BIT);
        gl.glClearColor(worldColor.x, worldColor.y, worldColor.z, 1.0f);
        
        gl.glDisable(GL3.GL_BLEND);
        
        eventListener.updateEvents();
        
        if(scene.isLightAmbientColorChanged()){
            
            int id = scene.getShaderByName("instanceLightedShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniform3f(s.uniformMap.get("lambient"), scene.getLight().ambient.x, scene.getLight().ambient.y, scene.getLight().ambient.z);
            gl.glUseProgram(0);
            
            scene.setLightAmbientColorChanged(false);
        }
        
        if(scene.isLightDiffuseColorChanged()){
            
            int id = scene.getShaderByName("instanceLightedShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniform3f(s.uniformMap.get("ldiffuse"), scene.getLight().diffuse.x, scene.getLight().diffuse.y, scene.getLight().diffuse.z);
            gl.glUseProgram(0);
            
            scene.setLightDiffuseColorChanged(false);
        }
        
        if(scene.isLightSpecularColorChanged()){
            
            int id = scene.getShaderByName("instanceLightedShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniform3f(s.uniformMap.get("lspecular"), scene.getLight().specular.x, scene.getLight().specular.y, scene.getLight().specular.z);
            gl.glUseProgram(0);
            
            scene.setLightSpecularColorChanged(false);
        }
        
        if(scene.isLightPositionChanged()){
            
            int id2 = scene.getShaderByName("lightShader");
            Shader s2 = scene.getShadersList().get(id2);
            gl.glUseProgram(id2);
                gl.glUniform3f(s2.uniformMap.get("lightPosition"), scene.getLight().position.x, scene.getLight().position.y, scene.getLight().position.z);
            gl.glUseProgram(0);
            /*
            int id = scene.getShaderByName("instanceLightedShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniform3f(s.uniformMap.get("lightPosition"), scene.getLight().position.x, scene.getLight().position.y, scene.getLight().position.z);
            gl.glUseProgram(0);
            */
            scene.setLightPositionChanged(false);
        }
        
        if(isInit){
            
            camera.setPivot(new Vec3F(scene.getVoxelSpace().centerX, scene.getVoxelSpace().centerY, scene.getVoxelSpace().centerZ));
            scene.setLightPosition(new Point3F(scene.getVoxelSpace().centerX, scene.getVoxelSpace().centerY, scene.getVoxelSpace().centerZ+scene.getVoxelSpace().widthZ+100));
            
            int id = scene.getShaderByName("instanceLightedShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniform3f(s.uniformMap.get("lambient"), scene.getLight().ambient.x, scene.getLight().ambient.y, scene.getLight().ambient.z);
                gl.glUniform3f(s.uniformMap.get("ldiffuse"), scene.getLight().diffuse.x, scene.getLight().diffuse.y, scene.getLight().diffuse.z);
                gl.glUniform3f(s.uniformMap.get("lspecular"), scene.getLight().specular.x, scene.getLight().specular.y, scene.getLight().specular.z);
                gl.glUniform3f(s.uniformMap.get("lightPosition"), scene.getLight().position.x, scene.getLight().position.y, scene.getLight().position.z);
            gl.glUseProgram(0);
            
            int id2 = scene.getShaderByName("simpleShader");
            Shader s2 = scene.getShadersList().get(id2);
            gl.glUseProgram(id2);
                gl.glUniform3f(s2.uniformMap.get("color"), 1, 0, 0);
            gl.glUseProgram(0);
            
            scene.setLightAmbientColorChanged(false);
            scene.setLightDiffuseColorChanged(false);
            scene.setLightSpecularColorChanged(false);
        }
        
        
        if(viewMatrixChanged || isInit){
            
            
            Mat4F normalMatrix = Mat4F.transpose(Mat4F.inverse(camera.getViewMatrix()));
            FloatBuffer normalMatrixBuffer = Buffers.newDirectFloatBuffer(normalMatrix.mat);
            int id = scene.getShaderByName("noTranslationShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniformMatrix4fv(s.uniformMap.get("normalMatrix"), 1, false, normalMatrixBuffer);
                gl.glUniform3f(s.uniformMap.get("eye"), camera.getLocation().x, camera.getLocation().y, camera.getLocation().z);
            gl.glUseProgram(0);
            
            int id2 = scene.getShaderByName("textureShader");
            Shader s2 = scene.getShadersList().get(id2);
            gl.glUseProgram(id2);
                gl.glUniform3f(s2.uniformMap.get("eye"), camera.getLocation().x, camera.getLocation().y, camera.getLocation().z);
            gl.glUseProgram(0);
            
            FloatBuffer viewMatrixBuffer = Buffers.newDirectFloatBuffer(camera.getViewMatrix().mat);
                    
            for(Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {

                if(!shader.getValue().isOrtho){
                    gl.glUseProgram(shader.getKey());
                        gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("viewMatrix"), 1, false, viewMatrixBuffer);
                    gl.glUseProgram(0);
                }
            }
            
            viewMatrixChanged = false;
            
        }
        
        if(projectionMatrixChanged || isInit){
            
            FloatBuffer projMatrixBuffer = Buffers.newDirectFloatBuffer(camera.getProjectionMatrix().mat);

            for(Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {

                if(!shader.getValue().isOrtho){
                    gl.glUseProgram(shader.getKey());
                        gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("projMatrix"), 1, false, projMatrixBuffer);
                    gl.glUseProgram(0);
                }
            }
            
            projectionMatrixChanged = false;
        }
        
        isInit = false;
        
        scene.draw(gl, camera);
        
        if(justOnce){
            animator.pause();
            justOnce = false;
        }
    }
    
    private void initScene(final GL3 gl) throws Exception{
        
       
        
        try{
                     
            Shader noTranslationShader = new AxisShader(gl, "noTranslationShader");
            Shader instanceLightedShader = new InstanceLightedShader(gl, "instanceLightedShader");
            Shader instanceShader = new InstanceShader(gl, "instanceShader");
            Shader texturedShader = new TextureShader(gl, "textureShader");
            texturedShader.isOrtho = true;
            Shader labelShader = new TextureShader(gl, "labelShader");
            Shader lightedShader = new LightedShader(gl, "lightShader");
            Shader simpleShader = new SimpleShader(gl, "simpleShader");
            
            scene.addShader(noTranslationShader);
            scene.addShader(instanceLightedShader);
            scene.addShader(instanceShader);
            scene.addShader(texturedShader);
            scene.addShader(simpleShader);
            scene.addShader(lightedShader);
            scene.addShader(labelShader);
            
            GLMesh axisMesh = GLMeshFactory.createMeshFromObj(new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("mesh/axis.obj")),
                                            new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("mesh/axis.mtl")));
            
            //GLMesh axisMesh = GLMeshFactory.createMeshFromX3D(new InputStreamReader(JoglListener.class.getClassLoader().getResourceAsStream("mesh/axis2.x3d")));
            axisMesh.setGlobalScale(0.03f);
            
            SceneObject axis = new SimpleSceneObject(axisMesh, noTranslationShader.getProgramId(), false);
            
            axis.setDrawType(GL3.GL_TRIANGLES);
            //scene.addObject(axis, gl);
            
            if(scene.getDtm() != null){
                
                //logger.info("Computing dtm normals");
                GLMesh dtmMesh = GLMeshFactory.createMeshAndComputeNormalesFromDTM(scene.getDtm());
                
                SceneObject dtmSceneObject = new SimpleSceneObject(dtmMesh, lightedShader.getProgramId(), false);

                scene.addObject(dtmSceneObject, gl);
            }
            
            //bounding-box
            GLMesh boundingBoxMesh = GLMeshFactory.createBoundingBox((float)voxelSpace.data.header.bottomCorner.x, 
                                                                    (float)voxelSpace.data.header.bottomCorner.y,
                                                                    (float)voxelSpace.data.header.bottomCorner.z,
                                                                    (float)voxelSpace.data.header.topCorner.x, 
                                                                    (float)voxelSpace.data.header.topCorner.y,
                                                                    (float)voxelSpace.data.header.topCorner.z);
                                                                    
            SceneObject boundingBox = new SimpleSceneObject2(boundingBoxMesh, simpleShader.getProgramId(), false);
            
            boundingBox.setDrawType(GL3.GL_LINES);
            scene.addObject(boundingBox, gl);
            
            //génération des labels sur la bounding-box
            Texture textureLabel1 = Texture.createTextTexture(gl, "label 1");
            SceneObject label1 = SceneObjectFactory.createTexturedPlane(new Vec3F(0, 0, 0), textureLabel1, labelShader.getProgramId());
            label1.attachTexture(textureLabel1);
            
            Mat4F rotationMatrix = new Mat4F();
            
            float theta = (float) Math.toRadians(90);
            rotationMatrix.mat = new float[]{1, 0, 0, 0,
                                            0, (float)Math.cos(theta), (float)-Math.sin(theta), 0,
                                            0, (float)Math.sin(theta), (float)Math.cos(theta), 0,
                                            0, 0, 0, 1,
                                            };
            
            //label.rotate(rotationMatrix);
            //label1.translate(new Vec3F((float)voxelSpace.data.header.topCorner.x, (float)voxelSpace.data.header.bottomCorner.y, (float)voxelSpace.data.header.topCorner.z));
            //scene.addObject(label1, gl);
            
            Texture textureLabel2 = Texture.createTextTexture(gl, "label 2");
            SceneObject label2 = SceneObjectFactory.createTexturedPlane(new Vec3F(0, 0, 0), textureLabel2, labelShader.getProgramId());
            label2.attachTexture(textureLabel2);
            //label2.translate(new Vec3F((float)voxelSpace.data.header.bottomCorner.x, (float)voxelSpace.data.header.bottomCorner.y, (float)voxelSpace.data.header.topCorner.z));
            //scene.addObject(label2, gl);
            
            Texture textureLabel3 = Texture.createTextTexture(gl, "label 3");
            SceneObject label3 = SceneObjectFactory.createTexturedPlane(new Vec3F(0, 0, 0), textureLabel3, labelShader.getProgramId());
            label3.attachTexture(textureLabel3);
            //label3.translate(new Vec3F(0, 70, 0));
            //scene.addObject(label3, gl);
            
            //if(settings.drawAxis){
                //SceneObject sceneObject = new SimpleSceneObject(MeshFactory.createLandmark(-1000, 1000), basicShader.getProgramId(), false);
                //sceneObject.setDrawType(GL3.GL_LINES);
                //scene.addObject(sceneObject, gl);
            //}
            
            //if(settings.drawDtm){
                //SceneObject terrainSceneObject = new SimpleSceneObject(MeshFactory.createMesh(terrain.getPoints(), terrain.getIndices()), basicShader.getProgramId(), true);
                //terrainSceneObject.setDrawType(GL3.GL_TRIANGLES);
                //scene.addObject(terrainSceneObject, gl);
            //}
            
            camera.addCameraListener(new CameraAdapter() {
                
                @Override
                public void viewMatrixChanged(Mat4F viewMatrix) {
                    //must be updated inside the thread loop
                    viewMatrixChanged = true;
                }

                @Override
                public void projMatrixChanged(final Mat4F projMatrix) {
                    //must be updated inside the thread loop
                    projectionMatrixChanged = true;
                }
            });
            
            voxelSpace.setShaderId(instanceLightedShader.getProgramId());

            scene.setVoxelSpace(voxelSpace);
            
            scene.canDraw = true;
            
            fireSceneInitialized();
            
        }catch(Exception e){
            throw new Exception("error in scene initialization", e);
        }
        
    }
    
}
