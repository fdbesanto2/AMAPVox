/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.engine3d.JOGLWindow;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceAdapter;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceListener;
import fr.ird.voxelidar.util.MatrixConverter;
import fr.ird.voxelidar.util.MatrixFileParser;
import fr.ird.voxelidar.util.Settings;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import fr.ird.voxelidar.voxelisation.VoxelisationTool;
import fr.ird.voxelidar.voxelisation.VoxelisationToolListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.collections.ObservableListBase;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class MainFrameController implements Initializable {
    
    private final static Logger logger = Logger.getLogger(MainFrameController.class);
    private Stage stage;
    
    private CalculateMatrixFrameController calculateMatrixFrameController;
    private Stage calculateMatrixFrame;
    
    private FileChooser fileChooserOpenInputFileALS;
    private FileChooser fileChooserOpenTrajectoryFileALS;
    private FileChooser fileChooserOpenOutputFileALS;
    private FileChooser fileChooserOpenInputFileTLS;
    private FileChooser fileChooserOpenVoxelFile;
    private FileChooser fileChooserOpenPopMatrixFile;
    private FileChooser fileChooserOpenSopMatrixFile;
    private FileChooser fileChooserOpenVopMatrixFile;
    private DirectoryChooser directoryChooserOpenOutputPathTLS;
    
    private Matrix4d popMatrix;
    private Matrix4d sopMatrix;
    private Matrix4d vopMatrix;
    private Matrix4d resultMatrix;
    
    @FXML
    private TextField textFieldEnterXMax;
    @FXML
    private TextField textFieldEnterYMin;
    @FXML
    private TextField textFieldEnterZMin;
    @FXML
    private TextField textFieldEnterZMax;
    @FXML
    private TextField textFieldEnterYMax;
    @FXML
    private TextField textFieldEnterXMin;
    @FXML
    private TextField textFieldXNumber;
    @FXML
    private TextField textFieldYNumber;
    @FXML
    private TextField textFieldZNumber;
    @FXML
    private TextField textFieldResolution;
    @FXML
    private CheckBox checkboxUsePopMatrix;
    @FXML
    private CheckBox checkboxUseSopMatrix;
    @FXML
    private CheckBox checkboxUseVopMatrix;
    @FXML
    private TextField labelM00;
    @FXML
    private TextField labelM10;
    @FXML
    private TextField labelM20;
    @FXML
    private TextField labelM30;
    @FXML
    private TextField labelM01;
    @FXML
    private TextField labelM11;
    @FXML
    private TextField labelM21;
    @FXML
    private TextField labelM31;
    @FXML
    private TextField labelM02;
    @FXML
    private TextField labelM12;
    @FXML
    private TextField labelM22;
    @FXML
    private TextField labelM32;
    @FXML
    private TextField labelM03;
    @FXML
    private TextField labelM13;
    @FXML
    private TextField labelM23;
    @FXML
    private TextField labelM33;
    @FXML
    private CheckBox checkboxUseDTMFilter;
    @FXML
    private TextField textfieldDTMPath;
    @FXML
    private Button buttonOpenDTMFile;
    @FXML
    private TextField textfieldDTMValue;
    @FXML
    private Button buttonOpenInputFileALS;
    @FXML
    private Button buttonOpenTrajectoryFileALS;
    @FXML
    private Button buttonOpenOutputFileALS;
    @FXML
    private Button buttonOpenOutputPathTLS;
    @FXML
    private Button buttonOpenInputFileTLS;
    @FXML
    private TextField textFieldInputFileALS;
    @FXML
    private TextField textFieldTrajectoryFileALS;
    @FXML
    private TextField textFieldOutputFileALS;
    @FXML
    private TextField textFieldOutputPathTLS;
    @FXML
    private TextField textFieldInputFileTLS;
    @FXML
    private ComboBox<String> comboboxModeALS;
    @FXML
    private ComboBox<String> comboboxModeTLS;
    @FXML
    private Button buttonLoadSelectedVoxelFile;
    @FXML
    private Button buttonRemoveVoxelFileFromListView;
    @FXML
    private Button buttonAddVoxelFileToListView;
    @FXML
    private ListView<File> listViewVoxelsFiles;
    @FXML
    private MenuItem menuItemSelectionAll;
    @FXML
    private MenuItem menuItemSelectionNone;
    @FXML
    private Button buttonOpen3DView;
    @FXML
    private ComboBox<String> comboboxAttributeToView;
    @FXML
    private Button buttonCreateAttribut;
    @FXML
    private MenuItem menuItemLoad;
    @FXML
    private MenuItem menuItemSave;
    @FXML
    private MenuItem menuItemSaveAs;
    @FXML
    private Button buttonOpenSopMatrixFile;
    @FXML
    private Button buttonOpenPopMatrixFile;
    @FXML
    private Button buttonOpenVopMatrixFile;
    @FXML
    private Button buttonEnterReferencePointsVop;
    @FXML
    private MenuItem menuitemALSActionExecute;
    @FXML
    private MenuItem menuitemALSActionAddTask;
    @FXML
    private ComboBox<?> comboboxWeighting;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        fileChooserOpenInputFileALS = new FileChooser();
        fileChooserOpenInputFileALS.setTitle("Open input file");
        fileChooserOpenInputFileALS.getExtensionFilters().addAll(
                                    new ExtensionFilter("All Files", "*.*"),
                                    new ExtensionFilter("Text Files", "*.txt"),
                                    new ExtensionFilter("Las Files", "*.las", "*.laz"));
        
        fileChooserOpenTrajectoryFileALS = new FileChooser();
        fileChooserOpenTrajectoryFileALS.setTitle("Open trajectory file");
        fileChooserOpenTrajectoryFileALS.getExtensionFilters().addAll(
                                    new ExtensionFilter("All Files", "*.*"),
                                    new ExtensionFilter("Text Files", "*.txt"));
        
        fileChooserOpenOutputFileALS = new FileChooser();
        fileChooserOpenOutputFileALS.setTitle("Choose output file");
        
        fileChooserOpenInputFileTLS = new FileChooser();
        fileChooserOpenInputFileTLS.setTitle("Open input file");
        fileChooserOpenInputFileTLS.getExtensionFilters().addAll(
                                    new ExtensionFilter("All Files", "*.*"),
                                    new ExtensionFilter("Text Files", "*.txt"),
                                    new ExtensionFilter("Rxp Files", "*.rxp"),
                                    new ExtensionFilter("Project Rsp Files", "*.rsp"));
        
        directoryChooserOpenOutputPathTLS = new DirectoryChooser();
        directoryChooserOpenOutputPathTLS.setTitle("Choose output path");
        
        fileChooserOpenVoxelFile = new FileChooser();
        fileChooserOpenVoxelFile.setTitle("Open voxel file");
        fileChooserOpenVoxelFile.getExtensionFilters().addAll(
                                    new ExtensionFilter("All Files", "*.*"),
                                    new ExtensionFilter("Voxel Files", "*.vox"),
                                    new ExtensionFilter("Rxp Files", "*.rxp"));
        
        fileChooserOpenPopMatrixFile = new FileChooser();
        fileChooserOpenPopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenPopMatrixFile.getExtensionFilters().addAll(
                                    new ExtensionFilter("All Files", "*.*"),
                                    new ExtensionFilter("Text Files", "*.txt"));
        
        fileChooserOpenSopMatrixFile = new FileChooser();
        fileChooserOpenSopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenSopMatrixFile.getExtensionFilters().addAll(
                                    new ExtensionFilter("All Files", "*.*"),
                                    new ExtensionFilter("Text Files", "*.txt"));
        
        fileChooserOpenVopMatrixFile = new FileChooser();
        fileChooserOpenVopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenVopMatrixFile.getExtensionFilters().addAll(
                                    new ExtensionFilter("All Files", "*.*"),
                                    new ExtensionFilter("Text Files", "*.txt"));
        
        comboboxModeALS.getItems().addAll("Las file", "Laz file", "Points file", "Shots file");
        comboboxModeTLS.getItems().addAll("Rxp scan", "Rsp project", "Points file", "Shots file");
        
        listViewVoxelsFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        listViewVoxelsFiles.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int size = listViewVoxelsFiles.getSelectionModel().getSelectedIndices().size();
                
                if(size == 1){
                    buttonLoadSelectedVoxelFile.setDisable(false);
                }else{
                    buttonLoadSelectedVoxelFile.setDisable(true);
                }
            }
        });
        
        popMatrix = new Matrix4d();
        popMatrix.setIdentity();
        sopMatrix = new Matrix4d();
        sopMatrix.setIdentity();
        vopMatrix = new Matrix4d();
        vopMatrix.setIdentity();
        resultMatrix = new Matrix4d();
        resultMatrix.setIdentity();
        
        calculateMatrixFrame = new Stage();
        
        
        Parent root;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CalculateMatrixFrame.fxml"));
            root = loader.load();
            calculateMatrixFrameController = loader.getController();
            calculateMatrixFrameController.setStage(calculateMatrixFrame);
            Scene scene = new Scene(root);
            calculateMatrixFrame.setScene(scene);  
        } catch (IOException ex) {
            logger.error(ex);
        }
        
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    private void updateResultMatrix(){
        
        resultMatrix = new Matrix4d();
        resultMatrix.setIdentity();
        
        if(checkboxUseSopMatrix.isSelected()){
            resultMatrix.mul(sopMatrix);
        }
        
        if(checkboxUsePopMatrix.isSelected()){
            resultMatrix.mul(popMatrix);
        }
        
        if(checkboxUseVopMatrix.isSelected()){
            resultMatrix.mul(vopMatrix);
        }
        
        
        labelM00.setText(String.valueOf(resultMatrix.m00));
        labelM01.setText(String.valueOf(resultMatrix.m01));
        labelM02.setText(String.valueOf(resultMatrix.m02));
        labelM03.setText(String.valueOf(resultMatrix.m03));
        labelM10.setText(String.valueOf(resultMatrix.m10));
        labelM11.setText(String.valueOf(resultMatrix.m11));
        labelM12.setText(String.valueOf(resultMatrix.m12));
        labelM13.setText(String.valueOf(resultMatrix.m13));
        labelM20.setText(String.valueOf(resultMatrix.m20));
        labelM21.setText(String.valueOf(resultMatrix.m21));
        labelM22.setText(String.valueOf(resultMatrix.m22));
        labelM23.setText(String.valueOf(resultMatrix.m23));
        labelM30.setText(String.valueOf(resultMatrix.m30));
        labelM31.setText(String.valueOf(resultMatrix.m31));
        labelM32.setText(String.valueOf(resultMatrix.m32));
        labelM33.setText(String.valueOf(resultMatrix.m33));
    }

    @FXML
    private void onActionButtonVizualize(ActionEvent event) {
        
        final VoxelSpace voxelSpace = new VoxelSpace(listViewVoxelsFiles.getSelectionModel().getSelectedItem());
        
        Settings settings = new Settings(false, true, true, true, comboboxAttributeToView.getSelectionModel().getSelectedItem());
        
        voxelSpace.setSettings(settings);
        voxelSpace.setCurrentAttribut(settings.attributeToVisualize);
                
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {
                        
                        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                            @Override
                            public void voxelSpaceCreationProgress(int progress) {
                                updateProgress(progress, 100);
                            }
                        });
                        
                        voxelSpace.load();
                        voxelSpace.updateValue();
                        
                        JOGLWindow joglWindow = new JOGLWindow(640, 480, 
                                listViewVoxelsFiles.getSelectionModel().getSelectedItem().toString(), 
                                voxelSpace, settings);
                        
                        joglWindow.show();
                        joglWindow.setOnTop();
                        return null;
                    }
                };
            }
        };
        
        ProgressDialog d = new ProgressDialog(service);
        
        d.show();
    }

    @FXML
    private void onActionButtonOpenInputFileALS(ActionEvent event) {
        
        
        File selectedFile = fileChooserOpenInputFileALS.showOpenDialog(stage);
        if (selectedFile != null) {
           textFieldInputFileALS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenTrajectoryFileALS(ActionEvent event) {
        
        File selectedFile = fileChooserOpenTrajectoryFileALS.showOpenDialog(stage);
        if (selectedFile != null) {
           textFieldTrajectoryFileALS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenOutputFileALS(ActionEvent event) {
        
        File selectedFile = fileChooserOpenOutputFileALS.showOpenDialog(stage);
        if (selectedFile != null) {
           textFieldOutputFileALS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenOutputPathTLS(ActionEvent event) {
        
        File selectedFile = directoryChooserOpenOutputPathTLS.showDialog(stage);
        if (selectedFile != null) {
           textFieldOutputPathTLS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenInputFileTLS(ActionEvent event) {
        
        File selectedFile = fileChooserOpenInputFileTLS.showOpenDialog(stage);
        if (selectedFile != null) {
           textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonLoadSelectedVoxelFile(ActionEvent event) {
        
        String[] parameters = VoxelSpaceData.readAttributs(listViewVoxelsFiles.getSelectionModel().getSelectedItem());
            
        for(int i=0 ; i< parameters.length ;i++){

            parameters[i] = parameters[i].replaceAll(" ", "");
            parameters[i] = parameters[i].replaceAll("#", "");
        }
        
        comboboxAttributeToView.getItems().addAll(parameters);
        
        if(parameters.length>3){
            comboboxAttributeToView.getSelectionModel().select(3);
        }
        
        buttonOpen3DView.setDisable(false);
    }

    @FXML
    private void onActionButtonRemoveVoxelFileFromListView(ActionEvent event) {
        
        ObservableList<File> selectedItems = listViewVoxelsFiles.getSelectionModel().getSelectedItems();
        listViewVoxelsFiles.getItems().removeAll(selectedItems);
        
        listViewVoxelsFiles.getSelectionModel().clearSelection();
        
    }

    @FXML
    private void onActionButtonAddVoxelFileToListView(ActionEvent event) {
        
        List<File> selectedFiles = fileChooserOpenInputFileTLS.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            for(File file : selectedFiles){
                listViewVoxelsFiles.getItems().add(file);
            }
        }
    }

    @FXML
    private void onActionMenuItemSelectionAll(ActionEvent event) {
        listViewVoxelsFiles.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemSelectionNone(ActionEvent event) {
        listViewVoxelsFiles.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionMenuItemLoad(ActionEvent event) {
        
    }

    @FXML
    private void onActionMenuItemSave(ActionEvent event) {
        
    }

    @FXML
    private void onActionMenuItemSaveAs(ActionEvent event) {
        
    }

    @FXML
    private void onActionButtonOpenSopMatrixFile(ActionEvent event) {
        
        File selectedFile = fileChooserOpenSopMatrixFile.showOpenDialog(stage);
        if (selectedFile != null) {
           
           sopMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
           updateResultMatrix();
        }
        
    }

    @FXML
    private void onActionButtonOpenPopMatrixFile(ActionEvent event) {
        
        File selectedFile = fileChooserOpenPopMatrixFile.showOpenDialog(stage);
        if (selectedFile != null) {
           
           popMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
           updateResultMatrix();
        }
    }

    @FXML
    private void onActionButtonOpenVopMatrixFile(ActionEvent event) {
        
        File selectedFile = fileChooserOpenVopMatrixFile.showOpenDialog(stage);
        if (selectedFile != null) {
           
           vopMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
           updateResultMatrix();
        }
    }

    @FXML
    private void onActionCheckboxUsePopMatrix(ActionEvent event) {
        updateResultMatrix();
    }

    @FXML
    private void onActionCheckboxUseSopMatrix(ActionEvent event) {
        updateResultMatrix();
    }

    @FXML
    private void onActionCheckboxUseVopMatrix(ActionEvent event) {
        buttonEnterReferencePointsVop.setDisable(false);
        updateResultMatrix();
    }

    @FXML
    private void onActionButtonEnterReferencePointsVop(ActionEvent event) {
        
        
        calculateMatrixFrame.show();

        calculateMatrixFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                if(calculateMatrixFrameController.getMatrix() != null){
                    vopMatrix = calculateMatrixFrameController.getMatrix();
                    updateResultMatrix();
                }
            }
        });
        
    }

    @FXML
    private void onActionMenuitemALSActionExecute(ActionEvent event) {
        
        final String inputVoxPath = textFieldInputFileALS.getText();
        
        final File trajectoryFile = new File(textFieldTrajectoryFileALS.getText());
        
        final File dtmFile = new File(textfieldDTMPath.getText());
        
        final VoxelParameters parameters = new VoxelParameters();
        parameters.setBottomCorner(new Point3d(
                            Double.valueOf(textFieldEnterXMin.getText()), 
                            Double.valueOf(textFieldEnterYMin.getText()), 
                            Double.valueOf(textFieldEnterZMin.getText())));
        
        parameters.setTopCorner(new Point3d(
                            Double.valueOf(textFieldEnterXMax.getText()), 
                            Double.valueOf(textFieldEnterYMax.getText()), 
                            Double.valueOf(textFieldEnterZMax.getText())));
        
        parameters.setSplit(new Point3i(
                            Integer.valueOf(textFieldXNumber.getText()), 
                            Integer.valueOf(textFieldYNumber.getText()), 
                            Integer.valueOf(textFieldZNumber.getText())));
        
        parameters.setResolution(Double.valueOf(textFieldResolution.getText()));
        parameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex());
        parameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
        
        
        final File outputFile = new File(textFieldOutputFileALS.getText());
        
        final VoxelisationTool voxTool = new VoxelisationTool();
        final long start_time = System.currentTimeMillis();
        
           
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {

                        voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                            @Override
                            public void voxelisationProgress(String progress, int ratio) {
                                updateMessage(progress);
                            }

                            @Override
                            public void voxelisationFinished(float duration) {

                                logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                            }
                        });
                        
                        voxTool.generateVoxelsFromLas(outputFile, new File(inputVoxPath), trajectoryFile, dtmFile, parameters, MatrixConverter.convertMatrix4dToMat4D(resultMatrix));
                        listViewVoxelsFiles.getItems().add(outputFile);

                        listViewVoxelsFiles.getSelectionModel().select(outputFile);
                        
                        return null;
                    }
                };
            }
        };

        ProgressDialog d = new ProgressDialog(service);
        
        d.show();
    }

    @FXML
    private void onActionMenuitemALSActionAddTask(ActionEvent event) {
    }
    
}
