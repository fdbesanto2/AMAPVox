/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation.tls;

import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jleica.LPointShotExtractor;
import fr.amap.amapvox.jleica.LShot;
import fr.amap.amapvox.jleica.ptx.PTXScan;
import fr.amap.commons.raster.asc.RegularDtm;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.amapvox.voxelisation.PointcloudFilter;
import fr.amap.amapvox.voxelisation.configuration.VoxCfg;
import fr.amap.amapvox.voxelisation.configuration.VoxelParameters;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize
 */
public class PTXVoxelisation extends TLSVoxelisation{

    private final static Logger logger = Logger.getLogger(PTXVoxelisation.class);
    
    private final PTXScan scan;
    
    public PTXVoxelisation(PTXScan scan, File outputFile, Mat4D vopMatrix, Mat4D popMatrix, Mat4D sopMatrix, VoxelParameters parameters, RegularDtm terrain, List<PointcloudFilter> pointcloud, VoxCfg cfg) {
        super(scan.getFile(), outputFile, vopMatrix, popMatrix, sopMatrix, parameters, terrain, pointcloud, cfg);
        this.scan = scan;
    }

    @Override
    public Object call() throws Exception {
        
        try {
            logger.info("ptx extraction is started");
            
            long startTime = System.currentTimeMillis();
        
            voxelAnalysis.createVoxelSpace();
            
            LPointShotExtractor pTXShots = new LPointShotExtractor(scan);
            
            Iterator<LShot> iterator = pTXShots.iterator();

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
            
            if(parameters.isGenerateMultiBandRaster()){
                voxelAnalysis.generateMultiBandsRaster(new File(outputFile.getAbsolutePath()+".bsq"), 
                        parameters.getRasterStartingHeight(), parameters.getRasterHeightStep(), 
                        parameters.getRasterBandNumber(), parameters.getRasterResolution());
            }

            if((parameters.isGenerateMultiBandRaster() && !parameters.isShortcutVoxelFileWriting()) || !parameters.isGenerateMultiBandRaster()){
                voxelAnalysis.computePADs();
                voxelAnalysis.write();
                //voxelAnalysis.calculatePADAndWrite(0);
            }
            

            if(voxelAnalysis.parameters.isCalculateGroundEnergy() && !voxelAnalysis.parameters.isTLS()){
                voxelAnalysis.writeGroundEnergy();
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
