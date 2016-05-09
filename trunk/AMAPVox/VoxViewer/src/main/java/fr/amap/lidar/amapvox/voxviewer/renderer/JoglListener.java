/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.renderer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.event.EventManager;
import fr.amap.lidar.amapvox.voxviewer.object.scene.Scene;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class JoglListener implements GLEventListener {
    
    private final static Logger LOGGER = Logger.getLogger(JoglListener.class);
    
    private final List<EventManager> eventListeners;
    private final Scene scene;
    private Vec3F worldColor;
    private final static Vec3F DEFAULT_WORLD_COLOR = new Vec3F(0.78f, 0.78f, 0.78f);
    
    private int width;
    private int height;
    
    public int viewportWidth;
    public int viewportHeight;
    
    private int startX = 0;
    private int startY = 0;
    
    private boolean justOnce = true;
    private final FPSAnimator animator;
    private boolean isInit;
    private boolean dynamicDraw = false;
    
    public Scene getScene() {
        return scene;
    }

    public Vec3F getWorldColor() {
        return worldColor;
    }

    public void setWorldColor(Vec3F worldColor) {
        this.worldColor = worldColor;
    }

    public List<EventManager> getEventListeners() {
        return eventListeners;
    }

    
    
    public JoglListener(FPSAnimator animator){
        
        scene = new Scene();
        eventListeners = new ArrayList<>();
        this.animator = animator;
        worldColor = DEFAULT_WORLD_COLOR;
    }
    
    public void addEventListener(EventManager eventListener){
        
        if(eventListener != null){
            eventListeners.add(eventListener);
        }
    }
    
    public void removeEventListener(EventManager eventListener){
        
        if(eventListener != null){
            eventListeners.remove(eventListener);
        }
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        
        isInit = true;
                
        GL gl_base = drawable.getGL();
        
        IntBuffer majorVersion = IntBuffer.allocate(1);
        gl_base.glGetIntegerv(GL3.GL_MAJOR_VERSION, majorVersion);
        
        int majVersion = majorVersion.get();
        if(majVersion < 3){
            LOGGER.error("Opengl major version is "+majVersion+", this value should be higher than 3.\n"
                        + "Try to update the driver of the graphic card.");
            
            drawable.destroy();
            return;
        }
        
        GL3 gl = gl_base.getGL3();
        
        //String extensions = gl.glGetString(GL3.GL_EXTENSIONS);
        
        try {
            scene.init(gl);
        } catch (Exception ex) {
            throw ex;
        }
        
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA );
                
        gl.glEnable(GL.GL_DEPTH_TEST);
        gl.glDepthFunc(GL.GL_LEQUAL);
                
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
        From doc:
        glViewport specifies the affine transformation of x and y from normalized 
        device coordinates to window coordinates. 
        Let x nd y nd be normalized device coordinates. 
        Then the window coordinates x w y w are computed as follows:

        x w = x nd + 1 ⁢* width * 2 + x
        y w = y nd + 1 ⁢* height * 2 + y
        */
        
        for(EventManager eventManager : eventListeners){
            eventManager.updateEvents();
        }
        
        gl.glViewport(startX, startY, viewportWidth, viewportHeight);
        
        //specify clear values for the color buffers (must be called before glClear)
        gl.glClearColor(worldColor.x, worldColor.y, worldColor.z, 1.0f);
        
        gl.glClear(GL.GL_DEPTH_BUFFER_BIT|GL.GL_COLOR_BUFFER_BIT);
        
        gl.glDisable(GL.GL_BLEND);
        
        isInit = false;
        
        scene.draw(gl);
        
        if(justOnce && !dynamicDraw){ //draw one single frame
            animator.pause();
            justOnce = false;
        }
    }
    
    /**
     * Draw the next frame
     */
    public void refresh(){
        
        justOnce = true;
        
        if(animator.isPaused()){
            animator.resume();
        }
        
    }
    
    public void updateMousePicker(int mouseXLoc, int mouseYLoc){
        
        scene.updateMousePicker(mouseXLoc, mouseYLoc, startX, startY, viewportWidth, viewportHeight);
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        
        this.width = width;
        this.height = height;
        
        GL3 gl=drawable.getGL().getGL3();
        
        viewportWidth = this.width-startX;
        viewportHeight = this.height;
        
        gl.glViewport(startX, startY, viewportWidth, viewportHeight);
        
        /*if(isInit){
            scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);
        }*/
        
        updateCamera();
    }
    
    
    public void updateCamera() {
        
        scene.getCamera().setViewportWidth(viewportWidth);
        scene.getCamera().setViewportHeight(viewportHeight);

        if (scene.getCamera().isPerspective()) {
            
            scene.getCamera().setPerspective(60.0f, (1.0f * this.width - startX) / height, scene.getCamera().getNearPersp(), scene.getCamera().getFarPersp());
        } else {

            //scene.getCamera().initOrtho(-1, 1, 1, -1, scene.getCamera().getNearOrtho(), scene.getCamera().getFarOrtho());
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
        
        updateCamera();
        
        /*scene.getCamera().setViewportWidth(viewportWidth);
        scene.getCamera().setViewportHeight(viewportHeight);
        
        scene.getCamera().setPerspective(60.0f, (1.0f*this.width-startX)/height, 1.0f, 1000.0f);*/
    }

    public int getStartY() {
        return startY;
    }

    public void setStartY(int startY) {
        this.startY = startY;
    }

    public boolean isDynamicDraw() {
        return dynamicDraw;
    }

    public void setDynamicDraw(boolean dynamicDraw) {
        this.dynamicDraw = dynamicDraw;
    }
}
