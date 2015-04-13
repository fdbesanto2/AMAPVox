/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.event;

import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.engine3d.renderer.JoglListener;

/**
 * Abstract class that describes user input behavior
 * @author Julien
 */
public abstract class EventManager {
    
    /**
     * 
     */
    protected final FPSAnimator animator;

    /**
     *
     */
    protected final JoglListener joglContext;
    
    EventManager(FPSAnimator animator, JoglListener context)
    {
        this.animator = animator;
        this.joglContext = context;
    }
    
    /**
     * update events
     */
    void updateEvents(){};
}
