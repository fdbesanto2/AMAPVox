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
import java.util.ArrayList;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;

/**
 *
 * @author calcul
 */
public class RiscanProjectExtractor extends LidarProjectExtractor{

    private final CheckBox checkboxSwitchFullDecimated;
    private Rsp rsp;
    
    
    public RiscanProjectExtractor() {
        
        super();
        
        checkboxSwitchFullDecimated = new CheckBox("Switch full/decimated");
        
        checkboxSwitchFullDecimated.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                selectLeavesContainingString(extractorFrameController.getRoot(), "mon", newValue);
            }
        });
         
        extractorFrameController.getHboxSelection().getChildren().add(checkboxSwitchFullDecimated);
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
    
    public void init(Rsp rsp){
        
        extractorFrameController.getRoot().setExpanded(true);
        this.rsp = rsp;
        
        ArrayList<Scans> rxpList = rsp.getRxpList();
        
        extractorFrameController.getRoot().getChildren().clear();
        
        for(Scans scans : rxpList){
            
            
            CheckBoxTreeItem<LidarScan> item = new CheckBoxTreeItem<>(
                    new LidarScan(null, null, scans.getName()));
            
            CheckBoxTreeItem<LidarScan> checkBoxTreeItemFull = new CheckBoxTreeItem<>(
                    new LidarScan(scans.getScanFull().getFile(), MatrixUtility.convertMat4DToMatrix4d(scans.getScanFull().getSopMatrix()), scans.getScanFull().getName()));
            
            CheckBoxTreeItem<LidarScan> checkBoxTreeItemLite = new CheckBoxTreeItem<>(
                    new LidarScan(scans.getScanLite().getFile(), MatrixUtility.convertMat4DToMatrix4d(scans.getScanLite().getSopMatrix()), scans.getScanLite().getName()));
            
            checkBoxTreeItemFull.setSelected(true);
            
            item.getChildren().add(checkBoxTreeItemFull);
            item.getChildren().add(checkBoxTreeItemLite);
            
            item.setExpanded(true);
            
            extractorFrameController.getRoot().getChildren().add(item);
        }
        
        extractorFrameController.getTreeViewLidarProjectContent().setCellFactory(CheckBoxTreeCell.<LidarScan>forTreeView());
        extractorFrameController.getTreeViewLidarProjectContent().setRoot(extractorFrameController.getRoot());
        
    }
}
