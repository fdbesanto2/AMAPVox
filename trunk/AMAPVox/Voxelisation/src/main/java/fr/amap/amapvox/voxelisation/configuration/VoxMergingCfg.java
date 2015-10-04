
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

package fr.amap.amapvox.voxelisation.configuration;

import fr.amap.amapvox.commons.configuration.Configuration;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author calcul
 */


public class VoxMergingCfg extends Configuration{

    private File outputFile;
    private VoxelParameters voxelParameters;
    private List<File> files;
    
    public VoxMergingCfg(){
        
    }

    public VoxMergingCfg(File outputFile, VoxelParameters voxelParameters, List<File> files) {
        this.outputFile = outputFile;
        this.voxelParameters = voxelParameters;
        this.files = files;
    }
    
    @Override
    public void readConfiguration(File inputParametersFile) throws JDOMException, IOException {
        
        initDocument(inputParametersFile);
        
        processMode = ProcessMode.MERGING;

        outputFile = new File(processElement.getChild("output_file").getAttributeValue("src"));

        Element filesElement = processElement.getChild("files");
        List<Element> childrens = filesElement.getChildren("file");

        files = new ArrayList<>();

        for (Element e : childrens) {
            files.add(new File(e.getAttributeValue("src")));
        }

        voxelParameters = new VoxelParameters();

        Element limitsElement = processElement.getChild("limits");

        if(limitsElement != null){
            Element limitElement = limitsElement.getChild("limit");
            voxelParameters.setMaxPAD(Float.valueOf(limitElement.getAttributeValue("max")));
        }

    }

    @Override
    public void writeConfiguration(File outputParametersFile) throws Exception {
        
        createCommonData();
        
        processElement.setAttribute(new Attribute("mode","merging"));
            
        Element outputFileElement = new Element("output_file");
        outputFileElement.setAttribute(new Attribute("src",outputFile.getAbsolutePath()));
        processElement.addContent(outputFileElement);

        Element limitsElement = new Element("limits");
        Element limitElement = new Element("limit");
        limitElement.setAttribute("name", "PAD");
        limitElement.setAttribute("min", "");
        limitElement.setAttribute("max", String.valueOf(voxelParameters.getMaxPAD()));
        limitsElement.addContent(limitElement);
        processElement.addContent(limitsElement);

        processElement.addContent(createFilesElement(files));
        
        writeDocument(outputParametersFile);
    }

    public File getOutputFile() {
        return outputFile;
    }

    public VoxelParameters getVoxelParameters() {
        return voxelParameters;
    }

    public List<File> getFiles() {
        return files;
    }
}
