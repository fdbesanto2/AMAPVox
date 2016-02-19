/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.lidar.amapvox.gui.task.TransmittanceMapService;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.VBox;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class ProgressFrameController implements Initializable {

    private ExecutorService executorService;
    
    @FXML
    private VBox vboxTasks;
    
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        executorService = Executors.newSingleThreadExecutor();
    }
    
    private TaskElement createTaskElement(Service service){
        
        TaskElement element = new TaskElement(service);
        element.setButtonType(TaskElement.ButtonType.CANCEL);
        
        return element;
    }
    
    public void addTask(Service service){
             
        final TaskElement taskElement = createTaskElement(service);
        
        service.setExecutor(executorService);
        
        service.start();
        
        taskElement.getControlButton().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                switch(taskElement.getButtonType()){
                    case CANCEL:
                        taskElement.setButtonType(TaskElement.ButtonType.RESTART);
                        service.cancel();
                        break;
                    case START:
                        taskElement.setButtonType(TaskElement.ButtonType.CANCEL);
                        service.start();
                        break;
                    case RESTART:
                        taskElement.setButtonType(TaskElement.ButtonType.CANCEL);
                        service.reset();
                        service.start();
                        break;
                }
            }
        });
        
        
        
        vboxTasks.getChildren().add(taskElement);
        
        
    }
}
