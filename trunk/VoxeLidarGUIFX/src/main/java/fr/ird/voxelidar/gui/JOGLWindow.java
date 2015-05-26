/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.gui;

import com.jogamp.nativewindow.util.Point;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.engine3d.event.BasicEvent;
import fr.ird.voxelidar.engine3d.input.InputKeyListener;
import fr.ird.voxelidar.engine3d.input.InputMouseAdapter;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.renderer.GLRenderFrame;
import fr.ird.voxelidar.engine3d.renderer.JoglListener;
import fr.ird.voxelidar.util.Settings;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class JOGLWindow{
        
    private final Logger logger = Logger.getLogger(JOGLWindow.class);
    private final GLRenderFrame renderFrame;
    private final JoglListener joglContext;
    private final FPSAnimator animator;  
    
    private int width;
    private int height;
    
    public JOGLWindow(int posX, int posY, int width, int height, String title, VoxelSpace voxelSpace, Settings settings) throws GLException, Exception{
        
        try{
            GLProfile glp = GLProfile.getGL2GL3();
            GLCapabilities caps = new GLCapabilities(glp);
            caps.setDoubleBuffered(true);

            this.width = width;
            this.height = height;
            
            renderFrame = GLRenderFrame.create(caps, posX, posY, width, height, title);
            

            animator = new FPSAnimator(renderFrame, 60);

            joglContext = new JoglListener(voxelSpace, settings, animator);
            BasicEvent eventListener = new BasicEvent(animator, joglContext);
            joglContext.attachEventListener(eventListener);
            
            joglContext.width = width;
            joglContext.height = height;

            renderFrame.addGLEventListener(joglContext);
            renderFrame.addKeyListener(new InputKeyListener(eventListener, animator));
            renderFrame.addMouseListener(new InputMouseAdapter(eventListener, animator));

            animator.start();
            
            
            
        }catch(GLException e){
            throw new GLException("Cannot init opengl", e);
        }catch(Exception e){
            throw new Exception("Unknown error happened", e);
        }
    }
    
    public void addWindowListener(WindowListener listener){
        renderFrame.addWindowListener(listener);
    }
    
    
    public Point getPosition(){
        Point locationOnScreen = renderFrame.getLocationOnScreen(null);
        return new Point(locationOnScreen.getX(), locationOnScreen.getY());
    }
    
    public void show(){
        this.setOnTop();
        renderFrame.setVisible(true);
    }
    
    public void setOnTop(){
        renderFrame.setAlwaysOnTop(true);
        renderFrame.setAlwaysOnTop(false);
    }

    public JoglListener getJoglContext() {
        return joglContext;
    }

    public FPSAnimator getAnimator() {
        return animator;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
}
