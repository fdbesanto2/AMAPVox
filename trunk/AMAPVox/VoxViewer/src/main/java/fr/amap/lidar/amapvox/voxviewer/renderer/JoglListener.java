/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.renderer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.util.ByteConverter;
import fr.amap.lidar.amapvox.voxviewer.event.EventManager;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.lidar.amapvox.voxviewer.object.scene.Scene;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
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
    
    private int screenshotTexture;
    private boolean takeScreenShot;
    private RenderListener renderListener;
    
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
//        
//        screenshotTexture = new Texture();
//        try {
//            screenshotTexture.setWidth(width);
//            screenshotTexture.init(gl);
//        } catch (Exception ex) {
//            LOGGER.error("Cannot init screenshot texture.");
//        }


            // Génération d'une texture
            IntBuffer tmp = IntBuffer.allocate(1);
            gl.glGenTextures(1, tmp);
            screenshotTexture = tmp.get(0);
        
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
        
        if(takeScreenShot){

            // Binding de la texture pour pouvoir la modifier.
            gl.glBindTexture(GL.GL_TEXTURE_2D, screenshotTexture);

            // Création de la texture 2D vierge de la taille de votre fenêtre OpenGL
            gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, viewportWidth, viewportHeight, 0, GL.GL_RGB, GL.GL_BYTE, null);

            // Paramètrage de notre texture (étirement et filtrage)
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_NEAREST);
            gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_NEAREST);
            
            IntBuffer depthrenderbuffer = IntBuffer.allocate(1);
            gl.glGenRenderbuffers(1, depthrenderbuffer);
            gl.glBindRenderbuffer(GL.GL_RENDERBUFFER, depthrenderbuffer.get(0));
            gl.glRenderbufferStorage(GL.GL_RENDERBUFFER, GL.GL_DEPTH_COMPONENT32, viewportWidth, viewportHeight);
            gl.glFramebufferRenderbuffer(GL.GL_FRAMEBUFFER, GL.GL_DEPTH_ATTACHMENT, GL.GL_RENDERBUFFER, depthrenderbuffer.get(0));
            
            // Génération d'un second FBO
            IntBuffer tmp = IntBuffer.allocate(1);
            gl.glGenFramebuffers(1, tmp);
            
            
            // On bind le FBO
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, tmp.get(0));

            // Affectation de notre texture au FBO
            gl.glFramebufferTexture2D(GL.GL_FRAMEBUFFER, GL.GL_COLOR_ATTACHMENT0, GL.GL_TEXTURE_2D, screenshotTexture, 0);

            // Affectation d'un drawbuffer au FBO
            IntBuffer drawBuffers = IntBuffer.wrap(new int[]{GL.GL_COLOR_ATTACHMENT0});
            gl.glDrawBuffers(1, drawBuffers);  
            
            if(gl.glCheckFramebufferStatus(GL.GL_FRAMEBUFFER) != GL.GL_FRAMEBUFFER_COMPLETE){
                System.out.println("test");
            }
            
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
        
        if(takeScreenShot){
            
            // Binding de la texture pour pouvoir la modifier.
            gl.glBindTexture(GL.GL_TEXTURE_2D, screenshotTexture);
            
            ByteBuffer imgBuffer = Buffers.newDirectByteBuffer(viewportWidth * viewportHeight * 3); 
            gl.glGetTexImage(GL.GL_TEXTURE_2D, 0, GL.GL_RGB, GL.GL_BYTE, imgBuffer);
            
            BufferedImage screenShotImg = new BufferedImage(viewportWidth, viewportHeight, BufferedImage.TYPE_INT_RGB);
            
            int[] rgbArray = new int[viewportWidth*viewportHeight*3];
            int count = 0;
            while(imgBuffer.hasRemaining()){
                rgbArray[count] = imgBuffer.get()&0xff;
                rgbArray[count+1] = imgBuffer.get()&0xff;
                rgbArray[count+2] = imgBuffer.get()&0xff;
                count+=3;
            }
            
            screenShotImg.setRGB(0, 0, viewportWidth, viewportHeight, rgbArray, 0, viewportWidth);
            renderListener.screenshotIsReady(screenShotImg);
            
            takeScreenShot = false;
            gl.glBindFramebuffer(GL.GL_FRAMEBUFFER, 0);
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

    public void setTakeScreenShot(boolean takeScreenShot, RenderListener listener) {
        this.takeScreenShot = takeScreenShot;
        this.renderListener = listener;
    }
}
