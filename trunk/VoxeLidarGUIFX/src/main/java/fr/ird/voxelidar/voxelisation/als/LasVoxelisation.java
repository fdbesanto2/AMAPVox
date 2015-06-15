/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.als;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelAnalysis;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.lidar.format.dtm.RegularDtm;
import fr.ird.voxelidar.lidar.format.dtm.DtmLoader;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.Processing;
import fr.ird.voxelidar.util.ProcessingListener;
import java.io.File;
import java.util.Iterator;
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
    private File outputFile;
    private VoxelParameters parameters;
    private final List<Trajectory> trajectoryList;
    private final boolean filterLowPoints;
    private final List<Filter> filters;
    private boolean updateALS;
    private PointsToShot conversion;
    private final RegularDtm terrain;

    public LasVoxelisation(File alsFile, File outputFile, Mat4D transfMatrix, VoxelParameters parameters, List<Filter> filters, boolean filterLowPoints, RegularDtm terrain, List<Trajectory> trajectoryList) {

        this.alsFile = alsFile;
        this.outputFile = outputFile;
        this.transfMatrix = transfMatrix;
        this.parameters = parameters;
        this.filterLowPoints = filterLowPoints;
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
            
            conversion = new PointsToShot(trajectoryList, alsFile, transfMatrix, filterLowPoints);
            
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
        

        voxelAnalysis.calculatePADAndWrite(0);

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
