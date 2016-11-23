package fr.amap.lidar.amapvox.gui.viewer3d;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import com.jogamp.newt.Window;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.WindowAdapter;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.lidar.format.jleica.ptg.PTGReader;
import fr.amap.commons.math.geometry.BoundingBox2F;
import fr.amap.commons.math.geometry.BoundingBox3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.lidar.amapvox.commons.LidarScan;
import fr.amap.commons.math.util.MatrixFileParser;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.commons.util.image.ScaleGradient;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.gui.DialogHelper;
import fr.amap.commons.javafx.io.FileChooserContext;
import fr.amap.lidar.amapvox.gui.HelpButtonController;
import fr.amap.lidar.amapvox.gui.PTGProjectExtractor;
import fr.amap.lidar.amapvox.gui.PTXProjectExtractor;
import fr.amap.lidar.amapvox.gui.RiscanProjectExtractor;
import fr.amap.lidar.amapvox.gui.SceneObjectPropertiesPanelController;
import fr.amap.lidar.amapvox.gui.SceneObjectWrapper;
import fr.amap.lidar.amapvox.voxelisation.configuration.PTXLidarScan;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import fr.amap.lidar.amapvox.voxviewer.FXNewtOverlap;
import fr.amap.lidar.amapvox.voxviewer.Viewer3D;
import fr.amap.lidar.amapvox.voxviewer.event.BasicEvent;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.AxisShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.ColorShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.InstanceLightedShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.PhongShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.SimpleShader;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.TextureShader;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.StringToImage;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.lidar.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.lidar.amapvox.voxviewer.object.scene.MousePicker;
import fr.amap.lidar.amapvox.voxviewer.object.scene.RasterSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObjectFactory;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObjectListener;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SimpleSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.VoxelObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.VoxelSpaceAdapter;
import fr.amap.lidar.amapvox.voxviewer.object.scene.VoxelSpaceSceneObject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Side;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;
import org.jdom2.JDOMException;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class Viewer3DPanelController implements Initializable {

    private Stage stage;
    private ResourceBundle resourceBundle;
    private final static Logger LOGGER = Logger.getLogger(Viewer3DPanelController.class);
    
    private static double SCREEN_WIDTH;
    private static double SCREEN_HEIGHT;
    
    /*Mode 1*/
    private File currentVoxelFile;
    private FileChooserContext fileChooserOpenDTMFile;
    private FileChooserContext fileChooserOpenMatrixFile;
    private Matrix4d rasterTransfMatrix;
    
    /*Mode 2*/
    private TreeItem<SceneObjectWrapper> root;
    private FileChooserContext sceneObjectChooser;
    private SceneObjectPropertiesPanelController sceneObjectPropertiesPanelController;
    private RiscanProjectExtractor riscanProjectExtractor;
    private PTXProjectExtractor ptxProjectExtractor;
    private PTGProjectExtractor ptgProjectExtractor;
    
    
    @FXML
    private CheckBox checkboxRaster1;
    @FXML
    private ComboBox<String> comboboxAttributeToView;
    @FXML
    private CheckBox checkboxRaster;
    @FXML
    private AnchorPane anchorPaneRasterParameters;
    @FXML
    private TextField textfieldRasterFilePath;
    @FXML
    private CheckBox checkboxUseTransformationMatrix;
    @FXML
    private Button buttonSetTransformationMatrix;
    @FXML
    private CheckBox checkboxFitRasterToVoxelSpace;
    @FXML
    private TextField textfieldRasterFittingMargin;
    @FXML
    private Button buttonOpen3DView;
    @FXML
    private TreeView<SceneObjectWrapper> treeViewSceneObjects;
    @FXML
    private TitledPane titledPaneSceneObjectProperties;
    @FXML
    private ScrollPane scrollPaneSceneObjectProperties;
    @FXML
    private RadioButton radioButtonMode1;
    @FXML
    private RadioButton radioButtonMode2;
    @FXML
    private VBox vboxMode1;
    @FXML
    private VBox vboxMode2;
    @FXML
    private Button helpButton3DViewer;
    @FXML
    private HelpButtonController helpButton3DViewerController;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        ToggleGroup group = new ToggleGroup();
        radioButtonMode1.setToggleGroup(group);
        radioButtonMode2.setToggleGroup(group);
        
        fileChooserOpenDTMFile = new FileChooserContext();
        fileChooserOpenDTMFile.fc.setTitle("Choose DTM file");
        fileChooserOpenDTMFile.fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"),
                new FileChooser.ExtensionFilter("DTM Files", "*.asc"));
        
        fileChooserOpenMatrixFile = new FileChooserContext();
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/SceneObjectPropertiesPanel.fxml"));
            AnchorPane sceneObjectPropertiesPane = loader.load();
            sceneObjectPropertiesPanelController = loader.getController();
            scrollPaneSceneObjectProperties.setContent(sceneObjectPropertiesPane);
        } catch (IOException ex) {
            LOGGER.error("Cannot load fxml file", ex);
        }
        
        treeViewSceneObjects.setCellFactory(new Callback<TreeView<SceneObjectWrapper>, TreeCell<SceneObjectWrapper>>() {
            @Override
            public TreeCell<SceneObjectWrapper> call(TreeView<SceneObjectWrapper> param) {
                
                return new SceneObjectTreeCell();
            }
        });
        //treeViewSceneObjects.setCellFactory(CheckBoxTreeCell.<SceneObjectWrapper>forTreeView());
        root = new TreeItem<>();
        root.setExpanded(true);
        treeViewSceneObjects.setRoot(root);
        
        
        sceneObjectChooser = new FileChooserContext();
        
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
        
        treeViewSceneObjects.setOnDragOver(dragOverEvent);
        treeViewSceneObjects.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles()) {
                    success = true;
                    for (File file : db.getFiles()) {
                        if (file != null) {
                            
                            try {
                                addSceneObjectToTree(file, Mat4D.identity());
                            } catch (Exception ex) {
                                DialogHelper.showErrorDialog(stage, ex);
                            }
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
        
        treeViewSceneObjects.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        treeViewSceneObjects.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<SceneObjectWrapper>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<SceneObjectWrapper>> observable, TreeItem<SceneObjectWrapper> oldValue, TreeItem<SceneObjectWrapper> newValue) {
                
                List<SceneObjectWrapper> selectedItems = getSelectedSceneObjectWrappers();
                
                if(selectedItems.size() > 0){
                    
                    sceneObjectPropertiesPanelController.setSceneObjectWrappers(selectedItems);
                    
                    SceneObjectWrapper lastSelectedItem = selectedItems.get(selectedItems.size() - 1);
                    if(lastSelectedItem.getSceneObject() != null){
                        sceneObjectPropertiesPanelController.setSceneObjectWrapper(lastSelectedItem);
                    }
                }
            }
        });
        
        riscanProjectExtractor = new RiscanProjectExtractor();
        ptxProjectExtractor = new PTXProjectExtractor();
        ptgProjectExtractor = new PTGProjectExtractor();
        
        /**Handle contextual menu for treeview**/
        MenuItem itemEnable = new MenuItem("Enable selection");
        MenuItem itemDisable = new MenuItem("Disable selection");
        
        itemEnable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<TreeItem<SceneObjectWrapper>> children = treeViewSceneObjects.getSelectionModel().getSelectedItems();
                
                for(TreeItem<SceneObjectWrapper> item : children){
                    if(item.getValue() != null){
                        item.getValue().setSelected(true);
                    }
                }
            }
        });
        
        itemDisable.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                ObservableList<TreeItem<SceneObjectWrapper>> children = treeViewSceneObjects.getSelectionModel().getSelectedItems();
                
                for(TreeItem<SceneObjectWrapper> item : children){
                    if(item.getValue() != null){
                        item.getValue().setSelected(false);
                    }
                }
            }
        });
        
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().setAll(itemEnable, itemDisable);
        
        treeViewSceneObjects.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                contextMenu.show(treeViewSceneObjects, Side.RIGHT, 0, 0);
            }
        });
        
        radioButtonMode2.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                vboxMode1.setDisable(newValue);
                vboxMode2.setDisable(!newValue);
                
            }
        });
        
        helpButton3DViewer.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(resourceBundle != null){
                    helpButton3DViewerController.showHelpDialog(resourceBundle.getString("help_3D_viewer"));
                }
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
    
    private List<SceneObjectWrapper> getSelectedSceneObjectWrappers(){
        
        ObservableList<TreeItem<SceneObjectWrapper>> selectedItems = treeViewSceneObjects.getSelectionModel().getSelectedItems();
        List<SceneObjectWrapper> selectedScObjects = new ArrayList<>();
        
        for(TreeItem<SceneObjectWrapper> item : selectedItems){
            
            if(item != null){
                if(!item.equals(root)){
                
                    if(item.getValue() != null){
                        selectedScObjects.add(item.getValue());
                    }
                }
            }
            
        }
        
        return selectedScObjects;
    }    
    
    public void updateCurrentVoxelFile(File voxelFile){
        
        this.currentVoxelFile = voxelFile;

        if (!VoxelFileReader.isFileAVoxelFile(voxelFile)) {
            return;
        }
        
        VoxelFileReader reader;
        try {
            reader = new VoxelFileReader(voxelFile);
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
        } catch (Exception ex) {
            LOGGER.error("Cannot read voxel file", ex);
        }
    }
    
    public void setStage(Stage stage){
        
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
    
    public void setResourceBundle(ResourceBundle bundle){
        this.resourceBundle = bundle;
    }
    
    private SceneObjectWrapper addSceneObject(File file){
        
        SceneObjectWrapper sceneObjectWrapper = new SceneObjectWrapper(file, new ProgressBar(0));
        root.getChildren().add(new TreeItem<>(sceneObjectWrapper));
        return sceneObjectWrapper;
    }
    
    private List<SceneObject> getSelectedItemsFromTree(){
        
        ObservableList<TreeItem<SceneObjectWrapper>> childrens = root.getChildren();
        List<SceneObject> sceneObjects = new ArrayList<>();
        
        for(TreeItem<SceneObjectWrapper> children : childrens){
            
            SceneObjectWrapper sceneObjectWrapper = children.getValue();
            if(sceneObjectWrapper.isSelected() && !(sceneObjectWrapper.getProgressBar().getProgress() < 1)){
                sceneObjects.add(sceneObjectWrapper.getSceneObject());
            }
            
        }
        
        return sceneObjects;
    }

    @FXML
    private void onActionButtonOpenRasterFile(ActionEvent event) {
        
        if(currentVoxelFile != null){
            fileChooserOpenDTMFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());
        }
        
        
        File selectedFile = fileChooserOpenDTMFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldRasterFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonSetTransformationMatrix(ActionEvent event) {
        
        fileChooserOpenMatrixFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());
        
        File selectedFile = fileChooserOpenMatrixFile.showOpenDialog(stage);
        
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
                    DialogHelper.showErrorDialog(stage, new Exception("bad format"));
                }
                
            } catch (IOException ex) {
                LOGGER.error("Cannot read matrix file", ex);
            }
            
        }
    }

    @FXML
    private void onActionCheckboxUseTransformationMatrix(ActionEvent event) {
    }

    @FXML
    private void onActionButtonVizualize(ActionEvent event) {
        
        if(radioButtonMode2.isSelected()){
            
            final List<SceneObject> sceneObjects = getSelectedItemsFromTree();
            
            try {

                Service s = new Service() {

                    @Override
                    protected Task createTask() {
                        return new Task() {

                            @Override
                            protected Object call() throws Exception {

                                Viewer3D viewer3D = new Viewer3D((int) (SCREEN_WIDTH / 4.0d), (int) (SCREEN_HEIGHT / 4.0d), (int) (SCREEN_WIDTH / 1.5d), (int) (SCREEN_HEIGHT / 2.0d), "3d view");
                                viewer3D.setDynamicDraw(true);
                                //viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));
                                
                                final SimpleBooleanProperty spaceKeyDown = new SimpleBooleanProperty(false);
                                
                                viewer3D.getRenderFrame().addKeyListener(new KeyAdapter() {

                                    @Override
                                    public void keyPressed(KeyEvent e) {
                                        if(e.getKeyCode() == KeyEvent.VK_SPACE){
                                            spaceKeyDown.set(true);
                                        }
                                    }
                                    
                                    @Override
                                    public void keyReleased(KeyEvent e) {
                                        if(e.getKeyCode() == KeyEvent.VK_SPACE){
                                            spaceKeyDown.set(false);
                                        }
                                    }
                                });

                                final SceneObject boundingBox = new SimpleSceneObject();
                                boundingBox.setMesh(GLMeshFactory.createBoundingBox(-10, -10, -10, 10, 10, 10));
                                boundingBox.setDrawType(GLMesh.DrawType.LINES);
                                boundingBox.setShader(new ColorShader());
                                boundingBox.setVisible(false);
                                viewer3D.getScene().addSceneObject(boundingBox);
                                
                                final SimpleSceneObject sceneObjectFlag = SceneObjectFactory.createFlag();
                                sceneObjectFlag.setPosition(new Point3F());
                                sceneObjectFlag.setShader(new SimpleShader());
                                sceneObjectFlag.setVisible(false);
                                viewer3D.getScene().addSceneObject(sceneObjectFlag);
                                
                                SceneObjectListener selectionListener = new SceneObjectListener() {
                                        
                                    @Override
                                    public void clicked(SceneObject sceneObject, MousePicker mousePicker, Point3D intersection) {
                                        
                                        //if(!sceneObject.isSelected()){
                                            
                                            BoundingBox3D boundingBox3D = sceneObject.getBoundingBox();
                                            boundingBox.setMesh(GLMeshFactory.createBoundingBox((float)boundingBox3D.min.x, (float)boundingBox3D.min.y, (float)boundingBox3D.min.z,
                                                            (float)boundingBox3D.max.x, (float)boundingBox3D.max.y, (float)boundingBox3D.max.z));

                                            boundingBox.setVisible(true);
                                            sceneObject.setSelected(true);
                                            
                                            if(sceneObject instanceof RasterSceneObject){
                                                Integer index = (Integer) sceneObject.doPicking(mousePicker);
                    
                                                if(index != null){
                                                    Point3F position = ((RasterSceneObject)sceneObject).getVertex(index);

                                                    //float elevation = ((RasterSceneObject)sceneObject).getScalarFieldsList().get("Elevation").getValue(index);
                                                    //System.out.println("Elevation : "+elevation);
                                                    sceneObjectFlag.setVisible(true);
                                                    sceneObjectFlag.setPosition(position);
                                                    
                                                    if(spaceKeyDown.get()){
                                                        viewer3D.getScene().getCamera().setTarget(new Vec3F(position.x, position.y, position.z));
                                                    }
                                                    //gizmo.setPosition(position);
                                                    //viewer3D.getScene().getCamera().setTarget(new Vec3F(position.x, position.y, position.z));
                                                }
                                            }
                                            
                                            
                                        /*}else{
                                            sceneObject.setSelected(false);
                                        }*/
                                        
                                    }
                                };
                                        
                                for(SceneObject sceneObject : sceneObjects){
                                    
                                    sceneObject.addSceneObjectListener(selectionListener);
                                    sceneObject.resetIds();
                                    
                                    /*if(sceneObject instanceof PointCloudSceneObject){
                                        sceneObject.setShader(viewer3D.getScene().colorShader);
                                    }*/
                                    
                                    viewer3D.getScene().addSceneObject(sceneObject);
                                }
                                
                                
                                if(sceneObjects.size() > 0){
                                    
                                    SceneObject root = sceneObjects.get(0);
                                    
                                    /**
                                     * *light**
                                     */
                                    
                                    viewer3D.getScene().setLightPosition(new Point3F(root.getGravityCenter().x, root.getGravityCenter().y,
                                            root.getGravityCenter().z + (float)(root.getBoundingBox().max.z-root.getBoundingBox().min.z)));
                                    
                                     
                                    /**
                                     * *camera**
                                     */
                                    
                                    TrackballCamera trackballCamera = new TrackballCamera();
                                    trackballCamera.setPivot(root);
                                    trackballCamera.setLocation(new Vec3F(root.getGravityCenter().x + 50, root.getGravityCenter().y, root.getGravityCenter().z + 50));
                                    viewer3D.getScene().setCamera(trackballCamera);
                                }
                               
                                
                                
                                viewer3D.show();

                                return null;
                            }
                        };
                    }
                };
                
                s.setOnFailed(new EventHandler() {
                    @Override
                    public void handle(Event event) {
                        LOGGER.error("Initialization failed");
                    }
                });

                s.start();

            } catch (Exception ex) {
                LOGGER.error("Cannot launch 3d view", ex);
            }
        }else{
            
            final File voxelFile = currentVoxelFile;
            final String attributeToView = comboboxAttributeToView.getSelectionModel().getSelectedItem();

            final boolean drawDTM = checkboxRaster.isSelected();
            final File dtmFile = new File(textfieldRasterFilePath.getText());
            final Mat4D dtmVOPMatrix = MatrixUtility.convertMatrix4dToMat4D(rasterTransfMatrix);
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

                                final Viewer3D viewer3D = new Viewer3D((int) (SCREEN_WIDTH / 4.0d), (int) (SCREEN_HEIGHT / 4.0d), (int) (SCREEN_WIDTH / 1.5d), (int) (SCREEN_HEIGHT / 2.0d), voxelFile.toString());
                                //viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));
                                viewer3D.setDynamicDraw(false);
                                fr.amap.lidar.amapvox.voxviewer.object.scene.Scene scene = viewer3D.getScene();

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
                                
                                SceneObject sceneObjectSelectedVox = new SimpleSceneObject(GLMeshFactory.createBoundingBox(
                                        -voxelResolution/2.0f,
                                        -voxelResolution/2.0f,
                                        -voxelResolution/2.0f,
                                        voxelResolution/2.0f,
                                        voxelResolution/2.0f,
                                        voxelResolution/2.0f), false);
                                
                                SimpleShader simpleShader = new SimpleShader();
                                simpleShader.setColor(new Vec3F(1, 0, 0));
                                sceneObjectSelectedVox.setVisible(false);
                                sceneObjectSelectedVox.setShader(simpleShader);
                                sceneObjectSelectedVox.setDrawType(GLMesh.DrawType.LINES);
                                
                                viewer3D.getScene().addSceneObject(sceneObjectSelectedVox);
                                
                                final SimpleBooleanProperty spaceKeyDown = new SimpleBooleanProperty(false);
                                
                                viewer3D.getRenderFrame().addKeyListener(new KeyAdapter() {

                                    @Override
                                    public void keyPressed(KeyEvent e) {
                                        if(e.getKeyCode() == KeyEvent.VK_SPACE){
                                            spaceKeyDown.set(true);
                                        }
                                    }
                                    
                                    @Override
                                    public void keyReleased(KeyEvent e) {
                                        if(e.getKeyCode() == KeyEvent.VK_SPACE){
                                            spaceKeyDown.set(false);
                                        }
                                    }
                                });
                                
                                

                                voxelSpace.changeCurrentAttribut(attributeToView);
                                voxelSpace.setShader(new InstanceLightedShader());
                                voxelSpace.setDrawType(GLMesh.DrawType.TRIANGLES);
                                scene.addSceneObject(voxelSpace);
                                

                                VoxelFileReader reader = new VoxelFileReader(voxelFile);
                                VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();
                                
                                //coordinates offset for float precision view
                                
                                Point3d oldMinCorner = new Point3d(infos.getMinCorner());
                                
                                infos.getMaxCorner().sub(oldMinCorner);
                                infos.setMinCorner(new Point3d());

                                /**
                                 * *DTM**
                                 */
                                SceneObject dtmSceneObject = null;
                                
                                if (drawDTM && dtmFile != null) {

                                    updateMessage("Loading DTM");
                                    updateProgress(0, 100);

                                    Raster dtm = AsciiGridHelper.readFromAscFile(dtmFile);

                                    Matrix4d dtmTransfMatrix = new Matrix4d();
                                    dtmTransfMatrix.setIdentity();
                                    dtmTransfMatrix.setTranslation(new Vector3d(-oldMinCorner.x, -oldMinCorner.y, -oldMinCorner.z));
                                    
                                    if (transform && dtmVOPMatrix != null) {
                                        dtmTransfMatrix.mul(MatrixUtility.convertMat4DToMatrix4d(dtmVOPMatrix));
                                    }
                                    
                                    dtm.setTransformationMatrix(MatrixUtility.convertMatrix4dToMat4D(dtmTransfMatrix));

                                    if (fitDTMToVoxelSpace) {

                                        dtm.setLimits(new BoundingBox2F(new Point2F((float) infos.getMinCorner().x, (float) infos.getMinCorner().y),
                                                new Point2F((float) infos.getMaxCorner().x, (float) infos.getMaxCorner().y)), mntFittingMargin);
                                    }

                                    updateMessage("Converting raster to mesh");
                                    dtm.buildMesh();

                                    GLMesh dtmMesh = GLMeshFactory.createMeshAndComputeNormalesFromDTM(dtm);
                                    dtmSceneObject = new SimpleSceneObject(dtmMesh, false);
                                    dtmSceneObject.setShader(new PhongShader());
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

                                scalePlane.setShader(new TextureShader());
                                scalePlane.setDrawType(GLMesh.DrawType.TRIANGLES);
                                scalePlane.setName("color scale");
                                scene.addSceneObjectAsHud(scalePlane);

                                GLMesh boundingBoxMesh = GLMeshFactory.createBoundingBox((float) infos.getMinCorner().x,
                                        (float) infos.getMinCorner().y,
                                        (float) infos.getMinCorner().z,
                                        (float) infos.getMaxCorner().x,
                                        (float) infos.getMaxCorner().y,
                                        (float) infos.getMaxCorner().z);

                                SceneObject boundingBox = new SimpleSceneObject(boundingBoxMesh, false);

                                SimpleShader s = scene.simpleShader;
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
                                 * Axis
                                 */
                                
                                /*InputStream axis3ObjStream = Viewer3D.class.getResourceAsStream("/mesh/axis3.obj");
                                InputStream axis3MtlStream = Viewer3D.class.getResourceAsStream("/mesh/axis3.mtl");


                                GLMesh axisMesh = GLMeshFactory.createMeshFromObj(axis3ObjStream, axis3MtlStream);
                                //axisMesh.translate(new Vec3F(264094, 331552, 235));
                                axisMesh.scale(new Vec3F(5, 5, 5));
                                SceneObject axisSceneObject = new SimpleSceneObject(axisMesh);
                                axisSceneObject.setShader(new AxisShader());
                                viewer3D.getScene().addSceneObject(axisSceneObject);*/

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
                                
                                final SceneObject dtmSceneObjectFinal = dtmSceneObject;

                                Platform.runLater(new Runnable() {

                                    @Override
                                    public void run() {

                                        final Stage viewer3DStage = new Stage();
                                        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Viewer3DFrame.fxml"));

                                        try {
                                            stage.setAlwaysOnTop(false);

                                            Parent root = loader.load();
                                            Scene scene1 = new Scene(root);
                                            viewer3DStage.setScene(scene1);

                                            Viewer3DFrameController viewer3DFrameController = loader.getController();
                                            viewer3DFrameController.setStage(viewer3DStage);
                                            viewer3DStage.setX(viewer3D.getPosition().getX());
                                            viewer3DStage.setY(viewer3D.getPosition().getY());
                                            viewer3DFrameController.setViewer3D(viewer3D);
                                            viewer3DFrameController.setAttributes(attributeToView, voxelSpace.data.getVoxelSpaceInfos().getColumnNames());
                                            
                                            
                                            viewer3DFrameController.initContent(voxelSpace);
                                            
                                            viewer3DFrameController.addSceneObject(voxelSpace, voxelFile.getName());
                                            
                                            if(dtmSceneObjectFinal != null){
                                                viewer3DFrameController.addSceneObject(dtmSceneObjectFinal, dtmFile.getName());
                                            }
                                            
                                            SceneObjectListener listener = new SceneObjectListener() {
                                                @Override
                                                public void clicked(SceneObject sceneObject, MousePicker mousePicker, Point3D intersection) {

                                                    Vec3F camLocation = viewer3D.getScene().getCamera().getLocation();

                                                    VoxelObject selectedVoxel = voxelSpace.doPicking(mousePicker);

                                                    if(selectedVoxel != null){

                                                        LinkedHashMap<String, Double> attributes = new LinkedHashMap<>();

                                                        for(int i=0;i<voxelSpace.getColumnsNames().length;i++){

                                                            float attribut = selectedVoxel.getAttributs()[i];

                                                            attributes.put(voxelSpace.getColumnsNames()[i] , new Double(attribut));
                                                        }
                                                        
                                                        Platform.runLater(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                viewer3DFrameController.setAttributes(attributes);
                                                            }
                                                        });

                                                        Point3f voxelPosition = voxelSpace.getVoxelPosition(selectedVoxel.$i, selectedVoxel.$j, selectedVoxel.$k);

                                                        sceneObjectSelectedVox.setPosition(new Point3F(voxelPosition.x, voxelPosition.y, voxelPosition.z));
                                                        sceneObjectSelectedVox.setVisible(true);

                                                        if(spaceKeyDown.get()){
                                                            viewer3D.getScene().getCamera().setTarget(new Vec3F(voxelPosition.x, voxelPosition.y, voxelPosition.z));
                                                        }

                                                    }else{
                                                        sceneObjectSelectedVox.setVisible(false);
                                                    }
                                                }
                                            };

                                            voxelSpace.addSceneObjectListener(listener);

                                            //viewer3D.getJoglContext().setStartX((int) viewer3DStage.getWidth());
                                            
                                            FXNewtOverlap fxNewtOverlap = new FXNewtOverlap();
                                            fxNewtOverlap.link(viewer3DStage, scene1, viewer3D, viewer3DFrameController.getAnchorPaneGL());

                                            viewer3DStage.setOnHidden(new EventHandler<WindowEvent>() {
                                                @Override
                                                public void handle(WindowEvent event) {
                                                    viewer3D.close();
                                                }
                                            });
                                            
                                            viewer3D.show();
                                            viewer3DStage.show();

                                        } catch (IOException e) {
                                            LOGGER.error("Loading ToolBarFrame.fxml failed", e);
                                        } catch (Exception e) {
                                            LOGGER.error("Error during toolbar init", e);
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
                LOGGER.error("Cannot launch 3d view", ex);
            }
        }
    }

    @FXML
    private void onActionMenuItemSelectAllSceneObjects(ActionEvent event) {
        treeViewSceneObjects.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemUnselectAllSceneObjects(ActionEvent event) {
        treeViewSceneObjects.getSelectionModel().clearSelection();
    }

    @FXML
    private void onActionButtonRemoveSceneObject(ActionEvent event) {
        
        ObservableList<TreeItem<SceneObjectWrapper>> selectedItems = treeViewSceneObjects.getSelectionModel().getSelectedItems();
        root.getChildren().removeAll(selectedItems);
    }
    
    private List<SceneObjectWrapper> getVoxelSpaceSceneObjectElements(){
        
        List<SceneObjectWrapper> elements = new ArrayList<>();
        
        ObservableList<TreeItem<SceneObjectWrapper>> children = root.getChildren();
        for(TreeItem<SceneObjectWrapper> child : children){
            if(child.getValue() != null){
                
                if(child.getValue().getSceneObject() != null){
                    
                     if(child.getValue().getSceneObject() instanceof VoxelSpaceSceneObject){
                         elements.add(child.getValue());
                     }
                }
                
            }
        }
        
        return elements;
    }
    
    private void addSceneObjectToTree(File file, Mat4D transfMatrix){
        
        
        String extension = FileManager.getExtension(file);
        
        SceneObjectImportTask taskImporter = null;

        switch (extension) {

            case ".las":
                taskImporter = new LasImportTask(file);
                break;
                
            case ".laz":
                taskImporter = new LazImportTask(file);
                break;
                
            case ".rsp":
                
                try {
                    Rsp rsp = new Rsp();
                    rsp.read(file);
                    
                    riscanProjectExtractor.init(rsp);

                    riscanProjectExtractor.getFrame().setOnHidden(new EventHandler<WindowEvent>() {

                        @Override
                        public void handle(WindowEvent event) {

                            List<LidarScan> selectedScans = riscanProjectExtractor.getController().getSelectedScans();

                            for(LidarScan scan : selectedScans){
                                addSceneObjectToTree(scan.file, MatrixUtility.convertMatrix4dToMat4D(scan.matrix));
                            }
                            
                        }
                    });

                    riscanProjectExtractor.getFrame().showAndWait();
                    
                } catch (JDOMException | IOException ex) {
                    LOGGER.error(ex);
                }
                
                return;
                
            case ".rxp":
                taskImporter = new RxpImportTask(file, transfMatrix);
                break;
                
            case ".ptg":
            case ".PTG":
                
                PTGReader ptgReader = new PTGReader();
                
                try {
                    ptgReader.openPTGFile(file);
                    
                } catch (IOException ex) {
                    LOGGER.error(ex);
                    return;
                }
                
                if(ptgReader.isBinaryFile()){
                    taskImporter = new PtgImportTask(file);
                }else{
                    
                    try {
                        ptgProjectExtractor.init(file);

                        ptgProjectExtractor.getFrame().showAndWait();

                        List<LidarScan> selectedScans = ptgProjectExtractor.getController().getSelectedScans();

                        for(LidarScan lidarScan : selectedScans){
                            addSceneObjectToTree(lidarScan.file, MatrixUtility.convertMatrix4dToMat4D(lidarScan.matrix));
                        }
                        
                        return;
                        
                    } catch (Exception ex) {
                        LOGGER.error(ex);
                        return;
                    }
                }
                
                break;
                
            case ".ptx":
            case ".PTX":
                try {
                    
                    ptxProjectExtractor.init(file);
                    
                    ptxProjectExtractor.getFrame().showAndWait();
                    
                    List<LidarScan> selectedScans = ptxProjectExtractor.getController().getSelectedScans();
                    List<PTXLidarScan> ptxScans = new ArrayList<>();
                    
                    for(LidarScan lidarScan : selectedScans){
                        ptxScans.add((PTXLidarScan)lidarScan);
                    }
                    
                    taskImporter = new PtxImportTask(file, ptxScans);
                    
                } catch (IOException ex) {
                    LOGGER.error(ex);
                    return;
                }
                break;
                
            case ".asc":
                taskImporter = new AscImportTask(file, getVoxelSpaceSceneObjectElements());
                break;
                
            case ".vox":
                
                break;
                
            default:
                //chargement d'un fichier csv
        }
        
        if(taskImporter == null){
            //handle other cases
            if (VoxelFileReader.isFileAVoxelFile(file)) {
                taskImporter = new VoxImportTask(file);

            }else{
                taskImporter = new CsvImportTask(file);
            }
        }
            
        final Task task = taskImporter;

        try {
            final SceneObjectWrapper sceneObjectWrapper = addSceneObject(file);

            taskImporter.showImportFrame(stage);

            Service s = new Service() {
                @Override
                protected Task createTask() {
                    return task;
                }
            };

            sceneObjectWrapper.getProgressInfo().textProperty().bind(s.messageProperty());
            sceneObjectWrapper.getProgressBar().progressProperty().bind(s.progressProperty());

            s.setOnSucceeded(new EventHandler() {
                @Override
                public void handle(Event event) {

                    if(s.getValue() != null){
                        sceneObjectWrapper.setSceneObject((SceneObject) s.getValue());
                        
                        if(sceneObjectWrapper.getProgressInfo().textProperty().isBound()){
                            sceneObjectWrapper.getProgressInfo().textProperty().unbind();
                        }
                        
                        sceneObjectWrapper.getProgressInfo().setText("Ready !");
                    }
                }
            });
            
            s.setOnFailed(new EventHandler() {
                @Override
                public void handle(Event event) {
                    if(sceneObjectWrapper.getProgressInfo().textProperty().isBound()){
                        sceneObjectWrapper.getProgressInfo().textProperty().unbind();
                    }
                    sceneObjectWrapper.getProgressInfo().setText("Ready !");
                    
                    if(sceneObjectWrapper.getProgressBar().progressProperty().isBound()){
                        sceneObjectWrapper.getProgressBar().progressProperty().unbind();
                    }
                    
                    sceneObjectWrapper.getProgressBar().setProgress(0);
                }
            });

            s.start();

        } catch (Exception ex) {
            LOGGER.error(ex);
        }
    }

    @FXML
    private void onActionButtonAddSceneObject(ActionEvent event) {
        
        List<File> selectedFiles = sceneObjectChooser.showOpenMultipleDialog(stage);
        
        if(selectedFiles != null){
            
            for(File selectedFile : selectedFiles){
                addSceneObjectToTree(selectedFile, Mat4D.identity());
            }
        }
        
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
    
}