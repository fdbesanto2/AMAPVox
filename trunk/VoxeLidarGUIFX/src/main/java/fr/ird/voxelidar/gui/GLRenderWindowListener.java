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
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 *
 * @author Julien
 */
public class GLRenderWindowListener extends WindowAdapter{

    private final Stage stage;
    private final FPSAnimator animator;
    
    public boolean isToolBoxFocused;
    
    public GLRenderWindowListener(Stage stage, FPSAnimator animator){
        
        this.stage = stage;
        
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
        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                //stage.toFront();
                stage.setX((int)locationOnScreen.getX()-stage.getWidth());
                stage.setY((int)locationOnScreen.getY());
            }
        });
        
    }
    
    @Override
    public void windowDestroyed(WindowEvent we) {
        
        animator.stop();
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                stage.close();
            }
        });
        
    }  
    
    @Override
    public void windowGainedFocus(WindowEvent we) {
        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if(!stage.isShowing()){
                    stage.toFront();
                }
            }
        });
    }
}
