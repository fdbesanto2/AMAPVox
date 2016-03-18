/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.voxelisation.ProcessTool;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class VoxFileMergingService extends Service<File>{

    private final File file;
    private ProcessTool processTool;
    
    public VoxFileMergingService(File file){
        this.file = file;
    }
    
    @Override
    protected Task<File> createTask() {
        return new Task() {
            @Override
            protected File call() throws Exception {
                
                final VoxMergingCfg cfg = new VoxMergingCfg();
                cfg.readConfiguration(file);
                    
                processTool = new ProcessTool();
                processTool.mergeVoxelFiles(cfg);
                
                return cfg.getOutputFile();
            }
            
            @Override
            protected void cancelled() {
                super.cancelled();
                processTool.setCancelled(true);
            }
        };
    }
    
}
