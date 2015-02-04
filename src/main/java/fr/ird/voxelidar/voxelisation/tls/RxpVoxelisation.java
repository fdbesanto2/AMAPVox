/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.tls;

import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelAnalysis;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelParameters;
import fr.ird.voxelidar.extraction.RxpExtraction;
import fr.ird.voxelidar.extraction.RxpExtractionListener;
import fr.ird.voxelidar.extraction.Shot;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.math.matrix.Mat3D;
import fr.ird.voxelidar.math.matrix.Mat4D;
import java.io.File;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class RxpVoxelisation {
    
    private static final Logger logger = Logger.getLogger(RxpVoxelisation.class);
    private RxpScan rxp;
    private Mat4D popMatrix;
    private VoxelParameters parameters;
    //private VoxelAnalysis voxelAnalysis;
    private BlockingQueue<Shot> queue;
    private int nbVoxelisationFinished;
    private int nbTask;
    private File outputFile;

    public int getNbVoxelisationFinished() {
        return nbVoxelisationFinished;
    }

    public void setNbVoxelisationFinished(int nbVoxelisationFinished) {
        this.nbVoxelisationFinished = nbVoxelisationFinished;
    }    
    
    public RxpVoxelisation(RxpScan rxp, int nbTask, File outputFile, Mat4D popMatrix, VoxelParameters parameters){
        
        this.rxp = rxp;
        this.popMatrix = popMatrix;
        this.parameters = parameters;
        
        nbVoxelisationFinished = 0;
        this.outputFile = outputFile;
        
        this.nbTask = nbTask;
    }
    
    public void voxelise(){
        
        queue = new LinkedBlockingQueue<>();
        
        final VoxelAnalysis voxelAnalysis = new VoxelAnalysis(queue);
        
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
        
        try {
            
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
            t.start();
            
            //wait until extraction is finished
            t.join();
            
            /*
            extraction.addRxpExtractionListener(new RxpExtractionListener() {

                @Override
                public void shotExtracted(fr.ird.voxelidar.extraction.Shot shot) {
                    
                    float[] echos = new float[shot.nbShots];
                
                    for (int i = 0; i < shot.nbShots; i++)
                    {
                        echos[i] = (float)shot.echos[i];
                    }

                    Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D((float)shot.beam_origin_x, (float)shot.beam_origin_y, (float)shot.beam_origin_z, 1.0d));
                    Vec3D uVector = Mat3D.multiply(rotation, new Vec3D((float)shot.beam_direction_x, (float)shot.beam_direction_y, (float)shot.beam_direction_z));

                    Point3f loc = new Point3f((float)locVector.x, (float)locVector.y, (float)locVector.z);
                    Vector3f u = new Vector3f((float)uVector.x, (float)uVector.y, (float)uVector.z);
                    Shot s = new Shot(shot.nbShots, loc, u, echos);
                    voxelAnalysis.voxelise(s);
                }
            });
            */
            /*
            long start_time = System.currentTimeMillis();
            
            logger.info("rxp extraction is started");
            extraction.extract(rxp.getFile());
            
            logger.info("rxp extraction is finished ( "+TimeCounter.getElapsedTimeInSeconds(start_time)+" )");
            */
            /*
            for(fr.ird.voxelidar.extraction.Shot shot: shots.shotList){
                
                float[] echos = new float[shot.nbShots];
                
                for (int i = 0; i < shot.nbShots; i++)
                {
                    echos[i] = (float)shot.echos[i];
                }

                Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D((float)shot.beam_origin_x, (float)shot.beam_origin_y, (float)shot.beam_origin_z, 1.0d));
                Vec3D uVector = Mat3D.multiply(rotation, new Vec3D((float)shot.beam_direction_x, (float)shot.beam_direction_y, (float)shot.beam_direction_z));
                
                loc = new Point3f((float)locVector.x, (float)locVector.y, (float)locVector.z);
                u = new Vector3f((float)uVector.x, (float)uVector.y, (float)uVector.z);
                s = new Shot(shot.nbShots, loc, u, echos);
                voxelAnalysis.voxelise(s);
                
                shot = null;
            }
            */
            //shots.shotList.clear();
            
            
            //voxelAnalysis.calculatePAD(0);
        
            
            
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(RxpVoxelisation.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /*
    @Override
    public File process() {
        
        Mat4D sopMatrix;
            
        sopMatrix = rxp.getSopMatrix();

        if (popMatrix == null) {
            popMatrix = Mat4D.identity();
        }

        Mat4D transfMatrix = Mat4D.multiply(popMatrix, sopMatrix);

        doExec(rxp.getFile(), transfMatrix);
        
        return outputFile;
    }
    
    private void doExec(File inputFile, Mat4D transfMatrix) {

        Process p;
        try {
            String command = Constants.PROGRAM_RXP_READER + " -0 " + "\"" + inputFile.getAbsolutePath() + "\"";
            p = Runtime.getRuntime().exec(command);
            logger.debug(command);

            Transformation fluxSortie = new Transformation(p.getInputStream(), inputFile, transfMatrix);
            new Thread(fluxSortie).start();

            p.waitFor();

        } catch (IOException | InterruptedException ex) {
            logger.error(null, ex);
        }
    }
    
    private class Transformation implements Runnable {

            private final InputStream inputStream;
            private final File outputFullPath;
            private final Mat4D transfMatrix;
            private VoxelAnalysis voxelAnalysis;

            public Transformation(InputStream inputStream, File outputFullPath, Mat4D transMatrix) {

                this.inputStream = inputStream;
                this.outputFullPath = outputFullPath;
                this.transfMatrix = transMatrix;
                
                        
                voxelAnalysis = new VoxelAnalysis();
                voxelAnalysis.init(parameters);
            }

            @Override
            public void run() {
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                
                String ligne;
                
                Mat3D rotation = new Mat3D();
                rotation.mat = new double[]{
                    transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
                    transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
                    transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
                };
                
                File f = new File("./fichier_echos");
                f.mkdirs();

                try {
                    while ((ligne = reader.readLine()) != null) {

                        String[] lineSplit = ligne.split(" ");

                        String id = lineSplit[0];

                        int echoCount = Integer.valueOf(lineSplit[1]);
                        
                        float xloc_s = Float.valueOf(lineSplit[2]);
                        float yloc_s = Float.valueOf(lineSplit[3]);
                        float zloc_s = Float.valueOf(lineSplit[4]);
                        
                        float x_u = Float.valueOf(lineSplit[5]);
                        float y_u = Float.valueOf(lineSplit[6]);
                        float z_u = Float.valueOf(lineSplit[7]);

                        Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(xloc_s, yloc_s, zloc_s, 1.0d));
                        Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(x_u, y_u, z_u));
                        
                        float[] ranges = new float[echoCount];
                        
                        for(int i=0;i<echoCount;i++){
                            ranges[i] = Float.valueOf(lineSplit[i+8]);
                        }
                        
                        Shot shot = new Shot(echoCount, 
                                new Point3f((float)locVector.x, (float)locVector.y, (float)locVector.z),
                                new Vector3f((float)uVector.x, (float)uVector.y, (float)uVector.z), ranges);
                        voxelAnalysis.voxelise(shot);

                    }
                    
                    reader.close();
                    
                    voxelAnalysis.calculatePAD(0);
                    voxelAnalysis.writeOutput(outputFile);

                } catch (IOException ex) {
                    
                    logger.error(null, ex);
                }
            }

        }
    */
}
