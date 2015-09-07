package fr.amap.amapvox.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.QuadCurveTo;
import javafx.scene.shape.Shape;
import javafx.scene.shape.VLineTo;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class RingsMaskingSetupFrameController implements Initializable {

    private class SinglePoint{
        
        public float x;
        public float y;

        public SinglePoint(float x, float y) {
            this.x = x;
            this.y = y;
        }
    }
    
    private List<List<Shape>> shapes;
    
    private Color darkGrayColor = new Color(0.48, 0.48, 0.48, 1);
    private Color lightGrayColor = new Color(0.8, 0.8, 0.8, 1);
    
    @FXML
    private AnchorPane anchorPaneRoot;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        float y = -100;
        
        float maxY = (float) -(360*Math.sin(Math.toRadians(90 + 74.0f)));
        
        shapes = new ArrayList<>();
        
        List<Shape> ring1 = createPolylineShapesFromAngles(360, 0, 13, maxY, 0);
        List<Shape> ring2 = createPolylineShapesFromAngles(360, 16, 28, maxY, 1);
        List<Shape> ring3 = createPolylineShapesFromAngles(360, 32, 43, maxY, 2);
        List<Shape> ring4 = createPolylineShapesFromAngles(360, 47, 58, maxY, 3);
        List<Shape> ring5 = createPolylineShapesFromAngles(360, 61, 74, maxY, 4);
        
        shapes.add(ring1);
        shapes.add(ring2);
        shapes.add(ring3);
        shapes.add(ring4);
        shapes.add(ring5);
    }    
    
    private List<Shape> createPolylineShapesFromAngles(float length, float lowerAngle, float upperAngle, float y, int index){
        
        List<Shape> ring = new ArrayList<>();
        
        double[] points1 = new double[]{length*Math.cos(Math.toRadians(90 + upperAngle/1.0f)),
                                        y, 
                                        0.0, 0.0,
                                        length*Math.cos(Math.toRadians(90 + lowerAngle/1.0f)),
                                        y,
        };
        
        Polyline p1 = getConfiguredPolyline(points1, lightGrayColor, index);
        
        
        double[] points2 = new double[]{length*Math.cos(Math.toRadians(90 - lowerAngle/1.0f)),
                                        y, 
                                        0.0, 0.0,
                                        length*Math.cos(Math.toRadians(90 - upperAngle/1.0f)),
                                        y,
        };
        
        Polyline p2 = getConfiguredPolyline(points2, lightGrayColor, index);
        
        anchorPaneRoot.getChildren().add(p1);
        anchorPaneRoot.getChildren().add(p2);
        
        Path path = createPathBetweenAngles(new SinglePoint((float) points1[0], (float) points1[1]),
                                new SinglePoint((float) points1[4], (float) points1[5]),
                                new SinglePoint((float) points2[0], (float) points2[1]),
                                new SinglePoint((float) points2[4], (float) points2[5]));
        
        path.setId(String.valueOf(index));
        
        ring.add(p1);
        ring.add(p2);
        ring.add(path);
        
        return ring;
    }
    
    private Path createPathBetweenAngles(SinglePoint point1, SinglePoint point2, SinglePoint point3, SinglePoint point4){
                
        Path path = new Path();
        
        MoveTo moveTo = new MoveTo();
        moveTo.setX(point1.x);
        moveTo.setY(point1.y);
        
        ArcTo arcTo = new ArcTo();
        arcTo.setX(point4.x);
        arcTo.setY(point4.y);
        arcTo.setRadiusX(point4.x - point1.x);
        arcTo.setRadiusY((point4.x - point1.x)*0.8f);
        arcTo.setSweepFlag(true);

        HLineTo hLineTo = new HLineTo();
        hLineTo.setX(point3.x);
        
        ArcTo arcTo2 = new ArcTo();
        arcTo2.setX(point2.x);
        arcTo2.setY(point2.y);
        arcTo2.setRadiusX(point3.x - point2.x);
        arcTo2.setRadiusY((point3.x - point2.x)*0.8f);

        HLineTo hLineTo2 = new HLineTo();
        hLineTo2.setX(point1.x);

        path.getElements().add(moveTo);
        path.getElements().add(arcTo);
        path.getElements().add(hLineTo);
        path.getElements().add(arcTo2);
        path.getElements().add(hLineTo2);
        
        path.setLayoutX(anchorPaneRoot.getPrefWidth()/2.0d);
        path.setLayoutY(anchorPaneRoot.getPrefHeight()-100);
        
        path.setFill(darkGrayColor);
        
        path.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                
                Color currentColor = (Color)path.getFill();
                Color newColor;
                
                if(currentColor.isOpaque()){
                    newColor = new Color(darkGrayColor.getRed(), darkGrayColor.getGreen(),
                        darkGrayColor.getBlue(), 0.0);
                    
                }else{
                    newColor = new Color(darkGrayColor.getRed(), darkGrayColor.getGreen(),
                        darkGrayColor.getBlue(), 1.0);
                }
                
                Color lighterColor = new Color(lightGrayColor.getRed(), lightGrayColor.getGreen(),
                        lightGrayColor.getBlue(), newColor.getOpacity());
                
                int index = Integer.valueOf(path.getId());
                List<Shape> shapeList = shapes.get(index);
                
                for(Shape s : shapeList){
                    String name = s.getClass().getSimpleName();
                    
                    if(name.equals("Polyline")){
                        s.setFill(lighterColor);
                    }else if(name.equals("Path")){
                        s.setFill(newColor);
                    }
                    
                }
                
            }
        });
        
        anchorPaneRoot.getChildren().add(path);
        
        return path;
    }

    private Polyline getConfiguredPolyline(double[] points, Color fillColor, int index){
        
        final Polyline polyline = new Polyline(points);
        polyline.setFill(fillColor);
        polyline.setLayoutX(anchorPaneRoot.getPrefWidth()/2.0d);
        polyline.setLayoutY(anchorPaneRoot.getPrefHeight()-100);
        polyline.setId(String.valueOf(index));
        
        polyline.setOnMouseClicked(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {
                
                Color currentColor = (Color)polyline.getFill();
                Color newColor;
                
                if(currentColor.isOpaque()){
                    newColor = new Color(lightGrayColor.getRed(), lightGrayColor.getGreen(),
                        lightGrayColor.getBlue(), 0.0);
                    
                }else{
                    newColor = new Color(lightGrayColor.getRed(), lightGrayColor.getGreen(),
                        lightGrayColor.getBlue(), 1.0);
                }
                
                Color darkerColor = new Color(darkGrayColor.getRed(), darkGrayColor.getGreen(),
                        darkGrayColor.getBlue(), newColor.getOpacity());
                //polyline.setFill(newColor);
                
                int index = Integer.valueOf(polyline.getId());
                List<Shape> shapeList = shapes.get(index);
                
                for(Shape s : shapeList){
                    String name = s.getClass().getSimpleName();
                    
                    if(name.equals("Polyline")){
                        s.setFill(newColor);
                    }else if(name.equals("Path")){
                        s.setFill(darkerColor);
                    }
                    
                }
                
            }
        });
        
        return polyline;
        
    }
    
    
}
