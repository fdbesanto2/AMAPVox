/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.commons.util.Filter;
import java.util.List;

/**
 *
 * @author calcul
 */
public class SimpleShotFilter implements ShotFilter{

    private final List<Filter> filters;

    public SimpleShotFilter(List<Filter> filters) {
        this.filters = filters;
    }
    
    @Override
    public boolean doFiltering(Shot shot) {
        
        if (filters != null) {

            for (Filter f : filters) {

                switch (f.getVariable()) {
                    case "Angle":
                        switch (f.getCondition()) {

                            case Filter.EQUAL:
                                if (shot.getAngle() != f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.GREATER_THAN:
                                if (shot.getAngle() <= f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.GREATER_THAN_OR_EQUAL:
                                if (shot.getAngle() < f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.LESS_THAN:
                                if (shot.getAngle() >= f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.LESS_THAN_OR_EQUAL:
                                if (shot.getAngle() > f.getValue()) {
                                    return false;
                                }
                                break;
                            case Filter.NOT_EQUAL:
                                if (shot.getAngle() == f.getValue()) {
                                    return false;
                                }
                                break;
                        }

                        break;
                }
            }
        }

        return true;
    }
    
}
