/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.util.vegetation.LADParams;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Julien
 */
public class VoxelAnalysisV2Test {
    
    private static final VoxelAnalysisV2 voxelAnalysis;
    
    static{
        
        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        
        VoxelParameters parameters = new VoxelParameters(new Point3d(0, 0, 0),
                                                        new Point3d(4, 4, 4),
                                                        new Point3i(4, 4, 4));
        
        parameters.infos.setResolution(1.0);
        parameters.infos.setMaxPAD(3.5f);
        parameters.infos.setType(VoxelSpaceInfos.Type.ALS);
        parameters.getEchoesWeightParams().setWeightingData(EchoesWeightParams.DEFAULT_ALS_WEIGHTING);
        parameters.setLadParams(new LADParams());
        
        cfg.setVoxelParameters(parameters);
        
        voxelAnalysis = new VoxelAnalysisV2(cfg);
        
        List<Shot> shots = new ArrayList<>();
        shots.add(new Shot(3, new Point3d(0.5, 0.5, 0.5), new Vector3d(1, 0, 0), new double[]{2, 3.2, 3.4}, new int[3], new float[3]));
        
        for(Shot shot : shots){
            voxelAnalysis.processOneShot(shot);
        }
        
    }
    
    public VoxelAnalysisV2Test() {
        
        
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }
    
    @Test
    public void testProcessOneShot() {
        
        new VoxelAnalysisV2(null);
    }
    
}
