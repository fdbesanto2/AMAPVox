/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import java.io.File;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class GlobalPaths {
    
    
    public final static File JAR_FILE = new File(GlobalPaths.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    
    static{
        Logger logger = Logger.getLogger(GlobalPaths.class);
        logger.info("The detected jar file path is "+JAR_FILE.getAbsolutePath());
    }
   
}
