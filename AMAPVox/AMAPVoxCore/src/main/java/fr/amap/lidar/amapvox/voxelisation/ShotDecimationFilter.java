/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

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
    private final int frequency;

    public ShotDecimationFilter(int offset, int frequency) {
        this.offset = offset;
        this.frequency = frequency;
    }
    
    public ShotDecimationFilter(int frequency) {
        this(0, frequency);
    }

    @Override
    public boolean accept(Shot shot) {
        //System.out.println(shot.index + " " + ((shot.index - offset) % frequency) + " " + ((shot.index - offset) % frequency == 0));
        return (shot.index - offset) % frequency == 0;
    }
}
