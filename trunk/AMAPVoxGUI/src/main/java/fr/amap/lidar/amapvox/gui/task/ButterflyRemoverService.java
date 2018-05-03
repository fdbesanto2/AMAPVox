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
public class ButterflyRemoverService extends Service<File>{

    private final File file;
    
    public ButterflyRemoverService(File file){
        
        this.file = file;
    }
    
    @Override
    protected Task<File> createTask() {
        
        return new Task<File>() {
            @Override
            protected File call() throws Exception {
                
                final ButterflyRemoverCfg cfg = new ButterflyRemoverCfg();
                cfg.readConfiguration(file);
                
                ButterflyRemover.clean(cfg.getInputFile(), cfg.getOutputFile());
                
                return cfg.getOutputFile();
            }
        };
    }
    
}
