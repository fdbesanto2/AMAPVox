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


public class MultiResCfg extends Configuration{

    private List<File> files;
    private File outputFile;
    private boolean multiResUseDefaultMaxPad;
    private float[] multiResPadMax;
    private VoxelParameters voxelParameters;

    public MultiResCfg() {
        
    }
    
    public MultiResCfg(List<File> files, File outputFile, boolean multiResUseDefaultMaxPad, float[] multiResPadMax, VoxelParameters voxelParameters) {
        
        this.files = files;
        this.outputFile = outputFile;
        this.multiResUseDefaultMaxPad = multiResUseDefaultMaxPad;
        this.multiResPadMax = multiResPadMax;
        this.voxelParameters = voxelParameters;
    }
    
    @Override
    public void readConfiguration(File inputParametersFile) throws JDOMException, IOException {
        
        initDocument(inputParametersFile);
        
        processMode = ProcessMode.MULTI_RES;

        Element filesElement = processElement.getChild("files");

        List<Element> fileElementList = filesElement.getChildren("file");

        files = new ArrayList<>();

        for (Element e : fileElementList) {
            files.add(new File(e.getAttributeValue("src")));
        }

        outputFile = new File(processElement.getChild("output_file").getAttributeValue("src"));

        Element limitsElement = processElement.getChild("limits");

        String tmp = limitsElement.getAttributeValue("use-default");
        if(tmp != null){
            multiResUseDefaultMaxPad = Boolean.valueOf(tmp);
        }

        List<Element> limitElementList = limitsElement.getChildren("limit");

        if (limitElementList != null) {
            multiResPadMax = new float[5];
            multiResPadMax[0] = Float.valueOf(limitElementList.get(0).getAttributeValue("max"));
            multiResPadMax[1] = Float.valueOf(limitElementList.get(1).getAttributeValue("max"));
            multiResPadMax[2] = Float.valueOf(limitElementList.get(2).getAttributeValue("max"));
            multiResPadMax[3] = Float.valueOf(limitElementList.get(3).getAttributeValue("max"));
            multiResPadMax[4] = Float.valueOf(limitElementList.get(4).getAttributeValue("max"));
        }
    }

    @Override
    public void writeConfiguration(File outputParametersFile) throws Exception {
        
        createCommonData();
        
        processElement.setAttribute(new Attribute("mode","multi-resolutions"));
        processElement.setAttribute(new Attribute("type","ALS"));

        /***FILE LIST TO PROCESS***/
        processElement.addContent(createFilesElement(files));

        Element outputFileElement = new Element("output_file");
        outputFileElement.setAttribute(new Attribute("src",outputFile.getAbsolutePath()));
        processElement.addContent(outputFileElement);
            
        Element limitsElement = new Element("limits");
        
        limitsElement.setAttribute("use-default", String.valueOf(multiResUseDefaultMaxPad));

        limitsElement.addContent(createLimitElement("PAD_1m", "", String.valueOf(multiResPadMax[0])));
        limitsElement.addContent(createLimitElement("PAD_2m", "", String.valueOf(multiResPadMax[1])));
        limitsElement.addContent(createLimitElement("PAD_3m", "", String.valueOf(multiResPadMax[2])));
        limitsElement.addContent(createLimitElement("PAD_4m", "", String.valueOf(multiResPadMax[3])));
        limitsElement.addContent(createLimitElement("PAD_5m", "", String.valueOf(multiResPadMax[4])));
        
        Element limitElement = new Element("limit");
        limitElement.setAttribute("name", "PAD");
        limitElement.setAttribute("min", "");
        limitElement.setAttribute("max", String.valueOf(voxelParameters.getMaxPAD()));
        
        limitsElement.addContent(limitElement);

        processElement.addContent(limitsElement);
        
        writeDocument(outputParametersFile);
    }

    public List<File> getFiles() {
        return files;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public boolean isMultiResUseDefaultMaxPad() {
        return multiResUseDefaultMaxPad;
    }

    public float[] getMultiResPadMax() {
        return multiResPadMax;
    }
}
