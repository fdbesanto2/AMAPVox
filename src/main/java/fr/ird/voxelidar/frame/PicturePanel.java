/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.frame;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

/**
 *
 * @author Julien
 */
public class PicturePanel extends JPanel{
    
    public BufferedImage img;
    public int width, height;
    public int posX, posY;
    public float ratio;
    public int originalWidth;
    public int originalHeight;
    
    public PicturePanel(BufferedImage img, int width, int height, int posX, int posY){
        
        this.width = width;
        this.height = height;
        this.originalWidth = width;
        this.originalHeight = height;
        this.img = img;
        this.posX = posX;
        this.posY = posY;
        this.ratio = width/height;
    }
    
    @Override
    public void paintComponent(Graphics g) {
    
        g.drawImage(img, posX, posY, width, height, null);
    }

}
