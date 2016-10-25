/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer;

import com.jogamp.newt.event.MouseAdapter;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.opengl.GLWindow;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Bounds;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;

/**
 * Set a {@link GLWindow} to overlap a javafx node.
 * @author Julien Heurtebize
 */
public class FXNewtOverlap {
    
    private final static boolean DEBUG = false;
    
    private static TabPane findTabPaneForNode(Node node) {
        TabPane tabPane = null ;

        for (Node n = node.getParent(); n != null && tabPane == null; n = n.getParent()) {
            if (n instanceof TabPane) {
                tabPane = (TabPane) n;
            }
        }

        return tabPane ;
    }
    
    public static void link (final Stage stage, final Scene scene, final Viewer3D viewer3D, final Node node) {
        
        final GLWindow gLWindow = viewer3D.getRenderFrame();
        
        
        /*stage.initStyle(StageStyle.UTILITY);
        stage.initOwner(null);
        stage.initModality(Modality.NONE);*/
        gLWindow.setUndecorated(true);
        
        /*scene.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                show(gLWindow, node);
                
                if(!gLWindow.isVisible()){
                    gLWindow.setVisible(true);
                }
            }
        });*/
        
        //initialize
        //gLWindow.setPosition((int)stage.getX(), gLWindow.getY());
        //gLWindow.setAlwaysOnTop(true);
        
        stage.xProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                if(DEBUG){
                    System.out.println("stage x move");
                }
                
                //Bounds bounds = node.getBoundsInLocal();
                //Bounds localToScreen = node.localToScreen(bounds);
                
                if(gLWindow.isVisible()){
                    gLWindow.setVisible(false);
                }
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
                
                //Bounds bounds = node.getBoundsInLocal();
                //Bounds localToScreen = node.localToScreen(bounds);
                
                if(gLWindow.isVisible()){
                    gLWindow.setVisible(false);
                }
                
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
        
        /*node.layoutBoundsProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(DEBUG){
                    System.out.println("node bounds in local changed");
                }
                //viewer3D.setDynamicDraw(false);
                show(gLWindow, node);
                gLWindow.setVisible(true);
            }
        });*/
        
        
        node.boundsInLocalProperty().addListener(new ChangeListener<Bounds>() {
            
            final Timer timer = new Timer(); // uses a timer to call your resize method
            TimerTask task = null; // task to execute after defined delay
            final long delayTime = 200; // delay that has to pass in order to consider an operation done
        
            @Override
            public void changed(ObservableValue<? extends Bounds> observable, Bounds oldValue, Bounds newValue) {
                
                
                if (task != null) { // there was already a task scheduled from the previous operation ...
                    task.cancel(); // cancel it, we have a new size to consider
                }

                task = new TimerTask(){ // create new task that calls your resize operation
                    @Override
                    public void run() {
                        if(DEBUG){
                            System.out.println("node bounds in local changed");
                        }
                        
                        show(gLWindow, node);
                
                        if(!gLWindow.isVisible()){
                            gLWindow.setVisible(true);
                        }
                    }
                };
                // schedule new task
                timer.schedule(task, delayTime);
                //viewer3D.setDynamicDraw(false);
                
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
            
            /*@Override
            public void windowGainedFocus(com.jogamp.newt.event.WindowEvent e) {
                if(!stage.isMaximized()){
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            //stage.show();
                        }
                    });
                    
                }
            }*/
            
            @Override
            public void windowDestroyed(com.jogamp.newt.event.WindowEvent e) {
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        stage.close();
                    }
                });
            }
        });
        
        stage.widthProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(DEBUG){
                    System.out.println("stage width changed");
                }
                show(gLWindow, node);
            }
        });
        
        /*stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(DEBUG){
                    System.out.println("stage width changed");
                }
                show(gLWindow, node);
            }
        });*/
        
        stage.heightProperty().addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                if(DEBUG){
                    System.out.println("stage height changed");
                }
                show(gLWindow, node);
            }
        });
        
        /*stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(DEBUG){
                    System.out.println("stage height changed");
                }
                show(gLWindow, node);
            }
        });*/
        
        stage.showingProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    if(DEBUG){
                        System.out.println("stage hiding");
                    }
                    
                    if(gLWindow.isVisible()){
                        gLWindow.setVisible(false);
                    }
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
                        if(stage.isIconified()){
                            stage.setIconified(false);
                        }
                        
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
                    if(stage.isMaximized()){
                        stage.setMaximized(false);
                    }
                    
                    
                    if(gLWindow.isVisible()){
                        viewer3D.show();
                        gLWindow.setVisible(false);
                    }
                }else{
                    if(DEBUG){
                        System.out.println("stage maximized");
                    }
                    
                    if(newValue.booleanValue() != oldValue.booleanValue()){
                        //fix for bug https://bugs.openjdk.java.net/browse/JDK-8087997
                        if(!stage.isMaximized()){
                            stage.setMaximized(true);
                        }

                        show(gLWindow, node);
                    }
                    
                }
                
            }
        });
    }
    
    private static void show(GLWindow gLWindow, Node node){
            
        final Bounds bounds = node.getBoundsInLocal();
        final Bounds localToScreen = node.localToScreen(bounds);
        
        final WindowAdapter adapter = new WindowAdapter() {
            
            @Override
            public void windowResized(com.jogamp.newt.event.WindowEvent e) {
                gLWindow.setPosition((int)localToScreen.getMinX(), (int)localToScreen.getMinY());
                gLWindow.removeWindowListener(this);
            }
        };
        
        gLWindow.addWindowListener(adapter);
        
        gLWindow.setSize((int)localToScreen.getWidth(), (int)localToScreen.getHeight());
    }
}
