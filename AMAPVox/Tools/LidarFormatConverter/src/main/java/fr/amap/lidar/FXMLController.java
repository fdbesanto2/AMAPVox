package fr.amap.lidar;

import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.commons.util.io.file.FileManager;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
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

    RiscanProjectExtractor riscanProjectExtractor = new RiscanProjectExtractor();
    PTXProjectExtractor ptxProjectExtractor = new PTXProjectExtractor();
    PTGProjectExtractor ptgProjectExtractor = new PTGProjectExtractor();

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
    @FXML
    private CheckBox checkboxExportIntensity;
    @FXML
    private CheckBox checkboxExportRGB;
    @FXML
    private ComboBox<String> outputFormat;
    @FXML
    private CheckBox checkboxExportTime;
    @FXML
    private CheckBox checkboxExportXYZ;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        outputFormat.getItems().setAll("txt", "las", "laz", "shots+echoes","shots+echoes+shotTimeStamp");

        outputFormat.getSelectionModel().selectFirst();

        listViewScans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        inputFileChooser = new FileChooser();
        inputFileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All files", "*"),
                new FileChooser.ExtensionFilter("Riegl Riscan project", "*.rsp"),
                new FileChooser.ExtensionFilter("Riegl scan", "*.rxp"),
                new FileChooser.ExtensionFilter("Leica/Faro scan", "*.ptx", "*.ptg"));

        outputDirectoryChooser = new DirectoryChooser();

        rspExtractorFrame = new Stage();

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

        if (selectedFile != null) {

            String extension = FileManager.getExtension(selectedFile);

            switch (extension) {

                case ".rxp":
                    listViewScans.getItems().add(new SimpleScan(selectedFile));
                    break;
                case ".rsp":
                    final Rsp rsp = new Rsp();

                    try {
                        
                        rsp.read(selectedFile);

                        riscanProjectExtractor.init(rsp);

                        riscanProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                            @Override
                            public void handle(WindowEvent event) {

                                List<LidarScan> selectedScans = riscanProjectExtractor.getController().getSelectedScans();

                                Mat4D popMatrix;
                                if (checkboxImportPOPMatrix.isSelected()) {
                                    popMatrix = MatrixUtility.convertMatrix4dToMat4D(rsp.getPopMatrix());
                                    System.out.println("POP matrix imported : " + popMatrix.toString());
                                } else {
                                    popMatrix = Mat4D.identity();
                                    System.out.println("POP matrix disabled, set to identity.");
                                }

                                for (LidarScan scan : selectedScans) {
                                    listViewScans.getItems().add(new SimpleScan(scan.file, popMatrix, MatrixUtility.convertMatrix4dToMat4D(scan.matrix)));
                                }

                            }
                        });

                        riscanProjectExtractor.getFrame().showAndWait();

                    } catch (JDOMException | IOException ex) {
                        System.err.println(ex);
                    }
                    break;

                case ".ptg":
                case ".PTG":
                    try {
                        
                        ptgProjectExtractor.init(selectedFile);
                        ptgProjectExtractor.getFrame().show();

                        ptgProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                            @Override
                            public void handle(WindowEvent event) {

                                final List<LidarScan> selectedScans = ptgProjectExtractor.getController().getSelectedScans();
                                
                                for (LidarScan scan : selectedScans) {
                                    listViewScans.getItems().add(new SimpleScan(scan.file, Mat4D.identity(), MatrixUtility.convertMatrix4dToMat4D(scan.matrix)));
                                }
                            }
                        });  

                    } catch (IOException ex) {
                        System.err.println(ex);
                    } catch (Exception ex) {
                        System.err.println(ex);
                    }
                    break;

                case ".ptx":
                case ".PTX":
                    try {

                        ptxProjectExtractor.init(selectedFile);
                        ptxProjectExtractor.getFrame().show();

                        ptxProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                            @Override
                            public void handle(WindowEvent event) {

                                final List<LidarScan> selectedScans = ptxProjectExtractor.getController().getSelectedScans();
                                
                                for (LidarScan scan : selectedScans) {
                                    listViewScans.getItems().add(new SimpleScan(scan.file, Mat4D.identity(), MatrixUtility.convertMatrix4dToMat4D(scan.matrix)));
                                }
                            }
                        });                        

                    } catch (IOException ex) {
                        System.err.println(ex);
                    } catch (Exception ex) {
                        System.err.println(ex);
                    }
                    break;

                default:
                    System.err.println("Invalid extension");
            }
        }
    }

    public void setStage(Stage stage) {
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

        if (directory != null) {
            textFieldOutputDirectory.setText(directory.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonLaunchConversion(ActionEvent event) {

        final String outputFormatStr = outputFormat.getSelectionModel().getSelectedItem();
        final boolean exportReflectance = checkboxExportReflectance.isSelected();
        final boolean exportAmplitude = checkboxExportAmplitude.isSelected();
        final boolean exportDeviation = checkboxExportDeviation.isSelected();
        final boolean exportTime = checkboxExportTime.isSelected();
        final boolean exportIntensity = checkboxExportIntensity.isSelected();
        final boolean exportRGB = checkboxExportRGB.isSelected();
        final boolean exportXYZ = checkboxExportXYZ.isSelected();

        if (!textFieldOutputDirectory.getText().isEmpty()) {

            final File directory = new File(textFieldOutputDirectory.getText());

            if (Files.exists(directory.toPath())) {

                Service s = new Service() {

                    @Override
                    protected Task createTask() {
                        return new Task() {

                            @Override
                            protected Object call() throws Exception {

                                RxpScanConversion rxpConverter = new RxpScanConversion();
                                PtgScanConversion ptgConverter = new PtgScanConversion();
                                PtxScanConversion ptxConverter = new PtxScanConversion();
                                
                                System.out.println("Starting conversion");

                                int count = 0;
                                for (SimpleScan scan : listViewScans.getItems()) {

                                    updateProgress(count, listViewScans.getItems().size());
                                    updateMessage("Convert file " + (count + 1) + "/" + listViewScans.getItems().size() + " : " + scan.file.getName());
                                    String extension = FileManager.getExtension(scan.file);
                                    
                                    
                                    switch(outputFormatStr){
                                        case "txt":
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toTxt(scan, directory, exportReflectance, exportAmplitude, exportDeviation, exportTime);
                                                    break;
                                                case ".PTX":
                                                case ".ptx":
                                                    ptxConverter.toTxt(scan, directory, exportRGB, exportIntensity);
                                                    break;
                                                case ".PTG":
                                                case ".ptg":
                                                    ptgConverter.toTxt(scan, directory, exportRGB, exportIntensity);
                                                    break;
                                                default:
                                                    break;
                                            }
                                            
                                            break;
                                        case "las":
                                            
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toLaz(scan, directory, false, exportIntensity);
                                                    break;
                                                case ".PTX":
                                                case ".ptx":
                                                    //ptxConverter.toLaz(scan, directory, false, exportIntensity);
                                                    break;
                                                case ".PTG":
                                                case ".ptg":
                                                    ptgConverter.toLaz(scan, directory, false, exportIntensity);
                                                    break;
                                                default:
                                                    break;
                                            }
                                            
                                            break;
                                        case "laz":
                                            
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toLaz(scan, directory, true, exportIntensity);
                                                    break;
                                                case ".PTX":
                                                case ".ptx":
                                                    //ptxConverter.toLaz(scan, directory, true, exportIntensity);
                                                    break;
                                                case ".PTG":
                                                case ".ptg":
                                                    ptgConverter.toLaz(scan, directory, true, exportIntensity);
                                                    break;
                                                default:
                                                    break;
                                            }
                                            
                                            break;
                                        case "shots+echoes":
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toShots(scan, directory, exportReflectance, exportDeviation, exportAmplitude, exportTime, exportXYZ);
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                        case "shots+echoes+shotTimeStamp":
                                            switch (extension) {
                                                case ".rxp":
                                                    rxpConverter.toShots2(scan, directory, exportReflectance, exportDeviation, exportAmplitude, exportTime, exportXYZ);
                                                    break;
                                                default:
                                                    break;
                                            }
                                            break;
                                    }

                                    count++;
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
