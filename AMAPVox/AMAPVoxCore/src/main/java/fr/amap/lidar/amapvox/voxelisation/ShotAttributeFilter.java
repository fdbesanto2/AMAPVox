/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.commons.util.filter.Filter;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.commons.util.filter.FloatFilter;

/**
 *
 * @author calcul
 */
public class ShotAttributeFilter implements Filter<Shot> {

    private final FloatFilter filter;

    public ShotAttributeFilter(FloatFilter filter) {
        this.filter = filter;
    }
    
    public FloatFilter getFilter() {
        return filter;
    }

    @Override
    public boolean accept(Shot shot) {

        switch (filter.getVariable()) {
            case "Angle":
                return filter.accept((float) shot.getAngle());
            default:
                // by default accept the shot
                return true;
        }
    }

}
