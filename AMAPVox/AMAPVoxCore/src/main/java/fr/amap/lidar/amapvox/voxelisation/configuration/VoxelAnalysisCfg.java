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
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.commons.Configuration;
import fr.amap.commons.util.filter.FloatFilter;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.lidar.amapvox.commons.LADParams;
import fr.amap.lidar.amapvox.commons.LeafAngleDistribution.Type;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.EchoFilter;
import fr.amap.lidar.amapvox.voxelisation.LaserSpecification;
import fr.amap.lidar.amapvox.voxelisation.ShotAttributeFilter;
import fr.amap.lidar.amapvox.voxelisation.ShotDecimationFilter;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoFilterByFileParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightByFileParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightByRankParams;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 *
 * @author calcul
 */
public class VoxelAnalysisCfg extends Configuration {

    public enum VoxelsFormat {

        NONE(0),
        VOXEL(1),
        RASTER(2);

        private final int format;

        private VoxelsFormat(int format) {
            this.format = format;
        }

        public int getFormat() {
            return format;
        }

    }

    protected final static Logger LOGGER = Logger.getLogger(VoxelAnalysisCfg.class);

    protected File inputFile;
    protected File outputFile;
    protected VoxelsFormat voxelsFormat = VoxelsFormat.VOXEL;
    protected boolean usePopMatrix;
    protected boolean useSopMatrix;
    protected boolean useVopMatrix;
    protected Matrix4d popMatrix;
    protected Matrix4d sopMatrix;
    protected Matrix4d vopMatrix;
    protected VoxelParameters voxelParameters;
    protected List<Filter<Shot>> shotFilters;
    protected List<FloatFilter> echoFilters;

    protected EchoFilter echoFilter;

    protected boolean exportShotSegment;

    protected Element limitsElement;
    protected Element filtersElement;
    protected Element echoFilteringElement;

