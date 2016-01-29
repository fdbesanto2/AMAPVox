package fr.amap.lidar.amapvox.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.amap.commons.util.Filter;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FilterFrameController implements Initializable {
    
    private Stage stage;
    private boolean requestAdd;
    
    @FXML
    private ComboBox<String> comboboxVariable;
    @FXML
    private ComboBox<String> comboboxInequality;
    @FXML
    private TextField textfieldValue;
    @FXML
    private Button buttonAdd;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        comboboxInequality.getItems().addAll("!=", "==", "<", "<=", ">", ">=");
        
        comboboxInequality.getSelectionModel().selectFirst();
        comboboxVariable.getSelectionModel().selectFirst();
    }    
    
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    public void setFilters(String... items){
        comboboxVariable.getItems().setAll(items);
    }
    
    public Filter getFilter(){
        
        if(textfieldValue.getText().isEmpty() || comboboxVariable.getSelectionModel().getSelectedIndex() < 0){
            return null;
        }
            
        Filter filter = new Filter(comboboxVariable.getSelectionModel().getSelectedItem(),
            Float.valueOf(textfieldValue.getText()), comboboxInequality.getSelectionModel().getSelectedIndex());
        
        
        return filter;
    }

    @FXML
    private void onActionButtonAdd(ActionEvent event) {
        requestAdd = true;
        stage.close();
        requestAdd = false;
    }

    public boolean isRequestAdd() {
        return requestAdd;
    }
}
