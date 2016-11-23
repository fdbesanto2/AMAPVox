/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.renderer;

import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.opengl.util.FPSAnimator;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MinimalWindowAdapter extends WindowAdapter{

    private final FPSAnimator animator;
    
    public MinimalWindowAdapter(FPSAnimator animator){
        this.animator = animator;
    }
    
    @Override
    public void windowResized(WindowEvent we) {
        
        if(animator.isPaused()){
            animator.resume();
        }
        
    }
    
    @Override
    public void windowDestroyed(WindowEvent we) {
        
        if(animator.isAnimating()){
            animator.stop();
        }
    }
}
