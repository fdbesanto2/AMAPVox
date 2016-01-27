/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.event;

import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.lidar.amapvox.voxviewer.input.InputKeyListener;
import fr.amap.lidar.amapvox.voxviewer.input.InputMouseAdapter;

/**
 * Abstract class that describes user input behavior
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class EventManager {
    
    public boolean mouseMoved;
    public boolean mouseMiddleButtonClicked;
    public boolean mouseWheelRotateUp;
    public boolean mouseWheelRotateDown;
    
    public boolean leftKeyPressed;
    public boolean rightKeyPressed;
    public boolean upKeyPressed;
    public boolean downKeyPressed;
    
    public boolean zKeyPressed;
    public boolean sKeyPressed;
    public boolean qKeyPressed;
    public boolean dKeyPressed;
    
    public boolean plusKeyPressed;
    public boolean minusKeyPressed;
    
    public boolean number1KeyPressed;
    public boolean number3KeyPressed;
    public boolean number5KeyPressed;
    public boolean number7KeyPressed;
    
    public boolean ctrlPressed;
    
    public boolean leftMousePressed;
    public boolean rightMousePressed;
    public boolean middleMousePressed;
    public boolean leftMouseDragged;
    public boolean rightMouseDragged;
    public boolean spaceKeyPressed;
    public boolean escapeKeyPressed;
    public int mouseXCurrentLocation;
    public int mouseYCurrentLocation;
    public int xrel, yrel;
    
    public int mouseXOldLocation;
    public int mouseYOldLocation;
    
    
    public int xOffsetOld;
    public int yOffsetOld;
    
    public int xOffset;
    public int yOffset;
    
    private int i=0;
    protected final float mouseSpeed = 2.0f;
    
    public boolean leftMouseWasReleased;
    public boolean rightMouseWasReleased;
    public boolean isMouseLocationUpdated;
    
    /**
     * 
     */
    protected final FPSAnimator animator;
    
    EventManager(FPSAnimator animator) {
        this.animator = animator;
    }

    public void setMouseXCurrentLocation(int mouseXCurrentLocation) {
        mouseXOldLocation = this.mouseXCurrentLocation;
        this.mouseXCurrentLocation = mouseXCurrentLocation;
        isMouseLocationUpdated = true;
    }

    public void setMouseYCurrentLocation(int mouseYCurrentLocation) {
        mouseYOldLocation = this.mouseYCurrentLocation;
        this.mouseYCurrentLocation = mouseYCurrentLocation;
        isMouseLocationUpdated = true;
    }

    public int getMouseXCurrentLocation() {
        return mouseXCurrentLocation;
    }

    public int getMouseYCurrentLocation() {
        return mouseYCurrentLocation;
    }
    
    /**
     * update events
     */
    public void updateEvents(){};
}
