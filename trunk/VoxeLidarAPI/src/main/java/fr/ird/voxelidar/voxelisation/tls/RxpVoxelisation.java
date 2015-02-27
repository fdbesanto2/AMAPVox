/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.tls;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelAnalysis;
import fr.ird.voxelidar.voxelisation.VoxelParameters;
import fr.ird.voxelidar.voxelisation.extraction.RxpExtraction;
import fr.ird.voxelidar.voxelisation.extraction.RxpExtractionListener;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.engine3d.math.matrix.Mat3D;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class RxpVoxelisation{
    
    private static final Logger logger = Logger.getLogger(RxpVoxelisation.class);
    private RxpScan rxp;
    private Mat4D popMatrix;
    private VoxelParameters parameters;
    //private VoxelAnalysis voxelAnalysis;
    private LinkedBlockingQueue<Shot> queue;
    private int nbVoxelisationFinished;
    private File outputFile;

    public int getNbVoxelisationFinished() {
        return nbVoxelisationFinished;
    }

    public void setNbVoxelisationFinished(int nbVoxelisationFinished) {
        this.nbVoxelisationFinished = nbVoxelisationFinished;
    }    
    
    public RxpVoxelisation(RxpScan rxp, File outputFile, Mat4D popMatrix, VoxelParameters parameters){
        
        this.rxp = rxp;
        this.popMatrix = popMatrix;
        this.parameters = parameters;
        
        nbVoxelisationFinished = 0;
        this.outputFile = outputFile;
    }
    public void voxelise(){
        
        queue = new LinkedBlockingQueue<>();
        
        final VoxelAnalysis voxelAnalysis = new VoxelAnalysis(queue, null);
        
        voxelAnalysis.init(parameters, outputFile);
        
        Mat4D sopMatrix;
            
        sopMatrix = rxp.getSopMatrix();

        if (popMatrix == null) {
            popMatrix = Mat4D.identity();
        }
        final Mat4D transfMatrix = Mat4D.multiply(popMatrix, sopMatrix);
        
        final Mat3D rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };
        
        //voxelAnalysis.setTransfMatrix(transfMatrix);
        //voxelAnalysis.setRotation(rotation);
            logger.info("rxp extraction is started");
            
            RxpExtraction extraction = new RxpExtraction(rxp.getFile(), queue, transfMatrix, rotation);
            
            extraction.addRxpExtractionListener(new RxpExtractionListener() {

                @Override
                public void isFinished() {
                    
                    voxelAnalysis.setIsFinished(true);
                }
            });
            
            //runnable to do the extraction
            new Thread(extraction).start();
            
            //runnable to get the extracted shots
            Thread t = new Thread(voxelAnalysis);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
            
        try {
            //wait until extraction is finished
            t.join();
            
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(RxpVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
