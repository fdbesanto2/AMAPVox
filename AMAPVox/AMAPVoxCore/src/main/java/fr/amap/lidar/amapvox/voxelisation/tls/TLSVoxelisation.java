/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.commons.util.CallableTask;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.util.Util;
import fr.amap.lidar.amapvox.voxelisation.AbstractVoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.SimpleVoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public abstract class TLSVoxelisation extends CallableTask<File> {

    private final static Logger LOGGER = Logger.getLogger(TLSVoxelisation.class);

    protected int nbVoxelisationFinished;
    protected final File inputFile;
    protected AbstractVoxelAnalysis voxelAnalysis;
    protected Mat4D transfMatrix;
    protected Mat3D rotation;
    protected final VoxelParameters parameters;
    protected final File outputFile;
    protected final TLSVoxCfg cfg;
    protected final Class voxelAnalysisClass;

    private TLSVoxelisation(TLSVoxCfg cfg, Class voxelAnalysisClass, boolean outputSuffix) {

        this.cfg = cfg;
        this.inputFile = cfg.getInputFile();
        this.parameters = cfg.getVoxelParameters();
        parameters.infos.setType(VoxelSpaceInfos.Type.TLS);
        this.outputFile = outputSuffix
                ? new File(cfg.getOutputFile().getAbsoluteFile() + "-" + voxelAnalysisClass.getSimpleName())
                : cfg.getOutputFile();

        this.voxelAnalysisClass = voxelAnalysisClass;

    }

    public TLSVoxelisation(TLSVoxCfg cfg, Class voxelAnalysisClass) {
        this(cfg, voxelAnalysisClass, true);
    }

    public TLSVoxelisation(TLSVoxCfg cfg) {
        this(cfg, VoxelAnalysis.class, false);
    }

    public void init() throws Exception {

        nbVoxelisationFinished = 0;

        // Transformation matrices
        Mat4D pop = null != cfg.getPopMatrix()
                ? MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix())
                : Mat4D.identity();
        Mat4D vop = null != cfg.getVopMatrix()
                ? MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix())
                : Mat4D.identity();
        Mat4D sop = MatrixUtility.convertMatrix4dToMat4D(cfg.getSopMatrix());
        Mat4D popVop = Mat4D.multiply(pop, vop);
        transfMatrix = Mat4D.multiply(sop, popVop);

        rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0], transfMatrix.mat[1], transfMatrix.mat[2],
            transfMatrix.mat[4], transfMatrix.mat[5], transfMatrix.mat[6],
            transfMatrix.mat[8], transfMatrix.mat[9], transfMatrix.mat[10]
        };

        // Digital Terrain Model
        Raster terrain = null;
        if (cfg.getVoxelParameters().getDtmFilteringParams().useDTMCorrection()) {
            LOGGER.info("Loading dtm...");
            terrain = Util.loadDTM(cfg.getVoxelParameters().getDtmFilteringParams().getDtmFile());
            if (terrain != null && cfg.getVoxelParameters().getDtmFilteringParams().isUseVOPMatrix()) {
                terrain.setTransformationMatrix(vop);
            }
        }

        voxelAnalysis = (AbstractVoxelAnalysis) voxelAnalysisClass
                .getConstructor(Raster.class, VoxelAnalysisCfg.class)
                .newInstance(terrain, cfg);
    }

    public int getNbVoxelisationFinished() {
        return nbVoxelisationFinished;
    }

    public void setNbVoxelisationFinished(int nbVoxelisationFinished) {
        this.nbVoxelisationFinished = nbVoxelisationFinished;
    }

    public void postProcess() throws IOException, Exception {

        voxelAnalysis.computePADs();

        voxelAnalysis.write(cfg.getVoxelsFormat(), outputFile);

        //VoxelAnalysisData resultData = voxelAnalysis.getResultData();
        //permet de signaler au garbage collector que cet élément peut être supprimé
        voxelAnalysis = null;

        fireSucceeded();
    }

}
