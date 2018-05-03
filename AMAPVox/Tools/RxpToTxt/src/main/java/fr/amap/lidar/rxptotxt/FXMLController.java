package fr.amap.lidar.rxptotxt;

import fr.amap.lidar.rxptotxt.RspExtractorFrameController;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.commons.math.matrix.Mat4D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import org.jdom2.JDOMException;
import org.controlsfx.dialog.ProgressDialog;

public class FXMLController implements Initializable {
    
    private FileChooser inputFileChooser;
    private DirectoryChooser outputDirectoryChooser;
    private Stage stage;
    
    private Stage rspExtractorFrame;
    private RspExtractorFrameController rspExtractorFrameController;
    
    @FXML
    private ListView<SimpleScan> listViewScans;
    @FXML
    private TextField textFieldOutputDirectory;
    @FXML
    private CheckBox checkboxExportReflectance;
    @FXML
    private CheckBox checkboxExportAmplitude;
    @FXML
    private CheckBox checkboxExportDeviation;
    @FXML
    private CheckBox checkboxImportPOPMatrix;
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        listViewScans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        inputFileChooser = new FileChooser();
        inputFileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Riscan project", "*.rsp"), 
                                                      new FileChooser.ExtensionFilter("Riegl scan", "*.rxp"));
        
        outputDirectoryChooser = new DirectoryChooser();
        
        rspExtractorFrame = new Stage();
        
         try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RspExtractorFrame.fxml"));
            Parent root = loader.load();
            rspExtractorFrameController = loader.getController();
            rspExtractorFrame.setScene(new Scene(root));
            rspExtractorFrameController.setStage(rspExtractorFrame);
        
        } catch (IOException ex) {
             System.err.println(ex);
        }
    }    

    @FXML
    private void onActionMenuItemSelectAllScans(ActionEvent event) {
        listViewScans.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemUnselectAllScans(ActionEvent event) {
        listViewScans.getSelectionModel().clearSelection();
    }


    @FXML
    private void onActionButtonOpenRspProject(ActionEvent event) {
        
        File selectedFile = inputFileChooser.showOpenDialog(stage);
        
        if(selectedFile != null){
            if(selectedFile.getName().endsWith(".rxp")){
                
                listViewScans.getItems().add(new SimpleScan(selectedFile));
                
            }else if(selectedFile.getName().endsWith(".rsp")){
                
                final Rsp rsp = new Rsp();
                
                try {
                    rsp.read(selectedFile);
                    
                    rspExtractorFrame.show();
                    
                    rspExtractorFrameController.init(rsp);
                    
                    rspExtractorFrame.setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {
                            
                            
                            List<RspExtractorFrameController.Scan> selectedScans = rspExtractorFrameController.getSelectedScans();
                            
                            Mat4D popMatrix;
                            if(checkboxImportPOPMatrix.isSelected()){
                                popMatrix = new Mat4D(rsp.getPopMatrix());
                                System.out.println("POP matrix imported : "+popMatrix.toString());
                            }else{
                                popMatrix = Mat4D.identity();
                                System.out.println("POP matrix disabled, set to identity.");
                            }
                            
                            for(RspExtractorFrameController.Scan scan : selectedScans){
                                listViewScans.getItems().add(new SimpleScan(scan.getFile(), popMatrix, scan.getSop()));
                            }
                        }
                    });                    
                    
                } catch (JDOMException | IOException ex) {
                    System.err.println(ex);
                }
            }else{
                System.err.println("Invalid extension");
            }
        }
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void onActionButtonRemoveScanFromListView(ActionEvent event) {
        ObservableList<SimpleScan> items = listViewScans.getItems();
        listViewScans.getItems().removeAll(items);
    }

    @FXML
    private void onActionButtonChooseOutputDirectory(ActionEvent event) {
        
        File directory = outputDirectoryChooser.showDialog(stage);
        
        if(directory != null){
            textFieldOutputDirectory.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonLaunchConversion(ActionEvent event) {
        
        final boolean exportReflectance = checkboxExportReflectance.isSelected();
        final boolean exportAmplitude = checkboxExportAmplitude.isSelected();
        final boolean exportDeviation = checkboxExportDeviation.isSelected();
        
        if(!textFieldOutputDirectory.getText().isEmpty()){
            
            final File directory = new File(textFieldOutputDirectory.getText());
        
            if(Files.exists(directory.toPath())){
                
                Service s = new Service() {

                    @Override
                    protected Task createTask() {
                        return new Task() {

                            @Override
                            protected Object call() throws Exception {
                                
                                System.out.println("Starting conversion");
                                
                                try {
                                    RxpScanConversion conversion = new RxpScanConversion();
                                    
                                    int count = 0;
                                    for(SimpleScan scan : listViewScans.getItems()){
                                        
                                        updateProgress(count, listViewScans.getItems().size());
                                        updateMessage("Convert file "+(count+1)+"/"+listViewScans.getItems().size()+" : "+scan.file.getName());
                                        
                                        conversion.toTxt(scan, directory, exportReflectance, exportAmplitude, exportDeviation);
                                        count++;
                                    }
                                } catch (IOException ex) {
                                    System.err.println(ex);
                                }catch (Exception ex) {
                                    System.err.println(ex);
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
    }
}
