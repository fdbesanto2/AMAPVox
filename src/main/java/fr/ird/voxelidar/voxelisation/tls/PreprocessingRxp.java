/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation.tls;

import fr.ird.voxelidar.Constants;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import fr.ird.voxelidar.math.matrix.Mat3D;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec3D;
import fr.ird.voxelidar.math.vector.Vec4D;
import fr.ird.voxelidar.util.CsvLine;
import fr.ird.voxelidar.voxelisation.Preprocessing;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class PreprocessingRxp extends Preprocessing{
    
    private static final Logger logger = Logger.getLogger(PreprocessingRxp.class);
    private RxpScan rxp;
    private Mat4D popMatrix;
    private File outputFile;
    
    public PreprocessingRxp(RxpScan rxp, Mat4D popMatrix){
        
        this.rxp = rxp;
        this.popMatrix = popMatrix;
    }

    @Override
    public File preprocess() {
        
        Mat4D sopMatrix;
            
        sopMatrix = rxp.getSopMatrix();

        if (popMatrix == null) {
            popMatrix = Mat4D.identity();
        }

        Mat4D transfMatrix = Mat4D.multiply(popMatrix, sopMatrix);

        doExec(rxp.getFile(), transfMatrix);
        
        return outputFile;
    }
    
    private void doExec(File file, Mat4D transfMatrix) {

        Process p;
        try {
            String command = Constants.PROGRAM_RXP_READER + " -0 " + "\"" + file.getAbsolutePath() + "\"";
            p = Runtime.getRuntime().exec(command);

            Transformation fluxSortie = new Transformation(p.getInputStream(), file.getAbsolutePath() + ".ech", transfMatrix);
            new Thread(fluxSortie).start();

            p.waitFor();

        } catch (IOException | InterruptedException ex) {
            logger.error(null, ex);
        }
    }
    
    private class Transformation implements Runnable {

            private final InputStream inputStream;
            private final String outputFullPath;
            private final Mat4D transfMatrix;

            private BufferedReader getBufferedReader(InputStream is) {
                return new BufferedReader(new InputStreamReader(is));
            }

            public Transformation(InputStream inputStream, String outputFullPath, Mat4D transMatrix) {

                this.inputStream = inputStream;
                this.outputFullPath = outputFullPath;
                this.transfMatrix = transMatrix;
            }

            @Override
            public void run() {

                BufferedReader br = getBufferedReader(inputStream);
                File file = new File(outputFullPath);
                String name = file.getName();
                String ligne;

                String get = (String) System.getProperties().get("user.dir");
                
                Mat3D rotation = new Mat3D();
                rotation.mat = new double[]{
                    transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
                    transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
                    transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
                };
                
                File f = new File("./fichier_echos");
                f.mkdirs();

                outputFile = new File(f + "\\" + name);
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
                    
                    writer.write("\"n\" \"xloc_s\" \"yloc_s\" \"zloc_s\" \"x_u\" \"y_u\" \"z_u\" \"r1\" \"r2\" \"r3\" \"r4\" \"r5\" \"r6\" \"r7\""+"\n");

                    while ((ligne = br.readLine()) != null) {

                        String[] lineSplit = ligne.split(" ");

                        CsvLine newLine = new CsvLine(" ");

                        String id = lineSplit[0];
                        
                        //id must be added when switching of voxelisation program
                        //newLine.add(id);

                        String echoCount = lineSplit[1];
                        newLine.add(echoCount);
                        
                        double xloc_s = Double.valueOf(lineSplit[2]);
                        double yloc_s = Double.valueOf(lineSplit[3]);
                        double zloc_s = Double.valueOf(lineSplit[4]);
                        
                        double x_u = Double.valueOf(lineSplit[5]);
                        double y_u = Double.valueOf(lineSplit[6]);
                        double z_u = Double.valueOf(lineSplit[7]);

                        Vec4D locVector = Mat4D.multiply(transfMatrix, new Vec4D(xloc_s, yloc_s, zloc_s, 1.0d));
                        Vec3D uVector = Mat3D.multiply(rotation, new Vec3D(x_u, y_u, z_u));
                        
                        newLine.add(String.valueOf(locVector.x));
                        newLine.add(String.valueOf(locVector.y));
                        newLine.add(String.valueOf(locVector.z));
                        
                        newLine.add(String.valueOf(uVector.x));
                        newLine.add(String.valueOf(uVector.y));
                        newLine.add(String.valueOf(uVector.z));

                        for (int i = 1, j = 8; j < lineSplit.length; i++, j++) {
                            switch (i) {
                                //écho 1
                                case 1:
                                    double echo1 = Double.valueOf(lineSplit[j]);
                                    newLine.add(String.valueOf(echo1));
                                    break;
                                //écho 2
                                case 2:
                                    double echo2 = Double.valueOf(lineSplit[j]);
                                    newLine.add(String.valueOf(echo2));
                                    break;
                                //écho 3
                                case 3:
                                    double echo3 = Double.valueOf(lineSplit[j]);
                                    newLine.add(String.valueOf(echo3));
                                    break;
                                //écho 4
                                case 4:
                                    double echo4 = Double.valueOf(lineSplit[j]);
                                    newLine.add(String.valueOf(echo4));
                                    break;
                                //écho 5
                                case 5:
                                    double echo5 = Double.valueOf(lineSplit[j]);
                                    newLine.add(String.valueOf(echo5));
                                    break;
                                //écho 6
                                case 6:
                                    double echo6 = Double.valueOf(lineSplit[j]);
                                    newLine.add(String.valueOf(echo6));
                                    break;
                                //écho 7
                                case 7:
                                    double echo7 = Double.valueOf(lineSplit[j]);
                                    newLine.add(String.valueOf(echo7));
                                    break;
                            }
                        }

                        writer.write(newLine.getLine());

                    }
                    writer.close();

                } catch (IOException ex) {
                    
                    logger.error(null, ex);
                }
            }

        }
    
}
