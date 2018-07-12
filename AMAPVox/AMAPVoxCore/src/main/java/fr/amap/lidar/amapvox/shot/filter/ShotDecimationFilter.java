/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot.filter;

import fr.amap.commons.util.filter.Filter;
import fr.amap.lidar.amapvox.shot.Shot;

/**
 * This filter decimates a fraction of the shots given a shotID frequency and 
 * an offset.
 * For instance accepts shots 0 10 20 30 , etc. or 3 13 23 33, etc.
 * @author Philippe Verley
 */
public class ShotDecimationFilter implements Filter<Shot> {

    private final int offset;
    private final int decimationFactor;

    public ShotDecimationFilter(int decimationFactor, int offset) {
        this.offset = offset;
        this.decimationFactor = decimationFactor;
    }
    
    public ShotDecimationFilter(int decimationFactor) {
        this(0, decimationFactor);
    }
    
    @Override
    public void init() {
        // nothing to do
    }

    @Override
    public boolean accept(Shot shot) {
        int indexRel = (shot.index - offset);
        return (indexRel >= 0) && (indexRel % decimationFactor == 0);
    }
    
    public int getDecimationFactor() {
        return decimationFactor;
    }
    
    public int getOffset() {
        return offset;
    }
}
