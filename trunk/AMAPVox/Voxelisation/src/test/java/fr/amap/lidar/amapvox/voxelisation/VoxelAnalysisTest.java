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
import junit.framework.TestCase;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

/**
 *
 * @author calcul
 */
public class VoxelAnalysisTest extends TestCase{
    
    private static final VoxelAnalysis voxelAnalysis;
    
    public VoxelAnalysisTest() {
        
        //voxelAnalysis.write();
        
    }
    
    static{
        voxelAnalysis = new VoxelAnalysis(null, null, new VoxelAnalysisCfg());
        VoxelParameters parameters = new VoxelParameters(new Point3d(0, 0, 0),
                                                        new Point3d(4, 4, 4),
                                                        new Point3i(4, 4, 4));
        
        parameters.infos.setResolution(1.0);
        parameters.infos.setMaxPAD(3.5f);
        parameters.infos.setType(Type.ALS);
        parameters.getEchoesWeightParams().setWeightingData(EchoesWeightParams.DEFAULT_ALS_WEIGHTING);
        parameters.setLadParams(new LADParams());
        
        
        voxelAnalysis.init(parameters, new File("C:\\Users\\Julien\\Documents\\test.vox"));
        voxelAnalysis.createVoxelSpace();
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    
    public void allTest() throws Exception {
        
        testPathLength();
    }
    
    @Test
    public void testPathLength() throws Exception{
        
        List<Shot> shots = new ArrayList<>();
        shots.add(new Shot(3, new Point3d(0.5, 0.5, 0.5), new Vector3d(1, 0, 0), new double[]{2, 3.2, 3.4}, new int[3], new float[3]));
        
        for(Shot shot : shots){
            voxelAnalysis.processOneShot(shot);
        }
        
        voxelAnalysis.computePADs();
        
        //test optical path length
        assertEquals(0.5, voxelAnalysis.getVoxels()[0][0][0].lMeanTotal, 0);
        assertEquals(1.0, voxelAnalysis.getVoxels()[1][0][0].lMeanTotal, 0);
        assertEquals(1.0, voxelAnalysis.getVoxels()[2][0][0].lMeanTotal, 0);
        assertEquals(0.9, voxelAnalysis.getVoxels()[3][0][0].lMeanTotal, 0.0000001);
        
        assertEquals(0.5, voxelAnalysis.getVoxels()[0][0][0].lgTotal, 0);
        assertEquals(1.0, voxelAnalysis.getVoxels()[1][0][0].lgTotal, 0);
        assertEquals(1.0, voxelAnalysis.getVoxels()[2][0][0].lgTotal, 0);
        assertEquals(0.9, voxelAnalysis.getVoxels()[3][0][0].lgTotal, 0.0000001);
        
        //
        
        voxelAnalysis.write();
        
    }
    
    public void testMultiEcho(){
        
    }
    
}
