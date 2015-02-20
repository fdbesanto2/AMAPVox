/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.chart;

import java.util.EventListener;

/**
 *
 * @author Julien
 */
public interface ChartListener extends EventListener{
    
    void chartCreationProgress(String progress, int ratio);
    void chartCreationFinished();
}
