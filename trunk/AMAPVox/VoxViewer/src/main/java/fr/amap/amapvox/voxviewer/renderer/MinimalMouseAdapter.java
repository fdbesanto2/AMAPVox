/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.renderer;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.opengl.util.FPSAnimator;

/**
 *
 * @author calcul
 */
public class MinimalMouseAdapter extends MouseAdapter{

    private final FPSAnimator animator;
    
    public MinimalMouseAdapter(FPSAnimator animator) {
        this.animator = animator;
    }
    
    @Override
    public void mouseClicked(MouseEvent me) {
        
        if(animator.isPaused()){
            animator.resume();
        }
        
        if(me.getButton() == MouseEvent.BUTTON2){
            
            if(animator.isPaused()){
                animator.resume();
            }else{
                animator.pause();
            }
            
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
         if(animator.isPaused()){
            animator.resume();
        }
    }

    @Override
    public void mouseReleased(MouseEvent me) {
        
        if(!animator.isPaused()){
            animator.pause();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if(animator.isPaused()){
            animator.resume();
        }
    }

    @Override
    public void mouseWheelMoved(MouseEvent e) {
        if(animator.isPaused()){
            animator.resume();
        }
    }
    
}
