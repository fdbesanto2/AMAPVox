/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.raster.multiband.BSQ;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.SimpleShotFilter;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.RasterParams;
import fr.amap.lidar.amapvox.voxelisation.postproc.MultiBandRaster;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class TLSVoxelization implements Callable<Object>{

    private final static Logger LOGGER = Logger.getLogger(TLSVoxelization.class);
    
    private final Iterator<Shot> shotIterator;
    private VoxelAnalysis voxelAnalysis;
    
    private final VoxelAnalysisCfg cfg;
    private final File outputFile;
    
    private final Mat4D transfMatrix;
    private final Mat3D rotationMatrix;
    
    public TLSVoxelization(Iterator<Shot> shotIterator, Raster terrain, List<PointcloudFilter> pointcloud, VoxelAnalysisCfg cfg, File outputFile, Mat4D transfMatrix){
        
        this.shotIterator = shotIterator;
        this.cfg = cfg;
        this.outputFile = outputFile;
        
        this.transfMatrix = transfMatrix;
        
        rotationMatrix = new Mat3D();
        rotationMatrix.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };
        
        //to move
        /*if(terrain != null){
            terrain.setTransformationMatrix(vopMatrix);
        }*/
        
        cfg.setShotFilter(new SimpleShotFilter(cfg.getShotFilters()));
        
        voxelAnalysis = new VoxelAnalysis(terrain, pointcloud, cfg);
        voxelAnalysis.init(cfg.getVoxelParameters(), outputFile);
    }
    
    @Override
    public Object call() throws Exception {
        
        try {
            LOGGER.info("Extraction is started");
            
            long startTime = System.currentTimeMillis();
        
            voxelAnalysis.createVoxelSpace();

            Shot shot;
            while(shotIterator.hasNext()){
                
                if (Thread.currentThread().isInterrupted()){
                    LOGGER.info("Task cancelled");
                    return null;
                }

                shot = shotIterator.next();
                if(shot != null){
                    Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));

                    Vec3D uVector = Mat3D.multiply(rotationMatrix, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

                    shot.setOriginAndDirection(new Point3d(locVector.x, locVector.y, locVector.z), new Vector3d(uVector.x, uVector.y, uVector.z));
                                        
                    voxelAnalysis.processOneShot(shot);
                }

            }
            
            LOGGER.info("Shots processed: "+voxelAnalysis.getNbShotsProcessed());
            
            RasterParams rasterParameters = cfg.getVoxelParameters().getRasterParams();
            
            boolean write = false;

            if(rasterParameters != null){

                if(rasterParameters.isGenerateMultiBandRaster()){

                    BSQ raster = MultiBandRaster.computeRaster(rasterParameters.getRasterStartingHeight(),
                                                rasterParameters.getRasterHeightStep(), 
                                                rasterParameters.getRasterBandNumber(), 
                                                rasterParameters.getRasterResolution(),
                                                cfg.getVoxelParameters().infos,
                                                voxelAnalysis.getVoxels(),
                                                voxelAnalysis.getDtm());
                    
                    MultiBandRaster.writeRaster(new File(outputFile.getAbsolutePath()+".bsq"), raster);

                    if(!rasterParameters.isShortcutVoxelFileWriting()){
                        write = true;
                    }
                }
            }else{

                write = true;
            }

            if(write){
                voxelAnalysis.computePADs();
                voxelAnalysis.write();
            }
            
            //VoxelAnalysisData resultData = voxelAnalysis.getResultData();
            
            //permet de signaler au garbage collector que cet élément peut être supprimé
            voxelAnalysis = null;
            
            //return resultData;
        
        }catch(OutOfMemoryError ex){
            LOGGER.error("Unsufficient memory, you need to allocate more to the JVM, change the Xmx value!",ex);
        }catch(Exception ex){
            LOGGER.error("Unknow exception in RXPVoxelisation.class in thread : "+Thread.currentThread().getName(), ex);
        }
        
        return null;
    }
    
}
