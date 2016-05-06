/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.tls;

import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.io.tls.rxp.ShotFilter;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.raster.multiband.BSQ;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.SimpleShotFilter;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
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
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RxpVoxelisation extends TLSVoxelisation{
    
    private final static Logger LOGGER = Logger.getLogger(RxpVoxelisation.class);
    
    private final boolean enableEmptyShotFiltering;
    
    public RxpVoxelisation(File inputFile, File outputFile, Mat4D vopMatrix, Mat4D popMatrix, Mat4D sopMatrix,
            VoxelParameters parameters, Raster terrain, List<PointcloudFilter> pointcloud, VoxelAnalysisCfg cfg, boolean enableEmptyShotFiltering) {
        super(inputFile, outputFile, vopMatrix, popMatrix, sopMatrix, parameters, terrain, pointcloud, cfg);
        
        this.enableEmptyShotFiltering = enableEmptyShotFiltering;
    }

    @Override
    public Object call() throws Exception {
        
        System.out.println(Thread.currentThread().getName());
        
        try {
            LOGGER.info("rxp extraction is started");
        
            voxelAnalysis.createVoxelSpace();
            RxpExtraction rxpExtraction = new RxpExtraction();
            int result = rxpExtraction.openRxpFile(inputFile, RxpExtraction.REFLECTANCE, RxpExtraction.DEVIATION);
            
            if(result != 0){
                LOGGER.error("Extraction aborted");
                return null;
            }
            
            Iterator<Shot> iterator = rxpExtraction.iterator();
            
            if(enableEmptyShotFiltering){
                
                ShotFilter shotFilter = new ShotFilter(iterator);
                
                Iterator<Shot> filterIterator = shotFilter.iterator();
                
                Shot shot;
                while(filterIterator.hasNext()){

                    if (Thread.currentThread().isInterrupted()){
                        LOGGER.info("Task cancelled");
                        return null;
                    }

                    shot = filterIterator.next();
                    if(shot != null){
                        Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));

                        Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

                        shot.setOriginAndDirection(new Point3d(locVector.x, locVector.y, locVector.z), new Vector3d(uVector.x, uVector.y, uVector.z));

                        voxelAnalysis.processOneShot(shot);
                    }
                }
                
                LOGGER.info("Number of shots removed : "+shotFilter.getNbThrownShots());
                
            }else{
                Shot shot;
                while(iterator.hasNext()){

                    if (Thread.currentThread().isInterrupted()){
                        LOGGER.info("Task cancelled");
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
            }
            
            LOGGER.info("Shots processed: "+voxelAnalysis.getNbShotsProcessed());
            
            //logger.info("Shots processed: " + voxelAnalysis.nbShotsTreated);
            //logger.info("voxelisation is finished ( " + TimeCounter.getElapsedStringTimeInSeconds(startTime) + " )");

            rxpExtraction.close();
            
            super.postProcess();
            
            /*RasterParams rasterParameters = parameters.getRasterParams();
            
            boolean write = false;

            if(rasterParameters != null){

                if(rasterParameters.isGenerateMultiBandRaster()){

                    BSQ raster = MultiBandRaster.computeRaster(rasterParameters.getRasterStartingHeight(),
                                                rasterParameters.getRasterHeightStep(), 
                                                rasterParameters.getRasterBandNumber(), 
                                                rasterParameters.getRasterResolution(),
                                                parameters.infos,
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
            */
            
            
            //return resultData;
        
        }catch(OutOfMemoryError | Exception ex){
            throw ex;
            //logger.error("Unsufficient memory, you need to allocate more to the JVM, change the Xmx value!",ex);
        }
        //logger.error("Unknow exception in RXPVoxelisation.class in thread : "+Thread.currentThread().getName()+", retrying",ex);
        //this.call();
        
        return null;
    }
}
