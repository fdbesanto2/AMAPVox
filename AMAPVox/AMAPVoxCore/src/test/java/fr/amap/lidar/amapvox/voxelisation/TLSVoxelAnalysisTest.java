/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightByRankParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.junit.Test;

/**
 * Test voxelisation algorithm for TLS shots.
 *
 * @author Philippe Verley (philippe.verley@ird.fr)
 */
public class TLSVoxelAnalysisTest {

    // write .vox files in temporary directory 
    private final boolean WRITE_VOX_FILE = true;

    // math context for comparing double numbers with reasonable precision
    private final MathContext MC = new MathContext(7);

    /**
     * Shots without echo. Basic tests: sampling variable and entering beam
     * fraction are one along shot path, specific shot angles, intercepted beam
     * fraction and number of echoes are zero everywhere,
     *
     * @throws Exception
     */
    @Test
    public void testUninterceptedShot() throws Exception {

        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        // voxel parameters
        VoxelParameters params = new VoxelParameters.Builder()
                .voxelSpace(new Point3d(0, 0, 0), new Point3d(5, 5, 5), 1.f, VoxelSpaceInfos.Type.TLS)
                .laserSpecification(LaserSpecification.LMS_Q560)
                .padMAX(10.0f).build();

        // voxelisation parameters
        params.setBeamSectionConstant(true);
        params.setRayPonderationEnabled(false);
        // set voxel parameters to voxel analysis configuration
        cfg.setVoxelParameters(params);

        // create new voxel analysis
        AbstractVoxelAnalysis voxAnalysis = new VoxelAnalysis(null, cfg);
        voxAnalysis.createVoxelSpace();

        List<Shot> shots = new ArrayList();
        // shot without echo following z-axis
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), null));
        // oblique shot without echo following (1, 1, 1) elementary vector
        shots.add(new Shot(1, new Point3d(1.5, 1.5, 1.5), new Vector3d(1, 1, 1), null));

        // process shots
        for (Shot shot : shots) {
            voxAnalysis.processOneShot(shot);
        }
        // compute plant area 
        voxAnalysis.computePADs();

        // write voxel file in temporary directory
        if (WRITE_VOX_FILE) {
            voxAnalysis.write(VoxelAnalysisCfg.VoxelsFormat.VOXEL, java.io.File.createTempFile("testUninterceptedShot", ".vox"));
        }

        // assertions on vertical shot
        Voxel voxel;
        // assertion on firt voxel
        assert (voxAnalysis.voxels[0][0][0].lgTotal == 0.5);
        for (int k = 1; k < 5; k++) {
            voxel = voxAnalysis.voxels[0][0][k];
            assert (voxel.lgTotal == 1);
            assert (voxel.nbSampling == 1);
            assert (voxel.bvEntering == 1);
            assert (voxel.angleMean == 0);
        }

        // assertions on oblique shot
        voxel = voxAnalysis.voxels[1][1][1];
        assert (voxel.lgTotal == (float) (Math.sqrt(3.d) / 2.d));
        for (int n = 2; n < 5; n++) {
            voxel = voxAnalysis.voxels[n][n][n];
            assert (equal(voxel.lgTotal, Math.sqrt(3.d)));
            assert (voxel.angleMean == 45);
        }

        // assertions on the whole voxel space
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 5; k++) {
                    voxel = voxAnalysis.voxels[i][j][k];
                    assert (voxel.bvIntercepted == 0);
                    assert (voxel.nbEchos == 0);
                }
            }

        }
    }

    /**
     * Shots with multiple echoes. Echoes in separate voxels. Echoes in separate
     * voxels, last one outside voxel space. Echoes in same voxels. Echoes on
     * voxel edges.
     *
     * @throws Exception
     */
    @Test
    public void testShotWithEchoes() throws Exception {

        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        // voxel parameters
        VoxelParameters params = new VoxelParameters.Builder()
                .voxelSpace(new Point3d(0, 0, 0), new Point3d(5, 5, 5), 1.f, VoxelSpaceInfos.Type.TLS)
                .echoesWeightByRankParams(EchoesWeightByRankParams.DEFAULT_TLS_WEIGHTING)
                .laserSpecification(LaserSpecification.LMS_Q560)
                .padMAX(10.0f).build();

        // voxelisation parameters
        params.setBeamSectionConstant(true);
        params.setRayPonderationEnabled(false);
        // set voxel parameters to voxel analysis configuration
        cfg.setVoxelParameters(params);

        // create new voxel analysis
        AbstractVoxelAnalysis voxAnalysis = new VoxelAnalysis(null, cfg);
        voxAnalysis.createVoxelSpace();

        List<Shot> shots = new ArrayList();
        // shot without 2 echoes inside voxel space going along z-axis
        shots.add(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 3.d}));
        // shot without 3 echoes, last one outside voxel sapce going along y-axis
        shots.add(new Shot(1, new Point3d(1.5, 0.5, 0.5), new Vector3d(0, 1, 0), new double[]{2.d, 3.d, 6.d}));
        // shot with 3 echoes, first two ones in same voxel going along y-axis
        shots.add(new Shot(2, new Point3d(2.5, 0.5, 0.5), new Vector3d(0, 1, 0), new double[]{2.d, 2.2d, 4.d}));
        // oblique shot with echoes at (3.5, 2.5, 1), (3.5, 3, 1.75) & (3.5, 3.5, 2)
        double dl = Math.sqrt(5.d) / 4.d;
        shots.add(new Shot(3, new Point3d(3.5, 0.5, 0.5), new Vector3d(0, 2, 1), new double[]{4 * dl, 5 * dl, 6 * dl}));

        // process shots
        for (Shot shot : shots) {
            voxAnalysis.processOneShot(shot);
        }
        // compute plant area 
        voxAnalysis.computePADs();

        // write voxel file in temporary directory
        if (WRITE_VOX_FILE) {
            voxAnalysis.write(VoxelAnalysisCfg.VoxelsFormat.VOXEL, java.io.File.createTempFile("testShotWithEchoes", ".vox"));
        }

        Voxel voxel;
        // first shot
        // assertion on voxel containing first echo
        voxel = voxAnalysis.voxels[0][0][1];
        assert (voxel.nbEchos == 1);
        assert (voxel.bvEntering == 1);
        assert (voxel.bvIntercepted == 0.5);
        // assertion on voxel following first echo
        voxel = voxAnalysis.voxels[0][0][2];
        assert (voxel.bvEntering == 0.5);
        assert (voxel.nbEchos == 0);
        // assertion on voxel containing second echo
        voxel = voxAnalysis.voxels[0][0][3];
        assert (voxel.nbEchos == 1);
        assert (voxel.bvEntering == 0.5);
        assert (voxel.bvIntercepted == 0.5);
        // assertion on voxel following last echo
        voxel = voxAnalysis.voxels[0][0][4];
        assert (voxel.nbEchos == 0);
        assert (voxel.nbSampling == 0);
        assert (voxel.bvEntering == 0);

        // second shot
        // assertion on voxel containing second echo (out of three)
        voxel = voxAnalysis.voxels[1][3][0];
        assert (voxel.nbEchos == 1);
        assert (equal(voxel.bvEntering, 2.d / 3.d));
        assert (equal(voxel.bvIntercepted, 1.d / 3.d));
        assert (voxel.angleMean == 90);
        // assertions on voxel following second echo
        voxel = voxAnalysis.voxels[1][4][0];
        assert (voxel.nbEchos == 0);
        assert (equal(voxel.bvEntering, 1.d / 3.d));
        assert (voxel.bvIntercepted == 0);

        // third shot
        // assertion on voxel containing the first two echoes
        voxel = voxAnalysis.voxels[2][2][0];
        assert (voxel.nbEchos == 2);
        assert (equal(voxel.bvIntercepted, 2.d / 3.d));
        assert (voxel.bvEntering == 1);
        // assertions on voxel following second echo
        voxel = voxAnalysis.voxels[2][3][0];
        assert (voxel.nbEchos == 0);
        assert (equal(voxel.bvEntering, 1.d / 3.d));

        // fourth shot
        // assertion on voxel containing first echo
        voxel = voxAnalysis.voxels[3][2][1];
        assert (voxel.nbEchos == 1);
        assert (equal(voxel.angleMean, Math.toDegrees(Math.atan(2.d / 1.d))));
        assert (equal(voxel.lgTotal, 2 * dl));
        // assertion on voxel following second echo
        voxel = voxAnalysis.voxels[3][3][1];
        assert (voxel.nbEchos == 1);
        assert (equal(voxel.lgTotal, dl));
        // assertion on voxel containing third echo
        voxel = voxAnalysis.voxels[3][3][2];
        assert (voxel.nbEchos == 1);
        assert (equal(voxel.lgTotal, dl));
        // assertion on voxel following last echo
        voxel = voxAnalysis.voxels[3][4][2];
        assert (voxel.nbEchos == 0);
        assert (equal(voxel.bvEntering, 0));
    }

    /**
     * Echo weighting inflated (beam fraction reaches zero before last echo).
     * Shot path interrupted prematurely.
     *
     * @throws Exception
     */
    @Test
    public void testShotWithOverWeightedEchoes() throws Exception {

        EchoesWeightByRankParams overweight = new EchoesWeightByRankParams(new double[][]{
            {1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {1.d, 1.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.5d, 0.5d, 0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {1 / 3.d, 1 / 3.d, 1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN},
            {0.25d, 0.25d, 0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN},
            {0.2d, 0.2d, 0.2d, 0.2d, 0.2d, 0.2d, Double.NaN},
            {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d}});

        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        // voxel parameters
        VoxelParameters params = new VoxelParameters.Builder()
                .voxelSpace(new Point3d(0, 0, 0), new Point3d(5, 5, 5), 1.f, VoxelSpaceInfos.Type.TLS)
                .echoesWeightByRankParams(overweight)
                .laserSpecification(LaserSpecification.LMS_Q560)
                .padMAX(10.0f).build();
        // voxelisation parameters
        params.setBeamSectionConstant(true);
        params.setRayPonderationEnabled(false);
        // set voxel parameters to voxel analysis configuration
        cfg.setVoxelParameters(params);

        // create new voxel analysis
        AbstractVoxelAnalysis voxAnalysis = new VoxelAnalysis(null, cfg);
        voxAnalysis.createVoxelSpace();

        // one shot goinp up z-axis with 2 echoes
        voxAnalysis.processOneShot(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 3.d}));

        // compute plant area 
        voxAnalysis.computePADs();

        // write voxel file in temporary directory
        if (WRITE_VOX_FILE) {
            voxAnalysis.write(VoxelAnalysisCfg.VoxelsFormat.VOXEL, java.io.File.createTempFile("testShotWithOverWeightedEchoes", ".vox"));
        }

        Voxel voxel;
        // first shot
        // assertion on voxel containing first echo
        voxel = voxAnalysis.voxels[0][0][1];
        assert (voxel.nbEchos == 1);
        assert (voxel.bvEntering == 1);
        assert (voxel.bvIntercepted == 1);
        // assertions on voxels following second echo
        for (int k = 2; k < 5; k++) {
            voxel = voxAnalysis.voxels[0][0][2];
            assert (voxel.nbEchos == 0);
            assert (voxel.bvEntering == 0);
            assert (voxel.bvIntercepted == 0);
            assert (voxel.nbSampling == 0);
        }
    }

    /**
     * Echo weighting attenuated (beam fraction does not reach zero). Shot path
     * extended beyond last echo.
     *
     * @throws Exception
     */
    @Test
    public void testShotWithUnderWeightedEchoes() throws Exception {

        EchoesWeightByRankParams underweight = new EchoesWeightByRankParams(new double[][]{
            {0.5d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {1 / 3.d, 1 / 3.d, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.25d, 0.25d, 0.25d, Double.NaN, Double.NaN, Double.NaN, Double.NaN},
            {0.2d, 0.2d, 0.2d, 0.2d, Double.NaN, Double.NaN, Double.NaN},
            {1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, 1 / 6.d, Double.NaN, Double.NaN},
            {1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, 1 / 7.d, Double.NaN},
            {1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d, 1 / 8.d}});

        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        // voxel parameters
        VoxelParameters params = new VoxelParameters.Builder()
                .voxelSpace(new Point3d(0, 0, 0), new Point3d(5, 5, 5), 1.f, VoxelSpaceInfos.Type.TLS)
                .echoesWeightByRankParams(underweight)
                .laserSpecification(LaserSpecification.LMS_Q560)
                .padMAX(10.0f).build();
        // voxelisation parameters
        params.setBeamSectionConstant(true);
        params.setRayPonderationEnabled(false);
        // set voxel parameters to voxel analysis configuration
        cfg.setVoxelParameters(params);

        // create new voxel analysis
        AbstractVoxelAnalysis voxAnalysis = new VoxelAnalysis(null, cfg);
        voxAnalysis.createVoxelSpace();

        // one shot goinp up z-axis with 2 echoes
        voxAnalysis.processOneShot(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 3.d}));

        // compute plant area 
        voxAnalysis.computePADs();

        // write voxel file in temporary directory
        if (WRITE_VOX_FILE) {
            voxAnalysis.write(VoxelAnalysisCfg.VoxelsFormat.VOXEL, java.io.File.createTempFile("testShotWithUnderWeightedEchoes", ".vox"));
        }

        Voxel voxel;
        // first shot
        // assertion on voxel containing first echo
        voxel = voxAnalysis.voxels[0][0][1];
        assert (voxel.nbEchos == 1);
        assert (voxel.bvEntering == 1);
        assert (equal(voxel.bvIntercepted, 1.d / 3.d));
        // assertions on voxel following second echo
        voxel = voxAnalysis.voxels[0][0][4];
        assert (equal(voxel.bvEntering, 1.d / 3.d));
        assert (voxel.bvIntercepted == 0);
        assert (voxel.nbSampling == 1);
    }

    @Test
    public void testShotMonoEcho() throws Exception {

        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        // voxel parameters
        VoxelParameters params = new VoxelParameters.Builder()
                .voxelSpace(new Point3d(0, 0, 0), new Point3d(5, 5, 5), 1.f, VoxelSpaceInfos.Type.TLS)
                .echoesWeightByRankParams(EchoesWeightByRankParams.DEFAULT_TLS_WEIGHTING)
                .laserSpecification(LaserSpecification.FARO_FOCUS_X330)
                .padMAX(10.0f).build();
        // voxelisation parameters
        params.setBeamSectionConstant(true);
        params.setRayPonderationEnabled(false);
        // set voxel parameters to voxel analysis configuration
        cfg.setVoxelParameters(params);

        // create new voxel analysis
        AbstractVoxelAnalysis voxAnalysis = new VoxelAnalysis(null, cfg);
        voxAnalysis.createVoxelSpace();

        // one shot goinp up z-axis without echo
        voxAnalysis.processOneShot(new Shot(0, new Point3d(0.5, 0.5, 0.5), new Vector3d(0, 0, 1), null));
        // one shot goinp up z-axis with 1 echo
        voxAnalysis.processOneShot(new Shot(0, new Point3d(0.5, 1.5, 0.5), new Vector3d(0, 0, 1), new double[]{2.d}));
        // one shot goinp up z-axis with 2 echoes
        // (non sense but voxelisation algorithm should stop propagation after first echo anyway)
        voxAnalysis.processOneShot(new Shot(0, new Point3d(0.5, 2.5, 0.5), new Vector3d(0, 0, 1), new double[]{1.d, 3.d}));

        // compute plant area 
        voxAnalysis.computePADs();

        // write voxel file in temporary directory
        if (WRITE_VOX_FILE) {
            voxAnalysis.write(VoxelAnalysisCfg.VoxelsFormat.VOXEL, java.io.File.createTempFile("testShotMonoEcho", ".vox"));
        }

        // assertions
        Voxel voxel;
        // first shot
        for (int k = 1; k < 5; k++) {
            voxel = voxAnalysis.voxels[0][0][k];
            assert (voxel.nbEchos == 0);
            assert (voxel.bvEntering == 1);
            assert (voxel.bvIntercepted == 0);
            assert (voxel.nbSampling == 1);
        }
        // second shot
        voxel = voxAnalysis.voxels[0][1][2];
        assert (voxel.nbEchos == 1);
        assert (voxel.bvIntercepted == 1);
        voxel = voxAnalysis.voxels[0][1][3];
        assert (voxel.nbEchos == 0);
        assert (voxel.nbSampling == 0);
        assert (voxel.bvEntering == 0);
        // third shot
        voxel = voxAnalysis.voxels[0][2][1];
        assert (voxel.nbEchos == 1);
        assert (equal(voxel.bvIntercepted, 0.5d));
        voxel = voxAnalysis.voxels[0][2][2];
        assert (voxel.nbEchos == 0);
        assert (voxel.nbSampling == 0);
        assert (voxel.bvEntering == 0);
    }

    private boolean equal(double v1, double v2) {
        return new BigDecimal(v1, MC).compareTo(new BigDecimal(v2, MC)) == 0;
    }
}