    @Override
    public void readConfiguration(File inputParametersFile) throws Exception {

        initDocument(inputParametersFile);

        voxelParameters = new VoxelParameters();

        Element inputFileElement = processElement.getChild("input_file");

        if (inputFileElement != null) {
            int inputTypeInteger = Integer.valueOf(inputFileElement.getAttributeValue("type"));
            inputType = getInputFileType(inputTypeInteger);
            inputFile = new File(inputFileElement.getAttributeValue("src"));
        } else {
            LOGGER.warn("Cannot find input_file element");
        }

        Element outputFileElement = processElement.getChild("output_file");
        if (outputFileElement != null) {
            outputFile = new File(outputFileElement.getAttributeValue("src"));
            String formatStr = outputFileElement.getAttributeValue("format");
            if (formatStr != null) {
                int format = Integer.valueOf(formatStr);
                switch (format) {
                    case 0:
                        voxelsFormat = VoxelsFormat.NONE;
                        break;
                    case 1:
                        voxelsFormat = VoxelsFormat.VOXEL;
                        break;
                    case 2:
                        voxelsFormat = VoxelsFormat.RASTER;
                        break;
                }
            }
        } else {
            LOGGER.warn("Cannot find output_file element");
        }

        Element voxelSpaceElement = processElement.getChild("voxelspace");

        if (voxelSpaceElement != null) {
            voxelParameters.infos.setMinCorner(new Point3d(
                    Double.valueOf(voxelSpaceElement.getAttributeValue("xmin")),
                    Double.valueOf(voxelSpaceElement.getAttributeValue("ymin")),
                    Double.valueOf(voxelSpaceElement.getAttributeValue("zmin"))));

            voxelParameters.infos.setMaxCorner(new Point3d(
                    Double.valueOf(voxelSpaceElement.getAttributeValue("xmax")),
                    Double.valueOf(voxelSpaceElement.getAttributeValue("ymax")),
                    Double.valueOf(voxelSpaceElement.getAttributeValue("zmax"))));

            voxelParameters.infos.setSplit(new Point3i(
                    Integer.valueOf(voxelSpaceElement.getAttributeValue("splitX")),
                    Integer.valueOf(voxelSpaceElement.getAttributeValue("splitY")),
                    Integer.valueOf(voxelSpaceElement.getAttributeValue("splitZ"))));

            try {
                voxelParameters.infos.setResolution(Double.valueOf(voxelSpaceElement.getAttributeValue("resolution")));
            } catch (Exception e) {
            }

        } else {
            //logger.info("Cannot find bounding-box element");
        }

        Element ponderationElement = processElement.getChild("ponderation");

        if (ponderationElement != null) {

            // ponderation by rank
            if (Boolean.valueOf(ponderationElement.getAttributeValue("byrank"))) {
                Element matrixElement = ponderationElement.getChild("matrix");
                int rowNumber = 7;
                int colNumber = 7;
                String data = matrixElement.getText();
                String[] datas = data.split(" ");
                double[][] weightingData = new double[rowNumber][colNumber];

                int count = 0;
                for (int i = 0; i < weightingData.length; i++) {
                    for (int j = 0; j < weightingData[0].length; j++) {
                        weightingData[i][j] = Double.valueOf(datas[count]);
                        count++;
                    }
                }
                voxelParameters.setEchoesWeightByRankParams(new EchoesWeightByRankParams(weightingData));
            } else {
                voxelParameters.setEchoesWeightByRankParams(null);
            }

            // ponderation from external CSV file
            if (Boolean.valueOf(ponderationElement.getAttributeValue("byfile"))) {
                Element weightFileElement = ponderationElement.getChild("weight_file");
                String weightFile = weightFileElement.getAttributeValue("src");
                voxelParameters.setEchoesWeightByFileParams(new EchoesWeightByFileParams(weightFile));
            } else {
                voxelParameters.setEchoesWeightByFileParams(null);
            }
        }

        /**
         * *TRANSMITTANCE MODE**
         */
        Element transmittanceElement = processElement.getChild("transmittance");

        if (transmittanceElement != null) {

            voxelParameters.setTransmittanceMode(Integer.valueOf(transmittanceElement.getAttributeValue("mode")));
        }

        /**
         * *PATH LENGTH MODE**
         */
        Element pathLengthElement = processElement.getChild("path-length");

        if (pathLengthElement != null) {

            voxelParameters.setPathLengthMode(pathLengthElement.getAttributeValue("mode"));
        }

        Element dtmFilterElement = processElement.getChild("dtm-filter");

        if (dtmFilterElement != null) {
            boolean useDTM = Boolean.valueOf(dtmFilterElement.getAttributeValue("enabled"));
            voxelParameters.getDtmFilteringParams().setActivate(useDTM);
            if (useDTM) {
                voxelParameters.getDtmFilteringParams().setDtmFile(new File(dtmFilterElement.getAttributeValue("src")));
                voxelParameters.getDtmFilteringParams().setMinDTMDistance(Float.valueOf(dtmFilterElement.getAttributeValue("height-min")));

                String useVopAttribute = dtmFilterElement.getAttributeValue("use-vop");

                if (useVopAttribute != null) {
                    voxelParameters.getDtmFilteringParams().setUseVOPMatrix(Boolean.valueOf(useVopAttribute));
                } else { //old configuration file
                    voxelParameters.getDtmFilteringParams().setUseVOPMatrix(true);
                }
            }
        }

        Element pointcloudFiltersElement = processElement.getChild("pointcloud-filters");

        if (pointcloudFiltersElement != null) {
            boolean usePointCloudFilter = Boolean.valueOf(pointcloudFiltersElement.getAttributeValue("enabled"));
            voxelParameters.setUsePointCloudFilter(usePointCloudFilter);
            if (usePointCloudFilter) {

                List<Element> childrens = pointcloudFiltersElement.getChildren("pointcloud-filter");

                if (childrens != null) {
                    List<PointcloudFilter> pointcloudFilters = new ArrayList<>();
                    for (Element e : childrens) {

                        CSVFile file = new CSVFile(e.getAttributeValue("src"));

                        try {
                            String columnSeparator = e.getAttributeValue("column-separator");
                            String headerIndex = e.getAttributeValue("header-index");
                            String hasHeader = e.getAttributeValue("has-header");
                            String nbOfLinesToRead = e.getAttributeValue("nb-of-lines-to-read");
                            String nbOfLinesToSkip = e.getAttributeValue("nb-of-lines-to-skip");
                            String columnAssignment = e.getAttributeValue("column-assignment");

                            file.setColumnSeparator(columnSeparator);
                            file.setHeaderIndex(Long.valueOf(headerIndex));
                            file.setContainsHeader(Boolean.valueOf(hasHeader));
                            file.setNbOfLinesToRead(Long.valueOf(nbOfLinesToRead));
                            file.setNbOfLinesToSkip(Long.valueOf(nbOfLinesToSkip));

                            Map<String, Integer> colMap = new HashMap<>();
                            String[] split = columnAssignment.split(",");
                            for (String s : split) {
                                int indexOfSep = s.indexOf("=");
                                String key = s.substring(0, indexOfSep);
                                String value = s.substring(indexOfSep + 1, s.length());
                                colMap.put(key, Integer.valueOf(value));
                            }

                            file.setColumnAssignment(colMap);

                        } catch (Exception ex) {
                            LOGGER.warn("Old file element detected, keep default old read parameters.");
                        }

                        boolean keep;
                        String operationType = e.getAttributeValue("operation-type");
                        keep = operationType.equals("Keep");
                        pointcloudFilters.add(new PointcloudFilter(file, Float.valueOf(e.getAttributeValue("error-margin")), keep));
                    }

                    voxelParameters.setPointcloudFilters(pointcloudFilters);

                }
            }
        }

        Element transformationElement = processElement.getChild("transformation");

        if (transformationElement != null) {
            usePopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-pop"));
            useSopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-sop"));
            useVopMatrix = Boolean.valueOf(transformationElement.getAttributeValue("use-vop"));
            List<Element> matrixList = transformationElement.getChildren("matrix");
            for (Element e : matrixList) {

                String matrixType = e.getAttributeValue("type_id");
                Matrix4d mat = getMatrixFromData(e.getText());

                switch (matrixType) {
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

        if (limitsElement != null) {
            List<Element> limitChildrensElement = limitsElement.getChildren("limit");

            if (limitChildrensElement != null) {

                if (limitChildrensElement.size() > 0) {
                    voxelParameters.infos.setMaxPAD(Float.valueOf(limitChildrensElement.get(0).getAttributeValue("max")));
                }
            }

        }

        filtersElement = processElement.getChild("filters");
        shotFilters = new ArrayList<>();
        echoFilters = new ArrayList<>();

        if (filtersElement != null) {

            Element shotFiltersElement = filtersElement.getChild("shot-filters");

            if (shotFiltersElement != null) {

                List<Element> childrensFilter = shotFiltersElement.getChildren("filter");

                if (childrensFilter != null) {
                    for (Element e : childrensFilter) {
                        if (null != e.getAttribute("variable")) {
                            // shot attribute filter
                            String variable = e.getAttributeValue("variable");
                            String inequality = e.getAttributeValue("inequality");
                            String value = e.getAttributeValue("value");
                            shotFilters.add(new ShotAttributeFilter(new FloatFilter(variable, Float.valueOf(value), FloatFilter.getConditionFromString(inequality))));
                        } else if (null != e.getAttribute("decimation-factor")) {
                            // shot decimation
                            int decimationFactor = Integer.valueOf(e.getAttributeValue("decimation-factor"));
                            int offset = Integer.valueOf(e.getAttributeValue("offset"));
                            shotFilters.add(new ShotDecimationFilter(decimationFactor, offset));
                        }
                    }
                }
            }

            echoFilteringElement = filtersElement.getChild("echo-filters");

            if (echoFilteringElement != null) {

                List<Element> childrensFilter = echoFilteringElement.getChildren("filter");

                if (childrensFilter != null) {
                    for (Element e : childrensFilter) {
                        if (null != e.getAttribute("variable")) {
                            String variable = e.getAttributeValue("variable");
                            String inequality = e.getAttributeValue("inequality");
                            String value = e.getAttributeValue("value");
                            echoFilters.add(new FloatFilter(variable, Float.valueOf(value), FloatFilter.getConditionFromString(inequality)));
                        } else if (null != e.getAttribute("src")) {
                            String src = e.getAttributeValue("src");
                            boolean discard = e.getAttributeValue("behavior").equalsIgnoreCase("discard");
                            voxelParameters.setEchoFilterByFileParams(new EchoFilterByFileParams(src, discard));
                        }
                    }
                }
            }
        }

        Element laserSpecElement = processElement.getChild("laser-specification");

        if (laserSpecElement != null) {

            String laserSpecName = laserSpecElement.getAttributeValue("name");

            switch (laserSpecName) {
                case "LMS_Q560":
                case "DEFAULT_ALS":
                    voxelParameters.setLaserSpecification(LaserSpecification.LMS_Q560);
                    break;
                case "LEICA_SCANSTATION_C10":
                    voxelParameters.setLaserSpecification(LaserSpecification.LEICA_SCANSTATION_C10);
                    break;
                case "VZ_400":
                    voxelParameters.setLaserSpecification(LaserSpecification.VZ_400);
                    break;
                case "LEICA_SCANSTATION_P30_40":
                    voxelParameters.setLaserSpecification(LaserSpecification.LEICA_SCANSTATION_P30_40);
                    break;
                case "custom":
                    String beamDivergenceStr = laserSpecElement.getAttributeValue("beam-divergence");
                    String beamDiameterAtExitStr = laserSpecElement.getAttributeValue("beam-diameter-at-exit");

                    if (beamDivergenceStr != null && beamDiameterAtExitStr != null) {
                        voxelParameters.setLaserSpecification(new LaserSpecification(Double.valueOf(beamDiameterAtExitStr), Double.valueOf(beamDivergenceStr), "custom"));
                    }

                    break;
                default:
                    voxelParameters.setLaserSpecification(null);
            }
        }

        Element ladElement = processElement.getChild("leaf-angle-distribution");
        if (ladElement != null) {

            LADParams ladParameters = new LADParams();

            ladParameters.setLadEstimationMode(Integer.valueOf(ladElement.getAttributeValue("mode")));
            ladParameters.setLadType(Type.fromString(ladElement.getAttributeValue("type")));

            String alphaValue = ladElement.getAttributeValue("alpha");
            String betaValue = ladElement.getAttributeValue("beta");

            if (alphaValue != null) {
                ladParameters.setLadBetaFunctionAlphaParameter(Float.valueOf(alphaValue));
            }

            if (betaValue != null) {
                ladParameters.setLadBetaFunctionBetaParameter(Float.valueOf(betaValue));
            }

            voxelParameters.setLadParams(ladParameters);
        }

        Element exportShotSegmentElement = processElement.getChild("export-shot-segment");

        if (exportShotSegmentElement != null) {
            exportShotSegment = Boolean.valueOf(exportShotSegmentElement.getAttributeValue("enabled"));
        }

    }

    @Override
    public void writeConfiguration(File outputParametersFile, String buildVersion) throws Exception {

        if (inputFile != null) {
            Element inputFileElement = new Element("input_file");
            inputFileElement.setAttribute(new Attribute("type", String.valueOf(inputType.type)));
            inputFileElement.setAttribute(new Attribute("src", inputFile.getAbsolutePath()));
            processElement.addContent(inputFileElement);
        } else {
            LOGGER.info("Global input file ignored.");
        }

        Element outputFileElement = new Element("output_file");
        outputFileElement.setAttribute(new Attribute("src", outputFile.getAbsolutePath()));
        outputFileElement.setAttribute(new Attribute("format", String.valueOf(voxelsFormat.getFormat())));
        processElement.addContent(outputFileElement);

        if (voxelParameters != null && voxelParameters.infos.getMinCorner() != null && voxelParameters.infos.getMaxCorner() != null) {
            Element voxelSpaceElement = new Element("voxelspace");
            voxelSpaceElement.setAttribute("xmin", String.valueOf(voxelParameters.infos.getMinCorner().x));
            voxelSpaceElement.setAttribute("ymin", String.valueOf(voxelParameters.infos.getMinCorner().y));
            voxelSpaceElement.setAttribute("zmin", String.valueOf(voxelParameters.infos.getMinCorner().z));
            voxelSpaceElement.setAttribute("xmax", String.valueOf(voxelParameters.infos.getMaxCorner().x));
            voxelSpaceElement.setAttribute("ymax", String.valueOf(voxelParameters.infos.getMaxCorner().y));
            voxelSpaceElement.setAttribute("zmax", String.valueOf(voxelParameters.infos.getMaxCorner().z));
            voxelSpaceElement.setAttribute("splitX", String.valueOf(voxelParameters.infos.getSplit().x));
            voxelSpaceElement.setAttribute("splitY", String.valueOf(voxelParameters.infos.getSplit().y));
            voxelSpaceElement.setAttribute("splitZ", String.valueOf(voxelParameters.infos.getSplit().z));
            voxelSpaceElement.setAttribute("resolution", String.valueOf(voxelParameters.infos.getResolution()));
            processElement.addContent(voxelSpaceElement);

        } else {
            LOGGER.info("Global bounding-box ignored.");
        }

        /**
         * *PONDERATION**
         */
        Element ponderationElement = new Element("ponderation");
        // by rank
        ponderationElement.setAttribute(new Attribute("byrank", String.valueOf(null != voxelParameters.getEchoesWeightByRankParams())));
        if (null != voxelParameters.getEchoesWeightByRankParams()) {
            StringBuilder weightingDataString = new StringBuilder();
            double[][] weightingData = voxelParameters.getEchoesWeightByRankParams().getWeightingData();
            for (int i = 0; i < weightingData.length; i++) {
                for (int j = 0; j < weightingData[0].length; j++) {
                    weightingDataString.append((float) weightingData[i][j]).append(" ");
                }
            }
            Element matrixElement = createMatrixElement("ponderation", weightingDataString.toString().trim());
            ponderationElement.addContent(matrixElement);
        }
        // by file
        ponderationElement.setAttribute(new Attribute("byfile", String.valueOf(null != voxelParameters.getEchoesWeightByFileParams())));
        if (null != voxelParameters.getEchoesWeightByFileParams()) {
            Element weightFileElement = new Element("weight_file");
            weightFileElement.setAttribute("src", voxelParameters.getEchoesWeightByFileParams().getFile().getAbsolutePath());
            ponderationElement.addContent(weightFileElement);
        }

        processElement.addContent(ponderationElement);

        /**
         * *TRANSMITTANCE MODE**
         */
        Element transmittanceElement = new Element("transmittance");
        transmittanceElement.setAttribute(new Attribute("mode", String.valueOf(voxelParameters.getTransmittanceMode())));
        processElement.addContent(transmittanceElement);

        /**
         * *PATH-LENGTH MODE**
         */
        Element pathLengthElement = new Element("path-length");
        pathLengthElement.setAttribute(new Attribute("mode", voxelParameters.getPathLengthMode()));
        processElement.addContent(pathLengthElement);

        /**
         * *DTM FILTER**
         */
        Element dtmFilterElement = new Element("dtm-filter");
        dtmFilterElement.setAttribute(new Attribute("enabled", String.valueOf(voxelParameters.getDtmFilteringParams().useDTMCorrection())));
        if (voxelParameters.getDtmFilteringParams().useDTMCorrection()) {
            if (voxelParameters.getDtmFilteringParams().getDtmFile() != null) {
                dtmFilterElement.setAttribute(new Attribute("src", voxelParameters.getDtmFilteringParams().getDtmFile().getAbsolutePath()));
            }

            dtmFilterElement.setAttribute(new Attribute("height-min", String.valueOf(voxelParameters.getDtmFilteringParams().getMinDTMDistance())));
            dtmFilterElement.setAttribute(new Attribute("use-vop", String.valueOf(voxelParameters.getDtmFilteringParams().isUseVOPMatrix())));
        }

        processElement.addContent(dtmFilterElement);

        /**
         * *LASER SPECIFICATION**
         */
        Element laserSpecElement = new Element("laser-specification");
        laserSpecElement.setAttribute("name", voxelParameters.getLaserSpecification().getName());
        laserSpecElement.setAttribute("beam-diameter-at-exit", String.valueOf(voxelParameters.getLaserSpecification().getBeamDiameterAtExit()));
        laserSpecElement.setAttribute("beam-divergence", String.valueOf(voxelParameters.getLaserSpecification().getBeamDivergence()));

        processElement.addContent(laserSpecElement);

        Element pointcloudFiltersElement = new Element("pointcloud-filters");
        pointcloudFiltersElement.setAttribute(new Attribute("enabled", String.valueOf(voxelParameters.isUsePointCloudFilter())));

        if (voxelParameters.isUsePointCloudFilter()) {

            List<PointcloudFilter> pointcloudFilters = voxelParameters.getPointcloudFilters();

            if (pointcloudFilters != null) {

                for (PointcloudFilter filter : pointcloudFilters) {
                    Element pointcloudFilterElement = new Element("pointcloud-filter");
                    pointcloudFilterElement.setAttribute(new Attribute("src", filter.getPointcloudFile().getAbsolutePath()));
                    pointcloudFilterElement.setAttribute(new Attribute("error-margin", String.valueOf(filter.getPointcloudErrorMargin())));

                    String operationType;
                    if (filter.isKeep()) {
                        operationType = "Keep";
                    } else {
                        operationType = "Discard";
                    }

                    pointcloudFilterElement.setAttribute(new Attribute("operation-type", operationType));

                    pointcloudFilterElement.setAttribute(new Attribute("column-separator", filter.getPointcloudFile().getColumnSeparator()));
                    pointcloudFilterElement.setAttribute(new Attribute("header-index", String.valueOf(filter.getPointcloudFile().getHeaderIndex())));
                    pointcloudFilterElement.setAttribute(new Attribute("has-header", String.valueOf(filter.getPointcloudFile().containsHeader())));
                    pointcloudFilterElement.setAttribute(new Attribute("nb-of-lines-to-read", String.valueOf(filter.getPointcloudFile().getNbOfLinesToRead())));
                    pointcloudFilterElement.setAttribute(new Attribute("nb-of-lines-to-skip", String.valueOf(filter.getPointcloudFile().getNbOfLinesToSkip())));

                    Map<String, Integer> columnAssignment = filter.getPointcloudFile().getColumnAssignment();
                    Iterator<Map.Entry<String, Integer>> iterator = columnAssignment.entrySet().iterator();
                    String colAssignment = new String();

                    while (iterator.hasNext()) {
                        Map.Entry<String, Integer> entry = iterator.next();
                        colAssignment += entry.getKey() + "=" + entry.getValue() + ",";
                    }

                    pointcloudFilterElement.setAttribute(new Attribute("column-assignment", colAssignment));

                    pointcloudFiltersElement.addContent(pointcloudFilterElement);
                }
            }

        }

        processElement.addContent(pointcloudFiltersElement);

        if (echoFilters != null) {

        }

        /**
         * *TRANSFORMATION**
         */
        Element transformationElement = new Element("transformation");

        transformationElement.setAttribute(new Attribute("use-pop", String.valueOf(usePopMatrix)));
        transformationElement.setAttribute(new Attribute("use-sop", String.valueOf(useSopMatrix)));
        transformationElement.setAttribute(new Attribute("use-vop", String.valueOf(useVopMatrix)));

        if (usePopMatrix && popMatrix != null) {
            Element matrixPopElement = createMatrixElement("pop", popMatrix.toString());
            transformationElement.addContent(matrixPopElement);
        }

        if (useSopMatrix && sopMatrix != null) {
            Element matrixSopElement = createMatrixElement("sop", sopMatrix.toString());
            transformationElement.addContent(matrixSopElement);
        }

        if (useVopMatrix && vopMatrix != null) {
            Element matrixVopElement = createMatrixElement("vop", vopMatrix.toString());
            transformationElement.addContent(matrixVopElement);
        }

        processElement.addContent(transformationElement);

        /**
         * *LIMITS**
         */
        limitsElement = new Element("limits");
        Element limitElement = new Element("limit");
        limitElement.setAttribute("name", "PAD");
        limitElement.setAttribute("min", "");
        limitElement.setAttribute("max", String.valueOf(voxelParameters.infos.getMaxPAD()));
        limitsElement.addContent(limitElement);

        processElement.addContent(limitsElement);

        filtersElement = new Element("filters");

        if (shotFilters != null && !shotFilters.isEmpty()) {

            Element shotFilterElement = new Element("shot-filters");

            for (Filter filter : shotFilters) {
                Element filterElement = new Element("filter");
                if (filter instanceof ShotAttributeFilter) {
                    FloatFilter f = ((ShotAttributeFilter) filter).getFilter();
                    filterElement.setAttribute("variable", f.getVariable());
                    filterElement.setAttribute("inequality", f.getConditionString());
                    filterElement.setAttribute("value", String.valueOf(f.getValue()));
                } else if (filter instanceof ShotDecimationFilter) {
                    ShotDecimationFilter f = (ShotDecimationFilter) filter;
                    filterElement.setAttribute("decimation-factor", String.valueOf(f.getDecimationFactor()));
                    filterElement.setAttribute("offset", String.valueOf(f.getOffset()));
                }
                shotFilterElement.addContent(filterElement);
            }

            filtersElement.addContent(shotFilterElement);
        }

        Element echoFilterElement = new Element("echo-filters");

        if (echoFilters != null && !echoFilters.isEmpty()) {
            for (FloatFilter f : echoFilters) {
                Element filterElement = new Element("filter");
                filterElement.setAttribute("variable", f.getVariable());
                filterElement.setAttribute("inequality", f.getConditionString());
                filterElement.setAttribute("value", String.valueOf(f.getValue()));
                echoFilterElement.addContent(filterElement);
            }
        }

        if (null != voxelParameters.getEchoFilterByFileParams()) {
            EchoFilterByFileParams filter = voxelParameters.getEchoFilterByFileParams();
            Element filterElement = new Element("filter");
            filterElement.setAttribute("src", filter.getFile().getAbsolutePath());
            filterElement.setAttribute("behavior", filter.discardEchoes() ? "discard" : "retain");
            echoFilterElement.addContent(filterElement);
        }

        if (echoFilterElement.getContentSize() > 0) {
            filtersElement.addContent(echoFilterElement);
        }

        processElement.addContent(filtersElement);

        LADParams ladParameters = voxelParameters.getLadParams();

        if (ladParameters != null) {

            Element ladElement = new Element("leaf-angle-distribution");
            ladElement.setAttribute("mode", String.valueOf(ladParameters.getLadEstimationMode()));
            ladElement.setAttribute("type", ladParameters.getLadType().toString());

            if (ladParameters.getLadType() == Type.TWO_PARAMETER_BETA || ladParameters.getLadType() == Type.ELLIPSOIDAL) {
                ladElement.setAttribute("alpha", String.valueOf(ladParameters.getLadBetaFunctionAlphaParameter()));

                if (ladParameters.getLadType() == Type.TWO_PARAMETER_BETA) {
                    ladElement.setAttribute("beta", String.valueOf(ladParameters.getLadBetaFunctionBetaParameter()));
                }
            }

            processElement.addContent(ladElement);
        }

        Element exportShotSegmentElement = new Element("export-shot-segment");
        exportShotSegmentElement.setAttribute("enabled", String.valueOf(exportShotSegment));
        processElement.addContent(exportShotSegmentElement);
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

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public void setOutputFile(File outputFile, VoxelsFormat voxelsFormat) {
        this.outputFile = outputFile;
        this.voxelsFormat = voxelsFormat;
    }

    public VoxelsFormat getVoxelsFormat() {
        return voxelsFormat;
    }

    public void setVoxelsFormat(VoxelsFormat voxelsFormat) {
        this.voxelsFormat = voxelsFormat;
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

    public List<Filter<Shot>> getShotFilters() {
        return shotFilters;
    }

    public void setEchoFilters(List<FloatFilter> filters) {
        this.echoFilters = filters;
    }

    public void setShotFilters(List<Filter<Shot>> shotFilters) {
        this.shotFilters = shotFilters;
    }

    public List<FloatFilter> getEchoFilters() {
        return echoFilters;
    }

    public EchoFilter getEchoFilter() {
        return echoFilter;
    }

    public void setEchoFilter(EchoFilter echoFilter) {
        this.echoFilter = echoFilter;
    }

    public boolean isExportShotSegment() {
        return exportShotSegment;
    }

    public void setExportShotSegment(boolean exportShotSegment) {
        this.exportShotSegment = exportShotSegment;
    }

}
