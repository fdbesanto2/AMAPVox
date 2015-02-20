/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.chart;

import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceAdapter;
import fr.ird.voxelidar.util.ProcessingListener;
import javax.swing.event.EventListenerList;
import org.jfree.data.xy.XYSeries;

/**
 * This class can be used to generate chart contained in a jframe
 * @author Julien
 */
public class Chart {
    
    private final EventListenerList listeners;
    
    private VoxelFilter filter;
    
    /**
     * 
     */
    public Chart(){
        listeners = new EventListenerList();
        filter = new VoxelFilter();
    }
    
    /**
     *
     * @param listener
     */
    public void addChartListener(ChartListener listener){
        listeners.add(ChartListener.class, listener);
    }
    
    private void fireChartCreationProgress(String progress, int ratio){
        
        for(ChartListener listener :listeners.getListeners(ChartListener.class)){
            
            listener.chartCreationProgress(progress, ratio);
        }
    }
    
    private void fireChartCreationFinished(){
        
        for(ChartListener listener :listeners.getListeners(ChartListener.class)){
            
            listener.chartCreationFinished();
        }
    }
    
    /**
     * set a filter on the generated chart
     * @param filter
     */
    public void setFilter(VoxelFilter filter){
        if(filter != null){
            this.filter = filter;
        }else{
            this.filter = new VoxelFilter();
        }
    }
    
    /**
     * Generate a vegetation profile from a voxel space
     * @param voxelSpace
     * @return
     */
    public ChartJFrame generateVegetationProfile(VoxelSpace voxelSpace){
        
        final ChartJFrame chartJFrame;
        
        readVoxelSpace(voxelSpace);
        
        fireChartCreationProgress("Computing data", 0);
        ChartFactory chartFactory = new ChartFactory();
        
        chartFactory.addProcessingListener(new ProcessingListener() {

            @Override
            public void processingStepProgress(String progress, int ratio) {
                fireChartCreationProgress("Computing data", ratio);
            }

            @Override
            public void processingFinished() {
                
            }
        });
        
        XYSeries data = chartFactory.generateVegetationProfile(filter, voxelSpace);
        chartJFrame = new ChartJFrame(data, "Vegetation profile","PAD", "height");
        
        fireChartCreationFinished();
        
        return chartJFrame;
        
    }
    
    private void readVoxelSpace(VoxelSpace voxelSpace){
        
        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

            @Override
            public void voxelSpaceCreationProgress(int progress) {
                fireChartCreationProgress("Reading voxel space file", progress);
            }
        });
        
        voxelSpace.load();
    }
    
    /**
     * Generate a two variables calculus
     * @param voxelSpace
     * @param filter filter the generated chart
     * @param title chart title
     * @param horizontalAxis horizontal variable, variable should exist
     * @param verticalAxis vertical variable, variable sould exist
     * @return
     */
    public ChartJFrame generateXYChart(VoxelSpace voxelSpace, VoxelFilter filter, String title, String horizontalAxis, String verticalAxis){
        
        final ChartJFrame chartJFrame;
        
        readVoxelSpace(voxelSpace);
        
        fireChartCreationProgress("Computing data", 0);
        ChartFactory chartFactory = new ChartFactory();
        
        chartFactory.addProcessingListener(new ProcessingListener() {

            @Override
            public void processingStepProgress(String progress, int ratio) {
                fireChartCreationProgress("Computing data", ratio);
            }

            @Override
            public void processingFinished() {
                
            }
        });
        
        
        XYSeries data = chartFactory.generateChartWithFilters(voxelSpace, horizontalAxis, verticalAxis, filter);
        
        chartJFrame = new ChartJFrame(data, title, horizontalAxis, verticalAxis);
        
        fireChartCreationFinished();
        
        return chartJFrame;
    }
}
