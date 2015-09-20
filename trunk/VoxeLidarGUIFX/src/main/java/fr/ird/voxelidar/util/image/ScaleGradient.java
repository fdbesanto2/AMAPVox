/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util.image;

import fr.ird.voxelidar.util.ColorGradient;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ScaleGradient {
    
    public static final short HORIZONTAL = 1;
    public static final short VERTICAL = 2;
    
    
    public static BufferedImage generateScale(Color[] gradientColor, float min, float max, int width, int height, short type){
        
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ColorGradient gradient = new ColorGradient(min, max);
        gradient.setGradientColor(gradientColor);
        
        switch(type){
            case VERTICAL:
                for (int i = 0; i < width; i++) {
                    for (int j = 0, k = height-1; j < height; j++,  k--) {
                        float value = (((max-min)*j)/height);
                        Color color = gradient.getColor(value);
                        bi.setRGB(i, k, color.getRGB());
                    }
                }

                break;
            case HORIZONTAL:
                
                for (int i = 0; i < height; i++) {
                    for (int j = 0; j < width; j++) {
                        //float value = (((max-min)*j)/width);
                        float value = (j/(float)width)*(max-min)+min;
                        Color color = gradient.getColor(value);
                        bi.setRGB(j, i, color.getRGB());
                    }
                }

                break;
        }
        
        return bi;
    }
}
