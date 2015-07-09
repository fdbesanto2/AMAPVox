/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.event;

import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.voxviewer.renderer.JoglListener;
import java.awt.Robot;

/**
 * Describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class BasicEvent extends EventManager{
    
    public boolean mouseMoved;
    public boolean mouseMiddleButtonClicked;
    public boolean mouseWheelRotateUp;
    public boolean mouseWheelRotateDown;
    
    public boolean leftKeyPressed;
    public boolean rightKeyPressed;
    public boolean upKeyPressed;
    public boolean downKeyPressed;
    
    public boolean zKeyPressed;
    public boolean sKeyPressed;
    public boolean qKeyPressed;
    public boolean dKeyPressed;
    
    public boolean leftMousePressed;
    public boolean rightMousePressed;
    public boolean leftMouseDragged;
    public boolean rightMouseDragged;
    public boolean spaceKeyPressed;
    public boolean escapeKeyPressed;
    public int mouseXCurrentLocation;
    public int mouseYCurrentLocation;
    public int xrel, yrel;
    
    public boolean relativeMouseMode;
    private Robot robot;
    
    public int mouseXOldLocation;
    public int mouseYOldLocation;
    
    
    public int xOffsetOld;
    public int yOffsetOld;
    
    public int xOffset;
    public int yOffset;
    
    private Vec3F center;
    
    private int i=0;
    private final float mouseSpeed = 2.0f;
    
    public boolean leftMouseWasReleased;
    public boolean rightMouseWasReleased;
    public boolean isMouseLocationUpdated;

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
    
    
    
    public BasicEvent(FPSAnimator animator, JoglListener context){
        
        super(animator, context);
        
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
        
        center = new Vec3F();
    }
    
    
    @Override
    public void updateEvents(){
        
        if(escapeKeyPressed){
            joglContext.getCamera().setLocation(new Vec3F(0, 0, 0));
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
        
        if(rightMousePressed){
            
            //if left mouse button was released then it's pressing
            if(rightMouseWasReleased){
                
                mouseXOldLocation = mouseXCurrentLocation;
                mouseYOldLocation = mouseYCurrentLocation;

                rightMouseWasReleased = false;
            }
            
        }else{
            
            rightMouseWasReleased = true;
        }
        
        
        
        if(isMouseLocationUpdated && leftMousePressed){
            
            
            xOffset = mouseXCurrentLocation - mouseXOldLocation;
            yOffset = mouseYCurrentLocation - mouseYOldLocation;
            /*
            float x = 0;
            float y = 0;
            
            if(xOffset != 0){
                x = 1;
            }
            if(yOffset != 0){
                y = 1;
            }
            
            joglContext.getCamera().rotateFromOrientation(new Vec3F(x, y, 0.0f), null, (float) Math.toRadians(xOffset)*mouseSpeed);*/
            
            Vec3F.normalize(new Vec3F(xOffset, yOffset, mouseSpeed));
            
            if(xOffset != 0){
                joglContext.getCamera().rotateFromOrientation(new Vec3F(1.0f, 0.0f, 0.0f), null, (float) Math.toRadians(xOffset)*mouseSpeed);
            }
            if(yOffset != 0){
                joglContext.getCamera().rotateFromOrientation(new Vec3F(0.0f, 1.0f, 0.0f), null, (float) Math.toRadians(yOffset)*mouseSpeed);
            }
        }
        
        //translate the world
        if(rightMouseDragged){
            
            xOffset = mouseXCurrentLocation - mouseXOldLocation;
            yOffset = mouseYCurrentLocation - mouseYOldLocation;
            joglContext.getCamera().translateV2(new Vec3F(xOffset, yOffset, 0.0f));
        }
        
        if(mouseWheelRotateUp){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, 0.0f, 10.0f));
        }
        if(mouseWheelRotateDown){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, 0.0f, -10.0f));
        }
        
        if(rightKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(4.0f, 0.0f, 0.0f));
        }
        
        if(leftKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(-4.0f, 0.0f, 0.0f));
        }
        
        if(upKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, 4.0f, 0.0f));
        }
        
        if(downKeyPressed){
            
            joglContext.getCamera().translate(new Vec3F(0.0f, -4.0f, 0.0f));
        }
        
        if(dKeyPressed){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y+1, currentLightPosition.z));
        }
        
        if(qKeyPressed){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y-1, currentLightPosition.z));
        }
        
        if(zKeyPressed){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z+1));
        }
        
        if(sKeyPressed){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z-1));
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

    public void setCenter(Vec3F center) {
        this.center = center;
    }
}
