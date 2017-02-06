/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class Global {
    
    
    public final static File JAR_FILE = new File(Global.class.getProtectionDomain().getCodeSource().getLocation().getFile());
    public static Manifest manifest;
    public static String buildVersion;
    
    static{
        
        Logger logger = Logger.getLogger(Global.class);
        logger.info("The detected jar file path is "+JAR_FILE.getAbsolutePath());
            
        try
        {
            
            Class clazz = Global.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attributes= manifest.getMainAttributes();
            buildVersion = attributes.getValue("Implementation-Build");            
            
            manifest = new Manifest(new URL(manifestPath).openStream());
            
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
}
