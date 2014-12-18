/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.listener;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;


/**
 *
 * @author Julien
 */
public class InputKeyListener implements KeyListener{
    
    private final EventManager listener;
    
    public InputKeyListener(EventManager listener){
        
        this.listener = listener;
         
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
        
        switch(ke.getKeyCode()){
            
            case KeyEvent.VK_LEFT:
                listener.leftKeyPressed = true;
                break;
            case KeyEvent.VK_RIGHT:
                listener.rightKeyPressed = true;
                break;
            case KeyEvent.VK_UP:
                listener.upKeyPressed = true;
                break;
            case KeyEvent.VK_DOWN:
                listener.downKeyPressed = true;
                break;
            case KeyEvent.VK_ENTER:
                listener.spaceKeyPressed = true;
                break;
            case KeyEvent.VK_ESCAPE:
                listener.escapeKeyPressed = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        
        if(  ke.isAutoRepeat() ) {
            return;
        }  
        
        switch(ke.getKeyCode()){
            
            case KeyEvent.VK_LEFT:
                listener.leftKeyPressed = false;
                break;
            case KeyEvent.VK_RIGHT:
                listener.rightKeyPressed = false;
                break;
            case KeyEvent.VK_UP:
                listener.upKeyPressed = false;
                break;
            case KeyEvent.VK_DOWN:
                listener.downKeyPressed = false;
                break;
            case KeyEvent.VK_ENTER:
                listener.spaceKeyPressed = false;
                break;
        }
    }
    
}
