/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.event;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.input.InputKeyListener;
import fr.amap.lidar.amapvox.voxviewer.input.InputMouseAdapter;
import fr.amap.lidar.amapvox.voxviewer.input.InputMouseAdapter.Button;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.lidar.amapvox.voxviewer.renderer.JoglListener;

/**
 * Describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class BasicEvent extends EventManager{
    
    private final JoglListener joglContext; 
    
    private int currentColorIndex = 0;
    private final InputMouseAdapter mouse;
    private final InputKeyListener keyboard;
    
    private int mouseX;
    private int mouseY;
    
    /**
     * 
     */
    protected final FPSAnimator animator;
    
    public BasicEvent(FPSAnimator animator, JoglListener context, InputMouseAdapter inputMouseAdapter, InputKeyListener inputKeyListener){
        
        super();
        
        this.animator = animator;
        this.joglContext = context;
        
        this.mouse = inputMouseAdapter;
        this.keyboard = inputKeyListener;
        
        /*mouseMoved = false;
        mouseMiddleButtonClicked = false;
        mouseWheelRotateUp = false;
        mouseWheelRotateDown = false;
        rightKeyPressed = false;
        leftKeyPressed = false;
        leftMousePressed = false;
        leftMouseDragged = false;
        rightMouseDragged = false;
        spaceKeyPressed = false;*/
        leftMouseWasReleased = false;
        //escapeKeyPressed = false;
        isMouseLocationUpdated = false;
    }
    
    @Override
    public void updateEvents(){
                
        /*if(mouse.isButtonDown(Button.LEFT) || mouse.isButtonDown(Button.RIGHT)
        && (mouse.getXLoc() != mouseX || mouse.getYLoc() != mouseY)){
            isMouseLocationUpdated = true;
            mouseXOldLocation = mouseX;
            mouseYOldLocation = mouseY;
        }*/
        
        //if(mouse.isDragged()){
            mouseXOldLocation = mouseX;
            mouseYOldLocation = mouseY;
        //}
        
        /*if(mouse.getXLoc() != mouseX || mouse.getYLoc() != mouseY){
            
            isMouseLocationUpdated = true;
            mouseXOldLocation = mouseX;
            mouseYOldLocation = mouseY;
        }*/
        
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
            
            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 0.0f, mouse.getWheelRotationValue()*5.0f));
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
        
        if(keyboard.isKeyDown(KeyEvent.VK_AMPERSAND) || keyboard.isKeyDown(KeyEvent.VK_NUMPAD1) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToBack();
            }else{
                joglContext.getScene().getCamera().setViewToFront();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_QUOTEDBL) || keyboard.isKeyDown(KeyEvent.VK_NUMPAD3) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToLeft();
            }else{
                joglContext.getScene().getCamera().setViewToRight();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_NUMPAD7) ){
            
            if(keyboard.isControlDown()){
                joglContext.getScene().getCamera().setViewToBottom();
                
            }else{
                joglContext.getScene().getCamera().setViewToTop();
            }
            
            resetMouseLocation();
            joglContext.refresh();
        }
        
        if(keyboard.isKeyDown(KeyEvent.VK_LEFT_PARENTHESIS) || keyboard.isKeyDown(KeyEvent.VK_NUMPAD5) ){
            joglContext.getScene().getCamera().switchPerspective();
            joglContext.updateCamera();
            resetMouseLocation();
            joglContext.refresh();
        }
        
        isMouseLocationUpdated = false;
        
        if(!animator.isPaused() && !joglContext.isDynamicDraw() &&
                !mouse.isButtonDown(Button.LEFT) && !mouse.isButtonDown(Button.RIGHT)){
            
            //this function cost time, it should not be called at each updateEvents method call
            animator.pause();
        }
        
    }
    
