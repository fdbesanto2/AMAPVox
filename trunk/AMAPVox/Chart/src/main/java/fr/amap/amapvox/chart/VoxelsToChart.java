/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.chart;

import fr.amap.amapvox.voxreader.Voxel;
import fr.amap.amapvox.voxreader.VoxelFileReader;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.Iterator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

/**
 *
 * @author calcul
 */
public class VoxelsToChart {
    
    public static JFreeChart createVegetationProfileChart(File voxelFile){
           
        VoxelFileReader reader = new VoxelFileReader(voxelFile);
        float resolution = reader.getVoxelSpaceInfos().getResolution();
        int layersNumber = (int)(reader.getVoxelSpaceInfos().getSplit().z * resolution);
        
        float[] padMeanByLayer = new float[layersNumber];
        int[] valuesNumberByLayer = new int[layersNumber];
        
        Iterator<Voxel> iterator = reader.iterator();
        
        while(iterator.hasNext()){
            Voxel voxel = iterator.next();
            
            if(!Float.isNaN(voxel.PadBVTotal)){
                int layerIndex = (int)voxel.ground_distance;
                
                if(layerIndex > 0){
                    padMeanByLayer[layerIndex] += voxel.PadBVTotal;
                    valuesNumberByLayer[layerIndex]++;
                }
            }            
        }
        
        final XYSeries series1 = new XYSeries("First", false);
        
        float lai = 0;
        
        for(int i=0;i<layersNumber;i++){
            
            padMeanByLayer[i] = padMeanByLayer[i] / valuesNumberByLayer[i];
            series1.add(padMeanByLayer[i], i);
            
            if(!Float.isNaN(padMeanByLayer[i])){
                lai += padMeanByLayer[i];
            }
        }
        
        
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            "Vegetation profile",    // title
            "PAD",             // x-axis label
            "Height above ground",      // y-axis label
            dataset);

        String fontName = "Palatino";
        chart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));
        chart.addSubtitle(new TextTitle("LAI = "+lai, new Font(fontName, Font.PLAIN, 14)));

        /*XYPlot plot = (XYPlot) chart.getPlot();
        plot.setDomainPannable(true);
        plot.setRangePannable(false);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getDomainAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getRangeAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        chart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.CENTER);*/
        /*XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            //renderer.setBaseShapesVisible(false);
            //renderer.setDrawSeriesLineAsPath(true);
            // set the default stroke for all series
            renderer.setAutoPopulateSeriesStroke(false);
            /*renderer.setDefaultStroke(new BasicStroke(3.0f, 
                    BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL), false);*/
            /*renderer.setSeriesPaint(0, Color.RED);
            renderer.setSeriesPaint(1, new Color(24, 123, 58));
            renderer.setSeriesPaint(2, new Color(149, 201, 136));
            renderer.setSeriesPaint(3, new Color(1, 62, 29));
            renderer.setSeriesPaint(4, new Color(81, 176, 86));
            renderer.setSeriesPaint(5, new Color(0, 55, 122));
            renderer.setSeriesPaint(6, new Color(0, 92, 165));
        }*/

        return chart;
    }
}
