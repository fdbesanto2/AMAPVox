/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.ird.voxelidar.configuration;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import javax.vecmath.Matrix4d;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author calcul
 */


public abstract class Configuration {
    
    protected final static Logger logger = Logger.getLogger(Configuration.class);
    
    protected ProcessMode processMode;
    protected InputType inputType = InputType.LAS_FILE;
    
    protected Element racine;
    protected Document document;
    protected Element processElement;
    
    public enum ProcessMode{
        
        VOXELISATION_ALS(0),
        VOXELISATION_TLS(1),
        MERGING(2),
        MULTI_VOXELISATION_ALS_AND_MULTI_RES(3),
        MULTI_RES(4),
        ;
        
        public final int mode;
        
        private ProcessMode(int mode){
            this.mode = mode;
        }
    }
    
    public enum InputType{
        
        LAS_FILE(0),
        LAZ_FILE(1),
        POINTS_FILE(2),
        SHOTS_FILE(3),
        RXP_SCAN(4),
        RSP_PROJECT(5),
        VOXEL_FILE(6);
        
        public int type;
        
        private InputType(int type){
            this.type = type;
        }
    }
    
    public abstract void readConfiguration(File inputParametersFile);
    public abstract void writeConfiguration(File outputParametersFile);
    
    public static String readType(File inputParametersFile){
        
        try {
            SAXBuilder sxb = new SAXBuilder();
            Document document = sxb.build(inputParametersFile);
            Element root = document.getRootElement();
            
            Element processElement = root.getChild("process");
            String mode = processElement.getAttributeValue("mode");
            String type = processElement.getAttributeValue("type");
            
            if(mode.equals("voxelisation")){
                mode += "-"+type;
            }
            
            return mode;
            
        } catch (JDOMException | IOException ex) {
            logger.error(ex);
        }
        
        return null;
    }
    
    protected void createCommonData(){
        
        racine = new Element("configuration");
        racine.setAttribute("creation-date", new Date().toString());
        
        try {
            Class clazz = Configuration.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attributes= manifest.getMainAttributes();
            String buildVersion = attributes.getValue("Implementation-Build");
            
            if(buildVersion != null){
                racine.setAttribute("build-version", buildVersion);
            }else{
                logger.error("Cannot get Implementation-Build property in manifest file");
            }
        } catch (Exception ex) {
            logger.error("Cannot get manifest file: ",ex);
        }
        
        document = new Document(racine);
        
        processElement = new Element("process");
        racine.addContent(processElement);
    }
    
    protected void initDocument(File inputFile){
        
        SAXBuilder sxb = new SAXBuilder();
        
        try {
            document = sxb.build(inputFile);
            
            Element root = document.getRootElement();

            processElement = root.getChild("process");
            String mode = processElement.getAttributeValue("mode");
            String type = processElement.getAttributeValue("type");
        
        } catch (JDOMException | IOException ex) {
            logger.error(ex);
        }
    }
    
    protected void writeDocument(File outputFile){
        
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        try {
            output.output(document, new BufferedOutputStream(new FileOutputStream(outputFile)));
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
    
    protected Element createFilesElement(List<File> files){
        
        Element filesElement = new Element("files");
            
        for(File f : files){
            filesElement.addContent(new Element("file").setAttribute("src", f.getAbsolutePath()));
        }
        
        return filesElement;
    }
    
    protected Element createLimitElement(String name, String min, String max){
        
        Element limitElement = new Element("limit");
        limitElement.setAttribute("name", name);
        limitElement.setAttribute("min", min);
        limitElement.setAttribute("max", max);
        
        return limitElement;
    }
    
    protected InputType getInputFileType(int type){
        
        switch(type){
            case 0:
                return InputType.LAS_FILE;
            case 1:
                return InputType.LAZ_FILE;
            case 2:
                return InputType.POINTS_FILE;
            case 3:
                return InputType.LAS_FILE;
            case 4:
                return InputType.RXP_SCAN;
            case 5:
                return InputType.RSP_PROJECT;
        }
        
        return null;
    }
    
    protected Matrix4d getMatrixFromData(String data){
        
        data = data.replaceAll("\n", ",");
        data = data.replaceAll(" ", "");
        String[] datas = data.split(",");
        
        Matrix4d mat = new Matrix4d();
        int i = 0;
        int j = 0;
        for(int k=0;k<datas.length;k++){

            mat.setElement(j, i, Double.valueOf(datas[k]));
            if(i%3 == 0 && i!=0){
                j++;
                i = 0; 
            }else{
                i++;
            }
        }
        
        return mat;
    }
    
    protected Element createMatrixElement(String id, String data){
        
        Element matrixElement = new Element("matrix");
        matrixElement.setAttribute("type_id", id);
        matrixElement.setText(data);
        
        return matrixElement;
    }
}
