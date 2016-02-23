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
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class ALSVoxelizationService extends Service<Void> {

    private final ALSVoxCfg cfg;
    private LasVoxelisation voxelization;
    
    public ALSVoxelizationService(ALSVoxCfg cfg){
        this.cfg = cfg;
    }
    
    @Override
    protected Task<Void> createTask() {
        
        Task task = new Task() {
                        
            @Override
            protected Object call() throws Exception {
                
                updateMessage("Started!");
                
                cfg.getVoxelParameters().infos.setType(VoxelSpaceInfos.Type.ALS);

                voxelization = new LasVoxelisation();
                voxelization.setProgressionStep(20);

                final Task t = this;

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

                return null;
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
