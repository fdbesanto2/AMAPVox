/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.chart;

import fr.amap.amapvox.voxreader.Voxel;
import fr.amap.amapvox.voxreader.VoxelFileReader;
import java.awt.Font;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.HorizontalAlignment;

/**
 *
 * @author calcul
 */
public class VoxelsToChart {
    
    private class QuadratInfo{
        
        public int splitCount;
        public int length;

        public QuadratInfo(int splitCount, int length) {
            this.splitCount = splitCount;
            this.length = length;
        }
        
    }
    private final VoxelFileChart[] voxelFiles;
    
    //quadrats
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
    
    public enum LayerReference{
        
        FROM_ABOVE_GROUND(0),
        FROM_BELOW_CANOPEE(1);
        
        private final int reference;

        private LayerReference(int reference) {
            this.reference = reference;
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
    
    public JFreeChart[] getVegetationProfileChartByQuadrats(LayerReference reference, float maxPAD){
        
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
                    charts[i] = getVegetationProfileChart(i*length, (i+1)*length, reference, maxPAD);
                    charts[i].setTitle(charts[i].getTitle().getText()+" - quadrat "+(i+1));
                }
                
                return charts;
            }
        }
        
        return null;
    }
    
    public JFreeChart[] getAttributProfileChartByQuadrats(String attribut, LayerReference reference){
        
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
                    charts[i] = getAttributProfileChart(attribut, reference, axis, i*length, (i+1)*length);
                    charts[i].setTitle(charts[i].getTitle().getText()+" - quadrat "+(i+1));
                }
                
                return charts;
            }
        }
        
        return null;
    }
    
    
    
    public JFreeChart getAttributProfileChart(String attribut, LayerReference reference){
        return getAttributProfileChart(attribut, reference, null, -1, -1);
    }
            
    public JFreeChart getVegetationProfileChart(LayerReference reference, float maxPAD){
        return getVegetationProfileChart(-1, -1, reference, maxPAD);
    }
    
    public JFreeChart createChart(String title, XYSeriesCollection dataset, String xAxisLabel, String yAxisLabel){
        
        JFreeChart chart = ChartFactory.createXYLineChart(
            title,  xAxisLabel, yAxisLabel, dataset, PlotOrientation.VERTICAL, true, true, false);

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
    
    public JFreeChart getProfileChart(XYSeriesCollection dataset, LayerReference reference, String attribut){
        
        String layerReferenceString;
        boolean inverseRangeAxis;
        
        if(reference == LayerReference.FROM_ABOVE_GROUND){
            layerReferenceString = "Height from above ground";
            inverseRangeAxis = false;
        }else{
            layerReferenceString = "Height from below canopy";
            inverseRangeAxis = true;
        }
        
        JFreeChart chart = createChart("", dataset, attribut, layerReferenceString);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRangeAxis().setInverted(inverseRangeAxis);
        
        return chart;
    }
    
    public JFreeChart getAttributProfileChart(String attribut, LayerReference reference, QuadratAxis axis, int indiceMin, int indiceMax){
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        
        for(VoxelFileChart voxelFile : voxelFiles){
            float res = voxelFile.reader.getVoxelSpaceInfos().getResolution();
            dataset.addSeries(createAttributeProfileSerie(voxelFile.reader, attribut, voxelFile.label, (int)(indiceMin/res), (int)(indiceMax/res), reference));
            
        }
        
        JFreeChart chart = getProfileChart(dataset, reference, attribut);
        chart.setTitle(attribut+" profile");
        
        return chart;
    }
    
    public JFreeChart getVegetationProfileChart(int indiceMin, int indiceMax, LayerReference reference, float maxPAD){
        
        final XYSeriesCollection dataset = new XYSeriesCollection();
        
        for (VoxelFileChart voxelFile : voxelFiles) {
            float res = voxelFile.reader.getVoxelSpaceInfos().getResolution();
            dataset.addSeries(createVegetationProfileSerie(voxelFile.reader, voxelFile.label, (int)(indiceMin/res), (int)(indiceMax/res), reference, maxPAD));
        }
        
        String title = "Vegetation profile";
        
        String layerReferenceString;
        boolean inverseRangeAxis;
        
        if(reference == LayerReference.FROM_ABOVE_GROUND){
            layerReferenceString = "Height from above ground";
            inverseRangeAxis = false;
        }else{
            layerReferenceString = "Height from below canopy";
            inverseRangeAxis = true;
        }
        
        JFreeChart chart = createChart(title, dataset,"PAD", layerReferenceString);
        XYPlot plot = (XYPlot) chart.getPlot();
        plot.getRangeAxis().setInverted(inverseRangeAxis);
        
        return chart;
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
    
    private XYSeries createAttributeProfileSerie(VoxelFileReader reader, String attributName, String key, int indiceMin, int indiceMax, LayerReference reference){
        
        float resolution = reader.getVoxelSpaceInfos().getResolution();
        int layersNumber = (int)(reader.getVoxelSpaceInfos().getSplit().z * resolution);
        
        float[] meanValueByLayer = new float[layersNumber];
        int[] valuesNumberByLayer = new int[layersNumber];
        
        //calcul de la couche sol ou canopée
        Iterator<Voxel> iterator;
        int[][] canopeeArray = null;
        if(reference == LayerReference.FROM_BELOW_CANOPEE){
            
            canopeeArray = new int[reader.getVoxelSpaceInfos().getSplit().x][reader.getVoxelSpaceInfos().getSplit().y];
            iterator = reader.iterator();
            while(iterator.hasNext()){

                Voxel voxel = iterator.next();

                if (voxel.nbSampling > 0 && voxel.nbEchos > 0) {

                    if(voxel.$k > canopeeArray[voxel.$i][voxel.$j]){
                        canopeeArray[voxel.$i][voxel.$j] = voxel.$k;
                    }
                }
            }
        }
        
        iterator = reader.iterator();
        
        while(iterator.hasNext()){
            
            Voxel voxel = iterator.next();
            
            double value;
            try {
                value = voxel.getFieldValue(Voxel.class, attributName, voxel);
            } catch (SecurityException | NoSuchFieldException | IllegalAccessException ex) {
                Logger.getLogger(VoxelsToChart.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
            
            if(!Double.isNaN(value)){
                                
                if(!doQuadratFiltering(voxel, indiceMin, indiceMax)){
                    
                    int layerIndex;
                    
                    if(reference == LayerReference.FROM_BELOW_CANOPEE){
                        layerIndex = canopeeArray[voxel.$i][voxel.$j] - voxel.$k;
                    }else{
                        layerIndex = (int)voxel.ground_distance;
                    }
                
                    if(layerIndex > 0){

                        meanValueByLayer[layerIndex] += value;
                        valuesNumberByLayer[layerIndex]++;
                    }
                }
            }        
        }
        
        final XYSeries series1 = new XYSeries(key, false);
        
        int maxHeight = layersNumber-1;
        for(int i = layersNumber-1; i>=0 ; i--){
            
            if(meanValueByLayer[i] != 0){
                maxHeight = i;
                break;
            }
        }
        
        for(int i=0;i<layersNumber;i++){
            
            meanValueByLayer[i] = meanValueByLayer[i] / valuesNumberByLayer[i];
            
            if(i <= maxHeight){
                series1.add(meanValueByLayer[i], i);
            }
        }
        
        series1.setKey(key);
        
        return series1;
    }
    
    private XYSeries createVegetationProfileSerie(VoxelFileReader reader, String key, int indiceMin, int indiceMax, LayerReference reference, float maxPAD){
        
        float resolution = reader.getVoxelSpaceInfos().getResolution();
        int layersNumber = (int)(reader.getVoxelSpaceInfos().getSplit().z * resolution);
        
        float[] padMeanByLayer = new float[layersNumber];
        int[] valuesNumberByLayer = new int[layersNumber];
        
        //calcul de la couche sol ou canopée
        Iterator<Voxel> iterator;
        int[][] canopeeArray = null;
        if(reference == LayerReference.FROM_BELOW_CANOPEE){
            
            canopeeArray = new int[reader.getVoxelSpaceInfos().getSplit().x][reader.getVoxelSpaceInfos().getSplit().y];
            iterator = reader.iterator();
            while(iterator.hasNext()){

                Voxel voxel = iterator.next();

                if (voxel.nbSampling > 0 && voxel.nbEchos > 0) {

                    if(voxel.$k > canopeeArray[voxel.$i][voxel.$j]){
                        canopeeArray[voxel.$i][voxel.$j] = voxel.$k;
                    }
                }
            }
        }
        
        iterator = reader.iterator();
        
        while(iterator.hasNext()){
            
            Voxel voxel = iterator.next();
            
            //float pad = voxel.calculatePAD(maxPAD);
            float pad = voxel.PadBVTotal;
            
            if(pad > maxPAD){
                pad = maxPAD;
            }
            
            if(!Float.isNaN(pad)){
                                
                if(!doQuadratFiltering(voxel, indiceMin, indiceMax)){
                    
                    int layerIndex;
                    
                    if(reference == LayerReference.FROM_BELOW_CANOPEE){
                        layerIndex = canopeeArray[voxel.$i][voxel.$j] - (int)((voxel.$k)/resolution);
                    }else{
                        layerIndex = (int)voxel.ground_distance;
                    }
                
                    if(layerIndex > 0){

                        padMeanByLayer[layerIndex] += pad;
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
