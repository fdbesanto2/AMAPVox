/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

import fr.amap.commons.util.vegetation.LADParams;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import java.io.File;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 * This class defines the parameters of the voxelization process.
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelParameters {
        
    //voxel space parameters
    public final VoxelSpaceInfos infos;
    /*public Point3d bottomCorner;
    public Point3d topCorner;
    public Point3i split;
    public double resolution;
    private float maxPAD = 5;
    private boolean TLS;*/
    private int transmittanceMode = 1;
    private String pathLengthMode = "A";
    
    //echoes filtering
    private List<PointcloudFilter> pointcloudFilters;
    private boolean usePointCloudFilter;
    
    private boolean mergingAfter;
    private File mergedFile;
    
    private NaNsCorrectionParams naNsCorrectionParams;
    private DTMFilteringParams dtmFilteringParams;
    private EchoesWeightParams echoesWeightParams;
    private GroundEnergyParams groundEnergyParams;
    private RasterParams rasterParams;
    private LADParams ladParams;
    
    private VoxelAnalysis.LaserSpecification laserSpecification = null;    
    
    public VoxelParameters() {
        
        infos = new VoxelSpaceInfos();
        infos.setType(VoxelSpaceInfos.Type.ALS);
        ladParams = new LADParams();
        echoesWeightParams = new EchoesWeightParams();
        dtmFilteringParams = new DTMFilteringParams();
        naNsCorrectionParams = new NaNsCorrectionParams(false);
    }
    
    public VoxelParameters(Point3d bottomCorner, Point3d topCorner, Point3i split) {
        
        /*this.bottomCorner = bottomCorner;
        this.topCorner = topCorner;
        this.split = split;*/
        infos = new VoxelSpaceInfos(bottomCorner, topCorner, split);
        
        ladParams = new LADParams();
        echoesWeightParams = new EchoesWeightParams();
        dtmFilteringParams = new DTMFilteringParams();
        naNsCorrectionParams = new NaNsCorrectionParams(false);
    }
    
    /**
     *
     * @param bottomCorner bottom left corner of the bounding box
     * @param topCorner top right corner of the bounding box
     * @param split number of voxels for each axis (depends of resolution)
     * @param resolution voxel size (depends of splitting)
     * @param maxPAD Maximum Plant Area Density
     * @param ladParams Leaf Angle Distribution parameters
     * @param echoesWeightParams Shot's Echoes weighting parameters
     * @param laserSpecification Lidar equipment specification
     * @param naNsCorrectionParams Parameters for the correction of non sampled voxels 
     * @param dtmFilteringParams DTM filtering parameters
     * @param TLS if true, the lidar is a Terrestrial Laser Scanner, if false, an Airborne Laser Scanner
     */
    public VoxelParameters(Point3d bottomCorner, Point3d topCorner, Point3i split, float resolution,
            float maxPAD,
            LADParams ladParams,
            EchoesWeightParams echoesWeightParams,
            VoxelAnalysis.LaserSpecification laserSpecification,
            NaNsCorrectionParams naNsCorrectionParams,
            DTMFilteringParams dtmFilteringParams,
            boolean TLS) {

        infos = new VoxelSpaceInfos(bottomCorner, topCorner, split);
        infos.setResolution(resolution);
        infos.setMaxPAD(maxPAD);
        
        infos.setType(TLS ? VoxelSpaceInfos.Type.TLS:VoxelSpaceInfos.Type.ALS);
        
        //check all parameters, if null set to default
        if (ladParams == null) {
            ladParams = new LADParams();
        }

        this.ladParams = ladParams;
        
        if (echoesWeightParams == null) {
            echoesWeightParams = new EchoesWeightParams();
        }
        this.echoesWeightParams = echoesWeightParams;
        
        if(laserSpecification == null){
            if(TLS){
                laserSpecification = VoxelAnalysis.LaserSpecification.VZ_400;
            }else{
                laserSpecification = VoxelAnalysis.LaserSpecification.DEFAULT_ALS;
            }
        }
        
        this.laserSpecification = laserSpecification;
        
        if(naNsCorrectionParams == null){
            naNsCorrectionParams = new NaNsCorrectionParams(false);
        }
        this.naNsCorrectionParams = naNsCorrectionParams;
        
        if(dtmFilteringParams == null){
            dtmFilteringParams = new DTMFilteringParams();
        }
        this.dtmFilteringParams = dtmFilteringParams;
        
        this.infos.setType(VoxelSpaceInfos.Type.TLS);
        //this.TLS = TLS;
    }

    public boolean isMergingAfter() {
        return mergingAfter;
    }

    public void setMergingAfter(boolean mergingAfter) {
        this.mergingAfter = mergingAfter;
    }

    public File getMergedFile() {
        return mergedFile;
    }

    public void setMergedFile(File mergedFile) {
        this.mergedFile = mergedFile;
    }

    public boolean isUsePointCloudFilter() {
        return usePointCloudFilter;
    }

    public void setUsePointCloudFilter(boolean usePointCloudFilter) {
        this.usePointCloudFilter = usePointCloudFilter;
    }

    public List<PointcloudFilter> getPointcloudFilters() {
        return pointcloudFilters;
    }

    public void setPointcloudFilters(List<PointcloudFilter> pointcloudFilters) {
        this.pointcloudFilters = pointcloudFilters;
    }

    public VoxelAnalysis.LaserSpecification getLaserSpecification() {
        return laserSpecification;
    }

    public void setLaserSpecification(VoxelAnalysis.LaserSpecification laserSpecification) {
        this.laserSpecification = laserSpecification;
    }

    /**
     * 
     * @return Multi-band layer PAD raster parameters
     */
    public RasterParams getRasterParams() {
        return rasterParams;
    }

    /**
     * 
     * @param rasterParams Multi-band layer PAD raster parameters
     */
    public void setRasterParams(RasterParams rasterParams) {
        this.rasterParams = rasterParams;
    }

    /**
     * 
     * @return Leaf Angle Distribution parameters
     */
    public LADParams getLadParams() {
        return ladParams;
    }

    /**
     * 
     * @param ladParams Leaf Angle Distribution parameters
     */
    public void setLadParams(LADParams ladParams) {
        this.ladParams = ladParams;
        
        infos.setLadType(ladParams.getLadType());
        infos.setLadParams(new double[]{ladParams.getLadBetaFunctionAlphaParameter(), ladParams.getLadBetaFunctionBetaParameter()});
    }

    /**
     * 
     * @return Ground-energy map generation parameters (ALS only)
     */
    public GroundEnergyParams getGroundEnergyParams() {
        return groundEnergyParams;
    }

    /**
     * 
     * @param groundEnergyParams Ground-energy map generation parameters (ALS only)
     */
    public void setGroundEnergyParams(GroundEnergyParams groundEnergyParams) {
        this.groundEnergyParams = groundEnergyParams;
    }   

    /**
     * 
     * @return Echoes weigting parameters
     */
    public EchoesWeightParams getEchoesWeightParams() {
        return echoesWeightParams;
    }

    /**
     * 
     * @param echoesWeightParams Echoes weigting parameters
     */
    public void setEchoesWeightParams(EchoesWeightParams echoesWeightParams) {
        this.echoesWeightParams = echoesWeightParams;
    }

    /**
     * 
     * @return The dtm filtering parameter
     */
    public DTMFilteringParams getDtmFilteringParams() {
        return dtmFilteringParams;
    }

    /**
     * 
     * @param dtmFilteringParams A dtm filtering parameter
     */
    public void setDtmFilteringParams(DTMFilteringParams dtmFilteringParams) {
        this.dtmFilteringParams = dtmFilteringParams;
    }

    public NaNsCorrectionParams getNaNsCorrectionParams() {
        return naNsCorrectionParams;
    }

    public void setNaNsCorrectionParams(NaNsCorrectionParams naNsCorrectionParams) {
        this.naNsCorrectionParams = naNsCorrectionParams;
    }

    public int getTransmittanceMode() {
        return transmittanceMode;
    }

    public void setTransmittanceMode(int transmittanceMode) {
        this.transmittanceMode = transmittanceMode;
    }

    public String getPathLengthMode() {
        return pathLengthMode;
    }

    public void setPathLengthMode(String pathLengthMode) {
        this.pathLengthMode = pathLengthMode;
    }
    
}
