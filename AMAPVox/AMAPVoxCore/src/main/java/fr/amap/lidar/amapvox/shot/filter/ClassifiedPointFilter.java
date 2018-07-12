/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot.filter;

import fr.amap.commons.util.filter.Filter;
import fr.amap.lidar.amapvox.voxelisation.als.AlsShot;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pverley
 */
public class ClassifiedPointFilter implements Filter<AlsShot.Echo> {
    
    private final List<Integer> classifiedPointsToDiscard;
    
    public ClassifiedPointFilter(List<Integer> classifiedPointsToDiscard) {
        
        this.classifiedPointsToDiscard = new ArrayList();
        if (null != classifiedPointsToDiscard) {
            this.classifiedPointsToDiscard.addAll(classifiedPointsToDiscard);
        }
        
         //work around for old cfg file version (from LasVoxelisation.java)
        if (!this.classifiedPointsToDiscard.contains(2)) {
            this.classifiedPointsToDiscard.add(2);
        }
    }
    
    public List<Integer> getClasses() {
        return classifiedPointsToDiscard;
    }
    
    @Override
    public void init() {
        // nothing to do
    }
    
    @Override
    public boolean accept(AlsShot.Echo echo) {
        AlsShot shot = (AlsShot) echo.shot;
        return shot.classifications != null && !classifiedPointsToDiscard.contains(shot.classifications[echo.rank]);
    }
    
}
