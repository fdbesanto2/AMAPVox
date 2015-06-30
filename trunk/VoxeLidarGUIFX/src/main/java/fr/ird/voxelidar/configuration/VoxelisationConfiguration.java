/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.configuration;

import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.voxelisation.PointcloudFilter;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
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
public class VoxelisationConfiguration extends Configuration{
    
    private final static Logger logger = Logger.getLogger(VoxelisationConfiguration.class);
    
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
    private List<Input> multiProcessInputs;
    private boolean correctNaNs;
    private List<MatrixAndFile> matricesAndFiles;
    private List<Filter> filters;
    private List<Integer> classifiedPointsToDiscard;
    //private boolean removeLowPoint = false;
    private float[] multiResPadMax;
    private boolean multiResUseDefaultMaxPad = false;
    private List<Input> multiResList;
    
    private Element voxelSpaceElement;
    private Element inputFileElement;
    private Element outputFileElement;
    private Element groundEnergyElement;
    private Element subVoxelSpaceElement;
    
    public VoxelisationConfiguration(){
        
    }
    
    public static VoxelisationConfiguration createMultiFileVoxelisationConfiguration(InputType inputType, 
            List<Input> inputs, File trajectoryFile, File outputPath, VoxelParameters voxelParameters, 
            boolean usePopMatrix, Matrix4d popMatrix, boolean useSopMatrix, Matrix4d sopMatrix, boolean useVopMatrix, Matrix4d vopMatrix){
        
        VoxelisationConfiguration configuration = new VoxelisationConfiguration();
        
        configuration.processMode = ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES;
        configuration.inputType = inputType;
        configuration.trajectoryFile = trajectoryFile;
        configuration.outputFile = outputPath;
        configuration.voxelParameters = voxelParameters;
        configuration.usePopMatrix = usePopMatrix;
        configuration.useSopMatrix = useSopMatrix;
        configuration.useVopMatrix = useVopMatrix;
        configuration.popMatrix = popMatrix;
        configuration.sopMatrix = sopMatrix;
        configuration.vopMatrix = vopMatrix;
        
        return configuration;
        
    }
    
