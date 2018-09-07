package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.commons.util.TimeCounter;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.util.BoundingBox3d;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.Scene;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel.VoxelManagerSettings;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.raster.multiband.BCommon;
import fr.amap.commons.raster.multiband.BHeader;
import fr.amap.commons.raster.multiband.BSQ;
import fr.amap.commons.util.Cancellable;
import fr.amap.commons.util.IteratorWithException;
import fr.amap.commons.util.Process;
import fr.amap.commons.util.filter.Filter;
import fr.amap.lidar.amapvox.commons.GTheta;
import fr.amap.lidar.amapvox.commons.LADParams;
import fr.amap.lidar.amapvox.commons.LeafAngleDistribution;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos.Type;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg.VoxelsFormat;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightByFileParams.EchoesWeight;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.GroundEnergyParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.vecmath.Point3i;

import java.awt.Color;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

public abstract class AbstractVoxelAnalysis extends Process implements Cancellable {

    // abstract function
    
    abstract public void processOneShot(Shot shot) throws Exception;
    
    // variable declaration
    
    private final static Logger LOGGER = Logger.getLogger(AbstractVoxelAnalysis.class);

    private boolean cancelled;
    
    int nShotsProcessed;
    int nShotsDiscarded;

    Voxel voxels[][][];
    VoxelManager voxelManager;

    private float MAX_PAD = 3;

    double[][] weightTable;
    IteratorWithException<EchoesWeight> weightIterator;
    double[][] residualEnergyTable;

    final boolean volumeWeighting = true;

    int pathLengthMode = 1; //1 = mode A, 2 = mode B
    int transMode = 1;

    GroundEnergy[][] groundEnergy;
    VoxelParameters parameters;
    final VoxelAnalysisCfg cfg;

    final Raster dtm;

    EchoesWeight echoesWeight;

    LaserSpecification laserSpec;

    final List<Filter<Shot>> shotFilters;
    final List<Filter<Shot.Echo>> echoFilters;

    private boolean padComputed;

    private BufferedWriter shotSegmentWriter;

    //directional transmittance (GTheta)
    private GTheta direcTransmittance;

    /**
     *
     */
    public class GroundEnergy {

        /*ATTENTION : le calcul de l'énergie arrivant au sol est incorrect actuellement,
         en effet, la prolongation d'un tir après son dernier écho jusqu'au sol ne se fait
         pas pour les tirs ayant le dernier écho en dehors de l'espace voxel.
         En correction, il faudrait faire un pré-calcul pour déterminer la taille de la 
        bounding-box pour inclure les tirs correspondants.
        Donc créer deux espaces voxels en l'occurence.*/
        public int groundEnergyPotential;
        public float groundEnergyActual;

        public GroundEnergy() {
            groundEnergyPotential = 0;
            groundEnergyActual = 0;
        }
    }

    private void generateResidualEnergyTable() {

        residualEnergyTable = new double[weightTable.length][weightTable[0].length];

        for (int i = 0; i < weightTable.length; i++) {

            double startEnergy = 1.d;

            for (int j = 0; j < i + 1; j++) {
                residualEnergyTable[i][j] = startEnergy;
                startEnergy -= weightTable[i][j];
            }
        }
    }

    double computeResidualEnergy(int nEcho, int iEcho, double weightCorr) {

        double cumWeight = 0.d;
        for (int rank = 0; rank < iEcho; rank++) {
            cumWeight += weightCorr * weightTable[nEcho - 1][rank];
        }

        return 1.d - cumWeight;
    }

    private void generateNoPonderationTables() {

        weightTable = new double[7][7];
        for (int i = 0; i < weightTable.length; i++) {
            for (int j = 0; j < weightTable[i].length; j++) {
                weightTable[i][j] = j < (i + 1) ? 1.d : Double.NaN;
            }
        }

        residualEnergyTable = new double[7][7];
        for (int i = 0; i < residualEnergyTable.length; i++) {
            for (int j = 0; j < residualEnergyTable[i].length; j++) {
                residualEnergyTable[i][j] = j < (i + 1) ? 1.d : Double.NaN;
            }
        }
    }

