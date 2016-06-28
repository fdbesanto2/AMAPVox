/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.util.ColorGradient;
import fr.amap.commons.util.io.file.FileManager;
import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class Util {
    
    public final static List<String> AVAILABLE_GRADIENT_COLOR_NAMES = new ArrayList<>();
    public final static List<Color[]> AVAILABLE_GRADIENT_COLORS = new ArrayList<>();
    
    private final static Logger LOGGER = Logger.getLogger(Util.class);

    static{
        try {

            Class c = ColorGradient.class;
            Field[] fields = c.getFields();
        
            for (Field field : fields) {

                String type = field.getType().getSimpleName();
                if (type.equals("Color[]")) {
                    AVAILABLE_GRADIENT_COLOR_NAMES.add(field.getName());
                    AVAILABLE_GRADIENT_COLORS.add((Color[])field.get(c));
                }
            }

        } catch (IllegalArgumentException | IllegalAccessException ex) {
            LOGGER.error("Cannot retrieve avaialble color gradients", ex);
        }
    }
 
    public static boolean checkIfVoxelFile(File voxelFile){
        
        boolean valid = true;
        
        if(voxelFile != null){
            String header = FileManager.readHeader(voxelFile.getAbsolutePath());
            
            if(header != null && header.equals("VOXEL SPACE")){
                
            }else{
                valid = false;
            }
        }else{
            valid = false;
        }
        
        return valid;
    }
    
    public static FxmlContext loadFxml(String resourcePath) throws IOException{
        
        FXMLLoader loader = new FXMLLoader(Util.class.getResource(resourcePath));
        
        Parent root = loader.load();

        return new FxmlContext(loader.getController(), root);
    }
}

