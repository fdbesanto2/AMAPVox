/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.event;

import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.lidar.amapvox.voxviewer.renderer.JoglListener;
import java.awt.Robot;

/**
 * Describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class BasicEvent extends EventManager{
    
    private final JoglListener joglContext; 
    
    private int currentColorIndex = 0;
    
    public BasicEvent(FPSAnimator animator, JoglListener context){
        
        super(animator);
        
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
        leftMouseWasReleased = false;
        escapeKeyPressed = false;
        isMouseLocationUpdated = false;
    }
    
    
    @Override
    public void updateEvents(){
        
        if(escapeKeyPressed){
            joglContext.getScene().getCamera().setLocation(new Vec3F(0, 0, 0));
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
        
        if(middleMousePressed){
            
            joglContext.updateMousePicker();
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
            //reset target
            //SceneObject pivot = joglContext.getScene().getCamera().getPivot();
            //joglContext.getScene().getCamera().setTarget(new Vec3F(pivot.getPosition().x, pivot.getPosition().y, pivot.getPosition().z));
        
            Vec3F.normalize(new Vec3F(xOffset, yOffset, mouseSpeed));
            
            if(xOffset != 0){
                joglContext.getScene().getCamera().rotateFromOrientationV2(new Vec3F(1.0f, 0.0f, 0.0f), xOffset, yOffset);
                //joglContext.getScene().getCamera().rotateFromOrientation(new Vec3F(1.0f, 0.0f, 0.0f), null, (float) Math.toRadians(xOffset)*mouseSpeed);
            }
            if(yOffset != 0){
                joglContext.getScene().getCamera().rotateFromOrientationV2(new Vec3F(1.0f, 0.0f, 0.0f), xOffset, yOffset);
                //joglContext.getScene().getCamera().rotateFromOrientation(new Vec3F(0.0f, 1.0f, 0.0f), null, (float) Math.toRadians(yOffset)*mouseSpeed);
            }
        }
        
        //translate the world
        if(isMouseLocationUpdated && rightMousePressed){
            
            xOffset = mouseXCurrentLocation - mouseXOldLocation;
            yOffset = mouseYCurrentLocation - mouseYOldLocation;
            joglContext.getScene().getCamera().translateV2(new Vec3F(xOffset, yOffset, 0.0f));
            
        }
        
        if(mouseWheelRotateUp){
            
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 0.0f, 5.0f));
        }
        if(mouseWheelRotateDown){
            
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 0.0f, -5.0f));
        }
        
        if(rightKeyPressed){
            
            joglContext.getScene().getCamera().translate(new Vec3F(4.0f, 0.0f, 0.0f));
        }
        
        if(leftKeyPressed){
            
            joglContext.getScene().getCamera().translate(new Vec3F(-4.0f, 0.0f, 0.0f));
        }
        
        if(upKeyPressed){
            
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 4.0f, 0.0f));
        }
        
        if(downKeyPressed){
            
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, -4.0f, 0.0f));
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
            
            SceneObject firstSceneObject = joglContext.getScene().getFirstSceneObject();
            if(firstSceneObject != null){
                
                if(currentColorIndex == 4){
                    currentColorIndex = 0;
                }else{
                    currentColorIndex++;
                }
                
                ((PointCloudSceneObject)firstSceneObject).switchToNextColor();
            }
            
            //joglContext.getScene().getCamera().rotateAroundPoint(new Vec3F(0.0f,1.0f,0.0f), new Vec3F(0.0f,0.0f,0.0f), (float) Math.toRadians(5));
        }
        
        if(plusKeyPressed){
            //joglContext.cuttingPlane(true);
        }
        
        if(minusKeyPressed){
            //joglContext.cuttingPlane(false);
        }
        
        if(number1KeyPressed){
            
            if(ctrlPressed){
                joglContext.getScene().getCamera().setViewToBack();
            }else{
                joglContext.getScene().getCamera().setViewToFront();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(number3KeyPressed){
            
            if(ctrlPressed){
                joglContext.getScene().getCamera().setViewToLeft();
            }else{
                joglContext.getScene().getCamera().setViewToRight();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(number7KeyPressed){
            
            if(ctrlPressed){
                joglContext.getScene().getCamera().setViewToBottom();
                
            }else{
                joglContext.getScene().getCamera().setViewToTop();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(number5KeyPressed){
            joglContext.getScene().getCamera().switchPerspective();
            joglContext.updateCamera();
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(!animator.isPaused() && !joglContext.isDynamicDraw() && (!leftMousePressed && !rightMousePressed)){
            animator.pause();
        }
        
        mouseWheelRotateUp = false;
        mouseWheelRotateDown = false;
        leftMouseDragged = false;
        rightMouseDragged = false;
        escapeKeyPressed = false;
        isMouseLocationUpdated = false;
        spaceKeyPressed = false;
        
    }
    
    private void resetMouseLocation(){
        
        mouseXOldLocation = mouseXCurrentLocation;
        mouseYOldLocation = mouseYCurrentLocation;
    }
}
