package fr.amap.amapvox.voxviewer;

import com.jogamp.nativewindow.util.Point;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.amapvox.commons.util.ColorGradient;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.math.vector.Vec3F;
import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jeeb.workspace.sunrapp.util.Colouring;
import fr.amap.amapvox.math.matrix.Mat4D;
import fr.amap.amapvox.math.vector.Vec4D;
import fr.amap.amapvox.voxviewer.event.BasicEvent;
import fr.amap.amapvox.voxviewer.event.EventManager;
import fr.amap.amapvox.voxviewer.input.InputKeyListener;
import fr.amap.amapvox.voxviewer.input.InputMouseAdapter;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.mesh.PointCloudGLMesh;
import fr.amap.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.amapvox.voxviewer.renderer.GLRenderFrame;
import fr.amap.amapvox.voxviewer.renderer.GLRenderWindowListener;
import fr.amap.amapvox.voxviewer.renderer.JoglListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.stage.Stage;
import javax.vecmath.Point3f;
import org.apache.log4j.Logger;

public class Viewer3D extends Application {

    private File voxelFile;
    private String attributeToView;
    static double SCREEN_WIDTH;
    static double SCREEN_HEIGHT;
    private List<String> parameters;
    private final GLRenderFrame renderFrame;
    private final JoglListener joglContext;
    private final FPSAnimator animator;  
    
    private boolean focused;
    
    private int width;
    private int height;
    
    
    public Viewer3D(int posX, int posY, int width, int height, String title) throws Exception{
        
        try{
            
            GLProfile glp = GLProfile.getGL2GL3();
            GLCapabilities caps = new GLCapabilities(glp);
            caps.setDoubleBuffered(true);
            
            this.width = width;
            this.height = height;
            
            renderFrame = GLRenderFrame.create(caps, posX, posY, width, height, title);
            animator = new FPSAnimator(renderFrame, 60);

            joglContext = new JoglListener(animator);
            joglContext.getScene().setWidth(width);
            joglContext.getScene().setHeight(height);

            renderFrame.addGLEventListener(joglContext);
            
            //BasicEvent eventListener = new BasicEvent(animator, joglContext);
            //joglContext.attachEventListener(eventListener);            
            
            animator.start();
            
            
            
        }catch(GLException e){
            throw new GLException("Cannot init opengl", e);
        }catch(Exception e){
            throw new Exception("Unknown error happened", e);
        }
    }
    
//    public Viewer3D(int width, int height, File voxelFile, String attributeToView, 
//            boolean drawDTM, File dtmFile, boolean transformDtm, Mat4D dtmTransform, boolean fitDTMToVoxelSpace, int fittingMargin){
//        
//        String cmd = "--width=" + width+","+
//                     "--height="+height+","+
//                     "--input="+voxelFile.getAbsolutePath()+","+
//                     "--attribut="+attributeToView+",";
//        
//        if(drawDTM && dtmFile != null){
//            cmd += "--dtm="+dtmFile.getAbsolutePath()+",";
//            
//            if(transformDtm){
//                cmd += "--dtm-transform"+",";
//            }
//            
//            if(dtmTransform == null){
//                dtmTransform = Mat4D.identity();
//            }
//            cmd += "--dtm-transf-matrix="+dtmTransform.toString()+",";
//            
//            if(fitDTMToVoxelSpace){
//                cmd += "--dtm-fit"+",";
//                cmd += "--dtm-fitting-margin="+fittingMargin+",";
//            }
//        }
//        
//        
//        
//        cmd = cmd.substring(0, cmd.length()-1);
//                     
//        //System.out.println(cmd.replaceAll(",", " "));
//        
//        String[] split = cmd.split(",");
//        parameters = new ArrayList<>(split.length);
//        
//        for(String s : split){
//            parameters.add(s);
//        }
//        
//        //launch(cmd.split(","));
//    }
    
    
    
