/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.util.CallableTaskAdapter;
import fr.amap.commons.util.LidarScan;
import fr.amap.commons.util.MatrixUtility;
import fr.amap.commons.util.ProcessingAdapter;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.ProcessTool;
import fr.amap.lidar.amapvox.voxelisation.configuration.PTXLidarScan;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxelisation.tls.PTXVoxelisation;
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
public class PTXVoxelizationService extends Service<List<File>>{

    private final File file;
    private ExecutorService exec;
    private final int coreNumber;
    private final SimpleIntegerProperty nbFileProcessed;
    private ProcessTool tool;
    
    public PTXVoxelizationService(File file, int coreNumber){
        this.file = file;
        this.coreNumber = coreNumber;
        nbFileProcessed = new SimpleIntegerProperty(0);
    }
    
    @Override
    protected Task<List<File>> createTask() {
        return new Task<List<File>>() {
            @Override
            protected List<File> call() throws Exception {
                
                final TLSVoxCfg cfg = new TLSVoxCfg();
                cfg.readConfiguration(file);
                
                File output = cfg.getOutputFile();
                File input = cfg.getInputFile();

                VoxelParameters parameters = cfg.getVoxelParameters();
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

                parameters.infos.setType(VoxelSpaceInfos.Type.TLS);

                Raster dtm = null;
                if (parameters.getDtmFilteringParams().useDTMCorrection()) {
                    
                    updateMessage("Loading dtm...");
                    
                    dtm = ProcessTool.loadDTM(parameters.getDtmFilteringParams().getDtmFile());
                }

                List<PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();

                if(pointcloudFilters != null){

                    if(vop == null){ vop = Mat4D.identity();}

                    if(parameters.isUsePointCloudFilter()){
                        
                        updateMessage("Loading point cloud filters...");
                        
                        for(fr.amap.lidar.amapvox.voxelisation.PointcloudFilter filter : pointcloudFilters){
                            filter.setOctree(ProcessTool.loadOctree(filter.getPointcloudFile(), vop));
                        }
                    }
                }

                ArrayList<File> files = new ArrayList<>();
                exec = Executors.newFixedThreadPool(coreNumber);
                
                nbFileProcessed.set(0);
                
                final int nbFilesToWrite = cfg.getVoxelParameters().isMergingAfter() ? lidarScans.size()+1 : lidarScans.size();

                try {
                    LinkedBlockingQueue<Callable<PTXVoxelisation>>  tasks = new LinkedBlockingQueue<>();

                    int count = 0;
                    for (LidarScan file : lidarScans) {

                        PTXLidarScan scan = (PTXLidarScan)file;
                        File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() +"-scan-"+count+ ".vox");
                        PTXVoxelisation ptxVoxelization = new PTXVoxelisation(scan.getScan(), outputFile, vop, pop, MatrixUtility.convertMatrix4dToMat4D(file.matrix), parameters, dtm, pointcloudFilters, cfg);
                        
                        ptxVoxelization.addCallableTaskListener(new CallableTaskAdapter() {
                            @Override
                            public void onSucceeded() {
                                nbFileProcessed.set(nbFileProcessed.getValue()+1);
                                updateProgress(nbFileProcessed.intValue(), nbFilesToWrite);
                            }
                        });
                        
                        tasks.put(ptxVoxelization);
                        files.add(outputFile);
                        count++;
                    }

                    updateMessage("Voxelization...");
                    
                    exec.invokeAll(tasks);

                    exec.shutdown();
                    
                    if (cfg.getVoxelParameters().isMergingAfter()) {
                        
                        updateMessage("Merging voxel files...");
                        
                        VoxMergingCfg mergingCfg = new VoxMergingCfg(cfg.getVoxelParameters().getMergedFile(), cfg.getVoxelParameters(), files);

                        tool = new ProcessTool();
                        
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
