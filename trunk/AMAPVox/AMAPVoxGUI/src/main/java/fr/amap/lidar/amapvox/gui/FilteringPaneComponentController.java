/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.util.Filter;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Julien
 */
public class FilteringPaneComponentController implements Initializable {

    private final static Logger logger = Logger.getLogger(FilteringPaneComponentController.class);
    private FilterFrameController filterFrameController;
    private Stage filterFrame;
    @FXML
    private ListView<Filter> listviewFilters;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        try {
            filterFrame = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FilterFrame.fxml"));
            Parent root = loader.load();
            filterFrameController = loader.getController();
            filterFrameController.setStage(filterFrame);
            filterFrame.setScene(new Scene(root));
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
    } 
    
    public void setFiltersNames(String... items){
        filterFrameController.setFilters(items);
    }
    
    public void setFilters(List<Filter> filters){
        listviewFilters.getItems().setAll(filters);
    }

    @FXML
    private void onActionButtonAddFilter(ActionEvent event) {
        
        filterFrame.show();

        filterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                if (filterFrameController.getFilter() != null && filterFrameController.isRequestAdd()) {
                    listviewFilters.getItems().addAll(filterFrameController.getFilter());
                }
            }
        });
    }

    @FXML
    private void onActionButtonRemoveFilter(ActionEvent event) {
        ObservableList<Filter> selectedItems = listviewFilters.getSelectionModel().getSelectedItems();
        listviewFilters.getItems().removeAll(selectedItems);
    }
    
    public List<Filter> getFilterList(){
        return listviewFilters.getItems();
    }
    
}
