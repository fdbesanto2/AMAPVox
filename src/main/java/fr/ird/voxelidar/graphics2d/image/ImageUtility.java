package fr.ird.voxelidar.graphics2d.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Julien
 */
public class ImageUtility {
    
    public enum Format{
        JPG("jpg"), PNG("png"), GIF("gif");
        
        private final String value;
        Format(String value){
            this.value = value;
        }
    }
    
    public static void saveImage(BufferedImage image, Format extension, String outputPath){
        
        try {
            if(!outputPath.endsWith("."+extension.value)){
                outputPath+="."+extension.value;
            }
            ImageIO.write(image, extension.value, new File(outputPath));
        } catch (IOException ex) {
            Logger.getLogger(ImageUtility.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
