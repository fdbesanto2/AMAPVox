/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

import fr.amap.commons.math.matrix.Mat4D;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.CheckBoxTreeItem;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class LidarProjectExtractor{
    
    private final static Logger LOGGER = Logger.getLogger(LidarProjectExtractor.class);
    
    protected RspExtractorFrameController extractorFrameController;
    protected Stage extractorFrame;
    
    public LidarProjectExtractor(){
        
        try {
            FXMLLoader loader = new FXMLLoader(LidarProjectExtractor.class.getResource("/fxml/RspExtractorFrame.fxml"));
            Parent root = loader.load();
            extractorFrame = new Stage();
            extractorFrameController = loader.getController();
            extractorFrame.setScene(new Scene(root));
            extractorFrameController.setStage(extractorFrame);
        
        } catch (IOException ex) {
            LOGGER.error("Cannot load fxml file", ex);
        }
    }
    public RspExtractorFrameController getController() {
        return extractorFrameController;
    }

    public Stage getFrame() {
        return extractorFrame;
    }

    
}
