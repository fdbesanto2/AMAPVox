package fr.amap.amapvox.voxviewer;

import com.jogamp.nativewindow.util.Point;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.newt.event.WindowUpdateEvent;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.amapvox.commons.math.matrix.Mat4D;
import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.vector.Vec3F;
import fr.amap.amapvox.commons.util.image.ScaleGradient;
import fr.amap.amapvox.jraster.asc.DtmLoader;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxviewer.event.BasicEvent;
import fr.amap.amapvox.voxviewer.event.EventManager;
import fr.amap.amapvox.voxviewer.input.InputKeyListener;
import fr.amap.amapvox.voxviewer.input.InputMouseAdapter;
import fr.amap.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.object.camera.TrackballCamera;
import fr.amap.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.amapvox.voxviewer.object.scene.SceneObjectFactory;
import fr.amap.amapvox.voxviewer.object.scene.SimpleSceneObject;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpaceSceneObject;
import fr.amap.amapvox.voxviewer.renderer.GLRenderFrame;
import fr.amap.amapvox.voxviewer.renderer.JoglListener;
import java.io.File;
import java.util.List;
import java.util.logging.Level;
import javafx.application.Application;
import javafx.stage.Stage;
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
    
    private final static Logger logger = Logger.getLogger(Viewer3D.class);
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
            
            
            joglContext.width = width;
            joglContext.height = height;

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
        Application.launch(FXViewer3D.class, "--width=853", "--height=512", "--input=/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/las.vox" ,"--attribut=PadBVTotal");
        
//        try {
//            Viewer3D viewer3D = new Viewer3D(0, 0, 640, 480, "Test");
//            viewer3D.attachEventManager(new BasicEvent(viewer3D.getAnimator(), viewer3D.getJoglContext()));
//        
//            fr.amap.amapvox.voxviewer.object.scene.Scene scene = viewer3D.getScene();
//
//            /***VOXEL SPACE***/
//            VoxelSpaceSceneObject voxelSpace = SceneObjectFactory.createVoxelSpace(new File("/home/calcul/Documents/Julien/test.vox"));
//            voxelSpace.setShader(scene.instanceLightedShader);
//            scene.addSceneObject(voxelSpace);
//
//            /***scale***/
//            Texture scaleTexture = Texture.createColorScaleTexture(
//                    ScaleGradient.generateScale(voxelSpace.getGradient(), voxelSpace.attributValueMin, voxelSpace.attributValueMax, viewer3D.getWidth()-80, (int)(viewer3D.getHeight()/20), ScaleGradient.HORIZONTAL), 
//                    voxelSpace.attributValueMin, voxelSpace.attributValueMax);
//
//            scene.addTexture(scaleTexture);
//
//            SceneObject scalePlane = SceneObjectFactory.createTexturedPlane(new Vec3F(40, 20, 0), viewer3D.getWidth()-80, (int)(viewer3D.getHeight()/20), scaleTexture, scene.getShaderByName("textureShader"));
//            scalePlane.setShader(scene.texturedShader);
//            scalePlane.setDrawType(GL3.GL_TRIANGLES);
//            scene.addTexture(scaleTexture);
//            scene.addSceneObject(scalePlane);
//            
//            /***DTM***/
//            RegularDtm dtm = DtmLoader.readFromAscFile(new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt_dtm.asc"));
//            Mat4D transfMatrix = new Mat4D();
//            transfMatrix.mat = new double[]{0.9540688863574789,0.29958731629459895,0.0,-448120.0441687209,
//                -0.29958731629459895,0.9540688863574789,0.0,-470918.3928060016,
//                0.0,0.0,1.0,0.0,
//                0.0,0.0,0.0,1.0};
//            
//            dtm.setTransformationMatrix(transfMatrix);
//            dtm.buildMesh();
//            GLMesh dtmMesh = GLMeshFactory.createMeshAndComputeNormalesFromDTM(dtm);
//            SceneObject dtmSceneObject = new SimpleSceneObject(dtmMesh, false);
//            dtmSceneObject.setShader(scene.lightedShader);
//            scene.addSceneObject(dtmSceneObject);
//
//            /***light***/
//            scene.setLightPosition(new Point3F(voxelSpace.getPosition().x, voxelSpace.getPosition().y, voxelSpace.getPosition().z+voxelSpace.widthZ+100));
//
//            /***camera***/
//            TrackballCamera trackballCamera = new TrackballCamera();
//            trackballCamera.setPivot(voxelSpace);
//            trackballCamera.setLocation(new Vec3F(voxelSpace.getPosition().x+voxelSpace.widthX, voxelSpace.getPosition().y+voxelSpace.widthY, voxelSpace.getPosition().z+voxelSpace.widthZ));
//            viewer3D.getScene().setCamera(trackballCamera);
//            
//            viewer3D.show();
//
//        } catch (Exception ex) {
//            java.util.logging.Logger.getLogger(Viewer3D.class.getName()).log(Level.SEVERE, null, ex);
//        }
        
        //launch(args);
        //launch("--help");
        //MainApp.usage();
        /*
        Mat4D mat = new Mat4D();
        mat.mat = new double[]{0.9540688863574789,0.29958731629459895,0.0,-448120.0441687209,
                -0.29958731629459895,0.9540688863574789,0.0,-470918.3928060016,
                0.0,0.0,1.0,0.0,
                0.0,0.0,0.0,1.0};*/
        
        
        //launch("--width=853", "--height=512", "--input=/home/calcul/Documents/Julien/test.vox" ,"--attribut=PadBVTotal");
        //launch("--width=500","--height=200","--input=/home/calcul/Documents/Julien/las_paracou_transmittance/las_1m.vox", "--attribut=bvEntering");
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
