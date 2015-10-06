/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.util.image;

import fr.amap.amapvox.commons.util.ColorGradient;
import fr.amap.amapvox.commons.util.DecimalScientificFormat;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.text.DecimalFormat;
import java.text.FieldPosition;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ScaleGradient {
    
    public static final short HORIZONTAL = 1;
    public static final short VERTICAL = 2;
    
    public static BufferedImage generateColorGradientImage(Color[] gradientColor, float min, float max, int width, int height, short orientation){
        
        BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        ColorGradient gradient = new ColorGradient(min, max);
        gradient.setGradientColor(gradientColor);
        
        switch(orientation){
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
    
    public static BufferedImage createColorScaleBufferedImage(Color[] gradientColor, float minValue, float maxValue, int width, int height, short orientation, int tickNumber){
        
        if(tickNumber < 2){
            tickNumber = 2;
        }
        
        BufferedImage image = generateColorGradientImage(gradientColor, minValue, maxValue, width, height, orientation);
        
        DecimalScientificFormat format = new DecimalScientificFormat();
        
        int borderX = 60;
        int borderY = 30;
        
        /***Génération de l'image avec BufferedImage***/
        BufferedImage imageWithTextcaption = new BufferedImage(image.getWidth()+borderX, image.getHeight()+borderY, image.getType());
        Graphics2D graphics = (Graphics2D)imageWithTextcaption.createGraphics();
        
        int leftXMargin = borderX/2;
        graphics.drawImage(image, leftXMargin, 0, null);
        graphics.setPaint(Color.BLACK);
        
        graphics.setFont(new Font("Comic Sans MS",Font.PLAIN,20));        
        
        FontMetrics fm = graphics.getFontMetrics();
        
        //calcul des valeurs intermédiaires
        float step = (maxValue - minValue)/(float)(tickNumber-1);
        
        float[] tickValues = new float[tickNumber];
        tickValues[0] = minValue;
        tickValues[tickNumber-1] = maxValue;
        
        float currentValue = step;
        for(int i=1;i<tickNumber-1;i++){
            
            tickValues[i] = currentValue;
            currentValue += step;
        }
            
        //création du texte associé aux valeurs des ticks
        int y = imageWithTextcaption.getHeight();
        
        //génération des ticks (sous forme de lignes)
        float majorTickSpace = (image.getWidth())/(float)(tickNumber-1);
        
        float currentTickRectXOffset = 0 + leftXMargin;
        int minorTickNumber = 8;
        
        int majorTickWidth = 1;
        int majorTickHeight = (int) (image.getHeight()*0.75f);
        int majorTickPosY = image.getHeight() - majorTickHeight;
        
        int minorTickWidth = 1;
        int minorTickHeight = (int) (image.getHeight()*0.5f);
        int minorTickPosY = image.getHeight() - minorTickHeight;
        
        float minorTickSpace = (majorTickSpace) / (float)(minorTickNumber+1);
        
        for(int i=0;i<tickNumber;i++){
            
            graphics.fillRect((int)currentTickRectXOffset, majorTickPosY, majorTickWidth, majorTickHeight);
            
            if(i<tickNumber -1){
                
                for(int j=0;j<minorTickNumber;j++){

                    currentTickRectXOffset += minorTickSpace;
                    graphics.fillRect((int)currentTickRectXOffset, minorTickPosY, minorTickWidth, minorTickHeight);
                }

                currentTickRectXOffset += minorTickSpace;
            }
            
        }
        
        //scale border
        graphics.drawRect(leftXMargin, 0, image.getWidth(), image.getHeight());
        
        //scale labels (values)
        currentTickRectXOffset = 0 + leftXMargin;
        for(int i=0;i<tickNumber;i++){
            
            String text = format.format(tickValues[i]);
            int textWidth = fm.stringWidth(text);
            int x = (int) (currentTickRectXOffset - ((int) (textWidth/2.0f)));
            
            graphics.drawString(text, x, y);
            
            currentTickRectXOffset += majorTickSpace;
        }
        
        
        return imageWithTextcaption;
    }
}
