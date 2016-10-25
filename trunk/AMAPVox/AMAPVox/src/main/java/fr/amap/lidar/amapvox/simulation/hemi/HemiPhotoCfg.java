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

package fr.amap.lidar.amapvox.simulation.hemi;

import fr.amap.lidar.amapvox.commons.Configuration;
import fr.amap.commons.util.LidarScan;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.jdom2.Attribute;
import org.jdom2.Element;
import org.jdom2.JDOMException;

/**
 *
 * @author calcul
 */


public class HemiPhotoCfg extends Configuration{

    private HemiParameters parameters;
    
    private HemiPhotoCfg(){
        
        parameters = new HemiParameters();        
    }
    
    public static HemiPhotoCfg readCfg(File file) throws IOException, JDOMException{
        
        HemiPhotoCfg cfg = new HemiPhotoCfg(new HemiParameters());
        cfg.readConfiguration(file);
        return cfg;
    }
    
    public HemiPhotoCfg(HemiParameters parameters){
        this.parameters = parameters;
    }
    
    @Override
    public void readConfiguration(File inputParametersFile) throws JDOMException, IOException {
        
        initDocument(inputParametersFile);
        
        switch(processTypeValue){
            case "0": //ECHOS
                parameters.setMode(HemiParameters.Mode.ECHOS);
                break;
            case "1": //PAD
                parameters.setMode(HemiParameters.Mode.PAD);
                break;
        }
        
        if(parameters.getMode() == HemiParameters.Mode.ECHOS){
            
            Element inputFilesElement = processElement.getChild("input_files");
            List<Element> childrens = inputFilesElement.getChildren("scan");
            
            List<LidarScan> scans = new ArrayList<>(childrens.size());
            
            for(Element children : childrens){
                
                Element inputFileElement = children.getChild("input_file");
                
                String inputFileSrc = inputFileElement.getAttributeValue("src");
                Element sopMatrixElement = children.getChild("SOP");
                
                Matrix4d sopMatrix = new Matrix4d();
                String[] matrixLines = sopMatrixElement.getText().split("\n");
                
                for(int i = 0;i<matrixLines.length;i++){
                    String[] matrixColumns = matrixLines[i].split(", ");
                    
                    for(int j = 0;j<matrixColumns.length;j++){
                        sopMatrix.setElement(i, j, Double.valueOf(matrixColumns[j]));
                    }
                    
                }
                
                if(inputFileSrc != null){
                    File f = new File(inputFileSrc);
                    scans.add(new LidarScan(f, sopMatrix, f.getName()));
                }
            }
            
            parameters.setRxpScansList(scans);
            
            
            
        }else if(parameters.getMode() == HemiParameters.Mode.PAD){
            
            Element inputFileElement = processElement.getChild("input_file");
            String inputFileSrc = inputFileElement.getAttributeValue("src");

            if(inputFileSrc != null){
                parameters.setVoxelFile(new File(inputFileSrc));
            }
            
            
            List<Point3d> positions = new ArrayList<>();
            
            Element sensorPositionElement = processElement.getChild("sensor-position");
            
            if(sensorPositionElement != null){ //work around for old config
                
                positions.add(new Point3d(Double.valueOf(sensorPositionElement.getAttributeValue("x")),
                Double.valueOf(sensorPositionElement.getAttributeValue("y")),
                Double.valueOf(sensorPositionElement.getAttributeValue("z"))));
                
                parameters.setSensorPositions(positions);
                
            }else{
                
                Element sensorPositionsElement = processElement.getChild("sensor-positions");
                List<Element> children = sensorPositionsElement.getChildren("position");
                for(Element element : children){
                    
                    positions.add(new Point3d(
                            Double.valueOf(element.getAttributeValue("x")),
                            Double.valueOf(element.getAttributeValue("y")),
                            Double.valueOf(element.getAttributeValue("z"))));
                }
                
                parameters.setSensorPositions(positions);
            }
            
            
        }
        
        
        //common parameters
        Element pixelNumberElement = processElement.getChild("pixel-number");
        parameters.setPixelNumber(Integer.valueOf(pixelNumberElement.getAttributeValue("value")));
        
        Element azimutsNumberElement = processElement.getChild("azimut-number");
        parameters.setAzimutsNumber(Integer.valueOf(azimutsNumberElement.getAttributeValue("value")));
        
        Element zenithNumberElement = processElement.getChild("zenith-number");
        parameters.setZenithsNumber(Integer.valueOf(zenithNumberElement.getAttributeValue("value")));
        
        //outputs
        
        Element outputFilesElement = processElement.getChild("output_files");
        Element outputTextFileElement = outputFilesElement.getChild("output_text_file");

        if(outputTextFileElement != null){
            boolean generateOutputTextFile = Boolean.valueOf(outputTextFileElement.getAttributeValue("generate"));
            parameters.setGenerateTextFile(generateOutputTextFile);

            if(generateOutputTextFile){

                String outputTextFileSrc = outputTextFileElement.getAttributeValue("src");
                if(outputTextFileSrc != null){
                    parameters.setOutputTextFile(new File(outputTextFileSrc));
                }

            }
        }

        Element outputBitmapFileElement = outputFilesElement.getChild("output_bitmap_file");

        if(outputBitmapFileElement != null){
            boolean generateOutputBitmapFile = Boolean.valueOf(outputBitmapFileElement.getAttributeValue("generate"));
            parameters.setGenerateBitmapFile(generateOutputBitmapFile);

            if(generateOutputBitmapFile){

                String outputBitmapFileSrc = outputBitmapFileElement.getAttributeValue("src");
                int bitmapMode = Integer.valueOf(outputBitmapFileElement.getAttributeValue("mode"));
                
                switch(bitmapMode){
                    case 0:
                        parameters.setBitmapMode(HemiParameters.BitmapMode.PIXEL);
                        break;
                    case 1:
                        parameters.setBitmapMode(HemiParameters.BitmapMode.COLOR);
                        break;
                }
                
                if(outputBitmapFileSrc != null){
                    parameters.setOutputBitmapFile(new File(outputBitmapFileSrc));
                }

            }
        }

        
    }

