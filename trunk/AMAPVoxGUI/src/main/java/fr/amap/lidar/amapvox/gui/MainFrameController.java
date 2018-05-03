/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.lidar.amapvox.gui.update.UpdaterFrameController;
import fr.amap.commons.javafx.io.FileChooserContext;
import fr.amap.lidar.amapvox.gui.export.DartExporterFrameController;
import fr.amap.lidar.amapvox.gui.viewer3d.ToolBoxFrameController;
import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.WindowAdapter;
import fr.amap.lidar.amapvox.gui.task.TaskElement;
import fr.amap.amapvox.als.las.PointDataRecordFormat.Classification;
import fr.amap.commons.javafx.chart.ChartViewer;
import fr.amap.lidar.amapvox.chart.VoxelFileChart;
import fr.amap.lidar.amapvox.chart.VoxelsToChart;
import fr.amap.lidar.amapvox.chart.VoxelsToChart.QuadratAxis;
import fr.amap.lidar.amapvox.commons.Configuration;
import fr.amap.lidar.amapvox.commons.Configuration.InputType;
import static fr.amap.lidar.amapvox.commons.Configuration.InputType.POINTS_FILE;
import static fr.amap.lidar.amapvox.commons.Configuration.InputType.RSP_PROJECT;
import static fr.amap.lidar.amapvox.commons.Configuration.InputType.RXP_SCAN;
import static fr.amap.lidar.amapvox.commons.Configuration.InputType.SHOTS_FILE;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.math.util.BoundingBox3d;
import fr.amap.commons.util.Filter;
import fr.amap.lidar.amapvox.commons.LidarScan;
import fr.amap.commons.math.util.MatrixFileParser;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.commons.math.util.SphericalCoordinates;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.commons.structure.pointcloud.PointCloud;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.RxpScan;
import fr.amap.lidar.amapvox.export.dart.DartPlotsXMLWriter;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.geometry.BoundingBox2F;
import fr.amap.commons.math.geometry.BoundingBox3F;
import fr.amap.lidar.amapvox.simulation.hemi.HemiParameters;
import fr.amap.lidar.amapvox.simulation.hemi.HemiPhotoCfg;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceParameters;
import fr.amap.lidar.amapvox.simulation.transmittance.SimulationPeriod;
import fr.amap.lidar.amapvox.simulation.transmittance.TransmittanceCfg;
import fr.amap.lidar.amapvox.simulation.transmittance.lai2xxx.LAI2200;
import fr.amap.lidar.amapvox.simulation.transmittance.lai2xxx.LAI2xxx;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.commons.GTheta;
import fr.amap.lidar.amapvox.commons.LeafAngleDistribution;
import static fr.amap.lidar.amapvox.commons.LeafAngleDistribution.Type.TWO_PARAMETER_BETA;
import static fr.amap.lidar.amapvox.commons.LeafAngleDistribution.Type.ELLIPSOIDAL;
import fr.amap.lidar.amapvox.voxelisation.LaserSpecification;
import fr.amap.lidar.amapvox.voxelisation.configuration.ALSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.Input;
import fr.amap.lidar.amapvox.voxelisation.configuration.MultiResCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.MultiVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.PTXLidarScan;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import fr.amap.commons.javafx.io.TextFileParserFrameController;
import fr.amap.commons.javafx.matrix.TransformationFrameController;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.util.image.ScaleGradient;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.DTMFilteringParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.GroundEnergyParams;
import fr.amap.lidar.amapvox.commons.LADParams;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpace;
import fr.amap.lidar.amapvox.gui.export.ObjExporterDialogController;
import fr.amap.lidar.amapvox.gui.task.ALSVoxelizationService;
import fr.amap.lidar.amapvox.gui.task.ButterflyRemoverService;
import fr.amap.lidar.amapvox.gui.task.HemiPhotoSimService;
import fr.amap.lidar.amapvox.gui.task.Lai2xxxSimService;
import fr.amap.lidar.amapvox.gui.task.PTGVoxelizationService;
import fr.amap.lidar.amapvox.gui.task.PTXVoxelizationService;
import fr.amap.lidar.amapvox.gui.task.RSPVoxelizationService;
import fr.amap.lidar.amapvox.gui.task.RXPVoxelizationService;
import fr.amap.lidar.amapvox.gui.task.TaskAdapter;
import fr.amap.lidar.amapvox.gui.task.TaskElementExecutor;
import fr.amap.lidar.amapvox.gui.task.TransmittanceSimService;
import fr.amap.lidar.amapvox.gui.task.VoxFileMergingService;
import fr.amap.lidar.amapvox.gui.viewer3d.Viewer3DPanelController;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.NaNsCorrectionParams;
import fr.amap.lidar.amapvox.voxelisation.postproc.ButterflyRemoverCfg;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.css.PseudoClass;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Slider;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;
import org.controlsfx.dialog.ProgressDialog;
import org.controlsfx.validation.ValidationSupport;
import org.jdom2.JDOMException;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import fr.amap.lidar.amapvox.gui.task.ServiceProvider;
import fr.amap.lidar.amapvox.voxelisation.als.PointsToShot;
import fr.amap.lidar.amapvox.voxelisation.postproc.VoxelSpaceUtil;
import fr.amap.viewer3d.SimpleViewer;
import fr.amap.viewer3d.event.EventManager;
import fr.amap.viewer3d.input.InputKeyListener;
import fr.amap.viewer3d.loading.shader.InstanceLightedShader;
import fr.amap.viewer3d.loading.shader.SimpleShader;
import fr.amap.viewer3d.loading.shader.TextureShader;
import fr.amap.viewer3d.loading.texture.StringToImage;
import fr.amap.viewer3d.loading.texture.Texture;
import fr.amap.viewer3d.mesh.GLMesh;
import fr.amap.viewer3d.mesh.GLMeshFactory;
import fr.amap.viewer3d.object.camera.TrackballCamera;
import fr.amap.viewer3d.object.scene.MousePicker;
import fr.amap.viewer3d.object.scene.SceneObject;
import fr.amap.viewer3d.object.scene.SceneObjectFactory;
import fr.amap.viewer3d.object.scene.SceneObjectListener;
import fr.amap.viewer3d.object.scene.SimpleSceneObject;
import fr.amap.lidar.amapvox.gui.viewer3d.VoxelObject;
import fr.amap.lidar.amapvox.gui.viewer3d.VoxelSpaceAdapter;
import fr.amap.lidar.amapvox.gui.viewer3d.VoxelSpaceSceneObject;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.Iterator;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Menu;
import javafx.scene.control.TextArea;
import javafx.stage.StageStyle;
import javax.vecmath.Point3f;
import org.jfree.data.xy.XYDataItem;
import scripts.DanielScript;
import scripts.Script;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MainFrameController implements Initializable {

    
    final Logger logger = LoggerFactory.getLogger(MainFrameController.class);
    
    private Stage stage;
    private ResourceBundle resourceBundle;

    private Stage calculateMatrixFrame;
    private Stage filterFrame;
    private Stage transformationFrame;
    private Stage dateChooserFrame;
    private Stage viewCapsSetupFrame;
    private Stage updaterFrame;
    private Stage attributsImporterFrame;
    private Stage positionImporterFrame;
    private Stage voxelSpaceCroppingFrame;
    
    private UpdaterFrameController updaterFrameController;
    private TransformationFrameController transformationFrameController;
    private DateChooserFrameController dateChooserFrameController;
    private ViewCapsSetupFrameController viewCapsSetupFrameController;
    private AttributsImporterFrameController attributsImporterFrameController;
    private TextFileParserFrameController textFileParserFrameController;
    private FilteringPaneComponentController filteringPaneController;
    private PositionImporterFrameController positionImporterFrameController;
    private VoxelSpaceCroppingFrameController voxelSpaceCroppingFrameController;
    
    private ValidationSupport voxSpaceValidationSupport;
    private ValidationSupport alsVoxValidationSupport;
    private ValidationSupport tlsVoxValidationSupport;
    
    private ValidationSupport transLightMapValidationSupport;
    private ValidationSupport lai2xxxSimValidationSupport;
    private ValidationSupport hemiPhotoSimValidationSupport;
    
    private RiscanProjectExtractor riscanProjectExtractor;
    private PTXProjectExtractor ptxProjectExtractor;
    private PTGProjectExtractor ptgProjectExtractor;
    
    private BlockingQueue<File> queue = new ArrayBlockingQueue<>(100);
    private int taskNumber = 0;
    private int taskID = 1;
    private boolean removeWarnings = false;

    private CalculateMatrixFrameController calculateMatrixFrameController;
    private FilterFrameController filterFrameController;
    
    private ObjExporterDialogController objExporterController;

    private File lastFCOpenInputFileALS;
    private File lastFCOpenTrajectoryFileALS;
    private File lastFCOpenOutputFileALS;
    private File lastFCOpenVoxelFile;
    private File lastFCOpenPopMatrixFile;
    private File lastFCOpenSopMatrixFile;
    private File lastFCOpenDTMFile;
    private File lastFCOpenPointCloudFile;
    private File lastFCAddTask;
    private File lastFCSaveMergingFile;
    private File lastFCSaveTransmittanceTextFile;
    private File lastDCSaveTransmittanceBitmapFile;
    private File lastFCOpenHemiPhotoOutputTextFile;
    
    private CSVFile trajectoryFile;

    private FileChooserContext fcOpenVoxelFileForAreaExtracting;
    private FileChooserContext fcSaveVoxelFileForAreaExtracting;
    private FileChooser fileChooserOpenConfiguration;
    private FileChooserContext fileChooserSaveConfiguration;
    private FileChooser fileChooserOpenInputFileALS;
    private FileChooser fileChooserOpenTrajectoryFileALS;
    private FileChooser fileChooserOpenOutputFileALS;
    private FileChooserContext fileChooserOpenInputFileTLS;
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
    private FileChooser fileChooserSaveHemiPhotoOutputTextFile;
    private FileChooserContext fileChooserSaveHemiPhotoOutputBitmapFile;
    private DirectoryChooser directoryChooserSaveHemiPhotoOutputBitmapFile;
    private DirectoryChooser directoryChooserSaveHemiPhotoOutputTextFile;
    private FileChooserContext fileChooserOpenCanopyAnalyserInputFile;
    private FileChooserContext fileChooserSaveCanopyAnalyserOutputFile;
    private FileChooserContext fileChooserSaveCanopyAnalyserCfgFile;
    private FileChooserContext fileChooserSaveTransmittanceSimCfgFile;
    private FileChooserContext fileChooserVoxMergingList;
    private FileChooserContext fileChooserOpenInputFileButterflyRemover;
    private FileChooserContext fileChooserOpenOutputFileButterflyRemover;
    private FileChooserContext fileChooserChooseOutputCfgFileButterflyRemover;
    
    private DirectoryChooser directoryChooserOpenOutputPathALS;
    private DirectoryChooser directoryChooserOpenOutputPathTLS;

    private Matrix4d popMatrix;
    private Matrix4d sopMatrix;
    private Matrix4d vopMatrix;
    private Matrix4d resultMatrix;
    
    private Matrix4d rasterTransfMatrix;

    private List<LidarScan> items;
    private int tlsVoxNbThreads = -1;
    private Rsp rsp;
    
    static double SCREEN_WIDTH;
    static double SCREEN_HEIGHT;

    private final static String MATRIX_FORMAT_ERROR_MSG = "Matrix file has to look like this: \n\n\t1.0 0.0 0.0 0.0\n\t0.0 1.0 0.0 0.0\n\t0.0 0.0 1.0 0.0\n\t0.0 0.0 0.0 1.0\n";
    private static PseudoClass loadedPseudoClass = PseudoClass.getPseudoClass("loaded");
    
    private ListView<CheckBox> listviewClassifications;
    
    //echo filtering for las, laz files
    private AnchorPane anchorPaneEchoFilteringClassifications;
    
    //echo filtering for rxp files
    private AnchorPane anchorPaneEchoFilteringRxp;
    
    private final HashSet<Point3i> voxelsToRemove = new HashSet<>();
    private boolean editingFrameOpened;
    
    private static String RS_STR_INPUT_TYPE_LAS;
    private static String RS_STR_INPUT_TYPE_LAZ;
    private static String RS_STR_INPUT_TYPE_XYZ;
    private static String RS_STR_INPUT_TYPE_SHOTS;
    private static String RS_STR_OPEN_IMAGE;
    private static String RS_STR_INFO;
    private static String RS_STR_OPEN_CONTAINING_FOLDER;
    private static String RS_STR_EDIT;
    private static String RS_STR_EXPORT;
    
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
    /*@FXML
    private TextField textfieldSensorPositionX;
    @FXML
    private TextField textfieldSensorPositionY;
    @FXML
    private TextField textfieldSensorPositionZ;*/
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
    /*@FXML
    private ListView<SceneObjectWrapper> listviewTreeSceneObjects;*/
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
    private Button buttonOpenTrajectoryFileALS;
    @FXML
    private TableView<SimulationPeriod> tableViewSimulationPeriods;
    @FXML
    private TableColumn<SimulationPeriod,String> tableColumnPeriod;
    @FXML
    private TableColumn<SimulationPeriod, String> tableColumnClearness;
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
    private Button buttonOpenPopMatrixFile;
    /*@FXML
    private ComboBox<String> comboboxWeighting;*/
    @FXML
    private ListView<TaskElement> listViewTaskList;
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
    private TextField textfieldLatitudeRadians;
    @FXML
    private TextField textfieldOutputBitmapFilePath;
    @FXML
    private Button buttonOpenVoxelFileTransmittance;
    @FXML
    private MenuButton menuButtonSelectionPeriodsList;
    @FXML
    private CheckBox checkboxGenerateTextFile;
    @FXML
    private CheckBox checkboxGenerateBitmapFile;
    @FXML
    private AnchorPane anchorpaneBoundingBoxParameters;
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
    private ListView<File> listViewProductsFiles;
    @FXML
    private TextField textFieldInputFileButterflyRemover;
    @FXML
    private TextField textFieldOutputFileButterflyRemover;
    @FXML
    private ListView<Point3d> listViewTransmittanceMapSensorPositions;
    @FXML
    private MenuItem menuItemSelectionNone11;
    @FXML
    private HBox hboxGenerateTextFile;
    @FXML
    private HBox hboxGenerateBitmapFiles;
    @FXML
    private Button buttonExecute;
    @FXML
    private ListView<File> listViewVoxMergingVoxelFiles;
    @FXML
    private MenuItem menuItemSelectionNone111;
    @FXML
    private HBox buttonGroupExecutionTransLightMap;
    @FXML
    private HBox buttongroupExecutionLai2xxxSim;
    @FXML
    private HBox buttonGroupExecutionHemiPhotoSim;
    @FXML
    private Tab tabHemiFromScans;
    @FXML
    private Tab tabHemiFromPAD;
    @FXML
    private Button helpButtonNaNsCorrection;
    @FXML
    private HelpButtonController helpButtonNaNsCorrectionController;
    @FXML
    private Button helpButtonAutoBBox;
    @FXML
    private HelpButtonController helpButtonAutoBBoxController;
    @FXML
    private Button helpButtonHemiPhoto;
    @FXML
    private AnchorPane viewer3DPanel;
    @FXML
    private Viewer3DPanelController viewer3DPanelController;
    @FXML
    private HelpButtonController helpButtonHemiPhotoController;
    @FXML
    private SplitPane splitPaneMain;
    @FXML
    private SplitPane splitPaneVoxelization;
    @FXML
    private TextField textfieldDirectionRotationTransmittanceMap;
    @FXML
    private CheckBox checkboxTransmittanceMapToricity;
    /*@FXML
    private ComboBox<Integer> comboboxTransMode;*/
    /*@FXML
    private ComboBox<String> comboboxPathLengthMode;*/
    @FXML
    private CheckBox checkboxEmptyShotsFilter;
    @FXML
    private Button buttonHelpEmptyShotsFilter;
    @FXML
    private HelpButtonController buttonHelpEmptyShotsFilterController;
    @FXML
    private VoxelSpacePanelController voxelSpacePanelVoxelizationController;
    @FXML
    private CheckBox checkboxWriteShotSegment;
    @FXML
    private ComboBox<String> comboboxScript;
    @FXML
    private TextArea textAreaWeighting;
    @FXML
    private VBox vboxWeighting;
    @FXML
    private HBox hboxAutomaticBBox;
    @FXML
    private ListView<Point3d> listViewHemiPhotoSensorPositions;
    @FXML
    private MenuItem menuItemSelectionNone12;
    @FXML
    private HBox hboxTrajectoryFile;
    @FXML
    private CheckBox checkboxApplyVOPMatrix;
    @FXML
    private VBox vBoxPointCloudFiltering;
    @FXML
    private HBox hBoxPointCloudFiltering;
    @FXML
    private ColorPicker colorPickerSeries;
    
    private void initValidationSupport(){
        
        //voxelization fields validation
        
        voxSpaceValidationSupport = new ValidationSupport();
        alsVoxValidationSupport = new ValidationSupport();
        tlsVoxValidationSupport = new ValidationSupport();
        
        voxSpaceValidationSupport.registerValidator(voxelSpacePanelVoxelizationController.getTextFieldEnterXMin(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpacePanelVoxelizationController.getTextFieldEnterYMin(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpacePanelVoxelizationController.getTextFieldEnterZMin(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpacePanelVoxelizationController.getTextFieldEnterXMax(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpacePanelVoxelizationController.getTextFieldEnterYMax(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(voxelSpacePanelVoxelizationController.getTextFieldEnterZMax(), false, Validators.fieldDoubleValidator);
        voxSpaceValidationSupport.registerValidator(textFieldResolution, false, Validators.fieldDoubleValidator);
        
        if(checkboxUseDTMFilter.isSelected()){
            voxSpaceValidationSupport.registerValidator(textfieldDTMPath, false, Validators.fileExistValidator);
        }
        
        checkboxUseDTMFilter.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    voxSpaceValidationSupport.registerValidator(textfieldDTMPath, false, Validators.fileExistValidator);
                }else{
                    voxSpaceValidationSupport.registerValidator(textfieldDTMPath, false, Validators.unregisterValidator);
                }
            }
        });
        
        alsVoxValidationSupport.registerValidator(textFieldInputFileALS, false, Validators.fileExistValidator);
        alsVoxValidationSupport.registerValidator(textFieldTrajectoryFileALS, false, Validators.fileExistValidator);
        alsVoxValidationSupport.registerValidator(textFieldOutputFileALS, false, Validators.fileValidityValidator);
        
        tlsVoxValidationSupport.registerValidator(textFieldInputFileTLS, false, Validators.fileExistValidator);
        
        tlsVoxValidationSupport.registerValidator(textFieldOutputPathTLS, false, Validators.directoryValidator);
        
        comboboxModeTLS.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                if(newValue.intValue() == 0){
                    tlsVoxValidationSupport.registerValidator(textFieldOutputPathTLS, false, Validators.fileValidityValidator);
                }else{
                    tlsVoxValidationSupport.registerValidator(textFieldOutputPathTLS, false, Validators.directoryValidator);
                }
            }
        });
        
        //transmittance light map fields validation
        
        transLightMapValidationSupport = new ValidationSupport();
        transLightMapValidationSupport.registerValidator(textfieldVoxelFilePathTransmittance, true, Validators.fileExistValidator);
        
        transLightMapValidationSupport.registerValidator(textfieldOutputBitmapFilePath, true, Validators.directoryValidator);
        transLightMapValidationSupport.registerValidator(textfieldDirectionRotationTransmittanceMap, true, Validators.fieldDoubleValidator);
        
        checkboxGenerateBitmapFile.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    transLightMapValidationSupport.registerValidator(textfieldOutputBitmapFilePath, true, Validators.directoryValidator);
                }else{
                    //unregister the validator
                    transLightMapValidationSupport.registerValidator(textfieldOutputBitmapFilePath, false, Validators.unregisterValidator);
                }
            }
        });
        
        checkboxGenerateTextFile.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    transLightMapValidationSupport.registerValidator(textfieldOutputTextFilePath, true, Validators.fileValidityValidator);
                }else{
                    //unregister the validator
                    transLightMapValidationSupport.registerValidator(textfieldOutputTextFilePath, false, Validators.unregisterValidator);
                }
            }
        });
        
        ObservableList<Point3d> content = FXCollections.observableArrayList();
        listViewTransmittanceMapSensorPositions.setItems(content);
        
        
        transLightMapValidationSupport.registerValidator(textfieldLatitudeRadians, true, Validators.fieldDoubleValidator);
        //transLightMapValidationSupport.registerValidator(listViewTransmittanceMapSensorPositions, true, emptyListValidator);
        //transLightMapValidationSupport.registerValidator(tableViewSimulationPeriods, true, emptyTableValidator);
        
        //transLightMapValidationSupport.
        transLightMapValidationSupport.invalidProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                buttonGroupExecutionTransLightMap.setDisable(newValue);
            }
        });
        
        //lai2200 simulations validation support
        lai2xxxSimValidationSupport = new ValidationSupport();
        lai2xxxSimValidationSupport.registerValidator(textfieldVoxelFilePathCanopyAnalyzer, true, Validators.fileExistValidator);
        lai2xxxSimValidationSupport.registerValidator(textfieldOutputCanopyAnalyzerTextFile, true, Validators.fileValidityValidator);
        
        lai2xxxSimValidationSupport.invalidProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                buttongroupExecutionLai2xxxSim.setDisable(newValue);
            }
        });
        
        //hemi photo simulation
        hemiPhotoSimValidationSupport = new ValidationSupport();
        hemiPhotoSimValidationSupport.registerValidator(textfieldPixelNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldAzimutsNumber, true, Validators.fieldIntegerValidator);
        hemiPhotoSimValidationSupport.registerValidator(textfieldZenithsNumber, true, Validators.fieldIntegerValidator);
        /*hemiPhotoSimValidationSupport.registerValidator(textfieldHemiPhotoOutputBitmapFile, true, Validators.fileValidityValidator);
        
        checkboxHemiPhotoGenerateBitmapFile.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    hemiPhotoSimValidationSupport.registerValidator(textfieldHemiPhotoOutputBitmapFile, true, Validators.fileValidityValidator);
                }else{
                    //unregister the validator
                    hemiPhotoSimValidationSupport.registerValidator(textfieldHemiPhotoOutputBitmapFile, false, Validators.unregisterValidator);
                }
            }
        });*/
        
        tabHemiFromScans.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    //unregister the validators
                    hemiPhotoSimValidationSupport.registerValidator(textfieldVoxelFilePathHemiPhoto, false, Validators.unregisterValidator);
                }
            }
        });
        
        tabHemiFromPAD.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    hemiPhotoSimValidationSupport.registerValidator(textfieldVoxelFilePathHemiPhoto, true, Validators.fileExistValidator);
                }
            }
        });
        
        hemiPhotoSimValidationSupport.invalidProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                buttonGroupExecutionHemiPhotoSim.setDisable(newValue);
            }
        });
        
    }
    
    private void initPostProcessTab(){
        
        //initialize voxel file merging panel
        listViewVoxMergingVoxelFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        listViewVoxMergingVoxelFiles.setOnDragOver(DragAndDropHelper.dragOverEvent);
        
        listViewVoxMergingVoxelFiles.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    for (File file : db.getFiles()) {
                        
                        addVoxelFileToMergingList(file);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        
        fileChooserVoxMergingList = new FileChooserContext();
        
        fileChooserOpenInputFileButterflyRemover = new FileChooserContext();
        fileChooserOpenOutputFileButterflyRemover = new FileChooserContext();
        fileChooserChooseOutputCfgFileButterflyRemover = new FileChooserContext();
        
    }
    
    private void initStrings(ResourceBundle rb){
        
        RS_STR_INPUT_TYPE_LAS = rb.getString("las_file");
        RS_STR_INPUT_TYPE_LAZ = rb.getString("laz_file");
        RS_STR_INPUT_TYPE_XYZ = rb.getString("xyz_file");
        RS_STR_INPUT_TYPE_SHOTS = rb.getString("shots_file");
        RS_STR_OPEN_IMAGE = rb.getString("open_image");
        RS_STR_INFO = rb.getString("info");
        RS_STR_EDIT = rb.getString("edit");
        RS_STR_OPEN_CONTAINING_FOLDER = rb.getString("open_containing_folder");
        RS_STR_EXPORT = rb.getString("export");
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        this.resourceBundle = rb;
        
        viewer3DPanelController.setResourceBundle(rb);
        
        initStrings(rb);
        
        colorPickerSeries.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {
            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) {
                if(listViewVoxelsFilesChart.getSelectionModel().getSelectedItems().size() == 1){
                    listViewVoxelsFilesChart.getSelectionModel().getSelectedItem().getSeriesParameters().setColor(new Color(
                            (float)newValue.getRed(), (float)newValue.getGreen(), (float)newValue.getBlue(), 1.0f));
                }
            }
        });
        
        comboboxScript.getItems().setAll("Daniel script");
        
        vboxWeighting.disableProperty().bind(checkboxEnableWeighting.selectedProperty().not());
        
        checkboxEnableWeighting.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue && textAreaWeighting.getText().isEmpty()){
                    
                    int selectedVoxTab = tabPaneVoxelisation.getSelectionModel().getSelectedIndex();
                    
                    if(selectedVoxTab == 0){ //ALS
                        fillWeightingData(EchoesWeightParams.DEFAULT_ALS_WEIGHTING);
                    }else if(selectedVoxTab == 1){ //TLS
                        fillWeightingData(EchoesWeightParams.DEFAULT_TLS_WEIGHTING);
                    }
                }
            }
        });
        
        /*comboboxTransMode.getItems().setAll(1, 2, 3);
        comboboxTransMode.getSelectionModel().selectFirst();
        
        comboboxPathLengthMode.getItems().setAll("A", "B");
        comboboxPathLengthMode.getSelectionModel().selectFirst();*/
        
        helpButtonNaNsCorrection.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                helpButtonNaNsCorrectionController.showHelpDialog(resourceBundle.getString("help_NaNs_correction"));
            }
        });
        
        helpButtonAutoBBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                helpButtonAutoBBoxController.showHelpDialog(resourceBundle.getString("help_bbox"));
            }
        });
                
        helpButtonHemiPhoto.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                helpButtonHemiPhotoController.showHelpDialog(resourceBundle.getString("help_hemiphoto"));
            }
        });
        
        buttonHelpEmptyShotsFilter.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                buttonHelpEmptyShotsFilterController.showHelpDialog(resourceBundle.getString("help_empty_shots_filter"));
            }
        });
        
        /*work around, the divider positions values are defined in the fxml,
        but when the window is initialized the values are lost*/
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                splitPaneMain.setDividerPositions(0.75f);
                splitPaneVoxelization.setDividerPositions(0.45f);
            }
        });
        
        
        initValidationSupport();
        initPostProcessTab();
        
        listViewTransmittanceMapSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        listViewTaskList.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

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
        fileChooserSaveTransmittanceSimCfgFile = new FileChooserContext();
        fileChooserOpenCanopyAnalyserInputFile = new FileChooserContext();
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        
        
        ContextMenu contextMenuProductsList = new ContextMenu();
        MenuItem openImageItem = new MenuItem(RS_STR_OPEN_IMAGE);
        openImageItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                File selectedFile = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                showImage(selectedFile);
            }
        });
        
        Menu menuEdit = new Menu(RS_STR_EDIT);
        
        MenuItem menuItemEditVoxels = new MenuItem("Remove voxels (delete key)");
        MenuItem menuItemFitToContent = new MenuItem("Fit to content");
        MenuItem menuItemCrop = new MenuItem("Crop");
        
        menuEdit.getItems().setAll(menuItemEditVoxels, menuItemFitToContent, menuItemCrop);
        
        menuItemFitToContent.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                 File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    fitVoxelSpaceToContent(selectedItem);
                }
            }
        });
        
        menuItemEditVoxels.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    editVoxelSpace(selectedItem);
                }
            }
        });
        
        menuItemCrop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    try {
                        voxelSpaceCroppingFrameController.setVoxelFile(selectedItem);
                        voxelSpaceCroppingFrame.show();
                    } catch (Exception ex) {
                        showErrorDialog(ex);
                    }
                }
            }
        });
        
        Menu menuExport = new Menu(RS_STR_EXPORT);
        MenuItem menuItemExportDartMaket = new MenuItem("Dart (maket.txt)");
        MenuItem menuItemExportDartPlots = new MenuItem("Dart (plots.xml)");
        MenuItem menuItemExportMeshObj = new MenuItem("Mesh (*.obj)");
        
        menuItemExportDartMaket.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    exportDartMaket(selectedItem);
                }
            }
        });
        
        menuItemExportDartPlots.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    exportDartPlots(selectedItem);
                }
            }
        });
        
        menuItemExportMeshObj.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    exportMeshObj(selectedItem);
                }
            }
        });
        
        menuExport.getItems().setAll(menuItemExportDartMaket, menuItemExportDartPlots, menuItemExportMeshObj);
        
        MenuItem menuItemInfo = new MenuItem(RS_STR_INFO);
        
        menuItemInfo.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                Alert alert = new Alert(AlertType.INFORMATION);
                
                File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    VoxelFileReader reader;
                    try {
                        reader = new VoxelFileReader(selectedItem);
                        VoxelSpaceInfos voxelSpaceInfos = reader.getVoxelSpaceInfos();
                        alert.setTitle("Information");
                        alert.setHeaderText("Voxel space informations");
                        alert.setContentText(voxelSpaceInfos.toString());
                        alert.show();
                    } catch (Exception ex) {
                        showErrorDialog(ex);
                    }
                    
                }
            }
        });
        
        final MenuItem menuItemOpenContainingFolder = new MenuItem(RS_STR_OPEN_CONTAINING_FOLDER);
        
        menuItemOpenContainingFolder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                
                final File selectedItem = listViewProductsFiles.getSelectionModel().getSelectedItem();
                
                if(selectedItem != null){
                    if(Desktop.isDesktopSupported()){
                        new Thread(() -> {
                            try {
                                Desktop.getDesktop().open(selectedItem.getParentFile());
                            } catch (IOException ex) {
                                logger.error("Cannot open directory "+selectedItem);
                            }
                        }).start();
                    }
                    
                }
                
            }
        });
        
        listViewProductsFiles.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                
                if(listViewProductsFiles.getSelectionModel().getSelectedIndices().size() == 1){
                    
                    File selectedFile = listViewProductsFiles.getSelectionModel().getSelectedItem();
                    String extension = FileManager.getExtension(selectedFile);
                    
                    switch(extension){
                        case ".png":
                        case ".bmp":
                        case ".jpg":
                            contextMenuProductsList.getItems().setAll(openImageItem, menuItemOpenContainingFolder);
                            contextMenuProductsList.show(listViewProductsFiles, event.getScreenX(), event.getScreenY());
                            break;
                        case ".vox":
                            
                        default:
                            if(VoxelFileReader.isFileAVoxelFile(selectedFile)){
                                contextMenuProductsList.getItems().setAll(menuItemInfo, menuItemOpenContainingFolder, menuEdit, menuExport);
                                contextMenuProductsList.show(listViewProductsFiles, event.getScreenX(), event.getScreenY());
                            }
                    }
                    
                    
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
                
                if(listViewVoxelsFilesChart.getSelectionModel().getSelectedItems().size() > 1){
                    colorPickerSeries.setDisable(true);
                }else if(listViewVoxelsFilesChart.getSelectionModel().getSelectedItems().size() == 1){
                    
                    VoxelFileChart selectedItem = listViewVoxelsFilesChart.getSelectionModel().getSelectedItem();
                    Color selectedItemColor = selectedItem.getSeriesParameters().getColor();
                    
                    colorPickerSeries.setDisable(false);
                    colorPickerSeries.setValue(new javafx.scene.paint.Color(
                            selectedItemColor.getRed()/255.0,
                            selectedItemColor.getGreen()/255.0,
                            selectedItemColor.getBlue()/255.0,
                            1.0));
                    
                    if(newValue.intValue() >= 0){
                        textfieldLabelVoxelFileChart.setText(listViewVoxelsFilesChart.getItems().get(newValue.intValue()).label);
                    }
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
        comboboxChooseDirectionsNumber.getSelectionModel().select(4);
        
        ToggleGroup scannerPositionsMode = new ToggleGroup();
        
        /*radiobuttonScannerPosSquaredArea.setToggleGroup(scannerPositionsMode);
        radiobuttonScannerPosFile.setToggleGroup(scannerPositionsMode);*/
        
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
        
        checkboxMultiFiles.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                anchorpaneBoundingBoxParameters.setDisable(newValue);
            }
        });
        
        hboxGenerateBitmapFiles.disableProperty().bind(checkboxGenerateBitmapFile.selectedProperty().not());
        hboxGenerateTextFile.disableProperty().bind(checkboxGenerateTextFile.selectedProperty().not());
                 
        fileChooserOpenConfiguration = new FileChooser();
        fileChooserOpenConfiguration.setTitle("Choose configuration file");

        fileChooserSaveConfiguration = new FileChooserContext("cfg.xml");
        fileChooserSaveConfiguration.fc.setTitle("Choose output file");

        fileChooserOpenInputFileALS = new FileChooser();
        fileChooserOpenInputFileALS.setTitle("Open input file");
        fileChooserOpenInputFileALS.getExtensionFilters().addAll(
                new ExtensionFilter("All Files", "*"),
                new ExtensionFilter("Shot files", "*.sht"),
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
        
        fileChooserSaveHemiPhotoOutputBitmapFile = new FileChooserContext("*.png");
        fileChooserSaveHemiPhotoOutputBitmapFile.fc.setTitle("Save bitmap file");
        
        directoryChooserSaveHemiPhotoOutputBitmapFile = new DirectoryChooser();
        directoryChooserSaveHemiPhotoOutputBitmapFile.setTitle("Choose bitmap files output directory");
        
        directoryChooserSaveHemiPhotoOutputTextFile = new DirectoryChooser();
        directoryChooserSaveHemiPhotoOutputTextFile.setTitle("Choose text files output directory");
        
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
            voxelSpaceCroppingFrame = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/VoxelSpaceCroppingFrame.fxml"));
            Parent root = loader.load();
            voxelSpaceCroppingFrameController = loader.getController();
            voxelSpaceCroppingFrame.setScene(new Scene(root));
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/update/UpdaterFrame.fxml"));
            Parent root = loader.load();
            updaterFrameController = loader.getController();
            updaterFrame.setScene(new Scene(root));
        
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }
        
        riscanProjectExtractor = new RiscanProjectExtractor();
        ptxProjectExtractor = new PTXProjectExtractor();
        ptgProjectExtractor = new PTGProjectExtractor();
        
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

        comboboxModeALS.getItems().addAll(RS_STR_INPUT_TYPE_LAS, RS_STR_INPUT_TYPE_LAZ, /*RS_STR_INPUT_TYPE_XYZ, */RS_STR_INPUT_TYPE_SHOTS);
        
        comboboxModeALS.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                if(newValue.equals(RS_STR_INPUT_TYPE_SHOTS)){
                    alsVoxValidationSupport.registerValidator(textFieldTrajectoryFileALS, false, Validators.unregisterValidator);
                }else{
                    alsVoxValidationSupport.registerValidator(textFieldTrajectoryFileALS, false, Validators.fileExistValidator);
                }
            }
        });
        
        comboboxModeTLS.getItems().setAll("Rxp scan", "Rsp project", "PTX","PTG"/*, RS_STR_INPUT_TYPE_XYZ, RS_STR_INPUT_TYPE_SHOTS*/);
        comboboxGroundEnergyOutputFormat.getItems().setAll("txt", "png");

        comboboxLaserSpecification.getItems().addAll(LaserSpecification.getPresets());
        
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
        
        comboboxLaserSpecification.getSelectionModel().select(LaserSpecification.LMS_Q560);
        
        comboboxLaserSpecification.disableProperty().bind(checkboxCustomLaserSpecification.selectedProperty());
        textFieldBeamDiameterAtExit.disableProperty().bind(checkboxCustomLaserSpecification.selectedProperty().not());
        textFieldBeamDivergence.disableProperty().bind(checkboxCustomLaserSpecification.selectedProperty().not());
        
        listViewProductsFiles.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        listViewProductsFiles.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int size = listViewProductsFiles.getSelectionModel().getSelectedIndices().size();

                if (size == 1) {
                    viewer3DPanelController.updateCurrentVoxelFile(listViewProductsFiles.getSelectionModel().getSelectedItem());
                }
            }
        });

        listViewTaskList.getSelectionModel().selectedIndexProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                int size = listViewTaskList.getSelectionModel().getSelectedIndices().size();

                if (size == 1) {
                    buttonLoadSelectedTask.setDisable(false);
                }else {
                    buttonLoadSelectedTask.setDisable(true);
                }
                
                buttonExecute.setDisable(size == 0);
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
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/export/ObjExporterDialog.fxml"));
            Parent root = loader.load();
            objExporterController = loader.getController();
            Stage s = new Stage();
            objExporterController.setStage(s);
            s.setScene(new Scene(root));
        } catch (IOException ex) {
            logger.error("Cannot load fxml file", ex);
        }

        textFieldResolution.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                voxelSpacePanelVoxelizationController.setResolution(Float.valueOf(newValue));
            }
        });
        
        textFieldResolution.textProperty().addListener(voxelSpacePanelVoxelizationController.getChangeListener());
        
        
        checkboxUseDTMFilter.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                if (checkboxUseDTMFilter.isSelected()) {
                    buttonOpenDTMFile.setDisable(false);
                    textfieldDTMPath.setDisable(false);
                    textfieldDTMValue.setDisable(false);
                    checkboxApplyVOPMatrix.setDisable(false);
                    labelDTMValue.setDisable(false);
                    labelDTMPath.setDisable(false);
                } else {
                    buttonOpenDTMFile.setDisable(true);
                    textfieldDTMPath.setDisable(true);
                    textfieldDTMValue.setDisable(true);
                    checkboxApplyVOPMatrix.setDisable(true);
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
                
                if(newValue.intValue() == 0 || newValue.intValue() == 1){
                    checkboxEmptyShotsFilter.setDisable(false);
                }else{
                    checkboxEmptyShotsFilter.setDisable(true);
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
                        checkboxEmptyShotsFilter.setDisable(false);
                        break;

                    default:
                        disableSopMatrixChoice(true);
                        disablePopMatrixChoice(true);
                        checkboxEmptyShotsFilter.setDisable(true);
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
                        hboxAutomaticBBox.setDisable(false);
                        break;
                    default:
                        anchorPaneGroundEnergyParameters.setDisable(true);
                        checkboxCalculateGroundEnergy.setDisable(true);
                        anchorPaneEchoFiltering.getChildren().set(0, anchorPaneEchoFilteringRxp);
                        //anchorPaneEchoFilteringClassifications.setVisible(false);
                        anchorpaneBoundingBoxParameters.setDisable(false);
                        hboxAutomaticBBox.setDisable(true);
                }
            }
        });

        int availableCores = Runtime.getRuntime().availableProcessors();

        sliderRSPCoresToUse.setMin(1);
        sliderRSPCoresToUse.setMax(availableCores);
        sliderRSPCoresToUse.setValue(availableCores);

        textFieldInputFileALS.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textFieldTrajectoryFileALS.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textFieldOutputFileALS.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textFieldInputFileTLS.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textFieldOutputFileMerging.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldDTMPath.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textFieldOutputFileGroundEnergy.setOnDragOver(DragAndDropHelper.dragOverEvent);
        listViewTaskList.setOnDragOver(DragAndDropHelper.dragOverEvent);
        listViewProductsFiles.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldVoxelFilePathTransmittance.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldOutputTextFilePath.setOnDragOver(DragAndDropHelper.dragOverEvent);
        textfieldOutputBitmapFilePath.setOnDragOver(DragAndDropHelper.dragOverEvent);

        textFieldInputFileALS.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1) {
                    success = true;
                    for (File file : db.getFiles()) {
                        if (file != null) {
                            textFieldInputFileALS.setText(file.getAbsolutePath());
                            selectALSInputMode(file);
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        textFieldTrajectoryFileALS.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1) {
                    success = true;
                    for (File file : db.getFiles()) {
                        if (file != null) {
                            onTrajectoryFileChoosed(file);
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        textFieldInputFileTLS.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1) {
                    success = true;
                    for (File file : db.getFiles()) {
                        if (file != null) {
                            onInputFileTLSChoosed(file);
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        setDragDroppedSingleFileEvent(textFieldOutputFileALS);
        setDragDroppedSingleFileEvent(textFieldOutputFileMerging);
        setDragDroppedSingleFileEvent(textfieldDTMPath);
        setDragDroppedSingleFileEvent(textFieldOutputFileGroundEnergy);
        setDragDroppedSingleFileEvent(textfieldVoxelFilePathTransmittance);
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

        
        
        listViewProductsFiles.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    for (File file : db.getFiles()) {
                        addFileToProductsList(file);
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });

        listViewProductsFiles.setOnDragDetected(new EventHandler<MouseEvent>() {

            @Override
            public void handle(MouseEvent event) {

                Dragboard db = listViewProductsFiles.startDragAndDrop(TransferMode.COPY);

                ClipboardContent content = new ClipboardContent();
                content.putFiles(listViewProductsFiles.getSelectionModel().getSelectedItems());
                db.setContent(content);

                event.consume();
            }
        });

        addPointcloudFilterComponent();

        checkboxUsePointcloudFilter.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {

                hBoxPointCloudFiltering.setDisable(!newValue);
                
                ObservableList<Node> list = vBoxPointCloudFiltering.getChildren();
                for (Node n : list) {
                    if (n instanceof PointCloudFilterPaneComponent) {

                        PointCloudFilterPaneComponent panel = (PointCloudFilterPaneComponent) n;
                        panel.disableContent(!newValue);
                    }
                }

                buttonAddPointcloudFilter.setDisable(!newValue);

            }
        });
        
        //displayGThetaAllDistributions();
    }
    
    @FXML
    private void onActionButtonOpenVoxelFileCanopyAnalyzer(ActionEvent event) {
        
        File selectedFile = fileChooserOpenCanopyAnalyserInputFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldVoxelFilePathCanopyAnalyzer.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionMenuItemPositionsHemiPhotoSelectionAll(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemPositionsCanopyAnalyzerSelectionNone(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().clearSelection();
    }
    
    @FXML
    private void onActionMenuItemPositionsCanopyAnalyzerSelectionAll(ActionEvent event) {
        listViewHemiPhotoSensorPositions.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemPositionsHemiPhotoSelectionNone(ActionEvent event) {
        listViewHemiPhotoSensorPositions.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemovePositionCanopyAnalyzer(ActionEvent event) {
        ObservableList<?> selectedItems = listViewCanopyAnalyzerSensorPositions.getSelectionModel().getSelectedItems();
        listViewCanopyAnalyzerSensorPositions.getItems().removeAll(selectedItems);
    }
    
    @FXML
    private void onActionButtonRemovePositionHemiPhoto(ActionEvent event) {
        ObservableList<?> selectedItems = listViewHemiPhotoSensorPositions.getSelectionModel().getSelectedItems();
        listViewHemiPhotoSensorPositions.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionButtonAddPositionCanopyAnalyzer(ActionEvent event) {
        
        if(!textfieldVoxelFilePathCanopyAnalyzer.getText().isEmpty()){
            File voxelFile = new File(textfieldVoxelFilePathCanopyAnalyzer.getText());
            if(voxelFile != null && voxelFile.exists()){
                positionImporterFrameController.setInitialVoxelFile(voxelFile);
            }
        }
        
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                listViewCanopyAnalyzerSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
            }
        });
    }
    
    @FXML
    private void onActionButtonAddPositionHemiPhoto(ActionEvent event) {
        
        if(!textfieldVoxelFilePathHemiPhoto.getText().isEmpty()){
            File voxelFile = new File(textfieldVoxelFilePathHemiPhoto.getText());
            if(voxelFile != null && voxelFile.exists()){
                positionImporterFrameController.setInitialVoxelFile(voxelFile);
            }
        }
        
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                listViewHemiPhotoSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
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
        
        TaskElement taskElement = addFileToTaskList(temporaryFile);
        if(taskElement != null){
            executeProcess(taskElement);
        }
        
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

        transmParameters.setDirectionsNumber(comboboxChooseCanopyAnalyzerSampling.getSelectionModel().getSelectedItem());

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
        transmParameters.setGenerateTextFile(checkboxGenerateCanopyAnalyzerTextFile.isSelected());

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
            cfg.writeConfiguration(file, Global.buildVersion);
            
        } catch (Exception ex) {
            throw ex;
        }
    }
    
    private void saveHemiPhotoSimulation(File file) throws Exception{
        
        if (file != null) {
            
            HemiParameters hemiParameters = new HemiParameters();
                
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
                    hemiParameters.setSensorPositions(listViewHemiPhotoSensorPositions.getItems());
                    break;
            }

            hemiParameters.setGenerateBitmapFile(checkboxHemiPhotoGenerateBitmapFile.isSelected());

            if(checkboxHemiPhotoGenerateBitmapFile.isSelected()){
                
                File outputBitmapFile = new File(textfieldHemiPhotoOutputBitmapFile.getText());
                if(selectedMode == 1 && !outputBitmapFile.isDirectory()){
                    throw new Exception("The selected output bitmap directory is not a directory !");
                }else if(selectedMode == 0 && outputBitmapFile.isDirectory()){
                    throw new Exception("The selected output bitmap file is not a file!");
                }
                
                hemiParameters.setOutputBitmapFile(outputBitmapFile);

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
                
                File outputTextFile = new File(textfieldHemiPhotoOutputTextFile.getText());
                if(selectedMode == 1 && !outputTextFile.isDirectory()){
                    throw new Exception("The selected output text directory is not a directory !");
                }else if(selectedMode == 0 && outputTextFile.isDirectory()){
                    throw new Exception("The selected output text file is not a file!");
                }
                
                hemiParameters.setOutputTextFile(outputTextFile);
            }

            HemiPhotoCfg hemiPhotoCfg = new HemiPhotoCfg(hemiParameters);
            hemiPhotoCfg.writeConfiguration(file, Global.buildVersion);
        }
    }

    @FXML
    private void onActionButtonSaveExecuteCanopyAnalyzerSimulation(ActionEvent event) {
        
        File selectedFile;
        
        try {
            
            selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
            if (selectedFile != null) {

                saveCanopyAnalyzerSimulation(selectedFile);
                
                TaskElement taskElement = addFileToTaskList(selectedFile);
                if(taskElement != null){
                    executeProcess(taskElement);
                }
            }
            
        } catch (Exception ex) {
            logger.error("Cannot write simulation file", ex);
            return;
        }        
    }
    
    private boolean checkALSVoxelizationParametersValidity(){
        
        if(voxSpaceValidationSupport.isInvalid()){
            voxSpaceValidationSupport.initInitialDecoration();
        }
        
        if(alsVoxValidationSupport.isInvalid()){
            alsVoxValidationSupport.initInitialDecoration();
        }

        if (alsVoxValidationSupport.isInvalid() || voxSpaceValidationSupport.isInvalid()) {

            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Check entries");
            alert.setContentText("Some parameters are not set,\nplease fill the missing arguments");

            alert.showAndWait();

            return false;
        }

        if (checkboxCalculateGroundEnergy.isSelected() && !checkboxUseDTMFilter.isSelected()) {

            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setResizable(true);
            alert.setTitle("INFORMATION");
            alert.setHeaderText("Inconsistency");
            alert.setContentText("Calculation of ground energy is enabled\nbut a DTM filter is required!");

            alert.showAndWait();
            
            return false;
        }
        
        return true;
    }
    
    private boolean saveALSVoxelization(File selectedFile){
        
        if(!checkALSVoxelizationParametersValidity()){
            return false;
        }
            
        VoxelParameters voxelParameters = new VoxelParameters();

        if(!checkboxMultiFiles.isSelected()){ 
            voxelParameters = getVoxelParametersFromUI();
        }

        boolean correctNaNs = checkboxMultiResAfterMode2.isSelected();

        if(correctNaNs){
            voxelParameters.setNaNsCorrectionParams(new NaNsCorrectionParams(
                    Float.valueOf(textfieldNbSamplingThresholdMultires.getText())));
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
                it = InputType.SHOTS_FILE;
                break;
            case 3:
                it = InputType.POINTS_FILE;
                break;
            default:
                it = InputType.LAS_FILE;
        }

        voxelParameters.getDtmFilteringParams().setActivate(checkboxUseDTMFilter.isSelected());
        if(checkboxUseDTMFilter.isSelected()){
            voxelParameters.getDtmFilteringParams().setMinDTMDistance(Float.valueOf(textfieldDTMValue.getText()));
            voxelParameters.getDtmFilteringParams().setDtmFile(new File(textfieldDTMPath.getText()));
        }

        voxelParameters.infos.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));

        EchoesWeightParams echoesWeightingParams = new EchoesWeightParams();
        
        if (checkboxEnableWeighting.isSelected()) {
            echoesWeightingParams.setWeightingMode(EchoesWeightParams.WEIGHTING_ECHOS_NUMBER);
            echoesWeightingParams.setWeightingData(parseWeightingData());
        } else {
            echoesWeightingParams.setWeightingMode(EchoesWeightParams.WEIGHTING_NONE);
        }
        
        voxelParameters.setEchoesWeightParams(echoesWeightingParams);

        GroundEnergyParams groundEnergyParameters = new GroundEnergyParams();
        
        groundEnergyParameters.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());
        String extension = "";

        if(checkboxCalculateGroundEnergy.isSelected()){

                groundEnergyParameters.setGroundEnergyFile(new File(textFieldOutputFileGroundEnergy.getText()));

                switch (comboboxGroundEnergyOutputFormat.getSelectionModel().getSelectedIndex()) {
                    case 1:
                        groundEnergyParameters.setGroundEnergyFileFormat(GroundEnergyParams.FILE_FORMAT_PNG);
                        extension = ".png";
                        break;
                    case 0:
                    default:
                        groundEnergyParameters.setGroundEnergyFileFormat(GroundEnergyParams.FILE_FORMAT_TXT);
                        extension = ".png";
            }
        }
        
        voxelParameters.setGroundEnergyParams(groundEnergyParameters);        

        VoxelAnalysisCfg cfg = null;

        if(!checkboxMultiFiles.isSelected()){

            cfg = new ALSVoxCfg();
            
            if(it != SHOTS_FILE){
                
                CSVFile trajFile = new CSVFile(textFieldTrajectoryFileALS.getText());
                if(trajectoryFile != null){
                    trajFile.setColumnAssignment(trajectoryFile.getColumnAssignment());
                    trajFile.setColumnSeparator(trajectoryFile.getColumnSeparator());
                    trajFile.setContainsHeader(trajectoryFile.containsHeader());
                    trajFile.setHeaderIndex(trajectoryFile.getHeaderIndex());
                    trajFile.setNbOfLinesToRead(trajectoryFile.getNbOfLinesToRead());
                    trajFile.setNbOfLinesToSkip(trajectoryFile.getNbOfLinesToSkip());
                }

                ((ALSVoxCfg)cfg).setTrajectoryFile(trajFile);
            }
            
            
            cfg.setVoxelParameters(voxelParameters);
            cfg.setInputType(it);
            cfg.setInputFile(new File(textFieldInputFileALS.getText()));
            cfg.setExportShotSegment(checkboxWriteShotSegment.isSelected());

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

                Raster dtm = null;

                if(generateRasters){

                    logger.info("Loading DTM file "+voxelParameters.getDtmFilteringParams().getDtmFile().getAbsolutePath());
                    try {
                        dtm = AsciiGridHelper.readFromAscFile(voxelParameters.getDtmFilteringParams().getDtmFile());
                        dtm.setTransformationMatrix(MatrixUtility.convertMatrix4dToMat4D(vopMatrix));
                    } catch (Exception ex) {
                        logger.error("Cannot read dtm file", ex);
                    }
                }

                for (File file : selectedFiles) {

                    logger.info("calculate bounding-box of file "+count+"/"+size);

                    BoundingBox3d boundingBox = calculateAutomaticallyMinAndMax(file, quick);

                    VoxelParameters individualVoxelParameters = new VoxelParameters();

                    individualVoxelParameters.infos.setMinCorner(boundingBox.min);
                    individualVoxelParameters.infos.setMaxCorner(boundingBox.max);

                    double resolution = Double.valueOf(textFieldResolution.getText());

                    int splitX = (int) Math.ceil((boundingBox.max.x - boundingBox.min.x) / resolution);
                    int splitY = (int) Math.ceil((boundingBox.max.y - boundingBox.min.y) / resolution);
                    int splitZ = (int) Math.ceil((boundingBox.max.z - boundingBox.min.z) / resolution);

                    individualVoxelParameters.infos.setSplit(new Point3i(splitX, splitY, splitZ));
                    individualVoxelParameters.infos.setResolution(resolution);

                    GroundEnergyParams groundEnergyParams = new GroundEnergyParams();
                    
                    groundEnergyParams.setCalculateGroundEnergy(checkboxCalculateGroundEnergy.isSelected());
                    
                    if(groundEnergyParams.isCalculateGroundEnergy()){
                        groundEnergyParams.setGroundEnergyFile(new File(outputPathFile.getAbsolutePath() + "/" + file.getName() + extension));
                    }
                    
                    individualVoxelParameters.setGroundEnergyParams(groundEnergyParams);

                    List<Input> subList = null;

                    File dtmFile = null;

                    if(generateRasters && dtm != null){

                        logger.info("Generate DTM raster of file "+count+"/"+size);

                        Raster dtmSubset = dtm.subset(new BoundingBox2F(
                                new Point2F((float)individualVoxelParameters.infos.getMinCorner().x, (float)individualVoxelParameters.infos.getMinCorner().y), 
                                new Point2F((float)individualVoxelParameters.infos.getMaxCorner().x, (float)individualVoxelParameters.infos.getMaxCorner().y)), 0);

                        dtmFile = new File(outputPathFile.getAbsolutePath() + File.separator + file.getName() +".asc");
                        try {
                            AsciiGridHelper.write(dtmFile, dtmSubset, false);
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
                ((MultiVoxCfg)cfg).setTrajectoryFile(new CSVFile(textFieldTrajectoryFileALS.getText()));
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
            cfg.setShotFilters(listviewFilters.getItems());

            try {
                cfg.writeConfiguration(selectedFile, Global.buildVersion);
                return true;
            } catch (Exception ex) {
                logger.error("Cannot write configuration file", ex);
            }
        }
        
        return false;
    }

    @FXML
    private void onActionButtonSaveALSVoxelization(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        
        if (selectedFile != null) {
            if(saveALSVoxelization(selectedFile)){
                addFileToTaskList(selectedFile);
            }
            
        }
    }

    @FXML
    private void onActionButtonSaveExecuteALSVoxelization(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        
        if (selectedFile != null) {
            if(saveALSVoxelization(selectedFile)){
                TaskElement taskElement = addFileToTaskList(selectedFile);
                if(taskElement != null){
                    executeProcess(taskElement);
                }
            }
        }
        
    }

    @FXML
    private void onActionButtonExecuteALSVoxelization(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            
            if(saveALSVoxelization(temporaryFile)){
                logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
                
                TaskElement taskElement = addFileToTaskList(temporaryFile);
                if(taskElement != null){
                    executeProcess(taskElement);
                }
            }
            
        }catch(IOException e){
            showErrorDialog(e);
            return;
        }catch(Exception e){
            showErrorDialog(e);
            return;
        }
        
    }

    @FXML
    private void onActionButtonExecuteTLSVoxelization(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            saveTLSVoxelization(temporaryFile);
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            
            TaskElement taskElement = addFileToTaskList(temporaryFile);
            if(taskElement != null){
                executeProcess(taskElement);
            }
            
        }catch(IOException e){
            showErrorDialog(e);
            return;
        }catch(Exception e){
            showErrorDialog(e);
            return;
        }
    }
    
    private boolean checkTLSVoxelizationParametersValidity(){
        
        if(voxSpaceValidationSupport.isInvalid()){
            voxSpaceValidationSupport.initInitialDecoration();
        }
        
        if(tlsVoxValidationSupport.isInvalid()){
            tlsVoxValidationSupport.initInitialDecoration();
        }
        
        if (voxSpaceValidationSupport.isInvalid()|| tlsVoxValidationSupport.isInvalid()) {

            Alert alert = new Alert(AlertType.WARNING);
            alert.setTitle("Warning");
            alert.setHeaderText("Check entries");
            alert.setContentText("Some parameters are not set , please fill the missing arguments");

            alert.showAndWait();

            return false;
        }
        
        return true;
    }
    
    private float[][] parseWeightingData(){
        
        float[][] weightingData = new float[7][7];
        
        for(int i = 0;i<7;i++){
            for(int j = 0;j<7;j++){
                weightingData[i][j] = Float.NaN;
            }
        }
        
        String text = textAreaWeighting.getText();
        String[] lines = text.split("\n");
        
        for(int i = 0;i< lines.length;i++){
            
            if(lines.length != 7){
                showErrorDialog(new Exception("Error occured while parsing weighting data, unsufficient number of lines."));
                return null;
            }
            
            String[] columns = lines[i].split(" ");
            
            for(int j = 0;j< columns.length;j++){
                                
                if(columns.length == 0){
                    showErrorDialog(new Exception("Error occured while parsing weighting data, invalid column."));
                    return null;
                }
                
                try{
                    weightingData[i][j] = Float.valueOf(columns[j]);
                }catch(Exception ex){
                    showErrorDialog(new Exception("Error occured while parsing weighting data, not a number.", ex));
                    return null;
                }
               
            }
        }
        
        return weightingData;
    }
    
    private void saveTLSVoxelization(File selectedFile){
        
        if(!checkTLSVoxelizationParametersValidity()){
            return;
        }
        
        VoxelParameters voxelParameters = getVoxelParametersFromUI();

        voxelParameters.setMergingAfter(checkboxMergeAfter.isSelected());
        voxelParameters.setMergedFile(new File(textFieldOutputPathTLS.getText(), textFieldMergedFileName.getText()));

        EchoesWeightParams echoesWeightingParameters = new EchoesWeightParams();
        
        if (checkboxEnableWeighting.isSelected()) {
            echoesWeightingParameters.setWeightingMode(EchoesWeightParams.WEIGHTING_ECHOS_NUMBER);
            echoesWeightingParameters.setWeightingData(parseWeightingData());
        } else {
            echoesWeightingParameters.setWeightingMode(EchoesWeightParams.WEIGHTING_NONE);
        }
        
        voxelParameters.setEchoesWeightParams(echoesWeightingParameters);

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

        //shot filtering
        cfg.setShotFilters(listviewFilters.getItems());
        cfg.setEnableEmptyShotsFiltering(checkboxEmptyShotsFilter.isSelected());

        if (it == InputType.RSP_PROJECT || it == InputType.PTG_PROJECT || it == InputType.PTX_PROJECT) {
            cfg.setLidarScans(listviewRxpScans.getItems());
        }

        cfg.setEchoFilters(filteringPaneController.getFilterList());

        try {
            cfg.writeConfiguration(selectedFile, Global.buildVersion);
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
            
            TaskElement taskElement = addFileToTaskList(selectedFile);
            if(taskElement != null){
                executeProcess(taskElement);
            }
        }
    }

    @FXML
    private void onActionButtonExecuteMergingProcess(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            saveMergingProcess(temporaryFile);
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            
            TaskElement taskElement = addFileToTaskList(temporaryFile);
            if(taskElement != null){
                executeProcess(taskElement);
            }
            
        }catch(IOException e){
            showErrorDialog(e);
            return;
        }catch(Exception e){
            showErrorDialog(e);
            return;
        }
    }
    
    private void saveMergingProcess(File selectedFile){
        
        VoxelParameters voxParameters = new VoxelParameters();
        voxParameters.infos.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));

        VoxMergingCfg cfg = new VoxMergingCfg(new File(textFieldOutputFileMerging.getText()),
                voxParameters, listViewVoxMergingVoxelFiles.getItems());

        try {
            cfg.writeConfiguration(selectedFile, Global.buildVersion);
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
            
            TaskElement taskElement = addFileToTaskList(selectedFile);
            if(taskElement != null){
                executeProcess(taskElement);
            }
        }
    }

    @FXML
    private void onActionButtonSaveCanopyAnalyzerDirections(ActionEvent event) {
        
        
        ChoiceDialog<String> choiceDialog = new ChoiceDialog<>();
        choiceDialog.getItems().addAll("OBJ", "CSV (spherical coordinates)", "CSV (cartesian coordinates)");
        choiceDialog.setSelectedItem("OBJ");

        choiceDialog.setTitle("Output format");
        choiceDialog.setContentText("Choose the output format");

        Optional<String> result = choiceDialog.showAndWait();
        
        if (result.isPresent()) {

            String format = result.get();

            boolean csv = (format.equals("CSV (spherical coordinates)") || format.equals("CSV (cartesian coordinates)"));

            boolean cartesian = format.equals("CSV (cartesian coordinates)") && csv;

            FileChooser fc = new FileChooser();
            File selectedFile = fc.showSaveDialog(stage);

            if (selectedFile != null) {
                
                LAI2xxx lAi2xxx = new LAI2200(comboboxChooseCanopyAnalyzerSampling.getSelectionModel().getSelectedItem(),
                        LAI2xxx.ViewCap.CAP_360, new boolean[]{false, false, false, false, false});

                lAi2xxx.computeDirections();
                Vector3f[] directions = lAi2xxx.getDirections();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {

                    if (csv) {
                        if (cartesian) {
                            writer.write("X_cartesian Y_cartesian Z_cartesian\n");
                        } else {
                            writer.write("azimut elevation\n");
                        }
                    }

                    SphericalCoordinates sc = new SphericalCoordinates();

                    for (Vector3f direction : directions) {

                        if (csv) {
                            if (cartesian) {
                                writer.write(direction.x + " " + direction.y + " " + direction.z + "\n");
                            } else {
                                sc.toSpherical(new Vector3d(direction));
                                writer.write(sc.getAzimut() + " " + sc.getZenith() + "\n");
                            }
                        } else {
                            writer.write("v " + direction.x + " " + direction.y + " " + direction.z + "\n");
                        }
                    }

                } catch (IOException ex) {
                    showErrorDialog(ex);
                }
            }
        }
        
    }


    @FXML
    private void onActionButtonOpenInputFileButterflyRemover(ActionEvent event) {
        
        File selectedFile = fileChooserOpenInputFileButterflyRemover.showOpenDialog(stage);
        
        if(selectedFile != null){
            textFieldInputFileButterflyRemover.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenOutputFileButterflyRemover(ActionEvent event) {
        
        File selectedFile = fileChooserOpenOutputFileButterflyRemover.showSaveDialog(stage);
        
        if(selectedFile != null){
            textFieldOutputFileButterflyRemover.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonExecuteButterflyRemover(ActionEvent event) {
        
        File temporaryFile;
        try {
            temporaryFile = File.createTempFile("cfg_temp", ".xml");
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            saveButterflyRemovercfg(temporaryFile);
            
            TaskElement taskElement = addFileToTaskList(temporaryFile);
            if(taskElement != null){
                executeProcess(taskElement);
            }
            
        } catch (IOException ex) {
            showErrorDialog(ex);
        } catch (Exception ex) {
            showErrorDialog(ex);
        }
    }
    
    private void saveButterflyRemovercfg(File file) throws Exception{
        
        ButterflyRemoverCfg cfg = new ButterflyRemoverCfg();
        cfg.setInputFile(new File(textFieldInputFileButterflyRemover.getText()));
        cfg.setOutputFile(new File(textFieldOutputFileButterflyRemover.getText()));
        
        try {
            cfg.writeConfiguration(file, Global.buildVersion);
        } catch (Exception ex) {
            throw new Exception("Cannot write configuration file", ex);
        }
    }

    @FXML
    private void onActionMenuItemPositionsTransmittanceMapSelectionAll(ActionEvent event) {
        listViewTransmittanceMapSensorPositions.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemPositionsTransmittanceMapSelectionNone(ActionEvent event) {
        listViewTransmittanceMapSensorPositions.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemovePositionTransmittanceMap(ActionEvent event) {
        
        ObservableList<Point3d> selectedItems = listViewTransmittanceMapSensorPositions.getSelectionModel().getSelectedItems();
        
        if(selectedItems.size() == listViewTransmittanceMapSensorPositions.getItems().size()){
            listViewTransmittanceMapSensorPositions.getItems().clear();
        }else{
            listViewTransmittanceMapSensorPositions.getItems().removeAll(selectedItems);
        }
    }

    @FXML
    private void onActionButtonAddPositionTransmittanceMap(ActionEvent event) {
        
        if(!textfieldVoxelFilePathTransmittance.getText().isEmpty()){
            File voxelFile = new File(textfieldVoxelFilePathTransmittance.getText());
            if(voxelFile.exists()){
                positionImporterFrameController.setInitialVoxelFile(voxelFile);
            }
        }
        
        positionImporterFrame.show();
        positionImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                
                listViewTransmittanceMapSensorPositions.getItems().addAll(positionImporterFrameController.getPositions());
            }
        });
    }

    @FXML
    private void onActionButtonExecuteTransmittanceMapSimulation(ActionEvent event) {
        
        try{
            executeTransmittanceLightMapSim();
        }catch(Exception ex){
            showErrorDialog(ex);
        }
    }

    @FXML
    private void onActionButtonSaveTransmittanceMapSimulation(ActionEvent event) {
        
        File selectedFile = fileChooserSaveTransmittanceSimCfgFile.showSaveDialog(stage);
        
        if(selectedFile != null){
            try{
                saveTransmittanceLightMapSim(selectedFile);
            }catch(Exception ex){
                showErrorDialog(ex);
            }
        }
    }

    @FXML
    private void onActionButtonSaveExecuteTransmittanceMapSimulation(ActionEvent event) {
        
        File selectedFile = fileChooserSaveTransmittanceSimCfgFile.showSaveDialog(stage);
        
        if(selectedFile != null){
            try{
                saveExecuteTransmittanceLightMapSim(selectedFile);
            }catch(Exception ex){
                showErrorDialog(ex);
            }
            
        }
    }

    @FXML
    private void onActionMenuItemSelectAllVoxFileFromMergeList(ActionEvent event) {
        listViewVoxMergingVoxelFiles.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemUnselectAllVoxFileFromMergeList(ActionEvent event) {
        listViewVoxMergingVoxelFiles.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemoveVoxFileFromMergingList(ActionEvent event) {
        ObservableList<File> selectedItems = listViewVoxMergingVoxelFiles.getSelectionModel().getSelectedItems();
        listViewVoxMergingVoxelFiles.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionButtonAddVoxFileToMergingList(ActionEvent event) {
        
        List<File> selectedFiles = fileChooserVoxMergingList.showOpenMultipleDialog(stage);
        
        if(selectedFiles != null){
            
            for(File file : selectedFiles){
                addVoxelFileToMergingList(file);
            }
            
        }
    }
    
    private void addVoxelFileToMergingList(File file){
        
        if(!listViewVoxMergingVoxelFiles.getItems().contains(file)){
            
            if(Util.checkIfVoxelFile(file)){
                listViewVoxMergingVoxelFiles.getItems().add(file);
            }
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
    
     private void showImage(File file){
        
        try {
            ImageView iv = new ImageView(new Image(file.toURI().toURL().toString()));
            iv.setPreserveRatio(true);
            Stage stage = new Stage();
            
            final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);
            
            zoomProperty.addListener(new InvalidationListener() {
                @Override
                public void invalidated(javafx.beans.Observable observable) {
                    iv.setFitWidth(zoomProperty.get() * 4);
                    iv.setFitHeight(zoomProperty.get() * 3);
                }
            });
            
            ScrollPane sp = new ScrollPane(iv);
            stage.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
                @Override
                public void handle(ScrollEvent event) {
                    if (event.getDeltaY() > 0) {
                        zoomProperty.set(zoomProperty.get() * 1.1);
                    } else if (event.getDeltaY() < 0) {
                        zoomProperty.set(zoomProperty.get() / 1.1);
                    }
                }
            });
            
            
            stage.setScene(new Scene(new Group(sp)));

            stage.sizeToScene();
            stage.show();
        } catch (IOException ex) {
            showErrorDialog(ex);
        }
        
    }
    
//    private void showImage(File file){
//        
//        try {
//            final Image image = new Image(Files.newInputStream(file.toPath()));
//            
//            ImageView iv = new ImageView(image);
//            iv.setPreserveRatio(true);
//            Stage stage = new Stage();            
//            
//            final DoubleProperty zoomProperty = new SimpleDoubleProperty(image.getWidth());
//            final DoubleProperty posXProperty = new SimpleDoubleProperty();
//            final DoubleProperty posYProperty = new SimpleDoubleProperty();
//            
//            final Canvas canvas = new Canvas(500, 500);
//            final Scene scene = new Scene(new Group(canvas));
//            
//            final GraphicsContext gc = canvas.getGraphicsContext2D();
//            
//            gc.setFill(Color.WHITE);
//            gc.drawImage(image, 0, 0);
//            
//            zoomProperty.addListener(new ChangeListener<Number>() {
//                @Override
//                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                    /*iv.setViewport(new Rectangle2D(
//                            posXProperty.get(),
//                            posYProperty.get(),
//                            500, 
//                            200));*/
//                    
//                    gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
//                    /*gc.drawImage(image, 
//                            posXProperty.get()-((zoomProperty.get() * 4)/2.0), posYProperty.get()-((zoomProperty.get() * 4)/2.0),
//                            zoomProperty.get() * 4, zoomProperty.get() * 4);*/
//                    
//                    int tx = 0, ty = 0;
//                    int sx = (int)(posXProperty.get()-((zoomProperty.get() * 4)/2.0));
//                    int sy = (int)(posYProperty.get()-((zoomProperty.get() * 4)/2.0));
//                    int dw = (int) (zoomProperty.get() * 4);
//                    int dh = (int) (zoomProperty.get() * 4);
//                    
//                    int sw = (int) (zoomProperty.get());
//                    int sh = (int) (zoomProperty.get());
//                    
//                    int Sx = (int)(dw/(float)sw);
//                    int Sy = (int)(dh/(float)sh);
//                    
//                    PixelReader reader = image.getPixelReader();
//                    PixelWriter writer = gc.getPixelWriter();
//                    
//                    for (int y = 0; y < sh; y++) {
//                        for (int x = 0; x < sw; x++) {
//                            final int argb = reader.getArgb(x, y);
//                            
//                            for (int dy = 0; dy < Sy; dy++) {
//                                for (int dx = 0; dx < Sx; dx++) {
//                                    writer.setArgb(x * Sx + dx, y * Sy + dy, argb);
//                                }
//                            }
//                        }
//                    }
//                    
//                    /*
//                    
//                    iv.setFitWidth();
//                    iv.setFitHeight();*/
//                    
//                    //iv.setFitWidth(zoomProperty.get() * 4);
//                    //iv.setFitHeight(zoomProperty.get() * 3);
//                }
//            });
//            
//            ScrollPane sp = new ScrollPane(canvas);
//            stage.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
//                @Override
//                public void handle(ScrollEvent event) {
//                    if (event.getDeltaY() > 0) {
//                        zoomProperty.set(zoomProperty.get() * 1.1);
//                    } else if (event.getDeltaY() < 0) {
//                        zoomProperty.set(zoomProperty.get() / 1.1);
//                    }
//                    
//                    posXProperty.set(event.getSceneX());
//                    posYProperty.set(event.getSceneY());
//                }
//            });
//            
//            stage.widthProperty().addListener(new ChangeListener<Number>() {
//                @Override
//                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                    canvas.setWidth(newValue.doubleValue());
//                }
//            });
//            
//            stage.heightProperty().addListener(new ChangeListener<Number>() {
//                @Override
//                public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                    canvas.setHeight(newValue.doubleValue());
//                }
//            });
//            
//            Menu exportMenu = new Menu("Export");
//            MenuItem exportAsBmp = new MenuItem("as bmp");
//            exportAsBmp.setOnAction(new EventHandler<ActionEvent>() {
//                @Override
//                public void handle(ActionEvent event) {
//                    
//                }
//            });
//            
//            exportMenu.getItems().add(exportAsBmp);
//            
//            ContextMenu contextMenu = new ContextMenu();
//            contextMenu.getItems().add(exportMenu);
//            
//            sp.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
//                @Override
//                public void handle(ContextMenuEvent event) {
//                }
//            });
//            
//            stage.setScene(scene);
//
//            stage.sizeToScene();
//            stage.show();
//        } catch (IOException ex) {
//            showErrorDialog(ex);
//        }
//        
//    }
    
    private void initEchoFiltering(){
        
        anchorPaneEchoFilteringClassifications = new AnchorPane();
        listviewClassifications = new ListView<>();
        
        
        listviewClassifications.getItems().addAll(
                createSelectedCheckbox(Classification.CREATED_NEVER_CLASSIFIED.getValue()+" - "+
                        Classification.CREATED_NEVER_CLASSIFIED.getDescription()),
                createSelectedCheckbox(Classification.UNCLASSIFIED.getValue()+" - "+
                        Classification.UNCLASSIFIED.getDescription()),
                new CheckBox(Classification.GROUND.getValue()+" - "+ //by default unselected, ground point will be removed
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
    
    private XYSeries generatePDFSerie(){
        
        LeafAngleDistribution distribution = getLeafAngleDistribution();
        
        XYSeries serie = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            
            serie.add(angleInDegrees, pdf);
        }
        
        return serie;
    }
    
    @FXML
    private void onActionButtonDisplayPdf(ActionEvent event) {
        
        XYSeriesCollection dataset = new XYSeriesCollection(generatePDFSerie());
            
        ChartViewer viewer = new ChartViewer("PDF function", 500, 500, 1);
        viewer.insertChart(ChartViewer.createBasicChart("PDF ~ angles", dataset, "Angle (degrees)", "PDF"));
        viewer.show();
        
    }
    
    private XYSeries generateGThetaSerie(){
        
        LeafAngleDistribution distribution = getLeafAngleDistribution();
        
        XYSeries serie = new XYSeries(distribution.getType(), false);
        
        GTheta m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        return serie;
    }

    @FXML
    private void onActionButtonDisplayGTheta(ActionEvent event) {
        
        XYSeriesCollection dataset = new XYSeriesCollection(generateGThetaSerie());
            
        ChartViewer viewer = new ChartViewer("GTheta", 500, 500, 1);
        viewer.insertChart(ChartViewer.createBasicChart("GTheta ~ inclinaison angle", dataset, "Angle (degrees)", "GTheta"));
        viewer.show();
    }

    @FXML
    private void onActionButtonOpenRspProject(ActionEvent event) {
        
        File selectedFile = fileChooserOpenInputFileTLS.showOpenDialog(stage);
        
        if(selectedFile != null){
            
            onTLSInputFileFileChoosed(selectedFile);
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

            try {
                saveHemiPhotoSimulation(selectedFile);
                
                TaskElement taskElement = addFileToTaskList(selectedFile);
                if(taskElement != null){
                    executeProcess(taskElement);
                }
            } catch (Exception ex) {
                showErrorDialog(ex);
            }
        }
    }
    
    @FXML
    private void onActionButtonExecuteHemiPhotoSimulation(ActionEvent event) {
        
        File temporaryFile;
        try{
            temporaryFile  = File.createTempFile("cfg_temp", ".xml");
            saveHemiPhotoSimulation(temporaryFile);
            
            logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
            
            TaskElement taskElement = addFileToTaskList(temporaryFile);
            if(taskElement != null){
                executeProcess(taskElement);
            }
            
        }catch(IOException e){
            showErrorDialog(e);
        }catch(Exception e){
            showErrorDialog(e);
        }
    }

    @FXML
    private void onActionButtonSaveHemiPhotoSimulation(ActionEvent event) {
        
        File selectedFile = fileChooserSaveConfiguration.showSaveDialog(stage);
        
        if (selectedFile != null) {

            try {
                saveHemiPhotoSimulation(selectedFile);
                addFileToTaskList(selectedFile);
            } catch (Exception ex) {
                showErrorDialog(ex);
            }
        }
    }
    

    @FXML
    private void onActionButtonOpenHemiPhotoOutputTextFile(ActionEvent event) {
        
        if(tabpaneHemiPhotoMode.getSelectionModel().getSelectedIndex() == 0){
            File selectedFile = fileChooserSaveHemiPhotoOutputTextFile.showSaveDialog(stage);
        
            if(selectedFile != null){
                textfieldHemiPhotoOutputTextFile.setText(selectedFile.getAbsolutePath());
            }
        }else{
            File selectedFile = directoryChooserSaveHemiPhotoOutputTextFile.showDialog(stage);
        
            if(selectedFile != null){
                textfieldHemiPhotoOutputTextFile.setText(selectedFile.getAbsolutePath());
            }
        }
    }

    @FXML
    private void onActionButtonOpenHemiPhotoOutputBitmapFile(ActionEvent event) {
        
        if(tabpaneHemiPhotoMode.getSelectionModel().getSelectedIndex() == 0){
            File selectedFile = fileChooserSaveHemiPhotoOutputBitmapFile.showSaveDialog(stage);
        
            if(selectedFile != null){
                textfieldHemiPhotoOutputBitmapFile.setText(selectedFile.getAbsolutePath());
            }
        }else{
            File selectedFile = directoryChooserSaveHemiPhotoOutputBitmapFile.showDialog(stage);
        
            if(selectedFile != null){
                textfieldHemiPhotoOutputBitmapFile.setText(selectedFile.getAbsolutePath());
            }
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
        
        viewer3DPanelController.setStage(stage);
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
    
    private static String[][] arrangeText(String[][] lines){
        
        //get the largest cell of each column
        
        int[] largestCells = new int[lines[0].length];
        
        for(int i=0;i<lines.length;i++){ //for each line
            
            for(int j=0;j<lines[i].length;j++){ //fo each column

                if(lines[i][j].length() > largestCells[j]){
                    largestCells[j] = lines[i][j].length();
                }
            }
        }
        
        //fill each cell to reach max character length
        for(int i=0;i<lines.length;i++){ //for each line
            
            for(int j=0;j<lines[i].length;j++){ //for each column

                for(int n = lines[i][j].length() ; n<largestCells[j] ; n++){
                    lines[i][j] += " ";
                }
            }
        }
        
        return lines;
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
        if (selectedFiles != null && selectedFiles.size() > 0) {
            
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
                
                selectALSInputMode(selectedFiles.get(0));
            }
        }
    }
    
    private void selectALSInputMode(File inputFile){
        
        String extension = FileManager.getExtension(inputFile);
                
        switch(extension){
            case ".las":
                comboboxModeALS.getSelectionModel().select(RS_STR_INPUT_TYPE_LAZ);
                hboxTrajectoryFile.setDisable(false);
                break;
            case ".laz":
                comboboxModeALS.getSelectionModel().select(RS_STR_INPUT_TYPE_LAZ);
                hboxTrajectoryFile.setDisable(false);
                break;
            case ".sht":
                comboboxModeALS.getSelectionModel().select(RS_STR_INPUT_TYPE_SHOTS);
                hboxTrajectoryFile.setDisable(true);
                break;
        }
    }
    
    private void onTLSInputFileFileChoosed(File selectedFile){
        
        Rsp selectedProject = new Rsp();
        try {
            selectedProject.read(selectedFile);

            riscanProjectExtractor.init(selectedProject);
            riscanProjectExtractor.getFrame().show();

            riscanProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                @Override
                public void handle(WindowEvent event) {
                    List<LidarScan> selectedScans = riscanProjectExtractor.getController().getSelectedScans();

                    ObservableList<LidarScan> items1 = listViewHemiPhotoScans.getItems();

                    for(LidarScan scan : selectedScans){
                        scan.name = scan.file.getAbsolutePath();
                        items1.add(scan);
                    }
                }
            });

        } catch (JDOMException | IOException ex) {
            showErrorDialog(ex);
        }
        
    }
    
    private void onTrajectoryFileChoosed(File selectedFile){
            
        textFileParserFrameController.setColumnAssignment(true);
        textFileParserFrameController.setColumnAssignmentValues("Ignore", "Easting", "Northing", "Elevation", "Time");

        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 1);
        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 2);
        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 3);
        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(3, 4);

        if(trajectoryFile != null){
            textFileParserFrameController.setHeaderExtractionEnabled(trajectoryFile.containsHeader());
            textFileParserFrameController.setSeparator(trajectoryFile.getColumnSeparator());
            trajectoryFile.setColumnAssignment(trajectoryFile.getColumnAssignment());
        }else{
            textFileParserFrameController.setHeaderExtractionEnabled(true);
            textFileParserFrameController.setSeparator(",");
        }


        try{
            textFileParserFrameController.setTextFile(selectedFile);
        }catch(IOException ex){
            showErrorDialog(ex);
            return;
        }

        Stage textFileParserFrame = textFileParserFrameController.getStage();
        textFileParserFrame.show();

        textFileParserFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                trajectoryFile = new CSVFile(selectedFile.getAbsolutePath());
                trajectoryFile.setColumnSeparator(textFileParserFrameController.getSeparator());
                trajectoryFile.setColumnAssignment(textFileParserFrameController.getAssignedColumnsItemsMap());
                trajectoryFile.setNbOfLinesToRead(textFileParserFrameController.getNumberOfLines());
                trajectoryFile.setNbOfLinesToSkip(textFileParserFrameController.getSkipLinesNumber());
                trajectoryFile.setContainsHeader(textFileParserFrameController.getHeaderIndex() != -1);
                trajectoryFile.setHeaderIndex(textFileParserFrameController.getHeaderIndex());

                textFieldTrajectoryFileALS.setText(selectedFile.getAbsolutePath());
            }
        });
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

        final File selectedFile = fileChooserOpenTrajectoryFileALS.showOpenDialog(stage);
        
        if (selectedFile != null) {
            
            lastFCOpenTrajectoryFileALS = selectedFile;
            onTrajectoryFileChoosed(selectedFile);
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
    
    private void onInputFileTLSChoosed(File selectedFile){
        
        String extension = FileManager.getExtension(selectedFile);

        switch (extension) {
            case ".rxp":

                textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());

                comboboxModeTLS.getSelectionModel().select(0);

                items = new ArrayList<>();
                items.add(new LidarScan(selectedFile, MatrixUtility.convertMat4DToMatrix4d(Mat4D.identity()), selectedFile.getName()));

                listviewRxpScans.getItems().setAll(items);

                break;
            case ".rsp":

                Rsp rsp = new Rsp();
                try {
                    rsp.read(selectedFile);

                    comboboxModeTLS.getSelectionModel().select(1);

                    textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());

                    riscanProjectExtractor.init(rsp);
                    riscanProjectExtractor.getFrame().show();

                    riscanProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {

                            final List<LidarScan> selectedScans = riscanProjectExtractor.getController().getSelectedScans();

                            items = new ArrayList<>();

                            for(LidarScan scan : selectedScans){
                                scan.name = scan.file.getAbsolutePath();
                                items.add(scan);
                            }

                            popMatrix = new Matrix4d(rsp.getPopMatrix());
                            updateResultMatrix();

                            listviewRxpScans.getItems().setAll(items);
                        }
                    });

                } catch (JDOMException | IOException ex) {
                    showErrorDialog(ex);
                }

                break;
            case ".ptg":
                try {

                    comboboxModeTLS.getSelectionModel().select(3);
                    textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());

                    ptgProjectExtractor.init(selectedFile);
                    ptgProjectExtractor.getFrame().show();

                    ptgProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {

                            final List<LidarScan> selectedScans = ptgProjectExtractor.getController().getSelectedScans();

                            items = new ArrayList<>();

                            for(LidarScan scan : selectedScans){
                                scan.name = scan.file.getAbsolutePath();
                                items.add(scan);
                            }

                            updateResultMatrix();
                            listviewRxpScans.getItems().setAll(items);
                        }
                    });  

                } catch (IOException ex) {
                    showErrorDialog(ex);
                } catch (Exception ex) {
                    showErrorDialog(ex);
                }

                break;
            case ".ptx":

                try {

                    comboboxModeTLS.getSelectionModel().select(2);
                    textFieldInputFileTLS.setText(selectedFile.getAbsolutePath());

                    ptxProjectExtractor.init(selectedFile);
                    ptxProjectExtractor.getFrame().show();

                    ptxProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {

                            final List<LidarScan> selectedScans = ptxProjectExtractor.getController().getSelectedScans();

                            items = new ArrayList<>();

                            for(LidarScan scan : selectedScans){

                                scan.name = ((PTXLidarScan)scan).toString();
                                items.add(scan);
                            }

                            updateResultMatrix();
                            listviewRxpScans.getItems().setAll(items);
                        }
                    });                        

                } catch (IOException ex) {
                    showErrorDialog(ex);
                } catch (Exception ex) {
                    showErrorDialog(ex);
                }

                break;
            default:
        }
    }

    @FXML
    private void onActionButtonOpenInputFileTLS(ActionEvent event) {

        File selectedFile = fileChooserOpenInputFileTLS.showOpenDialog(stage);
        
        if(selectedFile != null){
            
            onInputFileTLSChoosed(selectedFile);

        }
    }
    
    


    @FXML
    private void onActionButtonRemoveVoxelFileFromListView(ActionEvent event) {

        ObservableList<File> selectedItems = listViewProductsFiles.getSelectionModel().getSelectedItems();
        listViewProductsFiles.getItems().removeAll(selectedItems);

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
                addFileToProductsList(file);
            }
        }
    }

    @FXML
    private void onActionMenuItemSelectionAll(ActionEvent event) {
        listViewProductsFiles.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemSelectionNone(ActionEvent event) {
        listViewProductsFiles.getSelectionModel().clearSelection();
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
                                    sopMatrix = new Matrix4d(rxpScan.getSopMatrix());
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
                        
                        mat = new Matrix4d(tempRsp.getPopMatrix());

                        //scan unique
                        if (comboboxModeTLS.getSelectionModel().getSelectedIndex() == 0) {

                            File scanFile = new File(textFieldInputFileTLS.getText());

                            if (Files.exists(scanFile.toPath())) {
                                RxpScan rxpScan = tempRsp.getRxpScanByName(scanFile.getName());
                                if (rxpScan != null) {
                                    sopMatrix = new Matrix4d(rxpScan.getSopMatrix());
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

    public TaskElement addFileToTaskList(final File file) {

        ObservableList<TaskElement> taskElements = listViewTaskList.getItems();
        
        int idxCfgToReplace = -1;
        
        //don't add the task if it is already inside the task list
        int index = 0;
        for(TaskElement taskElement : taskElements){
            if(file.equals(taskElement.getLinkedFile())){
                idxCfgToReplace = index; 
                break;
            }
            index++;
        }
        
        //get the task type
        String type;
        
        try {
            type = Configuration.readType(file);
        } catch (JDOMException | IOException ex) {
            showErrorDialog(ex);
            return null;
        }
            
        TaskElement element = null;
        
        switch (type) {
                                
            case "voxelisation-ALS":
                
                element = new TaskElement(file, new ServiceProvider() {
                    @Override
                    public Service provide() {
                        return new ALSVoxelizationService(file);
                    }
                });

                //element = new TaskElement(s, file);
                element.setTaskIcon(TaskElement.VOXELIZATION_IMG);

                element.addTaskListener(new TaskAdapter() {
                    
                    @Override
                    public void onSucceeded(Service service) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                
                                File outputFile = ((ALSVoxelizationService)service).getValue();
                                if(outputFile != null){
                                    addFileToProductsList(outputFile);
                                }
                            }
                        });
                    }
                });
                
                break;
                
            case "voxelisation-TLS":

                final TLSVoxCfg tLSVoxCfg = new TLSVoxCfg();
                try {
                    tLSVoxCfg.readConfiguration(file);
                } catch (Exception ex) {
                    showErrorDialog(ex);
                    return null;
                }
                
                switch (tLSVoxCfg.getInputType()) {

                    case RSP_PROJECT:
                
                        element = new TaskElement(file, new ServiceProvider() {
                            @Override
                            public Service provide() {
                                if(tlsVoxNbThreads == -1){
                                    return new RSPVoxelizationService(file, (int)sliderRSPCoresToUse.getValue());
                                }else{
                                    return new RSPVoxelizationService(file, tlsVoxNbThreads);
                                }
                            }
                        });
                        element.setTaskIcon(TaskElement.VOXELIZATION_IMG);

                        element.addTaskListener(new TaskAdapter() {
                            @Override
                            public void onSucceeded(Service service) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<File> voxFiles = ((RSPVoxelizationService)service).getValue();

                                        for(File voxFile : voxFiles){
                                            addFileToProductsList(voxFile);
                                        }
                                    }
                                });
                            }
                        });

                        break;
                        
                    case RXP_SCAN:
                
                        element = new TaskElement(file, new ServiceProvider() {
                            @Override
                            public Service provide() {
                                return new RXPVoxelizationService(file);
                            }
                        });
                        
                        element.setTaskIcon(TaskElement.VOXELIZATION_IMG);

                        element.addTaskListener(new TaskAdapter() {
                            @Override
                            public void onSucceeded(Service service) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        addFileToProductsList(tLSVoxCfg.getOutputFile());
                                    }
                                });
                            }
                        });

                        break;
                        
                    case PTG_PROJECT:
                        
                        element = new TaskElement(file, new ServiceProvider() {
                            @Override
                            public Service provide() {
                                if(tlsVoxNbThreads == -1){
                                    return new PTGVoxelizationService(file, (int)sliderRSPCoresToUse.getValue());
                                }else{
                                    return new PTGVoxelizationService(file, tlsVoxNbThreads);
                                }
                            }
                        });
                        
                        element.setTaskIcon(TaskElement.VOXELIZATION_IMG);

                        element.addTaskListener(new TaskAdapter() {
                            @Override
                            public void onSucceeded(Service service) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<File> voxFiles = ((PTGVoxelizationService)service).getValue();

                                        for(File voxFile : voxFiles){
                                            addFileToProductsList(voxFile);
                                        }
                                    }
                                });
                            }
                        });
                        break;
                        
                    case PTX_PROJECT:
                        
                        element = new TaskElement(file, new ServiceProvider() {
                            @Override
                            public Service provide() {
                                if(tlsVoxNbThreads == -1){
                                    return new PTXVoxelizationService(file, (int)sliderRSPCoresToUse.getValue());
                                }else{
                                    return new PTXVoxelizationService(file, tlsVoxNbThreads);
                                }
                            }
                        });
                        
                        element.setTaskIcon(TaskElement.VOXELIZATION_IMG);

                        element.addTaskListener(new TaskAdapter() {
                            @Override
                            public void onSucceeded(Service service) {
                                Platform.runLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        List<File> voxFiles = ((PTXVoxelizationService)service).getValue();

                                        for(File voxFile : voxFiles){
                                            addFileToProductsList(voxFile);
                                        }
                                    }
                                });
                            }
                        });
                        break;
                }
                
                break;
                
            case "transmittance":
                
                /*TransmittanceCfg transCfg = new TransmittanceCfg(new TransmittanceParameters());
                
                try {
                    transCfg.readConfiguration(file);
                } catch (JDOMException | IOException ex) {
                    showErrorDialog(ex);
                    return null;
                }*/
                
                element = new TaskElement(file, new ServiceProvider() {
                    @Override
                    public Service provide() {
                        return new TransmittanceSimService(file);
                    }
                });
                
                element.setTaskIcon(TaskElement.TRANSMITTANCE_IMG);

                element.addTaskListener(new TaskAdapter() {
                    @Override
                    public void onSucceeded(Service service) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                
                                //if(transCfg.getParameters().isGenerateBitmapFile()){

                                    List<File> bitmapFiles = ((TransmittanceSimService)service).getValue().getOutputBitmapFiles();

                                    if(bitmapFiles != null){

                                        for(File file : bitmapFiles){
                                            addFileToProductsList(file);
                                        }
                                    }
                                //}
                            }
                        });
                    }
                });
                
                break;
                
            case "LAI2000":
            case "LAI2200":
                
                element = new TaskElement(file, new ServiceProvider() {
                    @Override
                    public Service provide() {
                        return new Lai2xxxSimService(file);
                    }
                });
                
                element.setTaskIcon(TaskElement.CANOPEE_ANALYZER_IMG);

                break;

            case "Hemi-Photo":
                                
                element = new TaskElement(file, new ServiceProvider() {
                    @Override
                    public Service provide() {
                        return new HemiPhotoSimService(file);
                    }
                });
                
                element.setTaskIcon(TaskElement.HEMI_IMG);
                
                element.addTaskListener(new TaskAdapter() {
                    @Override
                    public void onSucceeded(Service service) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                
                                File outputFile = ((HemiPhotoSimService)service).getValue();
                                
                                if(outputFile != null){
                                    addFileToProductsList(outputFile);
                                }
                            }
                        });
                    }
                });

                break;

            case "merging":

                element = new TaskElement(file, new ServiceProvider() {
                    @Override
                    public Service provide() {
                        return new VoxFileMergingService(file);
                    }
                });

                element.setTaskIcon(TaskElement.MISC_IMG);
                
                element.addTaskListener(new TaskAdapter() {
                    @Override
                    public void onSucceeded(Service service) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                
                                File outputFile = ((VoxFileMergingService)service).getValue();
                                if(outputFile != null){
                                    addFileToProductsList(outputFile);
                                }
                                
                            }
                        });
                    }
                });

                break;
                
            case "butterfly-removing":
                
                element = new TaskElement(file, new ServiceProvider() {
                    @Override
                    public Service provide() {
                        return new ButterflyRemoverService(file);
                    }
                });
                
                element.setTaskIcon(TaskElement.MISC_IMG);
                
                element.addTaskListener(new TaskAdapter() {
                    @Override
                    public void onSucceeded(Service service) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                
                                File outputFile = ((ButterflyRemoverService)service).getValue();
                                
                                if(outputFile != null){
                                    addFileToProductsList(outputFile);
                                }
                            }
                        });
                    }
                });
                
                break;
        }
        
        if(element != null){
            
            if(idxCfgToReplace != -1){
                if(listViewTaskList.getItems().get(idxCfgToReplace).getButtonType() == TaskElement.ButtonType.CANCEL){
                    Alert alert = new Alert(AlertType.WARNING);
                    alert.setContentText("This task is currently running, cancel the operation first.");
                    alert.show();
                }else{
                    listViewTaskList.getItems().set(idxCfgToReplace, element);
                }
                
            }else{
                listViewTaskList.getItems().add(element);
            }
            
            element.addTaskListener(new TaskAdapter() {

                @Override
                public void onFailed(Exception ex) {
                    showErrorDialog(ex);
                }
            });
        }
        
        return element;
    }

    private void addFileToProductsList(File file) {

        if (!listViewProductsFiles.getItems().contains(file) && Files.exists(file.toPath())) {
            listViewProductsFiles.getItems().add(file);
            listViewProductsFiles.getSelectionModel().clearSelection();
            listViewProductsFiles.getSelectionModel().select(file);
        }
    }
    
    public void addTasksToTaskList(File... tasks){
        
        for(File f : tasks){
            addFileToTaskList(f);
        }
    }
    
    public void executeTaskListInParallel(List<TaskElement> taskElements, int nbThreads){
        TaskElementExecutor executor = new TaskElementExecutor(nbThreads, taskElements);
        executor.execute();
    }
    
    public void executeTaskListSequentially(List<TaskElement> taskElements){
        
        TaskElementExecutor executor = new TaskElementExecutor(1, taskElements);
        executor.execute();
    }
    
    private void executeTaskList(List<TaskElement> taskElements){
        
        TaskElementExecutor executor;
        
        if(taskElements.size() == 1){
            executor = new TaskElementExecutor(1, taskElements);
            executor.execute();
        }else{
            ChoiceDialog<String> choiceDialog = new ChoiceDialog<>("Sequential execution",
                "Sequential execution", "Parallel execution");
        
            choiceDialog.showAndWait();

            String result = choiceDialog.getResult();

            if(result != null){
                switch(result){
                    case "Sequential execution":
                        executeTaskListSequentially(taskElements);
                        break;
                    case "Parallel execution":
                        
                        if(tlsVoxNbThreads == -1){
                            executeTaskListInParallel(taskElements, (int)sliderRSPCoresToUse.getValue());
                        }else{
                            executeTaskListInParallel(taskElements, tlsVoxNbThreads);
                        }
                        
                        break;
                }
            }
        }
    }

    @FXML
    private void onActionButtonExecute(ActionEvent event) {

        ObservableList<TaskElement> taskElements = listViewTaskList.getSelectionModel().getSelectedItems();
        
        executeTaskList(taskElements);
    }
    
    public void showErrorDialog(final Exception e){
        
        logger.error("An error occured", e);
        
        DialogHelper.showErrorDialog(stage, e);
    }
    
    private void executeProcess(final TaskElement taskElement) {
        
        taskElement.startTask();
    }

    private VoxelParameters getVoxelParametersFromUI() {

        VoxelParameters voxelParameters = new VoxelParameters();
        
        voxelParameters.infos.setMinCorner(new Point3d(
                Double.valueOf(voxelSpacePanelVoxelizationController.getTextFieldEnterXMin().getText()),
                Double.valueOf(voxelSpacePanelVoxelizationController.getTextFieldEnterYMin().getText()),
                Double.valueOf(voxelSpacePanelVoxelizationController.getTextFieldEnterZMin().getText())));

        voxelParameters.infos.setMaxCorner(new Point3d(
                Double.valueOf(voxelSpacePanelVoxelizationController.getTextFieldEnterXMax().getText()),
                Double.valueOf(voxelSpacePanelVoxelizationController.getTextFieldEnterYMax().getText()),
                Double.valueOf(voxelSpacePanelVoxelizationController.getTextFieldEnterZMax().getText())));

        voxelParameters.infos.setSplit(new Point3i(
                Integer.valueOf(voxelSpacePanelVoxelizationController.getTextFieldXNumber().getText()),
                Integer.valueOf(voxelSpacePanelVoxelizationController.getTextFieldYNumber().getText()),
                Integer.valueOf(voxelSpacePanelVoxelizationController.getTextFieldZNumber().getText())));

        voxelParameters.infos.setResolution(Double.valueOf(textFieldResolution.getText()));

        DTMFilteringParams dtmFilteringParams = new DTMFilteringParams();
        
        dtmFilteringParams.setActivate(checkboxUseDTMFilter.isSelected());
        if (checkboxUseDTMFilter.isSelected()) {
            dtmFilteringParams.setMinDTMDistance(Float.valueOf(textfieldDTMValue.getText()));
            dtmFilteringParams.setDtmFile(new File(textfieldDTMPath.getText()));
            dtmFilteringParams.setUseVOPMatrix(checkboxApplyVOPMatrix.isSelected());
        }
        
        voxelParameters.setDtmFilteringParams(dtmFilteringParams);

        voxelParameters.setUsePointCloudFilter(checkboxUsePointcloudFilter.isSelected());
        if (checkboxUsePointcloudFilter.isSelected()) {

            List<PointcloudFilter> pointcloudFilters = new ArrayList<>();

            ObservableList<Node> childrenUnmodifiable = vBoxPointCloudFiltering.getChildrenUnmodifiable();
            for (Node n : childrenUnmodifiable) {
                if (n instanceof PointCloudFilterPaneComponent) {
                    PointCloudFilterPaneComponent pane = (PointCloudFilterPaneComponent) n;

                    boolean keep;

                    int index = pane.getComboboxPointCloudFilteringType().getSelectionModel().getSelectedIndex();
                    keep = index == 0;

                    pointcloudFilters.add(new PointcloudFilter(pane.getCsvFile(),
                            Float.valueOf(pane.getTextfieldPointCloudErrorMargin().getText()),
                            keep));
                }
            }

            voxelParameters.setPointcloudFilters(pointcloudFilters);
            
            //voxelParameters.setPointcloudErrorMargin(Float.valueOf(textfieldPointCloudErrorMargin.getText()));
            //voxelParameters.setPointcloudFile(new File(textfieldPointCloudPath.getText()));
        }
        
        if(checkboxCustomLaserSpecification.isSelected()){
            try{
                voxelParameters.setLaserSpecification(new LaserSpecification(Double.valueOf(textFieldBeamDiameterAtExit.getText()), Double.valueOf(textFieldBeamDivergence.getText()), "custom"));
            }catch(Exception ex){
                showErrorDialog(new Exception("Cannot parse laser specification values !", ex));
            }
            
        }else{
            voxelParameters.setLaserSpecification(comboboxLaserSpecification.getSelectionModel().getSelectedItem());
        }
        
        
        LADParams ladParameters = new LADParams();
        
        ladParameters.setLadType(comboboxLADChoice.getSelectionModel().getSelectedItem());
            
        if(radiobuttonLADHomogeneous.isSelected()){
            ladParameters.setLadEstimationMode(0);
        }else{
            ladParameters.setLadEstimationMode(1);
        }
        
        if(comboboxLADChoice.getSelectionModel().getSelectedItem() == TWO_PARAMETER_BETA){
            try{
                ladParameters.setLadBetaFunctionAlphaParameter(Float.valueOf(textFieldTwoBetaAlphaParameter.getText()));
                ladParameters.setLadBetaFunctionBetaParameter(Float.valueOf(textFieldTwoBetaBetaParameter.getText()));
            }catch(Exception ex){
                Exception e = new Exception("Two-parameter beta function selected but alpha and beta parameters are not valid", ex);
                logger.error(e.getMessage(), e);
                showErrorDialog(e);
            }
        }
        
        if(comboboxLADChoice.getSelectionModel().getSelectedItem() == ELLIPSOIDAL){
            try{
                ladParameters.setLadBetaFunctionAlphaParameter(Float.valueOf(textFieldTwoBetaAlphaParameter.getText()));
            }catch(Exception ex){
                Exception e = new Exception("Ellipsoidal function selected but alpha parameter is not valid", ex);
                logger.error(e.getMessage(), e);
                showErrorDialog(e);
            }
        }
        
        voxelParameters.setLadParams(ladParameters);

        voxelParameters.infos.setMaxPAD(Float.valueOf(textFieldPADMax.getText()));
        voxelParameters.setTransmittanceMode(1);
        voxelParameters.setPathLengthMode("B");
        /*voxelParameters.setTransmittanceMode(comboboxTransMode.getSelectionModel().getSelectedItem());
        voxelParameters.setPathLengthMode(comboboxPathLengthMode.getSelectionModel().getSelectedItem());*/

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
            
            for(File f : selectedFiles){
                addFileToTaskList(f);
            }
            
            //listViewTaskList.getItems().addAll(selectedFiles);
        }
    }

    @FXML
    private void onActionButtonLoadSelectedTask(ActionEvent event) {

        File selectedFile = listViewTaskList.getSelectionModel().getSelectedItem().getLinkedFile();

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
                            listViewHemiPhotoSensorPositions.getItems().setAll(hemiParameters.getSensorPositions());
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
                    
                    if(type.equals("transmittance")){
                        
                        textfieldVoxelFilePathTransmittance.setText(params.getInputFile().getAbsolutePath());
                                
                        tabPaneVirtualMeasures.getSelectionModel().select(0);
                        comboboxChooseDirectionsNumber.getSelectionModel().select(new Integer(params.getDirectionsNumber()));
                        textfieldDirectionRotationTransmittanceMap.setText(String.valueOf(params.getDirectionsRotation()));
                        checkboxTransmittanceMapToricity.setSelected(params.isToricity());
                        
                        checkboxGenerateBitmapFile.setSelected(params.isGenerateBitmapFile());

                        if (params.isGenerateBitmapFile() && params.getBitmapFile() != null) {
                            textfieldOutputBitmapFilePath.setText(params.getBitmapFile().getAbsolutePath());
                        }
                        
                        checkboxGenerateTextFile.setSelected(params.isGenerateTextFile());
                        
                        if(params.isGenerateTextFile() && params.getTextFile() != null){
                            textfieldOutputTextFilePath.setText(params.getTextFile().getAbsolutePath());
                        }
                        
                        if (params.getPositions() != null) {
                            listViewTransmittanceMapSensorPositions.getItems().setAll(params.getPositions());
                        }

                        textfieldLatitudeRadians.setText(String.valueOf(params.getLatitudeInDegrees()));
                        
                        data.clear();

                        List<SimulationPeriod> simulationPeriods = params.getSimulationPeriods();

                        if (simulationPeriods != null) {
                            data.addAll(simulationPeriods);
                        }

                    }else{
                        
                        textfieldVoxelFilePathCanopyAnalyzer.setText(params.getInputFile().getAbsolutePath());
                        
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
                    

                }else if(type.equals("merging")){
                    
                    VoxMergingCfg cfg = new VoxMergingCfg();
                    cfg.readConfiguration(selectedFile);
                    
                    tabPaneVoxelisation.getSelectionModel().select(2);

                    List<File> files = cfg.getFiles();

                    if (files != null) {
                        listViewProductsFiles.getItems().addAll(files);
                    }
                    textFieldOutputFileMerging.setText(cfg.getOutputFile().getAbsolutePath());
                    textFieldPADMax.setText(String.valueOf(cfg.getVoxelParameters().infos.getMaxPAD()));
                    
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
                                
                    VoxelParameters voxelParameters = ((VoxelAnalysisCfg)cfg).getVoxelParameters();
                    checkboxWriteShotSegment.setSelected(((VoxelAnalysisCfg)cfg).isExportShotSegment());
                                        
                    LaserSpecification laserSpecification = voxelParameters.getLaserSpecification();
                    if(laserSpecification != null){
                        if(laserSpecification.getName().equals("custom")){
                            checkboxCustomLaserSpecification.setSelected(true);
                            DecimalFormatSymbols symb = new DecimalFormatSymbols();
                            symb.setDecimalSeparator('.');
                            DecimalFormat formatter = new DecimalFormat("#####.######", symb);

                            textFieldBeamDiameterAtExit.setText(formatter.format(laserSpecification.getBeamDiameterAtExit()));
                            textFieldBeamDivergence.setText(formatter.format(laserSpecification.getBeamDivergence()));
                        }else{
                            checkboxCustomLaserSpecification.setSelected(false);
                            comboboxLaserSpecification.getSelectionModel().select(laserSpecification);
                        }
                    }

                    textFieldResolution.setText(String.valueOf(voxelParameters.infos.getResolution()));

                    DTMFilteringParams dtmFilteringParams = voxelParameters.getDtmFilteringParams();
                    if(dtmFilteringParams == null){
                        dtmFilteringParams = new DTMFilteringParams();
                    }
                    
                    checkboxUseDTMFilter.setSelected(dtmFilteringParams.useDTMCorrection());
                    File tmpFile = dtmFilteringParams.getDtmFile();
                    if (tmpFile != null) {
                        textfieldDTMPath.setText(tmpFile.getAbsolutePath());
                        textfieldDTMValue.setText(String.valueOf(dtmFilteringParams.getMinDTMDistance()));
                        checkboxApplyVOPMatrix.setSelected(dtmFilteringParams.isUseVOPMatrix());
                    }                    

                    checkboxUsePointcloudFilter.setSelected(voxelParameters.isUsePointCloudFilter());
                    List<PointcloudFilter> pointcloudFilters = voxelParameters.getPointcloudFilters();
                    if (pointcloudFilters != null) {

                        clearPointcloudFiltersPane();

                        for (PointcloudFilter filter : pointcloudFilters) {
                            PointCloudFilterPaneComponent pane = addPointcloudFilterComponent();
                            pane.setCSVFile(filter.getPointcloudFile());
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

                    checkboxUsePopMatrix.setSelected(((VoxelAnalysisCfg)cfg).isUsePopMatrix());
                    checkboxUseSopMatrix.setSelected(((VoxelAnalysisCfg)cfg).isUseSopMatrix());
                    checkboxUseVopMatrix.setSelected(((VoxelAnalysisCfg)cfg).isUseVopMatrix());
                    
                    if(type.equals("voxelisation-ALS") || type.equals("multi-voxelisation")){
                        
                        List<Integer> classifiedPointsToDiscard = ((ALSVoxCfg)cfg).getClassifiedPointsToDiscard();

                        for (Integer i : classifiedPointsToDiscard) {
                            listviewClassifications.getItems().get(i).setSelected(false);
                        }
                    }else if(type.equals("voxelisation-TLS")){
                        
                        filteringPaneController.setFilters(((TLSVoxCfg)cfg).getEchoFilters());
                        checkboxEmptyShotsFilter.setSelected(((TLSVoxCfg)cfg).isEnableEmptyShotsFiltering());
                    }

                    textFieldPADMax.setText(String.valueOf(((VoxelAnalysisCfg)cfg).getVoxelParameters().infos.getMaxPAD()));

                    popMatrix = ((VoxelAnalysisCfg)cfg).getPopMatrix();
                    sopMatrix = ((VoxelAnalysisCfg)cfg).getSopMatrix();
                    vopMatrix = ((VoxelAnalysisCfg)cfg).getVopMatrix();

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

                    List<Filter> filters = ((VoxelAnalysisCfg)cfg).getShotFilters();
                    if (filters != null) {
                        listviewFilters.getItems().clear();
                        listviewFilters.getItems().addAll(filters);
                    }

                    if (((VoxelAnalysisCfg)cfg).getVoxelParameters().getEchoesWeightParams().getWeightingMode() == EchoesWeightParams.WEIGHTING_NONE) {
                        checkboxEnableWeighting.setSelected(false);
                    } else {
                        checkboxEnableWeighting.setSelected(true);
                        fillWeightingData(((VoxelAnalysisCfg)cfg).getVoxelParameters().getEchoesWeightParams().getWeightingData());
                    }
                    
                    LADParams ladParameters = voxelParameters.getLadParams();
                    if(ladParameters == null){
                        ladParameters = new LADParams();
                    }
                    
                    comboboxLADChoice.getSelectionModel().select(ladParameters.getLadType());
                    radiobuttonLADHomogeneous.setSelected(ladParameters.getLadEstimationMode() == 0);
                    textFieldTwoBetaAlphaParameter.setText(String.valueOf(ladParameters.getLadBetaFunctionAlphaParameter()));
                    textFieldTwoBetaBetaParameter.setText(String.valueOf(ladParameters.getLadBetaFunctionBetaParameter()));
                    
                    /*comboboxTransMode.getSelectionModel().select(new Integer(voxelParameters.getTransmittanceMode()));
                    comboboxPathLengthMode.getSelectionModel().select(voxelParameters.getPathLengthMode());*/
                    
                    if(type.equals("voxelisation-ALS") || type.equals("multi-voxelisation")){
                        
                        tabPaneVoxelisation.getSelectionModel().select(0);
                        
                        if(((ALSVoxCfg)cfg).getInputType() != SHOTS_FILE){
                            textFieldTrajectoryFileALS.setText(((ALSVoxCfg)cfg).getTrajectoryFile().getAbsolutePath());
                            trajectoryFile = ((ALSVoxCfg)cfg).getTrajectoryFile();
                        }

                        GroundEnergyParams groundEnergyParameters = ((ALSVoxCfg)cfg).getVoxelParameters().getGroundEnergyParams();
                        if(groundEnergyParameters == null){
                            groundEnergyParameters = new GroundEnergyParams();
                        }
                        
                        checkboxCalculateGroundEnergy.setSelected(groundEnergyParameters.isCalculateGroundEnergy());
                        if (groundEnergyParameters.getGroundEnergyFile() != null) {
                            comboboxGroundEnergyOutputFormat.getSelectionModel().select(groundEnergyParameters.getGroundEnergyFileFormat());
                            textFieldOutputFileGroundEnergy.setText(groundEnergyParameters.getGroundEnergyFile().getAbsolutePath());
                        }

                        if(type.equals("voxelisation-ALS")){
                            
                            textFieldInputFileALS.setText(((ALSVoxCfg)cfg).getInputFile().getAbsolutePath());
                            selectALSInputMode(((ALSVoxCfg)cfg).getInputFile());
                            
                            textFieldOutputFileALS.setText(((ALSVoxCfg)cfg).getOutputFile().getAbsolutePath());
                            checkboxMultiResAfterMode2.setSelected(((ALSVoxCfg)cfg).getVoxelParameters().getNaNsCorrectionParams().isActivate());
                            textfieldNbSamplingThresholdMultires.setText(String.valueOf(((ALSVoxCfg)cfg).getVoxelParameters().getNaNsCorrectionParams().getNbSamplingThreshold()));
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
                                textFieldResolution.setText(String.valueOf(inputs.get(0).voxelParameters.infos.getResolution()));
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
                                comboboxModeTLS.getSelectionModel().select(5);
                                break;
                            case PTG_PROJECT:
                                comboboxModeTLS.getSelectionModel().select(3);
                                break;
                            case PTX_PROJECT:
                                comboboxModeTLS.getSelectionModel().select(2);
                                break;
                        }

                        checkboxMergeAfter.setSelected(((TLSVoxCfg)cfg).getVoxelParameters().isMergingAfter());

                        if (((TLSVoxCfg)cfg).getVoxelParameters().getMergedFile() != null) {
                            textFieldMergedFileName.setText(((TLSVoxCfg)cfg).getVoxelParameters().getMergedFile().getName());
                        }

                        List<LidarScan> matricesAndFiles = ((TLSVoxCfg)cfg).getLidarScans();
                        if (matricesAndFiles != null) {
                            items = matricesAndFiles;
                            listviewRxpScans.getItems().setAll(items);
                        }
                    }
                    
                     if(type.equals("voxelisation-ALS") || type.equals("voxelisation-TLS")){

                        voxelSpacePanelVoxelizationController.getTextFieldEnterXMin().setText(String.valueOf(voxelParameters.infos.getMinCorner().x));
                        voxelSpacePanelVoxelizationController.getTextFieldEnterYMin().setText(String.valueOf(voxelParameters.infos.getMinCorner().y));
                        voxelSpacePanelVoxelizationController.getTextFieldEnterZMin().setText(String.valueOf(voxelParameters.infos.getMinCorner().z));

                        voxelSpacePanelVoxelizationController.getTextFieldEnterXMax().setText(String.valueOf(voxelParameters.infos.getMaxCorner().x));
                        voxelSpacePanelVoxelizationController.getTextFieldEnterYMax().setText(String.valueOf(voxelParameters.infos.getMaxCorner().y));
                        voxelSpacePanelVoxelizationController.getTextFieldEnterZMax().setText(String.valueOf(voxelParameters.infos.getMaxCorner().z));

                        voxelSpacePanelVoxelizationController.getTextFieldXNumber().setText(String.valueOf(voxelParameters.infos.getSplit().x));
                        voxelSpacePanelVoxelizationController.getTextFieldYNumber().setText(String.valueOf(voxelParameters.infos.getSplit().y));
                        voxelSpacePanelVoxelizationController.getTextFieldZNumber().setText(String.valueOf(voxelParameters.infos.getSplit().z));

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

        ObservableList<TaskElement> selectedItems = listViewTaskList.getSelectionModel().getSelectedItems();
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
    
    private void exportDartMaket(final File voxelFile){
        
        Stage exportDartFrame = new Stage();
        DartExporterFrameController controller;
        
        Parent root;
        
        if(Util.checkIfVoxelFile(voxelFile)){
            
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/export/DartExporterDialog.fxml"));
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
        }else{
            
            logger.error("File is not a voxel file: " + voxelFile.getAbsolutePath());
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("Incorrect file");
            alert.setContentText("File is corrupted or cannot be read!\n"
                    + "Do you want to keep it?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.CANCEL) {
                listViewProductsFiles.getItems().remove(voxelFile);
            }
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

    private BoundingBox3d calculateAutomaticallyMinAndMax(File file, boolean quick) {
        
        final BoundingBox3d boundingBox = fr.amap.lidar.amapvox.util.Util.getBoundingBoxOfPoints(file, resultMatrix, false, getListOfClassificationPointToDiscard());

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

                                final BoundingBox3d boundingBox = fr.amap.lidar.amapvox.util.Util.getBoundingBoxOfPoints(new File(textFieldInputFileALS.getText()), resultMatrix, quick, getListOfClassificationPointToDiscard());
                                
                                Point3d minPoint = boundingBox.min;
                                Point3d maxPoint = boundingBox.max;
                                
                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {
                                        voxelSpacePanelVoxelizationController.getTextFieldEnterXMin().setText(String.valueOf(minPoint.x));
                                        voxelSpacePanelVoxelizationController.getTextFieldEnterYMin().setText(String.valueOf(minPoint.y));
                                        voxelSpacePanelVoxelizationController.getTextFieldEnterZMin().setText(String.valueOf(minPoint.z));

                                        voxelSpacePanelVoxelizationController.getTextFieldEnterXMax().setText(String.valueOf(maxPoint.x));
                                        voxelSpacePanelVoxelizationController.getTextFieldEnterYMax().setText(String.valueOf(maxPoint.y));
                                        voxelSpacePanelVoxelizationController.getTextFieldEnterZMax().setText(String.valueOf(maxPoint.z));
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

    @FXML
    private void onActionButtonResetToIdentity(ActionEvent event) {

        resetMatrices();
        fillResultMatrix(resultMatrix);
    }

    @FXML
    private void onActionButtonResetPadLimitsToDefault(ActionEvent event) {
        textFieldPADMax.setText("5");
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

    @FXML
    private void onActionCheckboxUsePointcloudFilter(ActionEvent event) {
    }

    @FXML
    private void onActionButtonAddPointcloudFilter(ActionEvent event) {

        addPointcloudFilterComponent();
    }

    private PointCloudFilterPaneComponent addPointcloudFilterComponent() {

        final PointCloudFilterPaneComponent pcfpc = new PointCloudFilterPaneComponent();

        pcfpc.getButtonRemovePointCloudFilter().setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                vBoxPointCloudFiltering.getChildren().remove(pcfpc);
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
                    
                    lastFCOpenPointCloudFile = selectedFile;
                    
                    textFileParserFrameController.setColumnAssignment(true);
                    textFileParserFrameController.setColumnAssignmentValues("Ignore", "X", "Y", "Z");

                    textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 1);
                    textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 2);
                    textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 3);
                    textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(3, 4);
                    
                    textFileParserFrameController.setHeaderExtractionEnabled(true);
                    textFileParserFrameController.setSeparator(",");

                    try{
                        textFileParserFrameController.setTextFile(selectedFile);
                    }catch(IOException ex){
                        showErrorDialog(ex);
                        return;
                    }

                    Stage textFileParserFrame = textFileParserFrameController.getStage();
                    textFileParserFrame.show();

                    textFileParserFrame.setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {

                            CSVFile file =  new CSVFile(selectedFile.getAbsolutePath());
                            
                            file.setColumnSeparator(textFileParserFrameController.getSeparator());
                            file.setColumnAssignment(textFileParserFrameController.getAssignedColumnsItemsMap());
                            file.setNbOfLinesToRead(textFileParserFrameController.getNumberOfLines());
                            file.setNbOfLinesToSkip(textFileParserFrameController.getSkipLinesNumber());
                            file.setContainsHeader(textFileParserFrameController.getHeaderIndex() != -1);
                            file.setHeaderIndex(textFileParserFrameController.getHeaderIndex());
                            
                            pcfpc.setCSVFile(file);
                        }
                    });
                    
                    
                    
                }
            }
        });

        pcfpc.disableContent(!checkboxUsePointcloudFilter.isSelected());

        vBoxPointCloudFiltering.getChildren().add(pcfpc);

        return pcfpc;
    }

    private void clearPointcloudFiltersPane() {

        vBoxPointCloudFiltering.getChildren().clear();
    }

    @FXML
    private void onActionButtonGetBoundingBox(ActionEvent event) {

        Mat4D vopMatrixTmp = MatrixUtility.convertMatrix4dToMat4D(vopMatrix);
        if(vopMatrixTmp == null && checkboxUseVopMatrix.isSelected()){vopMatrixTmp = Mat4D.identity();}
        
        final Mat4D transfMatrix = vopMatrixTmp;
        
        ObservableList<Node> children = vBoxPointCloudFiltering.getChildren();

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

                                CSVFile file = pane.getCsvFile();

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

                                    voxelSpacePanelVoxelizationController.getTextFieldEnterXMin().setText(String.valueOf(boundingBox.min.x));
                                    voxelSpacePanelVoxelizationController.getTextFieldEnterYMin().setText(String.valueOf(boundingBox.min.y));
                                    voxelSpacePanelVoxelizationController.getTextFieldEnterZMin().setText(String.valueOf(boundingBox.min.z));

                                    voxelSpacePanelVoxelizationController.getTextFieldEnterXMax().setText(String.valueOf(boundingBox.max.x));
                                    voxelSpacePanelVoxelizationController.getTextFieldEnterYMax().setText(String.valueOf(boundingBox.max.y));
                                    voxelSpacePanelVoxelizationController.getTextFieldEnterZMax().setText(String.valueOf(boundingBox.max.z));
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
        
    }
    
    private void exportDartPlots(final File voxelFile){
        
        if(Util.checkIfVoxelFile(voxelFile)){
            
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
        }else{
            logger.error("File is not a voxel file: " + voxelFile.getAbsolutePath());
            Alert alert = new Alert(AlertType.CONFIRMATION);
            alert.setHeaderText("Incorrect file");
            alert.setContentText("File is corrupted or cannot be read!\n"
                    + "Do you want to keep it?");

            Optional<ButtonType> result = alert.showAndWait();

            if (result.get() == ButtonType.CANCEL) {
                listViewProductsFiles.getItems().remove(voxelFile);
            }
        }
        
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
    
    private void saveTransmittanceLightMapSim(File file) throws Exception{
        
            
        TransmittanceParameters transmParameters = new TransmittanceParameters();

        transmParameters.setInputFile(new File(textfieldVoxelFilePathTransmittance.getText()));
        transmParameters.setGenerateBitmapFile(checkboxGenerateBitmapFile.isSelected());
        transmParameters.setGenerateTextFile(checkboxGenerateTextFile.isSelected());
        transmParameters.setToricity(checkboxTransmittanceMapToricity.isSelected());

        if(checkboxGenerateBitmapFile.isSelected()){
            transmParameters.setBitmapFile(new File(textfieldOutputBitmapFilePath.getText()));
        }

        if(checkboxGenerateTextFile.isSelected()){
            transmParameters.setTextFile(new File(textfieldOutputTextFilePath.getText()));
        }
        
        transmParameters.setDirectionsNumber(comboboxChooseDirectionsNumber.getSelectionModel().getSelectedItem());
        transmParameters.setDirectionsRotation(Float.valueOf(textfieldDirectionRotationTransmittanceMap.getText()));

        transmParameters.setLatitudeInDegrees(Float.valueOf(textfieldLatitudeRadians.getText()));

        transmParameters.setSimulationPeriods(tableViewSimulationPeriods.getItems());
        
        transmParameters.setPositions(listViewTransmittanceMapSensorPositions.getItems());

        TransmittanceCfg cfg = new TransmittanceCfg(transmParameters);
        
        
        try {
            cfg.writeConfiguration(file, Global.buildVersion);
            addFileToTaskList(file);
        } catch (Exception ex) {
            throw new Exception("Cannot write configuration file", ex);
        }
    }
    
    private void saveExecuteTransmittanceLightMapSim(File file) throws Exception{
        
        saveTransmittanceLightMapSim(file);
        TaskElement taskElement = addFileToTaskList(file);
        if(taskElement != null){
            executeProcess(taskElement);
        }
    }
    
    private void executeTransmittanceLightMapSim() throws Exception{
        
        File temporaryFile  = File.createTempFile("cfg_temp", ".xml");
        saveTransmittanceLightMapSim(temporaryFile);
        logger.info("temporary file created : "+temporaryFile.getAbsolutePath());
        
        TaskElement taskElement = addFileToTaskList(temporaryFile);
        if(taskElement != null){
            executeProcess(taskElement);
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
                VoxelFileChart voxelFileChart = new VoxelFileChart(file, file.getName());
                Color color = (Color)VoxelsToChart.DEFAULT_RENDERER.lookupSeriesPaint(listViewVoxelsFilesChart.getItems().size());
                voxelFileChart.getSeriesParameters().setColor(color);
                listViewVoxelsFilesChart.getItems().add(voxelFileChart);
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
        
        switch (tabIndex) {
            case 0:
                chartWindowTitle = "Profile chart";
                break;
            case 1:
                chartWindowTitle = "Two variables statistics chart";
                break;
            default:
                chartWindowTitle = "Chart";
                break;
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
        Color seriesPaint = (Color) VoxelsToChart.DEFAULT_RENDERER.lookupSeriesPaint(0);
        
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

    @FXML
    private void onActionButtonSaveButterflyRemovingCfg(ActionEvent event) {
        
        File selectedFile = fileChooserChooseOutputCfgFileButterflyRemover.showSaveDialog(stage);
        
        if(selectedFile != null){
            try {
                saveButterflyRemovercfg(selectedFile);
                addFileToTaskList(selectedFile);
            } catch (Exception ex) {
                showErrorDialog(ex);
            }
        }
    }

    @FXML
    private void onActionButtonSaveExecuteButterflyRemovingCfg(ActionEvent event) {
        
         File selectedFile = fileChooserChooseOutputCfgFileButterflyRemover.showSaveDialog(stage);
        
        if(selectedFile != null){
            try {
                saveButterflyRemovercfg(selectedFile);
                TaskElement task = addFileToTaskList(selectedFile);
                executeProcess(task);
            } catch (Exception ex) {
                showErrorDialog(ex);
            }
        }
    }

    
    private void fitVoxelSpaceToContent(final File voxelFile){
        
        if(voxelFile.exists() && voxelFile.isFile()){
            try {
                
                if(fcSaveVoxelFileForAreaExtracting == null){
                    fcSaveVoxelFileForAreaExtracting = new FileChooserContext();

                    if(fcOpenVoxelFileForAreaExtracting != null){
                        fcSaveVoxelFileForAreaExtracting.fc.setInitialDirectory(fcOpenVoxelFileForAreaExtracting.lastSelectedFile.getParentFile());
                    }
                }
                
                File selectedFile = fcSaveVoxelFileForAreaExtracting.showSaveDialog(stage);

                if(selectedFile != null){
                
                    VoxelSpace voxelSpace = VoxelSpaceUtil.fitVoxelSpaceToContent(voxelFile);
                    
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))) {
                        writer.write(voxelSpace.getVoxelSpaceInfos()+"\n");
                        
                        for(int i=0;i<voxelSpace.voxels.size();i++){
                            Voxel voxel = (Voxel)voxelSpace.voxels.get(i);
                            writer.write(voxel+"\n");
                        }
                    }
                    
                    addFileToProductsList(selectedFile);
                }
                
                
            } catch (Exception ex) {
                showErrorDialog(ex);
            }
            
        }
    }
    
    private void exportMeshObj(final File voxelFile){
        
        if(Util.checkIfVoxelFile(voxelFile)){
            
            try {
                objExporterController.setVoxelFile(voxelFile);
            } catch (Exception ex) {
                showErrorDialog(ex);
            }
        }
        
        objExporterController.getStage().show();
    }

    
    private void editVoxelSpace(final File voxelFile){
        
        if(editingFrameOpened){
           return; 
        }
        
        editingFrameOpened = true;
        voxelsToRemove.clear();
        
        final String attributeToView = "PadBVTotal";

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

                            SimpleViewer viewer3D = new SimpleViewer((int) (SCREEN_WIDTH / 4.0d), (int) (SCREEN_HEIGHT / 4.0d), (int) (SCREEN_WIDTH / 1.5d), (int) (SCREEN_HEIGHT / 2.0d), voxelFile.toString());

                            fr.amap.viewer3d.object.scene.Scene scene = viewer3D.getScene();

                            /**
                             * *VOXEL SPACE**
                             */
                            updateMessage("Loading voxel space: " + voxelFile.getAbsolutePath());

                            final VoxelSpaceSceneObject voxelSpace = new VoxelSpaceSceneObject(voxelFile);
                            voxelSpace.setMousePickable(true);

                            voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                                @Override
                                public void voxelSpaceCreationProgress(int progress) {
                                    updateProgress(progress, 100);
                                }
                            });

                            voxelSpace.loadVoxels();
                            float voxelResolution = voxelSpace.data.getVoxelSpaceInfos().getResolution();
                            
                            VoxelFileReader reader = new VoxelFileReader(voxelFile);
                            VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

                            final SceneObject boundingBox = new SimpleSceneObject();
                            boundingBox.setMesh(GLMeshFactory.createBoundingBox((float) infos.getMinCorner().x,
                                    (float) infos.getMinCorner().y,
                                    (float) infos.getMinCorner().z,
                                    (float) infos.getMaxCorner().x,
                                    (float) infos.getMaxCorner().y,
                                    (float) infos.getMaxCorner().z));
                            
                            SimpleShader s = new SimpleShader();
                            s.setColor(new Vec3F(1, 0, 0));
                            boundingBox.setShader(s);
                            boundingBox.setDrawType(GLMesh.DrawType.LINES);
                            scene.addSceneObject(boundingBox);

                            /*
                                 * Voxel information
                             */
                            StringToImage stringToImage = new StringToImage(1024, 1024);
                            stringToImage.setAdaptableFontSize(true);
                            stringToImage.setBackgroundColor(new Color(255, 255, 255, 127));
                            stringToImage.setTextColor(new Color(0, 0, 0, 255));

                            BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_ARGB);

                            Texture texture = new Texture(image);

                            int pickingInfoObjectWidth = viewer3D.getWidth() / 5;
                            int pickingInfoObjectHeight = viewer3D.getHeight() / 5;

                            SceneObject pickingInfoObject = SceneObjectFactory.createTexturedPlane(new Vec3F(viewer3D.getWidth() - pickingInfoObjectWidth, viewer3D.getHeight() - pickingInfoObjectHeight, 0), pickingInfoObjectWidth, pickingInfoObjectHeight, texture);

                            pickingInfoObject.setShader(new TextureShader());
                            pickingInfoObject.setDrawType(GLMesh.DrawType.TRIANGLES);

                            SceneObject sceneObjectSelectedVox = new SimpleSceneObject(GLMeshFactory.createBoundingBox(
                                    -voxelResolution / 2.0f,
                                    -voxelResolution / 2.0f,
                                    -voxelResolution / 2.0f,
                                    voxelResolution / 2.0f,
                                    voxelResolution / 2.0f,
                                    voxelResolution / 2.0f), false);

                            SimpleShader simpleShader = new SimpleShader();
                            simpleShader.setColor(new Vec3F(1, 0, 0));
                            sceneObjectSelectedVox.setVisible(false);
                            sceneObjectSelectedVox.setShader(simpleShader);
                            sceneObjectSelectedVox.setDrawType(GLMesh.DrawType.LINES);

                            viewer3D.getScene().addSceneObject(sceneObjectSelectedVox);
                            
                            final SimpleObjectProperty<VoxelObject> selectedVoxel = new SimpleObjectProperty<>();

                            SceneObjectListener listener = new SceneObjectListener() {
                                @Override
                                public void clicked(SceneObject sceneObject, MousePicker mousePicker, Point3D intersection) {

                                    Vec3F camLocation = viewer3D.getScene().getCamera().getLocation();

                                    selectedVoxel.set(voxelSpace.doPicking(mousePicker));

                                    if (selectedVoxel.get() != null) {

                                        String[][] lines = new String[voxelSpace.getColumnsNames().length][2];

                                        for (int i = 0; i < voxelSpace.getColumnsNames().length; i++) {

                                            lines[i][0] = voxelSpace.getColumnsNames()[i];

                                            float attribut = selectedVoxel.get().getAttributs()[i];
                                            if (Float.isNaN(attribut)) {
                                                lines[i][1] = "NaN";
                                            } else {
                                                lines[i][1] = String.valueOf(Math.round(attribut * 1000.0f) / 1000.0f);
                                            }

                                        }

                                        arrangeText(lines);

                                        String text = "";
                                        for (int i = 0; i < voxelSpace.getColumnsNames().length; i++) {

                                            String attribut = lines[i][0] + " " + lines[i][1];
                                            text += attribut + "\n";
                                        }

                                        stringToImage.setText(text, 0, 0);

                                        texture.setBufferedImage(stringToImage.buildImage());
                                        Point3f voxelPosition = voxelSpace.getVoxelPosition(selectedVoxel.get().$i, selectedVoxel.get().$j, selectedVoxel.get().$k);

                                        sceneObjectSelectedVox.setPosition(new Point3F(voxelPosition.x, voxelPosition.y, voxelPosition.z));
                                        sceneObjectSelectedVox.setVisible(true);
                                        pickingInfoObject.setVisible(true);
                                    } else {
                                        sceneObjectSelectedVox.setVisible(false);
                                        pickingInfoObject.setVisible(false);
                                    }
                                }
                            };
                            
                            final SimpleIntegerProperty currentZCropIndex = new SimpleIntegerProperty(0);
                            
                            viewer3D.addEventListener(new EventManager(null, new InputKeyListener()) {
                                @Override
                                public void updateEvents() {
                                    
                                    if(this.keyboard.isKeyClicked(KeyEvent.VK_DELETE)){
                                        if(selectedVoxel.get() != null){
                                            selectedVoxel.get().setAlpha(0);
                                            selectedVoxel.get().isHidden = true;
                                            voxelSpace.updateVao();
                                            sceneObjectSelectedVox.setVisible(false);
                                            pickingInfoObject.setVisible(false);
                                            voxelsToRemove.add(new Point3i(selectedVoxel.get().$i, selectedVoxel.get().$j, selectedVoxel.get().$k));
                                        }
                                    }
                                }
                            });

                            voxelSpace.addSceneObjectListener(listener);

                            voxelSpace.changeCurrentAttribut(attributeToView);
                            voxelSpace.setShader(new InstanceLightedShader());
                            voxelSpace.setDrawType(GLMesh.DrawType.TRIANGLES);
                            scene.addSceneObject(voxelSpace);
                            
                            scene.addSceneObjectAsHud(pickingInfoObject);

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

                            scalePlane.setShader(new TextureShader());
                            scalePlane.setDrawType(GLMesh.DrawType.TRIANGLES);
                            scene.addSceneObjectAsHud(scalePlane);

                            

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
                            scene.setLightPosition(new Point3F(voxelSpace.getGravityCenter().x, voxelSpace.getGravityCenter().y, voxelSpace.getGravityCenter().z + voxelSpace.widthZ + 100));

                            /**
                             * *camera**
                             */
                            TrackballCamera trackballCamera = new TrackballCamera();
                            trackballCamera.setPivot(voxelSpace);
                            trackballCamera.setLocation(new Vec3F(voxelSpace.getGravityCenter().x + voxelSpace.widthX, voxelSpace.getGravityCenter().y + voxelSpace.widthY, voxelSpace.getGravityCenter().z + voxelSpace.widthZ));
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

                                        viewer3D.getRenderFrame().addWindowListener(new WindowAdapter() {

                                            @Override
                                            public void windowResized(com.jogamp.newt.event.WindowEvent we) {

                                                Window window = (Window) we.getSource();
                                                final double height = window.getHeight();

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
                                                        
                                                        if(fcSaveVoxelFileForAreaExtracting == null){
                                                            fcSaveVoxelFileForAreaExtracting = new FileChooserContext();

                                                            if(fcOpenVoxelFileForAreaExtracting != null){
                                                                fcSaveVoxelFileForAreaExtracting.fc.setInitialDirectory(fcOpenVoxelFileForAreaExtracting.lastSelectedFile.getParentFile());
                                                            }
                                                        }

                                                        File selectedFile = fcSaveVoxelFileForAreaExtracting.showSaveDialog(stage);
                                                        
                                                        if(selectedFile != null){
                                                            VoxelFileReader reader;
                                                            BufferedWriter writer = null;

                                                            try {
                                                                reader = new VoxelFileReader(voxelFile, true);
                                                                VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

                                                                writer = new BufferedWriter(new FileWriter(selectedFile));

                                                                writer.write(infos.toString()+"\n");

                                                                Iterator<Voxel> iterator = reader.iterator();

                                                                while(iterator.hasNext()){

                                                                    Voxel voxel = iterator.next();

                                                                    if(voxelsToRemove.contains(new Point3i(voxel.$i, voxel.$j, voxel.$k))){
                                                                        voxel.PadBVTotal = 0;
                                                                        voxel.nbEchos = 0;
                                                                        voxel.transmittance = 1;
                                                                        voxel.bvIntercepted = 0;
                                                                    }

                                                                    writer.write(voxel+"\n");
                                                                }

                                                                writer.close();

                                                                addFileToProductsList(selectedFile);


                                                            } catch (Exception ex) {
                                                                showErrorDialog(ex);
                                                            }finally{
                                                                try {
                                                                    if(writer != null){
                                                                        writer.close();
                                                                    }

                                                                } catch (IOException ex) {
                                                                    showErrorDialog(ex);
                                                                }
                                                            }
                                                        }
                                                        
                                                        editingFrameOpened = false;
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
                                        showErrorDialog(new Exception("Loading ToolBarFrame.fxml failed", e));
                                    } catch (Exception e) {
                                        showErrorDialog(new Exception("Error occured during toolbar init", e));
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
            showErrorDialog(new Exception("Cannot launch 3d view", ex));
        }
    }


    public Stage getStage() {
        return stage;
    }

    @FXML
    private void onActionButtonExecuteScript(ActionEvent event) {
        
        Script script = null;
                
        if(comboboxScript.getSelectionModel().getSelectedItem().equals("Daniel script")){
            
            script = new DanielScript(this);
        }
        
        if(script != null){
            script.launch();
        }
        
    }
    
    private void fillWeightingData(float[][] weightingData){
        
        String text = "";
        
        for(int i = 0;i<weightingData.length;i++){
            for(int j = 0;j<weightingData.length;j++){

                if(j != 0){
                    text += " ";
                }
                
                if(!Float.isNaN(weightingData[i][j])){
                    text += weightingData[i][j];
                }
                
                if(j == weightingData.length - 1 && i != weightingData.length - 1){
                    text += "\n";
                }
            }
        }
        
        textAreaWeighting.setText(text);
    }

    @FXML
    private void onActionButtonFillALSDefaultWeight(ActionEvent event) {
        
        fillWeightingData(EchoesWeightParams.DEFAULT_ALS_WEIGHTING);
    }

    @FXML
    private void onActionButtonFillTLSDefaultWeight(ActionEvent event) {
        fillWeightingData(EchoesWeightParams.DEFAULT_TLS_WEIGHTING);
    }
    /*@FXML
    private void onActionButtonGenerateShotsFile(ActionEvent event) {
        
    }*/

    @FXML
    private void onActionButtonExportALSLidarShots(ActionEvent event) {
        
        File alsFile = new File(textFieldInputFileALS.getText());
        
        if(!alsFile.exists()){
            showErrorDialog(new Exception("File does not exist."));
            return;
        }else if(!alsFile.isFile()){
            showErrorDialog(new Exception("Input is not a file."));
            return;
        }else if(trajectoryFile == null || !trajectoryFile.exists() || !trajectoryFile.isFile()){
            showErrorDialog(new Exception("Invalid trajectory file."));
            return;
        }
        
        FileChooser fc = new FileChooser();
        File selectedFile = fc.showSaveDialog(stage);
        
        if(selectedFile == null){
            return;
        }
        
        while(!selectedFile.getName().endsWith(".sht")){
            
            fc.setInitialFileName(selectedFile.getName()+".sht");
            fc.setInitialDirectory(new File(selectedFile.getParent()));
            selectedFile = fc.showSaveDialog(stage);
            
            if(selectedFile == null){
                return;
            }
        }

        PointsToShot pts = new PointsToShot(trajectoryFile, alsFile, MatrixUtility.convertMatrix4dToMat4D(vopMatrix));
        
        try {
            pts.init();
        } catch (Exception ex) {
            showErrorDialog(ex);
            return;
        }
        
        try {
            pts.write(selectedFile);
        } catch (Exception ex) {
            showErrorDialog(ex);
        }
    }

    @FXML
    private void onActionButtonSavePdf(ActionEvent event) {
        
        FileChooser fc = new FileChooser();
        
        File selectedFile = fc.showSaveDialog(stage);
        
        if(selectedFile != null){
            
            XYSeries serie = generatePDFSerie();
            
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))){
                
                writer.write("Angle(degrees) pdf\n");
                
                for(Object o : serie.getItems()){
                    writer.write(((XYDataItem)o).getX()+" "+((XYDataItem)o).getY()+"\n");
                }
                
            } catch (IOException ex) {
                showErrorDialog(ex);
            }
            
        }
        
        
    }

    @FXML
    private void onActionButtonSaveGTheta(ActionEvent event) {
        
        FileChooser fc = new FileChooser();
        
        File selectedFile = fc.showSaveDialog(stage);
        
        if(selectedFile != null){
            
            XYSeries serie = generateGThetaSerie();
            
            try(BufferedWriter writer = new BufferedWriter(new FileWriter(selectedFile))){
                
                writer.write("Angle(degrees) gtheta\n");
                
                for(Object o : serie.getItems()){
                    writer.write(((XYDataItem)o).getX()+" "+((XYDataItem)o).getY()+"\n");
                }
                
            } catch (IOException ex) {
                showErrorDialog(ex);
            }
            
        }
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
            case ".ptx":
            case ".PTX":
            case ".ptg":
            case ".PTG":
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
        
        //dataset.addSeries(serie2);
        
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
        
        //dataset.addSeries(serie5);
        
        //vertical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.VERTICAL);
        
        XYSeries serie6 = new XYSeries(distribution.getType(), false);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double pdf = distribution.getDensityProbability(Math.toRadians(angleInDegrees));
            serie6.add(angleInDegrees, pdf);
        }
        
        //dataset.addSeries(serie6);
        
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
        
        GTheta m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        //elliptical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ELLIPTICAL);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        //dataset.addSeries(serie);
        
        //erectophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.ERECTOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //extremophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.EXTREMOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //horizontal
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.HORIZONTAL);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        //dataset.addSeries(serie);
        
        //vertical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.VERTICAL);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        //dataset.addSeries(serie);
        
        //plagiophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLAGIOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //planophile
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLANOPHILE);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //spherical
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.SPHERIC);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
        
        //uniform
        distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.UNIFORM);
        
        serie = new XYSeries(distribution.getType(), false);
        
        m = new GTheta(distribution);
        m.buildTable(180);
        
        for(int i = 0 ; i < 180 ; i++){
            
            double angleInDegrees = i/2.0;
            double GTheta = m.getGThetaFromAngle(angleInDegrees, true);
            serie.add(angleInDegrees, GTheta);
        }
        
        dataset.addSeries(serie);
            
        ChartViewer viewer = new ChartViewer("GTheta", 500, 500, 1);
        viewer.insertChart(ChartViewer.createBasicChart("GTheta ~ Beam direction zenithal angle", dataset, "Beam direction zenithal angle (degrees)", "GTheta"));
        viewer.show();
    }

    public int getTlsVoxNbThreads() {
        return tlsVoxNbThreads;
    }

    public void setTlsVoxNbThreads(int tlsVoxNbThreads) {
        this.tlsVoxNbThreads = tlsVoxNbThreads;
    }
    
}
