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
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import fr.ird.voxelidar.util.DataSet;
import fr.ird.voxelidar.util.TimeCounter;
import java.io.File;
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
    
    public ArrayList<File> generateVoxelsFromRsp(File output, File input , VoxelParameters parameters, Mat4D vop, Mat4D pop, boolean mon) {
        
        startTime = System.currentTimeMillis();
        
        this.parameters = parameters;
        this.parameters.setTLS(true);
        
        Rsp rsp = new Rsp();
        rsp.read(input);

        ArrayList<Scans> rxpList = rsp.getRxpList();
        ArrayList<File> files = new ArrayList<>();
        
        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ArrayList<Callable<RxpVoxelisation>> tasks = new ArrayList<>();
        
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
                    tasks.add(new RxpVoxelisation(scan, outputFile, vop, pop, this.parameters));
                    files.add(outputFile);
                    //voxelisation.voxelise();
                    count++;
                    
                    
                }else if(!mon && !scan.getName().contains(".mon")){
                    File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
                    //fireProgress(outputFile.getAbsolutePath(), count);
                    tasks.add(new RxpVoxelisation(scan, outputFile, vop, pop, this.parameters));
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

    public void generateVoxelsFromRxp(File output, File input , VoxelParameters parameters, Mat4D vop, Mat4D pop) {
        
        startTime = System.currentTimeMillis();
        
        this.parameters = parameters;
        this.parameters.setTLS(true);
        
        RxpScan scan = new RxpScan();
        scan.setFile(input);
        
        File outputFile = new File(output.getAbsolutePath()+"/"+scan.getFile().getName()+".vox");
        
        fireProgress(outputFile.getAbsolutePath(), 1);
        
        RxpVoxelisation voxelisation = new RxpVoxelisation(scan, outputFile, vop, pop, parameters);
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
        
        
        for(int i=0;i<filesList.size();i++){
            
            VoxelSpace voxelSpace1 = new VoxelSpace(filesList.get(i));
            voxelSpace1.load();
            
            map1 = voxelSpace1.data.getVoxelMap();
            
            if(i == 0){
                toMerge = new boolean[voxelSpace1.getMapAttributs().size()];
                
                for(int j=0;j<toMerge.length;j++){
                    toMerge[j] = j>2;
                }
                
                VoxelSpace voxelSpace2 = new VoxelSpace(filesList.get(i));
                voxelSpace2.load();
                map2 = voxelSpace2.data.getVoxelMap();
                
            }else{
                map2 = result;
            }
            
            result = DataSet.mergeTwoDataSet(map1, map2, toMerge);
            
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