    private static void usage(){
        
        System.out.println("Utilisation : java -jar VoxViewer.jar [PARAMETRES]\n");
        System.out.println("Parametre\tDescription\n");
        System.out.println("--help\tAffiche ce message");
        System.out.println("--input=<Fichier voxel>\t\tChemin vers le fichier voxel (obligatoire)");
        System.out.println("--attribut=<Nom de l'attribut>\tChemin vers le fichier voxel");
        System.out.println("--dtm=<Fichier MNT>\t\tChemin vers le modele numerique de terrain associé au modèle");
        System.out.println("--dtm-transform\t\t\tChemin vers le modele numerique de terrain associé au modèle");
        System.out.println("--dtm-fit\t\t\tCharge uniquement la partie du MNT associee a l'espace voxel");
        System.out.println("--dtm-transf-matrix=<Matrice>\tTransforme le MNT dans la vue 3D en translation et rotation,\n"
                + "\t\t\t\tla valeur de Matrice doit être une liste de 16 valeurs séparées par des virgules.\n"
                + "\t\t\t\tLa matrice s'écrit de gauche à droite et de haut en bas.");
    }
    
    public fr.amap.amapvox.voxviewer.object.scene.Scene getScene(){
        return getJoglContext().getScene();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
    
    @Override
    public void start(final Stage stage){
//        
//        /*List<String> args;
//        Parameters p;
//        
//        if(parameters != null){
//            args = parameters;
//            p = new ParametersImpl(parameters);
//        }else{
//            p = getParameters();
//            args = p.getRaw();
//        }
//        
//        
//        //List<String> args = getParameters().getRaw();
//        
//        if(args.size() <= 1){
//            if(args.isEmpty()){
//                usage();
//                exit(0);
//            }else if(args.get(0).equals("--help")){
//                usage();
//                exit(0);
//                return;
//            }
//        }
//        
//        String cmd = "";
//        for(String arg : args){
//            cmd += arg+" ";
//        }
//        
//        logger.info("Launching command: "+cmd);
//        
//        Map<String, String> parametersWithValue =  p.getNamed();
//        List<String> parametersWithNoValue = p.getUnnamed();
//        
//        voxelFile = new File(parametersWithValue.get("input"));
//        attributeToView = parametersWithValue.get("attribut");
//        
//        
//        final int windowWidth;
//        final int windowHeight;
//        final boolean drawDTM;
//        final File dtmFile;
//        final boolean fitDTMToVoxelSpace;
//        final boolean transform;
//        final int dtmFittingMargin;
//        final Mat4D dtmTransfMatrix;
//        
//        //window size
//        ObservableList<Screen> screens = Screen.getScreens();
//
//        if(screens != null && screens.size() > 0){
//            SCREEN_WIDTH = screens.get(0).getBounds().getWidth();
//            SCREEN_HEIGHT = screens.get(0).getBounds().getHeight();
//        }
//        
//        int tmpWindowWidth;
//        try{
//            tmpWindowWidth = Integer.valueOf(parametersWithValue.get("width"));
//        }catch(Exception e){
//            tmpWindowWidth = (int)(SCREEN_WIDTH / 1.5d);
//        }
//        
//        int tmpWindowHeight;
//        
//        try{
//            tmpWindowHeight = Integer.valueOf(parametersWithValue.get("height"));
//        }catch(Exception e){
//            tmpWindowHeight = (int)(SCREEN_HEIGHT / 2.0d);
//        }
//        
//        windowWidth = tmpWindowWidth;
//        windowHeight = tmpWindowHeight;        
//        
//        String dtmPath = parametersWithValue.get("dtm");
//        
//        if(dtmPath == null){
//            drawDTM = false;
//            dtmFile = null;
//            transform = false;
//            fitDTMToVoxelSpace = false;
//            dtmFittingMargin = 0;
//            dtmTransfMatrix = Mat4D.identity();
//        }else{
//            drawDTM = true;
//            dtmFile = new File(dtmPath);
//            
//            fitDTMToVoxelSpace = parametersWithNoValue.contains("--dtm-fit");
//            transform = parametersWithNoValue.contains("--dtm-transform");
//            
//            if(fitDTMToVoxelSpace){
//                if(parametersWithValue.containsKey("dtm-fitting-margin")){
//                    dtmFittingMargin = Integer.valueOf(parametersWithValue.get("dtm-fitting-margin"));
//                }else{
//                    dtmFittingMargin = 0;
//                }
//            }else{
//                dtmFittingMargin = 0;
//            }            
//            
//            String matrix = parametersWithValue.get("dtm-transf-matrix");
//            
//            if(matrix == null){
//                dtmTransfMatrix = Mat4D.identity();
//            }else{
//                String[] matrixStringValues = matrix.replaceAll(" ", ",").split(",");
//                if(matrixStringValues.length != 16){
//                    dtmTransfMatrix = Mat4D.identity();
//                }else{
//                    dtmTransfMatrix = new Mat4D();
//                    
//                    double[] matrixValues = new double[16];
//                    for(int i = 0;i<matrixStringValues.length;i++){
//                        matrixValues[i] = Double.valueOf(matrixStringValues[i]);
//                    }
//                    
//                    dtmTransfMatrix.mat = matrixValues;
//                }
//            }
//        }
//        
//        final Stage toolBarFrameStage = new Stage();
//        final Viewer3D viewer3D;
//        
//        Service s = new Service() {
//
//            @Override
//            protected Task createTask() {
//                return new Task() {
//
//                    @Override
//                    protected Object call() throws Exception {
//                        
//                        
//                        final VoxelSpaceSceneObject voxelSpace = new VoxelSpaceSceneObject(voxelFile);
//                        voxelSpace.setCurrentAttribut(attributeToView);
//
//                        try {
//                            viewer3D = new Viewer3D(((int)windowWidth / 4), (int)windowHeight / 4, windowWidth, windowHeight,
//                                    voxelFile.toString());
//                            
//                            if(drawDTM && dtmFile != null){
//                                
//                                updateMessage("Reading raster file: "+dtmFile.getAbsolutePath());
//                                
//                                RegularDtm dtm = DtmLoader.readFromAscFile(dtmFile);
//            
//                                if(transform && dtmTransfMatrix != null){
//                                    dtm.setTransformationMatrix(dtmTransfMatrix);
//                                }
//                                
//                                if(fitDTMToVoxelSpace){
//                                    
//                                    VoxelFileReader reader = new VoxelFileReader(voxelFile, false);
//                                    VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();
//                                    
//                                    dtm.setLimits(new BoundingBox2F(new Point2F((float)infos.getMinCorner().x, (float)infos.getMinCorner().y), 
//                                                                    new Point2F((float)infos.getMaxCorner().x, (float)infos.getMaxCorner().y)), dtmFittingMargin);
//                                }
//                                
//                                updateMessage("Converting raster to mesh");
//                                dtm.buildMesh();
//                                //dtm.exportObj(new File("/home/calcul/Documents/Julien/test.obj"));
//                                
//                                viewer3D.getJoglContext().getScene().setDtm(dtm);
//                            }
//                            
//                        } catch (Exception ex) {
//                            logger.error(ex.getMessage(), ex);
//                            return null;
//                        }
//
//                        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {
//
//                            @Override
//                            public void voxelSpaceCreationProgress(int progress) {
//                                updateProgress(progress, 100);
//                            }
//                        });
//
//                        voxelSpace.load();
//                        voxelSpace.updateValue();
//                        
//                        
//                        
//                        final int posX = viewer3D.getPosition().getX();
//                        final int posY = viewer3D.getPosition().getY();                        
//                        
//                        
//                        
//                        Platform.runLater(new Runnable() {
//
//                            @Override
//                            public void run() {
//
//                                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ToolBoxFrame.fxml"));
//                                Parent root;
//                                try {
//                                    stage.setAlwaysOnTop(false);
//                                    final ToolBoxFrameController toolBarFrameController;
//                                    
//                                    
//                                    root = loader.load();
//                                    Scene scene = new Scene(root);
//                                    toolBarFrameStage.setScene(scene);
//                                    toolBarFrameStage.initStyle(StageStyle.UNDECORATED);
//                                    toolBarFrameStage.focusedProperty().addListener(new ChangeListener<Boolean>() {
//
//                                        @Override
//                                        public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                                            if(newValue){
//                                                toolBarFrameStage.setAlwaysOnTop(true);
//                                            }else{
//                                                
//                                                if(!viewer3d.isFocused()){
//                                                    toolBarFrameStage.setAlwaysOnTop(false);
//                                                }
//                                            }
//                                        }
//                                    });
//
//                                    toolBarFrameController = loader.getController();
//                                    toolBarFrameController.setJoglListener(joglWindow.getJoglContext());
//                                    
//                                    
//                                    //toolBarFrameController.setAttributes(comboboxAttributeToView.getItems());
//                                    
//                                    toolBarFrameController.setStage(toolBarFrameStage);
//                                    
//                                    toolBarFrameController.setAttributes(attributeToView, voxelSpace.data.getVoxelSpaceInfos().getColumnNames());
//                                    
//                                    toolBarFrameStage.setX(posX);
//                                    toolBarFrameStage.setY(posY);
//                                    toolBarFrameStage.show();
//                                    
//                                    //toolBarFrameStage.setHeight(joglWindow.getHeight() / 2);
//                                    joglWindow.getJoglContext().startX = (int)toolBarFrameStage.getWidth();
//                                    
//                                    joglWindow.getJoglContext().addListener(new JoglListenerListener() {
//
//                                        @Override
//                                        public void sceneInitialized() {
//                                            joglWindow.setOnTop();
//                                            toolBarFrameController.initContent();
//                                            Platform.runLater(new Runnable() {
//
//                                                @Override
//                                                public void run() {
//                                                    toolBarFrameStage.setAlwaysOnTop(true);
//                                                }
//                                            });
//                                            
//                                        }
//                                    });
//                                    
//                                    joglWindow.addWindowListener(new WindowAdapter() {
//
//                                        @Override
//                                        public void windowGainedFocus(com.jogamp.newt.event.WindowEvent e) {
//                                            
//                                            joglWindow.setIsFocused(true);
//                                            Platform.runLater(new Runnable() {
//
//                                                @Override
//                                                public void run() {
//                                                    toolBarFrameStage.setIconified(false);
//                                                    toolBarFrameStage.setAlwaysOnTop(true);
//                                                }
//                                            });
//                                        }
//
//                                        @Override
//                                        public void windowLostFocus(com.jogamp.newt.event.WindowEvent e) {
//                                            
//                                            joglWindow.setIsFocused(false);
//                                            Platform.runLater(new Runnable() {
//
//                                                @Override
//                                                public void run() {
//                                                    if(!toolBarFrameStage.focusedProperty().get()){
//                                                        toolBarFrameStage.setIconified(true);
//                                                        toolBarFrameStage.setAlwaysOnTop(false);
//                                                    }
//                                                }
//                                            });
//                                        }
//                                    });
//                                    
//                                    joglWindow.addWindowListener(new GLRenderWindowListener(toolBarFrameStage, joglWindow.getAnimator()));
//                                    //joglWindow.setOnTop();
//                                    joglWindow.show();
//                                    
//                                    toolBarFrameStage.setAlwaysOnTop(true);
//
//
//                                } catch (IOException e) {
//                                    logger.error("Loading ToolBarFrame.fxml failed", e);
//                                } catch (Exception e) {
//                                    logger.error("Error during toolbar init", e);
//                                }
//                            }
//                        });
//
//                        return null;
//                    }
//                };
//            }
//        };
//
//        ProgressDialog d = new ProgressDialog(s);
//        d.show();
//        /*
//        ChangeListener<Boolean> cl = new ChangeListener<Boolean>() {
//
//            @Override
//            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
//                
//                
//                if (newValue) {
//                    Platform.runLater(new Runnable() {
//
//                        @Override
//                        public void run() {
//                            
//                            
//                        }
//                    });
//                    
//                    stage.focusedProperty().removeListener(this);
//                }
//                
//                
//
//            }
//        };
//        stage.focusedProperty().addListener(cl);
//        */
//        s.start();
//        
            
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //Application.launch(FXViewer3D.class, "--width=853", "--height=512", "--input=/home/calcul/Documents/Julien/Sortie voxels/ALS/Paracou/2013/dalle9/las_2m.vox" ,"--attribut=PadBVTotal");
        //Application.launch(FXViewer3D.class, "--width=853", "--height=512", "--input=/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/las.vox" ,"--attribut=PadBVTotal");
        
        try {
            
            
            
            PointCloudSceneObject pointCloud = new PointCloudSceneObject(2);
            Rsp rsp = new Rsp();
            rsp.read(new File("/media/calcul/IomegaHDD/BDLidar/TLS/Paracou2013/Paracou2013complet.RISCAN/project.rsp"));
            
            
            long startTime = System.currentTimeMillis();
            
            RxpExtraction reader = new RxpExtraction();
            reader.openRxpFile(new File("/media/calcul/IomegaHDD/BDLidar/TLS/Paracou2013/Paracou2013complet.RISCAN/SCANS/ScanPos001/SINGLESCANS/130917_153258.mon.rxp"), RxpExtraction.SHOT_WITH_REFLECTANCE);
            Iterator<Shot> iterator = reader.iterator();
            
            /*List<Float> vertexDataList = new ArrayList<>(81547452);
            List<Float> colorDataList = new ArrayList<>(81547452);*/
            Mat4D sopMatrix = rsp.getRxpList().get(0).getSopMatrix();
            int count = 0;
            
            while(iterator.hasNext()){
                
                Shot shot = iterator.next();
                
                for(int i=0;i<shot.ranges.length;i++){
                    
                    double range = shot.ranges[i];
                    
                    float x = (float) (shot.origin.x + shot.direction.x * range);
                    float y = (float) (shot.origin.y + shot.direction.y * range);
                    float z = (float) (shot.origin.z + shot.direction.z * range);
                    
                    Vec4D transformedPoint = Mat4D.multiply(sopMatrix, new Vec4D(x, y, z, 1));
                    pointCloud.addPoint((float)transformedPoint.x, (float)transformedPoint.y, (float)transformedPoint.z);
                    
                    double reflectance = shot.reflectances[i];
                    
                    float reflectanceColor = (float) ((reflectance+38)/69.0f);
                    
                    pointCloud.addColor(0, reflectanceColor, reflectanceColor, reflectanceColor);
                    
                    float zColor = (float) (Math.abs(z)/70.0f);
                    Point3f rainbowRGB = Colouring.rainbowRGB(zColor);
                    pointCloud.addColor(1, rainbowRGB.x/255.0f, rainbowRGB.y/255.0f, rainbowRGB.z/255.0f);
                }
                
                count++;
                
            }
            
            long endTime = System.currentTimeMillis();
            System.out.println("Temps de lecture du fichier : "+(endTime-startTime)+" ms");
            System.out.println("Nombre de tirs : "+count);
            
            reader.openRxpFile(new File("/media/calcul/IomegaHDD/BDLidar/TLS/Paracou2013/Paracou2013complet.RISCAN/SCANS/ScanPos002/SINGLESCANS/130917_155228.mon.rxp"), RxpExtraction.SHOT_WITH_REFLECTANCE);
            iterator = reader.iterator();
            
            sopMatrix = rsp.getRxpList().get(1).getSopMatrix();
            
            count = 0;
            
            while(iterator.hasNext()){
                
                Shot shot = iterator.next();
                
                for(int i=0;i<shot.ranges.length;i++){
                    
                    double range = shot.ranges[i];
                    
                    float x = (float) (shot.origin.x + shot.direction.x * range);
                    float y = (float) (shot.origin.y + shot.direction.y * range);
                    float z = (float) (shot.origin.z + shot.direction.z * range);
                    
                    Vec4D transformedPoint = Mat4D.multiply(sopMatrix, new Vec4D(x, y, z, 1));
                    pointCloud.addPoint((float)transformedPoint.x, (float)transformedPoint.y, (float)transformedPoint.z);
                    
                    double reflectance = shot.reflectances[i];
                    
                    float reflectanceColor = (float) ((reflectance+38)/69.0f);
                    
                    pointCloud.addColor(0, reflectanceColor, reflectanceColor, reflectanceColor);
                    
                    float zColor = (float) (Math.abs(z)/70.0f);
                    Point3f rainbowRGB = Colouring.rainbowRGB(zColor);
                    pointCloud.addColor(1, rainbowRGB.x/255.0f, rainbowRGB.y/255.0f, rainbowRGB.z/255.0f);
                }
                
                count++;
                
            }
            
            //System.out.println("Nombre de points : "+vertexDataList.size()/3);
            
            /*float[] vertexData = new float[vertexDataList.size()];
            float[] colorData = new float[colorDataList.size()];
            
            for(int i=0;i<vertexDataList.size();i++){
                vertexData[i] = vertexDataList.get(i);
                colorData[i] = colorDataList.get(i);
            }*/
            
            
//            LasReader lasReader = new LasReader();
//            lasReader.open(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt.las"));
//            LasHeader header = lasReader.getHeader();
//            int numberOfPointrecords = (int) header.getNumberOfPointrecords();
//            
//            float[] vertexData = new float[numberOfPointrecords*3];
//            float[] colorData = new float[numberOfPointrecords*3];
//            
//            Iterator<PointDataRecordFormat> iterator = lasReader.iterator();
//            Mat4D transfMatrix = Mat4D.identity();
//            
//            int i=0;
//            while(iterator.hasNext()){
//                
//                PointDataRecordFormat point = iterator.next();
//                float x = (float) ((header.getxOffset()) + point.getX() * header.getxScaleFactor());
//                float y = (float) ((header.getyOffset()) + point.getY() * header.getyScaleFactor());
//                float z = (float) ((header.getzOffset()) + point.getZ() * header.getzScaleFactor());
//                
//                Vec4D pointTransformed = Mat4D.multiply(transfMatrix, new Vec4D(x, y, z, 1));
//                vertexData[i] = (float) pointTransformed.x;
//                vertexData[i+1] = (float) pointTransformed.y;
//                vertexData[i+2] = (float) pointTransformed.z;
//                
//                if(point.getClassification() == 2){
//                    colorData[i] = 1;
//                    colorData[i+2] = 0;
//                }else{
//                    colorData[i] = 0;
//                    colorData[i+2] = 1;
//                }
//                
//                colorData[i+1] = 0;
//                
//                
//                i+=3;
//            }
            
            Viewer3D viewer3D = new Viewer3D(((int) 640 / 4), (int) 480 / 4, 640, 480, "");
            viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));
            fr.amap.amapvox.voxviewer.object.scene.Scene scene = viewer3D.getScene();
            
            //PointCloudGLMesh pointcloudMesh = (PointCloudGLMesh) GLMeshFactory.createPointCloud(vertexData,colorData);
            
            //PointCloudSceneObject pointCloud = new PointCloudSceneObject(pointcloudMesh, false);
            pointCloud.initMesh();
            pointCloud.setShader(scene.colorShader);
            scene.addSceneObject(pointCloud);
            
            /**
             * *light**
             */
            scene.setLightPosition(new Point3F(pointCloud.getPosition().x, pointCloud.getPosition().y, pointCloud.getPosition().z + 100));
            
            /**
             * *camera**
             */
            TrackballCamera trackballCamera = new TrackballCamera();
            trackballCamera.setPivot(pointCloud);
            trackballCamera.setLocation(new Vec3F(pointCloud.getPosition().x-50, pointCloud.getPosition().y, pointCloud.getPosition().z+50));
            viewer3D.getScene().setCamera(trackballCamera);
            
            
            
            
            viewer3D.addWindowListener(new GLRenderWindowListener(null, viewer3D.getAnimator()));
            //joglWindow.setOnTop();
            viewer3D.show();
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(Viewer3D.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public void attachEventManager(EventManager eventManager){
        
        joglContext.attachEventListener(eventManager);
        renderFrame.addKeyListener(new InputKeyListener(eventManager, animator));
        renderFrame.addMouseListener(new InputMouseAdapter(eventManager, animator));
    }
    
    public void addWindowListener(WindowListener listener){
        
        renderFrame.addWindowListener(listener);
    }

    public GLRenderFrame getRenderFrame() {
        return renderFrame;
    }
    
    
    public Point getPosition(){
        Point locationOnScreen = renderFrame.getLocationOnScreen(null);
        return new Point(locationOnScreen.getX(), locationOnScreen.getY());
    }
    
    public void show(){
        this.setOnTop();
        renderFrame.setVisible(true);
    }
    
    public void setOnTop(){
        renderFrame.setAlwaysOnTop(true);
        renderFrame.setAlwaysOnTop(false);
    }

    public JoglListener getJoglContext() {
        return joglContext;
    }

    public FPSAnimator getAnimator() {
        return animator;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setIsFocused(boolean focused) {
        this.focused = focused;
    }

}
