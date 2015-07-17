package fr.amap.amapvox.voxviewer;

import com.jogamp.newt.event.WindowAdapter;
import com.sun.javafx.application.ParametersImpl;
import fr.amap.amapvox.commons.math.matrix.Mat4D;
import fr.amap.amapvox.commons.math.point.Point2F;
import fr.amap.amapvox.commons.util.BoundingBox2F;
import fr.amap.amapvox.jraster.asc.DtmLoader;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpace;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpaceAdapter;
import fr.amap.amapvox.voxviewer.object.scene.VoxelSpaceHeader;
import fr.amap.amapvox.voxviewer.renderer.GLRenderWindowListener;
import fr.amap.amapvox.voxviewer.renderer.JoglListenerListener;
import java.io.File;
import java.io.IOException;
import static java.lang.System.exit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.application.Application;
import static javafx.application.Application.launch;
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

public class Viewer3D extends Application {

    private File voxelFile;
    private String attributeToView;
    static double SCREEN_WIDTH;
    static double SCREEN_HEIGHT;
    private List<String> parameters;
    
    private final static Logger logger = Logger.getLogger(Viewer3D.class);
    
    public Viewer3D(){
        
    }
    
    public Viewer3D(int width, int height, File voxelFile, String attributeToView, 
            boolean drawDTM, File dtmFile, boolean transformDtm, Mat4D dtmTransform, boolean fitDTMToVoxelSpace, int fittingMargin){
        
        String cmd = "--width=" + width+","+
                     "--height="+height+","+
                     "--input="+voxelFile.getAbsolutePath()+","+
                     "--attribut="+attributeToView+",";
        
        if(drawDTM && dtmFile != null){
            cmd += "--dtm="+dtmFile.getAbsolutePath()+",";
            
            if(transformDtm){
                cmd += "--dtm-transform"+",";
            }
            
            if(dtmTransform == null){
                dtmTransform = Mat4D.identity();
            }
            cmd += "--dtm-transf-matrix="+dtmTransform.toString()+",";
            
            if(fitDTMToVoxelSpace){
                cmd += "--dtm-fit"+",";
            }
        }
        
        cmd = cmd.substring(0, cmd.length()-1);
                     
        //System.out.println(cmd.replaceAll(",", " "));
        
        String[] split = cmd.split(",");
        parameters = new ArrayList<>(split.length);
        
        for(String s : split){
            parameters.add(s);
        }
        
        //launch(cmd.split(","));
    }
    
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
    
