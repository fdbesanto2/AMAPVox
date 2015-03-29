/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.voxelisation.VoxelParameters;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author Julien
 */
public class Configuration {
    
    private final static Logger logger = Logger.getLogger(Configuration.class);
    
    public enum ProcessMode{
        
        VOXELISATION_ALS(0),
        VOXELISATION_TLS(1),
        MERGING(2),
        MULTI_PROCESS(3);
        
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
        RSP_PROJECT(5);
        
        public int type;
        
        private InputType(int type){
            this.type = type;
        }
    }
    
    private ProcessMode processMode = ProcessMode.VOXELISATION_ALS;
    private InputType inputType = InputType.LAS_FILE;
    
    private File inputFile;
    private File trajectoryFile;
    private File outputFile;
    private VoxelParameters voxelParameters;
    private boolean usePopMatrix;
    private boolean useSopMatrix;
    private boolean useVopMatrix;
    private Matrix4d popMatrix;
    private Matrix4d sopMatrix;
    private Matrix4d vopMatrix;
    private List<File> files;
    
    public Configuration(){
        
    }
    
    public Configuration(ProcessMode processMode, InputType inputType, 
            File inputFile, File trajectoryFile, File outputFile, VoxelParameters voxelParameters, 
            boolean usePopMatrix, Matrix4d popMatrix, boolean useSopMatrix, Matrix4d sopMatrix, boolean useVopMatrix, Matrix4d vopMatrix){
        
        this.processMode = processMode;
        this.inputType = inputType;
        this.inputFile = inputFile;
        this.trajectoryFile = trajectoryFile;
        this.outputFile = outputFile;
        this.voxelParameters = voxelParameters;
        this.usePopMatrix = usePopMatrix;
        this.useSopMatrix = useSopMatrix;
        this.useVopMatrix = useVopMatrix;
        this.popMatrix = popMatrix;
        this.sopMatrix = sopMatrix;
        this.vopMatrix = vopMatrix;
    }
    
