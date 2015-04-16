/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar;

import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Configuration {
    
    
    
    private final static Logger logger = Logger.getLogger(Configuration.class);
    
    public enum ProcessMode{
        
        VOXELISATION_ALS(0),
        VOXELISATION_TLS(1),
        MERGING(2),
        MULTI_PROCESS(3),
        MULTI_RES(4);
        
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
    private List<MatrixAndFile> matricesAndFiles;
    private List<Filter> filters;
    private float[] multiResPadMax;
    
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
        
        
        if(processMode == ProcessMode.VOXELISATION_ALS || processMode == ProcessMode.VOXELISATION_TLS){
            
            if(processMode == ProcessMode.VOXELISATION_ALS){
                processElement.setAttribute(new Attribute("mode","voxelisation"));
                processElement.setAttribute(new Attribute("type","ALS"));
            }else{
                processElement.setAttribute(new Attribute("mode","voxelisation"));
                processElement.setAttribute(new Attribute("type","TLS"));
            }
            
            Element inputFileElement = new Element("input_file");
            inputFileElement.setAttribute(new Attribute("type", String.valueOf(inputType.type)));
            inputFileElement.setAttribute(new Attribute("src",inputFile.getAbsolutePath()));
            processElement.addContent(inputFileElement);
            
            if(processMode == ProcessMode.VOXELISATION_ALS){
                Element trajectoryFileElement = new Element("trajectory");
                trajectoryFileElement.setAttribute(new Attribute("src",trajectoryFile.getAbsolutePath()));
                processElement.addContent(trajectoryFileElement);
            }
            
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
            voxelSpaceElement.setAttribute("resolution", String.valueOf(voxelParameters.resolution));
            processElement.addContent(voxelSpaceElement);
            
            /***PONDERATION***/
            Element ponderationElement = new Element("ponderation");
            ponderationElement.setAttribute(new Attribute("mode",String.valueOf(voxelParameters.getWeighting())));
            
            if(voxelParameters.getWeighting() > 0){
                StringBuilder weightingDataString = new StringBuilder();
                float[][] weightingData = voxelParameters.getWeightingData();
                for(int i=0;i<weightingData.length;i++){
                    for(int j=0;j<weightingData[0].length;j++){
                        weightingDataString.append(weightingData[i][j]).append(" ");
                    }
                }
                Element matrixElement = createMatrixElement("ponderation", weightingDataString.toString().trim());
                ponderationElement.addContent(matrixElement);
            }
            
            processElement.addContent(ponderationElement);
            
            /***DEM FILTER***/
            
            Element dtmFilterElement = new Element("dtm-filter");
            dtmFilterElement.setAttribute(new Attribute("enabled",String.valueOf(voxelParameters.useDTMCorrection())));
            if(voxelParameters.useDTMCorrection()){
                if(voxelParameters.getDtmFile() != null){
                    dtmFilterElement.setAttribute(new Attribute("src", voxelParameters.getDtmFile().getAbsolutePath()));
                }
                
                dtmFilterElement.setAttribute(new Attribute("height-min",String.valueOf(voxelParameters.minDTMDistance)));
            }

            processElement.addContent(dtmFilterElement);
            
            /***TRANSFORMATION***/
            
            Element transformationElement = new Element("transformation");


            transformationElement.setAttribute(new Attribute("use-pop",String.valueOf(usePopMatrix)));
            transformationElement.setAttribute(new Attribute("use-sop",String.valueOf(useSopMatrix)));
            transformationElement.setAttribute(new Attribute("use-vop",String.valueOf(useVopMatrix)));

            if(usePopMatrix && popMatrix != null){
                Element matrixPopElement = createMatrixElement("pop", popMatrix.toString());
                transformationElement.addContent(matrixPopElement);
            }

            if(useSopMatrix && sopMatrix != null){
                Element matrixSopElement = createMatrixElement("sop", sopMatrix.toString());
                transformationElement.addContent(matrixSopElement);
            }

            if(useVopMatrix && vopMatrix != null){
                Element matrixVopElement = createMatrixElement("vop", vopMatrix.toString());
                transformationElement.addContent(matrixVopElement);
            }

            processElement.addContent(transformationElement);
            
            /***LIMITS***/
            
            Element limitsElement = new Element("limits");
            Element limitElement = new Element("limit");
            limitElement.setAttribute("name", "PAD");
            limitElement.setAttribute("min", "");
            limitElement.setAttribute("max", String.valueOf(voxelParameters.getMaxPAD()));
            limitsElement.addContent(limitElement);
            processElement.addContent(limitsElement);
            
            if(filters != null){
                Element filtersElement = new Element("filters");
                
                for(Filter f : filters){
                    Element filterElement = new Element("filter");
                    filterElement.setAttribute("variable", f.getVariable());
                    filterElement.setAttribute("inequality", f.getConditionString());
                    filterElement.setAttribute("value", String.valueOf(f.getValue()));
                    filtersElement.addContent(filterElement);
                }
                
                processElement.addContent(filtersElement);
            }
            if(processMode == ProcessMode.VOXELISATION_ALS){
                
                Element groundEnergyElement = new Element("ground-energy");
                groundEnergyElement.setAttribute("generate", String.valueOf(voxelParameters.isCalculateGroundEnergy()));
                
                if(voxelParameters.getGroundEnergyFile() != null){
                    groundEnergyElement.setAttribute("src", voxelParameters.getGroundEnergyFile().getAbsolutePath());
                    groundEnergyElement.setAttribute("type", String.valueOf(voxelParameters.getGroundEnergyFileFormat()));
                }                
                
                processElement.addContent(groundEnergyElement);
                
            }else if(processMode == ProcessMode.VOXELISATION_TLS && inputType == InputType.RSP_PROJECT){
                
                /***MERGING***/
                
                Element mergingElement = new Element("merging");
                mergingElement.setAttribute("enabled", String.valueOf(voxelParameters.isMergingAfter()));
                if(voxelParameters.isMergingAfter()){
                    mergingElement.setAttribute("src", voxelParameters.getMergedFile().getAbsolutePath());
                }
                
                processElement.addContent(mergingElement);
                
                /***FILE LIST TO PROCESS***/
                
                Element filesElement = new Element("files");
                if(matricesAndFiles != null){
                    for(MatrixAndFile f : matricesAndFiles){
                        Element fileElement = new Element("file");
                        fileElement.setAttribute("src", f.file.getAbsolutePath());
                        fileElement.addContent(new Element("matrix").setText(f.matrix.toString()));
                        filesElement.addContent(fileElement);
                    }
                }
                processElement.addContent(filesElement);
            }
            
        }else if(processMode == ProcessMode.MULTI_RES){
            
            processElement.setAttribute(new Attribute("mode","multi-resolutions"));
            processElement.setAttribute(new Attribute("type","ALS"));
                        
            /***FILE LIST TO PROCESS***/
            
            processElement.addContent(createFilesElement(files));
            
            Element outputFileElement = new Element("output_file");
            outputFileElement.setAttribute(new Attribute("src",outputFile.getAbsolutePath()));
            processElement.addContent(outputFileElement);
            
            Element limitsElement = new Element("limits");
            
            limitsElement.addContent(createLimitElement("PAD_1m", "", String.valueOf(multiResPadMax[0])));
            limitsElement.addContent(createLimitElement("PAD_2m", "", String.valueOf(multiResPadMax[1])));
            limitsElement.addContent(createLimitElement("PAD_3m", "", String.valueOf(multiResPadMax[2])));
            limitsElement.addContent(createLimitElement("PAD_4m", "", String.valueOf(multiResPadMax[3])));
            
            processElement.addContent(limitsElement);
            
        }else if(processMode == ProcessMode.MERGING){
            
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
        }
        
        //Element transmittanceFormula = new Element("transmittance");
        //transmittanceFormula.setAttribute("mode", String.valueOf(voxelParameters.getTransmittanceMode()));
        
        //processElement.addContent(new Element("formula").addContent(transmittanceFormula));
        
        
        XMLOutputter output = new XMLOutputter(Format.getPrettyFormat());
        try {
            output.output(document, new BufferedOutputStream(new FileOutputStream(outputParametersFile)));
        } catch (IOException ex) {
            logger.error(ex);
        }
    }
    
    private Element createLimitElement(String name, String min, String max){
        
        Element limitElement = new Element("limit");
        limitElement.setAttribute("name", name);
        limitElement.setAttribute("min", min);
        limitElement.setAttribute("max", max);
        
        return limitElement;
    }
    
    private Element createFilesElement(List<File> files){
        
        Element filesElement = new Element("files");
            
        for(File f : files){
            filesElement.addContent(new Element("file").setAttribute("src", f.getAbsolutePath()));
        }
        
        return filesElement;
    }
    
    public void readConfiguration(File inputParametersFile) throws Exception{
                
        try {
            
            SAXBuilder sxb = new SAXBuilder();
            Document document = sxb.build(inputParametersFile);
            Element root = document.getRootElement();
            
            Element processElement = root.getChild("process");
            String mode = processElement.getAttributeValue("mode");
            String type = processElement.getAttributeValue("type");
            
            Element outputFileElement;
            Element filesElement;
            Element limitsElement;
            
            switch(mode){
                case "voxelisation":
                    Element inputFileElement = processElement.getChild("input_file");
                    
                    int inputTypeInteger = Integer.valueOf(inputFileElement.getAttributeValue("type"));
                    
                    switch(inputTypeInteger){
                        case 0:
                            inputType = InputType.LAS_FILE;
                            break;
                        case 1:
                            inputType = InputType.LAZ_FILE;
                            break;
                        case 2:
                            inputType = InputType.POINTS_FILE;
                            break;
                        case 3:
                            inputType = InputType.LAS_FILE;
                            break;
                        case 4:
                            inputType = InputType.RXP_SCAN;
                            break;
                        case 5:
                            inputType = InputType.RSP_PROJECT;
                            break;
                    }
                    
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
                    
                    try{
                        voxelParameters.setResolution(Double.valueOf(voxelSpaceElement.getAttributeValue("resolution")));
                    }catch(Exception e){}
                    
                    
                    Element ponderationElement = processElement.getChild("ponderation");
                    
                    if(ponderationElement != null){
                        
                        voxelParameters.setWeighting(Integer.valueOf(ponderationElement.getAttributeValue("mode")));
                        
                        if(voxelParameters.getWeighting() > 0){
                            Element matrixElement = ponderationElement.getChild("matrix");
                            int rowNumber = 7;
                            int colNumber = 7;
                            String data = matrixElement.getText();
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
                            Matrix4d mat = getMatrixFromData(e.getText());
                            
                            
                            
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
                    
                    limitsElement = processElement.getChild("limits");
                    
                    if(limitsElement != null){
                        Element limitElement = limitsElement.getChild("limit");
                        voxelParameters.setMaxPAD(Float.valueOf(limitElement.getAttributeValue("max")));
                    }
                    
                    
                    Element filtersElement = processElement.getChild("filters");
                    if(filtersElement != null){
                        
                        List<Element> childrensFilter = filtersElement.getChildren("filter");
                        
                        
                        if(childrensFilter != null){
                            
                            filters = new ArrayList<>();
                            
                            for(Element e : childrensFilter){

                                String variable = e.getAttributeValue("variable");
                                String inequality = e.getAttributeValue("inequality");
                                String value = e.getAttributeValue("value");
                                
                                filters.add(new Filter(variable, Double.valueOf(value), Filter.getConditionFromString(inequality)));
                            }
                        }
                    }
                    
                    
                    switch (type) {
                        case "TLS":

                            if (inputType == InputType.RSP_PROJECT) {

                                filesElement = processElement.getChild("files");
                                List<Element> childrens = filesElement.getChildren("file");
                                matricesAndFiles = new ArrayList<>();

                                for (Element e : childrens) {

                                    Matrix4d mat = getMatrixFromData(e.getChildText("matrix"));
                                    File f = new File(e.getAttributeValue("src"));

                                    matricesAndFiles.add(new MatrixAndFile(f, mat));
                                }

                                Element mergingElement = processElement.getChild("merging");
                                if (mergingElement != null) {
                                    voxelParameters.setMergingAfter(Boolean.valueOf(mergingElement.getAttributeValue("enabled")));
                                    if (voxelParameters.isMergingAfter()) {
                                        voxelParameters.setMergedFile(new File(mergingElement.getAttributeValue("src")));
                                    }
                                }
                            }

                            break;
                            
                        case "ALS":
                            try{
                                Element groundEnergyElement = processElement.getChild("ground-energy");
                                if(groundEnergyElement != null){
                                    voxelParameters.setCalculateGroundEnergy(Boolean.valueOf(groundEnergyElement.getAttributeValue("generate")));

                                    if(voxelParameters.isCalculateGroundEnergy()){
                                        voxelParameters.setGroundEnergyFileFormat(Short.valueOf(groundEnergyElement.getAttributeValue("type")));
                                        voxelParameters.setGroundEnergyFile(new File(groundEnergyElement.getAttributeValue("src")));
                                    }
                                }
                            }catch(Exception e){
                                logger.warn("Parameters are missing");
                            }
                            
                            
                            break;
                    }

                    break;

                case "merging":
                    processMode = ProcessMode.MERGING;

                    outputFile = new File(processElement.getChild("output_file").getAttributeValue("src"));

                    filesElement = processElement.getChild("files");
                    List<Element> childrens = filesElement.getChildren("file");

                    files = new ArrayList<>();

                    for (Element e : childrens) {
                        files.add(new File(e.getAttributeValue("src")));
                    }

                    break;

                case "multi-process":

                    processMode = ProcessMode.MULTI_PROCESS;
                    /*
                     filesElement = processElement.getChildren("files");
                    
                     files = new ArrayList<>();
                    
                     for(Element e : filesElement){
                     files.add(new File(e.getAttributeValue("src")));
                     }
                     */
                    break;

                case "multi-resolutions":
                    processMode = ProcessMode.MULTI_RES;

                    filesElement = processElement.getChild("files");

                    List<Element> fileElementList = filesElement.getChildren("file");

                    files = new ArrayList<>();

                    for (Element e : fileElementList) {
                        files.add(new File(e.getAttributeValue("src")));
                    }

                    outputFile = new File(processElement.getChild("output_file").getAttributeValue("src"));

                    limitsElement = processElement.getChild("limits");
                    List<Element> limitElementList = limitsElement.getChildren("limit");

                    if (limitElementList != null) {
                        multiResPadMax = new float[4];
                        multiResPadMax[0] = Float.valueOf(limitElementList.get(0).getAttributeValue("max"));
                        multiResPadMax[1] = Float.valueOf(limitElementList.get(1).getAttributeValue("max"));
                        multiResPadMax[2] = Float.valueOf(limitElementList.get(2).getAttributeValue("max"));
                        multiResPadMax[3] = Float.valueOf(limitElementList.get(3).getAttributeValue("max"));
                    }

                    break;
            }
            
            Element formulaElement = processElement.getChild("formula");
                    
            if(formulaElement != null){
                Element transmittanceElement = formulaElement.getChild("transmittance");
                if(transmittanceElement != null){
                    voxelParameters.setTransmittanceMode(Integer.valueOf(transmittanceElement.getAttributeValue("mode")));
                }
            }
            
        } catch (JDOMException | IOException ex) {
            throw new Exception(ex);
        }

    }
    
    private Element createMatrixElement(String id, String data){
        
        Element matrixElement = new Element("matrix");
        matrixElement.setAttribute("type_id", id);
        matrixElement.setText(data);
        
        return matrixElement;
    }
    
    private Matrix4d getMatrixFromData(String data){
        
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

    public List<MatrixAndFile> getMatricesAndFiles() {
        return matricesAndFiles;
    }

    public void setMatricesAndFiles(List<MatrixAndFile> matricesAndFiles) {
        this.matricesAndFiles = matricesAndFiles;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public float[] getMultiResPadMax() {
        return multiResPadMax;
    }

    public void setMultiResPadMax(float[] multiResPadMax) {
        this.multiResPadMax = multiResPadMax;
    }
    
}
