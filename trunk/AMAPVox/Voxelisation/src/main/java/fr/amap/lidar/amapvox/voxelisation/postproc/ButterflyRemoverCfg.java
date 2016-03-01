/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.postproc;

import fr.amap.lidar.amapvox.commons.Configuration;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author calcul
 */
public class ButterflyRemoverCfg extends Configuration{

    private File inputFile;
    private File outputFile;
            
    @Override
    public void readConfiguration(File inputParametersFile) throws Exception {
        
        initDocument(inputParametersFile);
        
        processMode = ProcessMode.BUTTERFLY_REMOVING;

        inputFile = new File(processElement.getChild("input_file").getAttributeValue("src"));
        outputFile = new File(processElement.getChild("output_file").getAttributeValue("src"));
    }

    @Override
    public void writeConfiguration(File outputParametersFile) throws Exception {
        
        createCommonData();
        
        processElement.setAttribute(new Attribute("mode","butterfly-removing"));
            
        Element outputFileElement = new Element("output_file");
        outputFileElement.setAttribute(new Attribute("src",outputFile.getAbsolutePath()));
        processElement.addContent(outputFileElement);

        Element inputFileElement = new Element("input_file");
        inputFileElement.setAttribute(new Attribute("src",inputFile.getAbsolutePath()));
        processElement.addContent(inputFileElement);
        
        writeDocument(outputParametersFile);
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }
    
}