    public VoxelisationConfiguration(ProcessMode processMode, InputType inputType, 
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
    
    @Override
    public void writeConfiguration(File outputParametersFile){
                      
        Element racine = new Element("configuration");
        racine.setAttribute("creation-date", new Date().toString());
        
        try {
            Class clazz = VoxelisationConfiguration.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1) + "/META-INF/MANIFEST.MF";
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            Attributes attributes= manifest.getMainAttributes();
            String buildVersion = attributes.getValue("Implementation-Build");
            
            if(buildVersion != null){
                racine.setAttribute("build-version", buildVersion);
            }else{
                logger.error("Cannot get Implementation-Build property in manifest file");
            }
        } catch (Exception ex) {
            logger.error("Cannot get manifest file: ",ex);
        }
        
        Document document = new Document(racine);
        
        Element processElement = new Element("process");
        racine.addContent(processElement);
        
        
        if(processMode == ProcessMode.VOXELISATION_ALS || 
            processMode == ProcessMode.VOXELISATION_TLS || 
            processMode == ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES){
            
            if(processMode == ProcessMode.VOXELISATION_ALS){
                processElement.setAttribute(new Attribute("mode","voxelisation"));
                processElement.setAttribute(new Attribute("type","ALS"));
            }else if(processMode == ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES){
                processElement.setAttribute(new Attribute("mode","multi-voxelisation"));
                processElement.setAttribute(new Attribute("type","ALS"));
            }else{
                processElement.setAttribute(new Attribute("mode","voxelisation"));
                processElement.setAttribute(new Attribute("type","TLS"));
            }
            
            if(processMode != ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES){
                
                inputFileElement = new Element("input_file");
                inputFileElement.setAttribute(new Attribute("type", String.valueOf(inputType.type)));
                inputFileElement.setAttribute(new Attribute("src",inputFile.getAbsolutePath()));
                processElement.addContent(inputFileElement);
                
                outputFileElement = new Element("output_file");
                outputFileElement.setAttribute(new Attribute("src",outputFile.getAbsolutePath()));
                processElement.addContent(outputFileElement);
            }            
            
            if(processMode == ProcessMode.VOXELISATION_ALS || processMode == ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES ){
                Element trajectoryFileElement = new Element("trajectory");
                trajectoryFileElement.setAttribute(new Attribute("src",trajectoryFile.getAbsolutePath()));
                processElement.addContent(trajectoryFileElement);
            }
            
            if(processMode != ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES){
                
                voxelSpaceElement = new Element("voxelspace");
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
            }
            
            
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
            
            /***DTM FILTER***/
            
            Element dtmFilterElement = new Element("dtm-filter");
            dtmFilterElement.setAttribute(new Attribute("enabled",String.valueOf(voxelParameters.useDTMCorrection())));
            if(voxelParameters.useDTMCorrection()){
                if(voxelParameters.getDtmFile() != null){
                    dtmFilterElement.setAttribute(new Attribute("src", voxelParameters.getDtmFile().getAbsolutePath()));
                }
                
                dtmFilterElement.setAttribute(new Attribute("height-min",String.valueOf(voxelParameters.minDTMDistance)));
            }
            
            processElement.addContent(dtmFilterElement);
            
            Element pointcloudFiltersElement = new Element("pointcloud-filters");
            pointcloudFiltersElement.setAttribute(new Attribute("enabled",String.valueOf(voxelParameters.isUsePointCloudFilter())));
            
            if(voxelParameters.isUsePointCloudFilter()){
                
                List<PointcloudFilter> pointcloudFilters = voxelParameters.getPointcloudFilters();
                
                if(pointcloudFilters != null){
                    
                    for(PointcloudFilter filter: pointcloudFilters){
                        Element pointcloudFilterElement = new Element("pointcloud-filter");
                        pointcloudFilterElement.setAttribute(new Attribute("src", filter.getPointcloudFile().getAbsolutePath()));
                        pointcloudFilterElement.setAttribute(new Attribute("error-margin",String.valueOf(filter.getPointcloudErrorMargin())));
                        
                        String operationType;
                        if(filter.isKeep()){
                            operationType = "Keep";
                        }else{
                            operationType = "Discard";
                        }
                        
                        pointcloudFilterElement.setAttribute(new Attribute("operation-type",operationType));
                        pointcloudFiltersElement.addContent(pointcloudFilterElement);
                    }
                }
                
            }
            
            processElement.addContent(pointcloudFiltersElement);
            
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
            
            if(processMode != ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES){
                processElement.addContent(limitsElement);
            }
            
            Element filtersElement = new Element("filters");
            
            if(filters != null && !filters.isEmpty()){
                
                Element shotFilterElement = new Element("shot-filters");
                
                for(Filter f : filters){
                    Element filterElement = new Element("filter");
                    filterElement.setAttribute("variable", f.getVariable());
                    filterElement.setAttribute("inequality", f.getConditionString());
                    filterElement.setAttribute("value", String.valueOf(f.getValue()));
                    shotFilterElement.addContent(filterElement);
                }
                
                filtersElement.addContent(shotFilterElement);
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
            
            
            processElement.addContent(filtersElement);
            
            if(processMode == ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES){
                
                limitsElement.setAttribute("use-default", String.valueOf(multiResUseDefaultMaxPad));

                limitsElement.addContent(createLimitElement("PAD_1m", "", String.valueOf(multiResPadMax[0])));
                limitsElement.addContent(createLimitElement("PAD_2m", "", String.valueOf(multiResPadMax[1])));
                limitsElement.addContent(createLimitElement("PAD_3m", "", String.valueOf(multiResPadMax[2])));
                limitsElement.addContent(createLimitElement("PAD_4m", "", String.valueOf(multiResPadMax[3])));
                limitsElement.addContent(createLimitElement("PAD_5m", "", String.valueOf(multiResPadMax[4])));
            
                processElement.addContent(limitsElement);
                
                Element inputsElement = new Element("inputs");
                
                if(multiProcessInputs != null){

                    for(Input input : multiProcessInputs){

                        Element inputElement = new Element("input");

                        if(input.voxelParameters != null){
                            voxelSpaceElement = new Element("voxelspace");
                            voxelSpaceElement.setAttribute("xmin", String.valueOf(input.voxelParameters.bottomCorner.x));
                            voxelSpaceElement.setAttribute("ymin", String.valueOf(input.voxelParameters.bottomCorner.y));
                            voxelSpaceElement.setAttribute("zmin", String.valueOf(input.voxelParameters.bottomCorner.z));
                            voxelSpaceElement.setAttribute("xmax", String.valueOf(input.voxelParameters.topCorner.x));
                            voxelSpaceElement.setAttribute("ymax", String.valueOf(input.voxelParameters.topCorner.y));
                            voxelSpaceElement.setAttribute("zmax", String.valueOf(input.voxelParameters.topCorner.z));
                            voxelSpaceElement.setAttribute("splitX", String.valueOf(input.voxelParameters.split.x));
                            voxelSpaceElement.setAttribute("splitY", String.valueOf(input.voxelParameters.split.y));
                            voxelSpaceElement.setAttribute("splitZ", String.valueOf(input.voxelParameters.split.z));
                            voxelSpaceElement.setAttribute("resolution", String.valueOf(input.voxelParameters.resolution));
                            inputElement.addContent(voxelSpaceElement);
                        }
                        if(input.inputFile != null){
                            inputFileElement = new Element("input_file");
                            inputFileElement.setAttribute(new Attribute("type", String.valueOf(inputType.type)));
                            inputFileElement.setAttribute(new Attribute("src",input.inputFile.getAbsolutePath()));
                            inputElement.addContent(inputFileElement);

                        }
                        
                        if(input.dtmFile != null){
                            Element dtmFileElement = new Element("dtm-filter");
                            dtmFileElement.setAttribute(new Attribute("src",input.dtmFile.getAbsolutePath()));
                            inputElement.addContent(dtmFileElement);
                        }
                        
                        if(input.outputFile != null){
                            outputFileElement = new Element("output_file");
                            outputFileElement.setAttribute(new Attribute("src",input.outputFile.getAbsolutePath()));
                            inputElement.addContent(outputFileElement);
                        }
                        
                        if(input.voxelParameters.getGroundEnergyFile() != null){
                            groundEnergyElement = new Element("ground-energy");
                            groundEnergyElement.setAttribute("generate", String.valueOf(input.voxelParameters.isCalculateGroundEnergy()));

                            if(input.voxelParameters.getGroundEnergyFile() != null){
                                groundEnergyElement.setAttribute("src", input.voxelParameters.getGroundEnergyFile().getAbsolutePath());
                                groundEnergyElement.setAttribute("type", String.valueOf(input.voxelParameters.getGroundEnergyFileFormat()));
                            }
                            inputElement.addContent(groundEnergyElement);
                        }
                        
                        if(input.multiResList != null){
                            
                            Element multiResElement = new Element("multi-res");
                            multiResElement.setAttribute("enabled", String.valueOf(isCorrectNaNs()));
                            
                            if(isCorrectNaNs()){
                                
                                Element multiResInputsElement = new Element("inputs");
                            
                                for(Input subInput : input.multiResList){

                                    Element multiResInputElement = new Element("input");
                                    subVoxelSpaceElement = new Element("voxelspace");
                                    subVoxelSpaceElement.setAttribute("splitX", String.valueOf(subInput.voxelParameters.split.x));
                                    subVoxelSpaceElement.setAttribute("splitY", String.valueOf(subInput.voxelParameters.split.y));
                                    subVoxelSpaceElement.setAttribute("splitZ", String.valueOf(subInput.voxelParameters.split.z));
                                    subVoxelSpaceElement.setAttribute("resolution", String.valueOf(subInput.voxelParameters.resolution));
                                    multiResInputElement.addContent(subVoxelSpaceElement);

                                    Element subOutputFileElement = new Element("output-file");
                                    subOutputFileElement.setAttribute(new Attribute("src",subInput.outputFile.getAbsolutePath()));
                                    multiResInputElement.addContent(subOutputFileElement);

                                    multiResInputsElement.addContent(multiResInputElement);

                                }

                                multiResElement.addContent(multiResInputsElement);

                                if(input.outputFileMultiRes != null){
                                    multiResElement.addContent(new Element("output-file").setAttribute("src", input.outputFileMultiRes.getAbsolutePath()));
                                }
                            }
                            
                            
                            inputElement.addContent(multiResElement);
                        }

                        inputsElement.addContent(inputElement);
                    }
                }

                processElement.addContent(inputsElement);
            }
            
            if(voxelParameters.isGenerateMultiBandRaster()){
                
                Element generateMultiBandRasterElement = new Element("multi-band-raster");
                generateMultiBandRasterElement.setAttribute("generate", String.valueOf(voxelParameters.isGenerateMultiBandRaster()));
                generateMultiBandRasterElement.setAttribute("discard_voxel_file_writing", String.valueOf(voxelParameters.isShortcutVoxelFileWriting()));
                
                generateMultiBandRasterElement.setAttribute("starting-height", String.valueOf(voxelParameters.getRasterStartingHeight()));
                generateMultiBandRasterElement.setAttribute("step", String.valueOf(voxelParameters.getRasterHeightStep()));
                generateMultiBandRasterElement.setAttribute("band-number", String.valueOf(voxelParameters.getRasterBandNumber()));
                generateMultiBandRasterElement.setAttribute("resolution", String.valueOf(voxelParameters.getRasterResolution()));
                
                processElement.addContent(generateMultiBandRasterElement);
            }
            
            
            if(processMode == ProcessMode.VOXELISATION_ALS){
                
                groundEnergyElement = new Element("ground-energy");
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
    
    
    
    @Override
    public void readConfiguration(File inputParametersFile){
                
        try {
            
            SAXBuilder sxb = new SAXBuilder();
            Document document = sxb.build(inputParametersFile);
            Element root = document.getRootElement();
            
            Element processElement = root.getChild("process");
            String mode = processElement.getAttributeValue("mode");
            String type = processElement.getAttributeValue("type");
            
            Element filesElement;
            Element limitsElement;
            Element trajectoryFileElement;
            
            switch(mode){
                
                case "voxelisation":
                case "multi-voxelisation":
                    
                    voxelParameters = new VoxelParameters();
                    
                    if(mode.equals("voxelisation")){
                        
                        inputFileElement = processElement.getChild("input_file");
                    
                        int inputTypeInteger = Integer.valueOf(inputFileElement.getAttributeValue("type"));
                        inputType = getInputFileType(inputTypeInteger);
                        

                        inputFile = new File(inputFileElement.getAttributeValue("src"));
                    }
                    
                    switch(type){
                        case "ALS":
                            if(mode.equals("multi-voxelisation")){
                                processMode = ProcessMode.MULTI_VOXELISATION_ALS_AND_MULTI_RES;
                            }else{
                                processMode = ProcessMode.VOXELISATION_ALS;
                            }
                            
                            trajectoryFileElement = processElement.getChild("trajectory");
                            trajectoryFile = new File(trajectoryFileElement.getAttributeValue("src"));
                            
                            break;
                        case "TLS":
                            processMode = ProcessMode.VOXELISATION_TLS;
                            break;
                    }
                    
                    if(mode.equals("voxelisation")){
                        
                        outputFileElement = processElement.getChild("output_file");
                        outputFile = new File(outputFileElement.getAttributeValue("src"));

                        voxelSpaceElement = processElement.getChild("voxelspace");

                        
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
                    }
                    
                    
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
                    
                    Element pointcloudFiltersElement = processElement.getChild("pointcloud-filters");
                    
                    if(pointcloudFiltersElement != null){
                        boolean usePointCloudFilter = Boolean.valueOf(pointcloudFiltersElement.getAttributeValue("enabled"));
                        voxelParameters.setUsePointCloudFilter(usePointCloudFilter);
                        if(usePointCloudFilter){
                            
                            List<Element> childrens = pointcloudFiltersElement.getChildren("pointcloud-filter");
                            
                            if(childrens != null){
                                List<PointcloudFilter> pointcloudFilters = new ArrayList<>();
                                for(Element e : childrens){
                                    
                                    boolean keep;
                                    String operationType = e.getAttributeValue("operation-type");
                                    if(operationType.equals("Keep")){
                                        keep = true;
                                    }else{
                                        keep = false;
                                    }
                                    pointcloudFilters.add(new PointcloudFilter(new File(e.getAttributeValue("src")), 
                                            Float.valueOf(e.getAttributeValue("error-margin")), keep));
                                }
                                
                                voxelParameters.setPointcloudFilters(pointcloudFilters);
                                
                            }
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
                        List<Element> limitChildrensElement = limitsElement.getChildren("limit");
                        
                        if(limitChildrensElement != null){
                            
                            if(limitChildrensElement.size() > 0){
                                voxelParameters.setMaxPAD(Float.valueOf(limitChildrensElement.get(0).getAttributeValue("max")));
                            }
                            
                            if(limitChildrensElement.size() >= 6){
                                multiResPadMax = new float[5];
                                multiResPadMax[0] = Float.valueOf(limitChildrensElement.get(1).getAttributeValue("max"));
                                multiResPadMax[1] = Float.valueOf(limitChildrensElement.get(2).getAttributeValue("max"));
                                multiResPadMax[2] = Float.valueOf(limitChildrensElement.get(3).getAttributeValue("max"));
                                multiResPadMax[3] = Float.valueOf(limitChildrensElement.get(4).getAttributeValue("max"));
                                multiResPadMax[4] = Float.valueOf(limitChildrensElement.get(5).getAttributeValue("max"));

                            }
                        }
                        
                    }
                    
                    
                    Element filtersElement = processElement.getChild("filters");
                    filters = new ArrayList<>();
                    
                    if(filtersElement != null){
                        
                        Element shotFiltersElement = filtersElement.getChild("shot-filters");
                        
                        if(shotFiltersElement != null){                            
                            
                            List<Element> childrensFilter = shotFiltersElement.getChildren("filter");

                            if(childrensFilter != null){

                                

                                for(Element e : childrensFilter){

                                    String variable = e.getAttributeValue("variable");
                                    String inequality = e.getAttributeValue("inequality");
                                    String value = e.getAttributeValue("value");

                                    filters.add(new Filter(variable, Float.valueOf(value), Filter.getConditionFromString(inequality)));
                                }
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
                                groundEnergyElement = processElement.getChild("ground-energy");
                                if(groundEnergyElement != null){
                                    voxelParameters.setCalculateGroundEnergy(Boolean.valueOf(groundEnergyElement.getAttributeValue("generate")));

                                    if(mode.equals("voxelisation")){
                                        if(voxelParameters.isCalculateGroundEnergy()){
                                            voxelParameters.setGroundEnergyFileFormat(Short.valueOf(groundEnergyElement.getAttributeValue("type")));
                                            voxelParameters.setGroundEnergyFile(new File(groundEnergyElement.getAttributeValue("src")));
                                        }
                                    }
                                }
                            }catch(Exception e){
                                logger.warn("Parameters are missing");
                            }
                            
                            
                            break;
                    }
                    
                    
                    
                    if(mode.equals("multi-voxelisation")){
                        
                        Element inputsElement = processElement.getChild("inputs");
                        
                        if(inputsElement != null){
                            List<Element> childrens = inputsElement.getChildren("input");
                            
                            if(childrens != null){
                                
                                multiProcessInputs = new ArrayList<>();
                                
                                int count = 0;
                                
                                for(Element child : childrens){
                                    
                                    subVoxelSpaceElement = child.getChild("voxelspace");
                                    
                                    VoxelParameters multiProcessVoxelParameters = null;
                                    
                                    if(subVoxelSpaceElement != null){
                                        
                                        multiProcessVoxelParameters = new VoxelParameters();
                                        double resolution = Double.valueOf(subVoxelSpaceElement.getAttributeValue("resolution"));
                                        
                                        multiProcessVoxelParameters.setBottomCorner(new Point3d(
                                                        Double.valueOf(subVoxelSpaceElement.getAttributeValue("xmin")), 
                                                        Double.valueOf(subVoxelSpaceElement.getAttributeValue("ymin")), 
                                                        Double.valueOf(subVoxelSpaceElement.getAttributeValue("zmin"))));

                                        multiProcessVoxelParameters.setTopCorner(new Point3d(
                                                            Double.valueOf(subVoxelSpaceElement.getAttributeValue("xmax")), 
                                                            Double.valueOf(subVoxelSpaceElement.getAttributeValue("ymax")), 
                                                            Double.valueOf(subVoxelSpaceElement.getAttributeValue("zmax"))));

                                        multiProcessVoxelParameters.setSplit(new Point3i(
                                                            Integer.valueOf(subVoxelSpaceElement.getAttributeValue("splitX")), 
                                                            Integer.valueOf(subVoxelSpaceElement.getAttributeValue("splitY")), 
                                                            Integer.valueOf(subVoxelSpaceElement.getAttributeValue("splitZ"))));

                                        
                                        multiProcessVoxelParameters.setResolution(resolution);
                                    }
                                    
                                    File inputFileChild = null;
                                    Element inputFileChildElement = child.getChild("input_file");
                                    
                                    if(inputFileChildElement != null){
                                        
                                        if(count == 0){
                                            inputType = getInputFileType(Integer.valueOf(inputFileChildElement.getAttributeValue("type")));
                                        }
                                        inputFileChild = new File(inputFileChildElement.getAttributeValue("src"));
                                    }
                                    
                                    File dtmFileChild = null;
                                    Element dtmFileElement = child.getChild("dtm-filter");
                                    
                                    if(dtmFileElement != null){
                                        
                                        dtmFileChild = new File(dtmFileElement.getAttributeValue("src"));
                                    }
                                    
                                    
                                    File outputFileChild = null;
                                    Element ouputFileChildElement = child.getChild("output_file");
                                    
                                    if(ouputFileChildElement != null){
                                        
                                        outputFileChild = new File(ouputFileChildElement.getAttributeValue("src"));
                                    }
                                    
                                    List<Input> multiResInputs = null;
                                    Element multiResElement = child.getChild("multi-res");
                                    File multiResOutputFile = null;
                                    
                                    if(multiResElement != null){
                                        
                                        boolean multiResEnabled = Boolean.valueOf(multiResElement.getAttributeValue("enabled"));
                                        
                                        if(multiResEnabled){
                                            multiResInputs = new ArrayList<>();
                                            List<Element> multiResInputsElement = multiResElement.getChild("inputs").getChildren("input");
                                            
                                            for(Element element : multiResInputsElement){
                                                
                                                Element subSubVoxelSpaceElement = element.getChild("voxelspace");
                                    
                                                VoxelParameters subVoxelParameters = null;

                                                if(subVoxelSpaceElement != null){
                                                    int splitX = Integer.valueOf(subSubVoxelSpaceElement.getAttributeValue("splitX"));
                                                    int splitY = Integer.valueOf(subSubVoxelSpaceElement.getAttributeValue("splitY"));
                                                    int splitZ = Integer.valueOf(subSubVoxelSpaceElement.getAttributeValue("splitZ"));
                                                    double resolution = Double.valueOf(subSubVoxelSpaceElement.getAttributeValue("resolution"));

                                                    subVoxelParameters = new VoxelParameters(null, null, new Point3i(splitX, splitY, splitZ));
                                                    subVoxelParameters.setResolution(resolution);
                                                }
                                                
                                                Element subSubOutputFileElement = element.getChild("output-file");
                                                
                                                File subOutputFile = null;
                                                if(subSubOutputFileElement != null){
                                                    subOutputFile = new File(subSubOutputFileElement.getAttributeValue("src"));
                                                }
                                                
                                                multiResInputs.add(new Input(subVoxelParameters, null, null, subOutputFile, null, null));
                                            }
                                            
                                            Element multiResOutputFileElement = multiResElement.getChild("output-file");
                                            
                                            if(multiResOutputFileElement != null){
                                                multiResOutputFile = new File(multiResOutputFileElement.getAttributeValue("src"));
                                            }
                                        }
                                    }
                                    
                                    multiProcessInputs.add(new Input(multiProcessVoxelParameters, inputFileChild, dtmFileChild, outputFileChild, multiResInputs, multiResOutputFile));
                                    
                                }
                                
                                count++;
                            }
                            
                        }
                    }

                    break;
            }
            
            if(mode.equals("voxelisation") || mode.equals("multi-voxelisation")){
                
                Element generateMultiBandRasterElement = processElement.getChild("multi-band-raster");
                
                if(generateMultiBandRasterElement != null){
                    
                    boolean generateMultiBandRaster = Boolean.valueOf(generateMultiBandRasterElement.getAttributeValue("generate"));
                    voxelParameters.setGenerateMultiBandRaster(generateMultiBandRaster);
                    
                    if(generateMultiBandRaster){
                        voxelParameters.setShortcutVoxelFileWriting(Boolean.valueOf(generateMultiBandRasterElement.getAttributeValue("discard_voxel_file_writing")));
                        voxelParameters.setRasterStartingHeight(Float.valueOf(generateMultiBandRasterElement.getAttributeValue("starting-height")));
                        voxelParameters.setRasterHeightStep(Float.valueOf(generateMultiBandRasterElement.getAttributeValue("step")));
                        voxelParameters.setRasterBandNumber(Integer.valueOf(generateMultiBandRasterElement.getAttributeValue("band-number")));
                        voxelParameters.setRasterResolution(Integer.valueOf(generateMultiBandRasterElement.getAttributeValue("resolution")));
                    }
                }
            }
            
            Element formulaElement = processElement.getChild("formula");
                    
            if(formulaElement != null){
                Element transmittanceElement = formulaElement.getChild("transmittance");
                if(transmittanceElement != null){
                    voxelParameters.setTransmittanceMode(Integer.valueOf(transmittanceElement.getAttributeValue("mode")));
                }
            }
            
        } catch (JDOMException | IOException ex) {
            logger.error("Cannot read configuration file", ex);
        }

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

    public boolean isMultiResUseDefaultMaxPad() {
        return multiResUseDefaultMaxPad;
    }

    public void setMultiResUseDefaultMaxPad(boolean multiResUseDefaultMaxPad) {
        this.multiResUseDefaultMaxPad = multiResUseDefaultMaxPad;
    }

    public List<Input> getMultiProcessInputs() {
        return multiProcessInputs;
    }

    public void setMultiProcessInputs(List<Input> multiProcessInputs) {
        this.multiProcessInputs = multiProcessInputs;
    }    

    public boolean isCorrectNaNs() {
        return correctNaNs;
    }

    public void setCorrectNaNs(boolean correctNaNs) {
        this.correctNaNs = correctNaNs;
    }

    public List<Integer> getClassifiedPointsToDiscard() {
        return classifiedPointsToDiscard;
    }

    public void setClassifiedPointsToDiscard(List<Integer> classifiedPointsToDiscard) {
        this.classifiedPointsToDiscard = classifiedPointsToDiscard;
    }
    
}
