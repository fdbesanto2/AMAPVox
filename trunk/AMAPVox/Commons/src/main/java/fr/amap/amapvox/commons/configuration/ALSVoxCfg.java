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

package fr.amap.amapvox.commons.configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author calcul
 */


public class ALSVoxCfg extends VoxCfg{

    private File trajectoryFile;
    private List<Integer> classifiedPointsToDiscard;
    
    @Override
    public void readConfiguration(File inputParametersFile) throws Exception {
        
        super.readConfiguration(inputParametersFile);
        
        Element trajectoryFileElement = processElement.getChild("trajectory");
        trajectoryFile = new File(trajectoryFileElement.getAttributeValue("src"));
        
        processMode = ProcessMode.VOXELISATION_ALS;
        
        Element groundEnergyElement = processElement.getChild("ground-energy");
        if(groundEnergyElement != null){
            voxelParameters.setCalculateGroundEnergy(Boolean.valueOf(groundEnergyElement.getAttributeValue("generate")));

            if(voxelParameters.isCalculateGroundEnergy()){
                voxelParameters.setGroundEnergyFileFormat(Short.valueOf(groundEnergyElement.getAttributeValue("type")));
                voxelParameters.setGroundEnergyFile(new File(groundEnergyElement.getAttributeValue("src")));
            }
        }
        
        classifiedPointsToDiscard = new ArrayList<>();

        Element pointFiltersElement = filtersElement.getChild("point-filters");
        if(pointFiltersElement != null){

            String classifications = pointFiltersElement.getAttributeValue("classifications");

            if(classifications !=null && !classifications.isEmpty()){

                String[] classificationsArray = classifications.split(" ");

                for(String s : classificationsArray){
                    classifiedPointsToDiscard.add(Integer.valueOf(s));
                }
            }
        }
    }

    @Override
    public void writeConfiguration(File outputParametersFile) throws Exception {
        
        createCommonData();
        
        super.writeConfiguration(outputParametersFile);
        
        processElement.setAttribute(new Attribute("mode","voxelisation"));
        processElement.setAttribute(new Attribute("type","ALS"));
        
        Element trajectoryFileElement = new Element("trajectory");
        trajectoryFileElement.setAttribute(new Attribute("src",trajectoryFile.getAbsolutePath()));
        processElement.addContent(trajectoryFileElement);
        
        Element groundEnergyElement = new Element("ground-energy");
        groundEnergyElement.setAttribute("generate", String.valueOf(voxelParameters.isCalculateGroundEnergy()));

        if(voxelParameters.getGroundEnergyFile() != null){
            groundEnergyElement.setAttribute("src", voxelParameters.getGroundEnergyFile().getAbsolutePath());
            groundEnergyElement.setAttribute("type", String.valueOf(voxelParameters.getGroundEnergyFileFormat()));
        }    
        
        if(classifiedPointsToDiscard != null){
            
            Element pointsFilterElement = new Element("point-filters");

            String classifiedPointsToDiscardString = "";
            for(Integer i : classifiedPointsToDiscard){
                classifiedPointsToDiscardString += i+" ";
            }

            pointsFilterElement.setAttribute("classifications", classifiedPointsToDiscardString);

            //pointsFilterElement.addContent(new Element("low-point-filter").setAttribute("enabled", String.valueOf(removeLowPoint)));
            filtersElement.addContent(pointsFilterElement);
        }

        processElement.addContent(groundEnergyElement);
        
        writeDocument(outputParametersFile);
    }
    
    public File getTrajectoryFile() {
        return trajectoryFile;
    }

    public void setTrajectoryFile(File trajectoryFile) {
        this.trajectoryFile = trajectoryFile;
    }
    
    public List<Integer> getClassifiedPointsToDiscard() {
        return classifiedPointsToDiscard;
    }

    public void setClassifiedPointsToDiscard(List<Integer> classifiedPointsToDiscard) {
        this.classifiedPointsToDiscard = classifiedPointsToDiscard;
    }
}
