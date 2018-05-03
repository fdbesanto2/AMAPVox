
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.util.io.file.CSVFile;
import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class PointCloudFilterPaneComponent extends HBox{
    
    @FXML 
    private HBox hBoxRoot;
    
    @FXML 
    private TextField textfieldPointCloudPath;
    
    @FXML 
    private Label labelPointCloudErrorMarginValue;
    
    @FXML 
    private TextField textfieldPointCloudErrorMargin;
    
    @FXML 
    private Label labelPointCloudPath;
    
    @FXML 
    private Button buttonOpenPointCloudFile;
    
    @FXML 
    private ComboBox comboboxPointCloudFilteringType;
    
    @FXML 
    private Button buttonRemovePointCloudFilter;
    
    private CSVFile csvFile;
    
    public PointCloudFilterPaneComponent(){
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PointcloudFilter.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException ex) {
             throw new RuntimeException(ex);
        }
        
        comboboxPointCloudFilteringType.getItems().addAll("Keep","Discard");
        comboboxPointCloudFilteringType.getSelectionModel().selectFirst();
    }
    
    public void setCSVFile(CSVFile file){
        
        if(file != null){
            csvFile = file;
            textfieldPointCloudPath.setText(csvFile.getAbsolutePath());
        }
    }

    public CSVFile getCsvFile() {
        return csvFile;
    }

    public Label getLabelPointCloudErrorMarginValue() {
        return labelPointCloudErrorMarginValue;
    }

    public TextField getTextfieldPointCloudErrorMargin() {
        return textfieldPointCloudErrorMargin;
    }

    public Label getLabelPointCloudPath() {
        return labelPointCloudPath;
    }

    public Button getButtonOpenPointCloudFile() {
        return buttonOpenPointCloudFile;
    }

    public ComboBox getComboboxPointCloudFilteringType() {
        return comboboxPointCloudFilteringType;
    }

    public Button getButtonRemovePointCloudFilter() {
        return buttonRemovePointCloudFilter;
    }
    
    
    public void disableContent(boolean disable){
        
        setDisable(disable);
        labelPointCloudPath.setDisable(disable);
        textfieldPointCloudPath.setDisable(disable);
        buttonOpenPointCloudFile.setDisable(disable);
        buttonRemovePointCloudFilter.setDisable(disable);
        textfieldPointCloudErrorMargin.setDisable(disable);
        textfieldPointCloudPath.setDisable(disable);
        comboboxPointCloudFilteringType.setDisable(disable);
        labelPointCloudErrorMarginValue.setDisable(disable);
    }
    
    
}
