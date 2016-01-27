/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.amapvox.commons.util.LidarScan;
import fr.amap.amapvox.commons.util.MatrixUtility;
import fr.amap.amapvox.io.tls.rsp.Scans;
import fr.amap.amapvox.jleica.ptx.PTXReader;
import fr.amap.amapvox.jleica.ptx.PTXScan;
import fr.amap.lidar.amapvox.voxelisation.configuration.PTXLidarScan;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;

/**
 *
 * @author calcul
 */
public class PTXProjectExtractor extends LidarProjectExtractor{
    
    public PTXProjectExtractor() {
        
        super();
    }
    
    public void init(File file) throws IOException{
        
        extractorFrameController.getRoot().setExpanded(true);
        
        PTXReader reader = new PTXReader();
        reader.openPTXFile(file);
        List<PTXScan> singlesScans = reader.getSinglesScans();
        
        extractorFrameController.getRoot().getChildren().clear();
        
        int count = 0;
        for(PTXScan scan : singlesScans){
            
            CheckBoxTreeItem<LidarScan> item = new CheckBoxTreeItem<>(
                    new PTXLidarScan(scan.getFile(), MatrixUtility.convertMat4DToMatrix4d(scan.getHeader().getTransfMatrix()), scan, count));
            
            item.setSelected(true);
            item.setExpanded(true);
            
            extractorFrameController.getRoot().getChildren().add(item);
            
            count++;
        }
        
        extractorFrameController.getTreeViewLidarProjectContent().setCellFactory(CheckBoxTreeCell.<LidarScan>forTreeView());
        extractorFrameController.getTreeViewLidarProjectContent().setRoot(extractorFrameController.getRoot());
        
    }
}
