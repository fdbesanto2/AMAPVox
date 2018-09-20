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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.junit.Test;

/**
 *
 * @author Julien Heurtebize
 */
public class VoxelAnalysisTest {

    public VoxelAnalysisTest() {
    }

    /**
     * Test of getPosition method, of class VoxelAnalysis.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testResolution() throws Exception {

        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();

        EchoesWeightByRankParams echoesWeightParams = new EchoesWeightByRankParams(EchoesWeightByRankParams.DEFAULT_ALS_WEIGHTING);
        VoxelParameters params = new VoxelParameters.Builder()
                .voxelSpace(new Point3d(-2.5, -2.5, -2.5), new Point3d(2.5, 2.5, 2.5), 1.0f, VoxelSpaceInfos.Type.ALS)
                .echoesWeightByRankParams(echoesWeightParams)
                .laserSpecification(LaserSpecification.LMS_Q560)
                .padMAX(10.0f).build();

        params.setBeamSectionConstant(false);
        params.setLastRayTruncated(false);
        params.setRayPonderationEnabled(true);
        cfg.setVoxelParameters(params);

        VoxelAnalysis voxAnalysis = new VoxelAnalysis(null, cfg);

        voxAnalysis.createVoxelSpace();

        try {
            voxAnalysis.processOneShot(new Shot(0, new Point3d(0, 0, 5), new Vector3d(0, 0, -1), new double[]{3.6, 4.7}));
            voxAnalysis.processOneShot(new Shot(1, new Point3d(0, 0, 5), new Vector3d(0, 0, -1), new double[]{10}));

            voxAnalysis.computePADs();
            //voxAnalysis.write(VoxelAnalysisCfg.VoxelsFormat.VOXEL, new File("/home/julien/Documents/test_resolutions/2B_1m.vox"));

        } catch (IOException ex) {
            Logger.getLogger(VoxelAnalysisTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(VoxelAnalysisTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void testUninterceptedShot() throws Exception {

        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        // default echo attenuation factor
        EchoesWeightByRankParams echoesWeightParams = new EchoesWeightByRankParams(EchoesWeightByRankParams.DEFAULT_TLS_WEIGHTING);
        // voxel parameters
        VoxelParameters params = new VoxelParameters.Builder()
                .voxelSpace(new Point3d(0, 0, 0), new Point3d(5, 5, 5), 1.f, VoxelSpaceInfos.Type.TLS)
                .echoesWeightByRankParams(echoesWeightParams)
                .laserSpecification(LaserSpecification.LMS_Q560)
                .padMAX(10.0f).build();
        // voxelisation parameters
        params.setBeamSectionConstant(true);
        params.setLastRayTruncated(false);
        params.setRayPonderationEnabled(false);
        // set voxel parameters to voxel analysis configuration
        cfg.setVoxelParameters(params);

        // create new voxel analysis
        AbstractVoxelAnalysis voxAnalysis = new SimpleVoxelAnalysis(null, cfg);
        voxAnalysis.createVoxelSpace();

        List<Shot> shots = new ArrayList();
        // shot without echo going from origin to sky
        shots.add(new Shot(2, new Point3d(0.5, 0.5, 0), new Vector3d(0, 0, 1), null));
        // shot without echo going from origin + 1 to max corner
        shots.add(new Shot(3, new Point3d(1.5, 1.5, 1.5), new Vector3d(1, 1, 1), null));

        // process shots
        for (Shot shot : shots) {
            voxAnalysis.processOneShot(shot);
        }
        // compute plant area 
        voxAnalysis.computePADs();

        // write voxel file
        //voxAnalysis.write(VoxelAnalysisCfg.VoxelsFormat.VOXEL, new java.io.File("/tmp/testUninterceptedShot.vox"));
        
        // assertions on vertical shot
        Voxel voxel;
        for (int k = 0; k < 5; k++) {
            voxel = voxAnalysis.voxels[0][0][k];
            assert (voxel.lgTotal == 1);
            assert (voxel.nbSampling == 1);
            assert (voxel.bvEntering == 1);
            assert (voxel.angleMean == 0);
        }

        // assertions on oblique shot
        voxel = voxAnalysis.voxels[1][1][1];
        assert(voxel.lgTotal == (float) (Math.sqrt(3.d) / 2.d));
        for (int n = 2; n < 5; n++) {
            voxel = voxAnalysis.voxels[n][n][n];
            assert(voxel.lgTotal == (float) (Math.sqrt(3.d)));
            assert (voxel.angleMean == 45);
        }
        
        // assertions on the whole voxel space
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                for (int k = 0; k < 5; k++) {
                    voxel = voxAnalysis.voxels[i][j][k];
                    assert(voxel.bvIntercepted == 0);
                    assert(voxel.nbEchos == 0);
                }
            }
            
        }
    }
}
