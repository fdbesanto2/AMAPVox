/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.commons.util.CallableTaskAdapter;
import fr.amap.lidar.amapvox.commons.LidarScan;
import fr.amap.commons.util.ProcessingAdapter;
import fr.amap.lidar.amapvox.voxelisation.postproc.VoxelFileMerging;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpVoxelisation;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class RSPVoxelizationService extends Service<List<File>>{

    private final File file;
    private final int coreNumber;
    private ExecutorService exec;
    private final SimpleIntegerProperty nbFileProcessed;
    private VoxelFileMerging tool;
    
    public RSPVoxelizationService(File file, int coreNumber){
        this.file = file;
        this.coreNumber = coreNumber;
        nbFileProcessed = new SimpleIntegerProperty(0);
    }

    @Override
    protected Task<List<File>> createTask() {
        
        Task task = new Task<List<File>>() {
                        
            @Override
            protected List<File> call() throws Exception {
                
                TLSVoxCfg mainCfg = new TLSVoxCfg();
                mainCfg.readConfiguration(file);
                List<LidarScan> lidarScans = mainCfg.getLidarScans();

                ArrayList<File> files = new ArrayList<>();
                exec = Executors.newFixedThreadPool(coreNumber);
                nbFileProcessed.set(0);
                int nbFilesToWrite = mainCfg.getVoxelParameters().isMergingAfter() ? lidarScans.size()+1 : lidarScans.size();
                try {
                    LinkedBlockingQueue<Callable<RxpVoxelisation>>  tasks = new LinkedBlockingQueue<>();
                    for (LidarScan scan : lidarScans) {
                        
                        TLSVoxCfg cfg = new TLSVoxCfg();
                        cfg.readConfiguration(file);
                        cfg.setInputFile(scan.file);
                        File outputFile = new File(mainCfg.getOutputFile().getAbsolutePath() + "/" + scan.file.getName() + ".vox");
                        cfg.setOutputFile(outputFile);
                        cfg.setSopMatrix(scan.matrix);
                        
                        RxpVoxelisation rxpVoxelisation = new RxpVoxelisation(cfg);
                        rxpVoxelisation.init();
                        rxpVoxelisation.addCallableTaskListener(new CallableTaskAdapter() {
                            @Override
                            public void onSucceeded() {
                                nbFileProcessed.set(nbFileProcessed.getValue()+1);
                                updateProgress(nbFileProcessed.intValue(), nbFilesToWrite);
                            }
                        });
                        tasks.put(rxpVoxelisation);
                        files.add(outputFile);
                    }

                    updateMessage("Voxelization...");
                    
                    exec.invokeAll(tasks);

                    //wait for all Callable to finish
                    exec.shutdown();
                    
                    if (mainCfg.getVoxelParameters().isMergingAfter()) {
                        
                        VoxMergingCfg mergingCfg = new VoxMergingCfg(mainCfg.getVoxelParameters().getMergedFile(), mainCfg.getVoxelParameters(), files);

                        tool = new VoxelFileMerging();
                        
                        tool.addProcessingListener(new ProcessingAdapter() {
                            @Override
                            public void processingStepProgress(String progressMsg, long progress, long max) {
                                updateMessage(progressMsg);
                                updateProgress(progress, max);
                            }
                        });
                        
                        tool.mergeVoxelFiles(mergingCfg);
                        files.add(mainCfg.getVoxelParameters().getMergedFile());
                    }
                }catch (InterruptedException | NullPointerException ex){
                    this.cancel();
                    throw ex;
                }

                return files;
            }
            
            @Override
            protected void cancelled() {
                super.cancelled();
                
                if(tool != null){
                    tool.setCancelled(true);
                }
                
            }
        };
        
        return task;
    }
    
}
