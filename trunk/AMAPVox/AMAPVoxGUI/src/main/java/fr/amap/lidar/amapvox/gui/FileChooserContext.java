/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import java.io.File;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 *
 * @author Julien
 */
public class FileChooserContext {
    
    public FileChooser fc;
    public File lastSelectedFile;
    private String defaultFileName;
    
    public FileChooserContext(){
        fc = new FileChooser();
    }
    
    public FileChooserContext(String defaultFileName){
        fc = new FileChooser();
        this.defaultFileName = defaultFileName;
    }
    
    public File showOpenDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
        }
        
        File resultFile = fc.showOpenDialog(ownerWindow);
        if(resultFile != null){
            lastSelectedFile = resultFile;
        }
        
        return resultFile;
    }
    
    public File showSaveDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
            fc.setInitialFileName(lastSelectedFile.getName());
        }else if(defaultFileName != null){
            fc.setInitialFileName(defaultFileName);
        }
        
        File resultFile = fc.showSaveDialog(ownerWindow);
        if(resultFile != null){
            lastSelectedFile = resultFile;
        }
        
        return resultFile;
    }
}
