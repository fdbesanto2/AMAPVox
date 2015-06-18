/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.gui.MainFrameController;
import fr.ird.voxelidar.lidar.format.raster.BCommon;
import fr.ird.voxelidar.lidar.format.raster.BHeader;
import fr.ird.voxelidar.lidar.format.raster.BSQ;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.File;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javax.imageio.ImageIO;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class FXPrincipal extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        
        
        
        /*
        BufferedImage image = ImageIO.read(new File("/home/calcul/Images/Capture du 2015-06-12 08:42:09.png"));
        
        BHeader header = new BHeader(image.getWidth(), image.getHeight(), 4, BCommon.NumberOfBits.N_BITS_8);
        BSQ raster = new BSQ(new File("/home/calcul/Documents/Julien/test.bsq"), header);
        
        Raster data = image.getData();
        
        for(int x = 0;x<image.getWidth();x++){
            for(int y = 0;y<image.getHeight();y++){
                int[] pixel = new int[3];
                
                data.getPixel(x, y, pixel);
                raster.setPixel(x, y, 0, new Color(pixel[0], pixel[1], pixel[2], 255));
                raster.setPixel(x, y, 1, new Color(pixel[1], pixel[1], pixel[2], 255));
                raster.setPixel(x, y, 2, new Color(pixel[2], pixel[1], pixel[2], 255));
                raster.setPixel(x, y, 3, new Color(255, pixel[1], pixel[2], 255));
            }
        }
        
        raster.writeImage();
        raster.writeHeader();
        */
        
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainFrame.fxml"));
        Parent root = loader.load();
        MainFrameController controller = loader.getController();
        
        Scene scene = new Scene(root);
            
        scene.getStylesheets().add("/styles/Styles.css");
        
        stage.setTitle("AMAPVox");
        stage.setScene(scene);
        stage.getIcons().addAll(new Image("/icons/icon_512x512.png"), 
                        new Image("/icons/icon_256x256.png"), 
                        new Image("/icons/icon_128x128.png"),
                        new Image("/icons/icon_64x64.png"));
        
        controller.setStage(stage);
        
        stage.show();
        
    }

    /**
     * The main() method is ignored in correctly deployed JavaFX application.
     * main() serves only as fallback in case the application can not be
     * launched through deployment artifacts, e.g., in IDEs with limited FX
     * support. NetBeans ignores main().
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
