/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jleica.LShot;
import fr.amap.amapvox.jleica.ptg.PTGScan;
import fr.amap.amapvox.jleica.LPointShotExtractor;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.RasterParams;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class PTGVoxelisation extends TLSVoxelisation{
    
    private final static Logger logger = Logger.getLogger(PTGVoxelisation.class);

    public PTGVoxelisation(File inputFile, File outputFile, Mat4D vopMatrix, Mat4D popMatrix, Mat4D sopMatrix, VoxelParameters parameters, Raster terrain, List<PointcloudFilter> pointcloud, VoxCfg cfg) {
        super(inputFile, outputFile, vopMatrix, popMatrix, sopMatrix, parameters, terrain, pointcloud, cfg);
    }

    @Override
    public Object call() throws Exception {
        
        try {
            logger.info("ptg extraction is started");
            
            long startTime = System.currentTimeMillis();
        
            voxelAnalysis.createVoxelSpace();
            
            PTGScan pTGScan = new PTGScan();
            pTGScan.openScanFile(inputFile);
            
            LPointShotExtractor pTGShots = new LPointShotExtractor(pTGScan);
            
            Iterator<LShot> iterator = pTGShots.iterator();

            Shot shot;
            while(iterator.hasNext()){
                
                if (Thread.currentThread().isInterrupted()){
                    logger.info("Task cancelled");
                    return null;
                }

                shot = iterator.next();
                if(shot != null){
                    Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));

                    Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

                    shot.setOriginAndDirection(new Point3d(locVector.x, locVector.y, locVector.z), new Vector3d(uVector.x, uVector.y, uVector.z));
                                        
                    voxelAnalysis.processOneShot(shot);
                }

            }
            
            logger.info("Shots processed: "+voxelAnalysis.getNbShotsProcessed());   
            
            RasterParams rasterParameters = parameters.getRasterParams();
            
            boolean write = false;

            if(rasterParameters != null){

                if(rasterParameters.isGenerateMultiBandRaster()){

                    voxelAnalysis.generateMultiBandsRaster(new File(outputFile.getAbsolutePath()+".bsq"), 
                    rasterParameters.getRasterStartingHeight(), rasterParameters.getRasterHeightStep(), 
                    rasterParameters.getRasterBandNumber(), rasterParameters.getRasterResolution());

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
            logger.error("Unsufficient memory, you need to allocate more to the JVM, change the Xmx value!",ex);
        }catch(Exception ex){
            logger.error("Unknow exception in RXPVoxelisation.class in thread : "+Thread.currentThread().getName()+", retrying",ex);
            this.call();
        }
        
        return null;
    }
    
    
}