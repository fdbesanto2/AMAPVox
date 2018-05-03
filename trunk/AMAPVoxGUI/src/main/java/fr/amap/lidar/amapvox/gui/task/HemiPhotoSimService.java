/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.simulation.hemi.HemiParameters;
import fr.amap.lidar.amapvox.simulation.hemi.HemiPhotoCfg;
import fr.amap.lidar.amapvox.simulation.hemi.HemiScanView;
import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class HemiPhotoSimService extends Service<File>{

    private final File file;
    private HemiScanView hemiScanView;
    
    public HemiPhotoSimService(File file){
        this.file = file;
    }
    
    @Override
    protected Task<File> createTask() {
        return new Task<File>() {
            @Override
            protected File call() throws Exception {
                
                HemiPhotoCfg cfg = new HemiPhotoCfg(new HemiParameters());
                cfg.readConfiguration(file);
                
                hemiScanView = new HemiScanView(cfg.getParameters());
                
                try{
                    updateMessage("Simulation in progress...");
                    hemiScanView.launchSimulation();
                }catch(Exception ex){
                    throw ex;
                }
                
                if(cfg.getParameters().isGenerateBitmapFile()){
                    return cfg.getParameters().getOutputBitmapFile();
                }
                
                return null;
            }
            
            @Override
            protected void cancelled() {
                super.cancelled();
                hemiScanView.setCancelled(true);
            }
        };
    }
    
}
