/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import fr.ird.voxelidar.util.ProcessingListener;
import fr.ird.voxelidar.voxelisation.als.LasVoxelisation;
import fr.ird.voxelidar.voxelisation.tls.RxpVoxelisation;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec2D;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.engine3d.object.scene.DtmLoader;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import fr.ird.voxelidar.util.DataSet;
import fr.ird.voxelidar.util.TimeCounter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class VoxelisationTool{
    
    final static Logger logger = Logger.getLogger(VoxelisationTool.class);
    
    private VoxelParameters parameters;
    private final EventListenerList listeners;
    private long startTime;
    
    public VoxelisationTool(){
        listeners = new EventListenerList();
    }
    
    public void addVoxelisationToolListener(VoxelisationToolListener listener){
        listeners.add(VoxelisationToolListener.class, listener);
    }
    
    public void fireProgress(String progress, int ratio){
        for(VoxelisationToolListener voxelisationToolListener: listeners.getListeners(VoxelisationToolListener.class)){
            voxelisationToolListener.voxelisationProgress(progress, ratio);
        }
    }
    
    public void fireFinished(float duration){
        
        for(VoxelisationToolListener voxelisationToolListener: listeners.getListeners(VoxelisationToolListener.class)){
            voxelisationToolListener.voxelisationFinished(duration);
        }
    }
    
    private Dtm loadDTM(File dtmFile){
        
        Dtm terrain = null;
        
        if(dtmFile != null && parameters.useDTMCorrection() ){
            
            try {
                terrain = DtmLoader.readFromAscFile(dtmFile, null);
                
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
        
        return terrain;
    }
    
    public ArrayList<File> generateVoxelsFromRsp(File output, File input , File dtmFile, VoxelParameters parameters, Mat4D vop, Mat4D pop, boolean mon) {
        
        startTime = System.currentTimeMillis();
        
        this.parameters = parameters;
        this.parameters.setTLS(true);
        
        Rsp rsp = new Rsp();
        rsp.read(input);

        ArrayList<Scans> rxpList = rsp.getRxpList();
        ArrayList<File> files = new ArrayList<>();
        
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ArrayList<Callable<RxpVoxelisation>> tasks = new ArrayList<>();
        
        Dtm terrain = loadDTM(dtmFile);
        
        int count = 1;
        for(Scans rxp :rxpList){
            
            Map<Integer, RxpScan> scanList = rxp.getScanList();

            for(Entry entry:scanList.entrySet()){
                
                RxpScan scan = (RxpScan) entry.getValue();
                //RxpVoxelisation voxelisation;
                
                if(mon && scan.getName().contains(".mon")){
                    File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
                    //fireProgress(outputFile.getAbsolutePath(), count);
                    //voxelisation = new RxpVoxelisation(scan, outputFile, vop, this.parameters);
                    tasks.add(new RxpVoxelisation(scan, outputFile, vop, pop, this.parameters, terrain));
                    files.add(outputFile);
                    //voxelisation.voxelise();
                    count++;
                    
                    
                }else if(!mon && !scan.getName().contains(".mon")){
                    File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
                    //fireProgress(outputFile.getAbsolutePath(), count);
                    tasks.add(new RxpVoxelisation(scan, outputFile, vop, pop, this.parameters, terrain));
                    files.add(outputFile);
                    //voxelisation = new RxpVoxelisation(scan, outputFile, vop, this.parameters);
                    //voxelisation.voxelise();
                    count++;
                }
            }
        }
        
        
        try {
            List<Future> results = (List) exec.invokeAll(tasks);
            exec.shutdown();
            for(Future f : results) {
                f.get();
            }
        } catch (InterruptedException | RejectedExecutionException | NullPointerException |ExecutionException ex ) {
            logger.error(ex.getMessage());
        }
        
        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
        
        return files;
    }

    public void generateVoxelsFromRxp(File output, File input , File dtmFile, VoxelParameters parameters, Mat4D vop, Mat4D pop) {
        
        startTime = System.currentTimeMillis();
        
        this.parameters = parameters;
        this.parameters.setTLS(true);
        
        RxpScan scan = new RxpScan();
        scan.setFile(input);
        
        File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
        
        fireProgress(outputFile.getAbsolutePath(), 1);
        
        Dtm terrain = loadDTM(dtmFile);
        
        RxpVoxelisation voxelisation = new RxpVoxelisation(scan, outputFile, vop, pop, parameters, terrain);
        voxelisation.call();
        
        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
        
    }
    
    public void generateVoxelsFromLas(File output, File input, File trajectoryFile, File dtmFile, VoxelParameters parameters, Mat4D vop) {
        
        startTime = System.currentTimeMillis();
        
        this.parameters = parameters;
        this.parameters.setTLS(false);
        
        LasVoxelisation voxelisation = new LasVoxelisation(input, output, vop, trajectoryFile, parameters, dtmFile);
        
        voxelisation.addProcessingListener(new ProcessingListener() {

            @Override
            public void processingStepProgress(String progress, int ratio) {
                fireProgress(progress, ratio);
            }

            @Override
            public void processingFinished() {
                
            }
        });
        
        voxelisation.process();
        
        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
        
    }
    
    public void mergeVoxelsFile(ArrayList<File> filesList, File output) throws NullPointerException{
        
        startTime = System.currentTimeMillis();
        boolean[] toMerge = null;
        Map<String, Float[]> map1, map2, result = null;
        int size = 0;
        double bottomCornerX = 0, bottomCornerY = 0, bottomCornerZ = 0;
        double topCornerX = 0, topCornerY = 0, topCornerZ = 0;
        int splitX = 0, splitY = 0, splitZ = 0;
        
        
        for(int i=0;i<filesList.size();i++){
            
            logger.info("Merging in progress, file "+(i+1)+" : "+filesList.size());
            
            VoxelSpace voxelSpace1 = new VoxelSpace(filesList.get(i));
            voxelSpace1.load();
            size = voxelSpace1.data.split.x * voxelSpace1.data.split.y * voxelSpace1.data.split.z;
            map1 = voxelSpace1.data.getVoxelMap();
            
            bottomCornerX = voxelSpace1.data.bottomCorner.x;
            bottomCornerY = voxelSpace1.data.bottomCorner.y;
            bottomCornerZ = voxelSpace1.data.bottomCorner.z;
            
            topCornerX = voxelSpace1.data.topCorner.x;
            topCornerY = voxelSpace1.data.topCorner.y;
            topCornerZ = voxelSpace1.data.topCorner.z;
            
            splitX = voxelSpace1.data.split.x;
            splitY = voxelSpace1.data.split.y;
            splitZ = voxelSpace1.data.split.z;
            
            if(i == 0){
                /*
                toMerge = new boolean[voxelSpace1.getMapAttributs().size()];
                
                for(int j=0;j<toMerge.length;j++){
                    
                    toMerge[j] = j>2;
                }
                */
                toMerge = new boolean[]{false, false, false, true, true, true, true, true, true, true, true, false, true, true, true, true, true};
                
                VoxelSpace voxelSpace2 = new VoxelSpace(filesList.get(i));
                voxelSpace2.load();
                map2 = voxelSpace2.data.getVoxelMap();
                
            }else{
                map2 = result;
            }
            
            result = DataSet.mergeTwoDataSet(map1, map2, toMerge);
            
        }
        
        
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {
            
            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + (float)bottomCornerX + " " + (float)bottomCornerY + " " + (float)bottomCornerZ + "\n");
            writer.write("#max_corner: " + (float)topCornerX + " " + (float)topCornerY + " " + (float)topCornerZ + "\n");
            writer.write("#split: " + splitX + " " + splitY + " " + splitZ + "\n");

            writer.write("#offset: " + (float) bottomCornerX + " " + (float) bottomCornerY + " " + (float) bottomCornerZ + "\n");

            
            String header = "";
            
            for(Entry entry:result.entrySet()){
                String columnName = (String) entry.getKey();
                header += columnName+" ";
            }
            header = header.trim();
            writer.write(header+"\n");
            
            for(int i=0;i<size;i++){
                
                String voxel = "";
                
                int count = 0;
                for(Entry entry:result.entrySet()){
                    
                    Float[] values = (Float[]) entry.getValue();
                    
                    if(count<3){
                        
                        voxel += values[i].intValue() + " ";
                    }else{
                        voxel += values[i]+" ";
                    }
                    
                    
                    count++;
                }
                
                voxel = voxel.trim();
                
                writer.write(voxel+"\n");
                
            }
            
            
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(VoxelisationTool.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
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

    public void addVoxelisationToolListener(ProcessingListener processingListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
}
