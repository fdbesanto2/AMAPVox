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
import fr.ird.voxelidar.engine3d.math.matrix.Mat3D;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.util.Filter;
import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class RxpVoxelisation implements Callable{
    
    private final static Logger logger = Logger.getLogger(RxpVoxelisation.class);
    private Mat4D vopPop;
    private final VoxelParameters parameters;
    private static final int compteur = 1;
    //private VoxelAnalysis voxelAnalysis;
    private final LinkedBlockingQueue<Shot> queue;
    private int nbVoxelisationFinished;
    private final File outputFile;
    private final VoxelAnalysis voxelAnalysis;
    private final RxpExtraction extraction;
    private final Thread extractionThread;
    private final Thread voxelAnalysisThread;

    public int getNbVoxelisationFinished() {
        return nbVoxelisationFinished;
    }

    public void setNbVoxelisationFinished(int nbVoxelisationFinished) {
        this.nbVoxelisationFinished = nbVoxelisationFinished;
    }    
    
    public RxpVoxelisation(File inputFile, File outputFile, Mat4D vopMatrix, Mat4D popMatrix, Mat4D sopMatrix, VoxelParameters parameters, Dtm terrain, List<Filter> filters){
        
        //this.vopPop = vopPop;
        this.parameters = parameters;
        
        nbVoxelisationFinished = 0;
        this.outputFile = outputFile;
        
        
        

        if (vopPop == null) {
            vopPop = Mat4D.identity();
        }
        
        //final Mat4D transfMatrix = Mat4D.multiply(Mat4D.multiply(popMatrix, sopMatrix), vopMatrix);
        //final Mat4D transfMatrix = Mat4D.identity();
        Mat4D popVop = Mat4D.multiply(popMatrix, vopMatrix);
        final Mat4D transfMatrix = Mat4D.multiply(sopMatrix, popVop);
        
        final Mat3D rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };
        terrain.setTransformationMatrix(vopMatrix);
        
        queue = new LinkedBlockingQueue<>();
        voxelAnalysis = new VoxelAnalysis(queue, terrain, filters);
        voxelAnalysis.init(parameters, outputFile);
        
        extraction = new RxpExtraction(inputFile, queue, transfMatrix, rotation);
        extractionThread = new Thread(extraction);
        voxelAnalysisThread = new Thread(voxelAnalysis);
        
        extraction.addRxpExtractionListener(new RxpExtractionListener() {

            @Override
            public void isFinished() {

                voxelAnalysis.setIsFinished(true);
            }
        });
    }

    @Override
    public Object call() {
        
        try {
            

            //voxelAnalysis.setTransfMatrix(transfMatrix);
            //voxelAnalysis.setRotation(rotation);
            logger.info("rxp extraction is started");
            
            extractionThread.start();
            //extractionThread.setPriority(Thread.MIN_PRIORITY);
            
            voxelAnalysisThread.start();
            //wait until extraction is finished
            voxelAnalysisThread.join();
            
            
        }catch (InterruptedException ex) {
            logger.error(ex.getMessage());
        }catch(Exception e){
            logger.error(e.getMessage());
        }
        
        return null;
    }
}