/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.amapvox.io.tls.rsp.RxpScan;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.util.MatrixUtility;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.util.Util;
import fr.amap.lidar.amapvox.voxelisation.PointcloudFilter;
import fr.amap.lidar.amapvox.voxelisation.postproc.VoxelFileMerging;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpEchoFilter;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpVoxelisation;
import java.io.File;
import java.util.List;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author calcul
 */
public class RXPVoxelizationService extends Service<Void>{

    private final File file;
    
    public RXPVoxelizationService(File file){
        this.file = file;
    }
    
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
        
                final TLSVoxCfg cfg = new TLSVoxCfg();
                cfg.readConfiguration(file);
                
                File output = cfg.getOutputFile();
                File input = cfg.getInputFile();
                Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
                Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
                Mat4D sop = MatrixUtility.convertMatrix4dToMat4D(cfg.getSopMatrix());    

                cfg.getVoxelParameters().infos.setType(VoxelSpaceInfos.Type.TLS);

                RxpScan scan = new RxpScan();
                scan.setFile(input);

                Raster dtm = null;
                if (cfg.getVoxelParameters().getDtmFilteringParams().useDTMCorrection()) {
                    
                    updateMessage("Loading dtm...");
                    
                    dtm = Util.loadDTM(cfg.getVoxelParameters().getDtmFilteringParams().getDtmFile());
                }

                List<PointcloudFilter> pointcloudFilters = cfg.getVoxelParameters().getPointcloudFilters();

                if(pointcloudFilters != null){

                    if(vop == null){ vop = Mat4D.identity();}
                    if(cfg.getVoxelParameters().isUsePointCloudFilter()){
                        for(PointcloudFilter filter : pointcloudFilters){
                            
                            updateMessage("Loading point cloud filters...");
                            filter.setOctree(Util.loadOctree(filter.getPointcloudFile(), vop));
                        }
                    }
                }

                if(pop == null){ pop = Mat4D.identity();}
                if(sop == null){ sop = Mat4D.identity();}
                if(vop == null){ vop = Mat4D.identity();}

                cfg.setEchoFilter(new RxpEchoFilter(cfg.getEchoFilters()));
                
                updateMessage("Voxelization...");
                
                RxpVoxelisation voxelisation = new RxpVoxelisation(input, output, vop, pop, sop, dtm,
                        pointcloudFilters, cfg, cfg.isEnableEmptyShotsFiltering());
                
                voxelisation.call();
                
                return null;
            }
        };
    }
    
}
