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

package fr.amap.lidar.amapvox.voxelisation.configuration;

import fr.amap.commons.util.filter.Filter;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.lidar.amapvox.shot.filter.ClassifiedPointFilter;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.GroundEnergyParams;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author calcul
 */


public class ALSVoxCfg extends VoxelAnalysisCfg{

    private CSVFile trajectoryFile;
    
    @Override
    public void readConfiguration(File inputParametersFile) throws Exception {
        
        super.readConfiguration(inputParametersFile);
        
        if(inputType == InputType.LAS_FILE || inputType == InputType.LAZ_FILE){
            
            Element trajectoryFileElement = processElement.getChild("trajectory");
        
            trajectoryFile = new CSVFile(trajectoryFileElement.getAttributeValue("src"));

            String columnSeparator = trajectoryFileElement.getAttributeValue("column-separator");
            String headerIndex = trajectoryFileElement.getAttributeValue("header-index");
            String hasHeader = trajectoryFileElement.getAttributeValue("has-header");
            String nbOfLinesToRead = trajectoryFileElement.getAttributeValue("nb-of-lines-to-read");
            String nbOfLinesToSkip = trajectoryFileElement.getAttributeValue("nb-of-lines-to-skip");
            String columnAssignment = trajectoryFileElement.getAttributeValue("column-assignment");

            if (columnSeparator == null) {
                LOGGER.warn("Old trajectory file element detected, keep default old read parameters.");
            } else {

                try {

                    trajectoryFile.setColumnSeparator(columnSeparator);
                    trajectoryFile.setHeaderIndex(Long.valueOf(headerIndex));
                    trajectoryFile.setContainsHeader(Boolean.valueOf(hasHeader));
                    trajectoryFile.setNbOfLinesToRead(Long.valueOf(nbOfLinesToRead));
                    trajectoryFile.setNbOfLinesToSkip(Long.valueOf(nbOfLinesToSkip));

                    Map<String, Integer> colMap = new HashMap<>();
                    String[] split = columnAssignment.split(",");
                    for (String s : split) {
                        int indexOfSep = s.indexOf("=");
                        String key = s.substring(0, indexOfSep);
                        String value = s.substring(indexOfSep + 1, s.length());
                        colMap.put(key, Integer.valueOf(value));
                    }

                    trajectoryFile.setColumnAssignment(colMap);
                } catch (Exception e) {
                    LOGGER.warn("Old trajectory file element detected, keep default old read parameters.");
                }
            }
        }
        
        
        processMode = ProcessMode.VOXELISATION_ALS;
        
        Element groundEnergyElement = processElement.getChild("ground-energy");
        if(groundEnergyElement != null){
            
            GroundEnergyParams groundEnergyParameters = new GroundEnergyParams();
            
            groundEnergyParameters.setCalculateGroundEnergy(Boolean.valueOf(groundEnergyElement.getAttributeValue("generate")));

            if(groundEnergyParameters.isCalculateGroundEnergy()){
                groundEnergyParameters.setGroundEnergyFileFormat(Short.valueOf(groundEnergyElement.getAttributeValue("type")));
                groundEnergyParameters.setGroundEnergyFile(new File(groundEnergyElement.getAttributeValue("src")));
            }
            
            voxelParameters.setGroundEnergyParams(groundEnergyParameters);
        }
        
        // echo classification filter
        Element pointFiltersElement = filtersElement.getChild("point-filters");
        if(pointFiltersElement != null){
            String classifications = pointFiltersElement.getAttributeValue("classifications");
            if(classifications !=null && !classifications.isEmpty()){
                List<Integer> classifiedPointsToDiscard = new ArrayList();
                String[] classificationsArray = classifications.split(" ");
                for(String s : classificationsArray){
                    classifiedPointsToDiscard.add(Integer.valueOf(s));
                }
                echoFilters.add(new ClassifiedPointFilter(classifiedPointsToDiscard));
            }
        }
        
        // correct NaNs values        
        Element correctNaNsElement = processElement.getChild("correct-NaNs");
        if(correctNaNsElement != null){
            voxelParameters.getNaNsCorrectionParams().setActivate(Boolean.valueOf(correctNaNsElement.getAttributeValue("enabled")));
            try{
                voxelParameters.getNaNsCorrectionParams().setNbSamplingThreshold(Float.valueOf(correctNaNsElement.getAttributeValue("threshold")));
            }catch(Exception e){ 
                System.err.println(e.fillInStackTrace());
            }
            
        }
    }

