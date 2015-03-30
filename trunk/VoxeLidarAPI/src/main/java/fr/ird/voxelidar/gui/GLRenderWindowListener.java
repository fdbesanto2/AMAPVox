/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.gui;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Point;

/**
 *
 * @author Julien
 */
public class GLRenderWindowListener extends WindowAdapter{

    private final JFrameTools toolsJFrame;
    private final FPSAnimator animator;
    
    public boolean isToolBoxFocused;
    
    public GLRenderWindowListener(JFrameTools toolsJFrame, FPSAnimator animator){
        
        this.toolsJFrame = toolsJFrame;
        
        this.animator = animator;
        this.isToolBoxFocused = false;
        
    }
    
    @Override
    public void windowResized(WindowEvent we) {
        
        //String name = Thread.currentThread().getName();
        if(animator.isPaused()){
            animator.resume();
        }
        
    }

    @Override
    public void windowMoved(WindowEvent we) {
                
        Window window = (Window)we.getSource();
        
        Point locationOnScreen = new Point(window.getX(), window.getY());
        toolsJFrame.setLocation(new java.awt.Point((int)locationOnScreen.getX()-275, (int)locationOnScreen.getY()-20));
        
        
    }
    
    @Override
    public void windowDestroyed(WindowEvent we) {
        
        animator.stop();
        toolsJFrame.dispose();
    }  
}
