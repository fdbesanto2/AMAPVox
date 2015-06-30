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

import java.io.File;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author calcul
 */


public class ALSVoxCfg extends VoxCfg{

    private File trajectoryFile;
    
    @Override
    public void readConfiguration(File inputParametersFile) {
        
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
    }

    @Override
    public void writeConfiguration(File outputParametersFile) {
        
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

        processElement.addContent(groundEnergyElement);
        
        writeDocument(outputParametersFile);
    }
    
    public File getTrajectoryFile() {
        return trajectoryFile;
    }

    public void setTrajectoryFile(File trajectoryFile) {
        this.trajectoryFile = trajectoryFile;
    }
}
