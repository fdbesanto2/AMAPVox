/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.lidar.amapvox.shot.Shot;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightByRankParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.IOException;
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
     * @throws java.lang.Exception
     */
    @Test
    public void testResolution() throws Exception {
        
        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        
        EchoesWeightByRankParams echoesWeightParams = new EchoesWeightByRankParams(EchoesWeightByRankParams.DEFAULT_ALS_WEIGHTING);
        VoxelParameters params = new VoxelParameters.Builder(new Point3d(-2.5, -2.5, -2.5), new Point3d(2.5, 2.5, 2.5), 1.0f, VoxelSpaceInfos.Type.ALS).echoesWeightByRankParams(echoesWeightParams).laserSpecification(LaserSpecification.LMS_Q560).padMAX(10.0f).build();
        
        params.setTransmittanceMode(2);
        params.setPathLengthMode("B");
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
    
}
