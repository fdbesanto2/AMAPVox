/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.frame;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 *
 * @author Julien
 */
public class ToolsJframeWindowListener implements WindowListener{
    
    GLRenderWindowListener renderWindowListener;
    
    public ToolsJframeWindowListener(GLRenderWindowListener renderWindowListener){
        
        this.renderWindowListener = renderWindowListener;
    }

    @Override
    public void windowOpened(WindowEvent e) {
        
    }

    @Override
    public void windowClosing(WindowEvent e) {
        
    }

    @Override
    public void windowClosed(WindowEvent e) {
        
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
        //renderWindowListener.isToolBoxFocused = true;
        
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        renderWindowListener.isToolBoxFocused = false;
    }
    
}
