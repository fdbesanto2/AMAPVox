/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.javafx.io;

import java.io.File;
import java.util.List;
import javafx.stage.FileChooser;
import javafx.stage.Window;

/**
 * The filechooser methods cannot be override, this class provides a workaround
 * @author Julien Heurtebize
 */
public class FileChooserContext {
    
    private static File DEFAULT_DIRECTORY = new File("");
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
    
    public List<File> showOpenMultipleDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
        }else{
            fc.setInitialDirectory(DEFAULT_DIRECTORY);
        }
        
        List<File> resultFile = fc.showOpenMultipleDialog(ownerWindow);
        if(resultFile != null && resultFile.size() > 0){
            lastSelectedFile = resultFile.get(0);
        }
        
        return resultFile;
    }
    
    public File showOpenDialog(Window ownerWindow){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
        }else{
            fc.setInitialDirectory(DEFAULT_DIRECTORY);
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
            fc.setInitialDirectory(DEFAULT_DIRECTORY);
            fc.setInitialFileName(defaultFileName);
        }
        
        File resultFile = fc.showSaveDialog(ownerWindow);
        if(resultFile != null){
            lastSelectedFile = resultFile;
        }
        
        return resultFile;
    }
    
    /**
     * 
     * @param ownerWindow
     * @param requiredExt Required file extension (example : .ext)
     * @return 
     */
    public File showSaveDialog(Window ownerWindow, String requiredExt){
        
        if(lastSelectedFile != null){
            fc.setInitialDirectory(lastSelectedFile.getParentFile());
            fc.setInitialFileName(lastSelectedFile.getName());
        }else if(defaultFileName != null){
            fc.setInitialDirectory(DEFAULT_DIRECTORY);
            fc.setInitialFileName(defaultFileName);
        }else{
            fc.setInitialDirectory(DEFAULT_DIRECTORY);
            fc.setInitialFileName("*"+requiredExt);
        }
        
        File resultFile;
        
        while(true){
            resultFile = fc.showSaveDialog(ownerWindow);
            
            if(resultFile == null){
                return null;
            }else if(resultFile.getName().endsWith(requiredExt)){
               break; 
            }else{
                fc.setInitialFileName(resultFile.getName()+requiredExt);
            }
        }
        /*do{
            resultFile = fc.showSaveDialog(ownerWindow);
            
            if(resultFile == null){
                return null;
            }
        }while(!resultFile.getName().endsWith(requiredExt));*/
        
        lastSelectedFile = resultFile;
        
        return resultFile;
    }

    public static void setDefaultDirectory(File defaultDirectory) {
        DEFAULT_DIRECTORY = defaultDirectory;
    }
}
