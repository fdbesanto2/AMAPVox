/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.event;

import com.jogamp.newt.event.KeyEvent;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.input.InputKeyListener;
import fr.amap.lidar.amapvox.voxviewer.input.InputMouseAdapter;
import fr.amap.lidar.amapvox.voxviewer.input.InputMouseAdapter.Button;
import fr.amap.lidar.amapvox.voxviewer.renderer.JoglListener;

/**
 * Describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class BasicEvent extends EventManager{
    
    private final JoglListener joglContext;
    
    private int mouseX;
    private int mouseY;
    
    private int mouseXOldLocation;
    private int mouseYOldLocation;
    
    public BasicEvent(JoglListener context, InputMouseAdapter inputMouseAdapter, InputKeyListener inputKeyListener){
        
        super(inputMouseAdapter, inputKeyListener);
        
        this.joglContext = context;
    }
    
    @Override
    public void updateEvents(){
                
        mouseXOldLocation = mouseX;
        mouseYOldLocation = mouseY;
        
        mouseX = mouse.getXLoc();
        mouseY = mouse.getYLoc();
        
        int deltaX = mouseX - mouseXOldLocation;
        int deltaY = mouseY - mouseYOldLocation;
        
        if(mouse.isButtonClicked(Button.MIDDLE)){
            
            joglContext.updateMousePicker(mouse.getXLoc(), mouse.getYLoc());
        }
        
        
        if(mouse.isButtonDown(Button.LEFT) && mouse.isDragged()){
            
            if(deltaX != 0 || deltaY != 0){
                joglContext.getScene().getCamera().rotateFromOrientationV2(new Vec3F(1.0f, 0.0f, 0.0f), deltaX*0.5f, deltaY*0.5f);
            }
        }
        
        //translate the world
        if(mouse.isButtonDown(Button.RIGHT) && mouse.isDragged()){
                       
            if(deltaX != 0 || deltaY != 0){
                if(Math.abs(deltaX) > 200 || Math.abs(deltaY) > 200){
                    System.out.println(deltaX+"\t"+deltaY);
                }
                joglContext.getScene().getCamera().translateV2(new Vec3F(deltaX*0.5f, deltaY*0.5f, 0.0f));
                
            }
        }
        
        if(mouse.isWheelRotateUp() || mouse.isWheelRotateDown()){
            
            float distanceToTarget = joglContext.getScene().getCamera().getDistanceToTarget();
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 0.0f, (distanceToTarget/5.0f)*mouse.getWheelRotationValue()/**mouseScrollSensitivity*/));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_RIGHT)){
            
            joglContext.getScene().getCamera().translate(new Vec3F(4.0f, 0.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_LEFT)){
            
            joglContext.getScene().getCamera().translate(new Vec3F(-4.0f, 0.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_UP)){
            
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 4.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_DOWN)){
            
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, -4.0f, 0.0f));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_D)){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y+1, currentLightPosition.z));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_Q)){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y-1, currentLightPosition.z));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_Z)){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z+1));
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_S)){
            
            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z-1));
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_AMPERSAND) || keyboard.isKeyClicked(KeyEvent.VK_NUMPAD1) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToBack();
            }else{
                joglContext.getScene().getCamera().setViewToFront();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_QUOTEDBL) || keyboard.isKeyClicked(KeyEvent.VK_NUMPAD3) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToLeft();
            }else{
                joglContext.getScene().getCamera().setViewToRight();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_NUMPAD7) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToBottom();
                
            }else{
                joglContext.getScene().getCamera().setViewToTop();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyClicked(KeyEvent.VK_LEFT_PARENTHESIS) || keyboard.isKeyClicked(KeyEvent.VK_NUMPAD5) ){
            joglContext.getScene().getCamera().switchPerspective();
            joglContext.updateCamera();
            resetMouseLocation();
            joglContext.refresh();
        }
    }
    
    private void resetMouseLocation(){
        
        mouseXOldLocation = mouse.getXLoc();
        mouseYOldLocation = mouse.getYLoc();
    }
}
