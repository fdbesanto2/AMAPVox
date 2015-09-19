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

package fr.amap.amapvox.simulation.transmittance;

import fr.amap.amapvox.commons.configuration.Configuration;
import fr.amap.amapvox.simulation.transmittance.util.Period;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.vecmath.Point3f;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author calcul
 */


public class VirtualMeasuresCfg extends Configuration{

    private Parameters parameters;
    
    private VirtualMeasuresCfg(){
        
        parameters = new Parameters();        
    }
    
    public static VirtualMeasuresCfg readCfg(File file) throws IOException, JDOMException{
        
        VirtualMeasuresCfg cfg = new VirtualMeasuresCfg(new Parameters());
        cfg.readConfiguration(file);
        return cfg;
    }
    
    public VirtualMeasuresCfg(Parameters parameters){
        this.parameters = parameters;
    }
    
    @Override
    public void readConfiguration(File inputParametersFile) throws JDOMException, IOException {
        
        initDocument(inputParametersFile);
        
        switch(processModeValue){
            case "transmittance":
                parameters.setMode(Parameters.Mode.TRANSMITTANCE);
                break;
            case "LAI2000":
                parameters.setMode(Parameters.Mode.LAI2000);
                break;
            case "LAI2200":
                parameters.setMode(Parameters.Mode.LAI2200);
                break;
        }
        
        
        Element inputFileElement = processElement.getChild("input_file");
        String inputFileSrc = inputFileElement.getAttributeValue("src");

        if(inputFileSrc != null){
            parameters.setInputFile(new File(inputFileSrc));
        }

        Element outputFilesElement = processElement.getChild("output_files");
        Element outputTextFileElement = outputFilesElement.getChild("output_text_file");

        if(outputTextFileElement != null){
            boolean generateOutputTextFile = Boolean.valueOf(outputTextFileElement.getAttributeValue("generate"));
            parameters.setGenerateTextFile(generateOutputTextFile);

            if(generateOutputTextFile){

                String outputTextFileSrc = outputTextFileElement.getAttributeValue("src");
                if(outputTextFileSrc != null){
                    parameters.setTextFile(new File(outputTextFileSrc));
                }

            }
        }

        Element outputBitmapFileElement = outputFilesElement.getChild("output_bitmap_file");

        if(outputBitmapFileElement != null){
            boolean generateOutputBitmapFile = Boolean.valueOf(outputBitmapFileElement.getAttributeValue("generate"));
            parameters.setGenerateBitmapFile(generateOutputBitmapFile);

            if(generateOutputBitmapFile){

                String outputBitmapFileSrc = outputBitmapFileElement.getAttributeValue("src");
                if(outputBitmapFileSrc != null){
                    parameters.setBitmapFile(new File(outputBitmapFileSrc));
                }

            }
        }

        Element directionsNumberElement = processElement.getChild("directions-number");
        if(directionsNumberElement != null){
            String directionsNumberValue = directionsNumberElement.getAttributeValue("value");
            if(directionsNumberValue != null){
                parameters.setDirectionsNumber(Integer.valueOf(directionsNumberValue));
            }
        }

        Element scannerPositionsElement = processElement.getChild("scanners-positions");

        if(scannerPositionsElement != null){

            String scannerPositionType = scannerPositionsElement.getAttributeValue("type");

            if(scannerPositionType != null){

                switch(scannerPositionType){

                    case "squared-area":

                        parameters.setUseScanPositionsFile(false);

                        String centerPositionX = scannerPositionsElement.getAttributeValue("centerX");
                        String centerPositionY = scannerPositionsElement.getAttributeValue("centerY");
                        String centerPositionZ = scannerPositionsElement.getAttributeValue("centerZ");

                        if(centerPositionX != null && centerPositionY != null && centerPositionZ != null){
                            parameters.setCenterPoint(new Point3f(Float.valueOf(centerPositionX), 
                                                                  Float.valueOf(centerPositionY), 
                                                                  Float.valueOf(centerPositionZ)));
                        }

                        String width = scannerPositionsElement.getAttributeValue("width");
                        if(width != null){
                            parameters.setWidth(Float.valueOf(width));
                        }

                        String step = scannerPositionsElement.getAttributeValue("step");
                        if(step != null){
                            parameters.setStep(Float.valueOf(step));
                        }

                        break;
                    case "file":

                        parameters.setUseScanPositionsFile(true);

                        String scanPositionsFileSrc = scannerPositionsElement.getAttributeValue("src");
                        if(scanPositionsFileSrc != null){
                            parameters.setPointsPositionsFile(new File(scanPositionsFileSrc));
                        }
                        break;
                }
            }

        }

        String latitude = processElement.getChild("latitude").getAttributeValue("value");
        if(latitude != null){
            parameters.setLatitudeInDegrees(Float.valueOf(latitude));
        }

        Element simulationPeriodsElement = processElement.getChild("simulation-periods");
        if(simulationPeriodsElement != null){
            List<Element> childrens = simulationPeriodsElement.getChildren("period");

            if(childrens != null){

                List<SimulationPeriod> simulationPeriods = new ArrayList<>();

                for(Element element : childrens){

                    String startingDateStr = element.getAttributeValue("from");
                    String endingDateStr = element.getAttributeValue("to");
                    String clearness = element.getAttributeValue("clearness");

                    Period period = new Period();
                    SimpleDateFormat simpleDateFormat  = new SimpleDateFormat("dd/MM/yy HH:mm");
                    try {
                        Date startingDate = simpleDateFormat.parse(startingDateStr);
                        Date endingDate = simpleDateFormat.parse(endingDateStr);

                        Calendar c1 = Calendar.getInstance();
                        c1.setTime(startingDate);

                        Calendar c2 = Calendar.getInstance();
                        c2.setTime(endingDate);

                        period.startDate = c1;
                        period.endDate = c2;

                        SimulationPeriod simulationPeriod = new SimulationPeriod(period, Float.valueOf(clearness));
                        simulationPeriods.add(simulationPeriod);

                    } catch (ParseException ex) {}



                }

                parameters.setSimulationPeriods(simulationPeriods);
            }
        }
    }

