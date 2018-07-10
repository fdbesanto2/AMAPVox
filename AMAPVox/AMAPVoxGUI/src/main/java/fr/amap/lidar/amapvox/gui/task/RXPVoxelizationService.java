/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpVoxelisation;
import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class RXPVoxelizationService extends Service<Void>{

    private final File file;
    
    public RXPVoxelizationService(File file){
        this.file = file;
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
        
                updateMessage("Voxelization...");
                // read configuration
                TLSVoxCfg cfg = new TLSVoxCfg();
                cfg.readConfiguration(file);
                // voxelisation
                RxpVoxelisation voxelisation = new RxpVoxelisation(cfg);
                voxelisation.init();
                voxelisation.call();
                
                return null;
            }
        };
    }
    
}
