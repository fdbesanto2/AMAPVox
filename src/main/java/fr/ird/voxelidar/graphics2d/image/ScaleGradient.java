/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics2d.image;

import fr.ird.voxelidar.util.ColorGradient;
import java.awt.Color;
import java.awt.image.BufferedImage;

/**
 *
 * @author Julien
 */
public class ScaleGradient {
    
    public static BufferedImage generateScale(Color[] gradientColor, float min, float max, int width, int height){
        
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ColorGradient gradient = new ColorGradient(min, max);
        gradient.setGradientColor(gradientColor);
        
        for (int i = 0; i < width; i++) {
            for (int j = 0, k = height-1; j < height; j++,  k--) {
                float value = (((max-min)*j)/height);
                Color color = gradient.getColor(value);
                bi.setRGB(i, k, color.getRGB());
            }
        }
        
        return bi;
    }
}
