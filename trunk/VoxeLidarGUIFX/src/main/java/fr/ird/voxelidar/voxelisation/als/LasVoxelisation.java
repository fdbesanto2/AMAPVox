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
    
    private final File alsFile;
    private final Mat4D popMatrix;
    private final File trajectoryFile;
    private final File outputFile;
    private final VoxelParameters parameters;
    private VoxelAnalysis voxelAnalysis;
    private LinkedBlockingQueue<Shot> queue;
    private final boolean filterLowPoints;

    public LasVoxelisation(File alsFile, File outputFile, Mat4D transfMatrix, File trajectoryFile, VoxelParameters parameters, List<Filter> filters, boolean filterLowPoints) {

        this.alsFile = alsFile;
        this.outputFile = outputFile;
        this.popMatrix = transfMatrix;
        this.trajectoryFile = trajectoryFile;
        this.parameters = parameters;
        
        queue = new LinkedBlockingQueue<>();
        
        Dtm terrain = null;
        
        if(parameters.getDtmFile() != null && parameters.useDTMCorrection() ){
            
            try {
                terrain = DtmLoader.readFromAscFile(parameters.getDtmFile());
                terrain.setTransformationMatrix(transfMatrix);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        
        
        voxelAnalysis = new VoxelAnalysis(queue, terrain, filters);
        this.filterLowPoints = filterLowPoints;
    }

    @Override
    public File process() {
        
                
        final long start_time = System.currentTimeMillis();
        
        voxelAnalysis.init(parameters, outputFile);
        final AlsToShot conversion = new AlsToShot(queue, trajectoryFile, alsFile, popMatrix, filterLowPoints);
        
        try {
            
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
            
            Thread t2 = new Thread(conversion);
            
            t2.start();
            
            Thread t = new Thread(voxelAnalysis);
            t.start();
            
            //wait until voxelisation finished
            t.join();
            
            return outputFile;
            
        } catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }
        
        return null;
    }   

}
