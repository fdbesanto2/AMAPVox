/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.commons.util.ProcessingListener;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.als.LasVoxelisation;
import fr.amap.lidar.amapvox.voxelisation.configuration.ALSVoxCfg;
import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class ALSVoxelizationService extends Service<File> {

    //private final ALSVoxCfg cfg;
    private final File file;
    
    public ALSVoxelizationService(File file){
        this.file = file;
    }
    
    @Override
    protected Task<File> createTask() {
        
        final LasVoxelisation voxelization = new LasVoxelisation();
        
        Task task = new Task() {
                        
            @Override
            protected File call() throws Exception {
                
                ALSVoxCfg cfg = new ALSVoxCfg();
                cfg.readConfiguration(file);
                
                updateMessage("Started!");
                
                cfg.getVoxelParameters().infos.setType(VoxelSpaceInfos.Type.ALS);

                voxelization.addProcessingListener(new ProcessingListener() {

                    @Override
                    public void processingStepProgress(String progressMsg, long progress, long max) {

                        updateMessage(progressMsg);
                        updateProgress(progress, max);
                    }

                    @Override
                    public void processingFinished(float duration) {

                    }
                });

                voxelization.process(cfg);

                return cfg.getOutputFile();
            }
    
            @Override
            protected void succeeded() {
                super.succeeded();
                updateMessage("Done!");
            }

            @Override
            protected void cancelled() {
                super.cancelled();
                voxelization.setCancelled(true);
                updateMessage("Cancelled!");
            }

            @Override
            protected void failed() {
                super.failed();
                updateMessage("Failed!");
            }
        };
        
        return task;
    }
    
}
