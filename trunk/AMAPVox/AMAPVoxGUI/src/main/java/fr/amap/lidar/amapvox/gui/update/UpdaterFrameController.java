package fr.amap.lidar.amapvox.gui.update;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.dropbox.core.DbxException;
import fr.amap.lidar.amapvox.gui.update.Updater;
import java.io.File;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TimeZone;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.util.Callback;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class UpdaterFrameController implements Initializable {
    @FXML
    private Button buttonRefresh;
    @FXML
    private TableView<ProgramDetail> tableView;
    @FXML
    private TableColumn<ProgramDetail, String> tableColumnName;
    @FXML
    private TableColumn<ProgramDetail, String> tableColumnDateOfUpload;
    @FXML
    private TextArea textAreaChangeLog;

    @FXML
    private void onActionButtonRefresh(ActionEvent event) {
        
        load();
    }

    @FXML
    private void onActionButtonUpdateToSelectedVersion(ActionEvent event) {
        
        ProgramDetail selectedItem = tableView.getSelectionModel().getSelectedItem();
        updateToVersion(selectedItem.getUrl());
    }
    
    private void updateToVersion(URL url){
        
        Service s = new Service() {

            @Override
            protected Task createTask() {
                
                return new Task() {

                    @Override
                    protected Object call() throws Exception {
                        
                        try {
                            updater.update(url);

                        } catch (Exception ex) {
                            logger.error(ex);
                        }
                        
                        return null;
                    }
                };
                
            }
        };
         
        ProgressDialog d = new ProgressDialog(s);
        d.show();
        
        s.start();
    }

    @FXML
    private void onActionButtonUpdateToLast(ActionEvent event) {
        
        if(tableView.getItems().size() > 0){
            URL url = tableView.getItems().get(0).getUrl();
            updateToVersion(url);
        }
    }
    
    private final static Logger logger = Logger.getLogger(UpdaterFrameController.class);
    private Updater updater;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        tableView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        tableView.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ProgramDetail>() {
            @Override
            public void changed(ObservableValue<? extends ProgramDetail> observable, ProgramDetail oldValue, ProgramDetail newValue) {
                if(newValue != null){
                    textAreaChangeLog.setText(newValue.getChangeLog());
                }
            }
        });
        
        tableColumnName.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProgramDetail, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ProgramDetail, String> param) {
                return new SimpleStringProperty(Updater.getFilenameFromURL(param.getValue().getUrl()));
            }
        });
        
        tableColumnDateOfUpload.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ProgramDetail, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<ProgramDetail, String> param) {
                
                NumberFormat numberFormat = ProgramDetail.NUMBER_FORMAT;
                Calendar date = param.getValue().getDate();
                
                return new SimpleStringProperty(numberFormat.format(date.get(Calendar.DAY_OF_MONTH)) + "/" + 
                    numberFormat.format(date.get(Calendar.MONTH)+1) + "/" + 
                    date.get(Calendar.YEAR) + " " +
                    numberFormat.format(date.get(Calendar.HOUR_OF_DAY)) + ":" +
                    numberFormat.format(date.get(Calendar.MINUTE)) + ":" +
                    numberFormat.format(date.get(Calendar.SECOND)));
            }
        });
    }  

    
    public void load(){
        
        if(updater == null){
            this.updater = new Updater();
        }
        
        Service s = new Service() {

            @Override
            protected Task createTask() {
                
                return new Task() {

                    @Override
                    protected Object call() throws Exception {
                        
                        try {
                            Map<Date, ProgramDetail> fileList = updater.getFileList();
                            List<ProgramDetail> programs = new ArrayList<>();

                            Iterator<Map.Entry<Date, ProgramDetail>> iterator = fileList.entrySet().iterator();
                            while(iterator.hasNext()){
                                Map.Entry<Date, ProgramDetail> program = iterator.next();
                                programs.add(program.getValue());
                            }

                            Collections.reverse(programs);

                            
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    tableView.getItems().clear();
                                    tableView.getItems().addAll(programs);
                                    tableView.getSelectionModel().selectFirst();
                                }
                            });

                        } catch (DbxException ex) {
                            logger.error(ex);
                        }
                        
                        return null;
                    }
                };
                
            }
        };
        
        ProgressDialog d = new ProgressDialog(s);
        d.show();
        
        s.start();
        
        
    }
    
}
