/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.frame;

import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import javax.media.nativewindow.util.Point;
import javax.swing.JFrame;

/**
 *
 * @author Julien
 */
public class GLRenderWindowListener implements WindowListener {

    private final JFrame toolsJFrame;
    private final GLWindow caller;
    private final FPSAnimator animator;
    
    public boolean isToolBoxFocused;
    
    public GLRenderWindowListener(GLWindow caller, JFrame toolsJFrame, FPSAnimator animator){
        
        this.toolsJFrame = toolsJFrame;
        this.caller = caller;
        this.animator = animator;
        this.isToolBoxFocused = false;
        
    }
    
    @Override
    public void windowResized(WindowEvent we) {
        
        //String name = Thread.currentThread().getName();
        if(!animator.isPaused()){
            animator.pause();
        }
        
    }

    @Override
    public void windowMoved(WindowEvent we) {
        
        if(!animator.isPaused() || !animator.isStarted()){
            animator.pause();
        }
        
        Point locationOnScreen = caller.getLocationOnScreen(null);
        toolsJFrame.setLocation(new java.awt.Point(locationOnScreen.getX()-275, locationOnScreen.getY()-20));
        
    }

    @Override
    public void windowDestroyNotify(WindowEvent we) {
        
    }

    @Override
    public void windowDestroyed(WindowEvent we) {
        
        animator.stop();
        toolsJFrame.dispose();
    }

    @Override
    public void windowGainedFocus(WindowEvent we) {
        
        System.out.println("glrenderwindow gained focus");
        
        if(!isToolBoxFocused && animator.isPaused()){
            animator.resume();
        }
        
        caller.requestFocus();
    }

    @Override
    public void windowLostFocus(WindowEvent we) {
        
        System.out.println("glrenderwindow losted focus");
        
        if(!isToolBoxFocused){
            animator.pause();
        }        
    }

    @Override
    public void windowRepaint(WindowUpdateEvent wue) {
        
    }
    
}
