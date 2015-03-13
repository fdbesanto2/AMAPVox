package fr.ird.voxelidar.util.image;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;

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
    
    private final static Logger logger = Logger.getLogger(ImageUtility.class);
    
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
            logger.error(ex);
        }
    }
}
