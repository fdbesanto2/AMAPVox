/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import de.matthiasmann.twlthemeeditor.gui.MessageDialog;
import fr.ird.voxelidar.Configuration.InputType;
import fr.ird.voxelidar.Configuration.ProcessMode;
import fr.ird.voxelidar.engine3d.JOGLWindow;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceAdapter;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData;
import fr.ird.voxelidar.gui.GLRenderWindowListener;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.lidar.format.dart.DartWriter;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import fr.ird.voxelidar.multires.ProcessingMultiRes;
import fr.ird.voxelidar.util.Filter;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SingleSelectionModel;
import javafx.scene.control.TabPane;
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
import org.controlsfx.control.RangeSlider;
import org.controlsfx.dialog.ProgressDialog;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class MainFrameController implements Initializable {
    
    
    private final static Logger logger = Logger.getLogger(MainFrameController.class);
    private Stage stage;
    
    private Stage calculateMatrixFrame;
    private Stage filterFrame;
    
    private BlockingQueue<File> queue = new ArrayBlockingQueue<>(100);
    private int taskNumber = 0;
    private int taskID = 1;
    
    private CalculateMatrixFrameController calculateMatrixFrameController;
    private FilterFrameController filterFrameController;

    private File lastFCOpenConfiguration;
    private File lastFCSaveConfiguration;
    private File lastFCOpenInputFileALS;
    private File lastFCOpenTrajectoryFileALS;
    private File lastFCOpenOutputFileALS;
    private File lastFCOpenInputFileTLS;
    private File lastFCOpenVoxelFile;
    private File lastFCOpenPopMatrixFile;
    private File lastFCOpenSopMatrixFile;
    private File lastFCOpenVopMatrixFile;
    private File lastFCOpenDTMFile;
    private File lastFCOpenMultiResVoxelFile;
    private File lastFCSaveOutputFileMultiRes;
    private File lastFCAddTask;
    private File lastFCSaveDartFile;
    private File lastFCSaveMergingFile;
    private File lastFCOpenPonderationFile;

    private FileChooser fileChooserOpenConfiguration;
    private FileChooser fileChooserSaveConfiguration;
    private FileChooser fileChooserOpenInputFileALS;
    private FileChooser fileChooserOpenTrajectoryFileALS;
    private FileChooser fileChooserOpenOutputFileALS;
    private FileChooser fileChooserOpenInputFileTLS;
    private FileChooser fileChooserOpenVoxelFile;
    private FileChooser fileChooserOpenPopMatrixFile;
    private FileChooser fileChooserOpenSopMatrixFile;
    private FileChooser fileChooserOpenVopMatrixFile;
    private FileChooser fileChooserOpenPonderationFile;
    private FileChooser fileChooserOpenDTMFile;
    private FileChooser fileChooserOpenMultiResVoxelFile;
    private FileChooser fileChooserAddTask;
    private FileChooser fileChooserOpenOutputFileMultiRes;
    private FileChooser fileChooserOpenOutputFileMerging;
    private FileChooser fileChooserSaveDartFile;
    private FileChooser fileChooserSaveOutputFileTLS;
    private DirectoryChooser directoryChooserOpenOutputPathTLS;

    private Matrix4d popMatrix;
    private Matrix4d sopMatrix;
    private Matrix4d vopMatrix;
    private Matrix4d resultMatrix;
    
    private String scanFilter;
    private boolean filterScan;
    private List<MatrixAndFile> items;
    private Rsp rsp;
    
    private final static String MATRIX_FORMAT_ERROR_MSG = "Matrix file has to look like this: \n\n\t1.0 0.0 0.0 0.0\n\t0.0 1.0 0.0 0.0\n\t0.0 0.0 1.0 0.0\n\t0.0 0.0 0.0 1.0\n";
    
    private RangeSlider rangeSliderFilterValue;
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
    private Button buttonOpenPopMatrixFile;
    @FXML
    private Button buttonOpenVopMatrixFile;
    @FXML
    private Button buttonEnterReferencePointsVop;
    @FXML
    private ComboBox<String> comboboxWeighting;
    @FXML
    private ListView<File> listViewTaskList;
    @FXML
    private Label labelDTMPath;
    @FXML
    private Label labelDTMValue;
    @FXML
    private Button buttonALSAddToTaskList;
    @FXML
    private Button buttonExecute;
    @FXML
    private Button buttonTLSAddToTaskList;
    @FXML
    private Button buttonAddVoxelFileToMultiResListView;
    @FXML
    private MenuItem menuItemSelectionAllMultiRes;
    @FXML
    private MenuItem menuItemSelectionNoneMultiRes;
    @FXML
    private Button buttonRemoveVoxelFileFromMultiResListView;
    @FXML
    private ListView<File> listViewMultiResVoxelFiles;
    @FXML
    private Button buttonMultiResAddToTaskList;
    @FXML
    private Button buttonOpenOutputFileMultiRes;
    @FXML
    private TextField textFieldOutputFileMultiRes;
    @FXML
    private Button buttonAddTaskToListView;
    @FXML
    private Button buttonLoadSelectedTask;
    private CheckBox comboBoxEnableWeighting;
    @FXML
    private CheckBox checkboxEnableWeighting;
    @FXML
    private CheckBox checkBoxUseDefaultSopMatrix;
    @FXML
    private CheckBox checkBoxUseDefaultPopMatrix;
    @FXML
    private TabPane tabPaneVoxelisation;
    @FXML
    private Button buttonRemoveTaskFromListView;
    @FXML
    private MenuItem menuItemTaskSelectionAll;
    @FXML
    private MenuItem menuItemTaskSelectionNone;
    @FXML
    private MenuButton menuButtonExport;
    @FXML
    private MenuItem menuItemExportDart;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private TextField textFieldPADMax;
    @FXML
    private TextField textFieldTLSFilter;
    @FXML
    private ListView<MatrixAndFile> listviewRxpScans;
    @FXML
    private CheckBox checkboxFilter;
    @FXML
    private CheckBox checkboxMergeAfter;
    @FXML
    private TextField textFieldMergedFileName;
    @FXML
    private Button buttonMergingAddToTaskList;
    @FXML
    private Button buttonOpenOutputFileMerging;
    @FXML
    private TextField textFieldOutputFileMerging;
    @FXML
    private ListView<Filter> listviewFilters;
    @FXML
    private Button buttonAddFilter;
    @FXML
    private Button buttonRemoveFilter;
    @FXML
    private Label labelTLSOutputPath;
    @FXML
    private Button buttonOpenPonderationFile;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        fileChooserOpenConfiguration = new FileChooser();
        fileChooserOpenConfiguration.setTitle("Choose configuration file");

        fileChooserSaveConfiguration = new FileChooser();
        fileChooserSaveConfiguration.setTitle("Choose output file");

        fileChooserOpenInputFileALS = new FileChooser();
        fileChooserOpenInputFileALS.setTitle("Open input file");
        fileChooserOpenInputFileALS.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("Las Files", "*.las", "*.laz"));

        fileChooserOpenTrajectoryFileALS = new FileChooser();
        fileChooserOpenTrajectoryFileALS.setTitle("Open trajectory file");
        fileChooserOpenTrajectoryFileALS.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Text Files", "*.txt"));

        fileChooserOpenOutputFileALS = new FileChooser();
        fileChooserOpenOutputFileALS.setTitle("Choose output file");

        fileChooserOpenInputFileTLS = new FileChooser();
        fileChooserOpenInputFileTLS.setTitle("Open input file");
        fileChooserOpenInputFileTLS.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Text Files", "*.txt"),
                new ExtensionFilter("Rxp Files", "*.rxp"),
                new ExtensionFilter("Project Rsp Files", "*.rsp"));

        directoryChooserOpenOutputPathTLS = new DirectoryChooser();
        directoryChooserOpenOutputPathTLS.setTitle("Choose output path");
        
        fileChooserSaveOutputFileTLS = new FileChooser();
        fileChooserSaveOutputFileTLS.setTitle("Save voxel file");

        fileChooserOpenVoxelFile = new FileChooser();
        fileChooserOpenVoxelFile.setTitle("Open voxel file");
        fileChooserOpenVoxelFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Voxel Files", "*.vox"));

        fileChooserOpenPopMatrixFile = new FileChooser();
        fileChooserOpenPopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenPopMatrixFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Text Files", "*.txt"));

        fileChooserOpenSopMatrixFile = new FileChooser();
        fileChooserOpenSopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenSopMatrixFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Text Files", "*.txt"));

        fileChooserOpenVopMatrixFile = new FileChooser();
        fileChooserOpenVopMatrixFile.setTitle("Choose matrix file");
        fileChooserOpenVopMatrixFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Text Files", "*.txt"));
        
        fileChooserOpenPonderationFile = new FileChooser();
        fileChooserOpenPonderationFile.setTitle("Choose ponderation file");
        fileChooserOpenPonderationFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Text Files", "*.txt"));

        fileChooserOpenDTMFile = new FileChooser();
        fileChooserOpenDTMFile.setTitle("Choose DTM file");
        fileChooserOpenDTMFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("DTM Files", "*.asc"));

        fileChooserOpenMultiResVoxelFile = new FileChooser();
        fileChooserOpenMultiResVoxelFile.setTitle("Choose voxel file");
        fileChooserOpenMultiResVoxelFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Voxel Files", "*.vox"));

        fileChooserOpenOutputFileMultiRes = new FileChooser();
        fileChooserOpenOutputFileMultiRes.setTitle("Save voxel file");

        fileChooserAddTask = new FileChooser();
        fileChooserAddTask.setTitle("Choose parameter file");
        fileChooserAddTask.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("XML Files", "*.xml"));
        
        fileChooserSaveDartFile  = new FileChooser();
        fileChooserSaveDartFile.setTitle("Save dart file (.maket)");
        fileChooserSaveDartFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Maket File", "*.maket"));
        
        fileChooserOpenOutputFileMerging  = new FileChooser();
        fileChooserOpenOutputFileMerging.setTitle("Choose voxel file");
        fileChooserOpenOutputFileMerging.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Voxel Files", "*.vox"));

        comboboxModeALS.getItems().addAll("Las file", "Laz file", "Points file (unavailable)", "Shots file (unavailable)");
        comboboxModeTLS.getItems().addAll("Rxp scan", "Rsp project", "Points file (unavailable)", "Shots file (unavailable)");
        comboboxWeighting.getItems().addAll("From the echo number", "From a matrix file", "Local recalculation (unavailable)");

        listViewVoxelsFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listViewVoxelsFiles.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int size = listViewVoxelsFiles.getSelectionModel().getSelectedIndices().size();

                if (size == 1) {
                    buttonLoadSelectedVoxelFile.setDisable(false);
                    menuButtonExport.setDisable(false);
                } else {
                    buttonLoadSelectedVoxelFile.setDisable(true);
                    menuButtonExport.setDisable(true);
                }
            }
        });
        
        listViewTaskList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int size = listViewTaskList.getSelectionModel().getSelectedIndices().size();

                if (size == 1) {
                    buttonLoadSelectedTask.setDisable(false);
                } else {
                    buttonLoadSelectedTask.setDisable(true);
                }
            }
        });

        listViewMultiResVoxelFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
        
        filterFrame = new Stage();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FilterFrame.fxml"));
            root = loader.load();
            filterFrameController = loader.getController();
            filterFrameController.setStage(filterFrame);
            filterFrame.setScene(new Scene(root));
        } catch (IOException ex) {
            logger.error(ex);
        }
        

        ChangeListener cl = new ChangeListener<Object>() {

            @Override
            public void changed(ObservableValue<? extends Object> observable, Object oldValue, Object newValue) {

                try {

                    float resolution = Float.valueOf(textFieldResolution.getText());

                    double minPointX = Double.valueOf(textFieldEnterXMin.getText());
                    double maxPointX = Double.valueOf(textFieldEnterXMax.getText());
                    int voxelNumberX = (int) (maxPointX - minPointX);
                    textFieldXNumber.setText(String.valueOf(voxelNumberX));
                    voxelNumberX = (int) ((maxPointX - minPointX) / resolution);
                    textFieldXNumber.setText(String.valueOf(voxelNumberX));

                    double minPointY = Double.valueOf(textFieldEnterYMin.getText());
                    double maxPointY = Double.valueOf(textFieldEnterYMax.getText());
                    int voxelNumberY = (int) (maxPointY - minPointY);
                    textFieldYNumber.setText(String.valueOf(voxelNumberY));
                    voxelNumberY = (int) ((maxPointY - minPointY) / resolution);
                    textFieldYNumber.setText(String.valueOf(voxelNumberY));

                    double minPointZ = Double.valueOf(textFieldEnterZMin.getText());
                    double maxPointZ = Double.valueOf(textFieldEnterZMax.getText());
                    int voxelNumberZ = (int) (maxPointZ - minPointZ);
                    textFieldZNumber.setText(String.valueOf(voxelNumberZ));
                    voxelNumberZ = (int) ((maxPointZ - minPointZ) / resolution);
                    textFieldZNumber.setText(String.valueOf(voxelNumberZ));

                } catch (Exception e) {

                }

            }
        };

        textFieldEnterXMin.textProperty().addListener(cl);
        textFieldEnterYMin.textProperty().addListener(cl);
        textFieldEnterZMin.textProperty().addListener(cl);

        textFieldEnterXMax.textProperty().addListener(cl);
        textFieldEnterYMax.textProperty().addListener(cl);
        textFieldEnterZMax.textProperty().addListener(cl);

        textFieldResolution.textProperty().addListener(cl);

        checkboxUseDTMFilter.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if (checkboxUseDTMFilter.isSelected()) {
                    buttonOpenDTMFile.setDisable(false);
                    textfieldDTMPath.setDisable(false);
                    textfieldDTMValue.setDisable(false);
                    labelDTMValue.setDisable(false);
                    labelDTMPath.setDisable(false);
                } else {
                    buttonOpenDTMFile.setDisable(true);
                    textfieldDTMPath.setDisable(true);
                    textfieldDTMValue.setDisable(true);
                    labelDTMValue.setDisable(true);
                    labelDTMPath.setDisable(true);
                }
            }
        });
        
        checkboxUseVopMatrix.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    buttonOpenVopMatrixFile.setDisable(false);
                    buttonEnterReferencePointsVop.setDisable(false);
                }else{
                    buttonOpenVopMatrixFile.setDisable(true);
                    buttonEnterReferencePointsVop.setDisable(true);
                }
            }
        });
        
        checkboxUsePopMatrix.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    checkBoxUseDefaultPopMatrix.setDisable(false);
                    buttonOpenPopMatrixFile.setDisable(false);
                }else{
                    checkBoxUseDefaultPopMatrix.setDisable(true);
                    buttonOpenPopMatrixFile.setDisable(true);
                }
            }
        });
        
        checkboxUseSopMatrix.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    checkBoxUseDefaultSopMatrix.setDisable(false);
                }else{
                    checkBoxUseDefaultSopMatrix.setDisable(true);
                }
            }
        });
        
        scanFilter = textFieldTLSFilter.getText();
        filterScan = checkboxFilter.isSelected();
        
        checkboxFilter.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                doFilterOnScanListView();
                
            }
        });
        
        comboboxWeighting.disableProperty().bind(checkboxEnableWeighting.selectedProperty().not());
        
        textFieldTLSFilter.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                doFilterOnScanListView();
            }
        });
        
        listviewRxpScans.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<MatrixAndFile>() {

            @Override
            public void changed(ObservableValue<? extends MatrixAndFile> observable, MatrixAndFile oldValue, MatrixAndFile newValue) {
                sopMatrix = newValue.matrix;
                updateResultMatrix();
            }
        });
        
        comboboxModeTLS.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                switch(newValue.intValue()){
                    
                    case 1:
                        checkboxFilter.setDisable(false);
                        listviewRxpScans.setDisable(false);
                        textFieldTLSFilter.setDisable(false);
                        checkboxMergeAfter.setDisable(false);
                        textFieldMergedFileName.setDisable(false);
                        disableSopMatrixChoice(false);
                        labelTLSOutputPath.setText("Output path");
                        break;
                        
                    default:
                        checkboxFilter.setDisable(true);
                        listviewRxpScans.setDisable(true);
                        textFieldTLSFilter.setDisable(true);
                        checkboxMergeAfter.setDisable(true);
                        textFieldMergedFileName.setDisable(true);
                        disableSopMatrixChoice(true);
                        labelTLSOutputPath.setText("Output file");
                }
            }
        });
        
        tabPaneVoxelisation.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                switch(newValue.intValue()){
                    
                    case 1:
                        disableSopMatrixChoice(false);
                        disablePopMatrixChoice(false);
                        break;
                        
                    default:
                        disableSopMatrixChoice(true);
                        disablePopMatrixChoice(true);
                }
            }
        });
        
        comboboxWeighting.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                switch(newValue.intValue()){
                    
                    case 1:
                        buttonOpenPonderationFile.setVisible(true);
                        break;
                        
                    default:
                        buttonOpenPonderationFile.setVisible(false);
                }
            }
        });

    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    private void disableSopMatrixChoice(boolean value){
        
        if(value){
            checkboxUseSopMatrix.setDisable(true);
            checkBoxUseDefaultSopMatrix.setDisable(true);
        }else{
            checkboxUseSopMatrix.setDisable(false);
            checkBoxUseDefaultSopMatrix.setDisable(false);
        }
    }
    
    private void disablePopMatrixChoice(boolean value){
        
        if(value){
            checkboxUsePopMatrix.setDisable(true);
            checkBoxUseDefaultPopMatrix.setDisable(true);
            buttonOpenPopMatrixFile.setDisable(true);
        }else{
            checkboxUsePopMatrix.setDisable(false);
            checkBoxUseDefaultPopMatrix.setDisable(false);
            buttonOpenPopMatrixFile.setDisable(false);
        }
    }

    private void updateResultMatrix() {

        resultMatrix = new Matrix4d();
        resultMatrix.setIdentity();
        
        if (checkboxUseVopMatrix.isSelected() && vopMatrix != null) {
            resultMatrix.mul(vopMatrix);
        }
        
        if (checkboxUsePopMatrix.isSelected() && popMatrix != null) {
            resultMatrix.mul(popMatrix);
        }
        
        if (checkboxUseSopMatrix.isSelected() && sopMatrix != null) {
            resultMatrix.mul(sopMatrix);
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
        final JOGLWindow joglWindow = new JOGLWindow(640, 480,
                                listViewVoxelsFiles.getSelectionModel().getSelectedItem().toString(),
                                voxelSpace, settings);
        
        Stage toolBarFrameStage = new Stage();
        ToolBarFrameController toolBarFrameController;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ToolBarFrame.fxml"));
        Parent root;
        try {
            root = loader.load();
            Scene scene = new Scene(root);
            toolBarFrameStage.setScene(scene);
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
        toolBarFrameController = loader.getController();
        
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
                        
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                
                                stage.setAlwaysOnTop(false);
                                
                                toolBarFrameController.setJoglListener(joglWindow.getJoglContext());
                                toolBarFrameController.setAttributes(comboboxAttributeToView.getItems());

                                joglWindow.addWindowListener(new GLRenderWindowListener(toolBarFrameStage, joglWindow.getAnimator()));

                                joglWindow.show();

                                toolBarFrameStage.setX(joglWindow.getPosition().getX()-toolBarFrameStage.getWidth());
                                toolBarFrameStage.setY(joglWindow.getPosition().getY());
                                toolBarFrameStage.show();


                                joglWindow.setOnTop();
                                
                            }
                        });
                        
                        
                        return null;
                    }
                };
            }
        };

        ProgressDialog d = new ProgressDialog(service);
        d.show();
        ChangeListener<Boolean> cl = new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                if(newValue){
                    joglWindow.setOnTop();
                    toolBarFrameStage.setAlwaysOnTop(true);
                    stage.focusedProperty().removeListener(this);
                }
                
            }
        };
        
        stage.focusedProperty().addListener(cl);
        
        service.start();
    }

    @FXML
    private void onActionButtonOpenInputFileALS(ActionEvent event) {

        if (lastFCOpenInputFileALS != null) {
            fileChooserOpenInputFileALS.setInitialDirectory(lastFCOpenInputFileALS.getParentFile());
        }

        File selectedFile = fileChooserOpenInputFileALS.showOpenDialog(stage);
        if (selectedFile != null) {
            lastFCOpenInputFileALS = selectedFile;
            textFieldInputFileALS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenTrajectoryFileALS(ActionEvent event) {

        if (lastFCOpenTrajectoryFileALS != null) {
            fileChooserOpenTrajectoryFileALS.setInitialDirectory(lastFCOpenTrajectoryFileALS.getParentFile());
        }

        File selectedFile = fileChooserOpenTrajectoryFileALS.showOpenDialog(stage);
        if (selectedFile != null) {
            lastFCOpenTrajectoryFileALS = selectedFile;
            textFieldTrajectoryFileALS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenOutputFileALS(ActionEvent event) {

        if (lastFCOpenOutputFileALS != null) {
            fileChooserOpenOutputFileALS.setInitialDirectory(lastFCOpenOutputFileALS.getParentFile());
        }

        File selectedFile = fileChooserOpenOutputFileALS.showSaveDialog(stage);
        if (selectedFile != null) {
            lastFCOpenOutputFileALS = selectedFile;
            textFieldOutputFileALS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenOutputPathTLS(ActionEvent event) {
        
        File selectedFile;
        
        switch(comboboxModeTLS.getSelectionModel().getSelectedIndex()){
            
            case 1:
                selectedFile = directoryChooserOpenOutputPathTLS.showDialog(stage);                
                break;
                
            default:
                selectedFile = fileChooserSaveOutputFileTLS.showSaveDialog(stage);
        }
        
        if (selectedFile != null) {
            textFieldOutputPathTLS.setText(selectedFile.getAbsolutePath());
        }
        
    }
    
    private void doFilterOnScanListView(){
        
        if(items != null && listviewRxpScans != null){
            
            listviewRxpScans.getItems().clear();
                
            for(MatrixAndFile fileID : items){
                
                if(fileID.file.getAbsolutePath().contains(scanFilter) && checkboxFilter.isSelected()){
                    listviewRxpScans.getItems().add(fileID);
                }else if(!fileID.file.getAbsolutePath().contains(scanFilter) && !checkboxFilter.isSelected()){
                    listviewRxpScans.getItems().add(fileID);
                }
            }
        }
    }

    @FXML
    private void onActionButtonOpenInputFileTLS(ActionEvent event) {

        if (lastFCOpenInputFileTLS != null) {
            fileChooserOpenInputFileTLS.setInitialDirectory(lastFCOpenInputFileTLS.getParentFile());
        }

        File selectedFile = fileChooserOpenInputFileTLS.showOpenDialog(stage);
        if (selectedFile != null) {

            lastFCOpenInputFileTLS = selectedFile;
            textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());
            
            String extension = FileManager.getExtension(selectedFile);
            
            switch(extension){
                case ".rxp":
                    comboboxModeTLS.getSelectionModel().select(0);
                    break;
                case ".rsp":
                    comboboxModeTLS.getSelectionModel().select(1);
                    
                    rsp = new Rsp();
                    rsp.read(selectedFile);

                    ArrayList<Scans> rxpList = rsp.getRxpList();
                    items = new ArrayList<>();
                    
                    Integer count = 0;
                    for(Scans rxp :rxpList){
                        Map<Integer, RxpScan> scanList = rxp.getScanList();

                        for(Entry scan:scanList.entrySet()){
                            RxpScan sc = (RxpScan) scan.getValue();
                            items.add(new MatrixAndFile(new File(sc.getAbsolutePath()), MatrixConverter.convertMat4DToMatrix4d(rxp.getSopMatrix())));
                        }
                        count++;
                    }
                    
                    popMatrix = MatrixConverter.convertMat4DToMatrix4d(rsp.getPopMatrix());
                    updateResultMatrix();
                    
                    doFilterOnScanListView();
                    
                    break;
                default:
                    comboboxModeTLS.getSelectionModel().select(2);    
            }
        }
    }

    @FXML
    private void onActionButtonLoadSelectedVoxelFile(ActionEvent event) {

        String[] parameters = VoxelSpaceData.readAttributs(listViewVoxelsFiles.getSelectionModel().getSelectedItem());

        for (int i = 0; i < parameters.length; i++) {

            parameters[i] = parameters[i].replaceAll(" ", "");
            parameters[i] = parameters[i].replaceAll("#", "");
        }

        comboboxAttributeToView.getItems().clear();
        comboboxAttributeToView.getItems().addAll(parameters);

        if (parameters.length > 3) {
            comboboxAttributeToView.getSelectionModel().select(3);
        }

        buttonOpen3DView.setDisable(false);
        tabPaneMain.getSelectionModel().select(2);
    }

    @FXML
    private void onActionButtonRemoveVoxelFileFromListView(ActionEvent event) {

        ObservableList<File> selectedItems = listViewVoxelsFiles.getSelectionModel().getSelectedItems();
        listViewVoxelsFiles.getItems().removeAll(selectedItems);

    }

    @FXML
    private void onActionButtonAddVoxelFileToListView(ActionEvent event) {

        if (lastFCOpenVoxelFile != null) {
            fileChooserOpenVoxelFile.setInitialDirectory(lastFCOpenVoxelFile.getParentFile());
        }

        List<File> selectedFiles = fileChooserOpenVoxelFile.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            lastFCOpenVoxelFile = selectedFiles.get(0);
            for (File file : selectedFiles) {
                addFileToVoxelList(file);
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

    private void onActionMenuItemLoad(ActionEvent event) {

        if (lastFCOpenConfiguration != null) {
            fileChooserOpenConfiguration.setInitialDirectory(lastFCOpenConfiguration.getParentFile());
        }

        File selectedFile = fileChooserOpenConfiguration.showOpenDialog(stage);
        if (selectedFile != null) {

            lastFCOpenConfiguration = selectedFile;

            Configuration cfg = new Configuration();
            cfg.readConfiguration(selectedFile);

            VoxelParameters voxelParameters = cfg.getVoxelParameters();
            textFieldEnterXMin.setText(String.valueOf(voxelParameters.bottomCorner.x));
            textFieldEnterYMin.setText(String.valueOf(voxelParameters.bottomCorner.y));
            textFieldEnterZMin.setText(String.valueOf(voxelParameters.bottomCorner.z));

            textFieldEnterXMax.setText(String.valueOf(voxelParameters.topCorner.x));
            textFieldEnterYMax.setText(String.valueOf(voxelParameters.topCorner.y));
            textFieldEnterZMax.setText(String.valueOf(voxelParameters.topCorner.z));

            textFieldXNumber.setText(String.valueOf(voxelParameters.split.x));
            textFieldYNumber.setText(String.valueOf(voxelParameters.split.y));
            textFieldZNumber.setText(String.valueOf(voxelParameters.split.z));

            checkboxUseDTMFilter.setSelected(voxelParameters.useDTMCorrection());
            textfieldDTMPath.setText(voxelParameters.getDtmFile().getAbsolutePath());
            textfieldDTMValue.setText(String.valueOf(voxelParameters.minDTMDistance));

            checkboxUsePopMatrix.setSelected(cfg.isUsePopMatrix());
            checkboxUseSopMatrix.setSelected(cfg.isUseSopMatrix());
            checkboxUseVopMatrix.setSelected(cfg.isUseVopMatrix());
            
            if(cfg.getPopMatrix() != null){
                popMatrix = cfg.getPopMatrix();
            }
            if(cfg.getSopMatrix() != null){
                sopMatrix = cfg.getSopMatrix();
            }
            if(cfg.getVopMatrix() != null){
                vopMatrix = cfg.getVopMatrix();
            }
            

            updateResultMatrix();

            switch (cfg.getProcessMode().mode) {
                case 0:
                    textFieldInputFileALS.setText(cfg.getInputFile().getAbsolutePath());
                    textFieldTrajectoryFileALS.setText(cfg.getTrajectoryFile().getAbsolutePath());
                    textFieldOutputFileALS.setText(cfg.getOutputFile().getAbsolutePath());
                    comboboxModeALS.getSelectionModel().select(cfg.getInputType().type);
                    break;

            }

            ProcessMode processMode = cfg.getProcessMode();

            switch (processMode.mode) {
                case 0:

                    break;
            }
        }
    }


    private void onActionButtonOpenSopMatrixFile(ActionEvent event) {

        if (lastFCOpenSopMatrixFile != null) {
            fileChooserOpenSopMatrixFile.setInitialDirectory(lastFCOpenSopMatrixFile.getParentFile());
        }

        File selectedFile = fileChooserOpenSopMatrixFile.showOpenDialog(stage);
        if (selectedFile != null) {

            sopMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
            updateResultMatrix();

            lastFCOpenSopMatrixFile = selectedFile;
        }

    }

    @FXML
    private void onActionButtonOpenPopMatrixFile(ActionEvent event) {

        if (lastFCOpenPopMatrixFile != null) {
            fileChooserOpenPopMatrixFile.setInitialDirectory(lastFCOpenPopMatrixFile.getParentFile());
        }

        File selectedFile = fileChooserOpenPopMatrixFile.showOpenDialog(stage);
        if (selectedFile != null) {
            
            String extension = FileManager.getExtension(selectedFile);
            Matrix4d mat;
            
            switch(extension){
                case ".rsp":
                    
                    Rsp tempRsp = new Rsp();
                    tempRsp.read(selectedFile);
                    mat = MatrixConverter.convertMat4DToMatrix4d(tempRsp.getPopMatrix());
                    
                    break;
                default:
                    mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                    
            }
            
            if(mat != null){
                popMatrix = mat;
            }else{
                showMatrixFormatErrorDialog();
            }
             
            updateResultMatrix();

            lastFCOpenPopMatrixFile = selectedFile;
        }
    }
    
    private void showMatrixFormatErrorDialog(){
        
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Impossible to parse matrix file");
        alert.setContentText(MATRIX_FORMAT_ERROR_MSG);

        alert.showAndWait();
    }
    @FXML
    private void onActionButtonOpenVopMatrixFile(ActionEvent event) {

        if (lastFCOpenVopMatrixFile != null) {
            fileChooserOpenVopMatrixFile.setInitialDirectory(lastFCOpenVopMatrixFile.getParentFile());
        }

        File selectedFile = fileChooserOpenVopMatrixFile.showOpenDialog(stage);
        if (selectedFile != null) {
            
            Matrix4d mat = MatrixFileParser.getMatrixFromFile(selectedFile);
            if(mat != null){
                vopMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
                updateResultMatrix();
            }else{
                showMatrixFormatErrorDialog();
            }

            lastFCOpenVopMatrixFile = selectedFile;
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
        updateResultMatrix();
    }

    @FXML
    private void onActionButtonEnterReferencePointsVop(ActionEvent event) {

        calculateMatrixFrame.show();

        calculateMatrixFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                if (calculateMatrixFrameController.getMatrix() != null) {
                    vopMatrix = calculateMatrixFrameController.getMatrix();
                    updateResultMatrix();
                }
            }
        });

    }

    @FXML
    private void onActionCheckboxUseDTMFilter(ActionEvent event) {

    }

    @FXML
    private void onActionButtonOpenDTMFile(ActionEvent event) {

        if (lastFCOpenDTMFile != null) {
            fileChooserOpenDTMFile.setInitialDirectory(lastFCOpenDTMFile.getParentFile());
        }

        File selectedFile = fileChooserOpenDTMFile.showOpenDialog(stage);
        if (selectedFile != null) {
            textfieldDTMPath.setText(selectedFile.getAbsolutePath());
            lastFCOpenDTMFile = selectedFile;
        }
    }

    @FXML
    private void onActionButtonALSAddToTaskList(ActionEvent event) {

        if (lastFCSaveConfiguration != null) {
            fileChooserSaveConfiguration.setInitialDirectory(lastFCSaveConfiguration.getParentFile());
        }

        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            lastFCSaveConfiguration = selectedFile;

            VoxelParameters voxelParameters = new VoxelParameters();
            voxelParameters.setBottomCorner(new Point3d(
                    Double.valueOf(textFieldEnterXMin.getText()),
                    Double.valueOf(textFieldEnterYMin.getText()),
                    Double.valueOf(textFieldEnterZMin.getText())));

            voxelParameters.setTopCorner(new Point3d(
                    Double.valueOf(textFieldEnterXMax.getText()),
                    Double.valueOf(textFieldEnterYMax.getText()),
                    Double.valueOf(textFieldEnterZMax.getText())));

            voxelParameters.setSplit(new Point3i(
                    Integer.valueOf(textFieldXNumber.getText()),
                    Integer.valueOf(textFieldYNumber.getText()),
                    Integer.valueOf(textFieldZNumber.getText())));

            voxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
            voxelParameters.setWeightingData(VoxelParameters.DEFAULT_ALS_WEIGHTING);
            voxelParameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
            voxelParameters.minDTMDistance = Float.valueOf(textfieldDTMValue.getText());
            voxelParameters.setDtmFile(new File(textfieldDTMPath.getText()));
            voxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));

            InputType it;

            switch (comboboxModeALS.getSelectionModel().getSelectedIndex()) {
                case 0:
                    it = InputType.LAS_FILE;
                    break;
                case 1:
                    it = InputType.LAZ_FILE;
                    break;
                case 2:
                    it = InputType.POINTS_FILE;
                    break;
                case 3:
                    it = InputType.SHOTS_FILE;
                    break;
                default:
                    it = InputType.LAS_FILE;
            }

            Configuration cfg = new Configuration(ProcessMode.VOXELISATION_ALS, it,
                    new File(textFieldInputFileALS.getText()),
                    new File(textFieldTrajectoryFileALS.getText()),
                    new File(textFieldOutputFileALS.getText()),
                    voxelParameters,
                    checkboxUsePopMatrix.isSelected(), popMatrix,
                    checkboxUseSopMatrix.isSelected(), sopMatrix,
                    checkboxUseVopMatrix.isSelected(), vopMatrix);
            
            
            cfg.setFilters(listviewFilters.getItems());
            
            cfg.writeConfiguration(selectedFile);

            addFileToTaskList(selectedFile);
        }
    }

    private void addFileToTaskList(File file) {

        if (!listViewTaskList.getItems().contains(file)) {
            listViewTaskList.getItems().add(file);
        }
    }

    private void addFileToVoxelList(File file) {

        if (!listViewVoxelsFiles.getItems().contains(file)) {
            listViewVoxelsFiles.getItems().add(file);
            listViewVoxelsFiles.getSelectionModel().select(file);
        }
    }

    @FXML
    private void onActionButtonExecute(ActionEvent event) {

        List<File> tasks = listViewTaskList.getItems();
        queue = new ArrayBlockingQueue<>(tasks.size());
        queue.addAll(tasks);
        taskNumber = tasks.size();
        
        try {
            if(!queue.isEmpty()){
                executeProcess(queue.take());
            }
            
        } catch (InterruptedException ex) {
            logger.error(ex);
        }
    }

    private void executeProcess(final File file) {

        final Configuration cfg = new Configuration();
        cfg.readConfiguration(file);

        final long start_time = System.currentTimeMillis();
        ProgressDialog d;
        Service<Void> service;

        ProcessMode processMode = cfg.getProcessMode();

        service = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {
                        
                        VoxelisationTool voxTool;
                        
                        final String msgTask = "Task "+taskID+"/"+taskNumber+" :"+file.getAbsolutePath();
                        updateMessage(msgTask);
                        
                        switch (processMode) {

                            case VOXELISATION_ALS:

                                voxTool = new VoxelisationTool();
                                voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                    @Override
                                    public void voxelisationProgress(String progress, int ratio) {
                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                updateMessage(msgTask+"\n"+progress);
                                            }
                                        });
                                        
                                    }

                                    @Override
                                    public void voxelisationFinished(float duration) {

                                        logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                    }
                                });

                                voxTool.generateVoxelsFromLas(cfg.getOutputFile(), cfg.getInputFile(), cfg.getTrajectoryFile(), cfg.getVoxelParameters(), MatrixConverter.convertMatrix4dToMat4D(cfg.getVopMatrix()), cfg.getFilters());

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {

                                        addFileToVoxelList(cfg.getOutputFile());
                                    }
                                });

                                break;

                            case VOXELISATION_TLS:

                                voxTool = new VoxelisationTool();
                                voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                    @Override
                                    public void voxelisationProgress(String progress, int ratio) {
                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                updateMessage(msgTask+"\n"+progress);
                                            }
                                        });
                                    }

                                    @Override
                                    public void voxelisationFinished(float duration) {

                                        logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                    }
                                });
                                
                                switch(cfg.getInputType()){
                                    
                                    case RSP_PROJECT:
                                        
                                        ArrayList<File> outputFiles = voxTool.generateVoxelsFromRsp(cfg.getOutputFile(), cfg.getInputFile(), cfg.getVoxelParameters(),
                                        MatrixConverter.convertMatrix4dToMat4D(cfg.getVopMatrix()),
                                        MatrixConverter.convertMatrix4dToMat4D(cfg.getPopMatrix()), cfg.getMatricesAndFiles(), cfg.getFilters());
                                
                                        if(cfg.getVoxelParameters().isMergingAfter()){
                                            voxTool = new VoxelisationTool();

                                            voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                                @Override
                                                public void voxelisationProgress(String progress, int ratio) {
                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {

                                                            updateMessage(msgTask+"\n"+progress);
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void voxelisationFinished(float duration) {

                                                    logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                                }
                                            });

                                            voxTool.mergeVoxelsFile(outputFiles, cfg.getVoxelParameters().getMergedFile());
                                        }

                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                for (File file : outputFiles) {
                                                    addFileToVoxelList(file);
                                                }
                                                if(cfg.getVoxelParameters().isMergingAfter()){
                                                    addFileToVoxelList(cfg.getVoxelParameters().getMergedFile());
                                                }
                                            }
                                        });
                                
                                        break;
                                        
                                    case RXP_SCAN:
                                        
                                        voxTool.generateVoxelsFromRxp(cfg.getOutputFile(), cfg.getInputFile(), 
                                                cfg.getVoxelParameters().getDtmFile(),
                                                cfg.getVoxelParameters(),
                                                MatrixConverter.convertMatrix4dToMat4D(cfg.getVopMatrix()),
                                                MatrixConverter.convertMatrix4dToMat4D(cfg.getPopMatrix()),
                                                MatrixConverter.convertMatrix4dToMat4D(cfg.getSopMatrix()), 
                                                cfg.getFilters());
                                        
                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                
                                                addFileToVoxelList(cfg.getOutputFile());
                                            }
                                        });
                                        
                                        break;
                                }
                                

                                break;

                            case MULTI_RES:

                                ProcessingMultiRes process = new ProcessingMultiRes();
                                process.process(cfg.getOutputFile(), cfg.getFiles());

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        addFileToVoxelList(cfg.getOutputFile());
                                    }
                                });

                                break;

                            case MERGING:

                                voxTool = new VoxelisationTool();
                                voxTool.mergeVoxelsFile(cfg.getFiles(), cfg.getOutputFile());

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        addFileToVoxelList(cfg.getOutputFile());
                                        setOnSucceeded(null);
                                    }
                                });

                                break;
                        }

                        return null;
                    }
                };
            }
        };

        d = new ProgressDialog(service);
        d.setResizable(true);
        d.show();
        
        service.stateProperty().addListener(new ChangeListener<Worker.State>() {

            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if(newValue == Worker.State.SUCCEEDED){
                    if (!queue.isEmpty()) {
                        try {
                            if (!queue.isEmpty()) {
                                taskID++;
                                executeProcess(queue.take());
                            }else{
                                taskID = 1;
                            }

                        } catch (InterruptedException ex) {
                            logger.error(ex);
                        }
                    }
                }
            }
        });

        service.start();
        
        
    }

    @FXML
    private void onActionButtonTLSAddToTaskList(ActionEvent event) {
        
        if (lastFCSaveConfiguration != null) {
            fileChooserSaveConfiguration.setInitialDirectory(lastFCSaveConfiguration.getParentFile());
        }

        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            lastFCSaveConfiguration = selectedFile;

            VoxelParameters voxelParameters = new VoxelParameters();
            voxelParameters.setBottomCorner(new Point3d(
                    Double.valueOf(textFieldEnterXMin.getText()),
                    Double.valueOf(textFieldEnterYMin.getText()),
                    Double.valueOf(textFieldEnterZMin.getText())));

            voxelParameters.setTopCorner(new Point3d(
                    Double.valueOf(textFieldEnterXMax.getText()),
                    Double.valueOf(textFieldEnterYMax.getText()),
                    Double.valueOf(textFieldEnterZMax.getText())));

            voxelParameters.setSplit(new Point3i(
                    Integer.valueOf(textFieldXNumber.getText()),
                    Integer.valueOf(textFieldYNumber.getText()),
                    Integer.valueOf(textFieldZNumber.getText())));
            
            if(checkboxEnableWeighting.isSelected()){
                voxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
                voxelParameters.setWeightingData(VoxelParameters.DEFAULT_TLS_WEIGHTING);
            }else{
                voxelParameters.setWeighting(0);
            }
            
            voxelParameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
            if(checkboxUseDTMFilter.isSelected()){
                voxelParameters.minDTMDistance = Float.valueOf(textfieldDTMValue.getText());
                voxelParameters.setDtmFile(new File(textfieldDTMPath.getText()));
            }
            
            voxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
            
            voxelParameters.setMergingAfter(checkboxMergeAfter.isSelected());
            voxelParameters.setMergedFile(new File(textFieldOutputPathTLS.getText(), textFieldMergedFileName.getText()));

            InputType it;

            switch (comboboxModeTLS.getSelectionModel().getSelectedIndex()) {
                case 1:
                    it = InputType.RSP_PROJECT;
                    
                    break;
                case 0:
                    it = InputType.RXP_SCAN;
                    break;
                case 2:
                    it = InputType.POINTS_FILE;
                    break;
                case 3:
                    it = InputType.SHOTS_FILE;
                    break;
                default:
                    it = InputType.RSP_PROJECT;
            }

            Configuration cfg = new Configuration(ProcessMode.VOXELISATION_TLS, it,
                    new File(textFieldInputFileTLS.getText()),
                    new File(textFieldTrajectoryFileALS.getText()),
                    new File(textFieldOutputPathTLS.getText()),
                    voxelParameters,
                    checkboxUsePopMatrix.isSelected(), popMatrix,
                    checkboxUseSopMatrix.isSelected(), sopMatrix,
                    checkboxUseVopMatrix.isSelected(), vopMatrix);
            
            if(it == InputType.RSP_PROJECT){
                List<MatrixAndFile> items = listviewRxpScans.getItems();
                
                cfg.setMatricesAndFiles(items);
            }
            
            cfg.setFilters(listviewFilters.getItems());

            cfg.writeConfiguration(selectedFile);

            addFileToTaskList(selectedFile);
        }
    }

    @FXML
    private void onActionButtonAddVoxelFileToMultiResListView(ActionEvent event) {

        if (lastFCOpenMultiResVoxelFile != null) {
            fileChooserOpenMultiResVoxelFile.setInitialDirectory(lastFCOpenMultiResVoxelFile.getParentFile());
        }

        List<File> selectedFiles = fileChooserOpenMultiResVoxelFile.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            listViewMultiResVoxelFiles.getItems().addAll(selectedFiles);
            lastFCOpenMultiResVoxelFile = selectedFiles.get(0);
        }
    }

    @FXML
    private void onActionMenuItemSelectionAllMultiRes(ActionEvent event) {
        listViewMultiResVoxelFiles.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemSelectionNoneMultiRes(ActionEvent event) {
        listViewMultiResVoxelFiles.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemoveVoxelFileFromMultiResListView(ActionEvent event) {

        ObservableList<File> selectedItems = listViewMultiResVoxelFiles.getSelectionModel().getSelectedItems();
        listViewMultiResVoxelFiles.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionButtonMultiResAddToTaskList(ActionEvent event) {

        if (lastFCSaveConfiguration != null) {
            fileChooserSaveConfiguration.setInitialDirectory(lastFCSaveConfiguration.getParentFile());
        }

        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            lastFCSaveConfiguration = selectedFile;

            Configuration cfg = new Configuration();
            cfg.setProcessMode(ProcessMode.MULTI_RES);
            cfg.setOutputFile(new File(textFieldOutputFileMultiRes.getText()));
            cfg.setFiles(listViewMultiResVoxelFiles.getItems());

            cfg.writeConfiguration(selectedFile);

            addFileToTaskList(selectedFile);
        }
    }

    @FXML
    private void onActionButtonOpenOutputFileMultiRes(ActionEvent event) {

        if (lastFCSaveOutputFileMultiRes != null) {
            fileChooserOpenOutputFileMultiRes.setInitialDirectory(lastFCSaveOutputFileMultiRes.getParentFile());
        }

        File selectedFile = fileChooserOpenOutputFileMultiRes.showSaveDialog(stage);

        if (selectedFile != null) {

            lastFCSaveOutputFileMultiRes = selectedFile;

            textFieldOutputFileMultiRes.setText(selectedFile.getAbsolutePath());
        }

    }

    @FXML
    private void onActionButtonAddTaskToListView(ActionEvent event) {

        if (lastFCAddTask != null) {
            fileChooserAddTask.setInitialDirectory(lastFCAddTask.getParentFile());
        }

        List<File> selectedFiles = fileChooserAddTask.showOpenMultipleDialog(stage);

        if (selectedFiles != null) {

            lastFCAddTask = selectedFiles.get(0).getParentFile();
            listViewTaskList.getItems().addAll(selectedFiles);
        }
    }

    @FXML
    private void onActionButtonLoadSelectedTask(ActionEvent event) {

        File selectedFile = listViewTaskList.getSelectionModel().getSelectedItem();

        if (selectedFile != null) {

            Configuration cfg = new Configuration();
            cfg.readConfiguration(selectedFile);

            

            switch (cfg.getProcessMode()) {
                case VOXELISATION_ALS:
                case VOXELISATION_TLS:
                    
                    
                    VoxelParameters voxelParameters = cfg.getVoxelParameters();
                    textFieldEnterXMin.setText(String.valueOf(voxelParameters.bottomCorner.x));
                    textFieldEnterYMin.setText(String.valueOf(voxelParameters.bottomCorner.y));
                    textFieldEnterZMin.setText(String.valueOf(voxelParameters.bottomCorner.z));

                    textFieldEnterXMax.setText(String.valueOf(voxelParameters.topCorner.x));
                    textFieldEnterYMax.setText(String.valueOf(voxelParameters.topCorner.y));
                    textFieldEnterZMax.setText(String.valueOf(voxelParameters.topCorner.z));

                    textFieldXNumber.setText(String.valueOf(voxelParameters.split.x));
                    textFieldYNumber.setText(String.valueOf(voxelParameters.split.y));
                    textFieldZNumber.setText(String.valueOf(voxelParameters.split.z));

                    checkboxUseDTMFilter.setSelected(voxelParameters.useDTMCorrection());
                    File tmpFile = voxelParameters.getDtmFile();
                    if(tmpFile != null){
                        textfieldDTMPath.setText(tmpFile.getAbsolutePath());
                        textfieldDTMValue.setText(String.valueOf(voxelParameters.minDTMDistance));
                    }

                    checkboxUsePopMatrix.setSelected(cfg.isUsePopMatrix());
                    checkboxUseSopMatrix.setSelected(cfg.isUseSopMatrix());
                    checkboxUseVopMatrix.setSelected(cfg.isUseVopMatrix());

                    popMatrix = cfg.getPopMatrix();
                    sopMatrix = cfg.getSopMatrix();
                    vopMatrix = cfg.getVopMatrix();

                    updateResultMatrix();
                    
                    List<Filter> filters = cfg.getFilters();
                    if(filters != null){
                        listviewFilters.getItems().clear();
                        listviewFilters.getItems().addAll(filters);
                    }
                    
                    switch (cfg.getProcessMode()) {
                        
                        case VOXELISATION_ALS:
                            tabPaneVoxelisation.getSelectionModel().select(0);
                            
                            textFieldInputFileALS.setText(cfg.getInputFile().getAbsolutePath());
                            textFieldTrajectoryFileALS.setText(cfg.getTrajectoryFile().getAbsolutePath());
                            textFieldOutputFileALS.setText(cfg.getOutputFile().getAbsolutePath());
                            
                            switch(cfg.getInputType()){
                                case LAS_FILE:
                                    comboboxModeALS.getSelectionModel().select(0);
                                    break;
                                case LAZ_FILE:
                                    comboboxModeALS.getSelectionModel().select(1);
                                    break;
                                case POINTS_FILE:
                                    comboboxModeALS.getSelectionModel().select(2);
                                    break;
                                case SHOTS_FILE:
                                    comboboxModeALS.getSelectionModel().select(3);
                                    break;
                            }
                            
                            break;
                        case VOXELISATION_TLS:
                            
                            tabPaneVoxelisation.getSelectionModel().select(1);
                            
                            textFieldInputFileTLS.setText(cfg.getInputFile().getAbsolutePath());
                            textFieldOutputPathTLS.setText(cfg.getOutputFile().getAbsolutePath());
                            
                            switch(cfg.getInputType()){
                                case RSP_PROJECT:
                                    comboboxModeTLS.getSelectionModel().select(1);
                                    break;
                                case RXP_SCAN:
                                    comboboxModeTLS.getSelectionModel().select(0);
                                    break;
                                case POINTS_FILE:
                                    comboboxModeTLS.getSelectionModel().select(2);
                                    break;
                                case SHOTS_FILE:
                                    comboboxModeTLS.getSelectionModel().select(3);
                                    break;
                            }
                            
                            checkboxMergeAfter.setSelected(cfg.getVoxelParameters().isMergingAfter());
                            
                            if(cfg.getVoxelParameters().getMergedFile() != null){
                                textFieldMergedFileName.setText(cfg.getVoxelParameters().getMergedFile().getName());
                            }
                            
                            List<MatrixAndFile> matricesAndFiles = cfg.getMatricesAndFiles();
                            if(matricesAndFiles != null){
                                items = matricesAndFiles;
                                doFilterOnScanListView();
                            }
                            break;
                    }
                    
                    
                    if(cfg.getVoxelParameters().getWeighting() == 0){
                        checkboxEnableWeighting.setSelected(false);
                    }else{
                        checkboxEnableWeighting.setSelected(true);
                        comboboxWeighting.getSelectionModel().select(cfg.getVoxelParameters().getWeighting()-1);
                    }
                    
                    
                    
                    break;
                    
                case MULTI_RES:
                    
                    tabPaneVoxelisation.getSelectionModel().select(2);
                    listViewMultiResVoxelFiles.getItems().addAll(cfg.getFiles());
                    textFieldOutputFileMultiRes.setText(cfg.getOutputFile().getAbsolutePath());
                    
                    break;
                    
                case MERGING:
                    
                    tabPaneVoxelisation.getSelectionModel().select(3);
                    
                    List<File> files = cfg.getFiles();
                    
                    if(files != null){
                        listViewVoxelsFiles.getItems().addAll(files);
                    }
                    textFieldOutputFileMerging.setText(cfg.getOutputFile().getAbsolutePath());
                    
                    break;

            }
        }

    }

    @FXML
    private void onActionButtonRemoveTaskFromListView(ActionEvent event) {
        
        ObservableList<File> selectedItems = listViewTaskList.getSelectionModel().getSelectedItems();
        listViewTaskList.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionMenuItemTaskSelectionAll(ActionEvent event) {
        listViewTaskList.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemTaskSelectionNone(ActionEvent event) {
        listViewTaskList.getSelectionModel().clearSelection();
        
        
    }

    @FXML
    private void onActionMenuItemExportDart(ActionEvent event) {
        
        if(lastFCSaveDartFile != null){
            fileChooserSaveDartFile.setInitialDirectory(lastFCSaveDartFile.getParentFile());
        }
        
        File selectedFile = fileChooserSaveDartFile.showSaveDialog(stage);
        
        if(selectedFile != null){
            lastFCSaveDartFile = selectedFile;
            
            final VoxelSpace voxelSpace = new VoxelSpace();
            voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {
                @Override
                public void voxelSpaceCreationFinished() {
                    DartWriter.writeFromVoxelSpace(voxelSpace.data, selectedFile);
                }
            });
            
            voxelSpace.loadFromFile(listViewVoxelsFiles.getSelectionModel().getSelectedItem());
        }
    }

    @FXML
    private void onActionButtonMergingAddToTaskList(ActionEvent event) {
        
        if (lastFCSaveConfiguration != null) {
            fileChooserSaveConfiguration.setInitialDirectory(lastFCSaveConfiguration.getParentFile());
        }

        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            lastFCSaveConfiguration = selectedFile;


            Configuration cfg = new Configuration();
            cfg.setProcessMode(ProcessMode.MERGING);
            
            cfg.setOutputFile(new File(textFieldOutputFileMerging.getText()));
            cfg.setFiles(listViewVoxelsFiles.getSelectionModel().getSelectedItems());

            cfg.writeConfiguration(selectedFile);

            addFileToTaskList(selectedFile);
        }
    }

    @FXML
    private void onActionButtonOpenOutputFileMerging(ActionEvent event) {
        
        if(lastFCSaveMergingFile != null){
            fileChooserOpenOutputFileMerging.setInitialDirectory(lastFCSaveMergingFile.getParentFile());
        }
        File selectedFile = fileChooserOpenOutputFileMerging.showSaveDialog(stage);
        
        if(selectedFile != null){
            lastFCSaveMergingFile = selectedFile;
            textFieldOutputFileMerging.setText(selectedFile.getAbsolutePath());
        }
        
    }

    @FXML
    private void onActionButtonAddFilter(ActionEvent event) {
        
        filterFrame.show();

        filterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                if (filterFrameController.getFilter() != null) {
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

    @FXML
    private void onActionButtonOpenPonderationFile(ActionEvent event) {
        
        if(lastFCOpenPonderationFile != null){
            fileChooserOpenPonderationFile.setInitialDirectory(lastFCOpenPonderationFile.getParentFile());
        }
        
        File selectedFile = fileChooserOpenPonderationFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            lastFCOpenPonderationFile = selectedFile;
            
            float[][] ponderationMatrix = MatrixFileParser.getPonderationMatrixFromFile(selectedFile);
            System.out.println("test");
        }
    }

}
