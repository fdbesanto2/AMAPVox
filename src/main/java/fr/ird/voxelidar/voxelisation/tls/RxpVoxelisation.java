/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.tls;

import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.Shot;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelAnalysis;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelParameters;
import fr.ird.voxelidar.Constants;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.math.matrix.Mat3D;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
import fr.ird.voxelidar.math.vector.Vec4D;
import fr.ird.voxelidar.util.CsvLine;
import fr.ird.voxelidar.voxelisation.Processing;
import fr.ird.voxelidar.voxelisation.VoxelisationParameters;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class RxpVoxelisation extends Processing{
    
    private static final Logger logger = Logger.getLogger(RxpVoxelisation.class);
    private RxpScan rxp;
    private Mat4D popMatrix;
    private File outputFile;
    private VoxelParameters parameters;
    
    public RxpVoxelisation(RxpScan rxp, File outputFile, Mat4D popMatrix, VoxelParameters parameters){
        
        this.rxp = rxp;
        this.popMatrix = popMatrix;
        this.parameters = parameters;
        this.outputFile = outputFile;
    }

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
    
}
