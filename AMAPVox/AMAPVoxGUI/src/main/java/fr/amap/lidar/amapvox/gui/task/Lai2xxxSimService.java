/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceCfg;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceParameters;
import fr.amap.lidar.amapvox.simulation.transmittance.lai2xxx.Lai2xxxSim;
import java.io.File;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class Lai2xxxSimService extends Service<Void>{

    private final File file;
    private Lai2xxxSim lai2xxxSim;
    
    public Lai2xxxSimService(File file){
        this.file = file;
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                
                TransmittanceCfg cfg = new TransmittanceCfg(new TransmittanceParameters());
                cfg.readConfiguration(file);
                
                lai2xxxSim = new Lai2xxxSim(cfg);
                
                try{
                    lai2xxxSim.process();
                }catch(Exception e){
                    throw e;
                }
                
                return null;
            }
            
            @Override
            protected void cancelled() {
                super.cancelled();
                lai2xxxSim.setCancelled(true);
            }
        };
    }
    
}