    @Override
    public void start(Stage stage){
        
        List<String> args;
        Parameters p;
        
        if(parameters != null){
            args = parameters;
            p = new ParametersImpl(parameters);
        }else{
            p = getParameters();
            args = p.getRaw();
        }
        
        
        //List<String> args = getParameters().getRaw();
        
        if(args.size() <= 1){
            if(args.isEmpty()){
                usage();
                exit(0);
            }else if(args.get(0).equals("--help")){
                usage();
                exit(0);
                return;
            }
        }
        
        String cmd = "";
        for(String arg : args){
            cmd += arg+" ";
        }
        
        logger.info("Launching command: "+cmd);
        
        Map<String, String> parametersWithValue =  p.getNamed();
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

        if(screens != null && screens.size() > 0){
            SCREEN_WIDTH = screens.get(0).getBounds().getWidth();
            SCREEN_HEIGHT = screens.get(0).getBounds().getHeight();
        }
        
        int tmpWindowWidth;
        try{
            tmpWindowWidth = Integer.valueOf(parametersWithValue.get("width"));
        }catch(Exception e){
            tmpWindowWidth = (int)(SCREEN_WIDTH / 1.5d);
        }
        
        int tmpWindowHeight;
        
        try{
            tmpWindowHeight = Integer.valueOf(parametersWithValue.get("height"));
        }catch(Exception e){
            tmpWindowHeight = (int)(SCREEN_HEIGHT / 2.0d);
        }
        
        windowWidth = tmpWindowWidth;
        windowHeight = tmpWindowHeight;        
        
        String dtmPath = parametersWithValue.get("dtm");
        
        if(dtmPath == null){
            drawDTM = false;
            dtmFile = null;
            transform = false;
            fitDTMToVoxelSpace = false;
            dtmFittingMargin = 0;
            dtmTransfMatrix = Mat4D.identity();
        }else{
            drawDTM = true;
            dtmFile = new File(dtmPath);
            
            fitDTMToVoxelSpace = parametersWithNoValue.contains("--dtm-fit");
            transform = parametersWithNoValue.contains("--dtm-transform");
            
            if(fitDTMToVoxelSpace){
                if(parametersWithValue.containsKey("dtm-fitting-margin")){
                    dtmFittingMargin = Integer.valueOf(parametersWithValue.get("dtm-fitting-margin"));
                }else{
                    dtmFittingMargin = 0;
                }
            }else{
                dtmFittingMargin = 0;
            }            
            
            String matrix = parametersWithValue.get("dtm-transf-matrix");
            
            if(matrix == null){
                dtmTransfMatrix = Mat4D.identity();
            }else{
                String[] matrixStringValues = matrix.replaceAll(" ", ",").split(",");
                if(matrixStringValues.length != 16){
                    dtmTransfMatrix = Mat4D.identity();
                }else{
                    dtmTransfMatrix = new Mat4D();
                    
                    double[] matrixValues = new double[16];
                    for(int i = 0;i<matrixStringValues.length;i++){
                        matrixValues[i] = Double.valueOf(matrixStringValues[i]);
                    }
                    
                    dtmTransfMatrix.mat = matrixValues;
                }
            }
        }
        
        final Stage toolBarFrameStage = new Stage();
        
        Service s = new Service() {

            @Override
            protected Task createTask() {
                return new Task() {

                    @Override
                    protected Object call() throws Exception {
                        
                        
                        final VoxelSpace voxelSpace = new VoxelSpace(voxelFile);
                        voxelSpace.setCurrentAttribut(attributeToView);

                        final JOGLWindow joglWindow;
                        try {
                            joglWindow = new JOGLWindow(((int)windowWidth / 4), (int)windowHeight / 4, windowWidth, windowHeight,
                                    voxelFile.toString(),
                                    voxelSpace);
                            
                            if(drawDTM && dtmFile != null){
                                
                                updateMessage("Reading raster file: "+dtmFile.getAbsolutePath());
                                
                                RegularDtm dtm = DtmLoader.readFromAscFile(dtmFile);
            
                                if(transform && dtmTransfMatrix != null){
                                    dtm.setTransformationMatrix(dtmTransfMatrix);
                                }
                                
                                if(fitDTMToVoxelSpace){
                                    
                                    VoxelSpaceHeader header = VoxelSpaceHeader.readVoxelFileHeader(voxelFile);
                                    
                                    dtm.setLimits(new BoundingBox2F(new Point2F((float)header.bottomCorner.x, (float)header.bottomCorner.y), 
                                                                    new Point2F((float)header.topCorner.x, (float)header.topCorner.y)), dtmFittingMargin);
                                }
                                
                                updateMessage("Converting raster to mesh");
                                dtm.buildMesh();
                                //dtm.exportObj(new File("/home/calcul/Documents/Julien/test.obj"));
                                
                                joglWindow.getJoglContext().getScene().setDtm(dtm);
                            }
                            
                        } catch (Exception ex) {
                            //logger.error(ex.getMessage(), ex);
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
                                    
                                    
                                    //toolBarFrameController.setAttributes(comboboxAttributeToView.getItems());
                                    
                                    toolBarFrameController.setStage(toolBarFrameStage);
                                    
                                    toolBarFrameController.setAttributes(attributeToView, voxelSpace.data.header.attributsNames);
                                    
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
                                    //logger.error("Loading ToolBarFrame.fxml failed", e);
                                } catch (Exception e) {
                                    //logger.error("Error during toolbar init", e);
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

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        launch(args);
        //launch("--help");
        //MainApp.usage();
        /*
        Mat4D mat = new Mat4D();
        mat.mat = new double[]{0.9540688863574789,0.29958731629459895,0.0,-448120.0441687209,
                -0.29958731629459895,0.9540688863574789,0.0,-470918.3928060016,
                0.0,0.0,1.0,0.0,
                0.0,0.0,0.0,1.0};
        
        
        launch("--input=/home/calcul/Documents/Julien/Sortie voxels/comparaison_als_tls_transect_paracou_2013/las_1m.vox",
                "--dtm=/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt_dtm.asc","--dtm-fit","--dtm-transform",
                "--dtm-transf-matrix="+mat.toString());*/
        //launch("--width=500","--height=200","--input=/home/calcul/Documents/Julien/las_paracou_transmittance/las_1m.vox", "--attribut=bvEntering");
    }

}
