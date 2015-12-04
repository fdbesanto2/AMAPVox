/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.renderer;

import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.util.FPSAnimator;

/**
 *
 * @author calcul
 */
public class MinimalKeyAdapter extends KeyAdapter{

    private final FPSAnimator animator;
    
    public MinimalKeyAdapter(FPSAnimator animator) {
        this.animator = animator;
    }

    
    @Override
    public void keyPressed(KeyEvent e) {
        
        if(animator.isPaused()){
            animator.resume();
        }
    }
    
}
