/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer;

import com.jogamp.newt.awt.NewtCanvasAWT;
import com.jogamp.newt.event.WindowAdapter;
import com.sun.javafx.application.ParametersImpl;
import com.sun.javafx.tk.TKStage;
import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.util.image.ScaleGradient;
import fr.amap.commons.raster.asc.DtmLoader;
import fr.amap.commons.raster.asc.RegularDtm;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import fr.amap.lidar.amapvox.voxviewer.event.BasicEvent;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.SimpleShader;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.lidar.amapvox.voxviewer.mesh.PointCloudGLMesh;
import fr.amap.lidar.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObjectFactory;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SimpleSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SimpleSceneObject2;
import fr.amap.lidar.amapvox.voxviewer.object.scene.VoxelSpaceSceneObject;
import fr.amap.lidar.amapvox.voxviewer.renderer.MinimalWindowAdapter;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.log4j.Logger;
import org.controlsfx.dialog.ProgressDialog;

/**
 *
 * @author calcul
 */
public class FXViewer3D extends Application {

    private static double SCREEN_WIDTH;
    private static double SCREEN_HEIGHT;
    private List<String> parameters;

    private File voxelFile;
    private String attributeToView;

    private final static Logger logger = Logger.getLogger(FXViewer3D.class);
    
