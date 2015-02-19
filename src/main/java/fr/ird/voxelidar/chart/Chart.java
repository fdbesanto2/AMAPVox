/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.chart;

import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceAdapter;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceListener;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceRawData;
import fr.ird.voxelidar.util.ProcessingListener;
import java.io.File;
import javax.swing.event.EventListenerList;
import org.jfree.data.xy.XYSeries;

/**
 *
 * @author Julien
 */
public class Chart {
    
    private final EventListenerList listeners;
    
    public Chart(){
        listeners = new EventListenerList();
    }
    
    public void addChartListener(ChartListener listener){
        listeners.add(ChartListener.class, listener);
    }
    
    public void fireChartCreationProgress(String progress, int ratio){
        
        for(ChartListener listener :listeners.getListeners(ChartListener.class)){
            
            listener.chartCreationProgress(progress, ratio);
        }
    }
    
    public void fireChartCreationFinished(){
        
        for(ChartListener listener :listeners.getListeners(ChartListener.class)){
            
            listener.chartCreationFinished();
        }
    }
    
    public ChartJFrame generateVegetationProfile(VoxelSpace voxelSpace, VoxelFilter filter){
        
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