    public void writeConfiguration(File outputParametersFile){
                
        Element racine = new Element("configuration");
        Document document = new Document(racine);
        
        Element processElement = new Element("process");
        racine.addContent(processElement);
        
        switch(processMode.mode){
            case 0:
                processElement.setAttribute(new Attribute("mode","voxelisation"));
                processElement.setAttribute(new Attribute("type","ALS"));
                break;
                
            case 1:
                processElement.setAttribute(new Attribute("mode","voxelisation"));
                processElement.setAttribute(new Attribute("type","TLS"));
                break;
        }
        
        
        Element inputFileElement = new Element("input_file");
        inputFileElement.setAttribute(new Attribute("type", String.valueOf(inputType.type)));
        
        inputFileElement.setAttribute(new Attribute("src",inputFile.getAbsolutePath()));
        processElement.addContent(inputFileElement);
        
        Element trajectoryFileElement = new Element("trajectory");
        trajectoryFileElement.setAttribute(new Attribute("src",trajectoryFile.getAbsolutePath()));
        processElement.addContent(trajectoryFileElement);
        
        Element outputFileElement = new Element("output_file");
        outputFileElement.setAttribute(new Attribute("src",outputFile.getAbsolutePath()));
        processElement.addContent(outputFileElement);
        
        Element voxelSpaceElement = new Element("voxelspace");
        voxelSpaceElement.setAttribute("xmin", String.valueOf(voxelParameters.bottomCorner.x));
        voxelSpaceElement.setAttribute("ymin", String.valueOf(voxelParameters.bottomCorner.y));
        voxelSpaceElement.setAttribute("zmin", String.valueOf(voxelParameters.bottomCorner.z));
        voxelSpaceElement.setAttribute("xmax", String.valueOf(voxelParameters.topCorner.x));
        voxelSpaceElement.setAttribute("ymax", String.valueOf(voxelParameters.topCorner.y));
        voxelSpaceElement.setAttribute("zmax", String.valueOf(voxelParameters.topCorner.z));
        voxelSpaceElement.setAttribute("splitX", String.valueOf(voxelParameters.split.x));
        voxelSpaceElement.setAttribute("splitY", String.valueOf(voxelParameters.split.y));
        voxelSpaceElement.setAttribute("splitZ", String.valueOf(voxelParameters.split.z));
        processElement.addContent(voxelSpaceElement);
        
        Element ponderationElement = new Element("ponderation");
        ponderationElement.setAttribute(new Attribute("mode","1"));
        
        StringBuilder weightingDataString = new StringBuilder();
        float[][] weightingData = voxelParameters.getWeightingData();
        for(int i=0;i<weightingData.length;i++){
            for(int j=0;j<weightingData[0].length;j++){
                weightingDataString.append(weightingData[i][j]).append(" ");
            }
        }
        
        Element matrixElement = createMatrixElement("ponderation", "float", weightingData.length, weightingData[0].length, weightingDataString.toString().trim());
        ponderationElement.addContent(matrixElement);
        processElement.addContent(ponderationElement);
        
        Element dtmFilterElement = new Element("dtm-filter");
        dtmFilterElement.setAttribute(new Attribute("enabled",String.valueOf(voxelParameters.useDTMCorrection())));
        if(voxelParameters.useDTMCorrection()){
            dtmFilterElement.setAttribute(new Attribute("src", voxelParameters.getDtmFile().getAbsolutePath()));
            dtmFilterElement.setAttribute(new Attribute("height-min",String.valueOf(voxelParameters.minDTMDistance)));
        }
        
        processElement.addContent(dtmFilterElement);
        
        Element transformationElement = new Element("transformation");
        
        
        transformationElement.setAttribute(new Attribute("use-pop",String.valueOf(usePopMatrix)));
        transformationElement.setAttribute(new Attribute("use-sop",String.valueOf(useSopMatrix)));
        transformationElement.setAttribute(new Attribute("use-vop",String.valueOf(useVopMatrix)));
        
        if(usePopMatrix && popMatrix != null){
            Element matrixPopElement = createMatrixElement("pop", "float", 4, 4, popMatrix.toString().replace("\n", ", "));
            transformationElement.addContent(matrixPopElement);
        }
        
        if(useSopMatrix && sopMatrix != null){
            Element matrixSopElement = createMatrixElement("sop", "float", 4, 4, sopMatrix.toString().replace("\n", ", "));
            transformationElement.addContent(matrixSopElement);
        }
        
        if(useVopMatrix && vopMatrix != null){
            Element matrixVopElement = createMatrixElement("vop", "float", 4, 4, vopMatrix.toString().replace("\n", ", "));
            transformationElement.addContent(matrixVopElement);
        }
        
        
        processElement.addContent(transformationElement);
        
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        try {
            output.output(document, new BufferedOutputStream(new FileOutputStream(outputParametersFile)));
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
    
    public void readConfiguration(File inputParametersFile){
                
        try {
            
            SAXBuilder sxb = new SAXBuilder();
            Document document = sxb.build(inputParametersFile);
            Element root = document.getRootElement();
            
            Element processElement = root.getChild("process");
            String mode = processElement.getAttributeValue("mode");
            String type = processElement.getAttributeValue("type");
            
            Element outputFileElement;
            List<Element> filesElement;
            
            switch(mode){
                case "voxelisation":
                    Element inputFileElement = processElement.getChild("input_file");
                    inputType.type = Integer.valueOf(inputFileElement.getAttributeValue("type"));
                    inputFile = new File(inputFileElement.getAttributeValue("src"));
                    
                    switch(type){
                        case "ALS":
                            processMode = ProcessMode.VOXELISATION_ALS;
                            Element trajectoryFileElement = processElement.getChild("trajectory");
                            trajectoryFile = new File(trajectoryFileElement.getAttributeValue("src"));
                            break;
                        case "TLS":
                            processMode = ProcessMode.VOXELISATION_TLS;
                            break;
                    }
                    
                    outputFileElement = processElement.getChild("output_file");
                    outputFile = new File(outputFileElement.getAttributeValue("src"));
                    
                    Element voxelSpaceElement = processElement.getChild("voxelspace");
                    
                    voxelParameters = new VoxelParameters();
                    voxelParameters.setBottomCorner(new Point3d(
                                    Double.valueOf(voxelSpaceElement.getAttributeValue("xmin")), 
                                    Double.valueOf(voxelSpaceElement.getAttributeValue("ymin")), 
                                    Double.valueOf(voxelSpaceElement.getAttributeValue("zmin"))));

                    voxelParameters.setTopCorner(new Point3d(
                                        Double.valueOf(voxelSpaceElement.getAttributeValue("xmax")), 
                                        Double.valueOf(voxelSpaceElement.getAttributeValue("ymax")), 
                                        Double.valueOf(voxelSpaceElement.getAttributeValue("zmax"))));

                    voxelParameters.setSplit(new Point3i(
                                        Integer.valueOf(voxelSpaceElement.getAttributeValue("splitX")), 
                                        Integer.valueOf(voxelSpaceElement.getAttributeValue("splitY")), 
                                        Integer.valueOf(voxelSpaceElement.getAttributeValue("splitZ"))));
                    
                    Element ponderationElement = processElement.getChild("ponderation");
                    
                    if(ponderationElement != null){
                        Element matrixElement = ponderationElement.getChild("matrix");
                        int rowNumber = Integer.valueOf(matrixElement.getChildText("rows"));
                        int colNumber = Integer.valueOf(matrixElement.getChildText("cols"));
                        String data = matrixElement.getChildText("data");
                        String[] datas = data.split(" ");
                        float[][] weightingData = new float[rowNumber][colNumber];
                        
                        int count = 0;
                        for(int i=0;i<weightingData.length;i++){
                            for(int j=0;j<weightingData[0].length;j++){
                                weightingData[i][j] = Float.valueOf(datas[count]);
                                count++;
                            }
                        }
                        
                        voxelParameters.setWeightingData(weightingData);
                    }
                    
                    Element dtmFilterElement = processElement.getChild("dtm-filter");
                    
                    if(dtmFilterElement != null){
                        boolean useDTM = Boolean.valueOf(dtmFilterElement.getAttributeValue("enabled"));
                        voxelParameters.setUseDTMCorrection(useDTM);
                        if(useDTM){
                            voxelParameters.setDtmFile(new File(dtmFilterElement.getAttributeValue("src")));
                            voxelParameters.minDTMDistance = Float.valueOf(dtmFilterElement.getAttributeValue("height-min"));
                        }                        
                    }
                    
                    Element transformationElement = processElement.getChild("transformation");
                    
                    if(transformationElement != null){
                        usePopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-pop"));
                        useSopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-sop"));
                        useVopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-vop"));
                        List<Element> matrixList = transformationElement.getChildren("matrix");
                        for(Element e : matrixList){
                            
                            String matrixType = e.getAttributeValue("type_id");
                            String data = e.getChild("data").getText();
                            data = data.substring(0, data.length()-1);
                            String[] datas = data.split(", ");
                            
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
                            
                            switch(matrixType){
                                case "pop":
                                    popMatrix = mat;
                                    break;
                                case "sop":
                                    sopMatrix = mat;
                                    break;
                                case "vop":
                                    vopMatrix = mat;
                                    break;
                            }
                        }
                    }
                    
                    break;
                    
                case "merging":
                    processMode = ProcessMode.MERGING;
                    filesElement = processElement.getChildren("files");
                    
                    files = new ArrayList<>();
                    
                    for(Element e : filesElement){
                        files.add(new File(e.getAttributeValue("src")));
                    }
                    
                    outputFileElement = processElement.getChild("output_file");
                    outputFile = new File(outputFileElement.getAttributeValue("src"));
                    
                    break;
                    
                case "multi-process":
                    
                    processMode = ProcessMode.MULTI_PROCESS;
                    filesElement = processElement.getChildren("files");
                    
                    files = new ArrayList<>();
                    
                    for(Element e : filesElement){
                        files.add(new File(e.getAttributeValue("src")));
                    }
                    
                    break;
            }
            
        } catch (JDOMException | IOException ex) {
            logger.error(ex);
        }

    }
    
    private Element createMatrixElement(String id, String type, int rowNumber, int columnNumber, String data){
        
        Element matrixElement = new Element("matrix");
        matrixElement.setAttribute("type_id", id);
        matrixElement.addContent(new Element("rows").setText(String.valueOf(rowNumber)));
        matrixElement.addContent(new Element("cols").setText(String.valueOf(columnNumber)));
        matrixElement.addContent(new Element("dt").setText(type));
        matrixElement.addContent(new Element("data").setText(data));
        
        return matrixElement;
    }
    

    public ProcessMode getProcessMode() {
        return processMode;
    }

    public void setProcessMode(ProcessMode processMode) {
        this.processMode = processMode;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public File getTrajectoryFile() {
        return trajectoryFile;
    }

    public void setTrajectoryFile(File trajectoryFile) {
        this.trajectoryFile = trajectoryFile;
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public VoxelParameters getVoxelParameters() {
        return voxelParameters;
    }

    public void setVoxelParameters(VoxelParameters voxelParameters) {
        this.voxelParameters = voxelParameters;
    }

    public boolean isUsePopMatrix() {
        return usePopMatrix;
    }

    public void setUsePopMatrix(boolean usePopMatrix) {
        this.usePopMatrix = usePopMatrix;
    }

    public boolean isUseSopMatrix() {
        return useSopMatrix;
    }

    public void setUseSopMatrix(boolean useSopMatrix) {
        this.useSopMatrix = useSopMatrix;
    }

    public boolean isUseVopMatrix() {
        return useVopMatrix;
    }

    public void setUseVopMatrix(boolean useVopMatrix) {
        this.useVopMatrix = useVopMatrix;
    }

    public Matrix4d getPopMatrix() {
        return popMatrix;
    }

    public void setPopMatrix(Matrix4d popMatrix) {
        this.popMatrix = popMatrix;
    }

    public Matrix4d getSopMatrix() {
        return sopMatrix;
    }

    public void setSopMatrix(Matrix4d sopMatrix) {
        this.sopMatrix = sopMatrix;
    }

    public Matrix4d getVopMatrix() {
        return vopMatrix;
    }

    public void setVopMatrix(Matrix4d vopMatrix) {
        this.vopMatrix = vopMatrix;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }
    
}
