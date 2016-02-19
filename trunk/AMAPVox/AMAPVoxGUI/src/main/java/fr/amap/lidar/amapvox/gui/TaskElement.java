/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

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
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

/**
 * Give control and information about a task execution 
 * @author Julien Heurtebize
 */
public class TaskElement extends GridPane{
    
    private final ImageView taskIcon;
    
    private final Button controlButton;
    private final ImageView cancelImage;
    private final ImageView startImage;
    private final ImageView restartImage;
    
    private final Label taskTitle;
    private final Label taskMessage;
    private final ProgressBar taskProgress;
    
    private ButtonType buttonType;
    private final Service task;
    
    public enum ButtonType{
        
        START(1),
        RESTART(2),
        CANCEL(3);
        
        private final int type;
        
        private ButtonType(int type){
            this.type = type;
        }
    }

    public TaskElement(Service task) {
        
        super();
        
        cancelImage = new ImageView(new Image(TaskElement.class.getResourceAsStream("/fxml/icons/gnome_delete_element.png")));
        startImage = new ImageView(new Image(TaskElement.class.getResourceAsStream("/fxml/icons/start_2.png")));
        restartImage = new ImageView(new Image(TaskElement.class.getResourceAsStream("/fxml/icons/restart_3.png")));
        
        cancelImage.setFitWidth(17);
        cancelImage.setFitHeight(17);
        
        startImage.setFitWidth(17);
        startImage.setFitHeight(17);
        
        restartImage.setFitWidth(17);
        restartImage.setFitHeight(17);
        
        setPrefHeight(64);
        setPrefWidth(USE_COMPUTED_SIZE);
        
        setMinHeight(USE_PREF_SIZE);
        setMinWidth(USE_COMPUTED_SIZE);
        
        taskIcon = new ImageView(new Image(TaskElement.class.getResourceAsStream("/fxml/icons/sun.png")));
        taskIcon.setFitWidth(25);
        taskIcon.setFitHeight(25);
        
        //set default button as a cancel button
        controlButton = new Button("Cancel");
        buttonType = ButtonType.CANCEL;
        controlButton.setGraphic(cancelImage);
        
        taskProgress = new ProgressBar(1);
        taskTitle = new Label("Task #");
        taskMessage = new Label("Processing");
        
        taskProgress.setPrefWidth(268);
        taskProgress.setPrefHeight(27);
        
        taskProgress.setProgress(1);
        
        VBox vbox = new VBox(taskTitle, taskProgress, taskMessage);
        
        getChildren().add(taskIcon);
        getChildren().add(vbox);
        getChildren().add(controlButton);
        
        setConstraints(taskIcon, 0, 0);
        setConstraints(vbox, 1, 0);
        setConstraints(controlButton, 2, 0);
        
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setPercentWidth(10);
        column1.setHalignment(HPos.CENTER);
        
        ColumnConstraints column2 = new ColumnConstraints();
        column2.setPercentWidth(65);
        
        ColumnConstraints column3 = new ColumnConstraints();
        column3.setPercentWidth(25);
        column3.setHalignment(HPos.CENTER);
        
        getColumnConstraints().addAll(column1, column2, column3);
        
        setStyle("-fx-border-width: 0 0 1 0;-fx-border-color:  transparent transparent linear-gradient(from 0.0% 0.0% to 100.0% 100.0%, transparent, rgba(0.0,0.0,0.0,0.2), transparent) transparent");
        
        this.task = task;
        
        taskTitle.textProperty().bind(task.titleProperty());
        taskProgress.progressProperty().bind(task.progressProperty());
        taskMessage.textProperty().bind(task.messageProperty());
        
        
        task.setOnCancelled(new EventHandler() {
            @Override
            public void handle(Event event) {
                setButtonType(ButtonType.RESTART);
                taskProgress.setDisable(true);
            }
        });
        
        task.setOnSucceeded(new EventHandler() {
            @Override
            public void handle(Event event) {
                setButtonType(ButtonType.RESTART);
            }
        });
        
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
    
}
