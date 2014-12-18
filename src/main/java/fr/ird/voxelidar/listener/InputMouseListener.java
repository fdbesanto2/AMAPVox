/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.listener;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.util.FPSAnimator;

/**
 *
 * @author Julien
 */
public class InputMouseListener implements MouseListener{
    
    private final EventManager listener;
    private final FPSAnimator animator;
    
    private int lastPositionX, lastPositionY;
    
    
    public InputMouseListener(EventManager listener , FPSAnimator animator){
        
        this.listener = listener;
        this.animator = animator;
    }

    @Override
    public void mouseClicked(com.jogamp.newt.event.MouseEvent me) {
        
        if(animator.isPaused()){
            animator.resume();
        }
        
        if(me.getButton() == MouseEvent.BUTTON2){
            
            if(animator.isPaused()){
                animator.resume();
            }else{
                animator.pause();
            }
            
        }
    }

    @Override
    public void mouseEntered(com.jogamp.newt.event.MouseEvent me) {
        
    }

    @Override
    public void mouseExited(com.jogamp.newt.event.MouseEvent me) {
        
    }

    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent me) {
        
        if(me.getButton() == MouseEvent.BUTTON1){
            
            listener.leftMousePressed = true;
        }
    }

    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent me) {
        
        if(  me.isAutoRepeat() ) {
            return;
        }
        
        if(me.getButton() == MouseEvent.BUTTON1){
            
            listener.leftMousePressed = false;
        }
        
    }

    @Override
    public void mouseMoved(com.jogamp.newt.event.MouseEvent me) {
        
        listener.mouseMoved = true;
        listener.setMouseXCurrentLocation(me.getX());
        listener.setMouseYCurrentLocation(me.getY());
    }

    @Override
    public void mouseDragged(com.jogamp.newt.event.MouseEvent me) {
        
        switch(me.getButton()){
            
            case MouseEvent.BUTTON1:
                
                listener.leftMouseDragged = true;
                
                listener.setMouseXCurrentLocation(me.getX());
                listener.setMouseYCurrentLocation(me.getY());



                if(lastPositionX != listener.getMouseXCurrentLocation()){
                    listener.xrel = listener.getMouseXCurrentLocation() - lastPositionX;
                }else{
                    listener.xrel = 0;
                }

                if(lastPositionY != listener.getMouseYCurrentLocation()){
                    listener.yrel = listener.getMouseYCurrentLocation() - lastPositionY;
                }else{
                    listener.yrel = 0;
                }

                lastPositionX = listener.getMouseXCurrentLocation();
                lastPositionY = listener.getMouseYCurrentLocation();
                
                break;
                
            case MouseEvent.BUTTON3:
                listener.rightMouseDragged = true;
                
                listener.setMouseXCurrentLocation(me.getX());
                listener.setMouseYCurrentLocation(me.getY());



                if(lastPositionX != listener.getMouseXCurrentLocation()){
                    listener.xrel = listener.getMouseXCurrentLocation() - lastPositionX;
                }else{
                    listener.xrel = 0;
                }

                if(lastPositionY != listener.getMouseYCurrentLocation()){
                    listener.yrel = listener.getMouseYCurrentLocation() - lastPositionY;
                }else{
                    listener.yrel = 0;
                }

                lastPositionX = listener.getMouseXCurrentLocation();
                lastPositionY = listener.getMouseYCurrentLocation();
                
                break;
        }
        
        
    }

    @Override
    public void mouseWheelMoved(com.jogamp.newt.event.MouseEvent me) {
        
        float[] rotation = me.getRotation();
        float verticalRotation = rotation[1];
        
        if(verticalRotation > 0.0f){
            
            listener.mouseWheelRotateUp = true;
                    
        }else if(verticalRotation <0.0f){
            
            listener.mouseWheelRotateDown = true;
        }
        
    }
    
}
