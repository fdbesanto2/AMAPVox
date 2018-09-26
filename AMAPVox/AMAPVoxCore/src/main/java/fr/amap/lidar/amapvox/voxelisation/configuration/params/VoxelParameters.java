/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.configuration.params;

import fr.amap.lidar.amapvox.commons.LADParams;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.LaserSpecification;
import java.io.File;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 * This class defines the parameters of the voxelization process.
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelParameters {

    //voxel space parameters
    public final VoxelSpaceInfos infos;

    // voxelisation algorithm parameters
    private boolean constantBeamSection = false;
    private boolean rayPonderationEnabled = true;

    private boolean mergingAfter;
    private File mergedFile;

    private NaNsCorrectionParams naNsCorrectionParams;
    private DTMFilteringParams dtmFilteringParams;
    private EchoesWeightByRankParams echoesWeightByRankParams;
    private EchoesWeightByFileParams echoesWeightByFileParams;
    private GroundEnergyParams groundEnergyParams;
    private LADParams ladParams;

    private LaserSpecification laserSpecification = null;

    public static class Builder {

        //voxel space parameters
        private final VoxelSpaceInfos infos = new VoxelSpaceInfos();

        // voxelisation algorithm parameters
        private final boolean constantBeamSection = false;
        private final boolean rayPonderationEnabled = true;

        private boolean mergingAfter = false;
        private File mergedFile = null;

        private NaNsCorrectionParams naNsCorrectionParams = new NaNsCorrectionParams(false);
        private DTMFilteringParams dtmFilteringParams = new DTMFilteringParams();
        private EchoesWeightByRankParams echoesWeightByRankParams;
        private GroundEnergyParams groundEnergyParams = new GroundEnergyParams();
        private LADParams ladParams = new LADParams();

        private LaserSpecification laserSpecification = LaserSpecification.LMS_Q560;

        public Builder voxelSpace(Point3d bottomCorner, Point3d topCorner, float resolution, VoxelSpaceInfos.Type type) {

            this.infos.setMinCorner(bottomCorner);
            this.infos.setMaxCorner(topCorner);
            this.infos.setResolution((double) resolution);
            this.infos.setType(type);
            return this;
        }

        public Builder echoesWeightByRankParams(EchoesWeightByRankParams echoesWeightByRankParams) {

            this.echoesWeightByRankParams = echoesWeightByRankParams;
            return this;
        }

        public Builder naNsCorrectionParams(NaNsCorrectionParams naNsCorrectionParams) {

            this.naNsCorrectionParams = naNsCorrectionParams;
            return this;
        }

        public Builder groundEnergyParams(GroundEnergyParams groundEnergyParams) {

            this.groundEnergyParams = groundEnergyParams;
            return this;
        }

        public Builder dtmFilteringParams(DTMFilteringParams dtmFilteringParams) {

            this.dtmFilteringParams = dtmFilteringParams;
            return this;
        }

        public Builder ladParams(LADParams ladParams) {

            this.ladParams = ladParams;
            return this;
        }

        public Builder laserSpecification(LaserSpecification laserSpecification) {

            this.laserSpecification = laserSpecification;
            return this;
        }

        public Builder padMAX(float padMAX) {

            this.infos.setMaxPAD(padMAX);
            return this;
        }

        public VoxelParameters build() {

            return new VoxelParameters(this);
        }
    }

    public VoxelParameters(Builder builder) {

        this.dtmFilteringParams = builder.dtmFilteringParams;
        this.echoesWeightByRankParams = builder.echoesWeightByRankParams;
        this.groundEnergyParams = builder.groundEnergyParams;
        this.infos = builder.infos;
        this.ladParams = builder.ladParams;
        this.laserSpecification = builder.laserSpecification;
        this.mergedFile = builder.mergedFile;
        this.mergingAfter = builder.mergingAfter;
        this.naNsCorrectionParams = builder.naNsCorrectionParams;
        this.constantBeamSection = builder.constantBeamSection;
        this.rayPonderationEnabled = builder.rayPonderationEnabled;
    }

    public VoxelParameters() {

        infos = new VoxelSpaceInfos();
        infos.setType(VoxelSpaceInfos.Type.ALS);
        ladParams = new LADParams();
        //echoesWeightByRankParams = new EchoesWeightByRankParams();
        dtmFilteringParams = new DTMFilteringParams();
        naNsCorrectionParams = new NaNsCorrectionParams(false);
    }

    public VoxelParameters(Point3d bottomCorner, Point3d topCorner, Point3i split) {

        /*this.bottomCorner = bottomCorner;
        this.topCorner = topCorner;
        this.split = split;*/
        infos = new VoxelSpaceInfos(bottomCorner, topCorner, split);

        ladParams = new LADParams();
        //echoesWeightByRankParams = new EchoesWeightByRankParams();
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
     * @param echoesWeightByRankParams Shot's Echoes weighting parameters
     * @param laserSpecification Lidar equipment specification
     * @param naNsCorrectionParams Parameters for the correction of non sampled
     * voxels
     * @param dtmFilteringParams DTM filtering parameters
     * @param TLS if true, the lidar is a Terrestrial Laser Scanner, if false,
     * an Airborne Laser Scanner
     */
    public VoxelParameters(Point3d bottomCorner, Point3d topCorner, Point3i split, float resolution,
            float maxPAD,
            LADParams ladParams,
            EchoesWeightByRankParams echoesWeightByRankParams,
            LaserSpecification laserSpecification,
            NaNsCorrectionParams naNsCorrectionParams,
            DTMFilteringParams dtmFilteringParams,
            boolean TLS) {

        infos = new VoxelSpaceInfos(bottomCorner, topCorner, split);
        infos.setResolution(resolution);
        infos.setMaxPAD(maxPAD);

        infos.setType(TLS ? VoxelSpaceInfos.Type.TLS : VoxelSpaceInfos.Type.ALS);

        //check all parameters, if null set to default
        if (ladParams == null) {
            ladParams = new LADParams();
        }

        this.ladParams = ladParams;

        this.echoesWeightByRankParams = echoesWeightByRankParams;

        if (laserSpecification == null) {
            if (TLS) {
                laserSpecification = LaserSpecification.VZ_400;
            } else {
                laserSpecification = LaserSpecification.LMS_Q560;
            }
        }

        this.laserSpecification = laserSpecification;

        if (naNsCorrectionParams == null) {
            naNsCorrectionParams = new NaNsCorrectionParams(false);
        }
        this.naNsCorrectionParams = naNsCorrectionParams;

        if (dtmFilteringParams == null) {
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

    public LaserSpecification getLaserSpecification() {
        return laserSpecification;
    }

    public void setLaserSpecification(LaserSpecification laserSpecification) {
        this.laserSpecification = laserSpecification;
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
     * @param groundEnergyParams Ground-energy map generation parameters (ALS
     * only)
     */
    public void setGroundEnergyParams(GroundEnergyParams groundEnergyParams) {
        this.groundEnergyParams = groundEnergyParams;
    }

    /**
     *
     * @return Echoes weigting parameters
     */
    public EchoesWeightByRankParams getEchoesWeightByRankParams() {
        return echoesWeightByRankParams;
    }

    /**
     *
     * @param echoesWeightByRankParams Echoes weigting parameters
     */
    public void setEchoesWeightByRankParams(EchoesWeightByRankParams echoesWeightByRankParams) {
        this.echoesWeightByRankParams = echoesWeightByRankParams;
    }

    /**
     *
     * @return Echoes weigting parameters
     */
    public EchoesWeightByFileParams getEchoesWeightByFileParams() {
        return echoesWeightByFileParams;
    }

    /**
     *
     * @param echoesWeightByFileParams Echoes weigting parameters
     */
    public void setEchoesWeightByFileParams(EchoesWeightByFileParams echoesWeightByFileParams) {
        this.echoesWeightByFileParams = echoesWeightByFileParams;
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

    public boolean isBeamSectionConstant() {
        return constantBeamSection;
    }

    public void setBeamSectionConstant(boolean constant) {
        this.constantBeamSection = constant;
    }
    
    public boolean isRayPonderationEnabled() {
        return rayPonderationEnabled;
    }

    public void setRayPonderationEnabled(boolean enabled) {
        this.rayPonderationEnabled = enabled;
    }
}
