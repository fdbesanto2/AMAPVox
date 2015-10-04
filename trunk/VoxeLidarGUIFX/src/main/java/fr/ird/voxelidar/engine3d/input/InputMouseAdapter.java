/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.input;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.engine3d.event.BasicEvent;

/**
 * Class to handle mouse states
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class InputMouseAdapter extends MouseAdapter{
    
    private final BasicEvent listener;
    private final FPSAnimator animator;
    
    private int lastPositionX, lastPositionY;
    
    
    public InputMouseAdapter(BasicEvent listener , FPSAnimator animator){
        
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
    public void mousePressed(com.jogamp.newt.event.MouseEvent me) {
        
        if(animator.isPaused()){
            animator.resume();
        }
        
        if(me.getButton() == MouseEvent.BUTTON1){
            
            listener.leftMousePressed = true;
        }
        
        if(me.getButton() == MouseEvent.BUTTON3){
            
            listener.rightMousePressed = true;
        }
    }

    @Override
    public void mouseReleased(com.jogamp.newt.event.MouseEvent me) {
        
        if(!animator.isPaused()){
            animator.pause();
        }
        
        if(  me.isAutoRepeat() ) {
            return;
        }
        
        if(me.getButton() == MouseEvent.BUTTON1){
            
            listener.leftMousePressed = false;
        }
        
        if(me.getButton() == MouseEvent.BUTTON3){
            
            listener.rightMousePressed = false;
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
        
        if(animator.isPaused()){
            animator.resume();
        }
        
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
        
        if(animator.isPaused()){
            animator.resume();
        }
        
        float[] rotation = me.getRotation();
        float verticalRotation = rotation[1];
        
        if(verticalRotation > 0.0f){
            
            listener.mouseWheelRotateUp = true;
                    
        }else if(verticalRotation <0.0f){
            
            listener.mouseWheelRotateDown = true;
        }
        
    }
    
}
