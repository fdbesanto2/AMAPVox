/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.renderer;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.Point;
import javafx.application.Platform;
import javafx.stage.Stage;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class GLRenderWindowListener extends WindowAdapter{

    private final Stage toolboxStage;
    private final FPSAnimator animator;
    
    public boolean isToolBoxFocused;
    private double maxToolBoxHeight;
    
    public GLRenderWindowListener(Stage stage, FPSAnimator animator){
        
        this.toolboxStage = stage;
        
        if(toolboxStage != null){
            maxToolBoxHeight = toolboxStage.getHeight();
        }
        
        this.animator = animator;
        this.isToolBoxFocused = false;
        
    }
    
    @Override
    public void windowResized(WindowEvent we) {
        
        Window window = (Window)we.getSource();
        final int height = window.getHeight();
        
        if(toolboxStage != null){
        
            Platform.runLater(new Runnable() {

                @Override
                public void run() {

                    if(height < maxToolBoxHeight){
                        toolboxStage.setHeight(height);
                    }else{
                        toolboxStage.setHeight(maxToolBoxHeight);
                    }

                }
            });
        }
        
        if(animator.isPaused()){
            animator.resume();
        }
        
    }

    @Override
    public void windowMoved(WindowEvent we) {
                
        Window window = (Window)we.getSource();
        
        final Point locationOnScreen = new Point(window.getX(), window.getY());
        
        if(toolboxStage != null){
            Platform.runLater(new Runnable() {
        
                @Override
                public void run() {
                    //stage.toFront();
                    toolboxStage.setX((int)locationOnScreen.getX());
                    //toolboxStage.setX((int)locationOnScreen.getX()-toolboxStage.getWidth());
                    toolboxStage.setY((int)locationOnScreen.getY());
    }
            });
        }
    
    }
    
    @Override
    public void windowDestroyed(WindowEvent we) {
        
        animator.stop();
        
        if(toolboxStage != null){
            Platform.runLater(new Runnable() {

                @Override
                public void run() {
                    toolboxStage.close();
    }  
            });
        }
    }  
    
    @Override
    public void windowGainedFocus(WindowEvent we) {
        
        /*
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                if(!toolboxStage.isShowing()){
                    toolboxStage.toFront();
                }
            }
        });*/
    }
    
    @Override
    public void windowLostFocus(WindowEvent we) {
        
    }
}
