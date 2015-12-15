/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

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
    
    public FileChooserContext(){
        fc = new FileChooser();
    }
    
    public File showOpenDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile);
            fc.setInitialFileName(lastSelectedFile.getName());
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
        }
        
        File resultFile = fc.showSaveDialog(ownerWindow);
        if(resultFile != null){
            lastSelectedFile = resultFile;
        }
        
        return resultFile;
    }
}
