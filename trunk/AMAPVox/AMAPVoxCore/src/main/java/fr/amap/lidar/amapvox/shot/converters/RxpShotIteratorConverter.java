/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot.converters;

import fr.amap.lidar.amapvox.shot.Shot;
import java.util.Iterator;

/**
 *
 * @author Julien Heurtebize
 */
public class RxpShotIteratorConverter implements Iterable<Shot>{

    private final Iterator<fr.amap.amapvox.io.tls.rxp.Shot> inputIterator;
    
    public RxpShotIteratorConverter(Iterator<fr.amap.amapvox.io.tls.rxp.Shot> inputIterator) {
        this.inputIterator = inputIterator;
    }

    @Override
    public Iterator<Shot> iterator() {
        
        return new Iterator<Shot>() {
            @Override
            public boolean hasNext() {
                return inputIterator.hasNext();
            }

            @Override
            public Shot next() {
                fr.amap.amapvox.io.tls.rxp.Shot inputShot = inputIterator.next();
                if(inputShot != null){
                    return new Shot(inputShot.origin, inputShot.direction, inputShot.ranges);
                }else{
                    return null;
                }
                
            }
        };
    }
}