    @Override
    public void writeConfiguration(File outputParametersFile) throws Exception {
        
        createCommonData();
        
        processElement.setAttribute(new Attribute("mode","Hemi-Photo"));
        
        HemiParameters.Mode mode = parameters.getMode();
        processElement.setAttribute(new Attribute("type", String.valueOf(mode.getMode())));
        
        if(mode == HemiParameters.Mode.ECHOS){
            
            //input
            Element inputFilesElement = new Element("input_files");
            List<LidarScan> rxpScansList = parameters.getRxpScansList();
            
            for(LidarScan scan : rxpScansList){
                Element scanElement = new Element("scan");
                
                Element sopMatrixElement = new Element("SOP");
                sopMatrixElement.setText(scan.matrix.toString());
                scanElement.addContent(sopMatrixElement);
                
                Element inputFileElement = new Element("input_file");
                inputFileElement.setAttribute(new Attribute("src", scan.file.getAbsolutePath()));
                scanElement.addContent(inputFileElement);
                
                inputFilesElement.addContent(scanElement);
            }
            
            processElement.addContent(inputFilesElement);
            
        }else if(mode == HemiParameters.Mode.PAD){
            
            //input
            Element inputFileElement = new Element("input_file");
            inputFileElement.setAttribute(new Attribute("type", String.valueOf(InputType.VOXEL_FILE)));
            inputFileElement.setAttribute(new Attribute("src",parameters.getVoxelFile().getAbsolutePath()));
            processElement.addContent(inputFileElement);
            
            Element sensorPositionsElement = new Element("sensor-positions");
            
            for(Point3d position : parameters.getSensorPositions()){
                Element positionElement = new Element("position");
                positionElement.setAttribute("x", String.valueOf(position.x));
                positionElement.setAttribute("y", String.valueOf(position.y));
                positionElement.setAttribute("z", String.valueOf(position.z));
                sensorPositionsElement.addContent(positionElement);
            }
            
            processElement.addContent(sensorPositionsElement);
        }
        
        //common parameters
        Element pixelNumberElement = new Element("pixel-number");
        pixelNumberElement.setAttribute("value", String.valueOf(parameters.getPixelNumber()));
        processElement.addContent(pixelNumberElement);
        
        Element azimutsNumberElement = new Element("azimut-number");
        azimutsNumberElement.setAttribute("value", String.valueOf(parameters.getAzimutsNumber()));
        processElement.addContent(azimutsNumberElement);
        
        Element zenithNumberElement = new Element("zenith-number");
        zenithNumberElement.setAttribute("value", String.valueOf(parameters.getZenithsNumber()));
        processElement.addContent(zenithNumberElement);
        
        //outputs
        Element outputFilesElement = new Element("output_files");
        Element outputTextFileElement = new Element("output_text_file");
        outputTextFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateTextFile()));
        
        if(parameters.isGenerateTextFile() && parameters.getOutputTextFile()!= null){
            outputTextFileElement.setAttribute("src", parameters.getOutputTextFile().getAbsolutePath());
        }
        
        outputFilesElement.addContent(outputTextFileElement);
        
        Element outputBitmapFileElement = new Element("output_bitmap_file");
        outputBitmapFileElement.setAttribute("generate", String.valueOf(parameters.isGenerateBitmapFile()));
        
        if(parameters.isGenerateBitmapFile()&& parameters.getOutputBitmapFile()!= null){
            outputBitmapFileElement.setAttribute("src", parameters.getOutputBitmapFile().getAbsolutePath());
            outputBitmapFileElement.setAttribute("mode", String.valueOf(parameters.getBitmapMode().getMode()));
        }
        
        outputFilesElement.addContent(outputBitmapFileElement);
        
        processElement.addContent(outputFilesElement);
        
        writeDocument(outputParametersFile);
    }

    public HemiParameters getParameters() {
        return parameters;
    }    
}
