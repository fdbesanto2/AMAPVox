/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.chart;

import fr.amap.amapvox.voxreader.Voxel;
import fr.amap.amapvox.voxreader.VoxelFileReader;
import java.awt.Shape;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.labels.XYToolTipGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.xy.AbstractXYDataset;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.ShapeUtilities;

/**
 *
 * @author calcul
 */
public class VoxelsToChart {
    
    private final VoxelFileChart[] voxelFiles;
    
    private boolean makeQuadrats;
    private QuadratAxis axis;
    private float min;
    private int splitCount;
    private int length;
    
    public enum QuadratAxis{
        
        X_AXIS(0),
        Y_AXIS(1),
        Z_AXIS(2);
        
        private final int axis;

        private QuadratAxis(int axis) {
            this.axis = axis;
        }
    }
    
    public VoxelsToChart(VoxelFileChart voxelFile){
        voxelFiles = new VoxelFileChart[]{voxelFile};
    }
    
    public VoxelsToChart(VoxelFileChart[] voxelFiles){
        this.voxelFiles = voxelFiles;
        
        for(VoxelFileChart voxelFileChart : this.voxelFiles){
            voxelFileChart.reader = new VoxelFileReader(voxelFileChart.file, true);
        }
    }
    
    public void configureQuadrats(QuadratAxis axis, float min, int splitCount, int length){
        
        makeQuadrats = true;
        this.axis = axis;
        this.min = min;
        this.splitCount = splitCount;
        this.length = length;
    }
    
    public JFreeChart[] getVegetationProfileChartByQuadrats(){
        
        if(voxelFiles.length > 0){
            
            VoxelFileReader reader = new VoxelFileReader(voxelFiles[0].file, true);
            
            if(makeQuadrats){
                switch(axis){
                    case X_AXIS:
                        if(length == -1 && splitCount != -1){
                            length = reader.getVoxelSpaceInfos().getSplit().x / splitCount;
                        }else if(length != -1 && splitCount == -1){
                            splitCount = reader.getVoxelSpaceInfos().getSplit().x/length;
                        }
                        break;
                    case Y_AXIS: 
                        if(length == -1 && splitCount != -1){
                            length = reader.getVoxelSpaceInfos().getSplit().y / splitCount;
                        }else if(length != -1 && splitCount == -1){
                            splitCount = reader.getVoxelSpaceInfos().getSplit().y/length;
                        }
                        break;
                    case Z_AXIS:
                        if(length == -1 && splitCount != -1){
                            length = reader.getVoxelSpaceInfos().getSplit().z / splitCount;
                        }else if(length != -1 && splitCount == -1){
                            splitCount = reader.getVoxelSpaceInfos().getSplit().z/length;
                        }
                        break;
                }
                
                JFreeChart[] charts = new JFreeChart[splitCount];
                for(int i = 0;i<splitCount;i++){
                    charts[i] = getVegetationProfileChart(i, axis, i*length, (i+1)*length);
                }
                
                return charts;
            }
        }
        
        return null;
    }
    
    public JFreeChart getVegetationProfileChart(){
        return getVegetationProfileChart(-1, null, -1, -1);
    }
    
    public JFreeChart createChart(String title, XYSeriesCollection dataset, String xAxisLabel, String yAxisLabel){
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,  xAxisLabel,yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

        String fontName = "Palatino";
        chart.getTitle().setFont(new Font(fontName, Font.BOLD, 18));
        XYPlot plot = (XYPlot) chart.getPlot();
        
        plot.setDomainPannable(true);
        plot.setRangePannable(true);
        plot.setDomainCrosshairVisible(true);
        plot.setRangeCrosshairVisible(true);
        plot.getDomainAxis().setLowerMargin(0.0);
        plot.getDomainAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getDomainAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        plot.getRangeAxis().setLabelFont(new Font(fontName, Font.BOLD, 14));
        plot.getRangeAxis().setTickLabelFont(new Font(fontName, Font.PLAIN, 12));
        chart.getLegend().setItemFont(new Font(fontName, Font.PLAIN, 14));
        chart.getLegend().setFrame(BlockBorder.NONE);
        
        LegendTitle subtitle = (LegendTitle) chart.getSubtitles().get(0);
        subtitle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        
        XYItemRenderer r = plot.getRenderer();
        if (r instanceof XYLineAndShapeRenderer) {
            XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) r;
            renderer.setBaseShapesVisible(true);

            Ellipse2D.Float shape = new Ellipse2D.Float(-2.5f, -2.5f, 5.0f, 5.0f);

            for (int i = 0; i < voxelFiles.length; i++) {
                renderer.setSeriesShape(i, shape);
            }
        }

