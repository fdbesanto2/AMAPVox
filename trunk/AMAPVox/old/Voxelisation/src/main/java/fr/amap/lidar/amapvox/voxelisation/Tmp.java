/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.RxpScan;
import fr.amap.amapvox.io.tls.rsp.Scans;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.util.LidarScan;
import fr.amap.commons.util.MatrixUtility;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.commons.util.vegetation.LADParams;
import fr.amap.lidar.amapvox.commons.Configuration;
import fr.amap.lidar.amapvox.voxelisation.als.PointsToShot;
import fr.amap.lidar.amapvox.voxelisation.als.PointsToShotIterator;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.DTMFilteringParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.NaNsCorrectionParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author calcul
 */
public class Tmp {
    
    public static void tls() throws Exception{
        
        ProcessTool tool = new ProcessTool();
        
        TLSVoxCfg tlsVoxCfg = new TLSVoxCfg();
        
        List<LidarScan> lidarScans = new ArrayList<>();
        Rsp rsp = new Rsp();
        rsp.read(new File("/media/forestview01/BDLidar/TLS/Paracou2013/Paracou2013complet.RISCAN/project.rsp"));
        
        ArrayList<Scans> scansList = rsp.getRxpList();
        
        for(Scans scans : scansList){
            RxpScan scan = scans.getScanLite();
            
            if(!scans.getName().equals("ScanPos053")){
                lidarScans.add(new LidarScan(scan.getFile(), MatrixUtility.convertMat4DToMatrix4d(scans.getSopMatrix()), scans.getName()));
            }
        }
        
        tlsVoxCfg.setLidarScans(lidarScans);
        tlsVoxCfg.setPopMatrix(MatrixUtility.convertMat4DToMatrix4d(rsp.getPopMatrix()));
        tlsVoxCfg.setShotFilter(new ShotFilter() {
            @Override
            public boolean doFiltering(Shot shot) {
                return shot.angle <= 20;
            }
        });
        
        Mat4D vopMatrix = new Mat4D();
        vopMatrix.mat = new double[]{0.9540688863574789, 0.29958731629459895, 0.0, -448120.0441687209,
            -0.29958731629459895, 0.9540688863574789, 0.0, -470918.3928060016,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0};
        
        tlsVoxCfg.setVopMatrix(MatrixUtility.convertMat4DToMatrix4d(vopMatrix));
        tlsVoxCfg.setInputFile(new File(""));
        tlsVoxCfg.setOutputFile(new File("/home/calcul/Documents/Julien/tests_comparaison_trajets_optiques"));
        
        EchoesWeightParams echoesWeightParams = new EchoesWeightParams();
        echoesWeightParams.setWeightingData(EchoesWeightParams.DEFAULT_TLS_WEIGHTING);
        echoesWeightParams.setWeightingMode(EchoesWeightParams.WEIGHTING_ECHOS_NUMBER);
        
        VoxelParameters params = new VoxelParameters(new Point3d(-12, 100, 38), new Point3d(12, 140, 39), new Point3i(24, 40, 1),
                1.0f, //resolution
                3.53f, //max PAD
                new LADParams(), //spherical distribution
                echoesWeightParams, //no ponderation
                VoxelAnalysis.LaserSpecification.VZ_400,
                new NaNsCorrectionParams(false), //no NA correction
                new DTMFilteringParams(), //no DTM filter
                false); //ALS
        
        tlsVoxCfg.setVoxelParameters(params);
        
        tool.setCoresNumber(1);
        
        ArrayList<File> outputFiles = tool.voxeliseFromRsp(tlsVoxCfg);
        tool.mergeVoxelFiles(new VoxMergingCfg(new File("/home/calcul/Documents/Julien/tests_comparaison_trajets_optiques/merged.vox"), params, outputFiles));

    }
    
    public static void als() throws Exception{
        
        VoxelAnalysisCfg cfg = new VoxelAnalysisCfg();
        
        EchoesWeightParams echoesWeightParams = new EchoesWeightParams();
        echoesWeightParams.setWeightingData(EchoesWeightParams.DEFAULT_ALS_WEIGHTING);
        echoesWeightParams.setWeightingMode(EchoesWeightParams.WEIGHTING_ECHOS_NUMBER);
        
        VoxelParameters params = new VoxelParameters(new Point3d(-12, 100, 38), new Point3d(12, 140, 39), new Point3i(1, 1, 1),
                1.0f, //resolution
                3.53f, //max PAD
                new LADParams(), //spherical distribution
                echoesWeightParams, //no ponderation
                VoxelAnalysis.LaserSpecification.DEFAULT_ALS,
                new NaNsCorrectionParams(false), //no NA correction
                new DTMFilteringParams(), //no DTM filter
                false); //ALS
        
        cfg.setVoxelParameters(params);
        
        Mat4D vopMatrix = new Mat4D();
        vopMatrix.mat = new double[]{0.9540688863574789, 0.29958731629459895, 0.0, -448120.0441687209,
            -0.29958731629459895, 0.9540688863574789, 0.0, -470918.3928060016,
            0.0, 0.0, 1.0, 0.0,
            0.0, 0.0, 0.0, 1.0};
        
        TmpVoxelAnalysis voxAnalysis = new TmpVoxelAnalysis(null, null, cfg);
        voxAnalysis.init(params, new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/test.vox"));
        voxAnalysis.createVoxelSpace();
        
        CSVFile trajectoryFile = new CSVFile("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/sbet_250913_01.txt");
        PointsToShot pointsToShot = new PointsToShot(trajectoryFile, //trajectory file
                new File("/home/calcul/Documents/Julien/samples_transect_sud_paracou_2013_ALS/ALSbuf_xyzirncapt.las"), //als file
                vopMatrix);
        
        pointsToShot.init();
        PointsToShotIterator iterator = pointsToShot.iterator();
        
        Shot shot;
        
        while((shot = iterator.next()) != null){
            
            voxAnalysis.processOneShot(shot);
        }
        
        voxAnalysis.write();
    }
 
    public static void main(String[] args) throws Exception {
        
        als();
    }
}
