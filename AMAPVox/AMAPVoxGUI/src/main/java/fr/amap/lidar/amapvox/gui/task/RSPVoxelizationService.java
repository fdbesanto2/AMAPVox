/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.task;

import fr.amap.commons.util.CallableTaskAdapter;
import fr.amap.lidar.amapvox.commons.LidarScan;
import fr.amap.commons.util.ProcessingAdapter;
import fr.amap.commons.util.filter.Filter;
import fr.amap.lidar.amapvox.shot.filter.EchoRankFilter;
import fr.amap.lidar.amapvox.voxelisation.postproc.VoxelFileMerging;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightByFileParams;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpVoxelisation;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class RSPVoxelizationService extends Service<List<File>> {

    private final File file;
    private final int coreNumber;
    private ExecutorService exec;
    private final SimpleIntegerProperty nbFileProcessed;
    private VoxelFileMerging tool;
    private final static Logger LOGGER = Logger.getLogger(RSPVoxelizationService.class);

    public RSPVoxelizationService(File file, int coreNumber) {
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

                // echo weight by file
                HashMap<String, String> echoWeightMap = null;
                if (null != mainCfg.getVoxelParameters().getEchoesWeightByFileParams()) {
                    echoWeightMap = readCSV(mainCfg.getVoxelParameters().getEchoesWeightByFileParams().getFile());
                }

                // echo filter by file
                HashMap<String, String> echoFilterMap = null;
                EchoRankFilter.Behavior behavior = EchoRankFilter.Behavior.DISCARD;
                EchoRankFilter echoRankFilter = null;
                for (Filter filter : mainCfg.getEchoFilters()) {
                    if (filter instanceof EchoRankFilter) {
                        echoRankFilter = (EchoRankFilter) filter;
                        echoFilterMap = readCSV(echoRankFilter.getFile());
                        behavior = echoRankFilter.behavior();
                        break;
                    }
                }

                ArrayList<File> files = new ArrayList();
                exec = Executors.newFixedThreadPool(Math.min(coreNumber, lidarScans.size()));
                nbFileProcessed.set(0);
                int nbFilesToWrite = mainCfg.getVoxelParameters().isMergingAfter() ? lidarScans.size() + 1 : lidarScans.size();
                try {
                    List<RxpVoxelisation> tasks = new ArrayList();
                    for (LidarScan scan : lidarScans) {

                        TLSVoxCfg cfg = new TLSVoxCfg();
                        cfg.readConfiguration(file);
                        cfg.setInputFile(scan.file);
                        File outputFile = new File(mainCfg.getOutputFile().getAbsolutePath() + "/" + scan.file.getName() + ".vox");
                        cfg.setOutputFile(outputFile);
                        cfg.setSopMatrix(scan.matrix);
                        
                        if (null != echoWeightMap) {
                            String key;
                            String rxp = scan.file.getName();
                            if (null != (key = findKey(echoWeightMap, rxp))) {
                                cfg.getVoxelParameters().setEchoesWeightByFileParams(new EchoesWeightByFileParams(echoWeightMap.get(key)));
                                //LOGGER.debug("Echo weight file " + cfg.getVoxelParameters().getEchoesWeightByFileParams().getFile());
                            } else {
                                cfg.getVoxelParameters().setEchoesWeightByFileParams(null);
                                LOGGER.warn("Could not find any echo weight file associated to RXP scan " + rxp + " in parameter file " + mainCfg.getVoxelParameters().getEchoesWeightByFileParams().getFile().getName());
                            }
                        }

                        if (null != echoFilterMap && null != echoRankFilter) {
                            String key;
                            String rxp = scan.file.getName();
                            if (null != (key = findKey(echoFilterMap, rxp))) {
                                EchoRankFilter filter = new EchoRankFilter(
                                        echoFilterMap.get(key),
                                        behavior);
                                cfg.addEchoFilter(filter);
                                LOGGER.debug("Echo filer file " + filter.getFile());
                                cfg.getEchoFilters().remove(echoRankFilter);
                            } else {
                                LOGGER.warn("Could not find any echo filter file associated to RXP scan " + rxp + " in parameter file " + echoRankFilter.getFile());
                            }
                        }

                        RxpVoxelisation rxpVoxelisation = new RxpVoxelisation(cfg);
                        rxpVoxelisation.init();
                        rxpVoxelisation.addCallableTaskListener(new CallableTaskAdapter() {
                            @Override
                            public void onSucceeded() {
                                nbFileProcessed.set(nbFileProcessed.getValue() + 1);
                                updateProgress(nbFileProcessed.intValue(), nbFilesToWrite);
                            }
                        });
                        tasks.add(rxpVoxelisation);
                    }

                    // wait for every scan voxelisation to finish
                    updateMessage("Voxelization...");
                    List<Future<File>> results = exec.invokeAll(tasks);
                    for (Future<File> result : results) {
                        files.add(result.get());
                    }

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
                } catch (InterruptedException | NullPointerException ex) {
                    this.cancel();
                    throw ex;
                }

                return files;
            }

            @Override
            protected void cancelled() {
                super.cancelled();

                if (tool != null) {
                    tool.setCancelled(true);
                }

            }
        };

        return task;
    }

    private String findKey(HashMap<String, String> map, String rxp) {

        for (String key : map.keySet()) {
            //System.out.println(rxp + " " + key + " startsWith? " + rxp.startsWith(key));
            if (rxp.startsWith(key)) {
                return key;
            }
        }
        return null;
    }

    private HashMap<String, String> readCSV(File file) throws FileNotFoundException, IOException {

        HashMap<String, String> map = new HashMap();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            // skip header
            reader.readLine();
            String line;
            while (null != (line = reader.readLine()) && !line.trim().isEmpty()) {
                String[] split = line.split("\t");
                if (2 != split.length) {
                    throw new IOException("Invalid line " + line + " in file " + file.getName() + ". Expect RXP_NAME \t CSV_FILE");
                }
                // expected split[0] = RXP_NAME split[1] CSV_FILE
                String csv = new File(file.toURI().resolve(split[1])).getCanonicalPath();
                map.put(split[0], csv);
            }
        }
        return map;
    }

}
