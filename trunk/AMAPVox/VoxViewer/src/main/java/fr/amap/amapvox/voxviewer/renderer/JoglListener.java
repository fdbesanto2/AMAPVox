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
import fr.amap.amapvox.commons.math.geometry.AABB;
import fr.amap.amapvox.commons.math.geometry.Plane;
import fr.amap.amapvox.commons.math.matrix.Mat4F;
import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.commons.util.BoundingBox3F;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxviewer.event.BasicEvent;
import fr.amap.amapvox.voxviewer.event.EventManager;
import fr.amap.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.object.camera.CameraAdapter;
import fr.amap.amapvox.voxviewer.object.scene.Scene;
import fr.amap.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.amapvox.voxviewer.object.scene.SceneObjectFactory;
import fr.amap.amapvox.voxviewer.object.scene.SimpleSceneObject;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpaceSceneObject;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class JoglListener implements GLEventListener {
    
    private EventManager eventListener;
    private Scene scene;
    private Vec3F worldColor;
    
    public int width;
    public int height;
    
    public int viewportWidth;
    public int viewportHeight;
    
    public int startX = 0;
    public int startY = 0;
    
    private RegularDtm terrain;
    private boolean isFpsInit = false;
    private boolean justOnce = false;
    private FPSAnimator animator;
    private boolean viewMatrixChanged = false;
    private boolean projectionMatrixChanged = false;
    private boolean isInit;
    
    private boolean isCuttingInit;
    private Vec3F lastRightVector = new Vec3F();
    private Vec3F loc;
    private float cuttingIncrementFactor = 1.0f;
    
    
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

    public EventManager getEventListener() {
        return eventListener;
    }

    
    
    public JoglListener(FPSAnimator animator){
        
        scene = new Scene();
        this.animator = animator;
        worldColor = new Vec3F(0.78f, 0.78f, 0.78f);
        listeners = new EventListenerList();
    }
    
    public void attachEventListener(EventManager eventListener){
        
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
        
        GL3 gl = drawable.getGL().getGL3();
        
        gl.glViewport(startX, startY, width-startX, height);
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT|GL3.GL_COLOR_BUFFER_BIT);
        gl.glClearColor(worldColor.x, worldColor.y, worldColor.z, 1.0f);
        
        gl.glDisable(GL3.GL_BLEND);
        
        if(eventListener != null){
            eventListener.updateEvents();
        }
        
        if(isInit){
            
            //scene.getCamera().setPivot(new Vec3F(scene.getVoxelSpace().centerX, scene.getVoxelSpace().centerY, scene.getVoxelSpace().centerZ));
            //scene.setLightPosition(new Point3F(scene.getVoxelSpace().centerX, scene.getVoxelSpace().centerY, scene.getVoxelSpace().centerZ+scene.getVoxelSpace().widthZ+100));
            
            /*int id = scene.getShaderByName("instanceLightedShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniform3f(s.uniformMap.get("lambient"), scene.getLight().ambient.x, scene.getLight().ambient.y, scene.getLight().ambient.z);
                gl.glUniform3f(s.uniformMap.get("ldiffuse"), scene.getLight().diffuse.x, scene.getLight().diffuse.y, scene.getLight().diffuse.z);
                gl.glUniform3f(s.uniformMap.get("lspecular"), scene.getLight().specular.x, scene.getLight().specular.y, scene.getLight().specular.z);
                gl.glUniform3f(s.uniformMap.get("lightPosition"), scene.getLight().position.x, scene.getLight().position.y, scene.getLight().position.z);
            gl.glUseProgram(0);*/
            
            int id2 = scene.getShaderByName("simpleShader");
            Shader s2 = scene.getShadersList().get(id2);
            gl.glUseProgram(id2);
                gl.glUniform3f(s2.uniformMap.get("color"), 1, 0, 0);
            gl.glUseProgram(0);
        }
        
        isInit = false;
        
        scene.draw(gl);
        
        if(justOnce){
            animator.pause();
            justOnce = false;
        }
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
        
        viewportWidth = this.width-startX;
        viewportHeight = this.height;
        
        gl.glViewport(startX, startY, viewportWidth, viewportHeight);        
        
        scene.setWidth(width-startX);
        scene.setHeight(height);
        
        if(isInit){
            scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);
        }
        
        updateCamera();
    }
    
    public void updateCamera(){
        
        if(scene.getCamera().isPerspective()){
            scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, scene.getCamera().getNearPersp(), scene.getCamera().getFarPersp());
        }else{
            //camera.initOrtho(0, width, height, 0, camera.getNearOrtho(), camera.getFarOrtho());
        
            scene.getCamera().setViewportWidth(viewportWidth);
            scene.getCamera().setViewportHeight(viewportHeight);
            
            //camera.initOrtho(-camera.getWidth()*0.5f, camera.getWidth()*0.5f, camera.getHeight()*0.5f, -camera.getHeight()*0.5f, camera.getNearOrtho(), camera.getFarOrtho());
            scene.getCamera().initOrtho(-((viewportWidth)/100), (viewportWidth)/100, viewportHeight/100, -(viewportHeight)/100, scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());

            
            scene.getCamera().setOrthographic(scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());
        }
        
    }
    
    private void initScene(final GL3 gl) throws Exception{
        
       
        
        try{
                     
            scene.init(gl);
            
            //VoxelSpaceInfos infos = voxelSpace.data.getVoxelSpaceInfos();
            
            //bounding-box
            /*GLMesh boundingBoxMesh = GLMeshFactory.createBoundingBox((float)infos.getMinCorner().x, 
                                                                    (float)infos.getMinCorner().y,
                                                                    (float)infos.getMinCorner().z,
                                                                    (float)infos.getMaxCorner().x, 
                                                                    (float)infos.getMaxCorner().y,
                                                                    (float)infos.getMaxCorner().z);
                                                                    
            SceneObject boundingBox = new SimpleSceneObject2(boundingBoxMesh, simpleShader.getProgramId(), false);
            
            boundingBox.setDrawType(GL3.GL_LINES);
            scene.addObject(boundingBox, gl);*/
            
            GLMesh axisMesh = GLMeshFactory.createMeshFromObj(new InputStreamReader(JoglListener.class.getClassLoader().getResourceAsStream("mesh/axis2.obj")),
                                            new InputStreamReader(JoglListener.class.getClassLoader().getResourceAsStream("mesh/axis2.mtl")));
            
            //GLMesh axisMesh = GLMeshFactory.createMeshFromX3D(new InputStreamReader(JoglListener.class.getClassLoader().getResourceAsStream("mesh/axis2.x3d")));
            
            //float volume = (int) (infos.getSplit().z * infos.getResolution());
            //axisMesh.setGlobalScale(0.03f*volume);
            
            /*SceneObject axis = new SimpleSceneObject(axisMesh, lightedShader.getProgramId(), false);
            axis.depthTest = false;
            Mat4F rotationMatrix = new Mat4F();
            
            float theta = (float) Math.toRadians(180);
            rotationMatrix.mat = new float[]{(float)Math.cos(theta), (float)-Math.sin(theta), 0, 0,
                                            (float)Math.sin(theta), (float)Math.cos(theta), 0, 0,
                                            0, 0, 1, 0,
                                            0, 0, 0, 1,
                                            };
            //axis.rotate(rotationMatrix);
            axis.translate(new Vec3F((float)voxelSpace.data.getVoxelSpaceInfos().getMinCorner().x, 
                                     (float)voxelSpace.data.getVoxelSpaceInfos().getMinCorner().y,
                                     (float)voxelSpace.data.getVoxelSpaceInfos().getMinCorner().z));
            
            axis.setDrawType(GL3.GL_TRIANGLES);
            scene.addObject(axis, gl);*/
            
            //génération des labels sur la bounding-box
            /*Texture textureLabel1 = Texture.createTextTexture(gl, "label 1");
            SceneObject label1 = SceneObjectFactory.createTexturedPlane(new Vec3F(0, 0, 0), textureLabel1, labelShader.getProgramId());
            label1.attachTexture(textureLabel1);
            
            rotationMatrix = new Mat4F();
            
            theta = (float) Math.toRadians(90);
            rotationMatrix.mat = new float[]{1, 0, 0, 0,
                                            0, (float)Math.cos(theta), (float)-Math.sin(theta), 0,
                                            0, (float)Math.sin(theta), (float)Math.cos(theta), 0,
                                            0, 0, 0, 1,
                                            };*/
            
            //label.rotate(rotationMatrix);
            //label1.translate(new Vec3F((float)voxelSpace.data.header.topCorner.x, (float)voxelSpace.data.header.bottomCorner.y, (float)voxelSpace.data.header.topCorner.z));
            //scene.addObject(label1, gl);
            
            Texture textureLabel2 = Texture.createTextTexture(gl, "label 2");
            SceneObject label2 = SceneObjectFactory.createTexturedPlane(new Vec3F(0, 0, 0), textureLabel2, scene.labelShader.getProgramId());
            label2.attachTexture(textureLabel2);
            //label2.translate(new Vec3F((float)voxelSpace.data.header.bottomCorner.x, (float)voxelSpace.data.header.bottomCorner.y, (float)voxelSpace.data.header.topCorner.z));
            //scene.addObject(label2, gl);
            
            Texture textureLabel3 = Texture.createTextTexture(gl, "label 3");
            SceneObject label3 = SceneObjectFactory.createTexturedPlane(new Vec3F(0, 0, 0), textureLabel3, scene.labelShader.getProgramId());
            label3.attachTexture(textureLabel3);
            //label3.translate(new Vec3F(0, 70, 0));
            //scene.addObject(label3, gl);
            
            fireSceneInitialized();
            
        }catch(Exception e){
            throw new Exception("error in scene initialization", e);
        }
        
    }

    public void setCuttingIncrementFactor(float cuttingIncrementFactor) {
        this.cuttingIncrementFactor = cuttingIncrementFactor;
    }
    
    
    public void cuttingPlane(boolean increase){
        
        Vec3F rightVector = scene.getCamera().getRightVector();
        Vec3F upVector = scene.getCamera().getUpVector();
        rightVector = Vec3F.normalize(rightVector);
        upVector = Vec3F.normalize(upVector);
        
        if(lastRightVector.x != rightVector.x || lastRightVector.y != rightVector.y || lastRightVector.z != rightVector.z){
            isCuttingInit = false;
        }
        
        lastRightVector = rightVector;
        
        //init
        if(!isCuttingInit){
            
            /*loc = scene.getCamera().getLocation();
            Point3d bottomCorner = scene.getVoxelSpace().data.getVoxelSpaceInfos().getMinCorner();
            Point3d topCorner = scene.getVoxelSpace().data.getVoxelSpaceInfos().getMaxCorner();
            AABB aabb = new AABB(new BoundingBox3F(new Point3F((float)bottomCorner.x,(float)bottomCorner.y,(float)bottomCorner.z),
                                               new Point3F((float)topCorner.x,(float)topCorner.y,(float)topCorner.z)));
            
            Point3F nearestPoint = aabb.getNearestPoint(new Point3F(loc.x, loc.y, loc.z));
            loc = new Vec3F(nearestPoint.x, nearestPoint.y, nearestPoint.z);
            isCuttingInit = true;*/
            
        }else{
            Vec3F forward = scene.getCamera().getForwardVector();
            Vec3F direction = Vec3F.normalize(forward);
            
            if(increase){
                loc = Vec3F.add(loc, Vec3F.multiply(direction, cuttingIncrementFactor));
            }else{
                loc = Vec3F.substract(loc, Vec3F.multiply(direction, cuttingIncrementFactor));
            }
            
        }
        
        
        Plane plane = new Plane(rightVector, upVector, new Point3F(loc.x, loc.y, loc.z));
        //System.out.println(loc.x+" "+loc.y+" "+loc.z);
        
        
        /*scene.getVoxelSpace().setCuttingPlane(plane);
        scene.getVoxelSpace().updateVao();*/
        drawNextFrame();
    }
    
    public void resetCuttingPlane(){
        
        /*scene.getVoxelSpace().clearCuttingPlane();
        scene.getVoxelSpace().updateVao();*/
        drawNextFrame();
        
        isCuttingInit = false;
        lastRightVector = new Vec3F();
    }
    
    private void resetMouseLocation(){
        
        eventListener.mouseXOldLocation = eventListener.mouseXCurrentLocation;
        eventListener.mouseYOldLocation = eventListener.mouseYCurrentLocation;
    }
    
    public void setViewToBack(){
        
        scene.getCamera().project(new Vec3F(scene.getCamera().getPivot().getPosition().x,
                                 scene.getCamera().getPivot().getPosition().y + getTargetDistance(),
                                 scene.getCamera().getPivot().getPosition().z),
                       new Vec3F(scene.getCamera().getPivot().getPosition().x,
                                 scene.getCamera().getPivot().getPosition().y,
                                 scene.getCamera().getPivot().getPosition().z));
        
        scene.getCamera().updateViewMatrix();
        
        resetMouseLocation();
        //scene.getCamera().notifyViewMatrixChanged();
        drawNextFrame();
    }
    
    public void setViewToFront(){
        
        scene.getCamera().project(new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                            scene.getCamera().getPivot().getPosition().y-getTargetDistance(),
                                            scene.getCamera().getPivot().getPosition().z), 
                
                        new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                scene.getCamera().getPivot().getPosition().y,
                                scene.getCamera().getPivot().getPosition().z));
        
        scene.getCamera().updateViewMatrix();
        
        resetMouseLocation();
        //scene.getCamera().notifyViewMatrixChanged();
        drawNextFrame();
    }
    
    public void setViewToLeft(){
        
        Vec3F location = scene.getCamera().getLocation();
        
        //if(location.x > 0){
            scene.getCamera().setLocation(new Vec3F(
                    scene.getCamera().getPivot().getPosition().x-getTargetDistance(), 
                    scene.getCamera().getPivot().getPosition().y, 
                    scene.getCamera().getPivot().getPosition().x));
        //}
        
        scene.getCamera().setTarget(new Vec3F(scene.getCamera().getLocation().x, 
                                            scene.getCamera().getLocation().y,
                                            scene.getCamera().getLocation().z));
        
        scene.getCamera().updateViewMatrix();
        
        resetMouseLocation();
        //scene.getCamera().notifyViewMatrixChanged();
        drawNextFrame();
    }
    
    public void setViewToBottom(){
        
        scene.getCamera().project(new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                    scene.getCamera().getPivot().getPosition().y,
                                    scene.getCamera().getPivot().getPosition().z-getTargetDistance()), 
                      new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                    scene.getCamera().getPivot().getPosition().y,
                                    scene.getCamera().getPivot().getPosition().z));
        
        scene.getCamera().updateViewMatrix();
        
        resetMouseLocation();
        //scene.getCamera().notifyViewMatrixChanged();
        drawNextFrame();
    }
    
    public void setViewToRight(){
        
        Vec3F location = scene.getCamera().getLocation();
        
        /*if(location.x < 0){
            camera.setLocation(new Vec3F(
                    scene.getVoxelSpace().getCenterX()+getTargetDistance(), 
                    scene.getVoxelSpace().getCenterY(),
                    scene.getVoxelSpace().getCenterZ()));
            
        }else if(location.x == 0){*/
            scene.getCamera().setLocation(new Vec3F(
                    scene.getCamera().getPivot().getPosition().x+getTargetDistance(),
                    scene.getCamera().getPivot().getPosition().y,
                    scene.getCamera().getPivot().getPosition().z));
        //}
        
        scene.getCamera().setTarget(new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                                      scene.getCamera().getLocation().y,
                                                      scene.getCamera().getLocation().z));
        
        scene.getCamera().updateViewMatrix();
        
        resetMouseLocation();
        //scene.getCamera().notifyViewMatrixChanged();
        drawNextFrame();
    }
    
    public void setViewToTop(){
        
        scene.getCamera().project(new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                                      scene.getCamera().getPivot().getPosition().y,
                                                      scene.getCamera().getPivot().getPosition().z+getTargetDistance()), 
                                        new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                                      scene.getCamera().getPivot().getPosition().y,
                                                      scene.getCamera().getPivot().getPosition().z));
        
        scene.getCamera().updateViewMatrix();
        
        resetMouseLocation();
        //scene.getCamera().notifyViewMatrixChanged();
        drawNextFrame();
    }
    
    public void setViewToOrthographic(){
        
        /*float objectDepth = Vec3F.dot(
                Vec3F.substract(
                        new Vec3F(voxelSpace.getCenterX(), voxelSpace.getCenterY(), voxelSpace.getCenterZ()),
                        camera.getLocation()),
                camera.getForwardVector());

        float cameraWidth = (2.0f / camera.getProjectionMatrix().mat[0]) * objectDepth;
        float cameraHeight = (2.0f / camera.getProjectionMatrix().mat[5]) * objectDepth;
            
        float ymax = (float) Math.tan(camera.getFovy() * Math.PI / 360.0f);
        float xmax = ymax * camera.getAspect();
        cameraWidth = objectDepth * xmax;
        cameraHeight = objectDepth * ymax;
        
        camera.setWidth(width);
        camera.setHeight(height);*/
        scene.getCamera().setOrthographic(scene.getCamera().getLeft(), scene.getCamera().getRight(), scene.getCamera().getTop(), scene.getCamera().getBottom(), scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());
        updateCamera();
        drawNextFrame();
    }
    
    public void setViewToOrthographic(float left, float right, float top, float bottom, float near, float far){
        
        scene.getCamera().setOrthographic(left, right, top, bottom, near, far);
        updateCamera();
        drawNextFrame();
    }
    
    public void setViewToPerspective(){
        
        scene.getCamera().setPerspective(scene.getCamera().getFovy(), scene.getCamera().getAspect(), scene.getCamera().getNearPersp(), scene.getCamera().getFarPersp());
        updateCamera();
        drawNextFrame();
    }
    
    public void setViewToPerspective(float fov, float near, float far){
        
        scene.getCamera().setPerspective(fov, scene.getCamera().getAspect(), near, far);
        updateCamera();
        drawNextFrame();
    }
    
    public void switchPerspective(){
        
        if(scene.getCamera().isPerspective()){
            setViewToOrthographic();
        }else{
            setViewToPerspective();
        }
    }
    
    private float getTargetDistance(){
        
        Vec3F location = scene.getCamera().getLocation();
        Vec3F center = new Vec3F(scene.getCamera().getPivot().getPosition().x, 
                                scene.getCamera().getPivot().getPosition().y,
                                scene.getCamera().getPivot().getPosition().z);
        
        return Vec3F.length(Vec3F.substract(location, center));
    }
}
