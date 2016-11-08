/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.util.CallableTaskAdapter;
import fr.amap.lidar.amapvox.commons.LidarScan;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.commons.util.ProcessingAdapter;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.util.Util;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.postproc.VoxelFileMerging;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxelisation.tls.PTGVoxelisation;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpEchoFilter;
import java.io.File;
import java.nio.file.Files;
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
public class PTGVoxelizationService extends Service<List<File>>{
    
    private final File file;
    private ExecutorService exec;
    private final int coreNumber;
    private final SimpleIntegerProperty nbFileProcessed;
    private VoxelFileMerging tool;
   
    public PTGVoxelizationService(File file, int coreNumber){
        this.file = file;
        this.coreNumber = coreNumber;
        nbFileProcessed = new SimpleIntegerProperty(0);
    }
    
    @Override
    protected Task<List<File>> createTask() {
        
        return new Task() {
            @Override
            protected List<File> call() throws Exception {
                
                final TLSVoxCfg cfg = new TLSVoxCfg();
                cfg.readConfiguration(file);
                
                File output = cfg.getOutputFile();
                File input = cfg.getInputFile();
                Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
                Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
                List<LidarScan> lidarScans = cfg.getLidarScans();
                cfg.setEchoFilter(new RxpEchoFilter(cfg.getEchoFilters()));

                if (!Files.isReadable(output.toPath())) {
                    throw new Exception("File " + output.getAbsolutePath() + " not reachable");
                }

                if (!Files.isReadable(input.toPath())) {
                    throw new Exception("File " + input.getAbsolutePath() + " not reachable");
                }

                cfg.getVoxelParameters().infos.setType(VoxelSpaceInfos.Type.TLS);

                Raster dtm = null;
                if (cfg.getVoxelParameters().getDtmFilteringParams().useDTMCorrection()) {
                    
                    updateMessage("Loading dtm...");
                    
                    dtm = Util.loadDTM(cfg.getVoxelParameters().getDtmFilteringParams().getDtmFile());
                }

                List<PointcloudFilter> pointcloudFilters = cfg.getVoxelParameters().getPointcloudFilters();

                if(pointcloudFilters != null){

                    if(vop == null){ vop = Mat4D.identity();}

                    if(cfg.getVoxelParameters().isUsePointCloudFilter()){
                        
                        updateMessage("Loading point cloud filters...");
                        
                        for(fr.amap.lidar.amapvox.voxelisation.PointcloudFilter filter : pointcloudFilters){
                            filter.setOctree(Util.loadOctree(filter.getPointcloudFile(), vop));
                        }
                    }
                }

                List<File> files = new ArrayList<>();
                exec = Executors.newFixedThreadPool(coreNumber);
                
                nbFileProcessed.set(0);
                
                final int nbFilesToWrite = cfg.getVoxelParameters().isMergingAfter() ? lidarScans.size()+1 : lidarScans.size();

                try {
                    LinkedBlockingQueue<Callable<PTGVoxelisation>>  tasks = new LinkedBlockingQueue<>();

                    int count = 1;
                    for (LidarScan file : lidarScans) {

                        File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() + ".vox");
                        PTGVoxelisation ptgVoxelisation = new PTGVoxelisation(file.file, outputFile, vop, pop, MatrixUtility.convertMatrix4dToMat4D(file.matrix), dtm, pointcloudFilters, cfg);
                        
                        ptgVoxelisation.addCallableTaskListener(new CallableTaskAdapter() {
                            @Override
                            public void onSucceeded() {
                                nbFileProcessed.set(nbFileProcessed.getValue()+1);
                                updateProgress(nbFileProcessed.intValue(), nbFilesToWrite);
                            }
                        });
                        tasks.put(ptgVoxelisation);
                        files.add(outputFile);
                        count++;
                    }

                    updateMessage("Voxelization...");
                    
                    exec.invokeAll(tasks);

                    exec.shutdown();
                    
                    
                    if (cfg.getVoxelParameters().isMergingAfter()) {
                        
                        updateMessage("Merging voxel files...");
                        
                        VoxMergingCfg mergingCfg = new VoxMergingCfg(cfg.getVoxelParameters().getMergedFile(), cfg.getVoxelParameters(), files);

                        tool = new VoxelFileMerging();
                        
                        tool.addProcessingListener(new ProcessingAdapter() {
                            @Override
                            public void processingStepProgress(String progressMsg, long progress, long max) {
                                updateMessage(progressMsg);
                                updateProgress(progress, max);
                            }
                        });
                        
                        tool.mergeVoxelFiles(mergingCfg);
                        
                        files.add(cfg.getVoxelParameters().getMergedFile());
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
    }
    
}
