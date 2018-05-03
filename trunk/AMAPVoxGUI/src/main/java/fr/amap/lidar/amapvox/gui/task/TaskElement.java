/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import java.io.File;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javax.swing.event.EventListenerList;

/**
 * Give control and information about a task execution 
 * @author Julien Heurtebize
 */
public class TaskElement extends GridPane{
    
    private final EventListenerList listeners;
    
    private final ImageView taskIcon;
    
    private final Button controlButton;
    private final ImageView cancelImage;
    private final ImageView startImage;
    private final ImageView restartImage;
    
    private final Label taskTitle;
    private final Label taskMessage;
    private final ProgressBar taskProgress;
    
    private ButtonType buttonType;
    private Service service;
    
    private final ServiceProvider itest;
    private final File linkedFile;
    
    public final static Image REMOVE_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/gnome_list_remove.png"));
    public final static Image START_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/start_2.png"));
    public final static Image RESTART_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/restart_3.png"));
    public final static Image VOXELIZATION_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/voxels.png"));
    public final static Image TRANSMITTANCE_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/sun.png"));
    public final static Image HEMI_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/hemispherical.png"));
    public final static Image CANOPEE_ANALYZER_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/lai2200.png"));
    public final static Image MISC_IMG = new Image(TaskElement.class.getResourceAsStream("/fxml/icons/configure.png"));
    
    public enum ButtonType{
        
        START(1),
        RESTART(2),
        CANCEL(3);
        
        private final int type;
        
        private ButtonType(int type){
            this.type = type;
        }
    }

    public TaskElement(/*Service service, */File linkedFile, ServiceProvider itest) {
        
        super();
        
        this.itest = itest;
        
        listeners = new EventListenerList();
        this.linkedFile = linkedFile;
        
        cancelImage = new ImageView(REMOVE_IMG);
        startImage = new ImageView(START_IMG);
        restartImage = new ImageView(RESTART_IMG);
        
        cancelImage.setFitWidth(17);
        cancelImage.setFitHeight(17);
        
        startImage.setFitWidth(17);
        startImage.setFitHeight(17);
        
        restartImage.setFitWidth(17);
        restartImage.setFitHeight(17);
        
        setPrefHeight(64);
        setPrefWidth(350);
        
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_COMPUTED_SIZE);
        
        taskIcon = new ImageView(TaskElement.TRANSMITTANCE_IMG);
        taskIcon.setFitWidth(25);
        taskIcon.setFitHeight(25);
        
        //set default button as a cancel button
        controlButton = new Button("Start");
        buttonType = ButtonType.START;
        controlButton.setGraphic(startImage);
        
        taskProgress = new ProgressBar(1);
        taskTitle = new Label(linkedFile.getAbsolutePath());
        taskMessage = new Label("");
        
        taskTitle.setTooltip(new Tooltip(linkedFile.getAbsolutePath()));
        
        taskProgress.setPrefWidth(268);
        taskProgress.setPrefHeight(27);
        
        taskProgress.setProgress(1);
        
        VBox vbox = new VBox(taskTitle, taskProgress, taskMessage);
        
        super.getChildren().add(taskIcon);
        super.getChildren().add(controlButton);
        super.getChildren().add(vbox);
        
        
        setConstraints(taskIcon, 0, 0);
        setConstraints(controlButton, 1, 0);
        setConstraints(vbox, 2, 0);
        
        
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(10);
        column1.setHalignment(HPos.CENTER);
        
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(25);
        column2.setHalignment(HPos.LEFT);
        
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(65);
        column3.setHalignment(HPos.LEFT);
        
        
        getColumnConstraints().addAll(column1, column2, column3);
        
        setStyle("-fx-border-width: 0 0 1 0;-fx-border-color:  transparent transparent linear-gradient(from 0.0% 0.0% to 100.0% 100.0%, transparent, rgba(0.0,0.0,0.0,0.2), transparent) transparent");
        
        //this.service = service;
        
        taskProgress.setProgress(0);
        /*taskMessage.textProperty().bind(service.messageProperty());
        
        service.setOnFailed(new EventHandler() {
            @Override
            public void handle(Event event) {
                
                setButtonType(ButtonType.RESTART);
                taskMessage.textProperty().unbind();
                taskProgress.progressProperty().unbind();
                taskProgress.setProgress(0);
                
                taskMessage.setTextFill(new Color(1, 0, 0, 1));
                taskMessage.setText("Error!");
                
                fireFailed(new Exception(service.getException()));
            }
        });
        
        service.setOnCancelled(new EventHandler() {
            @Override
            public void handle(Event event) {
                setButtonType(ButtonType.RESTART);
                taskMessage.textProperty().unbind();
                taskMessage.setText("Cancelled!");
                taskProgress.setDisable(true);
                
                taskProgress.progressProperty().unbind();
                taskProgress.setProgress(0);
                
                fireCancelled();
            }
        });
        
        service.setOnSucceeded(new EventHandler() {
            @Override
            public void handle(Event event) {
                setButtonType(ButtonType.RESTART);
                taskProgress.progressProperty().unbind();
                taskProgress.setProgress(100);
                taskMessage.textProperty().unbind();
                taskMessage.setText("Done!");
                fireSucceeded();
            }
        });
        
        controlButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                switch(buttonType){
                    case CANCEL:
                        setButtonType(TaskElement.ButtonType.RESTART);
                        service.cancel();
                        break;
                    case START:
                        fireStartButtonClicked();
                        setButtonType(TaskElement.ButtonType.CANCEL);
                        reset();
                        service.start();
                        break;
                    case RESTART:
                        startTask();
                        break;
                }
            }
        });*/
        
        controlButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                switch(buttonType){
                    case CANCEL:
                        setButtonType(TaskElement.ButtonType.RESTART);
                        service.cancel();
                        break;
                    case START:
                        setButtonType(TaskElement.ButtonType.CANCEL);
                        reset();
                        service.start();
                        break;
                    case RESTART:
                        startTask();
                        break;
                }
            }
        });
        
    }
    
    private void initService(){
        
        taskMessage.textProperty().bind(service.messageProperty());
        
        service.setOnFailed(new EventHandler() {
            @Override
            public void handle(Event event) {
                
                setButtonType(ButtonType.RESTART);
                taskMessage.textProperty().unbind();
                taskProgress.progressProperty().unbind();
                taskProgress.setProgress(0);
                
                taskMessage.setTextFill(new Color(1, 0, 0, 1));
                taskMessage.setText("Error!");
                
                fireFailed(new Exception(service.getException()));
                service = null;
            }
        });
        
        service.setOnCancelled(new EventHandler() {
            @Override
            public void handle(Event event) {
                setButtonType(ButtonType.RESTART);
                taskMessage.textProperty().unbind();
                taskMessage.setText("Cancelled!");
                taskProgress.setDisable(true);
                
                taskProgress.progressProperty().unbind();
                taskProgress.setProgress(0);
                
                fireCancelled();
                service = null;
            }
        });
        
        service.setOnSucceeded(new EventHandler() {
            @Override
            public void handle(Event event) {
                setButtonType(ButtonType.RESTART);
                taskProgress.progressProperty().unbind();
                taskProgress.setProgress(100);
                taskMessage.textProperty().unbind();
                taskMessage.setText("Done!");
                fireSucceeded(service);
                service = null;
            }
        });
    }
    
    public void startTask(){
        setButtonType(TaskElement.ButtonType.CANCEL);
        reset();
        service.reset();
        service.start();
    }
    
    private void reset(){
        service = itest.provide();
        initService();
        taskMessage.setTextFill(new Color(0, 0, 0, 1));
        taskMessage.textProperty().bind(service.messageProperty());
        taskProgress.progressProperty().bind(service.progressProperty());
        taskProgress.setDisable(false);
    }
    
    public void setTaskIcon(Image image){
        this.taskIcon.setImage(image);
    }

    public void setTaskTitle(String taskTitle) {
        this.taskTitle.setText(taskTitle);
    }

    public void setTaskMessage(String taskMessage) {
        this.taskMessage.setText(taskMessage);
    }
    
    public StringProperty getTaskMessageProperty(){
        return this.taskMessage.textProperty();
    }

    public ProgressBar getTaskProgress() {
        return taskProgress;
    }
    
    public void setButtonType(ButtonType type){
        
        this.buttonType = type;
        
        switch(type){
            case CANCEL:
                controlButton.setGraphic(cancelImage);
                controlButton.setText("Cancel");
                break;
            case START:
                controlButton.setGraphic(startImage);
                controlButton.setText("Start");
                break;
            case RESTART:
                controlButton.setGraphic(restartImage);
                controlButton.setText("Restart");
                break;
        }
    }
    
    public ButtonType getButtonType(){
        
        return buttonType;
    }

    public Button getControlButton() {
        return controlButton;
    }

    public File getLinkedFile() {
        return linkedFile;
    }

    public Service getService() {
        return service;
    }
    
    public void setService(Service service){
        this.service = service;
        
        initService();
    }
    
    private void fireFailed(Exception ex){
        for(TaskListener listener : listeners.getListeners(TaskListener.class)){
            listener.onFailed(ex);
        }
    }
    
    private void fireCancelled(){
        for(TaskListener listener : listeners.getListeners(TaskListener.class)){
            listener.onCancelled();
        }
    }
    
    private void fireSucceeded(Service service){
        
        for(TaskListener listener : listeners.getListeners(TaskListener.class)){
            listener.onSucceeded(service);
        }
    }
    
    public void addTaskListener(TaskListener taskListener){
        listeners.add(TaskListener.class, taskListener);
    }
    
    public void removeTaskListener(TaskListener taskListener){
        listeners.remove(TaskListener.class, taskListener);
    }
    
}
