/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.listener;

import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.frame.GLRenderFrame;
import fr.ird.voxelidar.graphics3d.jogl.JoglListener;
import fr.ird.voxelidar.math.vector.Vec3F;
import java.awt.Robot;
import javax.media.opengl.GLEventListener;

/**
 *
 * @author Julien
 */
public class EventManager {
    
    public boolean mouseMoved;
    public boolean mouseMiddleButtonClicked;
    public boolean mouseWheelRotateUp;
    public boolean mouseWheelRotateDown;
    public boolean leftKeyPressed;
    public boolean rightKeyPressed;
    public boolean upKeyPressed;
    public boolean downKeyPressed;
    public boolean leftMousePressed;
    public boolean leftMouseDragged;
    public boolean rightMouseDragged;
    public boolean spaceKeyPressed;
    public boolean escapeKeyPressed;
    private int mouseXCurrentLocation;
    private int mouseYCurrentLocation;
    public int xrel, yrel;
    
    public boolean relativeMouseMode;
    private Robot robot;
    
    public int mouseXOldLocation;
    public int mouseYOldLocation;
    
    
    public int xOffsetOld;
    public int yOffsetOld;
    
    public int xOffset;
    public int yOffset;
    
    private int i=0;
    private final float mouseSpeed = 2f;
    
    public boolean leftMouseWasReleased;
    public boolean isMouseLocationUpdated;
    
    private final FPSAnimator animator;
    private final GLRenderFrame renderFrame;
    public final JoglListener joglContext;

    public void setMouseXCurrentLocation(int mouseXCurrentLocation) {
        mouseXOldLocation = this.mouseXCurrentLocation;
        this.mouseXCurrentLocation = mouseXCurrentLocation;
        isMouseLocationUpdated = true;
    }

    public void setMouseYCurrentLocation(int mouseYCurrentLocation) {
        mouseYOldLocation = this.mouseYCurrentLocation;
        this.mouseYCurrentLocation = mouseYCurrentLocation;
        isMouseLocationUpdated = true;
    }

    public int getMouseXCurrentLocation() {
        return mouseXCurrentLocation;
    }

    public int getMouseYCurrentLocation() {
        return mouseYCurrentLocation;
    }
    
    
    
    public EventManager(FPSAnimator animator, GLRenderFrame renderFrame, JoglListener context){
        
        this.animator = animator;
        this.renderFrame = renderFrame;
        this.joglContext = context;
        mouseMoved = false;
        mouseMiddleButtonClicked = false;
        mouseWheelRotateUp = false;
        mouseWheelRotateDown = false;
        rightKeyPressed = false;
        leftKeyPressed = false;
        leftMousePressed = false;
        leftMouseDragged = false;
        rightMouseDragged = false;
        spaceKeyPressed = false;
        relativeMouseMode = true;
        leftMouseWasReleased = false;
        escapeKeyPressed = false;
        isMouseLocationUpdated = false;
    }
    
    
    
    public void updateEvents(GLEventListener context){
        
        if(escapeKeyPressed){
            joglContext.getCamera().projectFront();
            /*
            animator.stop();
            renderFrame.setFullscreen(false);
            animator.start();
            */
        }
        
        if(leftMousePressed){
            
            //if left mouse button was released then it's pressing
            if(leftMouseWasReleased){
                
                mouseXOldLocation = mouseXCurrentLocation;
                mouseYOldLocation = mouseYCurrentLocation;

                leftMouseWasReleased = false;
            }
            
        }else{
            
            leftMouseWasReleased = true;
        }
        
        
        
        if(isMouseLocationUpdated && leftMousePressed){
            
            Vec3F center = new Vec3F(joglContext.getScene().getVoxelSpace().getCenterX(),joglContext.getScene().getVoxelSpace().getCenterY(),joglContext.getScene().getVoxelSpace().getCenterZ());
            xOffset = mouseXCurrentLocation - mouseXOldLocation;
            yOffset = mouseYCurrentLocation - mouseYOldLocation;
            
            
            if(xOffset != 0){
                joglContext.getCamera().rotateFromOrientation(new Vec3F(1.0f, 0.0f, 0.0f), center, (float) Math.toRadians(xOffset)*mouseSpeed);
            }
            if(yOffset != 0){
                joglContext.getCamera().rotateFromOrientation(new Vec3F(0.0f, 1.0f, 0.0f), center, (float) Math.toRadians(yOffset)*mouseSpeed);
            }
        }
        
        //translate the world
        if(rightMouseDragged){
            
            joglContext.getCamera().translate(new Vec3F(xrel, yrel, 0.0f));
        }
        
        if(mouseWheelRotateUp){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, 0.0f, 10.0f));
        }
        if(mouseWheelRotateDown){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, 0.0f, -10.0f));
        }
        
        if(rightKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(-10.0f, 0.0f, 0.0f));
        }
        
        if(leftKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(10.0f, 0.0f, 0.0f));
        }
        
        if(upKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, 10.0f, 0.0f));
        }
        
        if(downKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, -10.0f, 0.0f));
        }
        
        if(spaceKeyPressed){
            
            //joglContext.getCamera().target.z--;
            //joglContext.getCamera().updateViewMatrix();
            joglContext.getCamera().rotateAroundPoint(new Vec3F(0.0f,1.0f,0.0f), new Vec3F(0.0f,0.0f,0.0f), (float) Math.toRadians(5));
        }
        
        if(relativeMouseMode){
            
            /*
            try {
                robot = new Robot();
                //robot.mouseMove(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()/2, 
                //GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()/2);
            } catch (AWTException ex) {
                Logger.getLogger(EventListener.class.getName()).log(Level.SEVERE, null, ex);
            }
            */
        }
        
        if(!animator.isPaused() && !leftMousePressed){
            animator.pause();
            //System.out.println("animator paused");
        }
        
        mouseWheelRotateUp = false;
        mouseWheelRotateDown = false;
        leftMouseDragged = false;
        rightMouseDragged = false;
        escapeKeyPressed = false;
        isMouseLocationUpdated = false;
    }
    
    public void updateToolBox(){
        
        /*
        joglContext.toolBox.jTextFieldXCameraPosition.setText(String.valueOf(joglContext.getCamera().location.x));
        joglContext.toolBox.jTextFieldYCameraPosition.setText(String.valueOf(joglContext.getCamera().location.y));
        joglContext.toolBox.jTextFieldZCameraPosition.setText(String.valueOf(joglContext.getCamera().location.z));
        
        joglContext.toolBox.jTextFieldXCameraTarget.setText(String.valueOf(joglContext.getCamera().target.x));
        joglContext.toolBox.jTextFieldYCameraTarget.setText(String.valueOf(joglContext.getCamera().target.y));
        joglContext.toolBox.jTextFieldZCameraTarget.setText(String.valueOf(joglContext.getCamera().target.z));
        
        joglContext.toolBox.jTextFieldFovPerspectiveCamera.setText(String.valueOf(joglContext.getCamera().fovy));
        joglContext.toolBox.jTextFieldAspectPerspectiveCamera.setText(String.valueOf(joglContext.getCamera().aspect));
        joglContext.toolBox.jTextFieldNearPerspectiveCamera.setText(String.valueOf(joglContext.getCamera().nearPersp));
        joglContext.toolBox.jTextFieldFarPerspectiveCamera.setText(String.valueOf(joglContext.getCamera().farPersp));
        */
    }
}
