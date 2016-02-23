/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.simulation.hemi.HemiPhotoCfg;
import fr.amap.lidar.amapvox.simulation.hemi.HemiScanView;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class HemiPhotoSimService extends Service<Void>{

    private final HemiPhotoCfg cfg;
    private HemiScanView hemiScanView;
    
    public HemiPhotoSimService(HemiPhotoCfg cfg){
        this.cfg = cfg;
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                
                hemiScanView = new HemiScanView(cfg.getParameters());
                
                try{
                    hemiScanView.launchSimulation();
                }catch(Exception ex){
                    throw ex;
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
