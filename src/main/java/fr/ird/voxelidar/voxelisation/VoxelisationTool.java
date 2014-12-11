/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.Rxp;
import fr.ird.voxelidar.math.matrix.Mat4D;
import fr.ird.voxelidar.math.vector.Vec4D;
import fr.ird.voxelidar.util.CsvLine;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class VoxelisationTool{
    
    final static Logger logger = Logger.getLogger(VoxelisationTool.class);
    
    private final static String RXP_PROGRAM_NAME = "TLSRivLib.exe";
    private final static String VOXELISATION_PROGRAM_NAME = "VoxelAnalysis.jar";
    
    private File outputPreprocessFile;
    private File outputVoxelisationFile;
    
    private double lowerCornerX;
    private double lowerCornerY;
    private double lowerCornerZ;
    
    private double topCornerX;
    private double topCornerY;
    private double topCornerZ;
    
    private int splitX;
    private int splitY;
    private int splitZ;
    
    private Rxp rxp;
    private Mat4D transfMatrix;
    
    
    private void preprocessRxpForVox(Rxp rxp, Mat4D transfMatrix) {
        
        PreprocessingRxp preprocessRxp = new PreprocessingRxp(rxp, transfMatrix);
        preprocessRxp.preprocess();
        
        Voxelisation voxelisation = new Voxelisation(outputPreprocessFile);
        voxelisation.voxelise();
        
        String outputFullPath = null;
        try {
            outputFullPath = outputPreprocessFile.getCanonicalPath();
        } catch (IOException ex) {
            logger.error(null, ex);
        }
        outputFullPath = outputFullPath.substring(0, outputFullPath.length()-4)+"_vox.txt";
        
        outputVoxelisationFile = new File(outputFullPath);
    }

    private void preprocessLasForVox(Las las) {

    }

    public File generateVoxelFile(Rxp rxp, Mat4D transfMatrix, double lowerCornerX, double lowerCornerY, int lowerCornerZ,
            double topCornerX, double topCornerY, double topCornerZ, int splitX, int splitY, int splitZ) {
        
        this.lowerCornerX = lowerCornerX;
        this.lowerCornerY = lowerCornerY;
        this.lowerCornerZ = lowerCornerZ;
        
        this.topCornerX = topCornerX;
        this.topCornerY = topCornerY;
        this.topCornerZ = topCornerZ;
        
        this.splitX = splitX;
        this.splitY = splitY;
        this.splitZ = splitZ;
        
        preprocessRxpForVox(rxp, transfMatrix);
        
        
        
        return outputVoxelisationFile;
        
    }
    
    private class Voxelisation{
        
        
        private final File inputFile;
        
        public Voxelisation(File inputFile){
            this.inputFile = inputFile;
        }
        
        public void voxelise(){
            
            String commande = "java -jar "+VOXELISATION_PROGRAM_NAME + " \"" + inputFile.getPath()+"\" ";
            commande += lowerCornerX + " ";
            commande += lowerCornerY + " ";
            commande += lowerCornerZ + " ";
            commande += topCornerX + " ";
            commande += topCornerY + " ";
            commande += topCornerZ + " ";
            commande += splitX + " ";
            commande += splitY + " ";
            commande += splitZ;
        
            Process p;
            try {
                p = Runtime.getRuntime().exec(commande);

                p.waitFor();

            } catch (IOException | InterruptedException ex) {
                logger.error(null, ex);
            }
        }

        
    }

    private class PreprocessingRxp{

        private Rsp rsp;
        private ArrayList<String> fileList;
        private Rxp rxp;
        private short mode;
        private Mat4D popMatrix;
        private Mat4D transfMatrix = Mat4D.identity();
        private boolean filtered = false;
        
        private File file;
        
        public PreprocessingRxp(Rxp rxp, Mat4D transfMatrix) {
            
            this.file = rxp.getRxpLiteFile();
            this.rxp = rxp;
            this.transfMatrix = transfMatrix;
        }
        
         

        private void doExec(File file, Mat4D transfMatrix) {

            Process p;
            try {
                String command = RXP_PROGRAM_NAME + " -0 " + "\"" + file.getAbsolutePath() + "\"";
                p = Runtime.getRuntime().exec(command);

                Transformation fluxSortie = new Transformation(p.getInputStream(), file.getAbsolutePath() + ".ech", transfMatrix);
                new Thread(fluxSortie).start();

                p.waitFor();

            } catch (IOException | InterruptedException ex) {
                logger.error(null, ex);
            }
        }
        
        public void preprocess() {
            
            Mat4D sopMatrix;
            
            sopMatrix = rxp.getSopMatrix();

            if (popMatrix == null) {
                popMatrix = Mat4D.identity();
            }

            transfMatrix = Mat4D.multiply(popMatrix, sopMatrix);
            
            doExec(file, transfMatrix);
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

                File f = new File("./fichier_echos");
                f.mkdirs();

                outputPreprocessFile = new File(f + "\\" + name);
                try {
                    BufferedWriter writer = new BufferedWriter(new FileWriter(outputPreprocessFile));

                    while ((ligne = br.readLine()) != null) {

                        String[] lineSplit = ligne.split(" ");

                        CsvLine newLine = new CsvLine(" ");

                        String id = lineSplit[0];
                        newLine.add(id);

                        String echoCount = lineSplit[1];
                        newLine.add(echoCount);

                        double xBeam = Double.valueOf(lineSplit[2]);
                        double yBeam = Double.valueOf(lineSplit[3]);
                        double zBeam = Double.valueOf(lineSplit[4]);

                        Vec4D newVector = Mat4D.multiply(transfMatrix, new Vec4D(xBeam, yBeam, zBeam, 1.0d));

                        newLine.add(String.valueOf(newVector.x));
                        newLine.add(String.valueOf(newVector.y));
                        newLine.add(String.valueOf(newVector.z));

                        for (int i = 1, j = 5; j < lineSplit.length; i++, j++) {
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

                        //System.out.println(ligne);
                    }
                    writer.close();

                //appel du jar de voxelisation
                } catch (IOException ex) {
                    
                    logger.error(null, ex);
                }
            }

        }
    }
}
