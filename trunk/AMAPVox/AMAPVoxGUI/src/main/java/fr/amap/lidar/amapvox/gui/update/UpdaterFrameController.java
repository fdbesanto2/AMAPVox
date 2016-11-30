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
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
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
    private ListView<ProgramDetail> listviewProgramVersions;

    @FXML
    private void onActionButtonRefresh(ActionEvent event) {
        
        load();
    }

    @FXML
    private void onActionButtonUpdateToSelectedVersion(ActionEvent event) {
        
        
        ProgramDetail selectedItem = listviewProgramVersions.getSelectionModel().getSelectedItem();
        updateToVersion(selectedItem.file);
        
    }
    
    private void updateToVersion(File file){
        
        Service s = new Service() {

            @Override
            protected Task createTask() {
                
                return new Task() {

                    @Override
                    protected Object call() throws Exception {
                        
                        try {
                            updater.update(file);

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
        
        try {
            Map<Date, File> fileList = updater.getFileList();
            File lastFile = null;

            for(Entry entry: fileList.entrySet()){
                lastFile = (File) entry.getValue();
            }
            
            updateToVersion(lastFile);
            
        } catch (DbxException ex) {
            logger.error(ex);
        }
    }
    
    private class ProgramDetail{
        
        private final Calendar date;
        private final File file;
        private final NumberFormat numberFormat;

        public ProgramDetail(Date date, File file) {
            this.date = Calendar.getInstance();
            this.date.setTime(date);
            this.file = file;
            
            numberFormat = NumberFormat.getInstance();
            numberFormat.setMinimumIntegerDigits(2);
        }     

        public Calendar getDate() {
            return date;
        }

        public File getFile() {
            return file;
        }
        @Override
        public String toString(){
            
            return file.getName()+ "\t\t" +
                    numberFormat.format(date.get(Calendar.DAY_OF_MONTH)) + "/" + 
                    numberFormat.format(date.get(Calendar.MONTH)+1) + "/" + 
                    date.get(Calendar.YEAR) + " " +
                    numberFormat.format(date.get(Calendar.HOUR_OF_DAY)) + ":" +
                    numberFormat.format(date.get(Calendar.MINUTE)) + ":" +
                    numberFormat.format(date.get(Calendar.SECOND));
        }
    }
    
    private final static Logger logger = Logger.getLogger(UpdaterFrameController.class);
    private Updater updater;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        listviewProgramVersions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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
                            updater.connect();
                            Map<Date, File> fileList = updater.getFileList();
                            List<ProgramDetail> programs = new ArrayList<>();

                            Iterator<Map.Entry<Date, File>> iterator = fileList.entrySet().iterator();
                            while(iterator.hasNext()){
                                Map.Entry<Date, File> program = iterator.next();
                                programs.add(new ProgramDetail(program.getKey(), program.getValue()));
                            }

                            Collections.reverse(programs);

                            
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    listviewProgramVersions.getItems().clear();
                                    listviewProgramVersions.getItems().addAll(programs);
                                    listviewProgramVersions.getSelectionModel().selectFirst();
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
