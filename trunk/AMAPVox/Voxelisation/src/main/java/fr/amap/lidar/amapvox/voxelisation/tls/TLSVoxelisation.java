/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.SimpleShotFilter;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * @author calcul
 */
public abstract class TLSVoxelisation implements Callable{

    protected int nbVoxelisationFinished;
    protected final File inputFile;
    protected VoxelAnalysis voxelAnalysis;
    protected final Mat4D transfMatrix;
    protected final Mat3D rotation;
    protected final VoxelParameters parameters;
    protected final File outputFile;
    
    public TLSVoxelisation(File inputFile, File outputFile, Mat4D vopMatrix, Mat4D popMatrix, Mat4D sopMatrix, VoxelParameters parameters, Raster terrain, List<PointcloudFilter> pointcloud, VoxelAnalysisCfg cfg){
        nbVoxelisationFinished = 0;
        this.inputFile = inputFile;
        this.parameters = parameters;
        this.outputFile = outputFile;
        
        if(popMatrix == null){
            popMatrix = Mat4D.identity();
        }
        if(vopMatrix == null){
            vopMatrix = Mat4D.identity();
        }
        Mat4D popVop = Mat4D.multiply(popMatrix, vopMatrix);
        transfMatrix = Mat4D.multiply(sopMatrix, popVop);
        
        rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };
        
        if(terrain != null){
            terrain.setTransformationMatrix(vopMatrix);
        }
        
        cfg.setShotFilter(new SimpleShotFilter(cfg.getShotFilters()));
        
        voxelAnalysis = new VoxelAnalysis(terrain, pointcloud, cfg);
        voxelAnalysis.init(parameters, outputFile);
    }
    
    public int getNbVoxelisationFinished() {
        return nbVoxelisationFinished;
    }

    public void setNbVoxelisationFinished(int nbVoxelisationFinished) {
        this.nbVoxelisationFinished = nbVoxelisationFinished;
    }   
    
}
