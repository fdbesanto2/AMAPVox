/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.chart;

import fr.amap.amapvox.voxcommons.Voxel;
import fr.amap.amapvox.voxreader.VoxelFileReader;
import java.awt.Font;
import java.awt.Paint;
import java.awt.geom.Ellipse2D;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.math3.stat.correlation.PearsonsCorrelation;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.Range;
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
    private int split = 1;
    private int length = -1;
    
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
        
        FROM_ABOVE_GROUND(0, "Height above ground"),
        FROM_BELOW_CANOPEE(1, "Height below canopy");
        
        private final int reference;
        private String label;
        
        public String getLabel(){
            return label;
        }

        private LayerReference(int reference, String label) {
            this.reference = reference;
            this.label = label;
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
    
    public void configureQuadrats(QuadratAxis axis, int split, int length){
        
        makeQuadrats = true;
        this.axis = axis;
        this.split = split;
        this.length = length;
    }
    
    private int getSplitCount(int length){
        
        if(makeQuadrats){
            
            int maxSplitCount = 0;
            int currentSplitCount = 0;
            
            for(VoxelFileChart file : voxelFiles){
            
                switch(axis){
                    case X_AXIS:
                        currentSplitCount = (int) (file.reader.getVoxelSpaceInfos().getSplit().x/length);
                        break;
                    case Y_AXIS: 
                        currentSplitCount = (int) (file.reader.getVoxelSpaceInfos().getSplit().y/length);
                        break;
                    case Z_AXIS:
                        currentSplitCount = (int) (file.reader.getVoxelSpaceInfos().getSplit().z/length);
                        break;
                }
                
                if(currentSplitCount > maxSplitCount){
                    maxSplitCount = currentSplitCount;
                }
            }
            
            return maxSplitCount;
            
        }else{
            return 0;
        }
    }
    
    private int[] getIndiceRange(VoxelFileChart voxelFile, int quadratIndex){
        
        int quadLength = 0; //longueur en voxels
        float resolution = voxelFile.reader.getVoxelSpaceInfos().getResolution();
                
        switch(axis){
            case X_AXIS:
                if( split != -1){
                    quadLength = voxelFile.reader.getVoxelSpaceInfos().getSplit().x / split;
                }else if(length != -1){
                    quadLength = (int) (length/resolution);
                }
                break;
            case Y_AXIS: 
                if(split != -1){
                    quadLength = voxelFile.reader.getVoxelSpaceInfos().getSplit().y / split;
                }else if(length != -1){
                    quadLength = (int) (length/resolution);
                }
                break;
            case Z_AXIS:
                if(split != -1){
                    quadLength = voxelFile.reader.getVoxelSpaceInfos().getSplit().z / split;
                }else if(length != -1){
                    quadLength = (int) (length/resolution);
                }
                break;
        }

        int indiceMin = (int) (quadratIndex * (quadLength-1));
        int indiceMax = (int) ((quadratIndex+1) * (quadLength-1));
        
        return new int[]{indiceMin, indiceMax};
    }
    
    private int getQuadratNumber(int split, int length){
        
        if(split == -1){
            return getSplitCount(length);
        }else{
            return split;
        }
    }
    
    public JFreeChart[] getVegetationProfileCharts(LayerReference reference, float maxPAD){
        
        boolean inverseRangeAxis;

        inverseRangeAxis = !(reference == LayerReference.FROM_ABOVE_GROUND);
            
        int quadratNumber = getQuadratNumber(split, length);
        
        JFreeChart[] charts = new JFreeChart[quadratNumber];
        
        for(int i=0;i<quadratNumber;i++){
            
            XYSeriesCollection dataset = new XYSeriesCollection();
            
            for(VoxelFileChart voxelFile : voxelFiles){
                
                int[] indices = getIndiceRange(voxelFile, i);
                
                XYSeries serie = createVegetationProfileSerie(voxelFile.reader, voxelFile.label, indices[0], indices[1], reference, maxPAD);
                dataset.addSeries(serie);
            }
            
            List<XYSeries> series = dataset.getSeries();
        
            double correlationValue = Double.NaN;

            if(series.size() == 2){

                XYSeries firstSerie = series.get(0);
                XYSeries secondSerie = series.get(1);

                Map<Double, Double[]> valuesMap = new HashMap<>();

                for(int j=0;j<firstSerie.getItemCount() ;j++){

                    Double[] value = new Double[]{firstSerie.getDataItem(j).getXValue(), Double.NaN};
                    valuesMap.put(firstSerie.getDataItem(j).getYValue(), value);
                }

                for(int j=0;j<secondSerie.getItemCount() ;j++){

                    Double[] value = valuesMap.get(Double.valueOf(secondSerie.getDataItem(j).getYValue()));
                    if(value == null){
                        valuesMap.put(secondSerie.getDataItem(j).getYValue(), new Double[]{Double.NaN, secondSerie.getDataItem(j).getXValue()});
                    }else if(Double.isNaN(value[1])){
                        value[1] = secondSerie.getDataItem(j).getXValue();
                        valuesMap.put(secondSerie.getDataItem(j).getYValue(), value);
                    }
                }


                List<Double> firstList = new ArrayList<>();
                List<Double> secondList = new ArrayList<>();

                Iterator<Map.Entry<Double, Double[]>> iterator = valuesMap.entrySet().iterator();
                while(iterator.hasNext()){
                    Map.Entry<Double, Double[]> next = iterator.next();
                    Double[] value = next.getValue();

                    if(!Double.isNaN(value[0]) && !Double.isNaN(value[1])){
                        firstList.add(value[0]);
                        secondList.add(value[1]);
                    }
                }

                double[] firstArray = new double[firstList.size()];
                double[] secondArray = new double[secondList.size()];

                for(int j=0;j<firstList.size();j++){
                    firstArray[j] = firstList.get(j);
                    secondArray[j] = secondList.get(j);
                }

                PearsonsCorrelation correlation = new PearsonsCorrelation();
                correlationValue = correlation.correlation(firstArray, secondArray);
            }
            
            charts[i] = createChart("Vegetation profile"+" - quadrat "+(i+1), dataset, "PAD", reference.getLabel());
            if(!Double.isNaN(correlationValue)){
                charts[i].addSubtitle(new TextTitle("R2 = "+(Math.round(Math.pow(correlationValue, 2)*100))/100.0));
            }
            
            ((XYPlot) charts[i].getPlot()).getRangeAxis().setInverted(inverseRangeAxis);
        }
        
        
        //set quadrats ranges
        
        double minX = 0;
        double maxX = 0;
        double minY = 0;
        double maxY = 0;
        
        int id = 0;
        for(JFreeChart chart : charts){
            
            XYPlot plot = (XYPlot) chart.getPlot();
            Range rangeOfRangeAxis = plot.getDataRange(plot.getRangeAxis());
            Range rangeOfDomainAxis = plot.getDataRange(plot.getDomainAxis());
            
            double currentMinY = rangeOfRangeAxis.getLowerBound();
            double currentMaxY = rangeOfRangeAxis.getUpperBound();
            double currentMinX = rangeOfDomainAxis.getLowerBound();
            double currentMaxX = rangeOfDomainAxis.getUpperBound();
                
            if(id == 0){
                minX = currentMinX;
                maxX = currentMaxX;
                minY = currentMinY;
                maxY = currentMaxY;
            }else{
            
                if(currentMinX < minX){minX = currentMinX;}
                if(currentMaxX > maxX){maxX = currentMaxX;}
                if(currentMinY < minY){minY = currentMinY;}
                if(currentMaxY > maxY){maxY = currentMaxY;}
            }
            
            id++;
        }
        
        for(JFreeChart chart : charts){
            
            XYPlot plot = (XYPlot) chart.getPlot();
            
            plot.getDomainAxis().setRange(minX, maxX);
            plot.getRangeAxis().setRange(minY, maxY);
        }
        
        return charts;
    }
    
    public JFreeChart[] getAttributProfileCharts(String attribut, LayerReference reference){
        
        boolean inverseRangeAxis;

        inverseRangeAxis = !(reference == LayerReference.FROM_ABOVE_GROUND);
            
        int quadratNumber = getQuadratNumber(split, length);
        
        JFreeChart[] charts = new JFreeChart[quadratNumber];
        
        for(int i=0;i<quadratNumber;i++){
            
            XYSeriesCollection dataset = new XYSeriesCollection();
            
            for(VoxelFileChart voxelFile : voxelFiles){
                
                int[] indices = getIndiceRange(voxelFile, i);
                
                XYSeries serie = createAttributeProfileSerie(voxelFile.reader, attribut, voxelFile.label, indices[0], indices[1], reference);
                dataset.addSeries(serie);
            }
                        
            charts[i] = createChart("Attribut profile"+" - quadrat "+(i+1), dataset, attribut, reference.getLabel());
            ((XYPlot) charts[i].getPlot()).getRangeAxis().setInverted(inverseRangeAxis);
            
        }
        
        return charts;
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
                Paint seriesPaint = renderer.lookupSeriesPaint(i);
                renderer.setLegendTextPaint(i, seriesPaint);
            }
        }

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
        int layersNumber = (int)(reader.getVoxelSpaceInfos().getSplit().z * resolution)*2;
        
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
        int layersNumber = (int)(reader.getVoxelSpaceInfos().getSplit().z);
        
        float[] padMeanByLayer = new float[layersNumber];
        int[] valuesNumberByLayer = new int[layersNumber];
        
        //calcul de la couche sol ou canopée
        Iterator<Voxel> iterator;
        int[][] canopeeArray = null;
        int[][] groundArray = null;
        
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
        }else if(reference == LayerReference.FROM_ABOVE_GROUND){
            groundArray = new int[reader.getVoxelSpaceInfos().getSplit().x][reader.getVoxelSpaceInfos().getSplit().y];
            
            for(int i=0;i<groundArray.length;i++){
                for(int j=0;j<groundArray[0].length;j++){
                    groundArray[i][j] = reader.getVoxelSpaceInfos().getSplit().z -1 ;
                }
            }
            
            iterator = reader.iterator();
            while(iterator.hasNext()){

                Voxel voxel = iterator.next();

                if (voxel.ground_distance > 0) {

                    if(voxel.$k < groundArray[voxel.$i][voxel.$j]){
                        groundArray[voxel.$i][voxel.$j] = voxel.$k;
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
                        //layerIndex = (int)((voxel.$k)/resolution) - groundArray[voxel.$i][voxel.$j];
                        layerIndex = (int) (voxel.ground_distance/resolution);
                    }
                
                    if(layerIndex > 0){

                        padMeanByLayer[layerIndex] += pad;
                        valuesNumberByLayer[layerIndex]++;
                    }
                }
            }        
        }
        
        final XYSeries serie = new XYSeries(key, false);
        
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
                serie.add(padMeanByLayer[i], i*resolution);
            }
            
            if(!Float.isNaN(padMeanByLayer[i]) ){
                lai += padMeanByLayer[i];
            }
        }
        
        lai *= resolution;
        
        serie.setKey(key+'\n'+"PAI = "+(Math.round(lai*10))/10.0);
        
        return serie;
    }
    
}