    @Override
    public void start(final Stage stage) {
        
        List<String> args;
        Parameters p;

        if (parameters != null) {
            args = parameters;
            p = new ParametersImpl(parameters);
        } else {
            p = getParameters();
            args = p.getRaw();
        }

        //List<String> args = getParameters().getRaw();
        if (args.size() <= 1) {
            if (args.isEmpty()) {
                //usage();
                exit(0);
            } else if (args.get(0).equals("--help")) {
                //usage();
                exit(0);
                return;
            }
        }

        String cmd = "";
        for (String arg : args) {
            cmd += arg + " ";
        }

        logger.info("Launching command: " + cmd);

        Map<String, String> parametersWithValue = p.getNamed();
        List<String> parametersWithNoValue = p.getUnnamed();

        voxelFile = new File(parametersWithValue.get("input"));
        attributeToView = parametersWithValue.get("attribut");

        final int windowWidth;
        final int windowHeight;
        final boolean drawDTM;
        final File dtmFile;
        final boolean fitDTMToVoxelSpace;
        final boolean transform;
        final int dtmFittingMargin;
        final Mat4D dtmTransfMatrix;

        //window size
        ObservableList<Screen> screens = Screen.getScreens();

        if (screens != null && screens.size() > 0) {
            SCREEN_WIDTH = screens.get(0).getBounds().getWidth();
            SCREEN_HEIGHT = screens.get(0).getBounds().getHeight();
        }

        int tmpWindowWidth;
        try {
            tmpWindowWidth = Integer.valueOf(parametersWithValue.get("width"));
        } catch (Exception e) {
            tmpWindowWidth = (int) (SCREEN_WIDTH / 1.5d);
        }

        int tmpWindowHeight;

        try {
            tmpWindowHeight = Integer.valueOf(parametersWithValue.get("height"));
        } catch (Exception e) {
            tmpWindowHeight = (int) (SCREEN_HEIGHT / 2.0d);
        }

        windowWidth = tmpWindowWidth;
        windowHeight = tmpWindowHeight;

        String dtmPath = parametersWithValue.get("dtm");

        if (dtmPath == null) {
            drawDTM = false;
            dtmFile = null;
            transform = false;
            fitDTMToVoxelSpace = false;
            dtmFittingMargin = 0;
            dtmTransfMatrix = Mat4D.identity();
        } else {
            drawDTM = true;
            dtmFile = new File(dtmPath);

            fitDTMToVoxelSpace = parametersWithNoValue.contains("--dtm-fit");
            transform = parametersWithNoValue.contains("--dtm-transform");

            if (fitDTMToVoxelSpace) {
                if (parametersWithValue.containsKey("dtm-fitting-margin")) {
                    dtmFittingMargin = Integer.valueOf(parametersWithValue.get("dtm-fitting-margin"));
                } else {
                    dtmFittingMargin = 0;
                }
            } else {
                dtmFittingMargin = 0;
            }

            String matrix = parametersWithValue.get("dtm-transf-matrix");

            if (matrix == null) {
                dtmTransfMatrix = Mat4D.identity();
            } else {
                String[] matrixStringValues = matrix.replaceAll(" ", ",").split(",");
                if (matrixStringValues.length != 16) {
                    dtmTransfMatrix = Mat4D.identity();
                } else {
                    dtmTransfMatrix = new Mat4D();

                    double[] matrixValues = new double[16];
                    for (int i = 0; i < matrixStringValues.length; i++) {
                        matrixValues[i] = Double.valueOf(matrixStringValues[i]);
                    }

                    dtmTransfMatrix.mat = matrixValues;
                }
            }
        }

        try {

            Service s = new Service() {

                @Override
                protected Task createTask() {
                    return new Task() {

                        @Override
                        protected Object call() throws Exception {

                            final Viewer3D viewer3D = new Viewer3D(((int) windowWidth / 4), (int) windowHeight / 4, windowWidth, windowHeight, voxelFile.toString());
                            viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));

                            fr.amap.lidar.amapvox.voxviewer.object.scene.Scene scene = viewer3D.getScene();
                            
                            LasReader lasReader = new LasReader();
                            lasReader.open(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt.las"));
                            LasHeader header = lasReader.getHeader();
                            int numberOfPointrecords = (int) header.getNumberOfPointrecords();
                            
                            float[] vertexData = new float[numberOfPointrecords*3];
                            float[] colorData = new float[numberOfPointrecords*3];
                            
                            Iterator<PointDataRecordFormat> iterator = lasReader.iterator();
                            Mat4D transfMatrix = Mat4D.identity();
                            /*transfMatrix.mat = new double[]{0.9540688863574789,0.29958731629459895,0.0,-448120.0441687209,
                            -0.29958731629459895,0.9540688863574789,0.0,-470918.3928060016,
                            0.0,0.0,1.0,0.0,
                            0.0,0.0,0.0,1.0};*/
                            
                            int i=0;
                            while(iterator.hasNext()){
                                
                                PointDataRecordFormat point = iterator.next();
                                float x = (float) ((header.getxOffset()) + point.getX() * header.getxScaleFactor());
                                float y = (float) ((header.getyOffset()) + point.getY() * header.getyScaleFactor());
                                float z = (float) ((header.getzOffset()) + point.getZ() * header.getzScaleFactor());
                                
                                Vec4D pointTransformed = Mat4D.multiply(transfMatrix, new Vec4D(x, y, z, 1));
                                vertexData[i] = (float) pointTransformed.x;
                                vertexData[i+1] = (float) pointTransformed.y;
                                vertexData[i+2] = (float) pointTransformed.z;
                                
                                if(point.getClassification() == 2){
                                    colorData[i] = 1;
                                    colorData[i+2] = 0;
                                }else{
                                    colorData[i] = 0;
                                    colorData[i+2] = 1;
                                }
                                
                                colorData[i+1] = 0;
                                
                                
                                i+=3;
                            }
                            
                            PointCloudGLMesh pointcloudMesh = (PointCloudGLMesh) GLMeshFactory.createPointCloud(vertexData,colorData);
                            
                            PointCloudSceneObject pointCloud = new PointCloudSceneObject(pointcloudMesh, false);
                            pointCloud.setShader(scene.colorShader);
                            scene.addSceneObject(pointCloud);
                            
                            
                            /**
                             * *VOXEL SPACE**
                             */
                            File voxelFile = new File("/home/calcul/Documents/Julien/test_lad/als_spherical_new.vox");
                            updateMessage("Loading voxel space: " + voxelFile.getAbsolutePath());
                            final VoxelSpaceSceneObject voxelSpace = SceneObjectFactory.createVoxelSpace(voxelFile);
                            voxelSpace.changeCurrentAttribut(attributeToView);
                            voxelSpace.setShader(scene.instanceLightedShader);
                            scene.addSceneObject(voxelSpace);
                            
                            VoxelFileReader reader = new VoxelFileReader(voxelFile);
                            VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();
                            
                            /***DTM***/
                            RegularDtm dtm = DtmLoader.readFromAscFile(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt_dtm.asc"));

                            //dtm.setTransformationMatrix(transfMatrix);
                            dtm.buildMesh();
                            GLMesh dtmMesh = GLMeshFactory.createMeshAndComputeNormalesFromDTM(dtm);
                            
                            Point3F position = new Point3F((float)((dtm.getLowerCorner().getX()+dtm.getUpperCorner().getX())/2.0d),
                                                            (float)((dtm.getLowerCorner().getY()+dtm.getUpperCorner().getY())/2.0d), 
                                                            (float)((dtm.getLowerCorner().getZ()+dtm.getUpperCorner().getZ())/2.0d));
                            
                            SceneObject dtmSceneObject = new SimpleSceneObject(dtmMesh, false, position);
                            dtmSceneObject.setShader(scene.lightedShader);
                            scene.addSceneObject(dtmSceneObject);
                            
                            /**
                             * *DTM**
                             */
                            /*if (drawDTM && dtmFile != null) {

                                updateMessage("Loading DTM");
                                RegularDtm dtm = DtmLoader.readFromAscFile(dtmFile);

                                if (transform && dtmTransfMatrix != null) {
                                    dtm.setTransformationMatrix(dtmTransfMatrix);
                                }

                                if (fitDTMToVoxelSpace) {

                                    dtm.setLimits(new BoundingBox2F(new Point2F((float) infos.getMinCorner().x, (float) infos.getMinCorner().y),
                                            new Point2F((float) infos.getMaxCorner().x, (float) infos.getMaxCorner().y)), dtmFittingMargin);
                                }

                                updateMessage("Converting raster to mesh");
                                dtm.buildMesh();

                                GLMesh dtmMesh = GLMeshFactory.createMeshAndComputeNormalesFromDTM(dtm);
                                SceneObject dtmSceneObject = new SimpleSceneObject(dtmMesh, false);
                                dtmSceneObject.setShader(scene.lightedShader);
                                scene.addSceneObject(dtmSceneObject);

                            }*/
                            
                            /**
                             * *scale**
                             */
                            updateMessage("Generating scale");
                            final Texture scaleTexture = new Texture(ScaleGradient.createColorScaleBufferedImage(voxelSpace.getGradient(),
                                    voxelSpace.getAttributValueMin(), voxelSpace.getAttributValueMax(),
                                    viewer3D.getWidth() - 80, (int) (viewer3D.getHeight() / 20),
                                    ScaleGradient.Orientation.HORIZONTAL, 5, 8));

                            SceneObject scalePlane = SceneObjectFactory.createTexturedPlane(new Vec3F(40, 20, 0), (int) (viewer3D.getWidth() - 80 - 200), (int) (viewer3D.getHeight() / 20), scaleTexture);
                            scalePlane.setShader(scene.texturedShader);
                            scalePlane.setDrawType(GLMesh.DrawType.TRIANGLES);
                            scene.addSceneObject(scalePlane);
                            
                            GLMesh boundingBoxMesh = GLMeshFactory.createBoundingBox((float)header.getMinX(), 
                                                                    (float)header.getMinY(),
                                                                    (float)header.getMinZ(),
                                                                    (float)header.getMaxX(), 
                                                                    (float)header.getMaxY(),
                                                                    (float)header.getMaxZ());
                                                                    
                            SceneObject boundingBox = new SimpleSceneObject2(boundingBoxMesh, false);
                            
                            SimpleShader s = (SimpleShader) scene.simpleShader;
                            s.setColor(new Vec3F(1, 1, 0));
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
                            trackballCamera.setPivot(dtmSceneObject);
                            trackballCamera.setLocation(new Vec3F(dtmSceneObject.getPosition().x-50, dtmSceneObject.getPosition().y, dtmSceneObject.getPosition().z+50));
                            viewer3D.getScene().setCamera(trackballCamera);
                            
                            
                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                        
                                        final Stage toolBarFrameStage = new Stage();
                                        final FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ToolBoxFrame.fxml"));

                                        try {
                                            stage.setAlwaysOnTop(false);

                                            Parent root = loader.load();
                                            Scene scene = new Scene(root);
                                            toolBarFrameStage.setScene(scene);
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
                                                    } else {

                                                        if (!viewer3D.isFocused()) {
                                                            toolBarFrameStage.setAlwaysOnTop(false);
                                                        }
                                                    }
                                                }
                                            });

                                            toolBarFrameStage.show();

                                            //toolBarFrameStage.setHeight(joglWindow.getHeight() / 2);
                                            viewer3D.getJoglContext().setStartX((int) toolBarFrameStage.getWidth());

//                                            viewer3D.getJoglContext().addListener(new JoglListenerListener() {
//
//                                                @Override
//                                                public void sceneInitialized() {
//                                                    viewer3D.setOnTop();
//                                                    toolBarFrameController.initContent(voxelSpace);
//                                                    Platform.runLater(new Runnable() {
//
//                                                        @Override
//                                                        public void run() {
//                                                            toolBarFrameStage.setAlwaysOnTop(true);
//                                                        }
//                                                    });
//
//                                                }
//                                            });
                                            

                                            viewer3D.addWindowListener(new WindowAdapter() {

                                                @Override
                                                public void windowGainedFocus(com.jogamp.newt.event.WindowEvent e) {

                                                    System.out.println("3d window gained focus");
                                                    System.out.println("is 3d window visible? " + Boolean.toString(!viewer3D.getRenderFrame().isVisible()));
                                                    
                                                    viewer3D.setIsFocused(true);
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

                                                    System.out.println("3d window lose focus");
                                                    System.out.println("is 3d window visible? " + Boolean.toString(!viewer3D.getRenderFrame().isVisible()));
                                                    
                                                    if(!viewer3D.getRenderFrame().isVisible()){
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
                                                   
                                                }
                                            });

                                            //viewer3D.addWindowListener(new MinimalWindowAdapter(toolBarFrameStage, viewer3D.getAnimator()));
                                            //joglWindow.setOnTop();
                                            viewer3D.show();

                                            toolBarFrameStage.setAlwaysOnTop(true);
                                        

                                    } catch (IOException e) {
                                        logger.error("Loading ToolBarFrame.fxml failed", e);
                                    }catch (Exception e) {
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
            logger.error(ex);
        }
        
    }
    
}
