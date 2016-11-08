/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.lidar.amapvox.voxelisation.Shot;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public abstract class ScanToShot implements Iterable<Shot>{
    
    protected Logger LOGGER = Logger.getLogger(ScanToShot.class);
    
    protected final Mat4D transfMatrix;
    
    public ScanToShot(Mat4D transfMatrix){
        this.transfMatrix = transfMatrix;
    } 
}
