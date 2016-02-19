/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceCfg;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceSim;
import java.io.File;
import java.util.List;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;


/**
 *
 * @author calcul
 */
public class TransmittanceMapService extends Service<Void> {

    private final String title;
    
    public TransmittanceMapService(String title) {
        
        this.title = title;
    }

    @Override
    protected Task<Void> createTask() {
        
        Task task = new Task() {
                        
            @Override
            protected Object call() throws Exception {
                
                updateTitle(title);
                
                updateMessage("Started!");
        
                for(int i=0;i<100000000;i++){

                    if(isCancelled()){
                        break;
                    }
                    updateProgress(i, 100000000);
                    updateMessage(i+"");
                }
                
                updateProgress(100, 100);

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
