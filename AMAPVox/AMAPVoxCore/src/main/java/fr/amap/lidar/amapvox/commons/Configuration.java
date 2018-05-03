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

package fr.amap.lidar.amapvox.commons;

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
    
    private final static Logger LOGGER = Logger.getLogger(Configuration.class);
    
    protected ProcessMode processMode;
    protected InputType inputType = InputType.LAS_FILE;
    
    protected Element racine;
    protected Document document;
    protected Element processElement;
    protected String processModeValue;
    protected String processTypeValue;
    
    public enum ProcessMode{
        
        VOXELISATION_ALS(0),
        VOXELISATION_TLS(1),
        MERGING(2),
        MULTI_VOXELISATION_ALS_AND_MULTI_RES(3),
        MULTI_RES(4),
        BUTTERFLY_REMOVING(5)
        ;
        
        public final int mode;
        
        private ProcessMode(int mode){
            this.mode = mode;
        }
    }
    
    public enum InputType{
        
        LAS_FILE(0),
        LAZ_FILE(1),
        SHOTS_FILE(2),
        POINTS_FILE(3),
        RXP_SCAN(4),
        RSP_PROJECT(5),
        VOXEL_FILE(6),
        PTX_PROJECT(7),
        PTG_PROJECT(8);
        
        public int type;
        
        private InputType(int type){
            this.type = type;
        }
    }
    
    public abstract void readConfiguration(File inputParametersFile) throws Exception;
    public abstract void writeConfiguration(File outputParametersFile, String buildVersion) throws Exception;
    
    public static String readType(File inputParametersFile) throws JDOMException, IOException{
        
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
            throw ex;
        }
    }
    
    protected void createCommonData(String buildVersion) throws Exception{
        
        racine = new Element("configuration");
        racine.setAttribute("creation-date", new Date().toString());
        
        if(buildVersion != null){
            racine.setAttribute("build-version", buildVersion);
        }else{
            throw new Exception("Cannot get Implementation-Build property in manifest file");
        }
        
        document = new Document(racine);
        
        processElement = new Element("process");
        racine.addContent(processElement);
    }
    
    protected void initDocument(File inputFile) throws JDOMException, IOException{
        
        SAXBuilder sxb = new SAXBuilder();
        
        try {
            document = sxb.build(inputFile);
            
            Element root = document.getRootElement();

            processElement = root.getChild("process");
            processModeValue = processElement.getAttributeValue("mode");
            processTypeValue = processElement.getAttributeValue("type");
        
        } catch (JDOMException | IOException ex) {
            throw ex;
        }
    }
    
    protected void writeDocument(File outputFile) throws IOException{
        
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        try {
            output.output(document, new BufferedOutputStream(new FileOutputStream(outputFile)));
        } catch (IOException ex) {
            throw ex;
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
                return InputType.SHOTS_FILE;
            case 3:
                return InputType.POINTS_FILE;
            case 4:
                return InputType.RXP_SCAN;
            case 5:
                return InputType.RSP_PROJECT;
            case 7:
                return InputType.PTX_PROJECT;
            case 8:
                return InputType.PTG_PROJECT;
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
