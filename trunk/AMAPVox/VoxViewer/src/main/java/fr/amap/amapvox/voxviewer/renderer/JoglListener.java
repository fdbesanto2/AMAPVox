/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.renderer;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.raster.asc.RegularDtm;
import fr.amap.amapvox.voxviewer.event.EventManager;
import fr.amap.amapvox.voxviewer.object.scene.MousePicker;
import fr.amap.amapvox.voxviewer.object.scene.Scene;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class JoglListener implements GLEventListener {
    
    private EventManager eventListener;
    private final Scene scene;
    private Vec3F worldColor;
    
    private int width;
    private int height;
    
    public int viewportWidth;
    public int viewportHeight;
    
    private int startX = 0;
    private int startY = 0;
    
    private boolean justOnce = true;
    private final FPSAnimator animator;
    private boolean isInit;
    
    
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
    }
    
    public void attachEventListener(EventManager eventListener){
        
        this.eventListener = eventListener;
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        
        isInit = true;
                
        GL3 gl = drawable.getGL().getGL3();
        
        String extensions = gl.glGetString(GL3.GL_EXTENSIONS);
        
        try {
            scene.init(gl);
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
        
        /*
        glViewport specifies the affine transformation of x and y from normalized 
        device coordinates to window coordinates. 
        Let x nd y nd be normalized device coordinates. 
        Then the window coordinates x w y w are computed as follows:

        x w = x nd + 1 ⁢* width * 2 + x
        y w = y nd + 1 ⁢* height * 2 + y
        */
        gl.glViewport(startX, startY, viewportWidth, viewportHeight);
        
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT|GL3.GL_COLOR_BUFFER_BIT);
        gl.glClearColor(worldColor.x, worldColor.y, worldColor.z, 1.0f);
        
        gl.glDisable(GL3.GL_BLEND);
        
        if(eventListener != null){
            eventListener.updateEvents();
        }
        
        
        
        isInit = false;
        
        scene.draw(gl);
        
        if(justOnce){ //draw one single frame
            animator.pause();
            justOnce = false;
        }
    }
    
    /**
     * Draw the next frame
     */
    public void refresh(){
        
        justOnce = true;
        
        animator.resume();
        
    }
    
    public void updateMousePicker(){
        
        scene.updateMousePicker(eventListener.getMouseXCurrentLocation(), eventListener.getMouseYCurrentLocation(), viewportWidth, viewportHeight);
        
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        
        this.width = width;
        this.height = height;
        
        GL3 gl=drawable.getGL().getGL3();
        
        viewportWidth = this.width-startX;
        viewportHeight = this.height;
        
        gl.glViewport(startX, startY, viewportWidth, viewportHeight);
        
        if(isInit){
            scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);
        }
        
        updateCamera();
    }
    
    
    public void updateCamera() {

        if (scene.getCamera().isPerspective()) {
            scene.getCamera().setViewportWidth(viewportWidth);
            scene.getCamera().setViewportHeight(viewportHeight);
            scene.getCamera().setPerspective(60.0f, (1.0f * this.width - startX) / height, scene.getCamera().getNearPersp(), scene.getCamera().getFarPersp());
        } else {
            //camera.initOrtho(0, width, height, 0, camera.getNearOrtho(), camera.getFarOrtho());


            //camera.initOrtho(-camera.getWidth()*0.5f, camera.getWidth()*0.5f, camera.getHeight()*0.5f, -camera.getHeight()*0.5f, camera.getNearOrtho(), camera.getFarOrtho());
            scene.getCamera().initOrtho(-((viewportWidth) / 100), (viewportWidth) / 100, viewportHeight / 100, -(viewportHeight) / 100, scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());

            scene.getCamera().setOrthographic(scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());
        }

    }

    public int getStartX() {
        return startX;
    }

    public void setStartX(int startX) {
        
        this.startX = startX;
        
        viewportWidth = this.width-startX;
        viewportHeight = this.height;
        
        scene.getCamera().setViewportWidth(viewportWidth);
        scene.getCamera().setViewportHeight(viewportHeight);
        
        scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }
    
}
