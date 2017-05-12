/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.als;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.util.Process;
import fr.amap.commons.util.ProcessingListener;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.lidar.amapvox.voxelisation.SimpleShotFilter;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.lidar.amapvox.voxelisation.configuration.ALSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.postproc.NaNsCorrection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import fr.amap.commons.util.Cancellable;
import fr.amap.commons.util.ProcessingAdapter;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.lidar.amapvox.commons.Configuration;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.shot.Shot;
import java.io.BufferedReader;
import java.io.FileReader;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

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
                    
                    if(cfg.getVoxelParameters().getDtmFilteringParams().isUseVOPMatrix()){
                        terrain.setTransformationMatrix(transfMatrix);
                    }
                    
                } catch (Exception ex) {
                    throw ex;
                }
            } 
        }
        
        voxelAnalysis = new VoxelAnalysis(terrain, null, cfg);
        voxelAnalysis.createVoxelSpace();
        
        if(cfg.getInputType() == Configuration.InputType.SHOTS_FILE){
            
            BufferedReader reader = new BufferedReader(new FileReader(cfg.getInputFile()));
            
            fireProgress("Voxelisation", 0, 100);

            long shotId = 0;
            long nbShots = FileManager.getLineNumber(cfg.getInputFile());
            
            String line;
            
            //skip header
            reader.readLine();
            
            while((line = reader.readLine()) != null){
                String[] split = line.split(" ");
                
                if(isCancelled()){
                    return null;
                }
                
                double xOrigin = Double.valueOf(split[0]);
                double yOrigin = Double.valueOf(split[1]);
                double zOrigin = Double.valueOf(split[2]);
                
                double xDirection = Double.valueOf(split[3]);
                double yDirection = Double.valueOf(split[4]);
                double zDirection = Double.valueOf(split[5]);
                
                int nbEchos = Integer.valueOf(split[6]);
                
                double[] ranges = new double[nbEchos];
                int[] classifications = new int[nbEchos];
                
                for (int i = 0; i < ranges.length; i++) {
                    
                    if((14+i) > split.length){
                        throw new Exception("Columns missing inside shot file");
                    }
                    
                    ranges[i] = Double.valueOf(split[7+i]);
                    classifications[i] = Integer.valueOf(split[14+i]);
                }
                
                AlsShot shot = new AlsShot(new Point3d(xOrigin, yOrigin, zOrigin), new Vector3d(xDirection, yDirection, zDirection), ranges);
                shot.classifications = classifications;
                shot.setMask(getMask(shot));
                
                fireProgress("Voxelisation...", shotId, nbShots);
                
                voxelAnalysis.processOneShot(new fr.amap.lidar.amapvox.shot.Shot(shot.origin, shot.direction, shot.ranges));
                
                shotId++;
            }
            
            reader.close();
            
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

            AlsShot shot;

            while((shot = iterator.next()) != null){

                if(isCancelled()){
                    return null;
                }

                fireProgress("Voxelisation...", iterator.getNbPointsProcessed(), iterator.getNbPoints());

                shot.setMask(getMask(shot));
                voxelAnalysis.processOneShot(shot);
            }

            logger.info("Shots processed: "+voxelAnalysis.getNbShotsProcessed());
        }
        
//        Voxel[][][] voxels = voxelAnalysis.getVoxels();
//        
        
        //appel code R via JRI
        
        //récupération résultat R
        
        voxelAnalysis.computePADs();

        //à désactiver par défaut car ce traitement ne sera plus nécessaire après correction dans R
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

        voxelAnalysis.write(cfg.getVoxelsFormat(), cfg.getOutputFile());

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
    
    private boolean[] getMask(AlsShot shot){
        
        boolean[] mask = new boolean[shot.getEchoesNumber()];
        
        for(int i = 0 ; i < mask.length ; i++){
            mask[i] = doFiltering(shot, i);
        }
        
        return mask;
    }
    
    private boolean doFiltering(AlsShot shot, int echoID) {
        
        if(shot.classifications != null && !classifiedPointsToDiscard.contains(shot.classifications[echoID])/*shot.classifications[echoID] != 2*/){
            return true;
        }else{
            return false;
        }
    }
    
}
