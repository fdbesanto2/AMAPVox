package fr.amap.lidar.amapvox.voxelisation;



/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.commons.util.vegetation.LeafAngleDistribution;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos.Type;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightParams;
import fr.amap.commons.util.vegetation.LADParams;
import fr.amap.commons.util.vegetation.LADParams;
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

/**
 *
 * @author calcul
 */
public class VoxelAnalysisTest {
    
    
    public VoxelAnalysisTest() {
        
        VoxelAnalysis voxelAnalysis = new VoxelAnalysis(null, null, new VoxelAnalysisCfg());
        VoxelParameters parameters = new VoxelParameters(new Point3d(-10, -10, -10),
                                                        new Point3d(10, 10, 10),
                                                        new Point3i(20, 20, 20));
        
        parameters.infos.setResolution(1.0);
        parameters.infos.setMaxPAD(3.5f);
        parameters.infos.setType(Type.ALS);
        parameters.getEchoesWeightParams().setWeightingData(EchoesWeightParams.DEFAULT_ALS_WEIGHTING);
        parameters.setLadParams(new LADParams());
        
        
        voxelAnalysis.init(parameters, new File("/home/calcul/Documents/Julien/test.vox"));
        voxelAnalysis.createVoxelSpace();
        
        List<Shot> shots = new ArrayList<>();
        shots.add(new Shot(4, new Point3d(0, 0, 15), new Vector3d(0, 0, -1), new double[]{6, 14, 18, 21}, new int[4], new float[4]));
        
        for(Shot shot : shots){
            voxelAnalysis.processOneShot(shot);
        }
        
        voxelAnalysis.computePADs();
        //voxelAnalysis.write();
        
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
    public void myTest() {
        new VoxelAnalysisTest();
    }
    
}
