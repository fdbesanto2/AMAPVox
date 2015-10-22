/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.Scans;
import fr.amap.amapvox.math.matrix.Mat4D;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.control.cell.CheckBoxTreeTableCell;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.util.Callback;

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
