/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceCfg;
import fr.amap.lidar.amapvox.simulation.transmittance.lai2xxx.Lai2xxxSim;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class Lai2xxxSimService extends Service<Void>{

    private final TransmittanceCfg cfg;
    private Lai2xxxSim lai2xxxSim;
    
    public Lai2xxxSimService(TransmittanceCfg cfg){
        this.cfg = cfg;
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task() {
            @Override
            protected Object call() throws Exception {
                
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
