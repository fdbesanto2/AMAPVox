/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.gui;

import com.jogamp.newt.event.WindowAdapter;
import fr.ird.voxelidar.configuration.Configuration;
import fr.ird.voxelidar.configuration.Configuration.InputType;
import fr.ird.voxelidar.configuration.Configuration.ProcessMode;
import fr.ird.voxelidar.configuration.Input;
import fr.ird.voxelidar.configuration.MatrixAndFile;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceAdapter;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceHeader;
import fr.ird.voxelidar.engine3d.renderer.JoglListenerListener;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.lidar.format.als.LasHeader;
import fr.ird.voxelidar.lidar.format.als.LasReader;
import fr.ird.voxelidar.lidar.format.als.PointDataRecordFormat0;
import fr.ird.voxelidar.lidar.format.dart.DartPlotsXMLWriter;
import fr.ird.voxelidar.lidar.format.dtm.DtmLoader;
import fr.ird.voxelidar.lidar.format.dtm.RegularDtm;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import fr.ird.voxelidar.multires.ProcessingMultiRes;
import fr.ird.voxelidar.update.Updater;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.MatrixConverter;
import fr.ird.voxelidar.util.MatrixFileParser;
import fr.ird.voxelidar.util.Settings;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.voxelisation.PointCloud;
import fr.ird.voxelidar.voxelisation.PointcloudFilter;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import fr.ird.voxelidar.voxelisation.VoxelisationTool;
import fr.ird.voxelidar.voxelisation.VoxelisationToolListener;
import fr.ird.voxelidar.voxelisation.als.LasPoint;
import fr.ird.voxelidar.voxelisation.extraction.als.LazExtraction;
import fr.ird.voxelidar.voxelisation.raytracing.BoundingBox3F;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;
import org.controlsfx.control.RangeSlider;
import org.controlsfx.dialog.ProgressDialog;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MainFrameController implements Initializable {

    @FXML
    private ComboBox<String> comboboxScript;
    @FXML
    private CheckBox checkboxRemoveLowPoint;
    @FXML
    private Slider sliderRSPCoresToUse;
    @FXML
    private TextField textFieldPadMax5m;
    @FXML
    private Label labelPadMax5m;
    @FXML
    private CheckBox checkboxOverwritePadLimit;
    @FXML
    private AnchorPane anchorpanePadLimits;
    @FXML
    private Button buttonOpenSopMatrixFile;
    @FXML
    private MenuItem menuitemClearWindow;
    @FXML
    private AnchorPane anchorpaneRoot;
    //private Button buttonOpenPointCloudFile;
    //private Label labelPointCloudPath;
    //private TextField textfieldPointCloudPath;

    @FXML
    private CheckBox checkboxUsePointcloudFilter;
    /*
     private TextField textfieldPointCloudErrorMargin;
     private Label labelPointCloudErrorMarginValue;
     private AnchorPane anchorpanePointCloudFilterParameters;
     */

    @FXML
    private Button buttonAddPointcloudFilter;

    @FXML
    private AnchorPane anchorpanePointCloudFiltering;
    @FXML
    private Button buttonGetBoundingBox;
    @FXML
    private TextField textfieldResMultiRes;
    @FXML
    private CheckBox checkboxMultiResAfter;
    @FXML
    private MenuItem menuItemUpdate;
    @FXML
    private AnchorPane anchorpanePreFiltering;
    @FXML
    private MenuItem menuItemExportDartPlots;
    @FXML
    private CheckBox checkboxRaster;
    @FXML
    private TextField textfieldRasterFilePath;
    @FXML
    private CheckBox checkboxUseTransformationMatrix;
    @FXML
    private Button buttonEnterReferencePointsTransformation;
    @FXML
    private Button buttonOpenTransformationMatrixFile;
    @FXML
    private Button buttonOpenRasterFile;
    @FXML
    private AnchorPane anchorPaneRasterParameters;
    @FXML
    private CheckBox checkboxFitRasterToVoxelSpace;

    @FXML
    private void onActionMenuItemUpdate(ActionEvent event) {
                                    
        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {

                            final Updater updater = new Updater();
                            
                            updater.update();
                            
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    Alert alert = new Alert(AlertType.INFORMATION);
                                    alert.setTitle("Information");
                                    alert.setHeaderText("About the Update");
                                    alert.setContentText("AmapVox needs to be restarted in order to apply update!");
                                    alert.show();
                                }
                            });
                            
                            return null;
                    }
                };
            }
        };

        ProgressDialog d = new ProgressDialog(service);
        d.initOwner(stage);
        d.show();

        service.start();
        
    }

    @FXML
    private void onActionMenuItemExportDartPlots(ActionEvent event) {
        
        final File voxelFile = listViewVoxelsFiles.getSelectionModel().getSelectedItem();
        
        if(checkVoxelFile(voxelFile)){
            
            fileChooserSaveDartFile.setInitialFileName("plots.xml");
            fileChooserSaveDartFile.setInitialDirectory(voxelFile.getParentFile());
            
            final File plotFile = fileChooserSaveDartFile.showSaveDialog(stage);
            
            if(plotFile != null){
                
                Alert alert = new Alert(AlertType.CONFIRMATION);
                
                alert.setTitle("Coordinate system");
                alert.setContentText("Choose your coordinate system");
                
                ButtonType buttonTypeGlobal = new ButtonType("Global");
                ButtonType buttonTypeLocal = new ButtonType("Local");

                alert.getButtonTypes().setAll(buttonTypeGlobal, buttonTypeLocal);
                
                Optional<ButtonType> result = alert.showAndWait();
                
                
                final boolean global;
                
                if(result.get() == buttonTypeGlobal){
                    global = true;
                }else if(result.get() == buttonTypeLocal){
                    global = false;
                }else{
                    return;
                }
                
                final DartPlotsXMLWriter dartPlotsXML = new DartPlotsXMLWriter();
                
                Service service = new Service() {

                    @Override
                    protected Task createTask() {
                        return new Task<Object>() {

                            @Override
                            protected Object call() throws Exception {
                                
                                
                                dartPlotsXML.writeFromVoxelFile(voxelFile, plotFile, global);
                                return null;
                            }
                        };
                    }
                };
                
                ProgressDialog progressDialog = new ProgressDialog(service);
                progressDialog.show();
                service.start();
                
                Button buttonCancel = new Button("cancel");
                progressDialog.setGraphic(buttonCancel);

                buttonCancel.setOnAction(new EventHandler<ActionEvent>() {

                    @Override
                    public void handle(ActionEvent event) {
                        service.cancel();
                        dartPlotsXML.setCancelled(true);
                    }
                });
                
            }
        }
        
        
        
    }

    @FXML
    private void onActionCheckboxUseTransformationMatrix(ActionEvent event) {
    }

    @FXML
    private void onActionButtonEnterReferencePointsTransformation(ActionEvent event) {
        
        calculateMatrixFrame.show();

        calculateMatrixFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                if (calculateMatrixFrameController.getMatrix() != null) {
                    rasterTransfMatrix = calculateMatrixFrameController.getMatrix();
                }
            }
        });
    }

    @FXML
    private void onActionButtonOpenTransformationMatrixFile(ActionEvent event) {
        
        File selectedFile = fileChooserOpenVopMatrixFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            
            Matrix4d mat = MatrixFileParser.getMatrixFromFile(selectedFile);
            if (mat != null) {
                
                rasterTransfMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
                if (rasterTransfMatrix == null) {
                    rasterTransfMatrix = new Matrix4d();
                    rasterTransfMatrix.setIdentity();
                }
                
            } else {
                showMatrixFormatErrorDialog();
            }
        }
    }

    @FXML
    private void onActionButtonOpenRasterFile(ActionEvent event) {
        
        File selectedFile = fileChooserOpenDTMFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldRasterFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    public class MinMax {

        public Point3d min;
        public Point3d max;

        public MinMax(Point3d min, Point3d max) {
            this.min = min;
            this.max = max;
        }
    }

    private final static Logger logger = Logger.getLogger(MainFrameController.class);
    private Stage stage;

    private Stage calculateMatrixFrame;
    private Stage filterFrame;

    private BlockingQueue<File> queue = new ArrayBlockingQueue<>(100);
    private int taskNumber = 0;
    private int taskID = 1;
    private boolean removeWarnings = false;

    private CalculateMatrixFrameController calculateMatrixFrameController;
    private FilterFrameController filterFrameController;

    private ImageView ivFormulaTransALSV1;
    private ImageView ivFormulaTransALSV2;
    private ImageView ivFormulaTransTLSV1;
    private ImageView ivFormulaTransTLSV2;

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
    private File lastFCOpenPointCloudFile;
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
    private FileChooser fileChooserOpenPointCloudFile;
    private FileChooser fileChooserOpenMultiResVoxelFile;
    private FileChooser fileChooserAddTask;
    private FileChooser fileChooserOpenOutputFileMultiRes;
    private FileChooser fileChooserOpenOutputFileMerging;
    private FileChooser fileChooserOpenScriptFile;
    private FileChooser fileChooserSaveDartFile;
    private FileChooser fileChooserSaveOutputFileTLS;
    private FileChooser fileChooserSaveGroundEnergyOutputFile;

    private DirectoryChooser directoryChooserOpenOutputPathALS;
    private DirectoryChooser directoryChooserOpenOutputPathTLS;

    private Matrix4d popMatrix;
    private Matrix4d sopMatrix;
    private Matrix4d vopMatrix;
    private Matrix4d resultMatrix;
    
    private Matrix4d rasterTransfMatrix;

    private String scanFilter;
    private boolean filterScan;
    private List<MatrixAndFile> items;
    private Rsp rsp;
    private double currentLastPointCloudLayoutY;
    
    static double SCREEN_WIDTH;
    static double SCREEN_HEIGHT;

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
    @FXML
    private ComboBox<ImageView> comboboxFormulaTransmittance;
    @FXML
    private Button buttonAutomatic;
    @FXML
    private Button buttonResetToIdentity;
    @FXML
    private TextField textFieldPadMax1m;
    @FXML
    private TextField textFieldPadMax2m;
    @FXML
    private TextField textFieldPadMax4m;
    @FXML
    private TextField textFieldPadMax3m;
    @FXML
    private Label labelPadMax1m;
    @FXML
    private Label labelPadMax2m;
    @FXML
    private Label labelPadMax4m;
    @FXML
    private Label labelPadMax3m;
    @FXML
    private Button buttonResetPadLimitsToDefault;
    @FXML
    private TextField textFieldOutputFileGroundEnergy;
    @FXML
    private Label labelOutputFileGroundEnergy;
    @FXML
    private Button buttonOpenOutputFileGroundEnergy;
    @FXML
    private CheckBox checkboxCalculateGroundEnergy;
    @FXML
    private ComboBox<String> comboboxGroundEnergyOutputFormat;
    @FXML
    private AnchorPane anchorPaneGroundEnergyParameters;
    @FXML
    private Button buttonOpenScriptFile;
    @FXML
    private Button buttonExecuteScript;
    @FXML
    private TextField textFieldScriptFile;

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
        
        directoryChooserOpenOutputPathALS = new DirectoryChooser();
        directoryChooserOpenOutputPathALS.setTitle("Choose output path");

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

        fileChooserOpenPointCloudFile = new FileChooser();
        fileChooserOpenPointCloudFile.setTitle("Choose point cloud file");
        fileChooserOpenPointCloudFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("TXT Files", "*.txt"));

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

        fileChooserSaveDartFile = new FileChooser();
        fileChooserSaveDartFile.setTitle("Save dart file (.maket)");
        fileChooserSaveDartFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Maket File", "*.maket"));

        fileChooserOpenOutputFileMerging = new FileChooser();
        fileChooserOpenOutputFileMerging.setTitle("Choose voxel file");
        fileChooserOpenOutputFileMerging.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Voxel Files", "*.vox"));

        fileChooserOpenScriptFile = new FileChooser();
        fileChooserOpenScriptFile.setTitle("Choose script file");

        fileChooserSaveGroundEnergyOutputFile = new FileChooser();
        fileChooserSaveGroundEnergyOutputFile.setTitle("Save ground energy file");

        comboboxModeALS.getItems().addAll("Las file", "Laz file", "Points file (unavailable)", "Shots file (unavailable)");
        comboboxModeTLS.getItems().addAll("Rxp scan", "Rsp project", "Points file (unavailable)", "Shots file (unavailable)");
        comboboxWeighting.getItems().addAll("From the echo number", "From a matrix file", "Local recalculation (unavailable)");
        comboboxGroundEnergyOutputFormat.getItems().addAll("txt", "png");

        ClassLoader classLoader = this.getClass().getClassLoader();

        ivFormulaTransALSV1 = new ImageView(classLoader.getResource("icons/formula_transmittance_als_1_v2.png").toString());
        ivFormulaTransALSV2 = new ImageView(classLoader.getResource("icons/formula_transmittance_als_2_v2.png").toString());
        ivFormulaTransTLSV1 = new ImageView(classLoader.getResource("icons/formula_transmittance_tls_1_v2.png").toString());
        ivFormulaTransTLSV2 = new ImageView(classLoader.getResource("icons/formula_transmittance_tls_2_v2.png").toString());

        comboboxFormulaTransmittance.getItems().addListener(new ListChangeListener<ImageView>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends ImageView> c) {
                if (c.getList().size() > 0) {
                    comboboxFormulaTransmittance.getSelectionModel().selectFirst();
                }
            }
        });

        comboboxFormulaTransmittance.getItems().addAll(ivFormulaTransALSV1, ivFormulaTransALSV2);

        comboboxFormulaTransmittance.setCellFactory(new Callback<ListView<ImageView>, ListCell<ImageView>>() {

            @Override
            public ListCell<ImageView> call(ListView<ImageView> param) {
                return new ListCell<ImageView>() {

                    private final ImageView view;

                    {
                        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                        view = new ImageView();
                    }

                    @Override
                    protected void updateItem(ImageView item, boolean empty) {
                        super.updateItem(item, empty);

                        if (item == null || empty) {
                            setGraphic(null);
                        } else {
                            view.setImage(item.getImage());
                            setGraphic(view);
                        }
                    }
                };

            }
        });

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

        resetMatrices();

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
                    int voxelNumberX = (int) Math.ceil((maxPointX - minPointX) / resolution);
                    textFieldXNumber.setText(String.valueOf(voxelNumberX));

                    double minPointY = Double.valueOf(textFieldEnterYMin.getText());
                    double maxPointY = Double.valueOf(textFieldEnterYMax.getText());
                    int voxelNumberY = (int) Math.ceil((maxPointY - minPointY) / resolution);
                    textFieldYNumber.setText(String.valueOf(voxelNumberY));

                    double minPointZ = Double.valueOf(textFieldEnterZMin.getText());
                    double maxPointZ = Double.valueOf(textFieldEnterZMax.getText());
                    int voxelNumberZ = (int) Math.ceil((maxPointZ - minPointZ) / resolution);
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
                if (newValue) {
                    buttonOpenVopMatrixFile.setDisable(false);
                    buttonEnterReferencePointsVop.setDisable(false);
                } else {
                    buttonOpenVopMatrixFile.setDisable(true);
                    buttonEnterReferencePointsVop.setDisable(true);
                }
            }
        });

        checkboxUsePopMatrix.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    checkBoxUseDefaultPopMatrix.setDisable(false);
                    buttonOpenPopMatrixFile.setDisable(false);
                } else {
                    checkBoxUseDefaultPopMatrix.setDisable(true);
                    buttonOpenPopMatrixFile.setDisable(true);
                }
            }
        });

        checkboxUseSopMatrix.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    checkBoxUseDefaultSopMatrix.setDisable(false);
                    buttonOpenSopMatrixFile.setDisable(false);
                } else {
                    checkBoxUseDefaultSopMatrix.setDisable(true);
                    buttonOpenSopMatrixFile.setDisable(true);
                }
            }
        });

        checkboxCalculateGroundEnergy.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    anchorPaneGroundEnergyParameters.setDisable(false);
                } else {
                    anchorPaneGroundEnergyParameters.setDisable(true);
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

        checkboxOverwritePadLimit.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (newValue) {
                    anchorpanePadLimits.setDisable(false);
                } else {
                    anchorpanePadLimits.setDisable(true);
                }
            }
        });
        
        textfieldResMultiRes.disableProperty().bind(checkboxMultiResAfter.selectedProperty().not());

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

                switch (newValue.intValue()) {

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
                        //disableSopMatrixChoice(true);
                        labelTLSOutputPath.setText("Output file");
                }
            }
        });

        tabPaneVoxelisation.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                switch (newValue.intValue()) {

                    case 1:
                        disableSopMatrixChoice(false);
                        disablePopMatrixChoice(false);
                        break;

                    default:
                        disableSopMatrixChoice(true);
                        disablePopMatrixChoice(true);
                }

                switch (newValue.intValue()) {
                    case 0:
                        checkboxCalculateGroundEnergy.setDisable(false);

                        if (checkboxCalculateGroundEnergy.isSelected()) {
                            anchorPaneGroundEnergyParameters.setDisable(true);
                            checkboxCalculateGroundEnergy.setDisable(false);
                        }
                        checkboxRemoveLowPoint.setDisable(false);
                        break;
                    default:
                        disableSopMatrixChoice(true);
                        disablePopMatrixChoice(true);
                        anchorPaneGroundEnergyParameters.setDisable(true);
                        checkboxCalculateGroundEnergy.setDisable(true);
                        checkboxRemoveLowPoint.setDisable(true);
                }

                comboboxFormulaTransmittance.getItems().clear();

                switch (newValue.intValue()) {

                    case 0:
                    case 2:
                        comboboxFormulaTransmittance.getItems().addAll(ivFormulaTransTLSV1, ivFormulaTransTLSV2);
                        break;
                    case 1:
                    case 3:
                        comboboxFormulaTransmittance.getItems().addAll(ivFormulaTransALSV1, ivFormulaTransALSV2);
                        break;

                    default:
                }

                switch (newValue.intValue()) {

                    case 2:
                        anchorpanePadLimits.setVisible(true);
                        checkboxOverwritePadLimit.setVisible(true);
                        textFieldPADMax.setDisable(true);

                        break;
                    default:
                        anchorpanePadLimits.setVisible(false);
                        checkboxOverwritePadLimit.setVisible(false);
                        textFieldPADMax.setDisable(false);
                }
            }
        });

        comboboxWeighting.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                switch (newValue.intValue()) {

                    case 1:
                        buttonOpenPonderationFile.setVisible(true);
                        break;

                    default:
                        buttonOpenPonderationFile.setVisible(false);
                }
            }
        });

        resetPadLimits();

        int availableCores = Runtime.getRuntime().availableProcessors();

        sliderRSPCoresToUse.setMin(1);
        sliderRSPCoresToUse.setMax(availableCores);
        sliderRSPCoresToUse.setValue(availableCores);

        EventHandler<DragEvent> dragOverEvent = new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        };

        textFieldInputFileALS.setOnDragOver(dragOverEvent);
        textFieldTrajectoryFileALS.setOnDragOver(dragOverEvent);
        textFieldOutputFileALS.setOnDragOver(dragOverEvent);
        textFieldInputFileTLS.setOnDragOver(dragOverEvent);
        textFieldOutputFileMultiRes.setOnDragOver(dragOverEvent);
        textFieldOutputFileMerging.setOnDragOver(dragOverEvent);
        textfieldDTMPath.setOnDragOver(dragOverEvent);
        textFieldOutputFileGroundEnergy.setOnDragOver(dragOverEvent);
        listViewTaskList.setOnDragOver(dragOverEvent);
        listViewMultiResVoxelFiles.setOnDragOver(dragOverEvent);
        listViewVoxelsFiles.setOnDragOver(dragOverEvent);

        setDragDroppedSingleFileEvent(textFieldInputFileALS);
        setDragDroppedSingleFileEvent(textFieldTrajectoryFileALS);
        setDragDroppedSingleFileEvent(textFieldOutputFileALS);
        setDragDroppedSingleFileEvent(textFieldInputFileTLS);
        setDragDroppedSingleFileEvent(textFieldOutputFileMultiRes);
        setDragDroppedSingleFileEvent(textFieldOutputFileMerging);
        setDragDroppedSingleFileEvent(textfieldDTMPath);
        setDragDroppedSingleFileEvent(textFieldOutputFileGroundEnergy);

        listViewTaskList.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    for (File file : db.getFiles()) {
                        addFileToTaskList(file);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

        listViewVoxelsFiles.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    for (File file : db.getFiles()) {
                        addFileToVoxelList(file);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

        listViewMultiResVoxelFiles.setOnDragOver(new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        });

        listViewVoxelsFiles.setOnDragDetected(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                Dragboard db = listViewVoxelsFiles.startDragAndDrop(TransferMode.COPY);

                ClipboardContent content = new ClipboardContent();
                content.putFiles(listViewVoxelsFiles.getSelectionModel().getSelectedItems());
                db.setContent(content);

                event.consume();
            }
        });

        listViewMultiResVoxelFiles.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    listViewMultiResVoxelFiles.getItems().addAll(db.getFiles());
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });


        currentLastPointCloudLayoutY = 50;
        addPointcloudFilterComponent();

        checkboxUsePointcloudFilter.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                ObservableList<Node> list = anchorpanePointCloudFiltering.getChildren();
                for (Node n : list) {
                    if (n instanceof PointCloudFilterPaneComponent) {

                        PointCloudFilterPaneComponent panel = (PointCloudFilterPaneComponent) n;
                        panel.disableContent(!newValue);
                    }
                }

                buttonAddPointcloudFilter.setDisable(!newValue);

            }
        });

        comboboxScript.getItems().addAll("Config file generator: Multiple files ALS process",
                "Config file generator: Multiple files ALS process + multi res",
                "Multiple files ALS process + multi res");

        comboboxScript.getSelectionModel().selectFirst();
        
        checkboxRaster.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                anchorPaneRasterParameters.setDisable(!newValue);
            }
        });
        
        rasterTransfMatrix = new Matrix4d();
        rasterTransfMatrix.setIdentity();
        
        checkboxUseTransformationMatrix.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                buttonEnterReferencePointsTransformation.setDisable(!newValue);
                buttonOpenTransformationMatrixFile.setDisable(!newValue);
            }
        });
    }

    public void setStage(Stage stage) {
        
        this.stage = stage;
        
        stage.setOnShown(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                ObservableList<Screen> screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

                if(screens != null && screens.size() > 0){
                    SCREEN_WIDTH = screens.get(0).getBounds().getWidth();
                    SCREEN_HEIGHT = screens.get(0).getBounds().getHeight();
                }
            }
        });
        
        
    }

    private void resetComponents() throws Exception {

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainFrame.fxml"));
                    Parent root = loader.load();

                    Scene scene = new Scene(root);

                    scene.getStylesheets().add("/styles/Styles.css");

                    //stage.setTitle("AMAPVox");
                    stage = new Stage();
                    stage.setTitle("AMAPVox");
                    stage.setScene(scene);
                } catch (IOException ex) {
                    java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });
        /*
         textFieldInputFileALS.setText("");
         textFieldTrajectoryFileALS.setText("");
         textFieldOutputFileALS.setText("");
         textFieldOutputFileGroundEnergy.setText("");
         textfieldDTMPath.setText("");
         textFieldInputFileTLS.setText("");
         textFieldOutputPathTLS.setText("");
         textFieldMergedFileName.setText("merged.vox");
         */
    }

    private void setDragDroppedSingleFileEvent(final TextField textField) {

        textField.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1) {
                    success = true;
                    for (File file : db.getFiles()) {
                        if (file != null) {
                            textField.setText(file.getAbsolutePath());
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    private boolean checkEntryAsNumber(TextField textField) {
        
        if (!NumberUtils.isNumber(textField.getText())) {
            textField.getStyleClass().add("invalidEntry");
            return false;
        }

        textField.getStyleClass().removeAll("invalidEntry");

        return true;
    }

    private boolean checkEntryAsFile(TextField textField) {

        boolean valid = true;

        if (textField.getText().isEmpty()) {
            valid = false;
        } else {

            File f = new File(textField.getText());

            if (!Files.exists(f.toPath())) {
                valid = false;
            }
        }

        if (valid) {
            textField.getStyleClass().removeAll("invalidEntry");
        } else {
            textField.getStyleClass().add("invalidEntry");
        }

        return true;
    }

    private boolean checkEntriesAsNumber(TextField... textFields) {

        boolean valid = true;

        for (TextField tf : textFields) {
            if (!checkEntryAsNumber(tf)) {
                valid = false;
            }
        }

        return valid;
    }

    private boolean checkEntriesAsFile(TextField... textFields) {

        boolean valid = true;

        for (TextField tf : textFields) {
            if (!checkEntryAsFile(tf)) {
                valid = false;
            }
        }

        return valid;
    }

    private void resetMatrices() {

        popMatrix = new Matrix4d();
        popMatrix.setIdentity();
        sopMatrix = new Matrix4d();
        sopMatrix.setIdentity();
        vopMatrix = new Matrix4d();
        vopMatrix.setIdentity();
        resultMatrix = new Matrix4d();
        resultMatrix.setIdentity();
    }

    private void resetPadLimits() {
        textFieldPADMax.setText(String.valueOf(5));
        textFieldPadMax1m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_1M));
        textFieldPadMax2m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_2M));
        textFieldPadMax3m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_3M));
        textFieldPadMax4m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_4M));
        textFieldPadMax5m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_5M));
    }

    private void disableSopMatrixChoice(boolean value) {

        if (!value) {
            checkboxUseSopMatrix.setDisable(true);
            checkBoxUseDefaultSopMatrix.setDisable(true);
        } else {
            checkboxUseSopMatrix.setDisable(false);
            checkBoxUseDefaultSopMatrix.setDisable(false);
        }
    }

    private void disablePopMatrixChoice(boolean value) {

        if (!value) {
            checkboxUsePopMatrix.setDisable(true);
            checkBoxUseDefaultPopMatrix.setDisable(true);
            buttonOpenPopMatrixFile.setDisable(true);
        } else {
            checkboxUsePopMatrix.setDisable(false);
            checkBoxUseDefaultPopMatrix.setDisable(false);
            buttonOpenPopMatrixFile.setDisable(false);
        }
    }

    private void fillResultMatrix(Matrix4d resultMatrix) {

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

        fillResultMatrix(resultMatrix);

    }

    @FXML
    private void onActionButtonVizualize(ActionEvent event) {

        final File voxelFile = listViewVoxelsFiles.getSelectionModel().getSelectedItem();
        final String attributeToView = comboboxAttributeToView.getSelectionModel().getSelectedItem();
        
        final Stage toolBarFrameStage = new Stage();
        final boolean drawDTM = checkboxRaster.isSelected();
        final File dtmFile = new File(textfieldRasterFilePath.getText());
        final Matrix4d dtmTransfMatrix = rasterTransfMatrix;
        final boolean fitDTMToVoxelSpace = checkboxFitRasterToVoxelSpace.isSelected();
        
        Service s = new Service() {

            @Override
            protected Task createTask() {
                return new Task() {

                    @Override
                    protected Object call() throws Exception {
                        
                        
                        final VoxelSpace voxelSpace = new VoxelSpace(voxelFile);

                        Settings settings = new Settings(false, true, true, true, attributeToView);

                        voxelSpace.setSettings(settings);
                        voxelSpace.setCurrentAttribut(settings.attributeToVisualize);

                        final JOGLWindow joglWindow;
                        try {
                            joglWindow = new JOGLWindow(((int)SCREEN_WIDTH / 4), (int)SCREEN_HEIGHT / 4, (int)(SCREEN_WIDTH / 1.5d), (int)SCREEN_HEIGHT / 2,
                                    voxelFile.toString(),
                                    voxelSpace, settings);
                            
                            if(drawDTM && dtmFile != null){
                                
                                updateMessage("Reading raster file: "+dtmFile.getAbsolutePath());
                                
                                RegularDtm dtm = DtmLoader.readFromAscFile(dtmFile);
            
                                if(dtmTransfMatrix != null){
                                    dtm.setTransformationMatrix(MatrixConverter.convertMatrix4dToMat4D(dtmTransfMatrix));
                                }
                                
                                if(fitDTMToVoxelSpace){
                                    
                                    VoxelSpaceHeader header = VoxelSpaceHeader.readVoxelFileHeader(voxelFile);
                                    dtm.setLimits(new Point3F((float)header.bottomCorner.x, (float)header.bottomCorner.y, (float)header.bottomCorner.z), 
                                                  new Point3F((float)header.topCorner.x, (float)header.topCorner.y, (float)header.topCorner.z));
                                }
                                
                                updateMessage("Converting raster to mesh");
                                dtm.buildMesh();
                                
                                joglWindow.getJoglContext().getScene().setDtm(dtm);
                            }
                            
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                            return null;
                        }

                        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                            @Override
                            public void voxelSpaceCreationProgress(int progress) {
                                updateProgress(progress, 100);
                            }
                        });

                        voxelSpace.load();
                        voxelSpace.updateValue();
                        
                        final int posX = joglWindow.getPosition().getX();
                        final int posY = joglWindow.getPosition().getY();                        
                        
                        
                        
                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {

                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ToolBoxFrame.fxml"));
                                Parent root;
                                try {
                                    stage.setAlwaysOnTop(false);
                                    ToolBoxFrameController toolBarFrameController;
                                    
                                    
                                    root = loader.load();
                                    Scene scene = new Scene(root);
                                    toolBarFrameStage.setScene(scene);
                                    toolBarFrameStage.initStyle(StageStyle.UNDECORATED);
                                    toolBarFrameStage.focusedProperty().addListener(new ChangeListener<Boolean>() {

                                        @Override
                                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                            if(newValue){
                                                toolBarFrameStage.setAlwaysOnTop(true);
                                            }else{
                                                
                                                if(!joglWindow.isFocused()){
                                                    toolBarFrameStage.setAlwaysOnTop(false);
                                                }
                                            }
                                        }
                                    });

                                    toolBarFrameController = loader.getController();
                                    toolBarFrameController.setJoglListener(joglWindow.getJoglContext());
                                    
                                    
                                    toolBarFrameController.setAttributes(comboboxAttributeToView.getItems());
                                    
                                    toolBarFrameController.setStage(toolBarFrameStage);
                                    
                                    toolBarFrameStage.setX(posX);
                                    toolBarFrameStage.setY(posY);
                                    toolBarFrameStage.show();
                                    
                                    //toolBarFrameStage.setHeight(joglWindow.getHeight() / 2);
                                    joglWindow.getJoglContext().startX = (int)toolBarFrameStage.getWidth();
                                    
                                    joglWindow.getJoglContext().addListener(new JoglListenerListener() {

                                        @Override
                                        public void sceneInitialized() {
                                            joglWindow.setOnTop();
                                            toolBarFrameController.initContent();
                                            Platform.runLater(new Runnable() {

                                                @Override
                                                public void run() {
                                                    toolBarFrameStage.setAlwaysOnTop(true);
                                                }
                                            });
                                            
                                        }
                                    });
                                    
                                    joglWindow.addWindowListener(new WindowAdapter() {

                                        @Override
                                        public void windowGainedFocus(com.jogamp.newt.event.WindowEvent e) {
                                            
                                            joglWindow.setIsFocused(true);
                                            Platform.runLater(new Runnable() {

                                                @Override
                                                public void run() {
                                                    toolBarFrameStage.setIconified(false);
                                                    toolBarFrameStage.setAlwaysOnTop(true);
                                                }
                                            });
                                        }

                                        @Override
                                        public void windowLostFocus(com.jogamp.newt.event.WindowEvent e) {
                                            
                                            joglWindow.setIsFocused(false);
                                            Platform.runLater(new Runnable() {

                                                @Override
                                                public void run() {
                                                    if(!toolBarFrameStage.focusedProperty().get()){
                                                        toolBarFrameStage.setIconified(true);
                                                        toolBarFrameStage.setAlwaysOnTop(false);
                                                    }
                                                }
                                            });
                                        }
                                    });
                                    
                                    joglWindow.addWindowListener(new GLRenderWindowListener(toolBarFrameStage, joglWindow.getAnimator()));
                                    //joglWindow.setOnTop();
                                    joglWindow.show();
                                    
                                    toolBarFrameStage.setAlwaysOnTop(true);


                                } catch (IOException e) {
                                    logger.error("Loading ToolBarFrame.fxml failed", e);
                                } catch (Exception e) {
                                    logger.error("Error during toolbar init", e);
                                }
                            }
                        });

                        return null;
                    }
                };
            }
        };

        ProgressDialog d = new ProgressDialog(s);
        d.initOwner(stage);
        d.show();
        /*
        ChangeListener<Boolean> cl = new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                
                if (newValue) {
                    Platform.runLater(new Runnable() {

                        @Override
                        public void run() {
                            
                            
                        }
                    });
                    
                    stage.focusedProperty().removeListener(this);
                }
                
                

            }
        };
        stage.focusedProperty().addListener(cl);
        */
        s.start();

    }

    @FXML
    private void onActionButtonOpenInputFileALS(ActionEvent event) {

        File f = new File(textFieldInputFileALS.getText());

        if (Files.exists(f.toPath())) {
            fileChooserOpenInputFileALS.setInitialDirectory(f.getParentFile());
        } else if (lastFCOpenInputFileALS != null) {

            fileChooserOpenInputFileALS.setInitialDirectory(lastFCOpenInputFileALS.getParentFile());

        }

        List<File> selectedFiles = fileChooserOpenInputFileALS.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            
            StringBuilder sb = new StringBuilder();
            
            int count = 0;
            for(File file : selectedFiles){
                sb.append(file.getAbsolutePath());
                count++;
                if(count < selectedFiles.size()){
                    sb.append(";");
                }
            }
            textFieldInputFileALS.setText(sb.toString());
        }
    }

    @FXML
    private void onActionButtonOpenTrajectoryFileALS(ActionEvent event) {

        if (lastFCOpenTrajectoryFileALS != null) {
            fileChooserOpenTrajectoryFileALS.setInitialDirectory(lastFCOpenTrajectoryFileALS.getParentFile());
        } else {
            File f = new File(textFieldTrajectoryFileALS.getText());

            if (Files.exists(f.toPath())) {
                fileChooserOpenTrajectoryFileALS.setInitialDirectory(f.getParentFile());
            }
        }

        File selectedFile = fileChooserOpenTrajectoryFileALS.showOpenDialog(stage);
        if (selectedFile != null) {
            lastFCOpenTrajectoryFileALS = selectedFile;
            textFieldTrajectoryFileALS.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenOutputFileALS(ActionEvent event) {

        if(textFieldInputFileALS.getText().contains(";")){ // multiple files
            
            File selectedDirectory = directoryChooserOpenOutputPathTLS.showDialog(stage);
            
            if(selectedDirectory != null){
                textFieldOutputFileALS.setText(selectedDirectory.getAbsolutePath());
            }
            
        }else{
            
            if (lastFCOpenOutputFileALS != null) {
                fileChooserOpenOutputFileALS.setInitialDirectory(lastFCOpenOutputFileALS.getParentFile());
            } else {
                File f = new File(textFieldOutputFileALS.getText());

                if (Files.exists(f.toPath())) {
                    fileChooserOpenOutputFileALS.setInitialDirectory(f.getParentFile());
                    fileChooserOpenOutputFileALS.setInitialFileName(f.getName());
                }
            }

            File selectedFile = fileChooserOpenOutputFileALS.showSaveDialog(stage);
            if (selectedFile != null) {
                lastFCOpenOutputFileALS = selectedFile;
                textFieldOutputFileALS.setText(selectedFile.getAbsolutePath());
            }
        }
        
    }

    @FXML
    private void onActionButtonOpenOutputPathTLS(ActionEvent event) {

        File selectedFile;

        switch (comboboxModeTLS.getSelectionModel().getSelectedIndex()) {

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

    private void doFilterOnScanListView() {

        if (items != null && listviewRxpScans != null) {

            listviewRxpScans.getItems().clear();

            for (MatrixAndFile fileID : items) {

                if (fileID.file.getAbsolutePath().contains(scanFilter) && checkboxFilter.isSelected()) {
                    listviewRxpScans.getItems().add(fileID);
                } else if (!fileID.file.getAbsolutePath().contains(scanFilter) && !checkboxFilter.isSelected()) {
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

            switch (extension) {
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
                    for (Scans rxp : rxpList) {
                        Map<Integer, RxpScan> scanList = rxp.getScanList();

                        for (Entry scan : scanList.entrySet()) {
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
    
    private boolean checkVoxelFile(File voxelFile){
        
        boolean valid = true;
        
        if(voxelFile == null || !FileManager.readHeader(voxelFile.getAbsolutePath()).equals("VOXEL SPACE")){
            valid = false;
        }
        
        if(!valid){
            
            logger.error("File is not a voxel file: " + voxelFile.getAbsolutePath());
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("Incorrect file");
            alert.setContentText("File is corrupted or cannot be read!\n"
                    + "Do you want to keep it?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.CANCEL) {
                listViewVoxelsFiles.getItems().remove(voxelFile);
            }
        }
        
        return valid;
    }

    @FXML
    private void onActionButtonLoadSelectedVoxelFile(ActionEvent event) {

        File voxelFile = listViewVoxelsFiles.getSelectionModel().getSelectedItem();

        if (!checkVoxelFile(voxelFile)) {
            return;
        }

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

        textFieldInputFileALS.getStyleClass().remove("invalidEntry");

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

    @FXML
    private void onActionButtonOpenSopMatrixFile(ActionEvent event) {

        if (lastFCOpenSopMatrixFile != null) {
            fileChooserOpenSopMatrixFile.setInitialDirectory(lastFCOpenSopMatrixFile.getParentFile());
        }

        File selectedFile = fileChooserOpenSopMatrixFile.showOpenDialog(stage);
        if (selectedFile != null) {

            lastFCOpenSopMatrixFile = selectedFile;

            String extension = FileManager.getExtension(selectedFile);
            Matrix4d mat;

            switch (extension) {
                case ".rsp":

                    Rsp tempRsp = new Rsp();
                    tempRsp.read(selectedFile);

                    //scan unique
                    if (comboboxModeTLS.getSelectionModel().getSelectedIndex() == 0) {

                        File scanFile;
                        if (textFieldInputFileTLS.getText().equals("")) {
                            scanFile = null;
                        } else {
                            scanFile = new File(textFieldInputFileTLS.getText());
                        }

                        if (scanFile != null && Files.exists(scanFile.toPath())) {
                            RxpScan rxpScan = tempRsp.getRxpScanByName(scanFile.getName());
                            if (rxpScan != null) {
                                sopMatrix = MatrixConverter.convertMat4DToMatrix4d(rxpScan.getSopMatrix());
                            } else {
                                Alert alert = new Alert(AlertType.ERROR);
                                alert.setTitle("Error");
                                alert.setHeaderText("Cannot get sop matrix from rsp file");
                                alert.setContentText("Check rsp file!");

                                alert.showAndWait();
                            }
                        } else {
                            Alert alert = new Alert(AlertType.ERROR);
                            alert.setTitle("Error");
                            alert.setHeaderText("Cannot get sop matrix from rsp file");
                            alert.setContentText("TLS input file should be a valid rxp file!");

                            alert.showAndWait();
                        }
                    }

                    break;
                default:
                    mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                    if (mat != null) {
                        sopMatrix = mat;
                    } else {
                        showMatrixFormatErrorDialog();
                    }

            }

            updateResultMatrix();
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

            switch (extension) {
                case ".rsp":

                    Rsp tempRsp = new Rsp();
                    tempRsp.read(selectedFile);
                    mat = MatrixConverter.convertMat4DToMatrix4d(tempRsp.getPopMatrix());

                    //scan unique
                    if (comboboxModeTLS.getSelectionModel().getSelectedIndex() == 0) {

                        File scanFile = new File(textFieldInputFileTLS.getText());

                        if (Files.exists(scanFile.toPath())) {
                            RxpScan rxpScan = tempRsp.getRxpScanByName(scanFile.getName());
                            if (rxpScan != null) {
                                sopMatrix = MatrixConverter.convertMat4DToMatrix4d(rxpScan.getSopMatrix());
                            }
                        }
                    }

                    break;
                default:
                    mat = MatrixFileParser.getMatrixFromFile(selectedFile);

            }

            if (mat != null) {
                popMatrix = mat;
            } else {
                showMatrixFormatErrorDialog();
            }

            updateResultMatrix();

            lastFCOpenPopMatrixFile = selectedFile;
        }
    }

    public void showMatrixFormatErrorDialog() {

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
            if (mat != null) {
                vopMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
                if (vopMatrix == null) {
                    vopMatrix = new Matrix4d();
                    vopMatrix.setIdentity();
                }
                updateResultMatrix();
            } else {
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
/*
        boolean check1 = checkEntriesAsNumber(textFieldEnterXMin,
                textFieldEnterYMin,
                textFieldEnterZMin,
                textFieldEnterXMax,
                textFieldEnterYMax,
                textFieldEnterZMax,
                textFieldResolution);
*/
        boolean check2 = checkEntriesAsFile(textFieldInputFileALS, textFieldTrajectoryFileALS, textFieldOutputFileALS);

        if (/*!check1 &&  ||*/ !check2) {

            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Check entries");
            alert.setContentText("Some parameters are not set up,\nplease fill the missing arguments");

            alert.showAndWait();

            return;
        }

        if (checkboxCalculateGroundEnergy.isSelected() && !checkboxUseDTMFilter.isSelected()) {

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setResizable(true);
            alert.setTitle("INFORMATION");
            alert.setHeaderText("Incoherence");
            alert.setContentText("Calculation of ground energy is enabled\nbut DTM filter is not!");

            alert.showAndWait();
        }

        if (lastFCSaveConfiguration != null) {
            fileChooserSaveConfiguration.setInitialDirectory(lastFCSaveConfiguration.getParentFile());
            fileChooserSaveConfiguration.setInitialFileName(lastFCSaveConfiguration.getName());
        } else {
            fileChooserSaveConfiguration.setInitialDirectory(new File(textFieldOutputFileALS.getText()).getParentFile());
            fileChooserSaveConfiguration.setInitialFileName(new File(textFieldOutputFileALS.getText()).getName() + "_cfg.xml");
        }

        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            lastFCSaveConfiguration = selectedFile;
            
            boolean correctNaNs = checkboxMultiResAfter.isSelected();

            if(textFieldInputFileALS.getText().contains(";")){ // multiple files
                

                File outputPathFile = new File(textFieldOutputFileALS.getText());
                    
                List<File> selectedFiles = new ArrayList<>();
                String[] split = textFieldInputFileALS.getText().split(";");

                for(String s : split){
                    selectedFiles.add(new File(s));
                }

                if (selectedFiles.size() > 0) {

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
                    
                    VoxelParameters commonVoxelParameters = new VoxelParameters();
                    
                    commonVoxelParameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
                    if (checkboxUseDTMFilter.isSelected()) {
                        commonVoxelParameters.minDTMDistance = Float.valueOf(textfieldDTMValue.getText());
                        commonVoxelParameters.setDtmFile(new File(textfieldDTMPath.getText()));
                    }

                    commonVoxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
                    commonVoxelParameters.setTransmittanceMode(comboboxFormulaTransmittance.getSelectionModel().getSelectedIndex());

                    commonVoxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
                    commonVoxelParameters.setWeightingData(VoxelParameters.DEFAULT_ALS_WEIGHTING);
                    commonVoxelParameters.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());
                    
                    
                    List<Float> resList = new ArrayList<>();
                    
                    if(!textfieldResMultiRes.getText().isEmpty()){
                                
                        String[] resArray = textfieldResMultiRes.getText().replaceAll(" ", "").split(",");
                        
                        for(String s : resArray){
                            try{
                                resList.add(Float.valueOf(s));
                            }catch(Exception e){}
                        }
                    }
                    
                    String extension;
                    
                    switch (comboboxGroundEnergyOutputFormat.getSelectionModel().getSelectedIndex()) {
                        case 0:
                            commonVoxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                            extension = ".txt";
                            break;
                        case 1:
                            commonVoxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_PNG);
                            extension = ".png";
                            break;
                        default:
                            commonVoxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                            extension = ".png";
                    }

                    
                    List<Input> inputList = new ArrayList<>();

                    for (File file : selectedFiles) {

                        MinMax minMax = calculateAutomaticallyMinAndMax(file);

                        VoxelParameters voxelParameters = new VoxelParameters();

                        voxelParameters.setBottomCorner(minMax.min);
                        voxelParameters.setTopCorner(minMax.max);

                        double resolution = Double.valueOf(textFieldResolution.getText());

                        int splitX = (int) Math.ceil((minMax.max.x - minMax.min.x) / resolution);
                        int splitY = (int) Math.ceil((minMax.max.y - minMax.min.y) / resolution);
                        int splitZ = (int) Math.ceil((minMax.max.z - minMax.min.z) / resolution);

                        voxelParameters.setSplit(new Point3i(splitX, splitY, splitZ));
                        voxelParameters.setResolution(resolution);
                        
                        voxelParameters.setCalculateGroundEnergy(checkboxMultiResAfter.isSelected());
                        if(commonVoxelParameters.isCalculateGroundEnergy()){
                            voxelParameters.setGroundEnergyFile(new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + extension));
                        }
                        
                        List<Input> subList = null;
                        
                        if(checkboxMultiResAfter.isSelected() ){
                            
                            subList = new ArrayList<>();
                            
                            for(Float res: resList){
                                VoxelParameters voxelParametersRes = new VoxelParameters();

                                voxelParametersRes.setBottomCorner(minMax.min);
                                voxelParametersRes.setTopCorner(minMax.max);

                                splitX = (int) Math.ceil((minMax.max.x - minMax.min.x) / res);
                                splitY = (int) Math.ceil((minMax.max.y - minMax.min.y) / res);
                                splitZ = (int) Math.ceil((minMax.max.z - minMax.min.z) / res);

                                voxelParametersRes.setSplit(new Point3i(splitX, splitY, splitZ));
                                voxelParametersRes.setResolution(res);

                                voxelParametersRes.setCalculateGroundEnergy(checkboxMultiResAfter.isSelected());
                                
                                if(commonVoxelParameters.isCalculateGroundEnergy()){
                                    voxelParametersRes.setGroundEnergyFile(new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + extension));
                                }
                                
                                subList.add(new Input(voxelParametersRes, file, new File(outputPathFile.getAbsolutePath() + "/" + file.getName() +"_res_"+ res +"m.vox"), null, null));
                            }
                        }

                        File voxFile = new File(outputPathFile.getAbsolutePath() + "/" + file.getName() +".vox");
                        inputList.add(new Input(voxelParameters, file, voxFile, subList, new File(outputPathFile.getAbsolutePath() + "/" + file.getName() +"_multires_.vox")));
                    }
                    
                    Configuration cfg = Configuration.createMultiFileVoxelisationConfiguration(it,
                            inputList,
                            new File(textFieldTrajectoryFileALS.getText()),
                            outputPathFile,
                            commonVoxelParameters,
                            checkboxUsePopMatrix.isSelected(), popMatrix,
                            checkboxUseSopMatrix.isSelected(), sopMatrix,
                            checkboxUseVopMatrix.isSelected(), vopMatrix);

                    cfg.setFilters(listviewFilters.getItems());

                    cfg.setMultiProcessInputs(inputList);
                    cfg.setCorrectNaNs(correctNaNs);
                    
                    float padMax1m = Float.valueOf(textFieldPadMax1m.getText());
                    float padMax2m = Float.valueOf(textFieldPadMax2m.getText());
                    float padMax3m = Float.valueOf(textFieldPadMax3m.getText());
                    float padMax4m = Float.valueOf(textFieldPadMax4m.getText());
                    float padMax5m = Float.valueOf(textFieldPadMax5m.getText());
                    cfg.setMultiResUseDefaultMaxPad(!checkboxOverwritePadLimit.isSelected());

                    cfg.setMultiResPadMax(new float[]{padMax1m, padMax2m, padMax3m, padMax4m, padMax5m});

                    cfg.writeConfiguration(selectedFile);
                    listViewTaskList.getItems().add(selectedFile);
                }

                removeWarnings = false;
                
            }else{
                VoxelParameters voxelParameters = getVoxelParametersFromUI();

                voxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
                voxelParameters.setWeightingData(VoxelParameters.DEFAULT_ALS_WEIGHTING);
                voxelParameters.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());

                if (checkboxCalculateGroundEnergy.isSelected() && !textFieldOutputFileGroundEnergy.getText().equals("")) {
                    voxelParameters.setGroundEnergyFile(new File(textFieldOutputFileGroundEnergy.getText()));

                    switch (comboboxGroundEnergyOutputFormat.getSelectionModel().getSelectedIndex()) {
                        case 0:
                            voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                            break;
                        case 1:
                            voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_PNG);
                            break;
                        default:
                            voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                    }

                }

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
                cfg.setRemoveLowPoint(checkboxRemoveLowPoint.isSelected());

                cfg.writeConfiguration(selectedFile);

                addFileToTaskList(selectedFile);
            }
            
        }
    }

    private void addFileToTaskList(File file) {

        if (!listViewTaskList.getItems().contains(file)) {
            listViewTaskList.getItems().add(file);
        }
    }

    private void addFileToVoxelList(File file) {

        if (!listViewVoxelsFiles.getItems().contains(file) && Files.exists(file.toPath())) {
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
        taskID = 1;

        try {
            if (!queue.isEmpty()) {
                executeProcess(queue.take());
            }

        } catch (InterruptedException ex) {
            logger.error(ex);
        }
    }

    private void executeProcess(final File file) {

        final Configuration cfg = new Configuration();

        try {
            cfg.readConfiguration(file);

        } catch (Exception e) {
            logger.error(e);
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Incorrect file");
            alert.setContentText("File is corrupted or cannot be read!\n"
                    + file.getAbsolutePath());
            alert.show();

            if (!queue.isEmpty()) {
                try {
                    taskID++;
                    executeProcess(queue.take());

                } catch (InterruptedException ex) {
                    logger.error(ex);
                }
            }

            return;
        }

        final int coreNumberToUse = (int) sliderRSPCoresToUse.getValue();

        final long start_time = System.currentTimeMillis();
        ProgressDialog d;
        final Service<Void> service;
        final VoxelisationTool voxTool = new VoxelisationTool();

        ProcessMode processMode = cfg.getProcessMode();

        service = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {

                        final String msgTask = "Task " + taskID + "/" + taskNumber + " :" + file.getAbsolutePath();
                        updateMessage(msgTask);

                        switch (processMode) {

                            case MERGING:

                                voxTool.mergeVoxelsFileV2(cfg.getFiles(), cfg.getOutputFile(), 0, cfg.getVoxelParameters().getMaxPAD());

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        addFileToVoxelList(cfg.getOutputFile());
                                        setOnSucceeded(null);
                                    }
                                });

                                break;

                            case VOXELISATION_ALS:

                                voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                    @Override
                                    public void voxelisationProgress(String progress, int ratio) {
                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                updateMessage(msgTask + "\n" + progress);
                                            }
                                        });

                                    }

                                    @Override
                                    public void voxelisationFinished(float duration) {

                                        logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                    }
                                });

                                voxTool.voxeliseFromAls(cfg.getOutputFile(), cfg.getInputFile(), cfg.getTrajectoryFile(), cfg.getVoxelParameters(), MatrixConverter.convertMatrix4dToMat4D(cfg.getVopMatrix()), cfg.getFilters(), cfg.isRemoveLowPoint());

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {

                                        addFileToVoxelList(cfg.getOutputFile());
                                    }
                                });

                                break;

                            case VOXELISATION_TLS:

                                voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                    @Override
                                    public void voxelisationProgress(String progress, int ratio) {
                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                updateMessage(msgTask + "\n" + progress);
                                            }
                                        });
                                    }

                                    @Override
                                    public void voxelisationFinished(float duration) {

                                        logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                    }
                                });

                                switch (cfg.getInputType()) {

                                    case RSP_PROJECT:

                                        try {
                                            ArrayList<File> outputFiles = voxTool.voxeliseFromRsp(cfg.getOutputFile(), cfg.getInputFile(), cfg.getVoxelParameters(),
                                                    MatrixConverter.convertMatrix4dToMat4D(cfg.getVopMatrix()),
                                                    MatrixConverter.convertMatrix4dToMat4D(cfg.getPopMatrix()),
                                                    cfg.getMatricesAndFiles(), cfg.getFilters(), coreNumberToUse);

                                            if (cfg.getVoxelParameters().isMergingAfter()) {

                                                voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                                    @Override
                                                    public void voxelisationProgress(String progress, int ratio) {
                                                        Platform.runLater(new Runnable() {

                                                            @Override
                                                            public void run() {

                                                                updateMessage(msgTask + "\n" + progress);
                                                            }
                                                        });
                                                    }

                                                    @Override
                                                    public void voxelisationFinished(float duration) {

                                                        logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                                    }
                                                });
                                                //if(!voxTool.isCancelled()){
                                                    voxTool.mergeVoxelsFileV2(outputFiles, cfg.getVoxelParameters().getMergedFile(), cfg.getVoxelParameters().getTransmittanceMode(), cfg.getVoxelParameters().getMaxPAD());
                                                //}
                                                
                                            }

                                            Platform.runLater(new Runnable() {

                                                @Override
                                                public void run() {

                                                    if(!voxTool.isCancelled()){
                                                        for (File file : outputFiles) {
                                                            addFileToVoxelList(file);
                                                        }
                                                        if (cfg.getVoxelParameters().isMergingAfter()) {
                                                            addFileToVoxelList(cfg.getVoxelParameters().getMergedFile());
                                                        }
                                                    }
                                                }
                                            });
                                            
                                        }catch (Exception e) {

                                        }

                                        break;

                                    case RXP_SCAN:

                                        voxTool.voxeliseFromRxp(cfg.getOutputFile(), cfg.getInputFile(),
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

                                ProcessingMultiRes process = new ProcessingMultiRes(cfg.getMultiResPadMax(), cfg.isMultiResUseDefaultMaxPad());

                                process.process(cfg.getFiles());
                                process.write(cfg.getOutputFile());

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {

                                        addFileToVoxelList(cfg.getOutputFile());
                                    }
                                });

                                break;
                                
                            case MULTI_VOXELISATION_ALS_AND_MULTI_RES:
                                
                                voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                    @Override
                                    public void voxelisationProgress(String progress, int ratio) {
                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                updateMessage(msgTask + "\n" + progress);
                                            }
                                        });

                                    }

                                    @Override
                                    public void voxelisationFinished(float duration) {

                                        logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                    }
                                });
                                voxTool.multiVoxelisation(cfg);
                                
                                break;

                        }

                        return null;
                    }
                };
            }
        };

        d = new ProgressDialog(service);
        d.initModality(Modality.NONE);
        d.initOwner(stage);
        d.setResizable(true);
        d.show();
        Button buttonCancel = new Button("cancel");
        d.setGraphic(buttonCancel);

        buttonCancel.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                service.cancel();
                voxTool.setCancelled(true);
            }
        });

        service.exceptionProperty().addListener(new ChangeListener<Throwable>() {

            @Override
            public void changed(ObservableValue<? extends Throwable> observable, Throwable oldValue, Throwable newValue) {
                System.out.println("test");
            }
        });

        service.stateProperty().addListener(new ChangeListener<Worker.State>() {

            @Override
            public void changed(ObservableValue<? extends Worker.State> observable, Worker.State oldValue, Worker.State newValue) {
                if (newValue == Worker.State.SUCCEEDED) {
                    if (!queue.isEmpty()) {
                        try {
                            taskID++;
                            executeProcess(queue.take());

                        } catch (InterruptedException ex) {
                            logger.error(ex);
                        }
                    }
                }
            }
        });

        service.setOnFailed(new EventHandler<WorkerStateEvent>() {

            @Override
            public void handle(WorkerStateEvent event) {
                logger.error("Service failed : ",service.getException());
            }
        });

        service.start();

    }

    private VoxelParameters getVoxelParametersFromUI() {

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

        voxelParameters.setResolution(Double.valueOf(textFieldResolution.getText()));

        voxelParameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
        if (checkboxUseDTMFilter.isSelected()) {
            voxelParameters.minDTMDistance = Float.valueOf(textfieldDTMValue.getText());
            voxelParameters.setDtmFile(new File(textfieldDTMPath.getText()));
        }

        voxelParameters.setUsePointCloudFilter(checkboxUsePointcloudFilter.isSelected());
        if (checkboxUsePointcloudFilter.isSelected()) {

            List<PointcloudFilter> pointcloudFilters = new ArrayList<>();

            ObservableList<Node> childrenUnmodifiable = anchorpanePointCloudFiltering.getChildrenUnmodifiable();
            for (Node n : childrenUnmodifiable) {
                if (n instanceof PointCloudFilterPaneComponent) {
                    PointCloudFilterPaneComponent pane = (PointCloudFilterPaneComponent) n;

                    boolean keep;

                    int index = pane.getComboboxPointCloudFilteringType().getSelectionModel().getSelectedIndex();
                    keep = index == 0;

                    pointcloudFilters.add(new PointcloudFilter(new File(pane.getTextfieldPointCloudPath().getText()),
                            Float.valueOf(pane.getTextfieldPointCloudErrorMargin().getText()),
                            keep));
                }
            }

            voxelParameters.setPointcloudFilters(pointcloudFilters);

            //voxelParameters.setPointcloudErrorMargin(Float.valueOf(textfieldPointCloudErrorMargin.getText()));
            //voxelParameters.setPointcloudFile(new File(textfieldPointCloudPath.getText()));
        }

        voxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
        voxelParameters.setTransmittanceMode(comboboxFormulaTransmittance.getSelectionModel().getSelectedIndex());

        return voxelParameters;
    }

    @FXML
    private void onActionButtonTLSAddToTaskList(ActionEvent event) {

        boolean check1 = checkEntriesAsNumber(textFieldEnterXMin,
                textFieldEnterYMin,
                textFieldEnterZMin,
                textFieldEnterXMax,
                textFieldEnterYMax,
                textFieldEnterZMax,
                textFieldResolution);

        boolean check2 = checkEntriesAsFile(textFieldInputFileTLS, textFieldOutputPathTLS);

        if (!check1 || !check2) {

            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Check entries");
            alert.setContentText("Some parameters are not set up, please fill the missing arguments");

            alert.showAndWait();

            return;
        }

        if (lastFCSaveConfiguration != null) {
            fileChooserSaveConfiguration.setInitialDirectory(lastFCSaveConfiguration.getParentFile());
            fileChooserSaveConfiguration.setInitialFileName(lastFCSaveConfiguration.getName());
        } else {
            File tempFile = new File(textFieldOutputPathTLS.getText());
            if (tempFile.isDirectory()) {
                fileChooserSaveConfiguration.setInitialDirectory(tempFile);
            } else {
                fileChooserSaveConfiguration.setInitialDirectory(tempFile.getParentFile());
            }

            fileChooserSaveConfiguration.setInitialFileName("cfg.xml");
        }

        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            lastFCSaveConfiguration = selectedFile;

            VoxelParameters voxelParameters = getVoxelParametersFromUI();

            voxelParameters.setMergingAfter(checkboxMergeAfter.isSelected());
            voxelParameters.setMergedFile(new File(textFieldOutputPathTLS.getText(), textFieldMergedFileName.getText()));

            if (checkboxEnableWeighting.isSelected()) {
                voxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
                voxelParameters.setWeightingData(VoxelParameters.DEFAULT_TLS_WEIGHTING);
            } else {
                voxelParameters.setWeighting(0);
            }

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

            if (it == InputType.RSP_PROJECT) {
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

            VoxelParameters voxParameters = new VoxelParameters();
            voxParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
            float padMax1m = Float.valueOf(textFieldPadMax1m.getText());
            float padMax2m = Float.valueOf(textFieldPadMax2m.getText());
            float padMax3m = Float.valueOf(textFieldPadMax3m.getText());
            float padMax4m = Float.valueOf(textFieldPadMax4m.getText());
            float padMax5m = Float.valueOf(textFieldPadMax5m.getText());
            cfg.setMultiResUseDefaultMaxPad(!checkboxOverwritePadLimit.isSelected());

            cfg.setMultiResPadMax(new float[]{padMax1m, padMax2m, padMax3m, padMax4m, padMax5m});
            cfg.setVoxelParameters(voxParameters);

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
            try {
                cfg.readConfiguration(selectedFile);

            } catch (Exception e) {
                logger.error(e);
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setHeaderText("Incorrect file");
                alert.setContentText("File is corrupted or cannot be read!\n"
                        + "Do you want to keep it?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.CANCEL) {
                    listViewTaskList.getItems().remove(selectedFile);
                }

                return;
            }

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

                    textFieldResolution.setText(String.valueOf(voxelParameters.resolution));

                    checkboxUseDTMFilter.setSelected(voxelParameters.useDTMCorrection());
                    File tmpFile = voxelParameters.getDtmFile();
                    if (tmpFile != null) {
                        textfieldDTMPath.setText(tmpFile.getAbsolutePath());
                        textfieldDTMValue.setText(String.valueOf(voxelParameters.minDTMDistance));
                    }

                    checkboxUsePointcloudFilter.setSelected(voxelParameters.isUsePointCloudFilter());
                    List<PointcloudFilter> pointcloudFilters = voxelParameters.getPointcloudFilters();
                    if (pointcloudFilters != null) {

                        clearPointcloudFiltersPane();

                        for (PointcloudFilter filter : pointcloudFilters) {
                            PointCloudFilterPaneComponent pane = addPointcloudFilterComponent();
                            pane.getTextfieldPointCloudPath().setText(filter.getPointcloudFile().getAbsolutePath());
                            pane.getTextfieldPointCloudErrorMargin().setText(String.valueOf(filter.getPointcloudErrorMargin()));

                            int index;
                            if (filter.isKeep()) {
                                index = 0;
                            } else {
                                index = 1;
                            }
                            pane.getComboboxPointCloudFilteringType().getSelectionModel().select(index);
                        }
                    }

                    checkboxUsePopMatrix.setSelected(cfg.isUsePopMatrix());
                    checkboxUseSopMatrix.setSelected(cfg.isUseSopMatrix());
                    checkboxUseVopMatrix.setSelected(cfg.isUseVopMatrix());
                    checkboxRemoveLowPoint.setSelected(cfg.isRemoveLowPoint());

                    textFieldPADMax.setText(String.valueOf(cfg.getVoxelParameters().getMaxPAD()));

                    popMatrix = cfg.getPopMatrix();
                    sopMatrix = cfg.getSopMatrix();
                    vopMatrix = cfg.getVopMatrix();

                    if (popMatrix == null) {
                        popMatrix = new Matrix4d();
                        popMatrix.setIdentity();
                    }

                    if (sopMatrix == null) {
                        sopMatrix = new Matrix4d();
                        sopMatrix.setIdentity();
                    }

                    if (vopMatrix == null) {
                        vopMatrix = new Matrix4d();
                        vopMatrix.setIdentity();
                    }

                    updateResultMatrix();

                    List<Filter> filters = cfg.getFilters();
                    if (filters != null) {
                        listviewFilters.getItems().clear();
                        listviewFilters.getItems().addAll(filters);
                    }

                    switch (cfg.getProcessMode()) {

                        case VOXELISATION_ALS:
                            tabPaneVoxelisation.getSelectionModel().select(0);

                            textFieldInputFileALS.setText(cfg.getInputFile().getAbsolutePath());
                            textFieldTrajectoryFileALS.setText(cfg.getTrajectoryFile().getAbsolutePath());
                            textFieldOutputFileALS.setText(cfg.getOutputFile().getAbsolutePath());

                            switch (cfg.getInputType()) {
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

                            checkboxCalculateGroundEnergy.setSelected(cfg.getVoxelParameters().isCalculateGroundEnergy());
                            if (cfg.getVoxelParameters().getGroundEnergyFile() != null) {
                                comboboxGroundEnergyOutputFormat.getSelectionModel().select(cfg.getVoxelParameters().getGroundEnergyFileFormat());
                                textFieldOutputFileGroundEnergy.setText(cfg.getVoxelParameters().getGroundEnergyFile().getAbsolutePath());
                            }

                            break;
                        case VOXELISATION_TLS:

                            tabPaneVoxelisation.getSelectionModel().select(1);

                            textFieldInputFileTLS.setText(cfg.getInputFile().getAbsolutePath());
                            textFieldOutputPathTLS.setText(cfg.getOutputFile().getAbsolutePath());

                            switch (cfg.getInputType()) {
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

                            if (cfg.getVoxelParameters().getMergedFile() != null) {
                                textFieldMergedFileName.setText(cfg.getVoxelParameters().getMergedFile().getName());
                            }

                            List<MatrixAndFile> matricesAndFiles = cfg.getMatricesAndFiles();
                            if (matricesAndFiles != null) {
                                items = matricesAndFiles;
                                doFilterOnScanListView();
                            }
                            break;
                    }

                    if (cfg.getVoxelParameters().getWeighting() == 0) {
                        checkboxEnableWeighting.setSelected(false);
                    } else {
                        checkboxEnableWeighting.setSelected(true);
                        comboboxWeighting.getSelectionModel().select(cfg.getVoxelParameters().getWeighting() - 1);
                    }

                    comboboxFormulaTransmittance.getSelectionModel().select(cfg.getVoxelParameters().getTransmittanceMode());

                    break;

                case MULTI_RES:

                    tabPaneVoxelisation.getSelectionModel().select(2);
                    listViewMultiResVoxelFiles.getItems().addAll(cfg.getFiles());
                    textFieldOutputFileMultiRes.setText(cfg.getOutputFile().getAbsolutePath());
                    checkboxOverwritePadLimit.setSelected(!cfg.isMultiResUseDefaultMaxPad());
                    break;

                case MERGING:

                    tabPaneVoxelisation.getSelectionModel().select(3);

                    List<File> files = cfg.getFiles();

                    if (files != null) {
                        listViewVoxelsFiles.getItems().addAll(files);
                    }
                    textFieldOutputFileMerging.setText(cfg.getOutputFile().getAbsolutePath());
                    textFieldPADMax.setText(String.valueOf(cfg.getVoxelParameters().getMaxPAD()));

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

        Stage exportDartFrame = new Stage();
        DartExporterFrameController controller;
        
        Parent root;
        
        File voxelFile = listViewVoxelsFiles.getSelectionModel().getSelectedItem();
        
        if(checkVoxelFile(voxelFile)){
            
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DartExporterFrame.fxml"));
                root = loader.load();
                controller = loader.getController();
                controller.setStage(exportDartFrame);
                controller.setParent(this);
                controller.setVoxelFile(voxelFile);
                exportDartFrame.setScene(new Scene(root));
            } catch (IOException ex) {
                logger.error(ex);
            }

            exportDartFrame.show();
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
            VoxelParameters voxParameters = new VoxelParameters();
            voxParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
            cfg.setVoxelParameters(voxParameters);
            cfg.writeConfiguration(selectedFile);

            addFileToTaskList(selectedFile);
        }
    }

    @FXML
    private void onActionButtonOpenOutputFileMerging(ActionEvent event) {

        if (lastFCSaveMergingFile != null) {
            fileChooserOpenOutputFileMerging.setInitialDirectory(lastFCSaveMergingFile.getParentFile());
        }
        File selectedFile = fileChooserOpenOutputFileMerging.showSaveDialog(stage);

        if (selectedFile != null) {
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

        if (lastFCOpenPonderationFile != null) {
            fileChooserOpenPonderationFile.setInitialDirectory(lastFCOpenPonderationFile.getParentFile());
        }

        File selectedFile = fileChooserOpenPonderationFile.showOpenDialog(stage);

        if (selectedFile != null) {
            lastFCOpenPonderationFile = selectedFile;

            float[][] ponderationMatrix = MatrixFileParser.getPonderationMatrixFromFile(selectedFile);
            System.out.println("test");
        }
    }

    private MinMax calculateAutomaticallyMinAndMax(File file) {

        Matrix4d identityMatrix = new Matrix4d();
        identityMatrix.setIdentity();

        ProgressDialog d;
        final Point3d minPoint = new Point3d();
        final Point3d maxPoint = new Point3d();
        if (resultMatrix.equals(identityMatrix)) {

            Point3d[] minMax = getLasMinMax(file);

            minPoint.set(minMax[0].x, minMax[0].y, minMax[0].z);
            maxPoint.set(minMax[1].x, minMax[1].y, minMax[1].z);

        } else {

            int count = 0;
            double xMin = 0, yMin = 0, zMin = 0;
            double xMax = 0, yMax = 0, zMax = 0;

            Mat4D mat = MatrixConverter.convertMatrix4dToMat4D(resultMatrix);
            LasHeader lasHeader;

            switch (FileManager.getExtension(file)) {
                case ".las":

                    LasReader lasReader = new LasReader();
                    lasReader.open(file);

                    lasHeader = lasReader.getHeader();
                    Iterator<PointDataRecordFormat0> iterator = lasReader.iterator();

                    while (iterator.hasNext()) {

                        PointDataRecordFormat0 point = iterator.next();

                        Vec4D pt = new Vec4D(((point.getX() * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                (point.getY() * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                (point.getZ() * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                1);

                        pt = Mat4D.multiply(mat, pt);

                        if (count != 0) {

                            if (pt.x < xMin) {
                                xMin = pt.x;
                            } else if (pt.x > xMax) {
                                xMax = pt.x;
                            }

                            if (pt.y < yMin) {
                                yMin = pt.y;
                            } else if (pt.y > yMax) {
                                yMax = pt.y;
                            }

                            if (pt.z < zMin) {
                                zMin = pt.z;
                            } else if (pt.z > zMax) {
                                zMax = pt.z;
                            }

                        } else {

                            xMin = pt.x;
                            yMin = pt.y;
                            zMin = pt.z;

                            xMax = pt.x;
                            yMax = pt.y;
                            zMax = pt.z;

                            count++;
                        }
                    }

                    minPoint.set(xMin, yMin, zMin);
                    maxPoint.set(xMax, yMax, zMax);

                    break;

                case ".laz":
                    LazExtraction lazReader = new LazExtraction();
                    lazReader.openLazFile(file);

                    lasHeader = lazReader.getHeader();
                    Iterator<LasPoint> it = lazReader.iterator();

                    while (it.hasNext()) {

                        LasPoint point = it.next();

                        Vec4D pt = new Vec4D(((point.x * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                (point.y * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                (point.z * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                1);

                        pt = Mat4D.multiply(mat, pt);

                        if (count != 0) {

                            if (pt.x < xMin) {
                                xMin = pt.x;
                            } else if (pt.x > xMax) {
                                xMax = pt.x;
                            }

                            if (pt.y < yMin) {
                                yMin = pt.y;
                            } else if (pt.y > yMax) {
                                yMax = pt.y;
                            }

                            if (pt.z < zMin) {
                                zMin = pt.z;
                            } else if (pt.z > zMax) {
                                zMax = pt.z;
                            }

                        } else {

                            xMin = pt.x;
                            yMin = pt.y;
                            zMin = pt.z;

                            xMax = pt.x;
                            yMax = pt.y;
                            zMax = pt.z;

                            count++;
                        }
                    }

                    minPoint.set(xMin, yMin, zMin);
                    maxPoint.set(xMax, yMax, zMax);

                    lazReader.close();

                    textFieldEnterXMin.setText(String.valueOf(minPoint.x));
                    textFieldEnterYMin.setText(String.valueOf(minPoint.y));
                    textFieldEnterZMin.setText(String.valueOf(minPoint.z));

                    textFieldEnterXMax.setText(String.valueOf(maxPoint.x));
                    textFieldEnterYMax.setText(String.valueOf(maxPoint.y));
                    textFieldEnterZMax.setText(String.valueOf(maxPoint.z));
            }
        }

        return new MinMax(minPoint, maxPoint);
    }

    @FXML
    private void onActionButtonAutomatic(ActionEvent event) {

        if (textFieldInputFileALS.getText().equals("") && !removeWarnings) {

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("An ALS file has to be open");
            alert.setContentText("An ALS file has to be open.\nTo proceed select ALS tab and choose a *.las file.");

            alert.showAndWait();

        } else {
            File file = new File(textFieldInputFileALS.getText());

            if (!Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS)) {

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("File not found");
                alert.setContentText("The file " + file.getAbsolutePath() + " cannot be found.");

                alert.showAndWait();

            } else if (FileManager.getExtension(file).equals(".las") || FileManager.getExtension(file).equals(".laz")) {

                Matrix4d identityMatrix = new Matrix4d();
                identityMatrix.setIdentity();

                ProgressDialog d;
                final Point3d minPoint = new Point3d();
                final Point3d maxPoint = new Point3d();

                Service<Void> service = new Service<Void>() {

                    @Override
                    protected Task<Void> createTask() {

                        return new Task<Void>() {
                            @Override
                            protected Void call() throws InterruptedException {

                                if (resultMatrix.equals(identityMatrix)) {

                                    Point3d[] minMax = getLasMinMax(new File(textFieldInputFileALS.getText()));

                                    minPoint.set(minMax[0].x, minMax[0].y, minMax[0].z);
                                    maxPoint.set(minMax[1].x, minMax[1].y, minMax[1].z);

                                } else {

                                    int count = 0;
                                    double xMin = 0, yMin = 0, zMin = 0;
                                    double xMax = 0, yMax = 0, zMax = 0;

                                    Mat4D mat = MatrixConverter.convertMatrix4dToMat4D(resultMatrix);
                                    LasHeader lasHeader;

                                    switch (FileManager.getExtension(file)) {
                                        case ".las":

                                            LasReader lasReader = new LasReader();
                                            lasReader.open(file);

                                            lasHeader = lasReader.getHeader();
                                            Iterator<PointDataRecordFormat0> iterator = lasReader.iterator();

                                            while (iterator.hasNext()) {

                                                PointDataRecordFormat0 point = iterator.next();

                                                Vec4D pt = new Vec4D(((point.getX() * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                                        (point.getY() * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                                        (point.getZ() * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                                        1);

                                                pt = Mat4D.multiply(mat, pt);

                                                if (count != 0) {

                                                    if (pt.x < xMin) {
                                                        xMin = pt.x;
                                                    } else if (pt.x > xMax) {
                                                        xMax = pt.x;
                                                    }

                                                    if (pt.y < yMin) {
                                                        yMin = pt.y;
                                                    } else if (pt.y > yMax) {
                                                        yMax = pt.y;
                                                    }

                                                    if (pt.z < zMin) {
                                                        zMin = pt.z;
                                                    } else if (pt.z > zMax) {
                                                        zMax = pt.z;
                                                    }

                                                } else {

                                                    xMin = pt.x;
                                                    yMin = pt.y;
                                                    zMin = pt.z;

                                                    xMax = pt.x;
                                                    yMax = pt.y;
                                                    zMax = pt.z;

                                                    count++;
                                                }
                                            }

                                            minPoint.set(xMin, yMin, zMin);
                                            maxPoint.set(xMax, yMax, zMax);

                                            break;

                                        case ".laz":
                                            LazExtraction lazReader = new LazExtraction();
                                            lazReader.openLazFile(file);

                                            lasHeader = lazReader.getHeader();
                                            Iterator<LasPoint> it = lazReader.iterator();

                                            while (it.hasNext()) {

                                                LasPoint point = it.next();

                                                Vec4D pt = new Vec4D(((point.x * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                                        (point.y * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                                        (point.z * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                                        1);

                                                pt = Mat4D.multiply(mat, pt);

                                                if (count != 0) {

                                                    if (pt.x < xMin) {
                                                        xMin = pt.x;
                                                    } else if (pt.x > xMax) {
                                                        xMax = pt.x;
                                                    }

                                                    if (pt.y < yMin) {
                                                        yMin = pt.y;
                                                    } else if (pt.y > yMax) {
                                                        yMax = pt.y;
                                                    }

                                                    if (pt.z < zMin) {
                                                        zMin = pt.z;
                                                    } else if (pt.z > zMax) {
                                                        zMax = pt.z;
                                                    }

                                                } else {

                                                    xMin = pt.x;
                                                    yMin = pt.y;
                                                    zMin = pt.z;

                                                    xMax = pt.x;
                                                    yMax = pt.y;
                                                    zMax = pt.z;

                                                    count++;
                                                }
                                            }

                                            minPoint.set(xMin, yMin, zMin);
                                            maxPoint.set(xMax, yMax, zMax);

                                            lazReader.close();

                                            break;
                                    }

                                }

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        textFieldEnterXMin.setText(String.valueOf(minPoint.x));
                                        textFieldEnterYMin.setText(String.valueOf(minPoint.y));
                                        textFieldEnterZMin.setText(String.valueOf(minPoint.z));

                                        textFieldEnterXMax.setText(String.valueOf(maxPoint.x));
                                        textFieldEnterYMax.setText(String.valueOf(maxPoint.y));
                                        textFieldEnterZMax.setText(String.valueOf(maxPoint.z));
                                    }
                                });

                                return null;
                            }
                        };

                    }
                ;
                };
                
                d = new ProgressDialog(service);
                d.initOwner(stage);
                d.setHeaderText("Please wait...");
                d.setResizable(true);

                d.show();

                service.start();

            }
        }

    }

    private Point3d[] getLasMinMax(File file) {

        if (textFieldInputFileALS.getText().equals("") && !removeWarnings) {

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setTitle("Information");
            alert.setHeaderText("An ALS file has to be open");
            alert.setContentText("An ALS file has to be open.\nTo proceed select ALS tab and choose a *.las file.");

            alert.showAndWait();

        } else {
            if (!Files.exists(file.toPath(), LinkOption.NOFOLLOW_LINKS)) {

                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setTitle("Error");
                alert.setHeaderText("File not found");
                alert.setContentText("The file " + file.getAbsolutePath() + " cannot be found.");

                alert.showAndWait();

            } else {

                LasHeader header = null;

                switch (FileManager.getExtension(file)) {
                    case ".las":
                        LasReader lasReader = new LasReader();
                        header = lasReader.readHeader(file);
                        break;

                    case ".laz":
                        LazExtraction laz = new LazExtraction();
                        laz.openLazFile(file);
                        header = laz.getHeader();
                        laz.close();
                        break;
                }

                if (header != null) {

                    double minX = header.getMinX();
                    double minY = header.getMinY();
                    double minZ = header.getMinZ();

                    double maxX = header.getMaxX();
                    double maxY = header.getMaxY();
                    double maxZ = header.getMaxZ();

                    return new Point3d[]{new Point3d(minX, minY, minZ), new Point3d(maxX, maxY, maxZ)};
                }

                return null;
            }
        }

        return null;
    }

    private void onActionButtonTransformationAutomatic(ActionEvent event) {

        Point3d[] minAndMax = getLasMinMax(new File(textFieldInputFileALS.getText()));

        if (minAndMax != null) {

            Point3d min = minAndMax[0];
            Point3d max = minAndMax[1];

            checkboxUseVopMatrix.setDisable(false);

            vopMatrix = new Matrix4d(1, 0, 0, -min.x,
                    0, 1, 0, -min.y,
                    0, 0, 1, -min.z,
                    0, 0, 0, 1);

            updateResultMatrix();
        }
    }

    @FXML
    private void onActionButtonResetToIdentity(ActionEvent event) {

        resetMatrices();
        fillResultMatrix(resultMatrix);
    }

    @FXML
    private void onActionButtonResetPadLimitsToDefault(ActionEvent event) {
        resetPadLimits();
    }

    @FXML
    private void onActionButtonOpenOutputFileGroundEnergy(ActionEvent event) {

        File f = new File(textFieldOutputFileGroundEnergy.getText());
        if (Files.exists(f.toPath())) {
            fileChooserSaveGroundEnergyOutputFile.setInitialDirectory(f.getParentFile());
            fileChooserSaveGroundEnergyOutputFile.setInitialFileName(f.getName());
        }

        File selectedFile = fileChooserSaveGroundEnergyOutputFile.showSaveDialog(stage);

        if (selectedFile != null) {
            textFieldOutputFileGroundEnergy.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonExecuteScript(ActionEvent event) {

        DirectoryChooser directoryChooserOutputPath;
        File outputPathFile;

        switch (comboboxScript.getSelectionModel().getSelectedIndex()) {

            case 0:

                removeWarnings = true;

                directoryChooserOutputPath = new DirectoryChooser();
                directoryChooserOutputPath.setTitle("Choose output path: ");

                outputPathFile = directoryChooserOutputPath.showDialog(stage);

                if (outputPathFile != null) {

                    FileChooser fileChooserChooseLasFiles = new FileChooser();
                    fileChooserChooseLasFiles.setTitle("Select ALS files: ");

                    List<File> selectedFiles = fileChooserChooseLasFiles.showOpenMultipleDialog(stage);

                    if (selectedFiles != null) {

                        for (File file : selectedFiles) {

                            MinMax minMax = calculateAutomaticallyMinAndMax(file);

                            VoxelParameters voxelParameters = new VoxelParameters();

                            voxelParameters.setBottomCorner(minMax.min);
                            voxelParameters.setTopCorner(minMax.max);

                            double resolution = Double.valueOf(textFieldResolution.getText());
                            int splitX = (int) ((minMax.max.x - minMax.min.x) / resolution);
                            int splitY = (int) ((minMax.max.y - minMax.min.y) / resolution);
                            int splitZ = (int) ((minMax.max.z - minMax.min.z) / resolution);

                            voxelParameters.setSplit(new Point3i(splitX, splitY, splitZ));
                            voxelParameters.setResolution(resolution);

                            voxelParameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
                            if (checkboxUseDTMFilter.isSelected()) {
                                voxelParameters.minDTMDistance = Float.valueOf(textfieldDTMValue.getText());
                                voxelParameters.setDtmFile(new File(textfieldDTMPath.getText()));
                            }

                            voxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
                            voxelParameters.setTransmittanceMode(comboboxFormulaTransmittance.getSelectionModel().getSelectedIndex());

                            voxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
                            voxelParameters.setWeightingData(VoxelParameters.DEFAULT_ALS_WEIGHTING);
                            voxelParameters.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());

                            if (checkboxCalculateGroundEnergy.isSelected() && !textFieldOutputFileGroundEnergy.getText().equals("")) {
                                voxelParameters.setGroundEnergyFile(new File(textFieldOutputFileGroundEnergy.getText()));

                                switch (comboboxGroundEnergyOutputFormat.getSelectionModel().getSelectedIndex()) {
                                    case 0:
                                        voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                                        break;
                                    case 1:
                                        voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_PNG);
                                        break;
                                    default:
                                        voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                                }

                            }

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
                                    file,
                                    new File(textFieldTrajectoryFileALS.getText()),
                                    new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + ".vox"),
                                    voxelParameters,
                                    checkboxUsePopMatrix.isSelected(), popMatrix,
                                    checkboxUseSopMatrix.isSelected(), sopMatrix,
                                    checkboxUseVopMatrix.isSelected(), vopMatrix);

                            cfg.setFilters(listviewFilters.getItems());
                            File configFile = new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + ".cfg");
                            cfg.writeConfiguration(configFile);
                            listViewTaskList.getItems().add(configFile);
                        }
                    }
                }

                removeWarnings = false;

                break;
            case 1:

                removeWarnings = true;

                directoryChooserOutputPath = new DirectoryChooser();
                directoryChooserOutputPath.setTitle("Choose output path: ");

                outputPathFile = directoryChooserOutputPath.showDialog(stage);

                if (outputPathFile != null) {

                    FileChooser fileChooserChooseLasFiles = new FileChooser();
                    fileChooserChooseLasFiles.setTitle("Select ALS files: ");

                    List<File> selectedFiles = fileChooserChooseLasFiles.showOpenMultipleDialog(stage);

                    if (selectedFiles != null) {

                        for (File file : selectedFiles) {

                            MinMax minMax = calculateAutomaticallyMinAndMax(file);
                            List<File> tempList = new ArrayList<>();

                            for (int i = 1; i < 5; i++) {

                                VoxelParameters voxelParameters = new VoxelParameters();

                                voxelParameters.setBottomCorner(minMax.min);
                                voxelParameters.setTopCorner(minMax.max);

                                double resolution = i;
                                int splitX = (int) ((minMax.max.x - minMax.min.x) / resolution);
                                int splitY = (int) ((minMax.max.y - minMax.min.y) / resolution);
                                int splitZ = (int) ((minMax.max.z - minMax.min.z) / resolution);

                                voxelParameters.setSplit(new Point3i(splitX, splitY, splitZ));
                                voxelParameters.setResolution(resolution);

                                voxelParameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
                                if (checkboxUseDTMFilter.isSelected()) {
                                    voxelParameters.minDTMDistance = Float.valueOf(textfieldDTMValue.getText());
                                    voxelParameters.setDtmFile(new File(textfieldDTMPath.getText()));
                                }

                                voxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
                                voxelParameters.setTransmittanceMode(comboboxFormulaTransmittance.getSelectionModel().getSelectedIndex());

                                voxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
                                voxelParameters.setWeightingData(VoxelParameters.DEFAULT_ALS_WEIGHTING);
                                voxelParameters.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());

                                if (checkboxCalculateGroundEnergy.isSelected() && !textFieldOutputFileGroundEnergy.getText().equals("")) {
                                    voxelParameters.setGroundEnergyFile(new File(textFieldOutputFileGroundEnergy.getText()));

                                    switch (comboboxGroundEnergyOutputFormat.getSelectionModel().getSelectedIndex()) {
                                        case 0:
                                            voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                                            break;
                                        case 1:
                                            voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_PNG);
                                            break;
                                        default:
                                            voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                                    }

                                }

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

                                File voxFile = new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + "_res" + i + "m.vox");
                                tempList.add(voxFile);

                                Configuration cfg = new Configuration(ProcessMode.VOXELISATION_ALS, it,
                                        file,
                                        new File(textFieldTrajectoryFileALS.getText()),
                                        voxFile,
                                        voxelParameters,
                                        checkboxUsePopMatrix.isSelected(), popMatrix,
                                        checkboxUseSopMatrix.isSelected(), sopMatrix,
                                        checkboxUseVopMatrix.isSelected(), vopMatrix);

                                cfg.setFilters(listviewFilters.getItems());
                                File configFile = new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + "_res" + i + "m.cfg");
                                cfg.writeConfiguration(configFile);
                                listViewTaskList.getItems().add(configFile);
                            }

                            File multiResConfigFile = new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + "_multi_res.cfg");

                            Configuration cfg = new Configuration();
                            cfg.setProcessMode(ProcessMode.MULTI_RES);
                            cfg.setOutputFile(new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + "_multi_res.vox"));
                            cfg.setFiles(tempList);

                            VoxelParameters voxParameters = new VoxelParameters();
                            voxParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
                            float padMax1m = Float.valueOf(textFieldPadMax1m.getText());
                            float padMax2m = Float.valueOf(textFieldPadMax2m.getText());
                            float padMax3m = Float.valueOf(textFieldPadMax3m.getText());
                            float padMax4m = Float.valueOf(textFieldPadMax4m.getText());
                            float padMax5m = Float.valueOf(textFieldPadMax5m.getText());
                            cfg.setMultiResUseDefaultMaxPad(!checkboxOverwritePadLimit.isSelected());

                            cfg.setMultiResPadMax(new float[]{padMax1m, padMax2m, padMax3m, padMax4m, padMax5m});
                            cfg.setVoxelParameters(voxParameters);

                            cfg.writeConfiguration(multiResConfigFile);

                            listViewTaskList.getItems().add(multiResConfigFile);
                        }
                    }
                }

                removeWarnings = false;

                break;
            
        }

    }

    @FXML
    private void onActionMenuitemClearWindow(ActionEvent event) {
        try {
            resetComponents();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void onActionButtonOpenPointCloudFile(ActionEvent event) {

    }

    @FXML
    private void onActionCheckboxUsePointcloudFilter(ActionEvent event) {
    }

    @FXML
    private void onActionButtonAddPointcloudFilter(ActionEvent event) {

        addPointcloudFilterComponent();
    }

    private PointCloudFilterPaneComponent addPointcloudFilterComponent() {

        PointCloudFilterPaneComponent pcfpc = new PointCloudFilterPaneComponent(anchorpanePointCloudFiltering);

        pcfpc.getButtonRemovePointCloudFilter().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                int index = anchorpanePointCloudFiltering.getChildren().indexOf(pcfpc);

                anchorpanePointCloudFiltering.getChildren().remove(index);

                ObservableList<Node> list = anchorpanePointCloudFiltering.getChildren();

                int count = 0;
                int count2 = 0;
                boolean offsetModified = false;

                for (Node n : list) {
                    if (n instanceof AnchorPane) {

                        count++;

                        AnchorPane p = (AnchorPane) n;
                        if ((count + count2) >= index) {

                            if ((count + count2) > index) {
                                double offset = p.getLayoutY() - 70;
                                p.setLayoutY(offset);
                            }

                            if (!offsetModified) {
                                currentLastPointCloudLayoutY -= 70;
                                offsetModified = true;
                            }

                        }

                    } else {
                        count2++;
                    }
                }
            }
        });

        pcfpc.getButtonOpenPointCloudFile().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                if (lastFCOpenPointCloudFile != null) {
                    fileChooserOpenPointCloudFile.setInitialDirectory(lastFCOpenPointCloudFile.getParentFile());
                }

                File selectedFile = fileChooserOpenPointCloudFile.showOpenDialog(stage);
                if (selectedFile != null) {
                    pcfpc.getTextfieldPointCloudPath().setText(selectedFile.getAbsolutePath());
                    lastFCOpenPointCloudFile = selectedFile;
                }
            }
        });

        pcfpc.disableContent(!checkboxUsePointcloudFilter.isSelected());

        pcfpc.relocate(pcfpc.getLayoutX(), currentLastPointCloudLayoutY);
        currentLastPointCloudLayoutY += 70;

        anchorpanePointCloudFiltering.getChildren().add(pcfpc);

        return pcfpc;
    }

    private void clearPointcloudFiltersPane() {

        ObservableList<Node> children = anchorpanePointCloudFiltering.getChildren();

        List<PointCloudFilterPaneComponent> tempList = new ArrayList<>();

        for (Node n : children) {
            if (n instanceof PointCloudFilterPaneComponent) {

                PointCloudFilterPaneComponent comp = (PointCloudFilterPaneComponent) n;
                tempList.add(comp);
            }
        }

        children.removeAll(tempList);

        currentLastPointCloudLayoutY = 50;

    }

    @FXML
    private void onActionButtonGetBoundingBox(ActionEvent event) {

        ObservableList<Node> children = anchorpanePointCloudFiltering.getChildren();

        List<PointCloudFilterPaneComponent> tempList = new ArrayList<>();

        for (Node n : children) {
            if (n instanceof PointCloudFilterPaneComponent) {

                PointCloudFilterPaneComponent comp = (PointCloudFilterPaneComponent) n;
                tempList.add(comp);
            }
        }

        Service<Void> service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {

                        final BoundingBox3F boundingBox = new BoundingBox3F();

                        int count = 0;

                        for (PointCloudFilterPaneComponent pane : tempList) {

                            if (pane.getComboboxPointCloudFilteringType().getSelectionModel().getSelectedIndex() == 0) {

                                File file = new File(pane.getTextfieldPointCloudPath().getText());

                                if (Files.exists(file.toPath()) && file.isFile()) {

                                    PointCloud pc = new PointCloud();
                                    pc.readFromFile(file);

                                    BoundingBox3F boundingBox2;
                                    if (count == 0) {
                                        boundingBox2 = pc.getBoundingBox();
                                        boundingBox.min = boundingBox2.min;
                                        boundingBox.max = boundingBox2.max;

                                    } else {
                                        boundingBox2 = pc.getBoundingBox();
                                        boundingBox.keepLargest(boundingBox2);
                                    }
                                    count++;
                                }
                            }
                        }

                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                Alert alert = new Alert(AlertType.CONFIRMATION);
                                alert.setTitle("Information");
                                alert.setHeaderText("Bounding box:");
                                alert.setContentText("Minimum: " + "x: " + boundingBox.min.x + " y: " + boundingBox.min.y + " z: " + boundingBox.min.z + "\n"
                                        + "Maximum: " + "x: " + boundingBox.max.x + " y: " + boundingBox.max.y + " z: " + boundingBox.max.z + "\n\n"
                                        + "Use for voxel space bounding-box?");

                                alert.initModality(Modality.NONE);
                                Optional<ButtonType> answer = alert.showAndWait();
                                if (answer.get() == ButtonType.OK) {

                                    textFieldEnterXMin.setText(String.valueOf(boundingBox.min.x));
                                    textFieldEnterYMin.setText(String.valueOf(boundingBox.min.y));
                                    textFieldEnterZMin.setText(String.valueOf(boundingBox.min.z));

                                    textFieldEnterXMax.setText(String.valueOf(boundingBox.max.x));
                                    textFieldEnterYMax.setText(String.valueOf(boundingBox.max.y));
                                    textFieldEnterZMax.setText(String.valueOf(boundingBox.max.z));
                                }
                            }
                        });

                        return null;
                    }
                };
            }
        };

        ProgressDialog d = new ProgressDialog(service);
        d.initOwner(stage);
        d.show();

        service.start();

    }

    public Stage getCalculateMatrixFrame() {
        return calculateMatrixFrame;
    }

    public CalculateMatrixFrameController getCalculateMatrixFrameController() {
        return calculateMatrixFrameController;
    }

}
