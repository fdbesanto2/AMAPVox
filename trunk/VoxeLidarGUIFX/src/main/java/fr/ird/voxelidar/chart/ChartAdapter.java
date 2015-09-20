/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.chart;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class ChartAdapter implements ChartListener{

    @Override
    public void chartCreationProgress(String progress, int ratio) {}

    @Override
    public void chartCreationFinished() {}
    
}
