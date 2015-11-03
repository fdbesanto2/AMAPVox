/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.Scans;
import fr.amap.amapvox.math.matrix.Mat4D;
import fr.amap.amapvox.math.point.Point3F;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Border;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class RspExtractorFrameController implements Initializable {
    
    @FXML
    private TreeView<Scan> treeViewRspContent;
    @FXML
    private CheckBox checkboxSwitchFullDecimated;

    @FXML
    private void onActionButtonImportSelectedScans(ActionEvent event) {
        
        selectedScans = getSelectedScans(root, new ArrayList<>());
        stage.close();
    }

    @FXML
    private void onActionMenuItemSelectAll(ActionEvent event) {
        
        root.setSelected(true);
    }

    @FXML
    private void onActionMenuItemSelectNone(ActionEvent event) {
        root.setSelected(false);
    }
    
    final CheckBoxTreeItem<Scan> root = new CheckBoxTreeItem<>(new Scan("Scan positions", null, null));
    private List<Scan> selectedScans;
    private Stage stage;
    
    public class Scan{
        
        private final String name;
        private final File file;
        private final Mat4D sop;

        public Scan(String name, File file, Mat4D sop) {
            this.name = name;
            this.file = file;
            this.sop = sop;
        }

        public String getName() {
            return name;
        }

        public File getFile() {
            return file;
        }

        public Mat4D getSop() {
            return sop;
        }
        
        
        @Override
        public String toString(){
            return name;
        }
        
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    
        selectedScans = new ArrayList<>();
        
        checkboxSwitchFullDecimated.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                selectLeavesContainingString(root, "mon", newValue);
            }
        });
        
    } 
    
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    public void init(Rsp rsp){
        
        root.setExpanded(true);
        
        
        
        ArrayList<Scans> rxpList = rsp.getRxpList();
        
        double minX = 0, minY = 0, minZ = 0;
        double maxX = 0, maxY = 0, maxZ = 0;
        
        for(int i1=0 ; i1 < rxpList.size();i1++){
            
            Scans scans1 = rxpList.get(i1);
            
            Mat4D sopMatrix1 = scans1.getSopMatrix();
            double x1 = sopMatrix1.mat[3];
            double y1 = sopMatrix1.mat[7];
            double z1 = sopMatrix1.mat[11];
            
            if(i1 == 0){
                minX = x1;
                minY = y1;
                minZ = z1;
                
                maxX = x1;
                maxY = y1;
                maxZ = z1;
            }else{
                
                minX = Double.min(x1, minX);
                minY = Double.min(y1, minY);
                minZ = Double.min(z1, minZ);
                
                maxX = Double.max(x1, maxX);
                maxY = Double.max(y1, maxY);
                maxZ = Double.max(z1, maxZ);
            }
        }
        
        Point3d minPosition = new Point3d(minX, minY, minZ);
        Point3d maxPosition = new Point3d(maxX, maxY, maxZ);
        
        int resolution = 10; //ratio meter/pixel
        
        int padding = 30;
        double sceneWidth = (Math.abs(maxPosition.x - minPosition.x)*resolution) + padding;
        double sceneHeight = (Math.abs(maxPosition.y - minPosition.y)*resolution) + padding;
        
        
        Stage stageTest = new Stage();
        

        // create canvas
        PannableCanvas canvas = new PannableCanvas();
        //canvas.setTranslateX(minPosition.x + sceneWidth/2.0);
        //canvas.setTranslateY(-minPosition.y + sceneHeight/2.0);

        // we don't want the canvas on the top/left in this example => just
        // translate it a bit
        // create sample nodes which can be dragged
        NodeGestures nodeGestures = new NodeGestures( canvas);        
        
        //searching same scanner locations
        for(int i1=0 ; i1 < rxpList.size();i1++){
            
            Scans scans1 = rxpList.get(i1);
            
            Mat4D sopMatrix1 = scans1.getSopMatrix();
            double x1 = sopMatrix1.mat[3];
            double y1 = sopMatrix1.mat[7];
            double z1 = sopMatrix1.mat[11];
            
            Point3d position = new Point3d(x1, y1, z1);
            
            Point3d canvasPosition = new Point3d(position);
            
            //on espace les positions pour plus de lisibilité
            canvasPosition.x *= resolution;
            canvasPosition.y *= resolution;
            
            //l'écriture du fichier vectoriel inverse la direction de l'axe y conventionnel
            canvasPosition.y = -canvasPosition.y;
            
            //on centre la position 0 au milieu de la scène
            canvasPosition.x += sceneWidth/2.0;
            canvasPosition.y += sceneHeight/2.0;
            
            
            Circle circle1 = new Circle(canvasPosition.x, canvasPosition.y, 5);
            
            if(canvasPosition.y < 0){
                System.out.println("test");
            }
            
            
            if(position.x == 0 && position.y == 0 && position.z == 0){ //reference scan
                circle1.setStroke(Color.RED);
                circle1.setFill(Color.RED.deriveColor(1, 1, 1, 0.5));
            }else{
                circle1.setStroke(Color.ORANGE);
                circle1.setFill(Color.ORANGE.deriveColor(1, 1, 1, 0.5));
            }
            
            //circle1.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
            //circle1.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());
            
            canvas.getChildren().add(circle1);
            
            Label label1 = new Label(scans1.getName());
            label1.setTranslateX(canvasPosition.x);
            label1.setTranslateY(canvasPosition.y);
            label1.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
            label1.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());
            
            canvas.getChildren().add(label1);
        }
        
        int minCanvasPositionX = (int) ((minPosition.x*resolution) + sceneWidth/2.0);
        int maxCanvasPositionX = (int) ((maxPosition.x*resolution) + (sceneWidth/2.0));
        
        int minCanvasPositionY = (int) ((-(minPosition.y*resolution)) + (sceneHeight/2.0));
        int maxCanvasPositionY = (int) ((-(maxPosition.y*resolution)) + (sceneHeight/2.0));
        
        canvas.addGrid(minCanvasPositionX, maxCanvasPositionY,
                maxCanvasPositionX - minCanvasPositionX,
                minCanvasPositionY - maxCanvasPositionY, resolution);
        

        // create scene which can be dragged and zoomed
        Scene scene = new Scene(canvas, sceneWidth, sceneHeight);

        SceneGestures sceneGestures = new SceneGestures(canvas);
        scene.addEventFilter( MouseEvent.MOUSE_PRESSED, sceneGestures.getOnMousePressedEventHandler());
        scene.addEventFilter( MouseEvent.MOUSE_DRAGGED, sceneGestures.getOnMouseDraggedEventHandler());
        scene.addEventFilter( ScrollEvent.ANY, sceneGestures.getOnScrollEventHandler());

        stageTest.setScene(scene);
        stageTest.setResizable(true);
        
        stageTest.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                stageTest.setWidth(500);
            }
        });

        stageTest.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number number, Number number2) {
                stageTest.setHeight(500);
            }
        });
        
        stageTest.hide();
        stageTest.show();
        
        
        
