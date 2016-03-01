/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import fr.amap.lidar.amapvox.voxelisation.postproc.ButterflyRemover;
import fr.amap.lidar.amapvox.voxelisation.postproc.ButterflyRemoverCfg;

/**
 *
 * @author calcul
 */
public class ButterflyRemoverService extends Service<Void>{

    private final ButterflyRemoverCfg cfg;
    
    public ButterflyRemoverService(ButterflyRemoverCfg cfg){
        
        this.cfg = cfg;
    }
    
    @Override
    protected Task<Void> createTask() {
        
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                
                ButterflyRemover.clean(cfg.getInputFile(), cfg.getOutputFile());
                
                return null;
            }
        };
    }
    
}
