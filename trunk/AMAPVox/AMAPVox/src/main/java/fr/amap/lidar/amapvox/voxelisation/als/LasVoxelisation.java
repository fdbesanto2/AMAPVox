/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.als;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.util.Filter;
import fr.amap.commons.util.Process;
import fr.amap.commons.util.ProcessingListener;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.raster.multiband.BSQ;
import fr.amap.commons.util.MatrixUtility;
import fr.amap.lidar.amapvox.voxelisation.SimpleShotFilter;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.configuration.ALSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.RasterParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxelisation.postproc.MultiBandRaster;
import fr.amap.lidar.amapvox.voxelisation.postproc.NaNsCorrection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;
import fr.amap.commons.util.Cancellable;
import fr.amap.commons.util.ProcessingAdapter;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.lidar.format.shot.ShotReader;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasVoxelisation extends Process implements Cancellable{
    
    private boolean cancelled;
    
    private final static Logger logger = Logger.getLogger(LasVoxelisation.class);
    
    private List<Integer> classifiedPointsToDiscard;
    private static boolean update;
    private PointsToShot conversion;
    private Raster terrain = null;
    private NaNsCorrection naNsCorrection = null;
    private VoxelAnalysis voxelAnalysis = null;

    public LasVoxelisation() {
        
        LasVoxelisation.update = true;
    }
    
    /**
     * 
     * @return if true, las file, trajectory file and DTM file will not be reloaded
     */
    public boolean isUpdate() {
        return update;
    }

    /**
     * 
     * @param update if true, las file, trajectory file and DTM file will not be reloaded
     */
    public void setUpdate(boolean update) {
        LasVoxelisation.update = update;
    }

    public File process(ALSVoxCfg cfg) throws Exception {
        
        setCancelled(false);
        
        if(cfg.getClassifiedPointsToDiscard() == null){
           this.classifiedPointsToDiscard = new ArrayList<>();
        }else{
           this.classifiedPointsToDiscard = cfg.getClassifiedPointsToDiscard();
        }
        
        if(!this.classifiedPointsToDiscard.contains(2)){ //work around for old cfg file version
            this.classifiedPointsToDiscard.add(2);
        }
        
        cfg.setEchoFilter(new LasEchoFilter(cfg.getEchoFilters(), classifiedPointsToDiscard));
        cfg.setShotFilter(new SimpleShotFilter(cfg.getShotFilters()));
        
        Mat4D transfMatrix = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        if (transfMatrix == null) {
            transfMatrix = Mat4D.identity();
        }
        
        if(update || terrain == null){
            
            if(cfg.getVoxelParameters().getDtmFilteringParams().getDtmFile() != null && cfg.getVoxelParameters().getDtmFilteringParams().useDTMCorrection() ){
            
                fireProgress("Reading DTM file", 0, 100);

                try {
                    terrain = AsciiGridHelper.readFromAscFile(cfg.getVoxelParameters().getDtmFilteringParams().getDtmFile());
                    terrain.setTransformationMatrix(transfMatrix);
                } catch (Exception ex) {
                    throw ex;
                }
            } 
        }
        
        voxelAnalysis = new VoxelAnalysis(terrain, null, cfg);
        voxelAnalysis.init(cfg.getVoxelParameters(), cfg.getOutputFile());
        voxelAnalysis.createVoxelSpace();
        
        if(FileManager.getExtension(cfg.getInputFile()).equals(".sht")){
            
            ShotReader shotReader = new ShotReader(cfg.getInputFile());
            
            fireProgress("Voxelisation", 0, 100);

            Iterator<Shot> iterator = shotReader.iterator();

            long shotId = 0;
            long nbShots = shotReader.getShotNumber();

            while(iterator.hasNext()){
                
                if(isCancelled()){
                    return null;
                }
                
                Shot shot = iterator.next();

                fireProgress("Voxelisation...", shotId, nbShots);
                
                voxelAnalysis.processOneShot(shot);
                
                shotId++;
            }
            
            shotReader.close();
            
            logger.info("Shots processed: "+voxelAnalysis.getNbShotsProcessed());
            
        }else{
            if(update || conversion == null){
            
                conversion = new PointsToShot(cfg.getTrajectoryFile(), cfg.getInputFile(), transfMatrix);

                conversion.addProcessingListener(new ProcessingListener() {

                    @Override
                    public void processingStepProgress(String progressMsg, long progress, long max) {

                        fireProgress(progressMsg, progress, max);
                    }

                    @Override
                    public void processingFinished(float duration) {
                        fireFinished(duration);
                    }
                });

                try {
                    conversion.init();
                } catch (IOException ex) {
                    logger.error(ex);
                    throw ex;
                } catch (Exception ex) {
                    logger.error(ex);
                    throw ex;
                }
            }else{

            }

            fireProgress("Voxelisation", 0, 100);

            PointsToShotIterator iterator = conversion.iterator();

            Shot shot;

            while((shot = iterator.next()) != null){

                if(isCancelled()){
                    return null;
                }

                fireProgress("Voxelisation...", iterator.getNbPointsProcessed(), iterator.getNbPoints());

                voxelAnalysis.processOneShot(shot);
            }

            logger.info("Shots processed: "+voxelAnalysis.getNbShotsProcessed());
        }
        
        
        
        RasterParams rasterParameters = cfg.getVoxelParameters().getRasterParams();
            
        boolean write = false;
        
        if(rasterParameters != null){

            if(rasterParameters.isGenerateMultiBandRaster()){

                fireProgress("Compute multi-band raster", 0, 100);
                
                BSQ raster = MultiBandRaster.computeRaster(rasterParameters.getRasterStartingHeight(),
                                                rasterParameters.getRasterHeightStep(), 
                                                rasterParameters.getRasterBandNumber(), 
                                                rasterParameters.getRasterResolution(),
                                                cfg.getVoxelParameters().infos,
                                                voxelAnalysis.getVoxels(),
                                                voxelAnalysis.getDtm());
                    
                fireProgress("Write multi-band raster", 0, 100);
                
                MultiBandRaster.writeRaster(new File(cfg.getOutputFile().getAbsolutePath()+".bsq"), raster);

                if(!rasterParameters.isShortcutVoxelFileWriting()){
                    write = true;
                }
            }
        }else{
            
            write = true;
        }
        
        if(write){
            
            voxelAnalysis.computePADs();
                    
            if(cfg.getVoxelParameters().getNaNsCorrectionParams().isActivate()){
                
                fireProgress("NA correction", 0, 100);
                
                naNsCorrection = new NaNsCorrection();
                naNsCorrection.correct(cfg.getVoxelParameters(), voxelAnalysis.getVoxels());
            }
            
            voxelAnalysis.addProcessingListener(new ProcessingAdapter() {
                @Override
                public void processingStepProgress(String progressMsg, long progress, long max) {
                    fireProgress(progressMsg, progress, max);
                }
            });

            voxelAnalysis.write(cfg.getVoxelsFormat());
        }

        if(cfg.getVoxelParameters().getGroundEnergyParams() != null &&
                cfg.getVoxelParameters().getGroundEnergyParams().isCalculateGroundEnergy()){
            voxelAnalysis.writeGroundEnergy();
        }

        return cfg.getOutputFile();
    }
    
    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        
        if(naNsCorrection != null){
            naNsCorrection.setCancelled(cancelled);
        }
        
        if(conversion != null){
            conversion.setCancelled(cancelled);
        }
        
        if(voxelAnalysis != null){
            voxelAnalysis.setCancelled(cancelled);
        }
    }
    
}