//        ArrayList<Scans> rxpList = rsp.getRxpList();
//        
//        Map<Integer, Integer> m = new HashMap<>();
//        List<Integer[]> l = new ArrayList<>();
//        
//        //searching same scanner locations
//        for(int i1=0 ; i1 < rxpList.size();i1++){
//            
//            Scans scans1 = rxpList.get(i1);
//            
//            Mat4D sopMatrix1 = scans1.getSopMatrix();
//            double x1 = sopMatrix1.mat[3];
//            double y1 = sopMatrix1.mat[7];
//            double z1 = sopMatrix1.mat[11];
//            
//            Point3d canvasPosition = new Point3d(x1, y1, z1);
//            
//            
//            for(int i2=0 ; i2 < rxpList.size();i2++){
//                
//                Scans scans2 = rxpList.get(i2);
//                
//                Mat4D sopMatrix2 = scans2.getSopMatrix();
//                
//                double x2 = sopMatrix2.mat[3];
//                double y2 = sopMatrix2.mat[7];
//                double z2 = sopMatrix2.mat[11];
//                
//                Point3d position2 = new Point3d(x2, y2, z2);
//                double distance = canvasPosition.distance(position2);
//                
//                if(distance < 0.5){
//                    m.put(i1, i2);
//                    break;
//                }
//            }
//        }
        
        root.getChildren().clear();
        
        for(Scans scans : rxpList){
            
            
            CheckBoxTreeItem<Scan> item = new CheckBoxTreeItem<>(
                    new Scan(scans.getName(), null, null));
            
            CheckBoxTreeItem<Scan> checkBoxTreeItemFull = new CheckBoxTreeItem<>(
                    new Scan(scans.getScanFull().getName(), scans.getScanFull().getFile(), scans.getScanFull().getSopMatrix()));
            
            CheckBoxTreeItem<Scan> checkBoxTreeItemLite = new CheckBoxTreeItem<>(new Scan(
                    scans.getScanLite().getName(), scans.getScanLite().getFile(), scans.getScanLite().getSopMatrix()));
            
            checkBoxTreeItemFull.setSelected(true);
            
            item.getChildren().add(checkBoxTreeItemFull);
            item.getChildren().add(checkBoxTreeItemLite);
            
            item.setExpanded(true);
            
            root.getChildren().add(item);
        }
        
        treeViewRspContent.setCellFactory(CheckBoxTreeCell.<Scan>forTreeView());
        treeViewRspContent.setRoot(root);
        
    }
    
    private List<Scan> getSelectedScans(CheckBoxTreeItem item, List<Scan> indices){
                
        ObservableList<CheckBoxTreeItem> childrens = item.getChildren();
        
        for(CheckBoxTreeItem children : childrens){
            
            if(children.isLeaf()){
                
                if(children.isSelected()){
                    indices.add((Scan)children.getValue());
                }
                
            }else{
                getSelectedScans(children, indices);
            }
        }
        
        return indices;
    }
    
    private void selectLeavesContainingString(CheckBoxTreeItem node, String string, boolean contains){
        
        ObservableList<CheckBoxTreeItem> childrens = node.getChildren();
        
        for(CheckBoxTreeItem children : childrens){
            
            if(children.isLeaf()){
                
                if(((Scan)children.getValue()).name.contains(string)){
                    children.setSelected(contains);
                }else{
                    children.setSelected(!contains);
                }
            }else{
                selectLeavesContainingString(children, string, contains);
            }
        }
    }

    public List<Scan> getSelectedScans() {
        return selectedScans;
    }
    
}
