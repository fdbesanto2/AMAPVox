/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelAnalysis;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.engine3d.object.scene.DtmLoader;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.Processing;
import fr.ird.voxelidar.util.ProcessingListener;
import java.io.File;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LasVoxelisation extends Processing {
    
    private final Logger logger = Logger.getLogger(LasVoxelisation.class);
    
    private File alsFile;
    private final Mat4D transfMatrix;
    //private final File trajectoryFile;
    private File outputFile;
    private VoxelParameters parameters;
    private VoxelAnalysis voxelAnalysis;
    private List<Trajectory> trajectoryList;
    //private LinkedBlockingQueue<Shot> queue;
    private final boolean filterLowPoints;
    private List<Filter> filters;
    private boolean alsFileChanged;
    private boolean updateALS = true;
    private PointsToShot conversion;

    public LasVoxelisation(File alsFile, File outputFile, Mat4D transfMatrix/*, File trajectoryFile*/, VoxelParameters parameters, List<Filter> filters, boolean filterLowPoints) {

        this.alsFile = alsFile;
        this.outputFile = outputFile;
        this.transfMatrix = transfMatrix;
        //this.trajectoryFile = trajectoryFile;
        this.parameters = parameters;
        this.filterLowPoints = filterLowPoints;
        this.filters = filters;
    }
    
    
    public void init(Dtm terrain, List<Trajectory> trajectoryList){
        
        voxelAnalysis = new VoxelAnalysis(null, terrain, filters);
        this.trajectoryList = trajectoryList;
        
    }
    
    public boolean isUpdateALS() {
        return updateALS;
    }

    public void setUpdateALS(boolean updateALS) {
        this.updateALS = updateALS;
    }

    @Override
    public File process() {
        
                
        final long start_time = System.currentTimeMillis();
        
        voxelAnalysis.init(parameters, outputFile);
        voxelAnalysis.createVoxelSpace();
        
        if(updateALS || conversion == null){
            
            conversion = new PointsToShot(voxelAnalysis, trajectoryList, alsFile, transfMatrix, filterLowPoints);
            
            conversion.addProcessingListener(new ProcessingListener() {

                @Override
                public void processingFinished() {
                    
                    voxelAnalysis.setIsFinished(true);
                }

                @Override
                public void processingStepProgress(String progress, int ratio) {
                    fireProgress(progress, ratio);
                }
            });
        }else if(!updateALS){
            conversion.setUpdateALS(updateALS);
        }
        
        try {
            
            Thread t2 = new Thread(conversion);
            
            t2.start();
            
            //Thread t = new Thread(voxelAnalysis);
            //t.start();
            
            //wait until voxelisation finished
            t2.join();
            
            voxelAnalysis.calculatePADAndWrite(0);
            
            if(parameters.isCalculateGroundEnergy() && !parameters.isTLS()){
                voxelAnalysis.writeGroundEnergy();
            }
            
            return outputFile;
            
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        
        return null;
    }   

    public File getAlsFile() {
        return alsFile;
    }

    public void setAlsFile(File alsFile) {
        this.alsFile = alsFile;
        alsFileChanged = true;
        
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
