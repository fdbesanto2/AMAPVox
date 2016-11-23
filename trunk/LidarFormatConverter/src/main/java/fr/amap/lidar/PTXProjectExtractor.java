/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar;

import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.lidar.format.jleica.ptx.PTXReader;
import fr.amap.lidar.format.jleica.ptx.PTXScan;
import java.io.File;
import java.io.IOException;
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
                    new PTXLidarScan(scan.getFile(), scan.getHeader().getTransfMatrix(), scan, count));
            
            item.setSelected(true);
            item.setExpanded(true);
            
            extractorFrameController.getRoot().getChildren().add(item);
            
            count++;
        }
        
        extractorFrameController.getTreeViewLidarProjectContent().setCellFactory(CheckBoxTreeCell.<LidarScan>forTreeView());
        extractorFrameController.getTreeViewLidarProjectContent().setRoot(extractorFrameController.getRoot());
        
    }
}
