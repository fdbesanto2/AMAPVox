/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

import com.jogamp.newt.Window;
import com.jogamp.newt.event.WindowAdapter;
import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.als.las.PointDataRecordFormat.Classification;
import fr.amap.amapvox.als.las.PointDataRecordFormat2;
import fr.amap.amapvox.als.laz.LazExtraction;
import fr.amap.amapvox.chart.ChartViewer;
import fr.amap.amapvox.chart.VoxelFileChart;
import fr.amap.amapvox.chart.VoxelsToChart;
import fr.amap.amapvox.chart.VoxelsToChart.QuadratAxis;
import fr.amap.amapvox.commons.configuration.Configuration;
import fr.amap.amapvox.commons.configuration.Configuration.InputType;
import static fr.amap.amapvox.commons.configuration.Configuration.InputType.LAS_FILE;
import static fr.amap.amapvox.commons.configuration.Configuration.InputType.LAZ_FILE;
import static fr.amap.amapvox.commons.configuration.Configuration.InputType.POINTS_FILE;
import static fr.amap.amapvox.commons.configuration.Configuration.InputType.RSP_PROJECT;
import static fr.amap.amapvox.commons.configuration.Configuration.InputType.RXP_SCAN;
import static fr.amap.amapvox.commons.configuration.Configuration.InputType.SHOTS_FILE;
import fr.amap.amapvox.commons.io.file.FileManager;
import fr.amap.amapvox.math.matrix.Mat4D;
import fr.amap.amapvox.math.point.Point2F;
import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.math.vector.Vec3F;
import fr.amap.amapvox.commons.util.BoundingBox3d;
import fr.amap.amapvox.commons.util.ColorGradient;
import fr.amap.amapvox.commons.util.Filter;
import fr.amap.amapvox.commons.util.LidarScan;
import fr.amap.amapvox.commons.util.MatrixFileParser;
import fr.amap.amapvox.commons.util.MatrixUtility;
import fr.amap.amapvox.voxelisation.PointcloudFilter;
import fr.amap.amapvox.commons.util.TimeCounter;
import fr.amap.amapvox.commons.util.image.ScaleGradient;
import fr.amap.amapvox.datastructure.pointcloud.PointCloud;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.RxpScan;
import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jdart.DartPlotsXMLWriter;
import fr.amap.amapvox.jleica.ptg.PTGReader;
import fr.amap.amapvox.jleica.ptg.PTGScan;
import fr.amap.amapvox.jraster.asc.DtmLoader;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.math.geometry.BoundingBox2F;
import fr.amap.amapvox.math.geometry.BoundingBox3F;
import fr.amap.amapvox.math.vector.Vec4D;
import fr.amap.amapvox.simulation.hemi.HemiParameters;
import fr.amap.amapvox.simulation.hemi.HemiPhotoCfg;
import fr.amap.amapvox.simulation.hemi.HemiScanView;
import fr.amap.amapvox.simulation.transmittance.TransmittanceParameters;
import fr.amap.amapvox.simulation.transmittance.SimulationPeriod;
import fr.amap.amapvox.simulation.transmittance.TransmittanceCfg;
import fr.amap.amapvox.simulation.transmittance.TransmittanceSim;
import fr.amap.amapvox.simulation.transmittance.lai2xxx.Lai2xxxSim;
import fr.amap.amapvox.voxcommons.VoxelSpaceInfos;
import fr.amap.amapvox.voxelisation.DirectionalTransmittance;
import fr.amap.amapvox.voxelisation.LeafAngleDistribution;
import static fr.amap.amapvox.voxelisation.LeafAngleDistribution.Type.TWO_PARAMETER_BETA;
import static fr.amap.amapvox.voxelisation.LeafAngleDistribution.Type.ELLIPSOIDAL;
import fr.amap.amapvox.voxelisation.ProcessTool;
import fr.amap.amapvox.voxelisation.ProcessToolListener;
import fr.amap.amapvox.voxelisation.VoxelAnalysis.LaserSpecification;
import fr.amap.amapvox.voxelisation.configuration.ALSVoxCfg;
import fr.amap.amapvox.voxelisation.configuration.Input;
import fr.amap.amapvox.voxelisation.configuration.MultiResCfg;
import fr.amap.amapvox.voxelisation.configuration.MultiVoxCfg;
import fr.amap.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.amapvox.voxelisation.configuration.VoxCfg;
import fr.amap.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.amapvox.voxelisation.configuration.VoxelParameters;
import fr.amap.amapvox.voxelisation.multires.ProcessingMultiRes;
import fr.amap.amapvox.voxreader.VoxelFileReader;
import fr.amap.amapvox.voxviewer.ToolBoxFrameController;
import fr.amap.amapvox.voxviewer.Viewer3D;
import fr.amap.amapvox.voxviewer.event.BasicEvent;
import fr.amap.amapvox.voxviewer.loading.shader.SimpleShader;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.amapvox.voxviewer.object.scene.SceneObjectFactory;
import fr.amap.amapvox.voxviewer.object.scene.SimpleSceneObject;
import fr.amap.amapvox.voxviewer.object.scene.SimpleSceneObject2;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpaceAdapter;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpaceSceneObject;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.css.PseudoClass;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
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
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.dialog.ProgressDialog;
import org.jdom2.JDOMException;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MainFrameController implements Initializable {

    
    final Logger logger = LoggerFactory.getLogger(MainFrameController.class);
    
    private Stage stage;

    private Stage rspExtractorFrame;
    private Stage calculateMatrixFrame;
    private Stage filterFrame;
    private Stage transformationFrame;
    private Stage dateChooserFrame;
    private Stage viewCapsSetupFrame;
    private Stage updaterFrame;
    private Stage attributsImporterFrame;
    private Stage positionImporterFrame;
    
    private RspExtractorFrameController rspExtractorFrameController;
    private UpdaterFrameController updaterFrameController;
    private TransformationFrameController transformationFrameController;
    private DateChooserFrameController dateChooserFrameController;
    private ViewCapsSetupFrameController viewCapsSetupFrameController;
    private AttributsImporterFrameController attributsImporterFrameController;
    private TextFileParserFrameController textFileParserFrameController;
    private SceneObjectPropertiesPanelController sceneObjectPropertiesPanelController;
    private FilteringPaneComponentController filteringPaneController;
    private PositionImporterFrameController positionImporterFrameController;
    
    private BlockingQueue<File> queue = new ArrayBlockingQueue<>(100);
    private int taskNumber = 0;
    private int taskID = 1;
    private boolean removeWarnings = false;

    private CalculateMatrixFrameController calculateMatrixFrameController;
    private FilterFrameController filterFrameController;

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
    private File lastFCSaveMergingFile;
    private File lastFCOpenPonderationFile;
    private File lastFCOpenPointsPositionFile;
    private File lastFCSaveTransmittanceTextFile;
    private File lastDCSaveTransmittanceBitmapFile;
    private File lastFCOpenHemiPhotoOutputBitmapFile;
    private File lastFCOpenHemiPhotoOutputTextFile;

    private FileChooser fileChooserOpenConfiguration;
    private FileChooserContext fileChooserSaveConfiguration;
    //private FileChooser fileChooserSaveConfiguration;
    private FileChooser fileChooserOpenInputFileALS;
    private FileChooser fileChooserOpenTrajectoryFileALS;
    private FileChooser fileChooserOpenOutputFileALS;
    private FileChooserContext fileChooserOpenInputFileTLS;
    //private FileChooser fileChooserOpenInputFileTLS;
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
    private FileChooser fileChooserOpenPointsPositionFile;
    private FileChooser fileChooserSaveTransmittanceTextFile;
    private DirectoryChooser directoryChooserSaveTransmittanceBitmapFile;
    private FileChooser fileChooserSaveHemiPhotoOutputBitmapFile;
    private FileChooser fileChooserSaveHemiPhotoOutputTextFile;
    private FileChooserContext fileChooserOpenCanopyAnalyserInputFile;
    private FileChooserContext fileChooserSaveCanopyAnalyserOutputFile;
    private FileChooserContext fileChooserSaveCanopyAnalyserCfgFile;
    
    private DirectoryChooser directoryChooserOpenOutputPathALS;
    private DirectoryChooser directoryChooserOpenOutputPathTLS;

    private Matrix4d popMatrix;
    private Matrix4d sopMatrix;
    private Matrix4d vopMatrix;
    private Matrix4d resultMatrix;
    
    private Matrix4d rasterTransfMatrix;
    
    //private boolean alsMultiFile;

    private List<LidarScan> items;
    private Rsp rsp;
    private double currentLastPointCloudLayoutY;
    
    static double SCREEN_WIDTH;
    static double SCREEN_HEIGHT;

    private final static String MATRIX_FORMAT_ERROR_MSG = "Matrix file has to look like this: \n\n\t1.0 0.0 0.0 0.0\n\t0.0 1.0 0.0 0.0\n\t0.0 0.0 1.0 0.0\n\t0.0 0.0 0.0 1.0\n";
    private static PseudoClass loadedPseudoClass = PseudoClass.getPseudoClass("loaded");
    
    private ListView<CheckBox> listviewClassifications;
    
    //echo filtering for las, laz files
    private AnchorPane anchorPaneEchoFilteringClassifications;
    
    //echo filtering for rxp files
    private AnchorPane anchorPaneEchoFilteringRxp;
    
    @FXML
    private RadioButton radiobuttonLADHomogeneous;
    @FXML
    private RadioButton radiobuttonLADLocalEstimation;
    @FXML
    private HBox hboxTwoBetaParameters;
    @FXML
    private TextField textFieldTwoBetaAlphaParameter;
    @FXML
    private TextField textFieldTwoBetaBetaParameter;
    @FXML
    private TextField textfieldNbSamplingThresholdMultires;
    @FXML
    private TextField textfieldMaxChartNumberInARow;
    @FXML
    private Label labelLADBeta;
    @FXML
    private Label labelLADAlpha;
    @FXML
    private ListView<LidarScan> listViewHemiPhotoScans;
    @FXML
    private VBox vBoxGenerateBitmapFiles;
    @FXML
    private TextField textfieldVoxelFilePathHemiPhoto;
    @FXML
    private TextField textfieldSensorPositionX;
    @FXML
    private TextField textfieldSensorPositionY;
    @FXML
    private TextField textfieldSensorPositionZ;
    @FXML
    private TextField textfieldPixelNumber;
    @FXML
    private TextField textfieldAzimutsNumber;
    @FXML
    private TextField textfieldZenithsNumber;
    @FXML
    private CheckBox checkboxGenerateSectorsTextFileHemiPhoto;
    @FXML
    private VBox vBoxGenerateBitmapFiles1;
    @FXML
    private ComboBox<String> comboboxHemiPhotoBitmapOutputMode;
    @FXML
    private TabPane tabpaneHemiPhotoMode;
    @FXML
    private CheckBox checkboxHemiPhotoGenerateBitmapFile;
    @FXML
    private TextField textfieldHemiPhotoOutputTextFile;
    @FXML
    private TextField textfieldHemiPhotoOutputBitmapFile;
    @FXML
    private ListView<SceneObjectWrapper> listviewTreeSceneObjects;
    @FXML
    private ComboBox<LeafAngleDistribution.Type> comboboxLADChoice;
    @FXML
    private TabPane tabpaneChart;
    @FXML
    private Slider sliderRSPCoresToUse;
    @FXML
    private Button buttonOpenSopMatrixFile;
    @FXML
    private CheckBox checkboxUsePointcloudFilter;
    @FXML
    private Button buttonAddPointcloudFilter;
    @FXML
    private AnchorPane anchorpanePointCloudFiltering;
    @FXML
    private CheckBox checkboxRaster;
    @FXML
    private TextField textfieldRasterFilePath;
    @FXML
    private CheckBox checkboxUseTransformationMatrix;
    @FXML
    private AnchorPane anchorPaneRasterParameters;
    @FXML
    private CheckBox checkboxFitRasterToVoxelSpace;
    @FXML
    private TextField textfieldRasterFittingMargin;
    @FXML
    private Button buttonSetTransformationMatrix;
    @FXML
    private TableView<SimulationPeriod> tableViewSimulationPeriods;
    @FXML
    private TableColumn<SimulationPeriod,String> tableColumnPeriod;
    @FXML
    private TableColumn<SimulationPeriod, String> tableColumnClearness;
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
    private ListView<File> listViewVoxelsFiles;
    @FXML
    private Button buttonOpen3DView;
    @FXML
    private ComboBox<String> comboboxAttributeToView;
    @FXML
    private Button buttonOpenPopMatrixFile;
    @FXML
    private ComboBox<String> comboboxWeighting;
    @FXML
    private ListView<File> listViewTaskList;
    @FXML
    private Label labelDTMPath;
    @FXML
    private Label labelDTMValue;
    @FXML
    private Button buttonLoadSelectedTask;
    @FXML
    private CheckBox checkboxEnableWeighting;
    @FXML
    private CheckBox checkBoxUseDefaultSopMatrix;
    @FXML
    private CheckBox checkBoxUseDefaultPopMatrix;
    @FXML
    private TabPane tabPaneVoxelisation;
    @FXML
    private MenuButton menuButtonExport;
    @FXML
    private TabPane tabPaneMain;
    @FXML
    private TextField textFieldPADMax;
    @FXML
    private ListView<LidarScan> listviewRxpScans;
    @FXML
    private CheckBox checkboxMergeAfter;
    @FXML
    private TextField textFieldMergedFileName;
    @FXML
    private TextField textFieldOutputFileMerging;
    @FXML
    private ListView<Filter> listviewFilters;
    @FXML
    private Label labelTLSOutputPath;
    @FXML
    private Button buttonOpenPonderationFile;
    @FXML
    private TextField textFieldOutputFileGroundEnergy;
    @FXML
    private CheckBox checkboxCalculateGroundEnergy;
    @FXML
    private ComboBox<String> comboboxGroundEnergyOutputFormat;
    @FXML
    private AnchorPane anchorPaneGroundEnergyParameters;
    @FXML
    private TextField textfieldVoxelFilePathTransmittance;
    @FXML
    private TextField textfieldOutputTextFilePath;
    @FXML
    private ComboBox<Integer> comboboxChooseDirectionsNumber;
    @FXML
    private TextField textfieldScannerPosCenterY;
    @FXML
    private TextField textfieldScannerPosCenterX;
    @FXML
    private TextField textfieldScannerPosCenterZ;
    @FXML
    private TextField textfieldScannerPointsPositionsFile;
    @FXML
    private RadioButton radiobuttonScannerPosSquaredArea;
    @FXML
    private TextField textfieldScannerStepArea;
    @FXML
    private RadioButton radiobuttonScannerPosFile;
    @FXML
    private TextField textfieldScannerWidthArea;
    @FXML
    private TextField textfieldLatitudeRadians;
    @FXML
    private TextField textfieldOutputBitmapFilePath;
    @FXML
    private CheckBox checkboxRaster1;
    @FXML
    private Button buttonOpenVoxelFileTransmittance;
    private Button buttonOpenOutputTextFile;
    @FXML
    private MenuButton menuButtonSelectionPeriodsList;
    @FXML
    private Button buttonOpenScannerPointsPositionsFile;
    private Button buttonOpenOutputBitmapFile;
    @FXML
    private CheckBox checkboxGenerateTextFile;
    @FXML
    private CheckBox checkboxGenerateBitmapFile;
    @FXML
    private AnchorPane anchorpaneBoundingBoxParameters;
    @FXML
    private CheckBox checkboxGenerateMultiBandRaster;
    @FXML
    private CheckBox checkboxDiscardVoxelFileWriting;
    @FXML
    private AnchorPane anchorPaneMultiBandRasterParameters;
    @FXML
    private TextField textfieldRasterResolution;
    @FXML
    private TextField textfieldRasterStartingHeight;
    @FXML
    private TextField textfieldRasterHeightStep;
    @FXML
    private TextField textfieldRasterBandNumber;
    @FXML
    private Button buttonSetVOPMatrix;
    @FXML
    private CheckBox checkboxMultiResAfterMode2;
    @FXML
    private CheckBox checkboxMultiFiles;
    @FXML
    private ComboBox<String> comboboxPreDefinedProfile;
    @FXML
    private RadioButton radiobuttonHeightFromAboveGround;
    @FXML
    private RadioButton radiobuttonHeightFromBelowCanopy;
    @FXML
    private TextField textfieldVegetationProfileMaxPAD;
    @FXML
    private RadioButton radiobuttonPreDefinedProfile;
    @FXML
    private ComboBox<String> comboboxFromVariableProfile;
    @FXML
    private RadioButton radiobuttonFromVariableProfile;
    @FXML
    private ListView<VoxelFileChart> listViewVoxelsFilesChart;
    @FXML
    private Button buttonRemoveVoxelFileFromListView1;
    @FXML
    private Button buttonAddVoxelFileToListViewForChart;
    @FXML
    private TextField textfieldLabelVoxelFileChart;
    @FXML
    private CheckBox checkboxMakeQuadrats;
    @FXML
    private ComboBox<String> comboboxSelectAxisForQuadrats;
    @FXML
    private RadioButton radiobuttonSplitCountForQuadrats;
    @FXML
    private TextField textFieldSplitCountForQuadrats;
    @FXML
    private RadioButton radiobuttonLengthForQuadrats;
    @FXML
    private TextField textFieldLengthForQuadrats;
    @FXML
    private AnchorPane anchorpaneQuadrats;
    @FXML
    private HBox hboxMaxPADVegetationProfile;
    @FXML
    private AnchorPane anchorpaneRoot;
    @FXML
    private MenuItem menuitemClearWindow;
    @FXML
    private MenuItem menuItemUpdate;
    @FXML
    private Label labelOutputFileGroundEnergy;
    private CheckBox checkboxGenerateLAI2xxxTypeFormat;
    @FXML
    private TitledPane titledPaneSceneObjectProperties;
    @FXML
    private ScrollPane scrollPaneSceneObjectProperties;
    @FXML
    private AnchorPane anchorPaneEchoFiltering;
    @FXML
    private Label labelDirectionsNumber1;
    @FXML
    private MenuItem menuItemSelectionNone1;
    @FXML
    private TextField textfieldVoxelFilePathCanopyAnalyzer;
    @FXML
    private ComboBox<Integer> comboboxChooseCanopyAnalyzerSampling;
    @FXML
    private TextField textFieldViewCapAngleCanopyAnalyzer;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask1;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask2;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask3;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask4;
    @FXML
    private ToggleButton toggleButtonCanopyAnalyzerRingMask5;
    @FXML
    private ListView<Point3d> listViewCanopyAnalyzerSensorPositions;
    @FXML
    private CheckBox checkboxGenerateCanopyAnalyzerTextFile;
    @FXML
    private CheckBox checkboxGenerateLAI2xxxFormat;
    @FXML
    private TextField textfieldOutputCanopyAnalyzerTextFile;
    @FXML
    private ToggleButton toggleButtonLAI2000Choice;
    @FXML
    private ToggleButton toggleButtonLAI2200Choice;
    @FXML
    private Label labelDirectionsNumber;
    @FXML
    private TabPane tabPaneVirtualMeasures;
    @FXML
    private ComboBox<LaserSpecification> comboboxLaserSpecification;
    @FXML
    private TextField textFieldBeamDiameterAtExit;
    @FXML
    private TextField textFieldBeamDivergence;
    @FXML
    private CheckBox checkboxCustomLaserSpecification;
    @FXML
    private MenuButton menuButtonAdvisablePADMaxValues;
    @FXML
    private MenuItem menuItemExportDart;
    @FXML
    private MenuItem menuItemExportDartPlots;

    @FXML
    private void onActionButtonOpenVoxelFileCanopyAnalyzer(ActionEvent event) {
        
        File selectedFile = fileChooserOpenCanopyAnalyserInputFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldVoxelFilePathCanopyAnalyzer.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionMenuItemPositionsCanopyAnalyzerSelectionAll(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemPositionsCanopyAnalyzerSelectionNone(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemovePositionCanopyAnalyzer(ActionEvent event) {
        ObservableList<?> selectedItems = listViewCanopyAnalyzerSensorPositions.getSelectionModel().getSelectedItems();
        listViewCanopyAnalyzerSensorPositions.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionButtonAddPositionCanopyAnalyzer(ActionEvent event) {
        
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                listViewCanopyAnalyzerSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
            }
        });
    }

    @FXML
    private void onActionButtonOpenOutputCanopyAnalyzerTextFile(ActionEvent event) {
        
        File selectedFile = fileChooserSaveCanopyAnalyserOutputFile.showSaveDialog(stage);
        
        if(selectedFile != null){
            
            textfieldOutputCanopyAnalyzerTextFile.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonExecuteCanopyAnalyzerSimulation(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            saveCanopyAnalyzerSimulation(temporaryFile);
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            
        }catch(IOException e){
            showErrorDialog(e);
            return;
        }catch(Exception e){
            showErrorDialog(e);
            return;
        }
        
        executeProcess(temporaryFile);
    }

    @FXML
    private void onActionButtonSaveCanopyAnalyzerSimulation(ActionEvent event) {
        
        File selectedFile = fileChooserSaveCanopyAnalyserCfgFile.showSaveDialog(stage);
        
        if(selectedFile != null){

            try {
                saveCanopyAnalyzerSimulation(selectedFile);
                addFileToTaskList(selectedFile);
            } catch (Exception ex) {
                logger.error("Cannot write simulation file", ex);
                return;
            }
        }
    }
    
    private void saveCanopyAnalyzerSimulation(File file) throws Exception{
        
            
        TransmittanceParameters transmParameters = new TransmittanceParameters();

        transmParameters.setShotNumber(comboboxChooseCanopyAnalyzerSampling.getSelectionModel().getSelectedItem());

        if(toggleButtonLAI2000Choice.isSelected()){
            transmParameters.setMode(TransmittanceParameters.Mode.LAI2000);
        }else{
            transmParameters.setMode(TransmittanceParameters.Mode.LAI2200);
        }

        transmParameters.setMasks(new boolean[]{toggleButtonCanopyAnalyzerRingMask1.isSelected(),
                                                toggleButtonCanopyAnalyzerRingMask2.isSelected(),
                                                toggleButtonCanopyAnalyzerRingMask3.isSelected(),
                                                toggleButtonCanopyAnalyzerRingMask4.isSelected(),
                                                toggleButtonCanopyAnalyzerRingMask5.isSelected()});

        transmParameters.setGenerateLAI2xxxTypeFormat(checkboxGenerateLAI2xxxFormat.isSelected());

        transmParameters.setInputFile(new File(textfieldVoxelFilePathCanopyAnalyzer.getText()));
        transmParameters.setGenerateTextFile(checkboxGenerateTextFile.isSelected());

        if(checkboxGenerateCanopyAnalyzerTextFile.isSelected()){
            transmParameters.setTextFile(new File(textfieldOutputCanopyAnalyzerTextFile.getText()));
        }
        if(comboboxChooseCanopyAnalyzerSampling.isEditable()){
            transmParameters.setDirectionsNumber(Integer.valueOf(comboboxChooseCanopyAnalyzerSampling.getEditor().getText()));
        }else{
            transmParameters.setDirectionsNumber(comboboxChooseCanopyAnalyzerSampling.getSelectionModel().getSelectedItem());
        }

        transmParameters.setPositions(listViewCanopyAnalyzerSensorPositions.getItems());

        //to remove later
        /*transmParameters.setUseScanPositionsFile(radiobuttonScannerPosFile.isSelected());

        if(radiobuttonScannerPosFile.isSelected()){
            transmParameters.setPointsPositionsFile(new File(textfieldScannerPointsPositionsFile.getText()));
        }else{
            transmParameters.setCenterPoint(new Point3f(Float.valueOf(textfieldScannerPosCenterX.getText()), 
                                                        Float.valueOf(textfieldScannerPosCenterY.getText()),
                                                        Float.valueOf(textfieldScannerPosCenterZ.getText())));

            transmParameters.setWidth(Float.valueOf(textfieldScannerWidthArea.getText()));
            transmParameters.setStep(Float.valueOf(textfieldScannerStepArea.getText()));
        }*/

        TransmittanceCfg cfg = new TransmittanceCfg(transmParameters);
        try {
            cfg.writeConfiguration(file);
            
        } catch (Exception ex) {
            throw ex;
        }
    }

    @FXML
    private void onActionButtonSaveExecuteCanopyAnalyzerSimulation(ActionEvent event) {
        
        File selectedFile;
        
        try {
            
            selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
            if (selectedFile != null) {

                saveCanopyAnalyzerSimulation(selectedFile);
                addFileToTaskList(selectedFile);
            }
            
        } catch (Exception ex) {
            logger.error("Cannot write simulation file", ex);
            return;
        }
        
        executeProcess(selectedFile);
    }
    
    private boolean checkALSVoxelizationParametersValidity(){
        
        boolean check1 = checkEntriesAsNumber(textFieldEnterXMin,
                textFieldEnterYMin,
                textFieldEnterZMin,
                textFieldEnterXMax,
                textFieldEnterYMax,
                textFieldEnterZMax,
                textFieldResolution);

        boolean check2;
        if(checkboxMultiFiles.isSelected()){
            check2 = checkEntriesAsFile(textFieldInputFileALS, textFieldTrajectoryFileALS, textFieldOutputFileALS);
        }else{
            check2 = checkEntriesAsFile(textFieldTrajectoryFileALS, textFieldOutputFileALS);
        }

        if ((!check1 && !checkboxMultiFiles.isSelected())  || !check2) {

            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Check entries");
            alert.setContentText("Some parameters are not set up,\nplease fill the missing arguments");

            alert.showAndWait();

            return false;
        }

        if (checkboxCalculateGroundEnergy.isSelected() && !checkboxUseDTMFilter.isSelected()) {

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setResizable(true);
            alert.setTitle("INFORMATION");
            alert.setHeaderText("Incoherence");
            alert.setContentText("Calculation of ground energy is enabled\nbut DTM filter is not!");

            alert.showAndWait();
            
            return false;
        }
        
        return true;
    }
    
    private void saveALSVoxelization(File selectedFile){
        
        if(!checkALSVoxelizationParametersValidity()){
            return;
        }
            
        VoxelParameters voxelParameters = new VoxelParameters();

        if(!checkboxMultiFiles.isSelected()){ 
            voxelParameters = getVoxelParametersFromUI();
        }

        boolean correctNaNs = checkboxMultiResAfterMode2.isSelected();

        voxelParameters.setCorrectNaNsMode2(correctNaNs);
        voxelParameters.setCorrectNaNsNbSamplingThreshold(Float.valueOf(textfieldNbSamplingThresholdMultires.getText()));

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

        voxelParameters.setUseDTMCorrection(checkboxUseDTMFilter.isSelected());
        if(checkboxUseDTMFilter.isSelected()){
            voxelParameters.minDTMDistance = Float.valueOf(textfieldDTMValue.getText());
            voxelParameters.setDtmFile(new File(textfieldDTMPath.getText()));
        }

        voxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
        voxelParameters.setTransmittanceMode(0);

        if(checkboxEnableWeighting.isSelected()){
            voxelParameters.setWeighting(comboboxWeighting.getSelectionModel().getSelectedIndex() + 1);
        }else{
            voxelParameters.setWeighting(0);
        }

        voxelParameters.setWeightingData(VoxelParameters.DEFAULT_ALS_WEIGHTING);

        voxelParameters.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());
        String extension = "";

        if(checkboxCalculateGroundEnergy.isSelected()){

                voxelParameters.setGroundEnergyFile(new File(textFieldOutputFileGroundEnergy.getText()));

                switch (comboboxGroundEnergyOutputFormat.getSelectionModel().getSelectedIndex()) {
                case 0:
                    voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                    extension = ".txt";
                    break;
                case 1:
                    voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_PNG);
                    extension = ".png";
                    break;
                default:
                    voxelParameters.setGroundEnergyFileFormat(VoxelParameters.FILE_FORMAT_TXT);
                    extension = ".png";
            }
        }

        voxelParameters.setGenerateMultiBandRaster(checkboxGenerateMultiBandRaster.isSelected());

        if(checkboxGenerateMultiBandRaster.isSelected()){

            voxelParameters.setRasterStartingHeight(Float.valueOf(textfieldRasterStartingHeight.getText()));
            voxelParameters.setRasterHeightStep(Float.valueOf(textfieldRasterHeightStep.getText()));
            voxelParameters.setShortcutVoxelFileWriting(checkboxDiscardVoxelFileWriting.isSelected());
            voxelParameters.setRasterResolution(Integer.valueOf(textfieldRasterResolution.getText()));
            voxelParameters.setRasterBandNumber(Integer.valueOf(textfieldRasterBandNumber.getText()));
        }

        VoxCfg cfg = null;

        if(!checkboxMultiFiles.isSelected()){

            cfg = new ALSVoxCfg();
            ((ALSVoxCfg)cfg).setTrajectoryFile(new File(textFieldTrajectoryFileALS.getText()));
            cfg.setVoxelParameters(voxelParameters);
            cfg.setInputType(it);
            cfg.setInputFile(new File(textFieldInputFileALS.getText()));

            /*cfg.setCorrectNaNs(correctNaNs);

            try{
                float padMax1m = Float.valueOf(textFieldPadMax1m.getText());
                float padMax2m = Float.valueOf(textFieldPadMax2m.getText());
                float padMax3m = Float.valueOf(textFieldPadMax3m.getText());
                float padMax4m = Float.valueOf(textFieldPadMax4m.getText());
                float padMax5m = Float.valueOf(textFieldPadMax5m.getText());
                cfg.setMultiResPadMax(new float[]{padMax1m, padMax2m, padMax3m, padMax4m, padMax5m});
            }catch(Exception e){}

            cfg.setMultiResUseDefaultMaxPad(!checkboxOverwritePadLimit.isSelected());*/


        }else{

            File outputPathFile = new File(textFieldOutputFileALS.getText());

            List<File> selectedFiles = new ArrayList<>();
            String[] split = textFieldInputFileALS.getText().split(";");

            for(String s : split){
                selectedFiles.add(new File(s));
            }

            if (selectedFiles.size() > 0) {

                boolean generateRasters = false;

                if (checkboxUseDTMFilter.isSelected()) {

                    ButtonType buttonTypeGenerateRasters = new ButtonType("Yes, generate one raster by file");
                    ButtonType buttonTypeNo = new ButtonType("No");

                    Alert alert = new Alert(AlertType.CONFIRMATION, "A DTM filter has been detected\n"
                            + "Would you like to generate one DTM raster file for each point file?\n"
                            +"This feature is advised to save memory but the process will be slower.",
                    buttonTypeGenerateRasters, buttonTypeNo);

                    alert.setResizable(true);
                    alert.setWidth(300);
                    alert.setTitle("DTM");

                    Optional<ButtonType> result = alert.showAndWait();

                    if(result.get() == buttonTypeGenerateRasters){
                        generateRasters = true;
                    }
                }

                List<Input> inputList = new ArrayList<>();
                boolean quick = getListOfClassificationPointToDiscard().isEmpty();

                int size = selectedFiles.size();
                int count = 1;

                RegularDtm dtm = null;

                if(generateRasters){

                    logger.info("Loading DTM file "+voxelParameters.getDtmFile().getAbsolutePath());
                    try {
                        dtm = DtmLoader.readFromAscFile(voxelParameters.getDtmFile());
                        dtm.setTransformationMatrix(MatrixUtility.convertMatrix4dToMat4D(vopMatrix));
                    } catch (Exception ex) {
                        logger.error("Cannot read dtm file", ex);
                    }
                }

                for (File file : selectedFiles) {

                    logger.info("calculate bounding-box of file "+count+"/"+size);

                    BoundingBox3d boundingBox = calculateAutomaticallyMinAndMax(file, quick);

                    VoxelParameters individualVoxelParameters = new VoxelParameters();

                    individualVoxelParameters.setBottomCorner(boundingBox.min);
                    individualVoxelParameters.setTopCorner(boundingBox.max);

                    double resolution = Double.valueOf(textFieldResolution.getText());

                    int splitX = (int) Math.ceil((boundingBox.max.x - boundingBox.min.x) / resolution);
                    int splitY = (int) Math.ceil((boundingBox.max.y - boundingBox.min.y) / resolution);
                    int splitZ = (int) Math.ceil((boundingBox.max.z - boundingBox.min.z) / resolution);

                    individualVoxelParameters.setSplit(new Point3i(splitX, splitY, splitZ));
                    individualVoxelParameters.setResolution(resolution);

                    individualVoxelParameters.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());
                    if(voxelParameters.isCalculateGroundEnergy()){
                        individualVoxelParameters.setGroundEnergyFile(new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + extension));
                    }

                    List<Input> subList = null;



                    File dtmFile = null;

                    if(generateRasters && dtm != null){

                        logger.info("Generate DTM raster of file "+count+"/"+size);

                        RegularDtm dtmSubset = dtm.subset(new BoundingBox2F(
                                new Point2F((float)individualVoxelParameters.getBottomCorner().x, (float)individualVoxelParameters.getBottomCorner().y), 
                                new Point2F((float)individualVoxelParameters.getTopCorner().x, (float)individualVoxelParameters.getTopCorner().y)), 0);

                        dtmFile = new File(outputPathFile.getAbsolutePath() + File.separator + file.getName() +".asc");
                        try {
                            dtmSubset.write(dtmFile);
                        } catch (IOException ex) {
                            logger.error("Cannot write dtm file", ex);
                        }
                    }

                    File voxFile = new File(outputPathFile.getAbsolutePath() + File.separator + file.getName() +".vox");
                    inputList.add(new Input(individualVoxelParameters, file, dtmFile, voxFile, subList, new File(outputPathFile.getAbsolutePath() + File.separator + file.getName() +"_multires_.vox")));

                    count++;
                }

                cfg = new MultiVoxCfg();

                ((MultiVoxCfg)cfg).setMultiProcessInputs(inputList);
                ((MultiVoxCfg)cfg).setTrajectoryFile(new File(textFieldTrajectoryFileALS.getText()));
            }

            removeWarnings = false;
        }

        if(cfg != null){

            cfg.setOutputFile(new File(textFieldOutputFileALS.getText()));

            cfg.setUsePopMatrix(false);
            cfg.setUseSopMatrix(false);
            cfg.setUseVopMatrix(checkboxUseVopMatrix.isSelected());

            cfg.setPopMatrix(popMatrix);
            cfg.setSopMatrix(sopMatrix);
            cfg.setVopMatrix(vopMatrix);

            cfg.setVoxelParameters(voxelParameters);

            ((ALSVoxCfg)cfg).setClassifiedPointsToDiscard(getListOfClassificationPointToDiscard());
            cfg.setFilters(listviewFilters.getItems());

            try {
                cfg.writeConfiguration(selectedFile);
                
            } catch (Exception ex) {
                logger.error("Cannot write configuration file", ex);
            }
        }
    }

    @FXML
    private void onActionButtonSaveALSVoxelization(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        
        if (selectedFile != null) {
            saveALSVoxelization(selectedFile);
            addFileToTaskList(selectedFile);
        }
    }

    @FXML
    private void onActionButtonSaveExecuteALSVoxelization(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        
        if (selectedFile != null) {
            saveALSVoxelization(selectedFile);
            addFileToTaskList(selectedFile);
            executeProcess(selectedFile);
        }
        
    }

    @FXML
    private void onActionButtonExecuteALSVoxelization(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            saveALSVoxelization(temporaryFile);
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            
        }catch(IOException e){
            showErrorDialog(e);
            return;
        }catch(Exception e){
            showErrorDialog(e);
            return;
        }
        
        executeProcess(temporaryFile);
    }

    @FXML
    private void onActionButtonExecuteTLSVoxelization(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            saveTLSVoxelization(temporaryFile);
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            
        }catch(IOException e){
            showErrorDialog(e);
            return;
        }catch(Exception e){
            showErrorDialog(e);
            return;
        }
        
        executeProcess(temporaryFile);
    }
    
    private boolean checkTLSVoxelizationParametersValidity(){
        
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

            return false;
        }
        
        return true;
    }
    
    private void saveTLSVoxelization(File selectedFile){
        
        if(!checkTLSVoxelizationParametersValidity()){
            return;
        }
        
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
            case 0:
                it = InputType.RXP_SCAN;
                break;
            case 1:
                it = InputType.RSP_PROJECT;
                break;
            case 2:
                it = InputType.PTX_PROJECT;
                break;
            case 3:
                it = InputType.PTG_PROJECT;
                break;
            case 4:
                it = InputType.POINTS_FILE;
                break;
            case 5:
                it = InputType.SHOTS_FILE;
                break;
            default:
                it = InputType.RSP_PROJECT;
        }

        TLSVoxCfg cfg = new TLSVoxCfg();
        /*
        VoxelisationConfiguration cfg = new VoxelisationConfiguration(ProcessMode.VOXELISATION_TLS, it,
                new File(textFieldInputFileTLS.getText()),
                new File(textFieldTrajectoryFileALS.getText()),
                new File(textFieldOutputPathTLS.getText()),
                voxelParameters,
                checkboxUsePopMatrix.isSelected(), popMatrix,
                checkboxUseSopMatrix.isSelected(), sopMatrix,
                checkboxUseVopMatrix.isSelected(), vopMatrix);*/

        cfg.setInputFile(new File(textFieldInputFileTLS.getText()));
        cfg.setOutputFile(new File(textFieldOutputPathTLS.getText()));
        cfg.setInputType(it);

        cfg.setUsePopMatrix(checkboxUsePopMatrix.isSelected());
        cfg.setUseSopMatrix(checkboxUseSopMatrix.isSelected());
        cfg.setUseVopMatrix(checkboxUseVopMatrix.isSelected());

        cfg.setPopMatrix(popMatrix);
        cfg.setSopMatrix(sopMatrix);
        cfg.setVopMatrix(vopMatrix);

        cfg.setVoxelParameters(voxelParameters);

        cfg.setFilters(listviewFilters.getItems());

        if (it == InputType.RSP_PROJECT || it == InputType.PTG_PROJECT || it == InputType.PTX_PROJECT) {
            cfg.setMatricesAndFiles(listviewRxpScans.getItems());
        }

        cfg.setEchoFilters(filteringPaneController.getFilterList());

        try {
            cfg.writeConfiguration(selectedFile);
        } catch (Exception ex) {
            logger.error("Cannot write configuration file");
        }

        
    }

    @FXML
    private void onActionButtonSaveTLSVoxelization(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            saveTLSVoxelization(selectedFile);
            addFileToTaskList(selectedFile);
        }
    }

    @FXML
    private void onActionButtonSaveExecuteTLSVoxelization(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {

            saveTLSVoxelization(selectedFile);
            executeProcess(selectedFile);
        }
    }

    @FXML
    private void onActionButtonExecuteMergingProcess(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            saveMergingProcess(temporaryFile);
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            
        }catch(IOException e){
            showErrorDialog(e);
            return;
        }catch(Exception e){
            showErrorDialog(e);
            return;
        }
        
        executeProcess(temporaryFile);
    }
    
    private void saveMergingProcess(File selectedFile){
        
        VoxelParameters voxParameters = new VoxelParameters();
        voxParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));

        VoxMergingCfg cfg = new VoxMergingCfg(new File(textFieldOutputFileMerging.getText()),
                voxParameters,
                listViewVoxelsFiles.getSelectionModel().getSelectedItems());

        try {
            cfg.writeConfiguration(selectedFile);
            addFileToTaskList(selectedFile);
        } catch (Exception ex) {
            logger.error("Cannot write configuration file", ex);
        }
    }

    @FXML
    private void onActionButtonSaveMergingProcess(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {
            saveMergingProcess(selectedFile);
        }
    }

    @FXML
    private void onActionButtonSaveExecuteMergingProcess(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        if (selectedFile != null) {
            saveMergingProcess(selectedFile);
            executeProcess(selectedFile);
        }
    }
    
    private class SceneObjectProperty{
        
        private String name;
        private String value;

        public SceneObjectProperty(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }
    }
    
    private void addMenuItemPadValue(MenuItem menuItem, final float value){
                
        menuItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                textFieldPADMax.setText(String.valueOf(value));
            }
        });
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
//        TableColumn<SceneObjectWrapper, String> propertyNameColumn = new TableColumn<>("Property");
//        TableColumn<SceneObjectWrapper, String> propertyValueColumn = new TableColumn<>("State/value");
//        
//        propertyNameColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SceneObjectWrapper, String>, ObservableValue<String>>() {
//
//            @Override
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<SceneObjectWrapper, String> param) {
//                return new SimpleStringProperty(param.getValue().getPropertyList());
//            }
//        });
//        
//        propertyValueColumn.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SceneObjectWrapper, String>, ObservableValue<String>>() {
//
//            @Override
//            public ObservableValue<String> call(TableColumn.CellDataFeatures<SceneObjectWrapper, String> param) {
//                return new SimpleStringProperty(param.getValue().getValue());
//            }
//        });
//        
//        tableviewSceneObjectProperties.getColumns().addAll(propertyNameColumn, propertyValueColumn);

        MenuItem menuItemPadValue1m = new MenuItem("1m voxel size");
        addMenuItemPadValue(menuItemPadValue1m, 3.536958f);
        
        MenuItem menuItemPadValue2m = new MenuItem("2m voxel size");
        addMenuItemPadValue(menuItemPadValue2m, 2.262798f);
        
        MenuItem menuItemPadValue3m = new MenuItem("3m voxel size");
        addMenuItemPadValue(menuItemPadValue3m, 1.749859f);
        
        MenuItem menuItemPadValue4m = new MenuItem("4m voxel size");
        addMenuItemPadValue(menuItemPadValue4m, 1.3882959f);
        
        MenuItem menuItemPadValue5m = new MenuItem("5m voxel size");
        addMenuItemPadValue(menuItemPadValue5m, 1.0848f);
        
        menuButtonAdvisablePADMaxValues.getItems().addAll(menuItemPadValue1m,
                                                        menuItemPadValue2m,
                                                        menuItemPadValue3m,
                                                        menuItemPadValue4m,
                                                        menuItemPadValue5m);
        
        fileChooserSaveCanopyAnalyserOutputFile = new FileChooserContext();
        fileChooserSaveCanopyAnalyserCfgFile = new FileChooserContext();
        fileChooserOpenCanopyAnalyserInputFile = new FileChooserContext();
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
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
        
        listviewTreeSceneObjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listviewTreeSceneObjects.setCellFactory(new Callback<ListView<SceneObjectWrapper>, ListCell<SceneObjectWrapper>>() {

            @Override
            public ListCell<SceneObjectWrapper> call(ListView<SceneObjectWrapper> param) {
                
                ListCell<SceneObjectWrapper> cell = new ListCell<SceneObjectWrapper>(){
                    
                    @Override
                    protected void updateItem(SceneObjectWrapper t, boolean bln) {
                        
                        super.updateItem(t, bln);                        
                        setGraphic(t);
                    }
                };
                
                return cell;
            }
        });
        
        listviewTreeSceneObjects.setOnDragOver(dragOverEvent);
        listviewTreeSceneObjects.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1) {
                    success = true;
                    for (File file : db.getFiles()) {
                        if (file != null) {
                            
                            try {
                                addSceneObjectToList(file);
                            } catch (Exception ex) {
                                showErrorDialog(ex);
                            }
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        listviewTreeSceneObjects.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<SceneObjectWrapper>() {

            @Override
            public void changed(ObservableValue<? extends SceneObjectWrapper> observable, SceneObjectWrapper oldValue, SceneObjectWrapper newValue) {
                
                if(newValue != null){
                    sceneObjectPropertiesPanelController.setSceneObjectWrapper(newValue);
                }
                
            }
        });
        
        ContextMenu contextMenuLidarScanEdit = new ContextMenu();
        MenuItem editItem = new MenuItem("Edit");
        
        editItem.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {
                
                filterFrameController.setFilters("Reflectance", "Deviation", "Amplitude");
                filterFrame.show();

                filterFrame.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {

                        if (filterFrameController.getFilter() != null) {
                            ObservableList<LidarScan> items = listViewHemiPhotoScans.getSelectionModel().getSelectedItems();
                            for(LidarScan scan : items){
                                scan.filters.add(filterFrameController.getFilter());
                            }
                        }
                    }
                });
            }
        });
        
        contextMenuLidarScanEdit.getItems().add(editItem);
        
        listViewHemiPhotoScans.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewHemiPhotoScans.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {

            @Override
            public void handle(ContextMenuEvent event) {
                contextMenuLidarScanEdit.show(listViewHemiPhotoScans, event.getScreenX(), event.getScreenY());
            }
        });
        
        /**LAD tab initialization**/
        comboboxLADChoice.getItems().addAll(LeafAngleDistribution.Type.UNIFORM,
                                            LeafAngleDistribution.Type.SPHERIC,
                                            LeafAngleDistribution.Type.ERECTOPHILE,
                                            LeafAngleDistribution.Type.PLANOPHILE,
                                            LeafAngleDistribution.Type.EXTREMOPHILE,
                                            LeafAngleDistribution.Type.PLAGIOPHILE,
                                            LeafAngleDistribution.Type.HORIZONTAL,
                                            LeafAngleDistribution.Type.VERTICAL,
                                            LeafAngleDistribution.Type.ELLIPSOIDAL,
                                            LeafAngleDistribution.Type.ELLIPTICAL,
                                            LeafAngleDistribution.Type.TWO_PARAMETER_BETA);
        
        comboboxLADChoice.getSelectionModel().select(LeafAngleDistribution.Type.SPHERIC);
        comboboxLADChoice.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LeafAngleDistribution.Type>() {

            @Override
            public void changed(ObservableValue<? extends LeafAngleDistribution.Type> observable, LeafAngleDistribution.Type oldValue, LeafAngleDistribution.Type newValue) {
                
                if(newValue == LeafAngleDistribution.Type.TWO_PARAMETER_BETA || newValue == LeafAngleDistribution.Type.ELLIPSOIDAL){
                    
                    hboxTwoBetaParameters.setVisible(true);
                    
                    if(newValue == LeafAngleDistribution.Type.ELLIPSOIDAL){
                        labelLADBeta.setVisible(false);
                    }else{
                        labelLADBeta.setVisible(true);
                    }
                }else{
                    hboxTwoBetaParameters.setVisible(false);
                }
            }
        });
        
        ToggleGroup ladTypeGroup = new ToggleGroup();
        radiobuttonLADHomogeneous.setToggleGroup(ladTypeGroup);
        radiobuttonLADLocalEstimation.setToggleGroup(ladTypeGroup);
        
        /**CHART panel initialization**/
        
        ToggleGroup profileChartType = new ToggleGroup();
        radiobuttonPreDefinedProfile.setToggleGroup(profileChartType);
        radiobuttonFromVariableProfile.setToggleGroup(profileChartType);
        
        ToggleGroup profileChartRelativeHeightType = new ToggleGroup();
        radiobuttonHeightFromAboveGround.setToggleGroup(profileChartRelativeHeightType);
        radiobuttonHeightFromBelowCanopy.setToggleGroup(profileChartRelativeHeightType);
        
        comboboxFromVariableProfile.disableProperty().bind(radiobuttonPreDefinedProfile.selectedProperty());
        comboboxPreDefinedProfile.disableProperty().bind(radiobuttonFromVariableProfile.selectedProperty());
        
        hboxMaxPADVegetationProfile.visibleProperty().bind(radiobuttonPreDefinedProfile.selectedProperty());
        
        listViewVoxelsFilesChart.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                if(newValue.intValue() >= 0){
                    textfieldLabelVoxelFileChart.setText(listViewVoxelsFilesChart.getItems().get(newValue.intValue()).label);
                }
            }
        });
        
        textfieldLabelVoxelFileChart.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                if(listViewVoxelsFilesChart.getSelectionModel().getSelectedIndex() >= 0){
                    listViewVoxelsFilesChart.getSelectionModel().getSelectedItem().label = newValue;
                }
            }
        });
        
        listViewVoxelsFilesChart.getItems().addListener(new ListChangeListener<VoxelFileChart>() {

            @Override
            public void onChanged(ListChangeListener.Change<? extends VoxelFileChart> c) {
                              
                while(c.next()){}
                
                if(c.wasAdded() && c.getAddedSize() == c.getList().size()){

                    try {
                        VoxelFileReader reader = new VoxelFileReader(c.getList().get(0).file);
                        String[] columnNames = reader.getVoxelSpaceInfos().getColumnNames();
                        comboboxFromVariableProfile.getItems().clear();
                        comboboxFromVariableProfile.getItems().addAll(columnNames);
                        comboboxFromVariableProfile.getSelectionModel().selectFirst();
                    } catch (Exception ex) {
                        logger.error("Cannot read voxel file", ex);
                    }
                }
                
            }
        });
        
        /*listViewVoxelsFilesChart.setCellFactory(new Callback<ListView<VoxelFileChart>, ListCell<VoxelFileChart>>() {

            @Override
            public ListCell<VoxelFileChart> call(ListView<VoxelFileChart> param) {
                
                return new ColorRectCell();
            }
        });*/
        
        
        anchorpaneQuadrats.disableProperty().bind(checkboxMakeQuadrats.selectedProperty().not());
        
        comboboxSelectAxisForQuadrats.getItems().addAll("X", "Y", "Z");
        comboboxSelectAxisForQuadrats.getSelectionModel().select(1);
        
        comboboxPreDefinedProfile.getItems().addAll("Vegetation (PAD)");
        comboboxPreDefinedProfile.getSelectionModel().selectFirst();
        
        radiobuttonSplitCountForQuadrats.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                textFieldSplitCountForQuadrats.setDisable(!newValue);
                textFieldLengthForQuadrats.setDisable(newValue);
            }
        });
        
        ToggleGroup chartMakeQuadratsSplitType = new ToggleGroup();
        radiobuttonLengthForQuadrats.setToggleGroup(chartMakeQuadratsSplitType);
        radiobuttonSplitCountForQuadrats.setToggleGroup(chartMakeQuadratsSplitType);
        
        /**Virtual measures panel initialization**/
        
        comboboxHemiPhotoBitmapOutputMode.getItems().addAll("Pixel", "Color");
        comboboxHemiPhotoBitmapOutputMode.getSelectionModel().selectFirst();
        
        ToggleGroup virtualMeasuresChoiceGroup = new ToggleGroup();
        
        toggleButtonLAI2000Choice.setToggleGroup(virtualMeasuresChoiceGroup);
        toggleButtonLAI2200Choice.setToggleGroup(virtualMeasuresChoiceGroup);
        
        comboboxChooseCanopyAnalyzerSampling.getItems().setAll(500, 4000, 10000);
        comboboxChooseCanopyAnalyzerSampling.getSelectionModel().selectFirst();
        
        initEchoFiltering();
        
        data = FXCollections.observableArrayList();
        
        tableViewSimulationPeriods.setItems(data);
        tableViewSimulationPeriods.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        comboboxChooseDirectionsNumber.getItems().addAll(1, 6, 16, 46, 136, 406);
        comboboxChooseDirectionsNumber.getSelectionModel().selectFirst();
        
        ToggleGroup scannerPositionsMode = new ToggleGroup();
        
        radiobuttonScannerPosSquaredArea.setToggleGroup(scannerPositionsMode);
        radiobuttonScannerPosFile.setToggleGroup(scannerPositionsMode);
        
        tableColumnPeriod.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SimulationPeriod, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SimulationPeriod, String> param) {
                return new SimpleStringProperty(param.getValue().getPeriod().toString());
            }
        });
        
        tableColumnClearness.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<SimulationPeriod, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<SimulationPeriod, String> param) {
                return new SimpleStringProperty(String.valueOf(param.getValue().getClearnessCoefficient()));
            }
        });
        /*
        textFieldInputFileALS.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                alsMultiFile = newValue.contains(";");
                anchorpaneBoundingBoxParameters.setDisable(alsMultiFile);
                //checkboxMultiResAfter.setDisable(!alsMultiFile);
                //textfieldResMultiRes.setDisable(!alsMultiFile);
            }
        });*/
        
        checkboxMultiFiles.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                anchorpaneBoundingBoxParameters.setDisable(newValue);
            }
        });
        
        checkboxGenerateTextFile.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                textfieldOutputTextFilePath.setDisable(!newValue);
                buttonOpenOutputTextFile.setDisable(!newValue);
                
            }
        });
        
        checkboxGenerateBitmapFile.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                textfieldOutputBitmapFilePath.setDisable(!newValue);
                buttonOpenOutputBitmapFile.setDisable(!newValue);
                
            }
        });
        
        checkboxGenerateMultiBandRaster.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                anchorPaneMultiBandRasterParameters.setDisable(!newValue);
            }
        });
         
        fileChooserOpenConfiguration = new FileChooser();
        fileChooserOpenConfiguration.setTitle("Choose configuration file");

        fileChooserSaveConfiguration = new FileChooserContext("cfg.xml");
        fileChooserSaveConfiguration.fc.setTitle("Choose output file");

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

        fileChooserOpenInputFileTLS = new FileChooserContext();
        fileChooserOpenInputFileTLS.fc.setTitle("Open input file");
        fileChooserOpenInputFileTLS.fc.getExtensionFilters().addAll(
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
        
        fileChooserSaveTransmittanceTextFile = new FileChooser();
        fileChooserSaveTransmittanceTextFile.setTitle("Save text file");
        
        directoryChooserSaveTransmittanceBitmapFile = new DirectoryChooser();
        directoryChooserSaveTransmittanceBitmapFile.setTitle("Choose output directory");
        
        fileChooserSaveHemiPhotoOutputBitmapFile = new FileChooser();
        fileChooserSaveHemiPhotoOutputBitmapFile.setTitle("Save bitmap file");
        
        fileChooserSaveHemiPhotoOutputTextFile = new FileChooser();
        fileChooserSaveHemiPhotoOutputTextFile.setTitle("Save text file");

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
        
        fileChooserOpenPointsPositionFile = new FileChooser();
        fileChooserOpenPointsPositionFile.setTitle("Choose points file");
        fileChooserOpenPointsPositionFile.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("TXT Files", "*.txt"));
        
        try {
            viewCapsSetupFrame = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ViewCapsSetupFrame.fxml"));
            Parent root = loader.load();
            viewCapsSetupFrameController = loader.getController();
            viewCapsSetupFrame.setScene(new Scene(root));
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SceneObjectPropertiesPanel.fxml"));
            AnchorPane sceneObjectPropertiesPane = loader.load();
            sceneObjectPropertiesPanelController = loader.getController();
            scrollPaneSceneObjectProperties.setContent(sceneObjectPropertiesPane);
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FilteringPaneComponent.fxml"));
            anchorPaneEchoFilteringRxp = loader.load();
            filteringPaneController = loader.getController();
            filteringPaneController.setFiltersNames("Reflectance", "Amplitude", "Deviation");
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        try {
            positionImporterFrame = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PositionImporterFrame.fxml"));
            Parent root = loader.load();
            positionImporterFrameController = loader.getController();
            positionImporterFrame.setScene(new Scene(root));
            positionImporterFrameController.setStage(positionImporterFrame);
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
            
            
        try {
            attributsImporterFrame = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AttributsImporterFrame.fxml"));
            Parent root = loader.load();
            attributsImporterFrameController = loader.getController();
            attributsImporterFrame.setScene(new Scene(root));
            attributsImporterFrameController.setStage(attributsImporterFrame);
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        try {
            textFileParserFrameController = TextFileParserFrameController.getInstance();
        } catch (Exception ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        try {
            transformationFrameController = TransformationFrameController.getInstance();
            transformationFrame = transformationFrameController.getStage();
        } catch (Exception ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        updaterFrame = new Stage();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/UpdaterFrame.fxml"));
            Parent root = loader.load();
            updaterFrameController = loader.getController();
            updaterFrame.setScene(new Scene(root));
        
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        rspExtractorFrame = new Stage();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/RspExtractorFrame.fxml"));
            Parent root = loader.load();
            rspExtractorFrameController = loader.getController();
            rspExtractorFrame.setScene(new Scene(root));
            rspExtractorFrameController.setStage(rspExtractorFrame);
        
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        dateChooserFrame = new Stage();
                
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/DateChooserFrame.fxml"));
            Parent root = loader.load();
            dateChooserFrameController = loader.getController();
            dateChooserFrame.setScene(new Scene(root));
            dateChooserFrameController.setStage(dateChooserFrame);
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }

        comboboxModeALS.getItems().addAll("Las file", "Laz file", "Points file (unavailable)", "Shots file (unavailable)");
        comboboxModeTLS.getItems().addAll("Rxp scan", "Rsp project", "ptx","ptg","Points file (unavailable)", "Shots file (unavailable)");
        comboboxWeighting.getItems().addAll("From the echo number", "From a matrix file", "Local recalculation (unavailable)");
        comboboxGroundEnergyOutputFormat.getItems().addAll("txt", "png");

        comboboxLaserSpecification.getItems().addAll(LaserSpecification.values());
        
        comboboxLaserSpecification.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LaserSpecification>() {
            @Override
            public void changed(ObservableValue<? extends LaserSpecification> observable, LaserSpecification oldValue, LaserSpecification newValue) {
                DecimalFormatSymbols symb = new DecimalFormatSymbols();
                symb.setDecimalSeparator('.');
                DecimalFormat formatter = new DecimalFormat("#####.######", symb);
                
                textFieldBeamDiameterAtExit.setText(formatter.format(newValue.getBeamDiameterAtExit()));
                textFieldBeamDivergence.setText(formatter.format(newValue.getBeamDivergence()));
            }
        });
        
        comboboxLaserSpecification.getSelectionModel().select(LaserSpecification.DEFAULT_ALS);
        
        checkboxCustomLaserSpecification.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                comboboxLaserSpecification.setDisable(newValue);
                textFieldBeamDiameterAtExit.setDisable(!newValue);
                textFieldBeamDivergence.setDisable(!newValue);
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

        resetMatrices();

        calculateMatrixFrame = new Stage();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/CalculateMatrixFrame.fxml"));
            Parent root = loader.load();
            calculateMatrixFrameController = loader.getController();
            calculateMatrixFrameController.setStage(calculateMatrixFrame);
            Scene scene = new Scene(root);
            calculateMatrixFrame.setScene(scene);
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }

        filterFrame = new Stage();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/FilterFrame.fxml"));
            Parent root = loader.load();
            filterFrameController = loader.getController();
            filterFrameController.setStage(filterFrame);
            filterFrameController.setFilters("Angle");
            filterFrame.setScene(new Scene(root));
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
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
                buttonSetVOPMatrix.setDisable(!newValue);
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
        

        comboboxWeighting.disableProperty().bind(checkboxEnableWeighting.selectedProperty().not());

        listviewRxpScans.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<LidarScan>() {

            @Override
            public void changed(ObservableValue<? extends LidarScan> observable, LidarScan oldValue, LidarScan newValue) {
                if(newValue != null){
                    sopMatrix = newValue.matrix;
                    updateResultMatrix();
                }
            }
        });

        comboboxModeTLS.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {

                switch (newValue.intValue()) {

                    case 1:
                    case 2:
                    case 3:
                        listviewRxpScans.setDisable(false);
                        checkboxMergeAfter.setDisable(false);
                        textFieldMergedFileName.setDisable(false);
                        disableSopMatrixChoice(false);
                        labelTLSOutputPath.setText("Output path");
                        break;

                    default:
                        listviewRxpScans.setDisable(true);
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
                        
                        anchorPaneEchoFiltering.getChildren().set(0, anchorPaneEchoFilteringClassifications);
                        
                        //anchorPaneEchoFilteringClassifications.setVisible(true);
                        anchorpaneBoundingBoxParameters.setDisable(checkboxMultiFiles.isSelected());
                        
                        break;
                    default:
                        anchorPaneGroundEnergyParameters.setDisable(true);
                        checkboxCalculateGroundEnergy.setDisable(true);
                        anchorPaneEchoFiltering.getChildren().set(0, anchorPaneEchoFilteringRxp);
                        //anchorPaneEchoFilteringClassifications.setVisible(false);
                        anchorpaneBoundingBoxParameters.setDisable(false);
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

        textFieldInputFileALS.setOnDragOver(dragOverEvent);
        textFieldTrajectoryFileALS.setOnDragOver(dragOverEvent);
        textFieldOutputFileALS.setOnDragOver(dragOverEvent);
        textFieldInputFileTLS.setOnDragOver(dragOverEvent);
        textFieldOutputFileMerging.setOnDragOver(dragOverEvent);
        textfieldDTMPath.setOnDragOver(dragOverEvent);
        textFieldOutputFileGroundEnergy.setOnDragOver(dragOverEvent);
        listViewTaskList.setOnDragOver(dragOverEvent);
        listViewVoxelsFiles.setOnDragOver(dragOverEvent);
        textfieldRasterFilePath.setOnDragOver(dragOverEvent);
        textfieldVoxelFilePathTransmittance.setOnDragOver(dragOverEvent);
        textfieldScannerPointsPositionsFile.setOnDragOver(dragOverEvent);
        textfieldOutputTextFilePath.setOnDragOver(dragOverEvent);
        textfieldOutputBitmapFilePath.setOnDragOver(dragOverEvent);

        setDragDroppedSingleFileEvent(textFieldInputFileALS);
        setDragDroppedSingleFileEvent(textFieldTrajectoryFileALS);
        setDragDroppedSingleFileEvent(textFieldOutputFileALS);
        setDragDroppedSingleFileEvent(textFieldInputFileTLS);
        setDragDroppedSingleFileEvent(textFieldOutputFileMerging);
        setDragDroppedSingleFileEvent(textfieldDTMPath);
        setDragDroppedSingleFileEvent(textFieldOutputFileGroundEnergy);
        setDragDroppedSingleFileEvent(textfieldRasterFilePath);
        setDragDroppedSingleFileEvent(textfieldVoxelFilePathTransmittance);
        setDragDroppedSingleFileEvent(textfieldScannerPointsPositionsFile);
        setDragDroppedSingleFileEvent(textfieldOutputTextFilePath);
        setDragDroppedSingleFileEvent(textfieldOutputBitmapFilePath);

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
                buttonSetTransformationMatrix.setDisable(!newValue);
            }
        });
    }
    
    private void initEchoFiltering(){
        
        anchorPaneEchoFilteringClassifications = new AnchorPane();
        listviewClassifications = new ListView<>();
        
        
        listviewClassifications.getItems().addAll(
                createSelectedCheckbox(Classification.CREATED_NEVER_CLASSIFIED.getValue()+" - "+
                        Classification.CREATED_NEVER_CLASSIFIED.getDescription()),
                createSelectedCheckbox(Classification.UNCLASSIFIED.getValue()+" - "+
                        Classification.UNCLASSIFIED.getDescription()),
                createSelectedCheckbox(Classification.GROUND.getValue()+" - "+
                        Classification.GROUND.getDescription()),
                createSelectedCheckbox(Classification.LOW_VEGETATION.getValue()+" - "+
                        Classification.LOW_VEGETATION.getDescription()),
                createSelectedCheckbox(Classification.MEDIUM_VEGETATION.getValue()+" - "+
                        Classification.MEDIUM_VEGETATION.getDescription()),
                createSelectedCheckbox(Classification.HIGH_VEGETATION.getValue()+" - "+
                        Classification.HIGH_VEGETATION.getDescription()),
                createSelectedCheckbox(Classification.BUILDING.getValue()+" - "+
                        Classification.BUILDING.getDescription()),
                createSelectedCheckbox(Classification.LOW_POINT.getValue()+" - "+
                        Classification.LOW_POINT.getDescription()),
                createSelectedCheckbox(Classification.MODEL_KEY_POINT.getValue()+" - "+
                        Classification.MODEL_KEY_POINT.getDescription()),
                createSelectedCheckbox(Classification.WATER.getValue()+" - "+
                        Classification.WATER.getDescription()),
                createSelectedCheckbox(Classification.RESERVED_10.getValue()+" - "+
                        Classification.RESERVED_10.getDescription()),
                createSelectedCheckbox(Classification.RESERVED_11.getValue()+" - "+
                        Classification.RESERVED_11.getDescription()),
                createSelectedCheckbox(Classification.OVERLAP_POINTS.getValue()+" - "+
                        Classification.OVERLAP_POINTS.getDescription()));
        
        listviewClassifications.setPrefSize(269, 134);
        
        anchorPaneEchoFilteringClassifications.getChildren().add(new VBox(new Label("Classifications"), listviewClassifications));
        anchorPaneEchoFilteringClassifications.setLayoutX(14);
        anchorPaneEchoFilteringClassifications.setLayoutY(14);
        anchorPaneEchoFiltering.getChildren().add(anchorPaneEchoFilteringClassifications);
        
    }
    
    @FXML
    private void onActionButtonDisplayPdf(ActionEvent event) {
        
        LeafAngleDistribution distribution = getLeafAngleDistribution();
        
        XYSeries serie = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            
            serie.add(angleInDegrees, pdf);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection(serie);
            
        ChartViewer viewer = new ChartViewer("PDF function", 500, 500, 1);
        viewer.insertChart(ChartViewer.createBasicChart("PDF ~ angles", dataset, "Angle (degrees)", "PDF"));
        viewer.show();
        
    }

    @FXML
    private void onActionButtonDisplayGTheta(ActionEvent event) {
        
        LeafAngleDistribution distribution = getLeafAngleDistribution();
        
        XYSeries serie = new XYSeries(distribution.getType(), false);
        
        DirectionalTransmittance m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        XYSeriesCollection dataset = new XYSeriesCollection(serie);
            
        ChartViewer viewer = new ChartViewer("GTheta", 500, 500, 1);
        viewer.insertChart(ChartViewer.createBasicChart("GTheta ~ inclinaison angle", dataset, "Angle (degrees)", "GTheta"));
        viewer.show();
        
        
    }

    @FXML
    private void onActionButtonOpenRspProject(ActionEvent event) {
        
        File selectedFile = fileChooserOpenInputFileTLS.showOpenDialog(stage);
        
        if(selectedFile != null){
            
            Rsp selectedProject = new Rsp();
            try {
                selectedProject.read(selectedFile);

                rspExtractorFrameController.init(selectedProject);
                rspExtractorFrame.show();

                rspExtractorFrame.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {
                        List<RspExtractorFrameController.Scan> selectedScans = rspExtractorFrameController.getSelectedScans();
                        
                        ObservableList<LidarScan> items1 = listViewHemiPhotoScans.getItems();
                        
                        for(RspExtractorFrameController.Scan scan : selectedScans){
                            items1.add(new LidarScan(scan.getFile(), MatrixUtility.convertMat4DToMatrix4d(scan.getSop())));
                        }
                        

                    }
                });

            } catch (JDOMException | IOException ex) {
                showErrorDialog(ex);
            }

        }
        
        
        
    }

    @FXML
    private void onActionButtonOpenVoxelFileHemiPhoto(ActionEvent event) {
        
        File selectedFile = fileChooserOpenVoxelFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldVoxelFilePathHemiPhoto.setText(selectedFile.getAbsolutePath());
        }
    }
    
    @FXML
    private void onActionButtonSaveExecuteHemiPhotoSimulation(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        
        if (selectedFile != null) {
            
            HemiParameters hemiParameters = new HemiParameters();
            try {
                
                hemiParameters.setPixelNumber(Integer.valueOf(textfieldPixelNumber.getText()));
                hemiParameters.setAzimutsNumber(Integer.valueOf(textfieldAzimutsNumber.getText()));
                hemiParameters.setZenithsNumber(Integer.valueOf(textfieldZenithsNumber.getText()));
                
                int selectedMode = tabpaneHemiPhotoMode.getSelectionModel().getSelectedIndex();
                
                switch(selectedMode){
                    case 0 :
                        hemiParameters.setMode(HemiParameters.Mode.ECHOS);
                        hemiParameters.setRxpScansList(listViewHemiPhotoScans.getItems());
                        break;
                    case 1:
                        hemiParameters.setMode(HemiParameters.Mode.PAD);
                        
                        hemiParameters.setVoxelFile(new File(textfieldVoxelFilePathHemiPhoto.getText()));
                        hemiParameters.setSensorPosition(new Point3d(Double.valueOf(textfieldSensorPositionX.getText()),
                                                                    Double.valueOf(textfieldSensorPositionY.getText()),
                                                                    Double.valueOf(textfieldSensorPositionZ.getText())));
                        break;
                }
                
                hemiParameters.setGenerateBitmapFile(checkboxHemiPhotoGenerateBitmapFile.isSelected());
                
                if(checkboxHemiPhotoGenerateBitmapFile.isSelected()){
                    hemiParameters.setOutputBitmapFile(new File(textfieldHemiPhotoOutputBitmapFile.getText()));
                    
                    int selectedIndex = comboboxHemiPhotoBitmapOutputMode.getSelectionModel().getSelectedIndex();
                    switch(selectedIndex){
                        case 0:
                            hemiParameters.setBitmapMode(HemiParameters.BitmapMode.PIXEL);
                            break;
                        case 1:
                            hemiParameters.setBitmapMode(HemiParameters.BitmapMode.COLOR);
                            break;
                    }
                    
                }
                
                hemiParameters.setGenerateTextFile(checkboxGenerateSectorsTextFileHemiPhoto.isSelected());
                
                if(checkboxGenerateSectorsTextFileHemiPhoto.isSelected()){
                    hemiParameters.setOutputTextFile(new File(textfieldHemiPhotoOutputTextFile.getText()));
                }
                
                HemiPhotoCfg hemiPhotoCfg = new HemiPhotoCfg(hemiParameters);
                hemiPhotoCfg.writeConfiguration(selectedFile);
                addTasksToTaskList(selectedFile);
                
            } catch (Exception ex) {
                showErrorDialog(ex);
            }
        }
    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputTextFile(ActionEvent event) {
        
        if(lastFCOpenHemiPhotoOutputTextFile != null){
            fileChooserSaveHemiPhotoOutputTextFile.setInitialDirectory(lastFCOpenHemiPhotoOutputTextFile.getParentFile());
        }
        
        File selectedFile = fileChooserSaveHemiPhotoOutputTextFile.showSaveDialog(stage);
        
        if(selectedFile != null){
            lastFCOpenHemiPhotoOutputTextFile = selectedFile;
            textfieldHemiPhotoOutputTextFile.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputBitmapFile(ActionEvent event) {
        
        if(lastFCOpenHemiPhotoOutputBitmapFile != null){
            fileChooserSaveHemiPhotoOutputBitmapFile.setInitialDirectory(lastFCOpenHemiPhotoOutputBitmapFile.getParentFile());
        }
        
        File selectedFile = fileChooserSaveHemiPhotoOutputBitmapFile.showSaveDialog(stage);
        
        if(selectedFile != null){
            lastFCOpenHemiPhotoOutputBitmapFile = selectedFile;
            textfieldHemiPhotoOutputBitmapFile.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionMenuItemSelectAllScansHemiPhoto(ActionEvent event) {
        listViewHemiPhotoScans.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemUnselectAllScansHemiPhoto(ActionEvent event) {
        listViewHemiPhotoScans.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemoveScanFromHemiPhotoListView(ActionEvent event) {
        
        ObservableList<LidarScan> selectedItems = listViewHemiPhotoScans.getSelectionModel().getSelectedItems();
        listViewHemiPhotoScans.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionMenuItemSelectAllSceneObjects(ActionEvent event) {
        listviewTreeSceneObjects.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemUnselectAllSceneObjects(ActionEvent event) {
        listviewTreeSceneObjects.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemoveSceneObject(ActionEvent event) {
        
        ObservableList<SceneObjectWrapper> selectedItems = listviewTreeSceneObjects.getSelectionModel().getSelectedItems();
        listviewTreeSceneObjects.getItems().removeAll(selectedItems);
    }

    public void setStage(final Stage stage) {
        
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

    private CheckBox createSelectedCheckbox(String text){
        
        CheckBox c = new CheckBox(text);
        c.setSelected(true);
        return c;
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
//        textFieldPADMax.setText(String.valueOf(5));
//        textFieldPadMax1m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_1M));
//        textFieldPadMax2m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_2M));
//        textFieldPadMax3m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_3M));
//        textFieldPadMax4m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_4M));
//        textFieldPadMax5m.setText(String.valueOf(ProcessingMultiRes.DEFAULT_MAX_PAD_5M));
    }

    private void disableSopMatrixChoice(boolean value) {

        checkboxUseSopMatrix.setDisable(value);
        checkBoxUseDefaultSopMatrix.setDisable(value);
        buttonOpenSopMatrixFile.setDisable(value);
        
    }

    private void disablePopMatrixChoice(boolean value) {

        checkboxUsePopMatrix.setDisable(value);
        checkBoxUseDefaultPopMatrix.setDisable(value);
        buttonOpenPopMatrixFile.setDisable(value);
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

        if (listviewTreeSceneObjects.getItems().size() > 0) {
            
            ObservableList<SceneObjectWrapper> sceneObjects = listviewTreeSceneObjects.getItems();
            
            try {

                Service s = new Service() {

                    @Override
                    protected Task createTask() {
                        return new Task() {

                            @Override
                            protected Object call() throws Exception {

                                Viewer3D viewer3D = new Viewer3D((int) (SCREEN_WIDTH / 4.0d), (int) (SCREEN_HEIGHT / 4.0d), (int) (SCREEN_WIDTH / 1.5d), (int) (SCREEN_HEIGHT / 2.0d), "3d view");
                                viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));

                                for(SceneObjectWrapper sceneObjectWrapper : sceneObjects){
                                    
                                    SceneObject sceneObject = sceneObjectWrapper.getSceneObject();
                                    
                                    /*if(sceneObject instanceof PointCloudSceneObject){
                                        sceneObject.setShader(viewer3D.getScene().colorShader);
                                    }*/
                                    
                                    viewer3D.getScene().addSceneObject(sceneObject);
                                }
                                
                                /**
                                 * *light**
                                 */
                                //scene.setLightPosition(new Point3F(voxelSpace.getPosition().x, voxelSpace.getPosition().y, voxelSpace.getPosition().z + voxelSpace.widthZ + 100));

                                /**
                                 * *camera**
                                 */
                                
                                SceneObject root = sceneObjects.get(0).getSceneObject();
                                TrackballCamera trackballCamera = new TrackballCamera();
                                trackballCamera.setPivot(root);
                                trackballCamera.setLocation(new Vec3F(root.getPosition().x +50, root.getPosition().y, root.getPosition().z+50));
                                viewer3D.getScene().setCamera(trackballCamera);
                                
                                viewer3D.show();                               

                                return null;
                            }
                        };
                    }
                };

                ProgressDialog d = new ProgressDialog(s);
                d.show();

                s.start();

            } catch (Exception ex) {
                logger.error("Cannot launch 3d view", ex);
            }

        } else {
            final File voxelFile = listViewVoxelsFiles.getSelectionModel().getSelectedItem();
            final String attributeToView = comboboxAttributeToView.getSelectionModel().getSelectedItem();

            final boolean drawDTM = checkboxRaster.isSelected();
            final File dtmFile = new File(textfieldRasterFilePath.getText());
            final Mat4D dtmTransfMatrix = MatrixUtility.convertMatrix4dToMat4D(rasterTransfMatrix);
            final boolean fitDTMToVoxelSpace = checkboxFitRasterToVoxelSpace.isSelected();
            final int mntFittingMargin = Integer.valueOf(textfieldRasterFittingMargin.getText());
            final boolean transform = checkboxUseTransformationMatrix.isSelected();

            //window size
            ObservableList<Screen> screens = Screen.getScreens();

            if (screens != null && screens.size() > 0) {
                SCREEN_WIDTH = screens.get(0).getBounds().getWidth();
                SCREEN_HEIGHT = screens.get(0).getBounds().getHeight();
            }

            try {

                Service s = new Service() {

                    @Override
                    protected Task createTask() {
                        return new Task() {

                            @Override
                            protected Object call() throws Exception {

                                Viewer3D viewer3D = new Viewer3D((int) (SCREEN_WIDTH / 4.0d), (int) (SCREEN_HEIGHT / 4.0d), (int) (SCREEN_WIDTH / 1.5d), (int) (SCREEN_HEIGHT / 2.0d), voxelFile.toString());
                                viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));

                                fr.amap.amapvox.voxviewer.object.scene.Scene scene = viewer3D.getScene();

                                /**
                                 * *VOXEL SPACE**
                                 */
                                updateMessage("Loading voxel space: " + voxelFile.getAbsolutePath());

                                VoxelSpaceSceneObject voxelSpace = new VoxelSpaceSceneObject(voxelFile);

                                voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                                    @Override
                                    public void voxelSpaceCreationProgress(int progress) {
                                        updateProgress(progress, 100);
                                    }
                                });

                                voxelSpace.loadVoxels();

                                voxelSpace.changeCurrentAttribut(attributeToView);
                                voxelSpace.setShader(scene.instanceLightedShader);
                                voxelSpace.setDrawType(GLMesh.DrawType.TRIANGLES);
                                scene.addSceneObject(voxelSpace);

                                VoxelFileReader reader = new VoxelFileReader(voxelFile);
                                VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

                                /**
                                 * *DTM**
                                 */
                                if (drawDTM && dtmFile != null) {

                                    updateMessage("Loading DTM");
                                    updateProgress(0, 100);

                                    RegularDtm dtm = DtmLoader.readFromAscFile(dtmFile);

                                    if (transform && dtmTransfMatrix != null) {
                                        dtm.setTransformationMatrix(dtmTransfMatrix);
                                    }

                                    if (fitDTMToVoxelSpace) {

                                        dtm.setLimits(new BoundingBox2F(new Point2F((float) infos.getMinCorner().x, (float) infos.getMinCorner().y),
                                                new Point2F((float) infos.getMaxCorner().x, (float) infos.getMaxCorner().y)), mntFittingMargin);
                                    }

                                    updateMessage("Converting raster to mesh");
                                    dtm.buildMesh();

                                    GLMesh dtmMesh = GLMeshFactory.createMeshAndComputeNormalesFromDTM(dtm);
                                    SceneObject dtmSceneObject = new SimpleSceneObject(dtmMesh, false);
                                    dtmSceneObject.setShader(scene.lightedShader);
                                    scene.addSceneObject(dtmSceneObject);

                                    updateProgress(100, 100);

                                }

                                /**
                                 * *scale**
                                 */
                                updateMessage("Generating scale");
                                final Texture scaleTexture = new Texture(ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                        voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                        viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                        ScaleGradient.Orientation.HORIZONTAL, 5, 8));

                                SceneObject scalePlane = SceneObjectFactory.createTexturedPlane(new Vec3F(40, 20, 0),
                                        (int) (viewer3D.getWidth() - 80),
                                        (int) (viewer3D.getHeight() / 20),
                                        scaleTexture);

                                scalePlane.setShader(scene.texturedShader);
                                scalePlane.setDrawType(GLMesh.DrawType.TRIANGLES);
                                scene.addSceneObject(scalePlane);

                                GLMesh boundingBoxMesh = GLMeshFactory.createBoundingBox((float) infos.getMinCorner().x,
                                        (float) infos.getMinCorner().y,
                                        (float) infos.getMinCorner().z,
                                        (float) infos.getMaxCorner().x,
                                        (float) infos.getMaxCorner().y,
                                        (float) infos.getMaxCorner().z);

                                SceneObject boundingBox = new SimpleSceneObject2(boundingBoxMesh, false);

                                SimpleShader s = (SimpleShader) scene.simpleShader;
                                s.setColor(new Vec3F(1, 0, 0));
                                boundingBox.setShader(s);
                                boundingBox.setDrawType(GLMesh.DrawType.LINES);
                                scene.addSceneObject(boundingBox);

                                voxelSpace.addPropertyChangeListener("gradientUpdated", new PropertyChangeListener() {

                                    @Override
                                    public void propertyChange(PropertyChangeEvent evt) {

                                        BufferedImage image = ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                                voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                                viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                                ScaleGradient.Orientation.HORIZONTAL, 5, 8);

                                        scaleTexture.setBufferedImage(image);
                                    }
                                });

                                /**
                                 * *light**
                                 */
                                scene.setLightPosition(new Point3F(voxelSpace.getPosition().x, voxelSpace.getPosition().y, voxelSpace.getPosition().z + voxelSpace.widthZ + 100));

                                /**
                                 * *camera**
                                 */
                                TrackballCamera trackballCamera = new TrackballCamera();
                                trackballCamera.setPivot(voxelSpace);
                                trackballCamera.setLocation(new Vec3F(voxelSpace.getPosition().x + voxelSpace.widthX, voxelSpace.getPosition().y + voxelSpace.widthY, voxelSpace.getPosition().z + voxelSpace.widthZ));
                                viewer3D.getScene().setCamera(trackballCamera);

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {

                                        final Stage toolBarFrameStage = new Stage();
                                        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ToolBoxFrame.fxml"));

                                        try {
                                            stage.setAlwaysOnTop(false);

                                            Parent root = loader.load();
                                            toolBarFrameStage.setScene(new Scene(root));
                                            toolBarFrameStage.initStyle(StageStyle.UNDECORATED);

                                            toolBarFrameStage.setAlwaysOnTop(true);

                                            ToolBoxFrameController toolBarFrameController = loader.getController();
                                            toolBarFrameController.setStage(toolBarFrameStage);
                                            toolBarFrameStage.setX(viewer3D.getPosition().getX());
                                            toolBarFrameStage.setY(viewer3D.getPosition().getY());
                                            toolBarFrameController.setJoglListener(viewer3D.getJoglContext());
                                            toolBarFrameController.setAttributes(attributeToView, voxelSpace.data.getVoxelSpaceInfos().getColumnNames());

                                            toolBarFrameStage.focusedProperty().addListener(new ChangeListener<Boolean>() {

                                                @Override
                                                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                                                    if (newValue) {
                                                        toolBarFrameStage.setAlwaysOnTop(true);

                                                        toolBarFrameStage.setX(viewer3D.getPosition().getX());
                                                        toolBarFrameStage.setY(viewer3D.getPosition().getY());
                                                    } else if (!viewer3D.isFocused()) {
                                                        toolBarFrameStage.setAlwaysOnTop(false);
                                                    }
                                                }
                                            });

                                            toolBarFrameController.initContent(voxelSpace);
                                            toolBarFrameStage.setAlwaysOnTop(true);

                                            toolBarFrameStage.show();

                                            double maxToolBoxHeight = toolBarFrameStage.getHeight();
                                            viewer3D.getJoglContext().setStartX((int) toolBarFrameStage.getWidth());

                                            viewer3D.addWindowListener(new WindowAdapter() {

                                                @Override
                                                public void windowResized(com.jogamp.newt.event.WindowEvent we) {

                                                    Window window = (Window) we.getSource();
                                                    final int height = window.getHeight();

                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {

                                                            if (height < maxToolBoxHeight) {
                                                                toolBarFrameStage.setHeight(height);
                                                            } else {
                                                                toolBarFrameStage.setHeight(maxToolBoxHeight);
                                                            }

                                                            toolBarFrameStage.setX(viewer3D.getPosition().getX());
                                                            toolBarFrameStage.setY(viewer3D.getPosition().getY());
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void windowMoved(com.jogamp.newt.event.WindowEvent we) {

                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            toolBarFrameStage.setX(viewer3D.getPosition().getX());
                                                            toolBarFrameStage.setY(viewer3D.getPosition().getY());
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void windowDestroyed(com.jogamp.newt.event.WindowEvent we) {

                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            toolBarFrameStage.close();
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void windowGainedFocus(com.jogamp.newt.event.WindowEvent we) {

                                                    viewer3D.setIsFocused(true);

                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {

                                                            if (!toolBarFrameStage.isShowing()) {
                                                                toolBarFrameStage.toFront();
                                                            }

                                                            toolBarFrameStage.setIconified(false);
                                                            toolBarFrameStage.setAlwaysOnTop(true);

                                                            toolBarFrameStage.setX(viewer3D.getPosition().getX());
                                                            toolBarFrameStage.setY(viewer3D.getPosition().getY());
                                                        }
                                                    });
                                                }

                                                @Override
                                                public void windowLostFocus(com.jogamp.newt.event.WindowEvent e) {

                                                    viewer3D.setIsFocused(false);
                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {
                                                            if (!toolBarFrameStage.focusedProperty().get()) {
                                                                toolBarFrameStage.setIconified(true);
                                                                toolBarFrameStage.setAlwaysOnTop(false);
                                                            }
                                                        }
                                                    });
                                                }
                                            });

                                            viewer3D.show();

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
                d.show();

                s.start();

            } catch (Exception ex) {
                logger.error("Cannot launch 3d view", ex);
            }
        }

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
            
            if(selectedFiles.size() > 1){
                checkboxMultiFiles.setSelected(true);
            }else{
                checkboxMultiFiles.setSelected(false);
            }
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
            case 2:
            case 3:
                selectedFile = directoryChooserOpenOutputPathTLS.showDialog(stage);
                break;

            default:
                selectedFile = fileChooserSaveOutputFileTLS.showSaveDialog(stage);
        }

        if (selectedFile != null) {
            textFieldOutputPathTLS.setText(selectedFile.getAbsolutePath());
        }

    }

    @FXML
    private void onActionButtonOpenInputFileTLS(ActionEvent event) {

        /*if (lastFCOpenInputFileTLS != null) {
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
                    
                    try {
                        rsp.read(selectedFile);
                        
                        ArrayList<Scans> rxpList = rsp.getRxpList();
                        items = new ArrayList<>();

                        for (Scans rxp : rxpList) {
                            Map<Integer, RxpScan> scanList = rxp.getScanList();

                            for (Entry scan : scanList.entrySet()) {
                                RxpScan sc = (RxpScan) scan.getValue();
                                items.add(new LidarScan(new File(sc.getAbsolutePath()), MatrixUtility.convertMat4DToMatrix4d(rxp.getSopMatrix())));
                            }
                        }

                        popMatrix = MatrixUtility.convertMat4DToMatrix4d(rsp.getPopMatrix());
                        updateResultMatrix();

                        doFilterOnScanListView();
                        

                    } catch (JDOMException | IOException ex) {
                        showErrorDialog(ex);
                    }

                    break;
                case ".ptg":
                    PTGReader pTGReader = new PTGReader();
                    try {
                        pTGReader.openPTGFile(selectedFile);
                        
                        if(pTGReader.isAsciiFile()){
                            
                            List<File> ptgList = pTGReader.getScanList();
                            
                            items = new ArrayList<>();

                            for (File file : ptgList) {
                                
                                PTGScan pTGScan = new PTGScan();
                                pTGScan.openScanFile(file);
                                
                                items.add(new LidarScan(file, MatrixUtility.convertMat4DToMatrix4d(pTGScan.getHeader().getTransfMatrix())));
                            }
                            
                            updateResultMatrix();
                            listviewRxpScans.getItems().setAll(items);
                        }
                        
                    } catch (IOException ex) {
                        showErrorDialog(ex);
                    } catch (Exception ex) {
                        showErrorDialog(ex);
                    }
                    
                    break;
                default:
                    comboboxModeTLS.getSelectionModel().select(2);
            }
        }*/
        
        File selectedFile = fileChooserOpenInputFileTLS.showOpenDialog(stage);
        
        if(selectedFile != null){
            
            String extension = FileManager.getExtension(selectedFile);

            switch (extension) {
                case ".rxp":
                    
                    textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());
                    
                    comboboxModeTLS.getSelectionModel().select(0);
                    
                    items = new ArrayList<>();
                    items.add(new LidarScan(selectedFile, MatrixUtility.convertMat4DToMatrix4d(Mat4D.identity())));
                    
                    listviewRxpScans.getItems().setAll(items);
                    
                    break;
                case ".rsp":
                    
                    Rsp rsp = new Rsp();
                    try {
                        rsp.read(selectedFile);
                        
                        comboboxModeTLS.getSelectionModel().select(1);
                        
                        textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());

                        rspExtractorFrameController.init(rsp);
                        rspExtractorFrame.show();

                        rspExtractorFrame.setOnHidden(new EventHandler<WindowEvent>() {

                            @Override
                            public void handle(WindowEvent event) {
                                
                                final List<RspExtractorFrameController.Scan> selectedScans = rspExtractorFrameController.getSelectedScans();
                                
                                items = new ArrayList<>();

                                for(RspExtractorFrameController.Scan scan : selectedScans){
                                    items.add(new LidarScan(scan.getFile(), MatrixUtility.convertMat4DToMatrix4d(scan.getSop())));
                                }

                                popMatrix = MatrixUtility.convertMat4DToMatrix4d(rsp.getPopMatrix());
                                updateResultMatrix();
                                
                                listviewRxpScans.getItems().setAll(items);
                            }
                        });

                    } catch (JDOMException | IOException ex) {
                        showErrorDialog(ex);
                    }

                    break;
                case ".ptg":
                    PTGReader pTGReader = new PTGReader();
                    try {
                        
                        pTGReader.openPTGFile(selectedFile);
                        
                        comboboxModeTLS.getSelectionModel().select(3);

                        if(pTGReader.isAsciiFile()){
                            
                            textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());

                            List<File> ptgList = pTGReader.getScanList();

                            items = new ArrayList<>();

                            for (File file : ptgList) {

                                PTGScan pTGScan = new PTGScan();
                                pTGScan.openScanFile(file);

                                items.add(new LidarScan(file, MatrixUtility.convertMat4DToMatrix4d(pTGScan.getHeader().getTransfMatrix())));
                            }

                            updateResultMatrix();
                            listviewRxpScans.getItems().setAll(items);
                        }

                    } catch (IOException ex) {
                        showErrorDialog(ex);
                    } catch (Exception ex) {
                        showErrorDialog(ex);
                    }

                    break;
                default:
            }
            
            

        }
    }
    
    private boolean checkVoxelFile(File voxelFile){
        
        boolean valid = true;
        
        if(voxelFile != null){
            String header = FileManager.readHeader(voxelFile.getAbsolutePath());
            
            if(header != null && header.equals("VOXEL SPACE")){
                
            }else{
                valid = false;
            }
        }else{
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
        
        VoxelFileReader reader;
        try {
            reader = new VoxelFileReader(listViewVoxelsFiles.getSelectionModel().getSelectedItem());
            String[] parameters = reader.getVoxelSpaceInfos().getColumnNames();

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
        } catch (Exception ex) {
            logger.error("Cannot read voxel file", ex);
        }
        
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
                    try {
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
                                    sopMatrix = MatrixUtility.convertMat4DToMatrix4d(rxpScan.getSopMatrix());
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

                    } catch (JDOMException ex) {
                        logger.error("Cannot parse rsp project file", ex);
                    } catch (IOException ex) {
                        logger.error("Cannot read rsp project file", ex);
                    }


                    break;
                default:
                    try {
                        mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                        if (mat != null) {
                            sopMatrix = mat;
                        } else {
                            showMatrixFormatErrorDialog();
                        }
                    } catch (IOException ex) {
                        logger.error("Cannot read matrix file", ex);
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
            Matrix4d mat = null;

            switch (extension) {
                case ".rsp":

                    try {
                        Rsp tempRsp = new Rsp();
                        tempRsp.read(selectedFile);
                        
                        mat = MatrixUtility.convertMat4DToMatrix4d(tempRsp.getPopMatrix());

                        //scan unique
                        if (comboboxModeTLS.getSelectionModel().getSelectedIndex() == 0) {

                            File scanFile = new File(textFieldInputFileTLS.getText());

                            if (Files.exists(scanFile.toPath())) {
                                RxpScan rxpScan = tempRsp.getRxpScanByName(scanFile.getName());
                                if (rxpScan != null) {
                                    sopMatrix = MatrixUtility.convertMat4DToMatrix4d(rxpScan.getSopMatrix());
                                }
                            }
                        }
                    
                    }catch (JDOMException ex) {
                        logger.error("Cannot parse rsp project file", ex);
                    } catch (IOException ex) {
                        logger.error("Cannot read rsp project file", ex);
                    }

                    break;
                default:
                    try {
                        mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                    } catch (IOException ex) {
                        logger.error("Cannot read matrix file", ex);
                    }
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
    
    private List<Integer> getListOfClassificationPointToDiscard(){
        
        List<Integer> pointClassificationsToDiscard = new ArrayList<>();
        List<CheckBox> classifications = listviewClassifications.getItems();
        
        for(CheckBox checkBox : classifications){
            if(!checkBox.isSelected()){

                String s = checkBox.getText().substring(0, checkBox.getText().indexOf("-")-1);
                pointClassificationsToDiscard.add(Integer.valueOf(s));
            }
        }
        
        return pointClassificationsToDiscard;
    }

    private void addFileToTaskList(File file) {

        if (!listViewTaskList.getItems().contains(file)) {
            listViewTaskList.getItems().add(file);
        }
    }

    private void addFileToVoxelList(File file) {

        if (!listViewVoxelsFiles.getItems().contains(file) && Files.exists(file.toPath())) {
            listViewVoxelsFiles.getItems().add(file);
            listViewVoxelsFiles.getSelectionModel().clearSelection();
            listViewVoxelsFiles.getSelectionModel().select(file);
        }
    }
    
    public void addTasksToTaskList(File... tasks){
        
        for(File f : tasks){
            addFileToTaskList(f);
        }
    }
    
    public void executeTaskList(List<File> tasks){
        
        queue = new ArrayBlockingQueue<>(tasks.size());
        queue.addAll(tasks);
        taskNumber = tasks.size();
        taskID = 1;

        try {
            if (!queue.isEmpty()) {
                executeProcess(queue.take());
            }

        } catch (InterruptedException ex) {
            logger.error("Process interrupted", ex);
        }
    }

    @FXML
    private void onActionButtonExecute(ActionEvent event) {

        executeTaskList(listViewTaskList.getItems());
    }
    
    private void showErrorDialog(final Exception e){
        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setContentText(e.getLocalizedMessage());
                alert.setResizable(true);
                alert.getDialogPane().setPrefSize(500, 200);
                alert.initOwner(stage);
                
                TextArea textArea = new TextArea(e.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                
                Label label = new Label("The exception stacktrace was:");

                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                GridPane.setVgrow(textArea, Priority.ALWAYS);
                GridPane.setHgrow(textArea, Priority.ALWAYS);

                GridPane expContent = new GridPane();
                expContent.setMaxWidth(Double.MAX_VALUE);
                expContent.add(label, 0, 0);
                expContent.add(textArea, 0, 1);

                // Set expandable Exception into the dialog pane.
                alert.getDialogPane().setExpandableContent(expContent);

                alert.showAndWait();
            }
        });
        
    }

    private void executeProcess(final File file) {


        try {
            final String type = Configuration.readType(file);
            
            ProgressDialog d;
            final Service<Void> service;
            
            final ProcessTool voxTool = new ProcessTool();
            voxTool.setCoresNumber((int) sliderRSPCoresToUse.getValue());
            
            final long start_time = System.currentTimeMillis();
                    

            service = new Service<Void>() {

                @Override
                protected Task<Void> createTask() {
                    return new Task<Void>() {
                        @Override
                        protected Void call() throws InterruptedException, Exception {

                            final String msgTask = "Task " + taskID + "/" + taskNumber + " :" + file.getAbsolutePath();
                            updateMessage(msgTask);

                            switch (type) {
                                
                                case "transmittance":  
                                    
                                    try {
                                        TransmittanceSim.simulationProcess(TransmittanceCfg.readCfg(file));
                                    } catch (IOException | JDOMException ex) {
                                        logger.error("Error", ex);
                                        showErrorDialog(ex);
                                    }
                                    
                                    break;
                                case "LAI2000":
                                case "LAI2200":
                                    
                                    try {
                                        Lai2xxxSim lai2xxxSim = new Lai2xxxSim(TransmittanceCfg.readCfg(file));
                                        lai2xxxSim.process();
                                        
                                    } catch (IOException | JDOMException ex) {
                                        logger.error("Cannot read configuration file", ex);
                                        showErrorDialog(ex);
                                    }
                                    
                                break;
                                    
                                case "Hemi-Photo":
                                    try {
                                        HemiPhotoCfg hemiphotoCfg = HemiPhotoCfg.readCfg(file);
                                        HemiScanView hemiScanView = new HemiScanView(hemiphotoCfg.getParameters());
                                        hemiScanView.launchSimulation();
                                        
                                    } catch (IOException | JDOMException ex) {
                                        logger.error("Cannot read configuration file", ex);
                                        showErrorDialog(ex);
                                    }
                                    
                                    break;

                                case "merging":
                                    
                                    final VoxMergingCfg voxMergingCfg = new VoxMergingCfg();
                                    
                                    try {
                                        voxMergingCfg.readConfiguration(file);
                                        
                                        voxTool.mergeVoxelFiles(voxMergingCfg/*voxMergingCfg.getFiles(), voxMergingCfg.getOutputFile(), 0, voxMergingCfg.getVoxelParameters().getMaxPAD()*/);

                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                addFileToVoxelList(voxMergingCfg.getOutputFile());
                                                setOnSucceeded(null);
                                            }
                                        });

                                    } catch (JDOMException ex) {
                                        logger.error("Cannot parse configuration file");
                                    } catch (IOException ex) {
                                        logger.error("Cannot read configuration file");
                                    }
                                    
                                    break;

                                case "voxelisation-ALS":

                                    
                                    try {
                                        
                                        final ALSVoxCfg aLSVoxCfg = new ALSVoxCfg();
                                        aLSVoxCfg.readConfiguration(file);
                                        
                                        voxTool.addProcessToolListener(new ProcessToolListener() {

                                            @Override
                                            public void processProgress(String progress, int ratio) {
                                                Platform.runLater(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        updateMessage(msgTask + "\n" + progress);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void processFinished(float duration) {

                                                logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                            }
                                        });

                                        try{
                                            voxTool.voxeliseFromAls(aLSVoxCfg);

                                            Platform.runLater(new Runnable() {

                                                @Override
                                                public void run() {

                                                    addFileToVoxelList(aLSVoxCfg.getOutputFile());
                                                }
                                            });
                                            
                                        }catch(IOException ex){
                                            logger.error(ex.getMessage(), ex);
                                            showErrorDialog(ex);
                                        }catch(Exception ex){
                                            logger.error(ex.getMessage(), ex);
                                            showErrorDialog(ex);
                                        }
                                        
                                        
                                    } catch (Exception ex) {
                                        logger.error(ex.getLocalizedMessage());
                                    }

                                    break;

                                case "voxelisation-TLS":

                                    final TLSVoxCfg tLSVoxCfg = new TLSVoxCfg();
                                    try {
                                        tLSVoxCfg.readConfiguration(file);
                                        
                                        voxTool.addProcessToolListener(new ProcessToolListener() {

                                            @Override
                                            public void processProgress(String progress, int ratio) {
                                                Platform.runLater(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        updateMessage(msgTask + "\n" + progress);
                                                    }
                                                });
                                            }

                                            @Override
                                            public void processFinished(float duration) {

                                                logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                            }
                                        });

                                        switch (tLSVoxCfg.getInputType()) {

                                            case RSP_PROJECT:

                                                try {
                                                    final ArrayList<File> outputFiles = voxTool.voxeliseFromRsp(tLSVoxCfg);

                                                    if (tLSVoxCfg.getVoxelParameters().isMergingAfter()) {

                                                        voxTool.addProcessToolListener(new ProcessToolListener() {

                                                            @Override
                                                            public void processProgress(String progress, int ratio) {
                                                                Platform.runLater(new Runnable() {

                                                                    @Override
                                                                    public void run() {

                                                                        updateMessage(msgTask + "\n" + progress);
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void processFinished(float duration) {

                                                                logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                                            }
                                                        });

                                                        VoxMergingCfg mergingCfg = new VoxMergingCfg(tLSVoxCfg.getVoxelParameters().getMergedFile(), tLSVoxCfg.getVoxelParameters(), outputFiles);

                                                        //if(!voxTool.isCancelled()){
                                                        voxTool.mergeVoxelFiles(mergingCfg/*outputFiles, tLSVoxCfg.getVoxelParameters().getMergedFile(), tLSVoxCfg.getVoxelParameters().getTransmittanceMode(), tLSVoxCfg.getVoxelParameters().getMaxPAD()*/);
                                                        //}

                                                    }

                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {

                                                            if (!voxTool.isCancelled()) {
                                                                for (File file : outputFiles) {
                                                                    addFileToVoxelList(file);
                                                                }
                                                                if (tLSVoxCfg.getVoxelParameters().isMergingAfter()) {
                                                                    addFileToVoxelList(tLSVoxCfg.getVoxelParameters().getMergedFile());
                                                                }
                                                            }
                                                        }
                                                    });

                                                } catch (Exception e) {

                                                }

                                                break;

                                            case RXP_SCAN:

                                                voxTool.voxeliseFromRxp(tLSVoxCfg);

                                                Platform.runLater(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        addFileToVoxelList(tLSVoxCfg.getOutputFile());
                                                    }
                                                });

                                                break;
                                                
                                            case PTG_PROJECT:
                                                
                                                try {
                                                    final ArrayList<File> outputFiles = voxTool.voxeliseFromPTG(tLSVoxCfg);

                                                    if (tLSVoxCfg.getVoxelParameters().isMergingAfter()) {

                                                        voxTool.addProcessToolListener(new ProcessToolListener() {

                                                            @Override
                                                            public void processProgress(String progress, int ratio) {
                                                                Platform.runLater(new Runnable() {

                                                                    @Override
                                                                    public void run() {

                                                                        updateMessage(msgTask + "\n" + progress);
                                                                    }
                                                                });
                                                            }

                                                            @Override
                                                            public void processFinished(float duration) {

                                                                logger.info("Voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                                            }
                                                        });

                                                        VoxMergingCfg mergingCfg = new VoxMergingCfg(tLSVoxCfg.getVoxelParameters().getMergedFile(), tLSVoxCfg.getVoxelParameters(), outputFiles);

                                                        //if(!voxTool.isCancelled()){
                                                        voxTool.mergeVoxelFiles(mergingCfg/*outputFiles, tLSVoxCfg.getVoxelParameters().getMergedFile(), tLSVoxCfg.getVoxelParameters().getTransmittanceMode(), tLSVoxCfg.getVoxelParameters().getMaxPAD()*/);
                                                        //}

                                                    }

                                                    Platform.runLater(new Runnable() {

                                                        @Override
                                                        public void run() {

                                                            if (!voxTool.isCancelled()) {
                                                                for (File file : outputFiles) {
                                                                    addFileToVoxelList(file);
                                                                }
                                                                if (tLSVoxCfg.getVoxelParameters().isMergingAfter()) {
                                                                    addFileToVoxelList(tLSVoxCfg.getVoxelParameters().getMergedFile());
                                                                }
                                                            }
                                                        }
                                                    });

                                                } catch (Exception e) {

                                                }
                                                break;
                                        }
                                    } catch (Exception ex) {
                                        logger.error("Cannot load configuration file", ex);
                                    }

                                    break;

                                case "multi-resolutions":

                                    final MultiResCfg multiResCfg = new MultiResCfg();
                                    
                                    try {
                                        multiResCfg.readConfiguration(file);
                                        
                                        ProcessingMultiRes process = new ProcessingMultiRes(multiResCfg.getMultiResPadMax(), multiResCfg.isMultiResUseDefaultMaxPad());

                                        process.process(multiResCfg.getFiles());
                                        process.write(multiResCfg.getOutputFile());

                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                addFileToVoxelList(multiResCfg.getOutputFile());
                                            }
                                        });
                                    
                                    } catch (JDOMException ex) {
                                        logger.error("Cannot parse configuration file", ex);
                                    } catch (IOException ex) {
                                        logger.error("Cannot read configuration file", ex);
                                    }

                                    

                                    break;

                                case "multi-voxelisation":

                                    MultiVoxCfg multiVoxCfg = new MultiVoxCfg();
                                    try {
                                        multiVoxCfg.readConfiguration(file);
                                        
                                        voxTool.addProcessToolListener(new ProcessToolListener() {

                                            @Override
                                            public void processProgress(String progress, int ratio) {
                                                Platform.runLater(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        updateMessage(msgTask + "\n" + progress);
                                                    }
                                                });

                                            }

                                            @Override
                                            public void processFinished(float duration) {

                                                logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                            }
                                        });
                                        voxTool.multiVoxelisation(multiVoxCfg);

                                    } catch (Exception ex) {
                                        logger.error("Cannot load configuration file");
                                    }
                                    
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
                                logger.error("Task processing was interrupted", ex);
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
            

        } catch (JDOMException | IOException e) {
            
            logger.error("An error occured", e);
            
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
                    logger.error("Tasks processing was interrupted", ex);
                }
            }
        }        

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
        
        voxelParameters.setLadType(comboboxLADChoice.getSelectionModel().getSelectedItem());
            
        if(radiobuttonLADHomogeneous.isSelected()){
            voxelParameters.setLadEstimationMode(0);
        }else{
            voxelParameters.setLadEstimationMode(1);
        }
        
        if(comboboxLADChoice.getSelectionModel().getSelectedItem() == TWO_PARAMETER_BETA){
            try{
                voxelParameters.setLadBetaFunctionAlphaParameter(Float.valueOf(textFieldTwoBetaAlphaParameter.getText()));
                voxelParameters.setLadBetaFunctionBetaParameter(Float.valueOf(textFieldTwoBetaBetaParameter.getText()));
            }catch(Exception ex){
                Exception e = new Exception("Two-parameter beta function selected but alpha and beta parameters are not valid", ex);
                logger.error(e.getMessage(), e);
                showErrorDialog(e);
            }
        }

        voxelParameters.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
        voxelParameters.setTransmittanceMode(0);

        return voxelParameters;
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

            try {

                String type = Configuration.readType(selectedFile);
                
                MultiResCfg cfg1;
                        
                if(type.equals("multi-resolutions")){

                    showErrorDialog(new Exception("This mode is not supported anymore"));
                    
                }else if(type.equals("Hemi-Photo")){
                    
                    tabPaneMain.getSelectionModel().select(1);
                    tabPaneVirtualMeasures.getSelectionModel().select(2);
                    
                    HemiPhotoCfg cfg = HemiPhotoCfg.readCfg(selectedFile);
                    HemiParameters hemiParameters = cfg.getParameters();
                    
                    switch(hemiParameters.getMode()){
                        case ECHOS:
                            listViewHemiPhotoScans.getItems().setAll(hemiParameters.getRxpScansList());
                            break;
                        case PAD:
                            textfieldVoxelFilePathHemiPhoto.setText(hemiParameters.getVoxelFile().getAbsolutePath());
                            textfieldSensorPositionX.setText(String.valueOf(hemiParameters.getSensorPosition().x));
                            textfieldSensorPositionY.setText(String.valueOf(hemiParameters.getSensorPosition().y));
                            textfieldSensorPositionZ.setText(String.valueOf(hemiParameters.getSensorPosition().z));
                            break;
                    }
                    
                    textfieldPixelNumber.setText(String.valueOf(hemiParameters.getPixelNumber()));
                    textfieldAzimutsNumber.setText(String.valueOf(hemiParameters.getAzimutsNumber()));
                    textfieldZenithsNumber.setText(String.valueOf(hemiParameters.getZenithsNumber()));
                    
                    checkboxGenerateSectorsTextFileHemiPhoto.setSelected(hemiParameters.isGenerateTextFile());
                    
                    if(hemiParameters.isGenerateTextFile()){
                        textfieldHemiPhotoOutputTextFile.setText(hemiParameters.getOutputTextFile().getAbsolutePath());
                    }
                    
                    checkboxHemiPhotoGenerateBitmapFile.setSelected(hemiParameters.isGenerateBitmapFile());
                    
                    if(hemiParameters.isGenerateBitmapFile()){
                        comboboxHemiPhotoBitmapOutputMode.getSelectionModel().select(hemiParameters.getBitmapMode().getMode());
                        textfieldHemiPhotoOutputBitmapFile.setText(hemiParameters.getOutputBitmapFile().getAbsolutePath());
                        
                    }
                    
                }else if(type.equals("transmittance") || type.equals("LAI2000") || type.equals("LAI2200")){
                    
                    tabPaneMain.getSelectionModel().select(1);
                    
                    
                    TransmittanceCfg cfg = TransmittanceCfg.readCfg(selectedFile);

                    //cfg.readConfiguration(selectedFile);
                    TransmittanceParameters params = cfg.getParameters();

                    textfieldVoxelFilePathCanopyAnalyzer.setText(params.getInputFile().getAbsolutePath());
                    
                    if(type.equals("transmittance")){
                        tabPaneVirtualMeasures.getSelectionModel().select(0);
                        comboboxChooseDirectionsNumber.getSelectionModel().select(new Integer(params.getDirectionsNumber()));
                    }else{
                        
                        if(type.equals("LAI2000")){
                            toggleButtonLAI2000Choice.setSelected(true);
                        }else if(type.equals("LAI2200")){
                            toggleButtonLAI2200Choice.setSelected(true);
                        }
                        
                        tabPaneVirtualMeasures.getSelectionModel().select(1);
                        
                        comboboxChooseCanopyAnalyzerSampling.getSelectionModel().select(Integer.valueOf(params.getDirectionsNumber()));
                        
                        boolean[] masks = params.getMasks();
                        if(masks != null && masks.length == 5){
                            toggleButtonCanopyAnalyzerRingMask1.setSelected(masks[0]);
                            toggleButtonCanopyAnalyzerRingMask2.setSelected(masks[1]);
                            toggleButtonCanopyAnalyzerRingMask3.setSelected(masks[2]);
                            toggleButtonCanopyAnalyzerRingMask4.setSelected(masks[3]);
                            toggleButtonCanopyAnalyzerRingMask5.setSelected(masks[4]);
                        }
                        
                        checkboxGenerateLAI2xxxFormat.setSelected(params.isGenerateLAI2xxxTypeFormat());
                        
                        List<Point3d> positions = params.getPositions();
                        if(positions != null){
                            listViewCanopyAnalyzerSensorPositions.getItems().setAll(positions);
                        }
                        
                        checkboxGenerateCanopyAnalyzerTextFile.setSelected(params.isGenerateTextFile());
                        if (params.isGenerateTextFile() && params.getTextFile() != null) {
                            textfieldOutputCanopyAnalyzerTextFile.setText(params.getTextFile().getAbsolutePath());
                        }
                        
                    }
                    
                    
                    
                    /*radiobuttonScannerPosFile.setSelected(params.isUseScanPositionsFile());
                    radiobuttonScannerPosSquaredArea.setSelected(!params.isUseScanPositionsFile());

                    if (params.isUseScanPositionsFile()) {
                        textfieldScannerPointsPositionsFile.setText(params.getPointsPositionsFile().getAbsolutePath());
                    } else {

                        if (params.getCenterPoint() != null) {
                            textfieldScannerPosCenterX.setText(String.valueOf(params.getCenterPoint().x));
                            textfieldScannerPosCenterY.setText(String.valueOf(params.getCenterPoint().y));
                            textfieldScannerPosCenterZ.setText(String.valueOf(params.getCenterPoint().z));
                        }

                        textfieldScannerWidthArea.setText(String.valueOf(params.getWidth()));
                        textfieldScannerStepArea.setText(String.valueOf(params.getStep()));
                    }

                    textfieldLatitudeRadians.setText(String.valueOf(params.getLatitudeInDegrees()));
                    data.clear();

                    List<SimulationPeriod> simulationPeriods = params.getSimulationPeriods();

                    if (simulationPeriods != null) {
                        data.addAll(simulationPeriods);
                    }*/

                    checkboxGenerateBitmapFile.setSelected(params.isGenerateBitmapFile());

                    if (params.isGenerateBitmapFile() && params.getBitmapFile() != null) {
                        textfieldOutputBitmapFilePath.setText(params.getBitmapFile().getAbsolutePath());
                    }
                    

                }else if(type.equals("merging")){
                    
                    VoxMergingCfg cfg = new VoxMergingCfg();
                    cfg.readConfiguration(selectedFile);
                    
                    tabPaneVoxelisation.getSelectionModel().select(2);

                    List<File> files = cfg.getFiles();

                    if (files != null) {
                        listViewVoxelsFiles.getItems().addAll(files);
                    }
                    textFieldOutputFileMerging.setText(cfg.getOutputFile().getAbsolutePath());
                    textFieldPADMax.setText(String.valueOf(cfg.getVoxelParameters().getMaxPAD()));
                    
                }else if(type.equals("voxelisation-ALS") || type.equals("voxelisation-TLS") || type.equals("multi-voxelisation")){
                    
                    Configuration cfg;
                    
                    if(type.equals("voxelisation-ALS")){
                       cfg = new ALSVoxCfg();
                    }else if(type.equals("voxelisation-TLS")){
                        cfg = new TLSVoxCfg();
                    }else{
                        cfg = new MultiVoxCfg();
                    }
                    
                    cfg.readConfiguration(selectedFile);
                    
                    //VoxelisationConfiguration cfg = new VoxelisationConfiguration();
                                
                    VoxelParameters voxelParameters = ((VoxCfg)cfg).getVoxelParameters();
                    checkboxGenerateMultiBandRaster.setSelected(voxelParameters.isGenerateMultiBandRaster());

                    if (voxelParameters.isGenerateMultiBandRaster()) {

                        textfieldRasterStartingHeight.setText(String.valueOf(voxelParameters.getRasterStartingHeight()));
                        textfieldRasterHeightStep.setText(String.valueOf(voxelParameters.getRasterHeightStep()));
                        textfieldRasterBandNumber.setText(String.valueOf(voxelParameters.getRasterBandNumber()));
                        textfieldRasterResolution.setText(String.valueOf(voxelParameters.getRasterResolution()));
                        checkboxDiscardVoxelFileWriting.setSelected(voxelParameters.isShortcutVoxelFileWriting());
                    }

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

                    checkboxUsePopMatrix.setSelected(((VoxCfg)cfg).isUsePopMatrix());
                    checkboxUseSopMatrix.setSelected(((VoxCfg)cfg).isUseSopMatrix());
                    checkboxUseVopMatrix.setSelected(((VoxCfg)cfg).isUseVopMatrix());
                    
                    if(type.equals("voxelisation-ALS") || type.equals("multi-voxelisation")){
                        
                        List<Integer> classifiedPointsToDiscard = ((ALSVoxCfg)cfg).getClassifiedPointsToDiscard();

                        for (Integer i : classifiedPointsToDiscard) {
                            listviewClassifications.getItems().get(i).setSelected(false);
                        }
                    }else if(type.equals("voxelisation-TLS")){
                        
                        filteringPaneController.setFilters(((TLSVoxCfg)cfg).getEchoFilters());
                    }

                    textFieldPADMax.setText(String.valueOf(((VoxCfg)cfg).getVoxelParameters().getMaxPAD()));

                    popMatrix = ((VoxCfg)cfg).getPopMatrix();
                    sopMatrix = ((VoxCfg)cfg).getSopMatrix();
                    vopMatrix = ((VoxCfg)cfg).getVopMatrix();

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

                    List<Filter> filters = ((VoxCfg)cfg).getFilters();
                    if (filters != null) {
                        listviewFilters.getItems().clear();
                        listviewFilters.getItems().addAll(filters);
                    }

                    if (((VoxCfg)cfg).getVoxelParameters().getWeighting() == 0) {
                        checkboxEnableWeighting.setSelected(false);
                    } else {
                        checkboxEnableWeighting.setSelected(true);
                        comboboxWeighting.getSelectionModel().select(((VoxCfg)cfg).getVoxelParameters().getWeighting() - 1);
                    }
                    
                    comboboxLADChoice.getSelectionModel().select(voxelParameters.getLadType());
                    radiobuttonLADHomogeneous.setSelected(voxelParameters.getLadEstimationMode() == 0);
                    textFieldTwoBetaAlphaParameter.setText(String.valueOf(voxelParameters.getLadBetaFunctionAlphaParameter()));
                    textFieldTwoBetaBetaParameter.setText(String.valueOf(voxelParameters.getLadBetaFunctionBetaParameter()));
                    
                    if(type.equals("voxelisation-ALS") || type.equals("multi-voxelisation")){
                        
                        tabPaneVoxelisation.getSelectionModel().select(0);
                        textFieldTrajectoryFileALS.setText(((ALSVoxCfg)cfg).getTrajectoryFile().getAbsolutePath());

                        switch (((ALSVoxCfg)cfg).getInputType()) {
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

                        checkboxCalculateGroundEnergy.setSelected(((ALSVoxCfg)cfg).getVoxelParameters().isCalculateGroundEnergy());
                        if (((ALSVoxCfg)cfg).getVoxelParameters().getGroundEnergyFile() != null) {
                            comboboxGroundEnergyOutputFormat.getSelectionModel().select(((ALSVoxCfg)cfg).getVoxelParameters().getGroundEnergyFileFormat());
                            textFieldOutputFileGroundEnergy.setText(((ALSVoxCfg)cfg).getVoxelParameters().getGroundEnergyFile().getAbsolutePath());
                        }

                        if(type.equals("voxelisation-ALS")){
                            
                            textFieldInputFileALS.setText(((ALSVoxCfg)cfg).getInputFile().getAbsolutePath());
                            textFieldOutputFileALS.setText(((ALSVoxCfg)cfg).getOutputFile().getAbsolutePath());
                            checkboxMultiResAfterMode2.setSelected(((ALSVoxCfg)cfg).getVoxelParameters().isCorrectNaNsMode2());
                            textfieldNbSamplingThresholdMultires.setText(String.valueOf(((ALSVoxCfg)cfg).getVoxelParameters().getCorrectNaNsNbSamplingThreshold()));
                            checkboxMultiFiles.setSelected(false);
                            
                        }else if(type.equals("multi-voxelisation")){
                            
                            List<Input> inputs = ((MultiVoxCfg)cfg).getMultiProcessInputs();
                            StringBuilder inputFiles = new StringBuilder();

                            for (Input input : inputs) {
                                inputFiles.append(input.inputFile.getAbsolutePath()).append(";");
                            }

                            textFieldInputFileALS.setText(inputFiles.toString());

                            if (inputs.size() > 0) {
                                textFieldOutputFileALS.setText(inputs.get(0).outputFile.getParentFile().getAbsolutePath());
                                textFieldResolution.setText(String.valueOf(inputs.get(0).voxelParameters.resolution));
                            }
                            
                            checkboxMultiFiles.setSelected(true);
                        }
                    }else{
                        
                        tabPaneVoxelisation.getSelectionModel().select(1);

                        textFieldInputFileTLS.setText(((TLSVoxCfg)cfg).getInputFile().getAbsolutePath());
                        textFieldOutputPathTLS.setText(((TLSVoxCfg)cfg).getOutputFile().getAbsolutePath());

                        switch (((TLSVoxCfg)cfg).getInputType()) {
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

                        checkboxMergeAfter.setSelected(((TLSVoxCfg)cfg).getVoxelParameters().isMergingAfter());

                        if (((TLSVoxCfg)cfg).getVoxelParameters().getMergedFile() != null) {
                            textFieldMergedFileName.setText(((TLSVoxCfg)cfg).getVoxelParameters().getMergedFile().getName());
                        }

                        List<LidarScan> matricesAndFiles = ((TLSVoxCfg)cfg).getMatricesAndFiles();
                        if (matricesAndFiles != null) {
                            items = matricesAndFiles;
                            listviewRxpScans.getItems().setAll(items);
                        }
                    }
                    
                     if(type.equals("voxelisation-ALS") || type.equals("voxelisation-TLS")){

                        textFieldEnterXMin.setText(String.valueOf(voxelParameters.bottomCorner.x));
                        textFieldEnterYMin.setText(String.valueOf(voxelParameters.bottomCorner.y));
                        textFieldEnterZMin.setText(String.valueOf(voxelParameters.bottomCorner.z));

                        textFieldEnterXMax.setText(String.valueOf(voxelParameters.topCorner.x));
                        textFieldEnterYMax.setText(String.valueOf(voxelParameters.topCorner.y));
                        textFieldEnterZMax.setText(String.valueOf(voxelParameters.topCorner.z));

                        textFieldXNumber.setText(String.valueOf(voxelParameters.split.x));
                        textFieldYNumber.setText(String.valueOf(voxelParameters.split.y));
                        textFieldZNumber.setText(String.valueOf(voxelParameters.split.z));

                    }
                }

            } catch (Exception e) {

                logger.error("Configuration file cannot be load", e);
                Alert alert = new Alert(AlertType.CONFIRMATION);
                alert.setHeaderText("Incorrect file");
                alert.setContentText("File is corrupted or cannot be read!\n"
                        + "Do you want to keep it?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.CANCEL) {
                    listViewTaskList.getItems().remove(selectedFile);
                }
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
                logger.error("Cannot load fxml file", ex);
            }

            exportDartFrame.show();
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

            try {
                float[][] ponderationMatrix = MatrixFileParser.getPonderationMatrixFromFile(selectedFile);
            } catch (IOException ex) {
                logger.error("Cannot read ponderation matrix file", ex);
            }
        }
    }

    private BoundingBox3d calculateAutomaticallyMinAndMax(File file, boolean quick) {

        Matrix4d identityMatrix = new Matrix4d();
        identityMatrix.setIdentity();
        
        ProcessTool processTool = new ProcessTool();
        final BoundingBox3d boundingBox = processTool.getBoundingBoxOfPoints(file, resultMatrix, false, getListOfClassificationPointToDiscard());

        return boundingBox;
    }
    
    private void getBoundingBoxOfPoints(final boolean quick){
        
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

                Service<Void> service = new Service<Void>() {

                    @Override
                    protected Task<Void> createTask() {

                        return new Task<Void>() {
                            @Override
                            protected Void call() throws InterruptedException {

                                ProcessTool processTool = new ProcessTool();
                                final BoundingBox3d boundingBox = processTool.getBoundingBoxOfPoints(new File(textFieldInputFileALS.getText()), resultMatrix, quick, getListOfClassificationPointToDiscard());
                                
                                Point3d minPoint = boundingBox.min;
                                Point3d maxPoint = boundingBox.max;
                                
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

    @FXML
    private void onActionButtonAutomatic(ActionEvent event) {

        getBoundingBoxOfPoints(true);
    }

    /*private Point3d[] getLasMinMax(File file) {

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
    }*/

    private void onActionButtonTransformationAutomatic(ActionEvent event) {

        ProcessTool processTool = new ProcessTool();
        
        BoundingBox3d boundingBox = processTool.getALSMinAndMax(new File(textFieldInputFileALS.getText()));

        if (boundingBox != null) {

            Point3d min = boundingBox.min;
            Point3d max = boundingBox.max;

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

        final PointCloudFilterPaneComponent pcfpc = new PointCloudFilterPaneComponent(anchorpanePointCloudFiltering);

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

        Mat4D vopMatrixTmp = MatrixUtility.convertMatrix4dToMat4D(vopMatrix);
        if(vopMatrixTmp == null && checkboxUseVopMatrix.isSelected()){vopMatrixTmp = Mat4D.identity();}
        
        final Mat4D transfMatrix = vopMatrixTmp;
        
        ObservableList<Node> children = anchorpanePointCloudFiltering.getChildren();

        final List<PointCloudFilterPaneComponent> tempList = new ArrayList<>();

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
                                    try {
                                        pc.readFromFile(file, transfMatrix);
                                    } catch (IOException ex) {
                                        
                                    }

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
    
    @FXML
    private void onActionMenuItemUpdate(ActionEvent event) {
                
        
        updaterFrame.show();
        updaterFrameController.load();
        
//        Service<Void> service = new Service<Void>() {
//            @Override
//            protected Task<Void> createTask() {
//                return new Task<Void>() {
//                    @Override
//                    protected Void call() throws InterruptedException {
//
//                            final Updater updater = new Updater();
//                            
//                            updater.connect();
//                            updater.getFileList();
//                            
//                            //updater.update();
//                            
//                            Platform.runLater(new Runnable() {
//
//                                @Override
//                                public void run() {
//                                    Alert alert = new Alert(AlertType.INFORMATION);
//                                    alert.setTitle("Information");
//                                    alert.setHeaderText("About the Update");
//                                    alert.setContentText("AmapVox needs to be restarted in order to apply update!");
//                                    alert.show();
//                                }
//                            });
//                            
//                            return null;
//                    }
//                };
//            }
//        };
//
//        ProgressDialog d = new ProgressDialog(service);
//        d.initOwner(stage);
//        d.show();
//
//        service.start();
        
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
                
                final Service service = new Service() {

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

    private void onActionButtonOpenTransformationMatrixFile(ActionEvent event) {
        
        fileChooserOpenVopMatrixFile.setInitialDirectory(listViewVoxelsFiles.getSelectionModel().getSelectedItem().getParentFile());
        
        File selectedFile = fileChooserOpenVopMatrixFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            
            Matrix4d mat;
            try {
                mat = MatrixFileParser.getMatrixFromFile(selectedFile);
                if (mat != null) {
                
                    rasterTransfMatrix = MatrixFileParser.getMatrixFromFile(selectedFile);
                    if (rasterTransfMatrix == null) {
                        rasterTransfMatrix = new Matrix4d();
                        rasterTransfMatrix.setIdentity();
                    }

                } else {
                    showMatrixFormatErrorDialog();
                }
                
            } catch (IOException ex) {
                logger.error("Cannot read matrix file", ex);
            }
            
        }
    }

    @FXML
    private void onActionButtonOpenRasterFile(ActionEvent event) {
        
        File selectedVoxelFile = listViewVoxelsFiles.getSelectionModel().getSelectedItem();
        if(selectedVoxelFile != null){
            fileChooserOpenDTMFile.setInitialDirectory(selectedVoxelFile.getParentFile());
        }
        
        
        File selectedFile = fileChooserOpenDTMFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldRasterFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonSetTransformationMatrix(ActionEvent event) {
                
        transformationFrameController.reset();
        
        transformationFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                
                if(transformationFrameController.isConfirmed()){
                    
                    rasterTransfMatrix = transformationFrameController.getMatrix();
                
                    if (rasterTransfMatrix == null) {
                        rasterTransfMatrix = new Matrix4d();
                        rasterTransfMatrix.setIdentity();
                    }
                }
                
            }
        });
        
        transformationFrame.show();
    }

    @FXML
    private void onActionButtonOpenVoxelFileTransmittance(ActionEvent event) {
        
        if(lastFCOpenVoxelFile != null){
            fileChooserOpenVoxelFile.setInitialDirectory(lastFCOpenVoxelFile.getParentFile());
        }
        
        File selectedFile = fileChooserOpenVoxelFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            
            lastFCOpenVoxelFile = selectedFile;
            textfieldVoxelFilePathTransmittance.setText(selectedFile.getAbsolutePath());
        }
        
    }

    @FXML
    private void onActionButtonOpenOutputTextFile(ActionEvent event) {
        
        if(lastFCSaveTransmittanceTextFile != null){
            fileChooserSaveTransmittanceTextFile.setInitialFileName(lastFCSaveTransmittanceTextFile.getName());
            fileChooserSaveTransmittanceTextFile.setInitialDirectory(lastFCSaveTransmittanceTextFile.getParentFile());
        }
        
        File selectedFile = fileChooserSaveTransmittanceTextFile.showSaveDialog(stage);
        
        if(selectedFile != null){
            
            lastFCSaveTransmittanceTextFile = selectedFile;
            textfieldOutputTextFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenScannerPointsPositionsFile(ActionEvent event) {
        
        if(lastFCOpenPointsPositionFile != null){
            fileChooserOpenPointsPositionFile.setInitialDirectory(lastFCOpenPointsPositionFile.getParentFile());
        }
        
        File selectedFile = fileChooserOpenPointsPositionFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            lastFCOpenPointsPositionFile = selectedFile;
            
            textfieldScannerPointsPositionsFile.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenOutputBitmapFile(ActionEvent event) {
        
        if(lastDCSaveTransmittanceBitmapFile != null){
            directoryChooserSaveTransmittanceBitmapFile.setInitialDirectory(lastDCSaveTransmittanceBitmapFile);
        }
        
        File selectedFile = directoryChooserSaveTransmittanceBitmapFile.showDialog(stage);
        
        if(selectedFile != null){
            
            lastDCSaveTransmittanceBitmapFile = selectedFile;
            textfieldOutputBitmapFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonAddPeriodToPeriodList(ActionEvent event) {
        
        dateChooserFrameController.reset();
        
        dateChooserFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                
                if(dateChooserFrameController.isConfirmed()){
                    
                    SimulationPeriod period = dateChooserFrameController.getDateRange();
                    
                    if(period != null){
                        data.add(period);
                    }                    
                }
                
            }
        });
        
        dateChooserFrame.show();
    }

    @FXML
    private void onActionButtonRemovePeriodFromPeriodList(ActionEvent event) {
        
        tableViewSimulationPeriods.getItems().removeAll(tableViewSimulationPeriods.getSelectionModel().getSelectedItems());
        
    }

    @FXML
    private void onActionMenuItemSelectAllPeriods(ActionEvent event) {
        tableViewSimulationPeriods.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemUnselectAllPeriods(ActionEvent event) {
        tableViewSimulationPeriods.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void onActionButtonSaveExecuteLightMapTransmittanceSimulation(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        
        if (selectedFile != null) {
            
            TransmittanceParameters transmParameters = new TransmittanceParameters();
        
            if(toggleButtonLAI2000Choice.isSelected() || toggleButtonLAI2200Choice.isSelected()){
                
                transmParameters.setShotNumber(comboboxChooseDirectionsNumber.getSelectionModel().getSelectedItem());
                
                if(toggleButtonLAI2000Choice.isSelected()){
                    transmParameters.setMode(TransmittanceParameters.Mode.LAI2000);
                }else{
                    transmParameters.setMode(TransmittanceParameters.Mode.LAI2200);
                }
                
                transmParameters.setMasks(new boolean[]{toggleButtonCanopyAnalyzerRingMask1.isSelected(),
                                                        toggleButtonCanopyAnalyzerRingMask2.isSelected(),
                                                        toggleButtonCanopyAnalyzerRingMask3.isSelected(),
                                                        toggleButtonCanopyAnalyzerRingMask4.isSelected(),
                                                        toggleButtonCanopyAnalyzerRingMask5.isSelected()});
                
                transmParameters.setGenerateLAI2xxxTypeFormat(checkboxGenerateLAI2xxxTypeFormat.isSelected());
            }
            
            transmParameters.setInputFile(new File(textfieldVoxelFilePathTransmittance.getText()));
            transmParameters.setGenerateBitmapFile(checkboxGenerateBitmapFile.isSelected());
            transmParameters.setGenerateTextFile(checkboxGenerateTextFile.isSelected());
            
            if(checkboxGenerateBitmapFile.isSelected()){
                transmParameters.setBitmapFile(new File(textfieldOutputBitmapFilePath.getText()));
            }
            
            if(checkboxGenerateTextFile.isSelected()){
                transmParameters.setTextFile(new File(textfieldOutputTextFilePath.getText()));
            }
            if(comboboxChooseDirectionsNumber.isEditable()){
                transmParameters.setDirectionsNumber(Integer.valueOf(comboboxChooseDirectionsNumber.getEditor().getText()));
            }else{
                transmParameters.setDirectionsNumber(comboboxChooseDirectionsNumber.getSelectionModel().getSelectedItem());
            }
            
            transmParameters.setLatitudeInDegrees(Float.valueOf(textfieldLatitudeRadians.getText()));
            transmParameters.setUseScanPositionsFile(radiobuttonScannerPosFile.isSelected());
            
            if(radiobuttonScannerPosFile.isSelected()){
                transmParameters.setPointsPositionsFile(new File(textfieldScannerPointsPositionsFile.getText()));
            }else{
                transmParameters.setCenterPoint(new Point3f(Float.valueOf(textfieldScannerPosCenterX.getText()), 
                                                            Float.valueOf(textfieldScannerPosCenterY.getText()),
                                                            Float.valueOf(textfieldScannerPosCenterZ.getText())));
                
                transmParameters.setWidth(Float.valueOf(textfieldScannerWidthArea.getText()));
                transmParameters.setStep(Float.valueOf(textfieldScannerStepArea.getText()));
            }
            
            transmParameters.setSimulationPeriods(tableViewSimulationPeriods.getItems());
            
            TransmittanceCfg cfg = new TransmittanceCfg(transmParameters);
            try {
                cfg.writeConfiguration(selectedFile);
                addFileToTaskList(selectedFile);
            } catch (Exception ex) {
                logger.error("Cannot write configuration file", ex);
            }
        }
    }

    @FXML
    private void onActionButtonAutomaticDeepSearch(ActionEvent event) {
        getBoundingBoxOfPoints(false);
    }

    @FXML
    private void onActionButtonSetVOPMatrix(ActionEvent event) {
        
        transformationFrameController.reset();
        transformationFrameController.fillMatrix(vopMatrix);
        
        transformationFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                
                if(transformationFrameController.isConfirmed()){
                    
                    vopMatrix = transformationFrameController.getMatrix();
                
                    if (vopMatrix == null) {
                        vopMatrix = new Matrix4d();
                        vopMatrix.setIdentity();
                    }
                    
                    updateResultMatrix();
                }
                
            }
        });
        
        transformationFrame.show();
    }

    @FXML
    private void onActionButtonSetupViewCap(ActionEvent event) {
        
        if(toggleButtonLAI2000Choice.isSelected()){
            viewCapsSetupFrameController.setViewCapAngles(ViewCapsSetupFrameController.ViewCaps.LAI_2000);
        }else if(toggleButtonLAI2200Choice.isSelected()){
            viewCapsSetupFrameController.setViewCapAngles(ViewCapsSetupFrameController.ViewCaps.LAI_2200);
        }
        
        viewCapsSetupFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                if(viewCapsSetupFrameController.isConfirmed()){
                    textFieldViewCapAngleCanopyAnalyzer.setText(String.valueOf(viewCapsSetupFrameController.getAngle()));
                }
            }
        });
        
        viewCapsSetupFrame.show();
        
        
    }

    @FXML
    private void onActionButtonAddVoxelFileToListViewForChart(ActionEvent event) {
        
        if (lastFCOpenVoxelFile != null) {
            fileChooserOpenVoxelFile.setInitialDirectory(lastFCOpenVoxelFile.getParentFile());
        }

        List<File> selectedFiles = fileChooserOpenVoxelFile.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            lastFCOpenVoxelFile = selectedFiles.get(0);
            
            for(File file : selectedFiles){
                listViewVoxelsFilesChart.getItems().add(new VoxelFileChart(file, file.getName()));
            }
            
            if(selectedFiles.size() > 0){
                listViewVoxelsFilesChart.getSelectionModel().selectFirst();
            }
        }
        
        
    }

    @FXML
    private void onActionButtonDrawChart(ActionEvent event) {
        
        String chartWindowTitle;
        
        int tabIndex = tabpaneChart.getSelectionModel().getSelectedIndex();
        
        if(tabIndex == 0){
            chartWindowTitle = "Profile chart";
        }else if(tabIndex == 1){
            chartWindowTitle = "Two variables statistics chart";
        }else{
            chartWindowTitle = "Chart";
        }
        
        int maxChartNumberInARow;
        try{
            maxChartNumberInARow = Integer.valueOf(textfieldMaxChartNumberInARow.getText());
        }catch(Exception e){
            maxChartNumberInARow = 6;
        }
        ChartViewer chartViewer = new ChartViewer(chartWindowTitle, 270, 600, maxChartNumberInARow);
        
        for(VoxelFileChart voxelFileChart : listViewVoxelsFilesChart.getItems()){
            voxelFileChart.loaded = true;
        }
        forceListRefreshOn(listViewVoxelsFilesChart);
        
        VoxelFileChart[] voxelFileChartArray = new VoxelFileChart[listViewVoxelsFilesChart.getItems().size()];
        listViewVoxelsFilesChart.getItems().toArray(voxelFileChartArray);
        
        VoxelsToChart voxelsToChart = new VoxelsToChart(voxelFileChartArray);
        
        final int chartWidth = 200;
        
        VoxelsToChart.LayerReference reference;
        if(radiobuttonHeightFromAboveGround.isSelected()){
            reference = VoxelsToChart.LayerReference.FROM_ABOVE_GROUND;
        }else{
            reference = VoxelsToChart.LayerReference.FROM_BELOW_CANOPEE;
        }
        
        float maxPAD;
        try{
            maxPAD = Float.valueOf(textfieldVegetationProfileMaxPAD.getText());
        }catch(Exception e){
            maxPAD = 5;
            textfieldVegetationProfileMaxPAD.setText("5");
        }
        
        if (checkboxMakeQuadrats.isSelected()) {
            
            int splitCount = -1;
            int length = -1;
            
            if(radiobuttonSplitCountForQuadrats.isSelected()){
                try {
                    splitCount = Integer.valueOf(textFieldSplitCountForQuadrats.getText());
                } catch (Exception e) {
                }
            }else{
                try {
                    length = Integer.valueOf(textFieldLengthForQuadrats.getText());
                } catch (Exception e) {
                }
            }
            
            int stageWidth = voxelFileChartArray.length * chartWidth;
            if (stageWidth > SCREEN_WIDTH) {
                chartViewer.getStage().setWidth(SCREEN_WIDTH);
            } else {
                chartViewer.getStage().setWidth(stageWidth);
            }

            VoxelsToChart.QuadratAxis axis;
            switch (comboboxSelectAxisForQuadrats.getSelectionModel().getSelectedIndex()) {
                case 0:
                    axis = VoxelsToChart.QuadratAxis.X_AXIS;
                    break;
                case 1:
                    axis = VoxelsToChart.QuadratAxis.Y_AXIS;
                    break;
                case 2:
                    axis = VoxelsToChart.QuadratAxis.Z_AXIS;
                    break;
                default:
                    axis = VoxelsToChart.QuadratAxis.Y_AXIS;
            }

            voxelsToChart.configureQuadrats(axis, splitCount, length);
            
        }else{
            voxelsToChart.configureQuadrats(QuadratAxis.Y_AXIS, 1, -1);
        }
        
        JFreeChart[] charts = null;

        if (radiobuttonPreDefinedProfile.isSelected()) { 

            if (comboboxPreDefinedProfile.getSelectionModel().getSelectedIndex() == 0) { //vegetation profile
                charts = voxelsToChart.getVegetationProfileCharts(reference, maxPAD);
            }

        } else { //from variable profile

            charts = voxelsToChart.getAttributProfileCharts(
                    comboboxFromVariableProfile.getSelectionModel().getSelectedItem(), reference);
        }

        if(charts != null){
            for (JFreeChart chart : charts) {
                chartViewer.insertChart(chart);
            }
        }

        
        chartViewer.show();
    }

    @FXML
    private void onActionButtonRemoveVoxelFileFromListViewForChart(ActionEvent event) {
        
        ObservableList<VoxelFileChart> selectedItems = listViewVoxelsFilesChart.getSelectionModel().getSelectedItems();
        listViewVoxelsFilesChart.getItems().removeAll(selectedItems);
    }

    private <T> void forceListRefreshOn(ListView<T> lsv) {
        ObservableList<T> items = lsv.<T>getItems();
        lsv.<T>setItems(null);
        lsv.<T>setItems(items);
    }
    
    static class ColorRectCell extends ListCell<VoxelFileChart> {

        @Override
        public void updateItem(VoxelFileChart item, boolean empty) {
            super.updateItem(item, empty);
            
            if (item != null/* && !item.loaded*/) {
                pseudoClassStateChanged(loadedPseudoClass, item.loaded);
                setText(item.label);
            }
        }
    }
    
    private ObservableList<SimulationPeriod> data;
    
    
    private boolean isFileTypeKnown(File file){
        
        String extension = FileManager.getExtension(file);
        
        switch(extension){
            
            case ".las":
            case ".laz":
            case ".asc":
            case ".rsp":
            case ".rxp":
            case ".vox":
            case ".txt":
            case "":
                return true;
            default:
                String header = FileManager.readHeader(file.getAbsolutePath());
            
                if(header != null && header.equals("VOXEL SPACE")){
                    return true;
                }
            
            return false;
        }
    }
    
    
    private void addSceneObjectToList(final File file) throws Exception{
        
        //if(isFileTypeKnown(file)){
            
            SceneObjectWrapper sceneObjectWrapper = new SceneObjectWrapper(file, new ProgressBar(0));
            
            
            String extension = FileManager.getExtension(file);
            
            LazAttributs lazAttributs = new LazAttributs();
            
            
            switch (extension) {

            case ".las":

                listviewTreeSceneObjects.getItems().add(sceneObjectWrapper);

                //select attributs to import in the attributs importer frame
                attributsImporterFrame.show();

                LasReader reader = new LasReader();
                reader.open(file);
                LasHeader header = reader.getHeader();

                LasAttributs lasAttributs = new LasAttributs(header.getPointDataFormatID());

                attributsImporterFrameController.setAttributsList(lasAttributs.getAttributsNames());

                //create scene object and set attributs colors
                attributsImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {

                        if (attributsImporterFrameController.getSelectedAttributs() != null
                                && !attributsImporterFrameController.getSelectedAttributs().isEmpty()) {

                            Service s = new Service() {

                                @Override
                                protected Task createTask() {
                                    return new Task() {

                                        @Override
                                        protected Object call() throws Exception {

                                            lasAttributs.processList(attributsImporterFrameController.getSelectedAttributs());

                                            PointCloudSceneObject sceneObject = new PointCloudSceneObject();

                                            LasReader reader = new LasReader();

                                            try {
                                                reader.open(file);

                                                LasHeader header = reader.getHeader();

                                                sceneObject.setPosition(new Point3F((float) (header.getMinX() + (header.getMaxX() - header.getMinX()) / 2.0),
                                                        (float) (header.getMinY() + (header.getMaxY() - header.getMinY()) / 2.0),
                                                        (float) (header.getMinZ() + (header.getMaxZ() - header.getMinZ()) / 2.0)));

                                                long pointNumber = header.getNumberOfPointrecords();

                                                long step = (1 * pointNumber) / 100;

                                                long count = 0;
                                                long pointsProcessed = 0;

                                                Iterator<PointDataRecordFormat> lasIterator = reader.iterator();

                                                count = 0;
                                                pointsProcessed = 0;

                                                lasIterator = reader.iterator();

                                                while (lasIterator.hasNext()) {

                                                    PointDataRecordFormat point = lasIterator.next();

                                                    sceneObject.addPoint((float) ((point.getX() * header.getxScaleFactor()) + header.getxOffset()),
                                                            (float) ((point.getY() * header.getyScaleFactor()) + header.getyOffset()),
                                                            (float) ((point.getZ() * header.getzScaleFactor()) + header.getzOffset()));

                                                    if (lasAttributs.isExportClassification()) {
                                                        sceneObject.addValue("classification", (float) point.getClassification());
                                                    }

                                                    if (lasAttributs.isExportIntensity()) {
                                                        sceneObject.addValue("intensity", (float) point.getIntensity());
                                                    }

                                                    if (lasAttributs.isExportNumberOfReturns()) {
                                                        sceneObject.addValue("number of returns", (float) point.getNumberOfReturns());
                                                    }

                                                    if (lasAttributs.isExportReturnNumber()) {
                                                        sceneObject.addValue("return number", (float) point.getReturnNumber());
                                                    }

                                                    if (lasAttributs.isExportTime()) {
                                                        sceneObject.addValue("GPS time", (float) point.getGpsTime());
                                                    }

                                                    if (header.getPointDataFormatID() == 2 || header.getPointDataFormatID() == 3) {

                                                        if (lasAttributs.isExportRed() || lasAttributs.isExportGreen() || lasAttributs.isExportBlue()) {

                                                            int red = 0;
                                                            int green = 0;
                                                            int blue = 0;
                                                            if (lasAttributs.isExportRed()) {
                                                                red = ((PointDataRecordFormat2) point).getRed();
                                                            }
                                                            if (lasAttributs.isExportGreen()) {
                                                                green = ((PointDataRecordFormat2) point).getGreen();
                                                            }
                                                            if (lasAttributs.isExportBlue()) {
                                                                blue = ((PointDataRecordFormat2) point).getBlue();
                                                            }

                                                            sceneObject.addValue("RGB color", red, false);
                                                            sceneObject.addValue("RGB color", green, false);
                                                            sceneObject.addValue("RGB color", blue, false);
                                                        }

                                                    }

                                                    count++;
                                                    pointsProcessed++;

                                                    if (count == step) {
                                                        updateProgress(pointsProcessed, pointNumber);
                                                        count = 0;
                                                    }
                                                }

                                                sceneObject.initMesh();
                                                sceneObject.setShader(fr.amap.amapvox.voxviewer.object.scene.Scene.colorShader);
                                                sceneObjectWrapper.setSceneObject(sceneObject);

                                            } catch (Exception ex) {
                                                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                            return null;
                                        }
                                    };
                                }
                            };

                            sceneObjectWrapper.getProgressBar().progressProperty().bind(s.progressProperty());
                            s.start();
                        }
                    }
                });

                break;
            case ".laz":

                listviewTreeSceneObjects.getItems().add(sceneObjectWrapper);

                //select attributs to import in the attributs importer frame
                attributsImporterFrame.show();
                attributsImporterFrameController.setAttributsList(lazAttributs.getAttributsNames());

                //create scene object and set attributs colors
                attributsImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {

                        if (attributsImporterFrameController.getSelectedAttributs() != null
                                && !attributsImporterFrameController.getSelectedAttributs().isEmpty()) {

                            Service s = new Service() {

                                @Override
                                protected Task createTask() {
                                    return new Task() {

                                        @Override
                                        protected Object call() throws Exception {

                                            lazAttributs.processList(attributsImporterFrameController.getSelectedAttributs());

                                            PointCloudSceneObject sceneObject = new PointCloudSceneObject();

                                            LazExtraction lazExtraction = new LazExtraction();
                                            try {
                                                lazExtraction.openLazFile(file);

                                                LasHeader header = lazExtraction.getHeader();

                                                sceneObject.setPosition(new Point3F((float) (header.getMinX() + (header.getMaxX() - header.getMinX()) / 2.0),
                                                        (float) (header.getMinY() + (header.getMaxY() - header.getMinY()) / 2.0),
                                                        (float) (header.getMinZ() + (header.getMaxZ() - header.getMinZ()) / 2.0)));

                                                long pointNumber = header.getNumberOfPointrecords();

                                                long step = (1 * pointNumber) / 100;

                                                long count = 0;
                                                long pointsProcessed = 0;

                                                ColorGradient cg = new ColorGradient(0, 1);
                                                cg.setGradientColor(ColorGradient.GRADIENT_RAINBOW3);
                                                Iterator<LasPoint> lazIterator = lazExtraction.iterator();

                                                count = 0;
                                                pointsProcessed = 0;

                                                /*lazExtraction = new LazExtraction();
                                            lazExtraction.openLazFile(file);
                                            lazIterator = lazExtraction.iterator();*/
                                                while (lazIterator.hasNext()) {

                                                    LasPoint point = lazIterator.next();

                                                    point.x = (point.x * header.getxScaleFactor()) + header.getxOffset();
                                                    point.y = (point.y * header.getyScaleFactor()) + header.getyOffset();
                                                    point.z = (point.z * header.getzScaleFactor()) + header.getzOffset();

                                                    sceneObject.addPoint((float) point.x, (float) point.y, (float) point.z);

                                                    if (lazAttributs.isExportClassification()) {
                                                        sceneObject.addValue("classification", (float) point.classification);
                                                    }

                                                    if (lazAttributs.isExportIntensity()) {
                                                        sceneObject.addValue("intensity", (float) point.i);
                                                    }

                                                    if (lazAttributs.isExportNumberOfReturns()) {
                                                        sceneObject.addValue("number of returns", (float) point.n);
                                                    }

                                                    if (lazAttributs.isExportReturnNumber()) {
                                                        sceneObject.addValue("number of returns", (float) point.r);
                                                    }

                                                    if (lazAttributs.isExportTime()) {
                                                        sceneObject.addValue("GPS time", (float) point.t);
                                                    }

                                                    count++;
                                                    pointsProcessed++;

                                                    if (count == step) {
                                                        updateProgress(pointsProcessed, pointNumber);
                                                        count = 0;
                                                    }
                                                }

                                                lazExtraction.close();

                                                sceneObject.initMesh();
                                                sceneObject.setShader(fr.amap.amapvox.voxviewer.object.scene.Scene.colorShader);
                                                sceneObjectWrapper.setSceneObject(sceneObject);

                                            } catch (Exception ex) {
                                                java.util.logging.Logger.getLogger(MainFrameController.class.getName()).log(Level.SEVERE, null, ex);
                                            }
                                            return null;
                                        }
                                    };
                                }
                            };

                            sceneObjectWrapper.getProgressBar().progressProperty().bind(s.progressProperty());
                            s.start();
                        }
                    }
                });

                break;
            case ".asc":
                break;
            case ".rsp":

                Rsp rsp = new Rsp();
                rsp.read(file);

                rspExtractorFrameController.init(rsp);
                rspExtractorFrame.show();

                rspExtractorFrame.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {

                        final List<RspExtractorFrameController.Scan> selectedScans = rspExtractorFrameController.getSelectedScans();

                        final int lastListIndex = listviewTreeSceneObjects.getItems().size();
                        for (RspExtractorFrameController.Scan scan : selectedScans) {
                            listviewTreeSceneObjects.getItems().add(new SceneObjectWrapper(scan.getFile(), new ProgressBar()));
                        }

                        Service s = new Service() {

                            @Override
                            protected Task createTask() {
                                return new Task() {

                                    @Override
                                    protected Object call() throws Exception {

                                        final List<SceneObject> sceneObjects = new ArrayList<>(selectedScans.size());

                                        for (RspExtractorFrameController.Scan scan : selectedScans) {

                                            PointCloudSceneObject pointCloud = new PointCloudSceneObject();

                                            RxpExtraction reader = new RxpExtraction();
                                            reader.openRxpFile(scan.getFile(), RxpExtraction.REFLECTANCE, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.TIME);
                                            final Iterator<Shot> iterator = reader.iterator();

                                            Mat4D sopMatrix = scan.getSop();

                                            while (iterator.hasNext()) {

                                                Shot shot = iterator.next();

                                                for (int i = 0; i < shot.ranges.length; i++) {

                                                    double range = shot.ranges[i];

                                                    float x = (float) (shot.origin.x + shot.direction.x * range);
                                                    float y = (float) (shot.origin.y + shot.direction.y * range);
                                                    float z = (float) (shot.origin.z + shot.direction.z * range);

                                                    Vec4D transformedPoint = Mat4D.multiply(sopMatrix, new Vec4D(x, y, z, 1));
                                                    pointCloud.addPoint((float) transformedPoint.x, (float) transformedPoint.y, (float) transformedPoint.z);

                                                    float reflectance = shot.reflectances[i];
                                                    float deviation = shot.deviations[i];
                                                    float amplitude = shot.amplitudes[i];
                                                    double time = shot.times[i];

                                                    pointCloud.addValue("reflectance", reflectance);
                                                    pointCloud.addValue("deviation", deviation);
                                                    pointCloud.addValue("amplitude", amplitude);
                                                    pointCloud.addValue("time", (float) time);
                                                }

                                            }

                                            pointCloud.initMesh();
                                            pointCloud.setShader(fr.amap.amapvox.voxviewer.object.scene.Scene.colorShader);
                                            sceneObjects.add(pointCloud);
                                            //updateProgress(count, selectedScans.size());
                                        }

                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                for (int i = 0; i < sceneObjects.size(); i++) {
                                                    SceneObject sceneObject = sceneObjects.get(i);
                                                    listviewTreeSceneObjects.getItems().get(lastListIndex + i).setSceneObject(sceneObject);
                                                    listviewTreeSceneObjects.getItems().get(lastListIndex + i).getProgressBar().setProgress(1);
                                                }
                                            }
                                        });

                                        return null;
                                    }
                                };
                            }
                        };

                        s.start();
                    }
                });

                break;
            case ".rxp":

                attributsImporterFrame.show();
                attributsImporterFrameController.setAttributsList("reflectance", "deviation", "amplitude");

                attributsImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {

                        if (attributsImporterFrameController.getSelectedAttributs() != null
                                && !attributsImporterFrameController.getSelectedAttributs().isEmpty()) {

                            listviewTreeSceneObjects.getItems().add(sceneObjectWrapper);

                            final List<String> selectedAttributs = attributsImporterFrameController.getSelectedAttributs();

                            Service s = new Service() {

                                @Override
                                protected Task createTask() {
                                    return new Task() {

                                        @Override
                                        protected Object call() throws Exception {

                                            boolean importReflectance = false, importDeviation = false, importAmplitude = false, importTime = false;
                                            List<Integer> typeList = new ArrayList<>();

                                            if (selectedAttributs.contains("reflectance")) {
                                                importReflectance = true;
                                                typeList.add(RxpExtraction.REFLECTANCE);
                                            }
                                            if (selectedAttributs.contains("deviation")) {
                                                importDeviation = true;
                                                typeList.add(RxpExtraction.DEVIATION);
                                            }
                                            if (selectedAttributs.contains("amplitude")) {
                                                importAmplitude = true;
                                                typeList.add(RxpExtraction.AMPLITUDE);
                                            }
                                            if (selectedAttributs.contains("time")) {
                                                importTime = true;
                                                typeList.add(RxpExtraction.TIME);
                                            }

                                            final PointCloudSceneObject pointCloud = new PointCloudSceneObject();

                                            RxpExtraction reader = new RxpExtraction();
                                            int[] typeListNative = new int[typeList.size()];

                                            for (int i = 0; i < typeList.size(); i++) {
                                                typeListNative[i] = typeList.get(i);
                                            }

                                            reader.openRxpFile(file, typeListNative);

                                            final Iterator<Shot> iterator = reader.iterator();

                                            Mat4D sopMatrix = Mat4D.identity();

                                            while (iterator.hasNext()) {

                                                Shot shot = iterator.next();

                                                for (int i = 0; i < shot.ranges.length; i++) {

                                                    double range = shot.ranges[i];

                                                    float x = (float) (shot.origin.x + shot.direction.x * range);
                                                    float y = (float) (shot.origin.y + shot.direction.y * range);
                                                    float z = (float) (shot.origin.z + shot.direction.z * range);

                                                    Vec4D transformedPoint = Mat4D.multiply(sopMatrix, new Vec4D(x, y, z, 1));
                                                    pointCloud.addPoint((float) transformedPoint.x, (float) transformedPoint.y, (float) transformedPoint.z);

                                                    if (importReflectance) {
                                                        float reflectance = shot.reflectances[i];
                                                        pointCloud.addValue("reflectance", reflectance);
                                                    }

                                                    if (importDeviation) {
                                                        float deviation = shot.deviations[i];
                                                        pointCloud.addValue("deviation", deviation);
                                                    }

                                                    if (importAmplitude) {
                                                        float amplitude = shot.amplitudes[i];
                                                        pointCloud.addValue("amplitude", amplitude);
                                                    }
                                                }

                                            }

                                            pointCloud.initMesh();
                                            pointCloud.setShader(fr.amap.amapvox.voxviewer.object.scene.Scene.colorShader);

                                            Platform.runLater(new Runnable() {

                                                @Override
                                                public void run() {
                                                    sceneObjectWrapper.setSceneObject(pointCloud);
                                                    sceneObjectWrapper.getProgressBar().setProgress(1);
                                                }
                                            });

                                            return null;
                                        }
                                    };
                                }
                            };

                            s.start();
                        }
                    }
                });

                break;
            case ".vox":

                String fileHeader = FileManager.readHeader(file.getAbsolutePath());

                if (fileHeader != null && fileHeader.equals("VOXEL SPACE")) {
                    
                    final SceneObjectWrapper voxelSpaceScObj = new SceneObjectWrapper(file, new ProgressBar());
                    listviewTreeSceneObjects.getItems().add(voxelSpaceScObj);
                
                    Service s = new Service() {
                        @Override
                        protected Task createTask() {
                            return new Task() {
                                @Override
                                protected Object call() throws Exception {
                                    
                                    //window size
                                    ObservableList<Screen> screens = Screen.getScreens();

                                    if (screens != null && screens.size() > 0) {
                                        SCREEN_WIDTH = screens.get(0).getBounds().getWidth();
                                        SCREEN_HEIGHT = screens.get(0).getBounds().getHeight();
                                    }

                                    VoxelSpaceSceneObject voxelSpace = new VoxelSpaceSceneObject(file);

                                    voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                                        @Override
                                        public void voxelSpaceCreationProgress(int progress) {
                                            updateProgress(progress, 100);
                                        }
                                    });

                                    voxelSpace.loadVoxels();

                                    voxelSpace.changeCurrentAttribut("transmittance");
                                    voxelSpace.setShader(fr.amap.amapvox.voxviewer.object.scene.Scene.instanceLightedShader);
                                    voxelSpace.setDrawType(GLMesh.DrawType.TRIANGLES);
                                    voxelSpace.setPosition(new Point3F(0, 0, 0));
                                    
                                    

                                    VoxelFileReader reader = new VoxelFileReader(file);
                                    VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

                                    
                                    /**
                                     * *scale**
                                     */
                                    
                                    //SceneObjectWrapper scaleScObj = new SceneObjectWrapper(file, new ProgressBar());
                                    
                                    /*updateMessage("Generating scale");
                                    final Texture scaleTexture = new Texture(ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                            voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                            viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                            ScaleGradient.Orientation.HORIZONTAL, 5, 8));

                                    SceneObject scalePlane = SceneObjectFactory.createTexturedPlane(new Vec3F(40, 20, 0),
                                            (int) (viewer3D.getWidth() - 80),
                                            (int) (viewer3D.getHeight() / 20),
                                            scaleTexture);

                                    scalePlane.setShader(scene.texturedShader);
                                    scalePlane.setDrawType(GLMesh.DrawType.TRIANGLES);
                                    
                                    scaleScObj.setSceneObject(scalePlane);
                                    listviewTreeSceneObjects.getItems().add(scaleScObj);

                                    GLMesh boundingBoxMesh = GLMeshFactory.createBoundingBox((float) infos.getMinCorner().x,
                                            (float) infos.getMinCorner().y,
                                            (float) infos.getMinCorner().z,
                                            (float) infos.getMaxCorner().x,
                                            (float) infos.getMaxCorner().y,
                                            (float) infos.getMaxCorner().z);

                                    SceneObject boundingBox = new SimpleSceneObject2(boundingBoxMesh, false);

                                    SimpleShader s = (SimpleShader) scene.simpleShader;
                                    s.setColor(new Vec3F(1, 0, 0));
                                    boundingBox.setShader(s);
                                    boundingBox.setDrawType(GLMesh.DrawType.LINES);
                                    scene.addSceneObject(boundingBox);*/

                                    /*voxelSpace.addPropertyChangeListener("gradientUpdated", new PropertyChangeListener() {

                                        @Override
                                        public void propertyChange(PropertyChangeEvent evt) {

                                            BufferedImage image = ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                                    voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                                    viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                                    ScaleGradient.Orientation.HORIZONTAL, 5, 8);

                                            scaleTexture.setBufferedImage(image);
                                        }
                                    });*/
                                    
                                    voxelSpaceScObj.setSceneObject(voxelSpace);
                                    voxelSpaceScObj.getProgressBar().setProgress(1);
                                    
                                    
                                    return null;
                                }
                            };
                        }
                    };
                    
                    voxelSpaceScObj.getProgressBar().progressProperty().bind(s.progressProperty());
                    s.start();
                }
                break;

            case ".txt":
            default:

                listviewTreeSceneObjects.getItems().add(sceneObjectWrapper);

                textFileParserFrameController.setColumnAssignment(true);
                textFileParserFrameController.setColumnAssignmentValues("Ignore", "X", "Y", "Z", "Red", "Green", "Blue", "Scalar field");

                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 1);
                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 2);
                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 3);
                textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(3, 4);

                textFileParserFrameController.setTextFile(file);

                Stage textFileParserFrame = textFileParserFrameController.getStage();
                textFileParserFrame.show();

                textFileParserFrame.setOnHidden(new EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {

                        String separator = textFileParserFrameController.getSeparator();
                        List<String> columnAssignment = textFileParserFrameController.getAssignedColumnsItems();
                        final int numberOfLines = textFileParserFrameController.getNumberOfLines();

                        final int headerIndex = textFileParserFrameController.getHeaderIndex();
                        int skipNumber = textFileParserFrameController.getSkipLinesNumber();

                        int xIndex = -1, yIndex = -1, zIndex = -1;
                        List<Integer> otherFieldsIndices = new ArrayList<>();

                        for (int i = 0; i < columnAssignment.size(); i++) {

                            String item = columnAssignment.get(i);

                            if (item != null) {
                                switch (item) {
                                    case "X":
                                        xIndex = i;
                                        break;
                                    case "Y":
                                        yIndex = i;
                                        break;
                                    case "Z":
                                        zIndex = i;
                                        break;
                                    case "Scalar field":
                                        otherFieldsIndices.add(i);
                                        break;
                                }
                            }
                        }

                        if (headerIndex != -1) {
                            skipNumber++;
                        }

                        final int finalSkipNumber = skipNumber;
                        final int finalXIndex = xIndex;
                        final int finalYIndex = yIndex;
                        final int finalZIndex = zIndex;

                        Service service = new Service() {

                            @Override
                            protected Task createTask() {
                                return new Task() {

                                    @Override
                                    protected Object call() throws Exception {

                                        PointCloudSceneObject sceneObject = new PointCloudSceneObject();
                                        sceneObject.setPosition(new Point3F(0, 0, 0));

                                        BufferedReader reader;
                                        try {
                                            reader = new BufferedReader(new FileReader(file));

                                            String line;
                                            int count = 0;

                                            for (int i = 0; i < finalSkipNumber; i++) {
                                                reader.readLine();
                                            }

                                            while ((line = reader.readLine()) != null) {

                                                if (count == numberOfLines) {
                                                    break;
                                                }

                                                String[] lineSplitted = line.split(separator);

                                                float x = 0, y = 0, z = 0;
                                                if (finalXIndex != -1) {
                                                    x = Float.valueOf(lineSplitted[finalXIndex]);
                                                }
                                                if (finalYIndex != -1) {
                                                    y = Float.valueOf(lineSplitted[finalYIndex]);
                                                }
                                                if (finalZIndex != -1) {
                                                    z = Float.valueOf(lineSplitted[finalZIndex]);
                                                }

                                                sceneObject.addPoint(x, y, z);

                                                int scalarFieldIndex = 0;

                                                if (!otherFieldsIndices.isEmpty()) {
                                                    for (Integer i : otherFieldsIndices) {
                                                        sceneObject.addValue("Scalar field " + scalarFieldIndex, Float.valueOf(lineSplitted[i]));
                                                        scalarFieldIndex++;
                                                    }
                                                } else {
                                                    sceneObject.addValue("default", count);
                                                }

                                                count++;
                                            }

                                            reader.close();

                                        } catch (FileNotFoundException ex) {
                                            logger.error("Cannot load point file", ex);
                                        } catch (IOException ex) {
                                            logger.error("Cannot load point file", ex);
                                        }

                                        sceneObject.initMesh();
                                        sceneObject.setShader(fr.amap.amapvox.voxviewer.object.scene.Scene.colorShader);

                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                sceneObjectWrapper.setSceneObject(sceneObject);
                                                sceneObjectWrapper.getProgressBar().setProgress(1);
                                            }
                                        });
                                        return null;
                                    }
                                };
                            }
                        };
                        
                        service.start();
                    }
                });

                break;
        }
            
        //}
    }    
    
    private LeafAngleDistribution getLeafAngleDistribution(){
        
        LeafAngleDistribution.Type type = comboboxLADChoice.getSelectionModel().getSelectedItem();
        
        double param1 = 0;
        double param2 = 0;
        
        if(type == ELLIPSOIDAL){
            try{
                param1 = Double.valueOf(textFieldTwoBetaAlphaParameter.getText());
            }catch(Exception e){}
        }
        
        if(type == TWO_PARAMETER_BETA){
        
            try{
                param1 = Double.valueOf(textFieldTwoBetaAlphaParameter.getText());
            }catch(Exception e){}
            
            try{
                param2 = Double.valueOf(textFieldTwoBetaBetaParameter.getText());
            }catch(Exception e){}
            
        }
        
        LeafAngleDistribution distribution = new LeafAngleDistribution(type, param1, param2);
        
        return distribution;
    }
    
    private void displayPDFAllDistributions(){
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        
        LeafAngleDistribution distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ELLIPSOIDAL);
        
        XYSeries serie = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie.add(angleInDegrees, pdf);
        }
        
        //elliptical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ELLIPTICAL);
        
        XYSeries serie2 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie2.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie2);
        
        //erectophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ERECTOPHILE);
        
        XYSeries serie3 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie3.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie3);
        
        //extremophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.EXTREMOPHILE);
        
        XYSeries serie4 = new XYSeries(distribution.getType().toString(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie4.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie4);
        
        //horizontal
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.HORIZONTAL);
        
        XYSeries serie5 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie5.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie5);
        
        //vertical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.VERTICAL);
        
        XYSeries serie6 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie6.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie6);
        
        //plagiophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLAGIOPHILE);
        
        XYSeries serie7 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie7.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie7);
        
        //planophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLANOPHILE);
        
        XYSeries serie8 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie8.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie8);
        
        //spherical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.SPHERIC);
        
        XYSeries serie9 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie9.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie9);
        
        //uniform
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.UNIFORM);
        
        XYSeries serie10 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie10.add(angleInDegrees, pdf);
        }
        
        dataset.addSeries(serie10);
            
        ChartViewer viewer = new ChartViewer("PDF", 500, 500, 1);
        viewer.insertChart(ChartViewer.createBasicChart("f(θL)", dataset, "Leaf inclination angle (degrees)", "PDF"));
        viewer.show();
    }
    
    private void displayGThetaAllDistributions(){
        
        XYSeriesCollection dataset = new XYSeriesCollection();
        
        
        LeafAngleDistribution distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ELLIPSOIDAL);
        
        XYSeries serie = new XYSeries(distribution.getType(), false);
        
        DirectionalTransmittance m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        //elliptical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ELLIPTICAL);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //erectophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ERECTOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //extremophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.EXTREMOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //horizontal
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.HORIZONTAL);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //vertical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.VERTICAL);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //plagiophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLAGIOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //planophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLANOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //spherical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.SPHERIC);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //uniform
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.UNIFORM);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new DirectionalTransmittance(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getTransmittanceFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
            
        ChartViewer viewer = new ChartViewer("GTheta", 500, 500, 1);
        viewer.insertChart(ChartViewer.createBasicChart("GTheta ~ Beam direction zenithal angle", dataset, "Beam direction zenithal angle (degrees)", "GTheta"));
        viewer.show();
    }
}