//    @Override
//    public void updateEvents(){
//        
//        
//        if(escapeKeyPressed){
//            joglContext.getScene().getCamera().setLocation(new Vec3F(0, 0, 0));
//        }
//        
//        if(leftMousePressed){
//            
//            //if left mouse button was released then it's pressing
//            if(leftMouseWasReleased){
//                
//                mouseXOldLocation = mouseXCurrentLocation;
//                mouseYOldLocation = mouseYCurrentLocation;
//
//                leftMouseWasReleased = false;
//            }
//            
//        }else{
//            
//            leftMouseWasReleased = true;
//        }
//        
//        if(rightMousePressed){
//            
//            //if left mouse button was released then it's pressing
//            if(rightMouseWasReleased){
//                
//                mouseXOldLocation = mouseXCurrentLocation;
//                mouseYOldLocation = mouseYCurrentLocation;
//
//                rightMouseWasReleased = false;
//            }
//            
//        }else{
//            
//            rightMouseWasReleased = true;
//        }
//        
//        if(middleMousePressed){
//            
//            joglContext.updateMousePicker();
//            middleMousePressed = false;
//        }
//        
//        
//        if(isMouseLocationUpdated && leftMousePressed){
//            
//            
//            xOffset = mouseXCurrentLocation - mouseXOldLocation;
//            yOffset = mouseYCurrentLocation - mouseYOldLocation;
//            /*
//            float x = 0;
//            float y = 0;
//            
//            if(xOffset != 0){
//                x = 1;
//            }
//            if(yOffset != 0){
//                y = 1;
//            }
//            
//            joglContext.getCamera().rotateFromOrientation(new Vec3F(x, y, 0.0f), null, (float) Math.toRadians(xOffset)*mouseSpeed);*/
//            //reset target
//            //SceneObject pivot = joglContext.getScene().getCamera().getPivot();
//            //joglContext.getScene().getCamera().setTarget(new Vec3F(pivot.getPosition().x, pivot.getPosition().y, pivot.getPosition().z));
//        
//            Vec3F.normalize(new Vec3F(xOffset, yOffset, mouseSpeed));
//            
//            if(xOffset != 0){
//                joglContext.getScene().getCamera().rotateFromOrientationV2(new Vec3F(1.0f, 0.0f, 0.0f), xOffset, yOffset);
//                //joglContext.getScene().getCamera().rotateFromOrientation(new Vec3F(1.0f, 0.0f, 0.0f), null, (float) Math.toRadians(xOffset)*mouseSpeed);
//            }
//            if(yOffset != 0){
//                joglContext.getScene().getCamera().rotateFromOrientationV2(new Vec3F(1.0f, 0.0f, 0.0f), xOffset, yOffset);
//                //joglContext.getScene().getCamera().rotateFromOrientation(new Vec3F(0.0f, 1.0f, 0.0f), null, (float) Math.toRadians(yOffset)*mouseSpeed);
//            }
//        }
//        
//        //translate the world
//        if(isMouseLocationUpdated && rightMousePressed){
//            
//            xOffset = mouseXCurrentLocation - mouseXOldLocation;
//            yOffset = mouseYCurrentLocation - mouseYOldLocation;
//            joglContext.getScene().getCamera().translateV2(new Vec3F(xOffset, yOffset, 0.0f));
//            
//        }
//        
//        if(mouseWheelRotateUp){
//            
//            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 0.0f, 5.0f));
//        }
//        if(mouseWheelRotateDown){
//            
//            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 0.0f, -5.0f));
//        }
//        
//        if(rightKeyPressed){
//            
//            joglContext.getScene().getCamera().translate(new Vec3F(4.0f, 0.0f, 0.0f));
//        }
//        
//        if(leftKeyPressed){
//            
//            joglContext.getScene().getCamera().translate(new Vec3F(-4.0f, 0.0f, 0.0f));
//        }
//        
//        if(upKeyPressed){
//            
//            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, 4.0f, 0.0f));
//        }
//        
//        if(downKeyPressed){
//            
//            joglContext.getScene().getCamera().translate(new Vec3F(0.0f, -4.0f, 0.0f));
//        }
//        
//        if(dKeyPressed){
//            
//            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
//            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y+1, currentLightPosition.z));
//        }
//        
//        if(qKeyPressed){
//            
//            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
//            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y-1, currentLightPosition.z));
//        }
//        
//        if(zKeyPressed){
//            
//            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
//            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z+1));
//        }
//        
//        if(sKeyPressed){
//            
//            Point3F currentLightPosition = joglContext.getScene().getLightPosition();
//            joglContext.getScene().setLightPosition(new Point3F(currentLightPosition.x, currentLightPosition.y, currentLightPosition.z-1));
//        }
//        
//        if(spaceKeyPressed){
//            
//            SceneObject firstSceneObject = joglContext.getScene().getFirstSceneObject();
//            if(firstSceneObject != null){
//                
//                if(currentColorIndex == 4){
//                    currentColorIndex = 0;
//                }else{
//                    currentColorIndex++;
//                }
//                
//                ((PointCloudSceneObject)firstSceneObject).switchToNextColor();
//            }
//            
//            //joglContext.getScene().getCamera().rotateAroundPoint(new Vec3F(0.0f,1.0f,0.0f), new Vec3F(0.0f,0.0f,0.0f), (float) Math.toRadians(5));
//        }
//        
//        if(plusKeyPressed){
//            //joglContext.cuttingPlane(true);
//        }
//        
//        if(minusKeyPressed){
//            //joglContext.cuttingPlane(false);
//        }
//        
//        if(number1KeyPressed){
//            
//            if(ctrlPressed){
//                joglContext.getScene().getCamera().setViewToBack();
//            }else{
//                joglContext.getScene().getCamera().setViewToFront();
//            }
//            
//            resetMouseLocation();
//            joglContext.refresh();
//        }
//        
//        if(number3KeyPressed){
//            
//            if(ctrlPressed){
//                joglContext.getScene().getCamera().setViewToLeft();
//            }else{
//                joglContext.getScene().getCamera().setViewToRight();
//            }
//            
//            resetMouseLocation();
//            joglContext.refresh();
//        }
//        
//        if(number7KeyPressed){
//            
//            if(ctrlPressed){
//                joglContext.getScene().getCamera().setViewToBottom();
//                
//            }else{
//                joglContext.getScene().getCamera().setViewToTop();
//            }
//            
//            resetMouseLocation();
//            joglContext.refresh();
//        }
//        
//        if(number5KeyPressed){
//            joglContext.getScene().getCamera().switchPerspective();
//            joglContext.updateCamera();
//            resetMouseLocation();
//            joglContext.refresh();
//            number5KeyPressed = false;
//        }
//        
//        if(!animator.isPaused() && !joglContext.isDynamicDraw() && (!leftMousePressed && !rightMousePressed)){
//            animator.pause();
//        }
//        
//        mouseWheelRotateUp = false;
//        mouseWheelRotateDown = false;
//        leftMouseDragged = false;
//        rightMouseDragged = false;
//        escapeKeyPressed = false;
//        isMouseLocationUpdated = false;
//        spaceKeyPressed = false;
//        
//    }
    
    private void resetMouseLocation(){
        
        mouseXOldLocation = mouse.getXLoc();
        mouseYOldLocation = mouse.getYLoc();
    }
}
