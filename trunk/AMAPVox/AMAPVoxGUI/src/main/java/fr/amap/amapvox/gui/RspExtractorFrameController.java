/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

import fr.amap.amapvox.commons.util.LidarScan;
import fr.amap.amapvox.commons.util.MatrixUtility;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.Scans;
import fr.amap.commons.math.matrix.Mat4D;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.Label;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javax.vecmath.Point3d;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class RspExtractorFrameController implements Initializable {
    
    @FXML
    private HBox hboxSelection;
    @FXML
    private TreeView<LidarScan> treeViewLidarProjectContent;

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
    
    final CheckBoxTreeItem<LidarScan> root = new CheckBoxTreeItem<>(new LidarScan(null, null, "Scan positions"));
    private List<LidarScan> selectedScans;
    private Stage stage;
    private Rsp rsp;

    @FXML
    private void onActionButtonVizualizeScansPositions(ActionEvent event) {
        
        List<LidarScan> selectedScans = getSelectedScans(root, new ArrayList<>());
        
            double minX = 0, minY = 0, minZ = 0;
            double maxX = 0, maxY = 0, maxZ = 0;

            for(int i1=0 ; i1 < selectedScans.size();i1++){

                LidarScan scan = selectedScans.get(i1);

                Mat4D sopMatrix1 = MatrixUtility.convertMatrix4dToMat4D(scan.getMatrix());
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
            for(int i1=0 ; i1 < selectedScans.size();i1++){

                LidarScan scan = selectedScans.get(i1);

                Mat4D sopMatrix1 = MatrixUtility.convertMatrix4dToMat4D(scan.getMatrix());
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

                Label label1 = new Label(scan.getName()+" ("+((Math.round(position.x*10000))/10000.0)+
                                                                                                    " "+((Math.round(position.y*10000))/10000.0)+
                                                                                                    " "+((Math.round(position.z*10000))/10000.0)+")");

                label1.setTranslateX(canvasPosition.x);
                label1.setTranslateY(canvasPosition.y);
                label1.addEventFilter( MouseEvent.MOUSE_PRESSED, nodeGestures.getOnMousePressedEventHandler());
                label1.addEventFilter( MouseEvent.MOUSE_DRAGGED, nodeGestures.getOnMouseDraggedEventHandler());

                canvas.getChildren().add(label1);
            }

            /*int minCanvasPositionX = (int) ((minPosition.x*resolution) + sceneWidth/2.0);
            int maxCanvasPositionX = (int) ((maxPosition.x*resolution) + (sceneWidth/2.0));

            int minCanvasPositionY = (int) ((-(minPosition.y*resolution)) + (sceneHeight/2.0));
            int maxCanvasPositionY = (int) ((-(maxPosition.y*resolution)) + (sceneHeight/2.0));

            canvas.addGrid(minCanvasPositionX, maxCanvasPositionY,
                    maxCanvasPositionX - minCanvasPositionX,
                    minCanvasPositionY - maxCanvasPositionY, resolution);*/


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
        
    }
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
    
        selectedScans = new ArrayList<>();
    } 
    
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    private List<LidarScan> getSelectedScans(CheckBoxTreeItem item, List<LidarScan> indices){
                
        ObservableList<CheckBoxTreeItem> childrens = item.getChildren();
        
        for(CheckBoxTreeItem children : childrens){
            
            if(children.isLeaf()){
                
                if(children.isSelected()){
                    indices.add((LidarScan)children.getValue());
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
                
                if(((LidarScan)children.getValue()).getName().contains(string)){
                    children.setSelected(contains);
                }else{
                    children.setSelected(!contains);
                }
            }else{
                selectLeavesContainingString(children, string, contains);
            }
        }
    }

    public List<LidarScan> getSelectedScans() {
        return selectedScans;
    }

    public HBox getHboxSelection() {
        return hboxSelection;
    }

    public TreeView<LidarScan> getTreeViewLidarProjectContent() {
        return treeViewLidarProjectContent;
    }

    public CheckBoxTreeItem<LidarScan> getRoot() {
        return root;
    }
}
