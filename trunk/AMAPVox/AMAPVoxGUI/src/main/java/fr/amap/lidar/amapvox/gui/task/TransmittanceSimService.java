/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.commons.util.ProcessingAdapter;
import fr.amap.commons.util.ProcessingListener;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceCfg;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceSim;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class TransmittanceSimService extends Service<TransmittanceSim>{

    private final TransmittanceCfg cfg;
    private TransmittanceSim transSim;
    
    public TransmittanceSimService(TransmittanceCfg cfg){
        this.cfg = cfg;
    }
    
    @Override
    protected Task<TransmittanceSim> createTask() {
        
        return new Task<TransmittanceSim>() {
            @Override
            protected TransmittanceSim call() throws Exception {
                
                transSim = new TransmittanceSim();
                
                updateMessage("Compute transmittance...");
                
                transSim.addProcessingListener(new ProcessingAdapter() {
                    @Override
                    public void processingStepProgress(String progressMsg, long progress, long max) {
                        updateProgress(progress, max);
                    }
                });
                
                transSim.simulationProcess(cfg);
                
                return transSim;
            }
            
            @Override
            protected void cancelled() {
                super.cancelled();
                transSim.setCancelled(true);
            }
        };
    }
    
}
