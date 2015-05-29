/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.renderer;

import com.jogamp.nativewindow.WindowClosingProtocol;
import com.jogamp.newt.NewtFactory;
import com.jogamp.newt.Window;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.GLCapabilities;
/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class GLRenderFrame extends GLWindow{
    
    public int width;
    public int height;
    
    private GLRenderFrame(Window w){
        
        super(w);
    }
    
    public static GLRenderFrame create(GLCapabilities caps, int posX, int posY, int width, int height, String title){
        
                
        GLRenderFrame viewer = new GLRenderFrame(NewtFactory.createWindow(caps)); 
        viewer.setTitle("3D viewer - "+title);
        
        //viewer.setVisible(true); 
        viewer.setSize(width, height);
        viewer.width = width;
        viewer.height = height;
        viewer.setPosition(posX, posY);
        /*viewer.setPosition((GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getWidth()/2)-320,
                           (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDisplayMode().getHeight()/2)-240);*/
        //viewer.setAlwaysOnTop(true); 
        viewer.setDefaultCloseOperation(WindowClosingProtocol.WindowClosingMode.DISPOSE_ON_CLOSE);

        return viewer; 
    }

}
