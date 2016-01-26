/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.rxptolaz;

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
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.stage.Stage;

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
        
        selectedScans = getSelectedScans(root, new ArrayList<Scan>());
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
