/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.opengl.GLWindow;
import java.awt.MouseInfo;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 * Set a {@link GLWindow} to overlap a javafx node.
 * @author Julien Heurtebize
 */
public class FXNewtOverlap {
    
    private final static boolean DEBUG = true;
    
    public static void link (final Stage stage, final Scene scene, final Viewer3D viewer3D, final Node node) {
        
        final GLWindow gLWindow = viewer3D.getRenderFrame();
        
        gLWindow.setUndecorated(true);
        
        scene.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                show(gLWindow, node);
                gLWindow.setVisible(true);
            }
        });
        
        //initialize
        //gLWindow.setPosition((int)stage.getX(), gLWindow.getY());
        //gLWindow.setAlwaysOnTop(true);
        
        stage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                if(DEBUG){
                    System.out.println("stage x move");
                }
                
                Bounds bounds = node.getBoundsInLocal();
                Bounds localToScreen = node.localToScreen(bounds);
                gLWindow.setVisible(false);
                /*gLWindow.setSize((int)localToScreen.getWidth(), (int)localToScreen.getHeight());
                gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());*/
                
            }
        });
        
        stage.yProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                if(DEBUG){
                    System.out.println("stage y move");
                }
                
                Bounds bounds = node.getBoundsInLocal();
                Bounds localToScreen = node.localToScreen(bounds);
                gLWindow.setVisible(false);
                /*gLWindow.setSize((int)localToScreen.getWidth(), (int)localToScreen.getHeight());
                gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());*/
                
            }
        });
        
        stage.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    if(DEBUG){
                        System.out.println("stage unfocused");
                    }
                    if(newValue.booleanValue() != oldValue.booleanValue()){
                        gLWindow.setAlwaysOnTop(true);
                        gLWindow.setAlwaysOnTop(false);
                    }
                    
                    //show(gLWindow, node);
                }else{
                    if(DEBUG){
                        System.out.println("stage focused");
                    }
                    if(newValue.booleanValue() != oldValue.booleanValue()){
                        show(gLWindow, node);
                        gLWindow.setAlwaysOnTop(true);
                    }
                    
                }
            }
        });
        
        
        
        node.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                
                if(DEBUG){
                    System.out.println("node bounds in local changed");
                }
                //viewer3D.setDynamicDraw(false);
                if(newValue != oldValue){
                    show(gLWindow, node);
                    gLWindow.setVisible(true);
                }                
            }
        });
        
        
        gLWindow.addMouseListener(new MouseAdapter() {

            /*@Override
            public void mouseMoved(com.jogamp.newt.event.MouseEvent e) {
                show(gLWindow, node);
                gLWindow.setVisible(true);
            }*/
            
            @Override
            public void mouseReleased(com.jogamp.newt.event.MouseEvent e) {
                if(DEBUG){
                    System.out.println("mouse released");
                }
            }
            
            @Override
            public void mouseEntered(com.jogamp.newt.event.MouseEvent e) {
                if(DEBUG){
                    System.out.println("mouse entered");
                }
            }
        });
        
        gLWindow.addWindowListener(new WindowAdapter() {
            @Override
            public void windowResized(com.jogamp.newt.event.WindowEvent e) {
                if(DEBUG){
                    System.out.println("gl window resized");
                }
                //gLWindow.setVisible(true);
            }

            @Override
            public void windowMoved(com.jogamp.newt.event.WindowEvent e) {
                if(DEBUG){
                    System.out.println("gl window moved");
                }
                //gLWindow.setVisible(true);
            }
        });
        
        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(DEBUG){
                    System.out.println("stage width changed");
                }
                show(gLWindow, node);
            }
        });
        
        stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(DEBUG){
                    System.out.println("stage height changed");
                }
                show(gLWindow, node);
            }
        });
        
        stage.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    if(DEBUG){
                        System.out.println("stage hiding");
                    }
                    gLWindow.setVisible(false);
                }
            }
        });
        
        stage.maximizedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    if(DEBUG){
                        System.out.println("stage maximized");
                    }
                    if(newValue.booleanValue() != oldValue.booleanValue()){
                        //fix for bug https://bugs.openjdk.java.net/browse/JDK-8087997
                        stage.setIconified(false);
                        show(gLWindow, node);
                    }
                    
                    
                }else{
                    if(DEBUG){
                        System.out.println("stage minimized");
                    }
                    //gLWindow.setVisible(false);
                }
            }
        });
        
        
        stage.iconifiedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                if(newValue){
                    if(DEBUG){
                        System.out.println("stage iconified");
                    }
                    //fix for bug https://bugs.openjdk.java.net/browse/JDK-8087997
                    stage.setMaximized(false);
                    
                    gLWindow.setVisible(false);
                }else{
                    if(DEBUG){
                        System.out.println("stage maximized");
                    }
                    
                    if(newValue.booleanValue() != oldValue.booleanValue()){
                        //fix for bug https://bugs.openjdk.java.net/browse/JDK-8087997
                        stage.setMaximized(true);

                        show(gLWindow, node);
                    }
                    
                }
                
            }
        });
    }
    
    private static void show(GLWindow gLWindow, Node node){
        Bounds bounds = node.getBoundsInLocal();
        Bounds localToScreen = node.localToScreen(bounds);
        gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());
        gLWindow.setSize((int)localToScreen.getWidth(), (int)localToScreen.getHeight());
    }
}
