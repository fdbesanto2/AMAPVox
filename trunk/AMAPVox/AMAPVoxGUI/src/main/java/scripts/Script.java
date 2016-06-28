/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import fr.amap.lidar.amapvox.gui.MainFrameController;

/**
 *
 * @author Julien Heurtebize
 */
public abstract class Script {
    
    protected final MainFrameController controller;
    
    public Script(MainFrameController controller){
        
        this.controller = controller;
    }
    
    public abstract void launch();
    
}
