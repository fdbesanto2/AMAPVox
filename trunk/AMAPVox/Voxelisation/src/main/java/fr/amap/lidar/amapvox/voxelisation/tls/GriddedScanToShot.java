/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jleica.GriddedPointScan;
import fr.amap.amapvox.jleica.LPointShotExtractor;
import fr.amap.amapvox.jleica.LShot;
import fr.amap.commons.math.matrix.Mat4D;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author calcul
 */
public class GriddedScanToShot extends ScanToShot{

    private final GriddedPointScan scan;
    
    public GriddedScanToShot(Mat4D transfMatrix, GriddedPointScan scan) {
        
        super(transfMatrix);
        
        this.scan = scan;
    }

    @Override
    public Iterator<Shot> iterator() {
        
        LPointShotExtractor pTXShots;
        
        try {
            pTXShots = new LPointShotExtractor(scan);
            
            Iterator<LShot> iterator = pTXShots.iterator();

            return new Iterator<Shot>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @Override
                public Shot next() {
                    return iterator.next();
                }
            };

        } catch (Exception ex) {
            LOGGER.error(ex);
            return null;
        }
    }
    
}
