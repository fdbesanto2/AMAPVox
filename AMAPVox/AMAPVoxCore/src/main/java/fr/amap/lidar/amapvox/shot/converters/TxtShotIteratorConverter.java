/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot.converters;

import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.format.shot.Echo;
import fr.amap.commons.util.IteratorWithException;
import java.util.Iterator;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class TxtShotIteratorConverter implements Iterable<Shot>{

    private final IteratorWithException<fr.amap.lidar.format.shot.Shot> inputIterator;
    private int index;
    
    public TxtShotIteratorConverter(IteratorWithException<fr.amap.lidar.format.shot.Shot> inputIterator) {
        this.inputIterator = inputIterator;
        this.index = 0;
    }

    @Override
    public Iterator<Shot> iterator(){
        
        return new Iterator<Shot>() {
            @Override
            public boolean hasNext() {
                try {
                    return inputIterator.hasNext();
                } catch (Exception ex) {
                    
                    return false;
                }
            }

            @Override
            public Shot next() {
                fr.amap.lidar.format.shot.Shot inputShot;
                try {
                    inputShot = inputIterator.next();
                } catch (Exception ex) {
                    return null;
                }
                if(inputShot != null){
                    
                    double[] ranges = null;
                    Echo[] echoes = inputShot.getEchoes();
                    if(echoes != null){
                        ranges = new double[echoes.length];
                        for (int i = 0; i < echoes.length; i++) {
                            Echo echo = echoes[i];
                            ranges[i] = echo.getRange();
                        }
                    }
                    
                    return new Shot(index++, new Point3d(inputShot.getXOrigin(), inputShot.getYOrigin(), inputShot.getZOrigin()),
                            new Vector3d(inputShot.getXDirection(), inputShot.getYDirection(), inputShot.getZDirection()), ranges);
                }else{
                    return null;
                }
                
            }
        };
    }
}
