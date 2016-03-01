/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.voxelisation.ProcessTool;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class VoxFileMergingService extends Service<Void>{

    private final VoxMergingCfg cfg;
    private ProcessTool processTool;
    
    public VoxFileMergingService(VoxMergingCfg cfg){
        this.cfg = cfg;
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                
                processTool = new ProcessTool();
                processTool.mergeVoxelFiles(cfg);
                
                return null;
            }
            
            @Override
            protected void cancelled() {
                super.cancelled();
                processTool.setCancelled(true);
            }
        };
    }
    
}
