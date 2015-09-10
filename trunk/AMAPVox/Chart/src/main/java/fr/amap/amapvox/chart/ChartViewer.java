/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.chart;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.imageio.ImageIO;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDPixelMap;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.util.ExportUtils;
import org.jfree.data.xy.XYDataset;
import org.jfree.fx.FXGraphics2D;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.graphics2d.svg.SVGUtils;
import org.jfree.ui.Drawable;
/**
 *
 * @author calcul
 */
public class ChartViewer extends Control{
    
    private final Stage stage;
    private ContextMenu menu;
    private List<ChartCanvas> chartCanvasList;
    //private HBox hBoxPane;
    private ScrollPane scrollPane ;
    private VBox vBoxPane;
    private int maxChartNumberInARow;
    private int currentRowIndex;
    
    static class ChartCanvas extends Canvas { 
        
        JFreeChart chart;
        
        private FXGraphics2D g2;
        
        public ChartCanvas(JFreeChart chart) {
            this.chart = chart;
            
            GraphicsContext graphicsContext2D = getGraphicsContext2D();
            this.g2 = new FXGraphics2D(graphicsContext2D);
            
            // Redraw canvas when size changes. 
            widthProperty().addListener(evt -> draw()); 
            heightProperty().addListener(evt -> draw()); 
        }  
        
        private void draw() { 
            double width = getWidth(); 
            double height = getHeight();
            getGraphicsContext2D().clearRect(0, 0, width, height);
            this.chart.draw(this.g2, new Rectangle2D.Double(0, 0, width, 
                    height));
        } 
        
        @Override 
        public boolean isResizable() { 
            return true;
        }  
        
        @Override 
        public double prefWidth(double height) { return getWidth(); }  
        
        @Override 
        public double prefHeight(double width) { return getHeight(); } 
    } 
    
    private class CanvasPositionsAndSize{
        
        public List<Rectangle2D> positionsAndSizes;
        public double totalWidth;
        public double totalHeight;

        public CanvasPositionsAndSize(List<Rectangle2D> positionsAndSizes, double totalWidth, double totalHeight) {
            this.positionsAndSizes = positionsAndSizes;
            this.totalWidth = totalWidth;
            this.totalHeight = totalHeight;
        }
        
    }
    
    public void insertChart(JFreeChart chart){
        
        final ChartCanvas chartCanvas = new ChartCanvas(chart);
        
        if((chartCanvasList.size() / (vBoxPane.getChildren().size())) >= maxChartNumberInARow){
            currentRowIndex++;
            vBoxPane.getChildren().add(new HBox());
        }
        
        ((HBox)vBoxPane.getChildren().get(currentRowIndex)).getChildren().add(chartCanvas);
        ((HBox)vBoxPane.getChildren().get(currentRowIndex)).setAlignment(Pos.CENTER_LEFT);
        
        chartCanvasList.add(chartCanvas);
        
        refreshChartPositionAndSize();

    }
    
    private void refreshChartPositionAndSize(){
        
        int index = 0;
        int chartWidth = 0;
        
        ObservableList<Node> children = vBoxPane.getChildren();
        for(Node hBoxNode : children){
            
            if(index == 0){
                chartWidth = ((HBox)hBoxNode).getChildren().size();
            }
            
            for(Node canvasNode : ((HBox)hBoxNode).getChildren()){
                
                ChartCanvas chartCanvas = (ChartCanvas) canvasNode;
                chartCanvas.setWidth(scrollPane.getWidth()/chartWidth);
                chartCanvas.setHeight(scrollPane.getHeight());
            }
            
            index++;
        }
    }
    
    private ContextMenu createContextMenu() {
        
        menu = new ContextMenu();

        Menu export = new Menu("Export As");

        MenuItem pngItem = new MenuItem("PNG...");
        pngItem.setOnAction((ActionEvent e) -> {
            handleExportToPNG();
        });
        export.getItems().add(pngItem);

        MenuItem jpegItem = new MenuItem("JPEG...");
        jpegItem.setOnAction((ActionEvent e) -> {
            handleExportToJPEG();
        });
        export.getItems().add(jpegItem);
        
        
        MenuItem pdfItem = new MenuItem("PDF...");
        pdfItem.setOnAction((ActionEvent e) -> {
            handleExportToPDF();
        });
        export.getItems().add(pdfItem);
        
        if (ExportUtils.isJFreeSVGAvailable()) {
            MenuItem svgItem = new MenuItem("SVG...");
            svgItem.setOnAction((ActionEvent e) -> {
                handleExportToSVG();
            });
            export.getItems().add(svgItem);
        }
        menu.getItems().add(export);
        return menu;
    }

