/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.input;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.engine3d.event.BasicEvent;


/**
 * Class to handle keys states
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class InputKeyListener implements KeyListener{
    
    private final BasicEvent listener;
    private final FPSAnimator animator;
    
    public InputKeyListener(BasicEvent listener, FPSAnimator animator){
        
        this.listener = listener;
        this.animator = animator;
    }
    
    @Override
    public void keyPressed(KeyEvent ke) {
        
        if(animator.isPaused()){
            animator.resume();
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
            case KeyEvent.VK_Z:
                listener.zKeyPressed = true;
                break;
            case KeyEvent.VK_S:
                listener.sKeyPressed = true;
                break;
            case KeyEvent.VK_Q:
                listener.qKeyPressed = true;
                break;
            case KeyEvent.VK_D:
                listener.dKeyPressed = true;
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
            case KeyEvent.VK_Z:
                listener.zKeyPressed = false;
                break;
            case KeyEvent.VK_S:
                listener.sKeyPressed = false;
                break;
            case KeyEvent.VK_Q:
                listener.qKeyPressed = false;
                break;
            case KeyEvent.VK_D:
                listener.dKeyPressed = false;
                break;
        }
    }
    
}