    private float getGroundDistance(float x, float y, float z) {

        return (dtm != null && parameters.getDtmFilteringParams().useDTMCorrection())
                ? z - (float) (dtm.getSimpleHeight(x, y))
                : z;
    }

    public AbstractVoxelAnalysis(Raster terrain, VoxelAnalysisCfg cfg) throws Exception {

        this.dtm = terrain;

        shotFilters = cfg.getShotFilters();
        for (Filter filter : shotFilters) {
            filter.init();
        }
        echoFilters = cfg.getEchoFilters();
        for (Filter filter : echoFilters) {
            filter.init();
        }

        this.cfg = cfg;
        
        nShotsProcessed = 0;
        nShotsDiscarded = 0;

        init(cfg.getVoxelParameters());
    }

    private void init(VoxelParameters parameters) throws Exception {

        this.parameters = parameters;
        this.parameters.infos.setTransmittanceMode(parameters.getTransmittanceMode());
        this.parameters.infos.setPathLengthMode(parameters.getPathLengthMode());

        if (null != parameters.getEchoesWeightByRankParams()) {
            weightTable = parameters.getEchoesWeightByRankParams().getWeightingData();
            generateResidualEnergyTable();
        } else {
            // no energy ponderation by rank
            generateNoPonderationTables();
        }

        if (null != parameters.getEchoesWeightByFileParams()) {
            LOGGER.info("Open echoes weight file " + parameters.getEchoesWeightByFileParams().getFile());
            weightIterator = parameters.getEchoesWeightByFileParams().iterator();
            echoesWeight = weightIterator.next();
        }

        MAX_PAD = parameters.infos.getMaxPAD();
        this.transMode = parameters.getTransmittanceMode();

        String pathLengthModeStr = parameters.getPathLengthMode();
        if (pathLengthModeStr.equals("A")) {
            pathLengthMode = 1;
        } else {
            pathLengthMode = 2;
        }

        //test
        /*pathLengthMode = 2;
        this.transMode = 1;*/
        laserSpec = parameters.getLaserSpecification();

        if (laserSpec == null) {
            if (parameters.infos.getType() == VoxelSpaceInfos.Type.TLS) {
                laserSpec = LaserSpecification.VZ_400;
            } else {
                laserSpec = LaserSpecification.LMS_Q560;
            }
        }

        LADParams ladParameters = parameters.getLadParams();
        if (ladParameters == null) {
            ladParameters = new LADParams();
        }

        LeafAngleDistribution distribution = new LeafAngleDistribution(ladParameters.getLadType(),
                ladParameters.getLadBetaFunctionAlphaParameter(),
                ladParameters.getLadBetaFunctionBetaParameter());

        direcTransmittance = new GTheta(distribution);

        LOGGER.info("Building transmittance functions table");
        direcTransmittance.buildTable(GTheta.DEFAULT_STEP_NUMBER);
        LOGGER.info("Transmittance functions table is built");

        if (cfg != null && cfg.isExportShotSegment()) {
            try {
                shotSegmentWriter = new BufferedWriter(new FileWriter(new File(cfg.getOutputFile().getAbsolutePath() + ".segments")));
                shotSegmentWriter.write("i j k norm_transmittance weight\n");
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(VoxelAnalysis.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get position of the center of a voxel
     *
     * @param indices
     * @return
     */
    public Point3d getPosition(Point3i indices) {

        Point3d minCorner = parameters.infos.getMinCorner();
        Point3d voxSize = voxelManager.getVoxelSpace().getVoxelSize();

        double posX = minCorner.x + (voxSize.x / 2.0d) + (indices.x * voxSize.x);
        double posY = minCorner.y + (voxSize.y / 2.0d) + (indices.y * voxSize.y);
        double posZ = minCorner.z + (voxSize.z / 2.0d) + (indices.z * voxSize.z);

        return new Point3d(posX, posY, posZ);
    }

    boolean retainEcho(Shot.Echo echo) throws Exception {

        if (echo.rank >= 0 && echoFilters != null) {
            for (Filter filter : echoFilters) {
                if (!filter.accept(echo)) {
                    return false;
                }
            }
        }

        // DTM filtering
        if (parameters.getDtmFilteringParams().useDTMCorrection()) {
            float echoDistance = getGroundDistance((float) echo.location.x, (float) echo.location.y, (float) echo.location.z);
            return Float.isNaN(echoDistance) && (echoDistance >= parameters.getDtmFilteringParams().getMinDTMDistance());
        }

        // echo retained by every filter
        return true;
    }

    boolean retainShot(Shot shot) throws Exception {

        for (Filter<Shot> filter : shotFilters) {
            // as soon as a filter discard the shot returns false 
            if (!filter.accept(shot)) {
                return false;
            }
        }
        // all filters retain the shot
        return true;
    }

    boolean isInsideSameVoxel(Shot.Echo echo1, Shot.Echo echo2) {

        if (echo1 == null || echo2 == null) {
            return false;
        }

        Point3i indices1 = voxelManager.getVoxelIndicesFromPoint(echo1.location);
        Point3i indices2 = voxelManager.getVoxelIndicesFromPoint(echo2.location);

        return indices1 != null && indices2 != null && indices1.equals(indices2);
    }

    /**
     * Propagate the given {@code shot} in the voxel space disregarding the
     * vegetation in order to estimate a potential beam volume.
     *
     * @param shot
     */
    void freePropagation(Shot shot) {

        LineElement line = new LineSegment(shot.origin, shot.direction, 999999);

        // first voxel crossed by the shot
        VoxelCrossingContext context = voxelManager.getFirstVoxelV2(line);

        if (null != context) {
            do {
                // current voxel
                Point3i indices = context.indices;

                // average beam surface within voxel
                // distance from origin to voxel center
                Point3d voxelPosition = getPosition(new Point3i(indices.x, indices.y, indices.z));
                double distance = voxelPosition.distance(shot.origin);
                // beam surface at voxel center
                double surface = Math.pow((Math.tan(0.5d * laserSpec.getBeamDivergence()) * distance) + 0.5d * laserSpec.getBeamDiameterAtExit(), 2) * Math.PI;

                // ray length within voxel
                // distance from shot origin to shot interception point with current voxel
                double dIn = context.length;
                // get next voxel
                context = voxelManager.CrossVoxel(line, indices);
                // distance from shot origin to shot interception point with next voxel
                double dOut = context.length;
                double rayLength = dOut - dIn;

                // instantiate voxel on the fly when first encountered
                if (voxels[indices.x][indices.y][indices.z] == null) {
                    voxels[indices.x][indices.y][indices.z] = initVoxel(indices.x, indices.y, indices.z);
                }

                // increment potential beam volume
                voxels[indices.x][indices.y][indices.z].bvPotential += (surface * rayLength);

            } while (context.indices != null);
        }
    }

    public static float computeTransmittance(double bvEntering, double bvIntercepted) {

        float transmittance;

        if (bvEntering == 0) {

            transmittance = Float.NaN;

        } else if (bvIntercepted > bvEntering) {
            transmittance = Float.NaN;

        } else {
            transmittance = (float) ((bvEntering - bvIntercepted) / bvEntering);
        }

        return transmittance;
    }

    public static float computeNormTransmittanceMode2(double transmittance, double _sumSurfMulLength, double lMeanTotal) {
        float normalizedTransmittance = (float) Math.pow((transmittance / _sumSurfMulLength), 1 / lMeanTotal);
        return normalizedTransmittance;
    }

    public static float computeNormTransmittanceMode3(double transmittance, double _sumSurfMulLength) {
        float normalizedTransmittance = (float) (transmittance / _sumSurfMulLength);
        return normalizedTransmittance;
    }

    public static float computeNormTransmittance(double transmittance, double lMeanTotal) {
        float normalizedTransmittance = (float) Math.pow(transmittance, 1 / lMeanTotal);
        return normalizedTransmittance;
    }

    public static float computePADFromNormTransmittance(float transmittance, float angleMean, float maxPAD, GTheta direcTransmittance) {

        float pad;

        if (Float.isNaN(transmittance)) {

            pad = Float.NaN;

        } else if (transmittance == 0) {

            pad = maxPAD;

        } else {

            float coefficientGTheta = (float) direcTransmittance.getGThetaFromAngle(angleMean, true);

            pad = (float) (Math.log(transmittance) / (-coefficientGTheta));

            if (Float.isNaN(pad)) {
                pad = Float.NaN;
            } else if (pad > maxPAD || Float.isInfinite(pad)) {
                pad = maxPAD;
            }

        }

        return pad + 0.0f; //set +0.0f to avoid -0.0f
    }

    public float computePADFromNormTransmittance(float transmittance, float angleMean) {

        return computePADFromNormTransmittance(transmittance, angleMean, MAX_PAD, direcTransmittance);
    }

    public Voxel computePADFromVoxel(Voxel voxel, int i, int j, int k) {

        if (voxel == null) {

            voxel = initVoxel(i, j, k);
        }

        voxel.angleMean = voxel.angleMean / voxel.nbSampling;

        if (voxel.nbSampling >= voxel.nbEchos) {

            voxel.lMeanTotal = voxel.lgTotal / (voxel.nbSampling);

        }

        float normalizedTransmittance;

        switch (transMode) {
            case 2:
                normalizedTransmittance = computeNormTransmittanceMode2(voxel.transmittance_tmp, voxel.cumulatedBeamVolume, voxel.lMeanTotal);
                break;
            case 3:
                //normalizedTransmittance = computeNormTransmittanceV2(voxel.transmittance_tmp, voxel.sumSurfMulLength);
                normalizedTransmittance = computeNormTransmittanceMode3(voxel.transmittance_tmp, voxel.cumulatedBeamVolumIn); //CL
                break;

            case 1:
            default:
                float transmittance = computeTransmittance(voxel.bvEntering, voxel.bvIntercepted);
                normalizedTransmittance = computeNormTransmittance(transmittance, voxel.lMeanTotal);
        }

        voxel.transmittance = normalizedTransmittance;
        voxel.PadBVTotal = computePADFromNormTransmittance(normalizedTransmittance, voxel.angleMean);

        return voxel;
    }

    public void computePADs() {

        for (int i = 0; i < parameters.infos.getSplit().x; i++) {
            for (int j = 0; j < parameters.infos.getSplit().y; j++) {
                for (int k = 0; k < parameters.infos.getSplit().z; k++) {

                    Voxel voxel = voxels[i][j][k];
                    voxels[i][j][k] = computePADFromVoxel(voxel, i, j, k);
                }
            }
        }

        padComputed = true;
    }

    private void writeVoxel(File outputFile) throws FileNotFoundException, Exception {

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {

            // headers
            writer.write(parameters.infos.headerToString() + "\n");
            String sep = " ";
            List<String> outputFields = new ArrayList();
            outputFields.add("i");
            outputFields.add("j");
            outputFields.add("k");
            outputFields.add("PadBVTotal");
            outputFields.add("angleMean");
            outputFields.add("bvEntering");
            outputFields.add("bvIntercepted");
            outputFields.add("ground_distance");
            outputFields.add("lMeanTotal");
            outputFields.add("lgTotal");
            outputFields.add("nbEchos");
            outputFields.add("nbSampling");
            outputFields.add("transmittance");
            if (transMode == 2) {
                outputFields.add("sumSurfMulLength");
                outputFields.add("transmittance_tmp");
            } else if (transMode == 3) {
                outputFields.add("sumSurfMulLengthMulEnt");
                outputFields.add("transmittance_tmp");
            }
            outputFields.add("bvPotential");

            StringBuilder header = new StringBuilder();
            for (String field : outputFields) {
                header.append(field);
                header.append(sep);
            }
            writer.write(header.toString().trim() + "\n");

            // voxels
            MathContext mc = new MathContext(9);
            int count = 0;
            int nbLines = parameters.infos.getSplit().x * parameters.infos.getSplit().y * parameters.infos.getSplit().z;
            for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                for (int j = 0; j < parameters.infos.getSplit().y; j++) {
                    for (int k = 0; k < parameters.infos.getSplit().z; k++) {
                        // task cancelled
                        if (isCancelled()) {
                            return;
                        }
                        // current voxel
                        fireProgress("Writing file", count++, nbLines);
                        Voxel voxel = voxels[i][j][k];
                        // compute PAD
                        if (!padComputed) {
                            voxel = computePADFromVoxel(voxel, i, j, k);
                        }
                        // voxel attributes to String
                        StringBuilder voxelSB = new StringBuilder();
                        for (String name : outputFields) {
                            try {
                                Field field = Voxel.class.getField(name);
                                switch (field.getType().getName()) {
                                    case "double":
                                    case "float":
                                        BigDecimal bd = new BigDecimal(field.getDouble(voxel), mc);
                                        voxelSB.append(bd.doubleValue());
                                        break;
                                    case "int":
                                        voxelSB.append(field.getInt(voxel));
                                        break;
                                }
                            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
                                voxelSB.append(Float.NaN);
                            }
                            voxelSB.append(" ");
                        }
                        // write line
                        writer.write(voxelSB.toString().trim() + "\n");
                    }
                }
            }
            padComputed = true;
        }
    }

    private void writeRaster(File outputFile) throws IOException, Exception {

        float scale = 1.0f;
        float resolution = parameters.infos.getResolution();
        int rasterXSize = (int) (Math.ceil(parameters.infos.getSplit().x / scale));
        int rasterYSize = (int) (Math.ceil(parameters.infos.getSplit().y / scale));
        BHeader header = new BHeader(rasterXSize, rasterYSize, 1, BCommon.NumberOfBits.N_BITS_32);
        header.setUlxmap(parameters.infos.getMinCorner().x + (resolution / 2.0f));
        header.setUlymap(parameters.infos.getMaxCorner().y - (resolution / 2.0f));
        header.setXdim((float) parameters.infos.getVoxelSize().x);
        header.setYdim((float) parameters.infos.getVoxelSize().y);
        BSQ raster = new BSQ(outputFile, header);
        long valMax = 4294967294L;
        long valNoData = 4294967295L;
        double laiMax = 50000;
        header.addMetadata("NO_DATA", String.valueOf(valNoData));
        header.addMetadata("MAX_VAL", String.valueOf(valMax));
        header.addMetadata("MAX_VEG_M2", String.valueOf(laiMax));
        for (int i = 0; i < parameters.infos.getSplit().x; i++) {
            for (int j = 0; j < parameters.infos.getSplit().y; j++) {

                double laiSum = 0;

                for (int k = parameters.infos.getSplit().z - 1; k >= 0; k--) {

                    Voxel vox = voxels[i][j][k];

                    if (vox != null && vox.ground_distance >= 0 && vox.PadBVTotal > 0 && !Float.isNaN(vox.PadBVTotal)) {

                        //calcul de la position de départ
                        Point3d voxelPosition = getPosition(new Point3i(i, j, k));

                        Point3d subVoxelSize = new Point3d(parameters.infos.getVoxelSize().x / parameters.infos.getResolution(),
                                parameters.infos.getVoxelSize().y / parameters.infos.getResolution(),
                                parameters.infos.getVoxelSize().z / parameters.infos.getResolution());

                        Point3d startPosition = new Point3d(voxelPosition.x - (parameters.infos.getVoxelSize().x - subVoxelSize.x) / 2.0,
                                voxelPosition.y - (parameters.infos.getVoxelSize().y - subVoxelSize.y) / 2.0,
                                voxelPosition.z - (parameters.infos.getVoxelSize().z - subVoxelSize.z) / 2.0);

                        Point3i nbSubVoxels = new Point3i((int) Math.ceil(parameters.infos.getVoxelSize().x),
                                (int) Math.ceil(parameters.infos.getVoxelSize().y),
                                (int) Math.ceil(parameters.infos.getVoxelSize().z));

                        //parcours des sous-voxels
                        int nbSubVoxelsAboveGround = 0;

                        for (int i2 = 0; i2 < nbSubVoxels.x; i2++) {
                            for (int j2 = 0; j2 < nbSubVoxels.y; j2++) {
                                for (int k2 = nbSubVoxels.z - 1; k2 >= 0; k2--) {

                                    float groundDistance = getGroundDistance((float) (startPosition.x + (i2 * subVoxelSize.x)),
                                            (float) (startPosition.y + (j2 * subVoxelSize.y)),
                                            (float) (startPosition.z + (k2 * subVoxelSize.z)));

                                    if (groundDistance > 0) {
                                        nbSubVoxelsAboveGround++;
                                    }
                                }
                            }
                        }

                        float volume = (float) (subVoxelSize.x * subVoxelSize.y * subVoxelSize.z * nbSubVoxelsAboveGround);
                        double lai = vox.PadBVTotal * volume;

                        laiSum += lai;
                    }
                }

                long value = (long) ((laiSum / laiMax) * (valMax));

                String binaryString = Long.toBinaryString(value);
                byte[] bval = new BigInteger(binaryString, 2).toByteArray();
                ArrayUtils.reverse(bval);
                byte b0 = 0x0, b1 = 0x0, b2 = 0x0, b3 = 0x0;
                if (bval.length > 0) {
                    b0 = bval[0];
                }
                if (bval.length > 1) {
                    b1 = bval[1];
                }
                if (bval.length > 2) {
                    b2 = bval[2];
                }
                if (bval.length > 3) {
                    b3 = bval[3];
                }

                raster.setPixel(i, parameters.infos.getSplit().y - j - 1, 0, b0, b1, b2, b3);
            }
        }

        raster.writeHeader();
        raster.writeImage();
    }

    public void write(VoxelsFormat format, File outputFile) throws FileNotFoundException, Exception {

        //tmp
        //writer.close();
        if (cfg.isExportShotSegment()) {
            shotSegmentWriter.close();
        }

        LOGGER.info("writing file: " + outputFile.getAbsolutePath());

        if (null != format) {
            switch (format) {
                case NONE:
                    return;
                case VOXEL:
                    writeVoxel(outputFile);
                    break;
                case RASTER:
                    writeRaster(outputFile);
            }
        }
    }

    public void writeGroundEnergy() throws IOException {

        if (groundEnergy.length > 0 && groundEnergy[0].length > 0) {

            long start_time = System.currentTimeMillis();

            LOGGER.info("writing file: " + parameters.getGroundEnergyParams().getGroundEnergyFile().getAbsolutePath());

            try {

                if (parameters.getGroundEnergyParams().getGroundEnergyFileFormat() == GroundEnergyParams.FILE_FORMAT_PNG) {

                    BufferedImage image = new BufferedImage(parameters.infos.getSplit().x, parameters.infos.getSplit().y, BufferedImage.TYPE_INT_ARGB);

                    for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                        for (int j = 0; j < parameters.infos.getSplit().y; j++) {

                            float transmittance = groundEnergy[i][j].groundEnergyActual / groundEnergy[i][j].groundEnergyPotential;

                            Color c;

                            if (transmittance <= 1.0 && transmittance >= 0.0) {
                                c = new Color(ColorSpace.getInstance(ColorSpace.CS_GRAY), new float[]{transmittance}, 1.0f);

                            } else if (transmittance > 1.0) {
                                c = new Color(1.0f, 0.0f, 0.0f, 1.0f);
                            } else if (transmittance < 0.0) {
                                c = new Color(0.0f, 1.0f, 0.0f, 1.0f);
                            } else {
                                c = new Color(0.0f, 0.0f, 1.0f, 1.0f);
                            }

                            image.setRGB(i, parameters.infos.getSplit().y - 1 - j, c.getRGB());

                        }
                    }

                    ImageIO.write(image, "png", new File(parameters.getGroundEnergyParams().getGroundEnergyFile().getAbsolutePath()));

                } else {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(parameters.getGroundEnergyParams().getGroundEnergyFile()))) {
                        writer.write("i j groundEnergyActual groundEnergyPotential transmittance\n");

                        for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                            for (int j = 0; j < parameters.infos.getSplit().y; j++) {

                                float transmittance = groundEnergy[i][j].groundEnergyActual / groundEnergy[i][j].groundEnergyPotential;
                                writer.write(i + " " + j + " " + groundEnergy[i][j].groundEnergyActual + " " + groundEnergy[i][j].groundEnergyPotential + " " + transmittance + "\n");
                            }
                        }
                    }
                }

                LOGGER.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

            } catch (IOException ex) {
                throw ex;
            }

        } else {

            LOGGER.warn("Ground energy is set up but no values are available");
        }

    }

    public void createVoxelSpace() {

        try {

            voxels = new Voxel[parameters.infos.getSplit().x][parameters.infos.getSplit().y][parameters.infos.getSplit().z];

            if (parameters.getGroundEnergyParams() != null
                    && parameters.getGroundEnergyParams().isCalculateGroundEnergy() && parameters.infos.getType() != Type.TLS) {

                // allocate
                LOGGER.info("allocate!!!!!!!!");

                groundEnergy = new GroundEnergy[parameters.infos.getSplit().x][parameters.infos.getSplit().y];

                for (int i = 0; i < parameters.infos.getSplit().x; i++) {
                    for (int j = 0; j < parameters.infos.getSplit().y; j++) {
                        groundEnergy[i][j] = new GroundEnergy();
                    }
                }
            }

            Scene scene = new Scene();
            scene.setBoundingBox(new BoundingBox3d(parameters.infos.getMinCorner(), parameters.infos.getMaxCorner()));

            voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.infos.getSplit(), VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

            LOGGER.info(voxelManager.getInformations());

        } catch (Exception e) {
            LOGGER.error(e + " " + this.getClass().getName());
        }

    }

    Voxel initVoxel(int i, int j, int k) {

        Voxel vox = new Voxel(i, j, k);

        Point3d position = getPosition(new Point3i(i, j, k));

        float dist;

        if (dtm != null && parameters.getDtmFilteringParams().useDTMCorrection()) {

            float dtmHeightXY = dtm.getSimpleHeight((float) position.x, (float) position.y);
            if (Float.isNaN(dtmHeightXY)) {
                dist = (float) (position.z);
            } else {
                dist = (float) (position.z - dtmHeightXY);
            }

        } else {
            dist = (float) (position.z - parameters.infos.getMinCorner().z);
        }

        vox.ground_distance = dist;

        return vox;
    }

    public int getNbShotsProcessed() {
        return nShotsProcessed;
    }

    public Voxel[][][] getVoxels() {
        return voxels;
    }

    public Raster getDtm() {
        return dtm;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