        return chart;
    }
    
    public JFreeChart getVegetationProfileChart(int quadratIndex, QuadratAxis axis, int indiceMin, int indiceMax){
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        
        for(VoxelFileChart voxelFile : voxelFiles){
            dataset.addSeries(createVegetationProfileSerie(voxelFile.reader, voxelFile.label, indiceMin, indiceMax));
        }
        
        String title = "Vegetation profile";
        
        if(makeQuadrats){
            title += " - quadrat "+(quadratIndex+1);
        }
        
        return createChart(title, dataset,"PAD", "Height above ground");
    }
    
    private boolean doQuadratFiltering(Voxel voxel, int indiceMin, int indiceMax){
        
        if(makeQuadrats){
            
            switch(axis){
                case X_AXIS:
                    if(voxel.$i < indiceMin || voxel.$i > indiceMax){
                        return true;
                    }
                    break;
                case Y_AXIS: 
                    if(voxel.$j < indiceMin || voxel.$j > indiceMax){
                        return true;
                    }
                    break;
                case Z_AXIS:
                    if(voxel.$k < indiceMin || voxel.$k > indiceMax){
                        return true;
                    }
                    break;
            }
        }
        
        
        return false;
    }
    
    private XYSeries createVegetationProfileSerie(VoxelFileReader reader, String key, int indiceMin, int indiceMax){
        
        float resolution = reader.getVoxelSpaceInfos().getResolution();
        int layersNumber = (int)(reader.getVoxelSpaceInfos().getSplit().z * resolution);
        
        float[] padMeanByLayer = new float[layersNumber];
        int[] valuesNumberByLayer = new int[layersNumber];
        
        Iterator<Voxel> iterator = reader.iterator();
        
        while(iterator.hasNext()){
            
            Voxel voxel = iterator.next();
            
            if(!Float.isNaN(voxel.PadBVTotal)){
                                
                if(!doQuadratFiltering(voxel, indiceMin, indiceMax)){
                    
                    int layerIndex = (int)voxel.ground_distance;
                
                    if(layerIndex > 0){

                        if(voxel.PadBVTotal > 3){
                            voxel.PadBVTotal = 3;
                        }
                        padMeanByLayer[layerIndex] += voxel.PadBVTotal;
                        valuesNumberByLayer[layerIndex]++;
                    }
                }
            }        
        }
        
        final XYSeries series1 = new XYSeries(key, false);
        
        float lai = 0;
        
        int maxHeight = layersNumber-1;
        for(int i = layersNumber-1; i>=0 ; i--){
            
            if(padMeanByLayer[i] != 0){
                maxHeight = i;
                break;
            }
        }
        
        for(int i=0;i<layersNumber;i++){
            
            padMeanByLayer[i] = padMeanByLayer[i] / valuesNumberByLayer[i];
            
            if(i <= maxHeight){
                series1.add(padMeanByLayer[i], i);
            }
            
            if(!Float.isNaN(padMeanByLayer[i]) ){
                lai += padMeanByLayer[i];
            }
        }
        DecimalFormat df = new DecimalFormat("0.0000");
        
        series1.setKey(key+'\n'+"LAI = "+df.format(lai));
        
        return series1;
    }
    
}