    @Override
    public void writeConfiguration(File outputParametersFile, String buildVersion) throws Exception {
        
        createCommonData(buildVersion);
        
        super.writeConfiguration(outputParametersFile, buildVersion);
        
        processElement.setAttribute(new Attribute("mode","voxelisation"));
        
        processElement.setAttribute(new Attribute("type","ALS"));
        
        if(inputType == InputType.LAS_FILE || inputType == InputType.LAZ_FILE){
            
           
            Element trajectoryFileElement = new Element("trajectory");
            trajectoryFileElement.setAttribute(new Attribute("src",trajectoryFile.getAbsolutePath()));
            trajectoryFileElement.setAttribute(new Attribute("column-separator",trajectoryFile.getColumnSeparator()));
            trajectoryFileElement.setAttribute(new Attribute("header-index",String.valueOf(trajectoryFile.getHeaderIndex())));
            trajectoryFileElement.setAttribute(new Attribute("has-header", String.valueOf(trajectoryFile.containsHeader())));
            trajectoryFileElement.setAttribute(new Attribute("nb-of-lines-to-read", String.valueOf(trajectoryFile.getNbOfLinesToRead())));
            trajectoryFileElement.setAttribute(new Attribute("nb-of-lines-to-skip", String.valueOf(trajectoryFile.getNbOfLinesToSkip())));

            Map<String, Integer> columnAssignment = trajectoryFile.getColumnAssignment();
            Iterator<Map.Entry<String, Integer>> iterator = columnAssignment.entrySet().iterator();
            String colAssignment = new String();

            while(iterator.hasNext()){
                Map.Entry<String, Integer> entry = iterator.next();
                colAssignment += entry.getKey()+"="+entry.getValue()+",";
            }

            trajectoryFileElement.setAttribute(new Attribute("column-assignment", colAssignment));

            processElement.addContent(trajectoryFileElement);

        }
        
        GroundEnergyParams groundEnergyParameters = voxelParameters.getGroundEnergyParams();
        
        if(groundEnergyParameters != null){
            
            Element groundEnergyElement = new Element("ground-energy");
            groundEnergyElement.setAttribute("generate", String.valueOf(groundEnergyParameters.isCalculateGroundEnergy()));
            
            if(groundEnergyParameters.getGroundEnergyFile() != null){
                groundEnergyElement.setAttribute("src", groundEnergyParameters.getGroundEnergyFile().getAbsolutePath());
                groundEnergyElement.setAttribute("type", String.valueOf(groundEnergyParameters.getGroundEnergyFileFormat()));
            }    

            processElement.addContent(groundEnergyElement);
        }
        
        // look for classification point filter 
        for (Filter filter : getEchoFilters()) {
            if (filter instanceof ClassifiedPointFilter) {
                Element pointsFilterElement = new Element("point-filters");
                String classifiedPointsToDiscardString = "";
                List<Integer> classifiedPointsToDiscard = ((ClassifiedPointFilter) filter).getClasses();
                for (Integer i : classifiedPointsToDiscard) {
                    classifiedPointsToDiscardString += i + " ";
                }
                pointsFilterElement.setAttribute("classifications", classifiedPointsToDiscardString.trim());
                filtersElement.addContent(pointsFilterElement);
            }
        }
        
        Element correctNaNsElement = new Element("correct-NaNs");
        correctNaNsElement.setAttribute("enabled", String.valueOf(voxelParameters.getNaNsCorrectionParams().isActivate()));
        correctNaNsElement.setAttribute("threshold", String.valueOf(voxelParameters.getNaNsCorrectionParams().getNbSamplingThreshold()));
        processElement.addContent(correctNaNsElement);
        
        
        writeDocument(outputParametersFile);
    }
    
    public CSVFile getTrajectoryFile() {
        return trajectoryFile;
    }

    public void setTrajectoryFile(CSVFile trajectoryFile) {
        this.trajectoryFile = trajectoryFile;
    }
}
