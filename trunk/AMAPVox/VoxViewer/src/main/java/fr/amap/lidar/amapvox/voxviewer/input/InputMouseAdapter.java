/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.input;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import fr.amap.lidar.amapvox.voxviewer.event.EventManager;

/**
 * Class to handle mouse states
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class InputMouseAdapter extends MouseAdapter{
    
    private final EventManager listener;
    
    private int lastPositionX, lastPositionY;
    
    
    public InputMouseAdapter(EventManager listener){
        
        this.listener = listener;
    }

    @Override
    public void mousePressed(com.jogamp.newt.event.MouseEvent me) {
                
        if(me.getButton() == MouseEvent.BUTTON1){
            
            listener.leftMousePressed = true;
        }
        
        if(me.getButton() == MouseEvent.BUTTON3){
            
            listener.rightMousePressed = true;
        }
        
        if(me.getButton() == MouseEvent.BUTTON2){
            
            listener.middleMousePressed = true;
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
        
        if(me.getButton() == MouseEvent.BUTTON3){
            
            listener.rightMousePressed = false;
        }
        
        if(me.getButton() == MouseEvent.BUTTON2){
            
            listener.middleMousePressed = false;
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
