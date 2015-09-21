/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation.als;

import fr.amap.amapvox.commons.math.matrix.Mat4D;
import fr.amap.amapvox.commons.util.Filter;
import fr.amap.amapvox.commons.util.Processing;
import fr.amap.amapvox.commons.util.ProcessingListener;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxelisation.VoxelAnalysis;
import fr.amap.amapvox.voxelisation.configuration.VoxelParameters;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasVoxelisation extends Processing {
    
    private final static Logger logger = Logger.getLogger(LasVoxelisation.class);
    
    private File alsFile;
    private final Mat4D transfMatrix;
    private File outputFile;
    private VoxelParameters parameters;
    private final List<Trajectory> trajectoryList;
    private final List<Integer> classifiedPointsToDiscard;
    private final List<Filter> filters;
    private boolean updateALS;
    private PointsToShot conversion;
    private final RegularDtm terrain;

    public LasVoxelisation(File alsFile, File outputFile, Mat4D transfMatrix, VoxelParameters parameters, List<Filter> filters, List<Integer> classifiedPointsToDiscard, RegularDtm terrain, List<Trajectory> trajectoryList) {

        this.alsFile = alsFile;
        this.outputFile = outputFile;
        this.transfMatrix = transfMatrix;
        this.parameters = parameters;
        this.classifiedPointsToDiscard = classifiedPointsToDiscard;
        this.filters = filters;
        this.terrain = terrain;
        this.trajectoryList = trajectoryList;
        this.updateALS = true;
    }
    
    public boolean isUpdateALS() {
        return updateALS;
    }

    public void setUpdateALS(boolean updateALS) {
        this.updateALS = updateALS;
    }

    @Override
    public File process() {
                
        VoxelAnalysis voxelAnalysis = new VoxelAnalysis(terrain, null, filters);
        voxelAnalysis.init(parameters, outputFile);
        voxelAnalysis.createVoxelSpace();
        
        
        if(updateALS || conversion == null){
            
            conversion = new PointsToShot(trajectoryList, alsFile, transfMatrix, classifiedPointsToDiscard);
            
            conversion.addProcessingListener(new ProcessingListener() {

                @Override
                public void processingStepProgress(String progress, int ratio) {
                    fireProgress(progress, ratio);
                }

                @Override
                public void processingFinished() {
                    fireFinished();
                }
            });
            
        }else if(!updateALS){
            conversion.setUpdateALS(updateALS);
        }
        
        conversion.init();
                    
        Iterator<Shot> iterator = conversion.iterator();
        
        Shot shot;
        
        while((shot = iterator.next()) != null){
                        
            voxelAnalysis.processOneShot(shot);
        }
        
        logger.info("Shots processed: "+voxelAnalysis.getNbShotsProcessed());
        
        
        if(parameters.isGenerateMultiBandRaster()){
            voxelAnalysis.generateMultiBandsRaster(new File(outputFile.getAbsolutePath()+".bsq"), 
                    parameters.getRasterStartingHeight(), parameters.getRasterHeightStep(),
                    parameters.getRasterBandNumber(), parameters.getRasterResolution());
        }
        
        if((parameters.isGenerateMultiBandRaster() && !parameters.isShortcutVoxelFileWriting()) || !parameters.isGenerateMultiBandRaster()){
            
            voxelAnalysis.computePADs();
            if(parameters.isCorrectNaNsMode2()){
                voxelAnalysis.correctNaNs();
            }
            
            voxelAnalysis.write();
            //voxelAnalysis.calculatePADAndWrite(0);
        }

        if(parameters.isCalculateGroundEnergy() && !parameters.isTLS()){
            voxelAnalysis.writeGroundEnergy();
        }

        return outputFile;
    }   

    public File getAlsFile() {
        return alsFile;
    }

    public void setAlsFile(File alsFile) {
        this.alsFile = alsFile;
        
    }

    public File getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(File outputFile) {
        this.outputFile = outputFile;
    }

    public VoxelParameters getParameters() {
        return parameters;
    }

    public void setParameters(VoxelParameters parameters) {
        this.parameters = parameters;
    }
    
}
