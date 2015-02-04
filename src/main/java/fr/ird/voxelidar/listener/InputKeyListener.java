/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.listener;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.util.FPSAnimator;


/**
 *
 * @author Julien
 */
public class InputKeyListener implements KeyListener{
    
    private final EventManager listener;
    private final FPSAnimator animator;
    
    public InputKeyListener(EventManager listener, FPSAnimator animator){
        
        this.listener = listener;
        this.animator = animator;
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
        
        if(animator.isPaused()){
            animator.resume();
            //System.out.println("animator resumed");
        }
        
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
        
        if(!animator.isPaused()){
            //animator.pause();
            //System.out.println("animator paused");
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
