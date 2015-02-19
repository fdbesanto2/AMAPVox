/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import fr.ird.voxelidar.util.ProcessingListener;
import fr.ird.voxelidar.util.Processing;
import fr.ird.voxelidar.voxelisation.als.LasVoxelisation;
import fr.ird.voxelidar.voxelisation.tls.RxpVoxelisation;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.VoxelAnalysis;
import fr.ird.voxelidar.Constants;
import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.lidar.format.als.Las;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec2D;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class VoxelisationTool extends Processing{
    
    final static Logger logger = Logger.getLogger(VoxelisationTool.class);
    
    private VoxelParameters parameters;
    
    public VoxelisationTool(){
    }
    
    public void generateVoxelsFromRsp(File output, File input , VoxelParameters parameters, Mat4D vop, boolean mon) {
        
        
        this.parameters = parameters;
        this.parameters.setIsTLS(true);
        
        Rsp rsp = new Rsp();
        rsp.read(input);

        ArrayList<Scans> rxpList = rsp.getRxpList();
        
        
        
        int count = 1;
        for(Scans rxp :rxpList){
            
            Map<Integer, RxpScan> scanList = rxp.getScanList();

            for(Entry entry:scanList.entrySet()){
                
                RxpScan scan = (RxpScan) entry.getValue();
                RxpVoxelisation voxelisation;
                
                if(mon && scan.getName().contains(".mon")){
                    File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
                    fireProgress(outputFile.getAbsolutePath(), count);
                    voxelisation = new RxpVoxelisation(scan, outputFile, vop, this.parameters);
                    voxelisation.voxelise();
                    count++;
                    
                    
                }else if(!mon && !scan.getName().contains(".mon")){
                    File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
                    fireProgress(outputFile.getAbsolutePath(), count);
                    voxelisation = new RxpVoxelisation(scan, outputFile, vop, this.parameters);
                    voxelisation.voxelise();
                    count++;
                }
            }
        }
        
        fireFinished();
        
    }

    public void generateVoxelsFromRxp(File output, File input , VoxelParameters parameters, Mat4D vop) {
        
        this.parameters = parameters;
        this.parameters.setIsTLS(true);
        
        RxpScan scan = new RxpScan();
        scan.setFile(input);
        
        File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
        
        fireProgress(outputFile.getAbsolutePath(), 1);
        
        RxpVoxelisation voxelisation = new RxpVoxelisation(scan, outputFile, vop, parameters);
        voxelisation.voxelise();
        
        fireFinished();
        
    }
    
    public File generateVoxelsFromLas(File output, File input, File trajectoryFile, File dtmFile, VoxelParameters parameters, Mat4D vop) {
        
        this.parameters = parameters;
        this.parameters.setIsTLS(false);
        
        LasVoxelisation voxelisation = new LasVoxelisation(input, output, vop, trajectoryFile, parameters, dtmFile);
        
        voxelisation.addProcessingListener(new ProcessingListener() {

            @Override
            public void processingStepProgress(String progress, int ratio) {
                fireProgress(progress, ratio);
            }

            @Override
            public void processingFinished() {
                fireFinished();
            }
        });
        
        voxelisation.process();
        
        return null;
        
    }
    
    public static File mergeVoxelsFile(ArrayList<File> filesList, File output) throws NullPointerException{
        
        if(filesList != null && filesList.size()>0){
            
            
            try {
                BufferedReader reader = new BufferedReader(new FileReader(filesList.get(0)));
                
                String line;
                
                String attributs = reader.readLine();
                int columnsNumber = attributs.split(" ").length;
                
                String metadata = reader.readLine();
                
                ArrayList<float[]> voxelsList = new ArrayList<>();
                
                while((line = reader.readLine()) != null){
                    
                    String[] voxel = line.split(" ");
                    float[] voxelAttributs = new float[voxel.length];
                    
                    for(int i=0;i<voxel.length;i++){
                        
                        voxelAttributs[i] = Float.valueOf(voxel[i]);
                    }
                    
                    voxelsList.add(voxelAttributs);
                }
                
                reader.close();
                
                for(int i=1;i<filesList.size();i++){
                    
                    reader = new BufferedReader(new FileReader(filesList.get(i)));
                    
                    int columnsNumber2 = reader.readLine().split(" ").length;
                    
                    if(columnsNumber2 != columnsNumber){
                        return null;
                    }
                
                    //skip metadata
                    reader.readLine();
                    
                    int count = 0;
                    while((line = reader.readLine()) != null){

                        String[] voxel = line.split(" ");
                        float[] voxelAttributs = new float[voxel.length];                        

                        for(int j=0;j<voxel.length;j++){

                            voxelAttributs[j] = Float.valueOf(voxel[j]);
                        }
                        
                        float[] array = voxelsList.get(count);
                        
                        for(int j=0;j<array.length;j++){
                            
                            if(j<3){
                                //array[j] = array[j];
                            }else{
                                array[j] = array[j]+voxelAttributs[j];
                            }
                        }
                        
                        voxelsList.set(count, array);
                        
                        count++;
                    }
                    
                    reader.close();
                }
                
                BufferedWriter writer = new BufferedWriter(new FileWriter(output));
                
                writer.write(attributs+"\n");
                writer.write(metadata+"\n");
                
                for(int i=0;i<voxelsList.size();i++){
                    
                    line = "";
                    
                    float[] voxel = voxelsList.get(i);
                    
                    for(int j=0;j<voxel.length;j++){
                        
                        String value ="";
                        
                        if(j<3){
                            value = String.valueOf((int)voxel[j]);
                        }else{
                            value = String.valueOf(voxel[j]);
                        }
                        
                        line+=value+" ";
                    }
                    writer.write(line.trim()+"\n");
                }
                
                writer.close();
                
            } catch (FileNotFoundException ex) {
                java.util.logging.Logger.getLogger(VoxelisationTool.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(VoxelisationTool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        
        return output;
    }

    @Override
    public File process() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private class Voxelisation{
        
        
        private File inputFile;
        private File outputFile;
        private VoxelAnalysis voxelAnalysis;
        
        public Voxelisation(File inputFile, File outputFile){
            this.inputFile = inputFile;
            this.outputFile = outputFile;
        }
        
        
        //new version
        
        public void voxelise2(Shot shot){
            
            
            //voxelAnalysis.voxelise(shot);
            
        }
        
        /**eloÃ¯ version (take properties file)**/
//        
//        public File voxelise(){
//            
//            
//            /**generate parameters file (eloÃ¯ program only)**/
//            
//            File propertiesFile = new File("properties.txt");
//            
//            try {
//                
//                BufferedWriter writer = new BufferedWriter(new FileWriter(propertiesFile));
//                
//                writer.write("zoneUtilisateurOuZoneScan:1"+"\n");
//                writer.write("dossierOutput:./"+"\n");
//                writer.write("methodePonderationEchos:1"+"\n");
//                
//                writer.write("typeExecution:5"+"\n"); //extraire densitÃ© fichier texte als
//                writer.write("fichierXYZ:"+inputFile.getAbsolutePath()+"\n");
//                
//                writer.write("pointMailleMin.x:"+parameters.getLowerCornerX()+"\n");
//                writer.write("pointMailleMin.y:"+parameters.getLowerCornerY()+"\n");
//                writer.write("pointMailleMin.z:"+parameters.getLowerCornerZ()+"\n");
//                
//                writer.write("pointMailleMax.x:"+parameters.getTopCornerX()+"\n");
//                writer.write("pointMailleMax.y:"+parameters.getTopCornerY()+"\n");
//                writer.write("pointMailleMax.z:"+parameters.getTopCornerZ()+"\n");
//                
//                writer.write("resolution:"+parameters.getResolution());
//                
//                writer.close();
//                
//            } catch (IOException ex) {
//                java.util.logging.Logger.getLogger(VoxelisationTool.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//            String commande = Constants.PROGRAM_VOX_ELOI +" -i "+ propertiesFile.getAbsolutePath();
//        
//            Process p;
//            try {
//                p = Runtime.getRuntime().exec(commande);
//
//                p.waitFor();
//
//            } catch (IOException | InterruptedException ex) {
//                logger.error(null, ex);
//            }
//            
//            /*eloÃ¯ program generate a directory named "densite" and 
//            the voxel file named "densite3D", so we just rename and 
//            move the file to the user choice directory
//            WARNING: the renameTo method only work if the destination 
//            file is into the same physical disk
//            */
//            File oldFile = new File("./densite/densite3D");
//            
//            try {
//                //add header
//                BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
//                writer.write("i j k lg_traversant lg_interception n_interceptes n_apres surface distance_scanner densite\n");
//                
//                BufferedReader reader = new BufferedReader(new FileReader(oldFile));
//                
//                String line;
//                while((line = reader.readLine()) != null){
//                    writer.write(line+"\n");
//                }
//                
//                writer.close();
//                reader.close();
//                
//                oldFile.delete();
//                
//            } catch (IOException ex) {
//                java.util.logging.Logger.getLogger(VoxelisationTool.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            
//            //oldFile.renameTo(outputFile);
//            
//            return outputFile;
//        }

        
    }
    
    public static Mat4D getMatrixTransformation(Vector3d point1, Vector3d point2){
        
        
        if((point1.x == point2.x) && (point1.y == point2.y) && (point1.z == point2.z)){
            return Mat4D.identity();
        }
        Vec2D v = new Vec2D(point1.x - point2.x, point1.y - point2.y);
        double rho = (double) Math.atan(v.x / v.y);

        Vector3d trans = new Vector3d(-point2.x, -point2.y, -point2.z);
        trans.z = 0; //no vertical translation
        
        Mat4D mat4x4Rotation = new Mat4D();
        Mat4D mat4x4Translation = new Mat4D();

        mat4x4Rotation.mat = new double[]{
            (double) Math.cos(rho), (double) -Math.sin(rho), 0, 0,
            (double) Math.sin(rho), (double) Math.cos(rho), 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        };
        
        mat4x4Translation.mat = new double[]{
            1, 0, 0, trans.x,
            0, 1, 0, trans.y,
            0, 0, 1, trans.z,
            0, 0, 0, 1
        };
        
        Mat4D mat4x4 = Mat4D.multiply(mat4x4Translation, mat4x4Rotation);
        
        return mat4x4;
    }

    
}
