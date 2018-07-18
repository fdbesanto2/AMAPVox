package fr.amap.lidar.amapvox.gui.viewer3d;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import com.jogamp.common.nio.Buffers;
import com.jogamp.newt.event.KeyAdapter;
import com.jogamp.newt.event.KeyEvent;
import fr.amap.commons.math.geometry.BoundingBox2F;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.util.MatrixFileParser;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.commons.util.image.ScaleGradient;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.gui.DialogHelper;
import fr.amap.commons.javafx.io.FileChooserContext;
import fr.amap.commons.raster.asc.Face;
import fr.amap.commons.raster.asc.Point;
import fr.amap.commons.util.ColorGradient;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import fr.amap.viewer3d.FXNewtOverlap;
import fr.amap.viewer3d.SimpleViewer;
import fr.amap.viewer3d.loading.shader.InstanceLightedShader;
import fr.amap.viewer3d.loading.shader.PhongShader;
import fr.amap.viewer3d.loading.shader.SimpleShader;
import fr.amap.viewer3d.loading.shader.TextureShader;
import fr.amap.viewer3d.loading.texture.Texture;
import fr.amap.viewer3d.mesh.GLMesh;
import fr.amap.viewer3d.mesh.GLMeshFactory;
import fr.amap.viewer3d.mesh.SimpleGLMesh;
import fr.amap.viewer3d.object.camera.TrackballCamera;
import fr.amap.viewer3d.object.scene.MousePicker;
import fr.amap.viewer3d.object.scene.SceneObject;
import fr.amap.viewer3d.object.scene.SceneObjectFactory;
import fr.amap.viewer3d.object.scene.SceneObjectListener;
import fr.amap.viewer3d.object.scene.SimpleSceneObject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class Viewer3DPanelController implements Initializable {

    private Stage stage;
    private final static Logger LOGGER = Logger.getLogger(Viewer3DPanelController.class);

    private static double SCREEN_WIDTH;
    private static double SCREEN_HEIGHT;

    private FileChooserContext fileChooserOpenVoxelFile;
    private File currentVoxelFile;
    private FileChooserContext fileChooserOpenDTMFile;
    private FileChooserContext fileChooserOpenMatrixFile;

    private Matrix4d rasterTransfMatrix;

    @FXML
    private TextField textFieldVoxelFile;

    @FXML
    private ComboBox<String> comboboxAttributeToView;
    @FXML
    private VBox vboxRasterProperties;
    @FXML
    private HBox hboxRasterFile;
    @FXML
    private HBox hboxAttributeToView;
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

    /**
     * Initialises the controller class.
     *
     * @param url
     * @param rb
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        fileChooserOpenVoxelFile = new FileChooserContext();
        fileChooserOpenVoxelFile.fc.setTitle("Choose Voxel file");
        fileChooserOpenVoxelFile.fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"),
                new FileChooser.ExtensionFilter("Voxel Files", "*.vox"));

        fileChooserOpenDTMFile = new FileChooserContext();
        fileChooserOpenDTMFile.fc.setTitle("Choose DTM file");
        fileChooserOpenDTMFile.fc.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"),
                new FileChooser.ExtensionFilter("DTM Files", "*.asc"));

        fileChooserOpenMatrixFile = new FileChooserContext();

        rasterTransfMatrix = new Matrix4d();
        rasterTransfMatrix.setIdentity();

        checkboxUseTransformationMatrix.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            buttonSetTransformationMatrix.setDisable(!newValue);
        });
    }

    private void updateCurrentVoxelFile(File voxelFile) {

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

    public void setStage(Stage stage) {

        this.stage = stage;

        stage.setOnShown((WindowEvent event) -> {
            ObservableList<Screen> screens = Screen.getScreensForRectangle(stage.getX(), stage.getY(), stage.getWidth(), stage.getHeight());

            if (screens != null && screens.size() > 0) {
                SCREEN_WIDTH = screens.get(0).getBounds().getWidth();
                SCREEN_HEIGHT = screens.get(0).getBounds().getHeight();
            }
        });
    }

    @FXML
    private void onActionButtonOpenVoxelFile(ActionEvent event) {

        if (currentVoxelFile != null) {
            fileChooserOpenVoxelFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());
        }

        File selectedFile = fileChooserOpenVoxelFile.showOpenDialog(stage);

        if (selectedFile != null) {
            textFieldVoxelFile.setText(selectedFile.getAbsolutePath());
            updateCurrentVoxelFile(selectedFile);
            hboxRasterFile.setDisable(false);
            hboxAttributeToView.setDisable(false);
            buttonOpen3DView.setDisable(false);
        } else {
            hboxRasterFile.setDisable(true);
            vboxRasterProperties.setDisable(true);
            hboxAttributeToView.setDisable(true);
            buttonOpen3DView.setDisable(true);
        }
    }

    @FXML
    private void onActionButtonOpenRasterFile(ActionEvent event) {

        if (currentVoxelFile != null) {
            fileChooserOpenDTMFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());
        }

        File selectedFile = fileChooserOpenDTMFile.showOpenDialog(stage);

        if (selectedFile != null) {
            textfieldRasterFilePath.setText(selectedFile.getAbsolutePath());
            vboxRasterProperties.setDisable(false);
        } else {
            vboxRasterProperties.setDisable(true);
        }
    }

    @FXML
    private void onActionButtonSetTransformationMatrix(ActionEvent event) {

        fileChooserOpenMatrixFile.fc.setInitialDirectory(currentVoxelFile.getParentFile());

        File selectedFile = fileChooserOpenMatrixFile.showOpenDialog(stage);

        if (selectedFile != null) {

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
    private void onActionButtonVizualize(ActionEvent event) {

        final File voxelFile = currentVoxelFile;
        final String attributeToView = comboboxAttributeToView.getSelectionModel().getSelectedItem();

        final File dtmFile = new File(textfieldRasterFilePath.getText());
        final boolean drawDTM = dtmFile.exists();
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

                            final SimpleViewer viewer3D = new SimpleViewer((int) (SCREEN_WIDTH / 4.0d), (int) (SCREEN_HEIGHT / 4.0d), (int) (SCREEN_WIDTH / 1.5d), (int) (SCREEN_HEIGHT / 2.0d), voxelFile.toString());
                            //viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));
                            viewer3D.setDynamicDraw(false);
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

                            final SimpleBooleanProperty spaceKeyDown = new SimpleBooleanProperty(false);

                            viewer3D.getRenderFrame().addKeyListener(new KeyAdapter() {

                                @Override
                                public void keyPressed(KeyEvent e) {
                                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                                        spaceKeyDown.set(true);
                                    }
                                }

                                @Override
                                public void keyReleased(KeyEvent e) {
                                    if (e.getKeyCode() == KeyEvent.VK_SPACE) {
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

                                GLMesh dtmMesh = createMeshAndComputeNormalesFromDTM(dtm);
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

                            voxelSpace.addPropertyChangeListener("gradientUpdated", (PropertyChangeEvent evt) -> {
                                BufferedImage image = ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                        voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                        viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                        ScaleGradient.Orientation.HORIZONTAL, 5, 8);

                                scaleTexture.setBufferedImage(image);
                            });

                            /**
                             * Axis
                             */
                            /*InputStream axis3ObjStream = SimpleViewer.class.getResourceAsStream("/mesh/axis3.obj");
                                InputStream axis3MtlStream = SimpleViewer.class.getResourceAsStream("/mesh/axis3.mtl");


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

                            Platform.runLater(() -> {
                                final Stage viewer3DStage = new Stage();
                                final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/viewer3d/Viewer3DFrame.fxml"));
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
                                    if (dtmSceneObjectFinal != null) {
                                        viewer3DFrameController.addSceneObject(dtmSceneObjectFinal, dtmFile.getName());
                                    }
                                    SceneObjectListener listener = (SceneObject sceneObject, MousePicker mousePicker, Point3D intersection) -> {
                                        Vec3F camLocation = viewer3D.getScene().getCamera().getLocation();
                                        VoxelObject selectedVoxel = voxelSpace.doPicking(mousePicker);
                                        if (selectedVoxel != null) {
                                            LinkedHashMap<String, Double> attributes = new LinkedHashMap<>();
                                            for (int i = 0; i < voxelSpace.getColumnsNames().length; i++) {

                                                float attribut = selectedVoxel.getAttributs()[i];

                                                attributes.put(voxelSpace.getColumnsNames()[i], new Double(attribut));
                                            }
                                            Platform.runLater(() -> {
                                                viewer3DFrameController.setAttributes(attributes);
                                            });
                                            Point3f voxelPosition = voxelSpace.getVoxelPosition(selectedVoxel.$i, selectedVoxel.$j, selectedVoxel.$k);
                                            sceneObjectSelectedVox.setPosition(new Point3F(voxelPosition.x, voxelPosition.y, voxelPosition.z));
                                            sceneObjectSelectedVox.setVisible(true);
                                            if (spaceKeyDown.get()) {
                                                viewer3D.getScene().getCamera().setTarget(new Vec3F(voxelPosition.x, voxelPosition.y, voxelPosition.z));
                                            }
                                        } else {
                                            sceneObjectSelectedVox.setVisible(false);
                                        }
                                    };
                                    voxelSpace.addSceneObjectListener(listener);
                                    //viewer3D.getJoglContext().setStartX((int) viewer3DStage.getWidth());
                                    FXNewtOverlap fxNewtOverlap = new FXNewtOverlap();
                                    fxNewtOverlap.link(viewer3DStage, scene1, viewer3D, viewer3DFrameController.getAnchorPaneGL());
                                    viewer3DStage.setOnCloseRequest((WindowEvent event1) -> {
                                        viewer3D.close();
                                        event1.consume();
                                    });
                                    viewer3D.show();
                                    viewer3DStage.show();
                                } catch (IOException e) {
                                    LOGGER.error("Loading ToolBarFrame.fxml failed", e);
                                } catch (Exception e) {
                                    LOGGER.error("Error during toolbar init", e);
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

    public static GLMesh createMeshAndComputeNormalesFromDTM(Raster dtm) {

        List<Point> points = dtm.getPoints();
        List<Face> faces = dtm.getFaces();

        GLMesh mesh = new SimpleGLMesh();

        float[] vertexData = new float[points.size() * 3];
        for (int i = 0, j = 0; i < points.size(); i++, j += 3) {

            vertexData[j] = points.get(i).x;
            vertexData[j + 1] = points.get(i).y;
            vertexData[j + 2] = points.get(i).z;
        }

        float[] normalData = new float[points.size() * 3];
        for (int i = 0, j = 0; i < points.size(); i++, j += 3) {

            Vec3F meanNormale = new Vec3F(0, 0, 0);

            for (Integer faceIndex : points.get(i).faces) {

                Face face = faces.get(faceIndex);

                Point point1 = points.get(face.getPoint1());
                Point point2 = points.get(face.getPoint2());
                Point point3 = points.get(face.getPoint3());

                Vec3F vec1 = Vec3F.substract(new Vec3F(point2.x, point2.y, point2.z), new Vec3F(point1.x, point1.y, point1.z));
                Vec3F vec2 = Vec3F.substract(new Vec3F(point3.x, point3.y, point3.z), new Vec3F(point1.x, point1.y, point1.z));

                meanNormale = Vec3F.add(meanNormale, Vec3F.normalize(Vec3F.cross(vec2, vec1)));

            }

            meanNormale = Vec3F.normalize(meanNormale);

            normalData[j] = meanNormale.x;
            normalData[j + 1] = meanNormale.y;
            normalData[j + 2] = meanNormale.z;
        }

        int indexData[] = new int[faces.size() * 3];
        for (int i = 0, j = 0; i < faces.size(); i++, j += 3) {

            indexData[j] = faces.get(i).getPoint1();
            indexData[j + 1] = faces.get(i).getPoint2();
            indexData[j + 2] = faces.get(i).getPoint3();
        }

        mesh.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        mesh.normalBuffer = Buffers.newDirectFloatBuffer(normalData);
        mesh.vertexCount = indexData.length;

        ColorGradient gradient = new ColorGradient(dtm.getzMin(), dtm.getzMax());
        gradient.setGradientColor(ColorGradient.GRADIENT_RAINBOW3);

        float colorData[] = new float[points.size() * 3];
        for (int i = 0, j = 0; i < points.size(); i++, j += 3) {

            Color color = gradient.getColor(points.get(i).z);
            colorData[j] = color.getRed() / 255.0f;
            colorData[j + 1] = color.getGreen() / 255.0f;
            colorData[j + 2] = color.getBlue() / 255.0f;

        }

        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);

        return mesh;
    }
}