    public Stage getStage() {
        return stage;
    }
    
    
    public ChartViewer(String title, int width, int height, int maxChartNumberInARow){
        
        stage = new Stage();
        stage.setTitle(title); 
        stage.setWidth(width);
        stage.setHeight(height);
        
        this.maxChartNumberInARow = maxChartNumberInARow;
        
        this.setContextMenu(createContextMenu());
        
        vBoxPane = new VBox();
        scrollPane = new ScrollPane();
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setFitToHeight(true);
        scrollPane.setFitToWidth(true);
        scrollPane.setPannable(true);
        
        scrollPane.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                MouseButton button = event.getButton();
                if(button == MouseButton.SECONDARY){
                    menu.show(stage, event.getScreenX(), event.getScreenY());
                }
            }
        });
        
        scrollPane.widthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                refreshChartPositionAndSize();
            }
        });
        
        scrollPane.heightProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                refreshChartPositionAndSize();
            }
        });
        
        vBoxPane.getChildren().add(new HBox());
        scrollPane.setContent(vBoxPane);
        
        chartCanvasList = new ArrayList<>();
        stage.setScene(new Scene(scrollPane));
        
    }
    
    public void show(){
        
        stage.show();
    }
    
    private CanvasPositionsAndSize getCanvasPositionAndSize(){
        
        List<Rectangle2D> positionsAndSizes = new ArrayList<>();
        
        double totalWidth = 0;
        double totalHeight = 0;

        double canvasHeight = 0;

        int index = 0;
        for(ChartCanvas canvas : chartCanvasList){

            if(index == 0){
                canvasHeight = canvas.getHeight();
            }
            if(index > maxChartNumberInARow-1){
                break;
            }

            totalWidth += canvas.getWidth();
            index++;
        }

        totalHeight = canvasHeight * Math.ceil(chartCanvasList.size()/(double)maxChartNumberInARow);

        index = 0;
        double posY = 0;

        ObservableList<Node> children = vBoxPane.getChildren();

        for(Node hBoxNode : children){

            for(Node canvasNode : ((HBox)hBoxNode).getChildren()){

                ChartCanvas chartCanvas = (ChartCanvas) canvasNode;

                if(index > maxChartNumberInARow-1){
                    index = 0;
                    posY += chartCanvas.getHeight();
                }

                double posX = index * chartCanvas.getWidth();

                Rectangle2D drawArea = new Rectangle2D.Double(posX, posY, chartCanvas.getWidth(), chartCanvas.getHeight());
                positionsAndSizes.add(drawArea);
                index++;
            }
        }
        
        CanvasPositionsAndSize canvasPositionsAndSize = new CanvasPositionsAndSize(positionsAndSizes, totalWidth, totalHeight);
        return canvasPositionsAndSize;
    }
    
        /**
     * A handler for the export to SVG option in the context menu.
     */
    private void handleExportToSVG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to SVG");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "Scalable Vector Graphics (SVG)", "svg"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            
            CanvasPositionsAndSize canvasPositionAndSize = getCanvasPositionAndSize();
            
            SVGGraphics2D sVGGraphics2D = new SVGGraphics2D((int)canvasPositionAndSize.totalWidth, (int)canvasPositionAndSize.totalHeight);
            
            Graphics2D graphics2D = (Graphics2D) sVGGraphics2D.create();
            
            int index = 0;
            for(ChartCanvas canvas : chartCanvasList){
                
                ((Drawable)canvas.chart).draw(graphics2D, canvasPositionAndSize.positionsAndSizes.get(index));
                index++;
            }
            
            try {
                SVGUtils.writeToSVG(file, sVGGraphics2D.getSVGElement());
            } catch (IOException ex) {
                Logger.getLogger(ChartViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    /**
     * A handler for the export to PDF option in the context menu.
     */
    private void handleExportToPDF() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "Portable Document Format (PDF)", "pdf"));
        fileChooser.setTitle("Export to PDF");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            
            
            
            try {
                
                CanvasPositionsAndSize canvasPositionAndSize = getCanvasPositionAndSize();

                PDDocument doc = new PDDocument();
            
                PDPage page = new PDPage(new PDRectangle((float) canvasPositionAndSize.totalWidth, (float) canvasPositionAndSize.totalHeight));
                doc.addPage(page);
            
                BufferedImage image = new BufferedImage((int) canvasPositionAndSize.totalWidth, (int) canvasPositionAndSize.totalHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = image.createGraphics();

                int index = 0;
                for (ChartCanvas canvas : chartCanvasList) {

                    Rectangle2D rectangle2D = canvasPositionAndSize.positionsAndSizes.get(index);

                    ((Drawable) canvas.chart).draw(g2, new Rectangle((int) rectangle2D.getX(), (int) rectangle2D.getY(),
                            (int) rectangle2D.getWidth(), (int) rectangle2D.getHeight()));
                    index++;
                }
                
                PDPageContentStream contentStream = new PDPageContentStream(doc, page, true, false);
                PDXObjectImage pdImage = new PDPixelMap(doc, image);
                contentStream.drawImage(pdImage, 0, 0);
                
                PDPageContentStream cos = new PDPageContentStream(doc, page);
                cos.drawXObject(pdImage, 0, 0, pdImage.getWidth(), pdImage.getHeight());
                cos.close();
                
                doc.save(file);
                
            } catch (IOException | COSVisitorException ex) {
                Logger.getLogger(ChartViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
            /*ExportUtils.writeAsPDF(this.chart, (int)canvas.getWidth(),
            (int)canvas.getHeight(), file);*/ 
            
            
            
            /*ExportUtils.writeAsPDF(this.chart, (int)canvas.getWidth(),
                        (int)canvas.getHeight(), file);*/
        } 
    }
    
    /**
     * A handler for the export to PNG option in the context menu.
     */
    private void handleExportToPNG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to PNG");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "Portable Network Graphics (PNG)", "png"));
        File file = fileChooser.showSaveDialog(stage);
        
        if (file != null) {
            try {

                CanvasPositionsAndSize canvasPositionAndSize = getCanvasPositionAndSize();

                BufferedImage image = new BufferedImage((int) canvasPositionAndSize.totalWidth, (int) canvasPositionAndSize.totalHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = image.createGraphics();

                int index = 0;
                for (ChartCanvas canvas : chartCanvasList) {

                    Rectangle2D rectangle2D = canvasPositionAndSize.positionsAndSizes.get(index);

                    ((Drawable) canvas.chart).draw(g2, new Rectangle((int) rectangle2D.getX(), (int) rectangle2D.getY(),
                            (int) rectangle2D.getWidth(), (int) rectangle2D.getHeight()));
                    index++;
                }

                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                    ImageIO.write(image, "png", out);
                }
                
            } catch (IOException ex) {
                // FIXME: show a dialog with the error
            }
        }  
    }

    /**
     * A handler for the export to JPEG option in the context menu.
     */
    private void handleExportToJPEG() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export to JPEG");
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter(
                "JPEG", "jpg"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                CanvasPositionsAndSize canvasPositionAndSize = getCanvasPositionAndSize();

                BufferedImage image = new BufferedImage((int) canvasPositionAndSize.totalWidth, (int) canvasPositionAndSize.totalHeight, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2 = image.createGraphics();

                int index = 0;
                for (ChartCanvas canvas : chartCanvasList) {

                    Rectangle2D rectangle2D = canvasPositionAndSize.positionsAndSizes.get(index);

                    ((Drawable) canvas.chart).draw(g2, new Rectangle((int) rectangle2D.getX(), (int) rectangle2D.getY(),
                            (int) rectangle2D.getWidth(), (int) rectangle2D.getHeight()));
                    index++;
                }

                try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file))) {
                    ImageIO.write(image, "jpg", out);
                }
                
                /*ExportUtils.writeAsJPEG(chartCanvasList.get(0).chart, (int)chartCanvasList.get(0).getWidth(),
                        (int)chartCanvasList.get(0).getHeight(), file);*/
            } catch (IOException ex) {
                // FIXME: show a dialog with the error
            }
        }        
    }
}
