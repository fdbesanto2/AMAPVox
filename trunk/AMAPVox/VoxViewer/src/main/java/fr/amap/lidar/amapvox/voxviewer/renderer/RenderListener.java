/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.renderer;

import java.awt.image.BufferedImage;
import java.util.EventListener;

/**
 *
 * @author Julien Heurtebize
 */
public interface RenderListener extends EventListener{
    
    public void screenshotIsReady(BufferedImage image);
}