    @Override
    public void writeConfiguration(File outputParametersFile) throws Exception {
        
        createCommonData();
        
        switch(parameters.getMode()){
            case TRANSMITTANCE:
                processElement.setAttribute(new Attribute("mode","transmittance"));
                break;
            case LAI2000:
                processElement.setAttribute(new Attribute("mode","LAI2000"));
                break;
            case LAI2200:
                processElement.setAttribute(new Attribute("mode","LAI2200"));
                break;
        }
        
        //input
        Element inputFileElement = new Element("input_file");
        inputFileElement.setAttribute(new Attribute("type", String.valueOf(InputType.VOXEL_FILE)));
        inputFileElement.setAttribute(new Attribute("src",parameters.getInputFile().getAbsolutePath()));
        processElement.addContent(inputFileElement);
        
        //outputs
        Element outputFilesElement = new Element("output_files");
        Element outputTextFileElement = new Element("output_text_file");
        outputTextFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateTextFile()));
        
        if(parameters.isGenerateTextFile() && parameters.getTextFile() != null){
            outputTextFileElement.setAttribute("src", parameters.getTextFile().getAbsolutePath());
        }
        
        outputFilesElement.addContent(outputTextFileElement);
        
        Element outputBitmapFileElement = new Element("output_bitmap_file");
        outputBitmapFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateBitmapFile()));
        
        if(parameters.isGenerateBitmapFile()&& parameters.getBitmapFile()!= null){
            outputBitmapFileElement.setAttribute("src", parameters.getBitmapFile().getAbsolutePath());
        }
        
        outputFilesElement.addContent(outputBitmapFileElement);
        
        processElement.addContent(outputFilesElement);
        
        //directions
        processElement.addContent(new Element("directions-number").setAttribute("value", String.valueOf(parameters.getDirectionsNumber())));
        
        //scanners positions
        Element scannersPositionsElement = new Element("scanners-positions");
        
        if(parameters.isUseScanPositionsFile()){
            scannersPositionsElement.setAttribute("type", "file");
            
            if(parameters.getPointsPositionsFile() != null){
                scannersPositionsElement.setAttribute("src", parameters.getPointsPositionsFile().getAbsolutePath());
            }
            
        }else{
            scannersPositionsElement.setAttribute("type", "squared-area");
            
            if(parameters.getCenterPoint() != null){
                scannersPositionsElement.setAttribute("centerX", String.valueOf(parameters.getCenterPoint().x));
                scannersPositionsElement.setAttribute("centerY", String.valueOf(parameters.getCenterPoint().y));
                scannersPositionsElement.setAttribute("centerZ", String.valueOf(parameters.getCenterPoint().z));
            }
            
            scannersPositionsElement.setAttribute("width", String.valueOf(parameters.getWidth()));
            scannersPositionsElement.setAttribute("step", String.valueOf(parameters.getStep()));
        }
                
        processElement.addContent(scannersPositionsElement);
        
        //latitude (radians)
        Element latitudeElement = new Element("latitude").setAttribute("value", String.valueOf(parameters.getLatitudeInDegrees()));
        processElement.addContent(latitudeElement);
        
        //simulation periods
        Element simulationPeriodsElement = new Element("simulation-periods");
        
        List<SimulationPeriod> simulationPeriods = parameters.getSimulationPeriods();
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        
        for(SimulationPeriod period  : simulationPeriods){
            Element periodElement = new Element("period");
            periodElement.setAttribute("from", dateFormat.format(period.getPeriod().startDate.getTime()));
            periodElement.setAttribute("to", dateFormat.format(period.getPeriod().endDate.getTime()));
            periodElement.setAttribute("clearness", String.valueOf(period.getClearnessCoefficient()));
            simulationPeriodsElement.addContent(periodElement);
        }
        
        processElement.addContent(simulationPeriodsElement);
        
        writeDocument(outputParametersFile);
    }

    public Parameters getParameters() {
        return parameters;
    }    
}
