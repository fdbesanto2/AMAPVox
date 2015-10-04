/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.input;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.amapvox.voxviewer.event.BasicEvent;


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
        
        listener.ctrlPressed = ke.isControlDown();
        
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
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_EQUALS:    
                listener.plusKeyPressed = true;
                break;
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_MINUS:
                listener.minusKeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD1:
            case KeyEvent.VK_AMPERSAND:
                listener.number1KeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD3:
            case KeyEvent.VK_QUOTEDBL:
                listener.number3KeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD7:
            case 232:
                listener.number7KeyPressed = true;
                break;
            case KeyEvent.VK_NUMPAD5:
            case KeyEvent.VK_LEFT_PARENTHESIS:
                listener.number5KeyPressed = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent ke) {
        
        if(  ke.isAutoRepeat() ) {
            return;
        }
        
        listener.ctrlPressed = ke.isControlDown();
        
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
            case KeyEvent.VK_ADD:
            case KeyEvent.VK_EQUALS:  
                listener.plusKeyPressed = false;
                break;
            case KeyEvent.VK_SUBTRACT:
            case KeyEvent.VK_MINUS:
                listener.minusKeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD1:
            case KeyEvent.VK_AMPERSAND:
                listener.number1KeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD3:
            case KeyEvent.VK_QUOTEDBL:
                listener.number3KeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD7:
            case 232:
                listener.number7KeyPressed = false;
                break;
            case KeyEvent.VK_NUMPAD5:
            case KeyEvent.VK_LEFT_PARENTHESIS:
                listener.number5KeyPressed = false;
                break;
        }
    }
    
}
