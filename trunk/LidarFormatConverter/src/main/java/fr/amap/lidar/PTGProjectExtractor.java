/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar;

import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.lidar.format.jleica.ptg.PTGReader;
import fr.amap.lidar.format.jleica.ptg.PTGScan;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.scene.control.cell.CheckBoxTreeCell;

/**
 *
 * @author calcul
 */
public class PTGProjectExtractor extends LidarProjectExtractor{
    
    public PTGProjectExtractor() {
        
        super();
    }
    
    public void init(File file) throws IOException, Exception{
        
        extractorFrameController.getRoot().setExpanded(true);
        
        PTGReader reader = new PTGReader();
        reader.openPTGFile(file);
        
        List<File> singlesScans = reader.getScanList();
        
        extractorFrameController.getRoot().getChildren().clear();
        
        for(File f : singlesScans){
            
            PTGScan scan = new PTGScan();
            scan.openScanFile(f);
            
            CheckBoxTreeItem<LidarScan> item = new CheckBoxTreeItem<>(
                    new LidarScan(scan.getFile(), scan.getHeader().getTransfMatrix(), scan.getFile().getName()));
            
            item.setSelected(true);
            item.setExpanded(true);
            
            extractorFrameController.getRoot().getChildren().add(item);
        }
        
        extractorFrameController.getTreeViewLidarProjectContent().setCellFactory(CheckBoxTreeCell.<LidarScan>forTreeView());
        extractorFrameController.getTreeViewLidarProjectContent().setRoot(extractorFrameController.getRoot());
        
    }
}
