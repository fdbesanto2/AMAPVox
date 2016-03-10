/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.event;

import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.lidar.amapvox.voxviewer.input.InputKeyListener;
import fr.amap.lidar.amapvox.voxviewer.input.InputMouseAdapter;

/**
 * Abstract class that describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class EventManager {
        
    protected final InputMouseAdapter mouse;
    protected final InputKeyListener keyboard;
    
    public EventManager(InputMouseAdapter inputMouseAdapter, InputKeyListener inputKeyListener) {
        
        this.mouse = inputMouseAdapter;
        this.keyboard = inputKeyListener;
    }
    
    /**
     * update events
     */
    public abstract void updateEvents();

    public InputMouseAdapter getMouse() {
        return mouse;
    }

    public InputKeyListener getKeyboard() {
        return keyboard;
    }
}
