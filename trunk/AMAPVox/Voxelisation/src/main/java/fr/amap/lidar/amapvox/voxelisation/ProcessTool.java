/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.als.laz.LazExtraction;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.util.BoundingBox3d;
import fr.amap.commons.util.DataSet.Mode;
import static fr.amap.commons.util.DataSet.Mode.SUM;
import fr.amap.commons.util.LidarScan;
import fr.amap.commons.util.MatrixUtility;
import fr.amap.commons.util.ProcessingListener;
import fr.amap.commons.util.TimeCounter;
import fr.amap.commons.structure.octree.Octree;
import fr.amap.commons.structure.octree.OctreeFactory;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.amapvox.io.tls.rsp.RxpScan;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.util.Process;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.commons.util.vegetation.DirectionalTransmittance;
import fr.amap.commons.util.vegetation.LADParams;
import fr.amap.commons.util.vegetation.LeafAngleDistribution;
import fr.amap.lidar.amapvox.voxelisation.als.LasVoxelisation;
import fr.amap.lidar.amapvox.voxelisation.configuration.ALSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.Input;
import fr.amap.lidar.amapvox.voxelisation.configuration.MultiVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.PTXLidarScan;
import fr.amap.lidar.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import fr.amap.lidar.amapvox.voxelisation.tls.PTGVoxelisation;
import fr.amap.lidar.amapvox.voxelisation.tls.PTXVoxelisation;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpEchoFilter;
import fr.amap.lidar.amapvox.voxelisation.tls.RxpVoxelisation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;
import fr.amap.commons.util.Cancellable;/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class ProcessTool extends Process implements Cancellable{
    
    private final static Logger logger = Logger.getLogger(ProcessTool.class);
    
    private long startTime;
    private boolean cancelled;
    private ExecutorService exec;
    private int coresNumber;

    public ProcessTool() {
        cancelled = false;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        
        if(exec != null){
            exec.shutdownNow();
        }
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public static Raster loadDTM(File dtmFile) {

        Raster terrain = null;

        if (dtmFile != null) {

            try {
                terrain = AsciiGridHelper.readFromAscFile(dtmFile);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return terrain;
    }
    
    public static Octree loadOctree(CSVFile pointcloudFile, Mat4D vopMatrix) {

        Octree octree = null;
        
        if (pointcloudFile != null) {

            try {
                logger.info("Loading point cloud file...");
                octree = OctreeFactory.createOctreeFromPointFile(pointcloudFile, OctreeFactory.DEFAULT_MAXIMUM_POINT_NUMBER, false, vopMatrix);
                octree.build();
                logger.info("Point cloud file loaded");
                
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return octree;
    }
    
    public ArrayList<File> voxeliseFromPTG(TLSVoxCfg cfg){
        
        File output = cfg.getOutputFile();
        File input = cfg.getInputFile();
        VoxelParameters parameters = cfg.getVoxelParameters();
        Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
        List<LidarScan> matricesAndFiles = cfg.getLidarScans();
        cfg.setEchoFilter(new RxpEchoFilter(cfg.getEchoFilters()));
        
        if (!Files.isReadable(output.toPath())) {
            logger.error("File " + output.getAbsolutePath() + " not reachable");
        }

        if (!Files.isReadable(input.toPath())) {
            logger.error("File " + input.getAbsolutePath() + " not reachable");
        }

        startTime = System.currentTimeMillis();

        parameters.infos.setType(VoxelSpaceInfos.Type.TLS);
        
        Raster dtm = null;
        if (parameters.getDtmFilteringParams().useDTMCorrection()) {
            dtm = loadDTM(parameters.getDtmFilteringParams().getDtmFile());
        }
        
        List<PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();
        
        if(pointcloudFilters != null){
            
            if(vop == null){ vop = Mat4D.identity();}
            
            if(parameters.isUsePointCloudFilter()){
                for(fr.amap.lidar.amapvox.voxelisation.PointcloudFilter filter : pointcloudFilters){
                    filter.setOctree(loadOctree(filter.getPointcloudFile(), vop));
                }
            }
        }
        
        ArrayList<File> files = new ArrayList<>();
        exec = Executors.newFixedThreadPool(coresNumber);
        
        try {
            LinkedBlockingQueue<Callable<PTGVoxelisation>>  tasks = new LinkedBlockingQueue<>();

            int count = 1;
            for (LidarScan file : matricesAndFiles) {

                File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() + ".vox");
                tasks.put(new PTGVoxelisation(file.file, outputFile, vop, pop, MatrixUtility.convertMatrix4dToMat4D(file.matrix), parameters, dtm, pointcloudFilters, cfg));
                files.add(outputFile);
                count++;
            }
            
            exec.invokeAll(tasks);
            
            exec.shutdown();
            
            
        }catch (InterruptedException ex){
            logger.info("Voxelisation was stopped");
            cancelled = true;
            return null;
        }catch(NullPointerException ex){
            logger.error("Unknwown exception", ex);
            cancelled = true;
            return null;
        }finally{
            
        }
        
        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

        return files;
    }
    
    public ArrayList<File> voxeliseFromPTX(TLSVoxCfg cfg){
        
        File output = cfg.getOutputFile();
        File input = cfg.getInputFile();
        
        VoxelParameters parameters = cfg.getVoxelParameters();
        Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
        
        List<LidarScan> lidarScans = cfg.getLidarScans();
        cfg.setEchoFilter(new RxpEchoFilter(cfg.getEchoFilters()));
        
        if (!Files.isReadable(output.toPath())) {
            logger.error("File " + output.getAbsolutePath() + " not reachable");
        }

        if (!Files.isReadable(input.toPath())) {
            logger.error("File " + input.getAbsolutePath() + " not reachable");
        }

        startTime = System.currentTimeMillis();

        parameters.infos.setType(VoxelSpaceInfos.Type.TLS);
        
        Raster dtm = null;
        if (parameters.getDtmFilteringParams().useDTMCorrection()) {
            dtm = loadDTM(parameters.getDtmFilteringParams().getDtmFile());
        }
        
        List<PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();
        
        if(pointcloudFilters != null){
            
            if(vop == null){ vop = Mat4D.identity();}
            
            if(parameters.isUsePointCloudFilter()){
                for(fr.amap.lidar.amapvox.voxelisation.PointcloudFilter filter : pointcloudFilters){
                    filter.setOctree(loadOctree(filter.getPointcloudFile(), vop));
                }
            }
        }
        
        ArrayList<File> files = new ArrayList<>();
        exec = Executors.newFixedThreadPool(coresNumber);
        
        try {
            LinkedBlockingQueue<Callable<PTXVoxelisation>>  tasks = new LinkedBlockingQueue<>();

            int count = 0;
            for (LidarScan file : lidarScans) {
                
                PTXLidarScan scan = (PTXLidarScan)file;
                File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() +"-scan-"+count+ ".vox");
                tasks.put(new PTXVoxelisation(scan.getScan(), outputFile, vop, pop, MatrixUtility.convertMatrix4dToMat4D(file.matrix), parameters, dtm, pointcloudFilters, cfg));
                files.add(outputFile);
                count++;
            }
            
            exec.invokeAll(tasks);
            
            exec.shutdown();
            
            
        }catch (InterruptedException ex){
            logger.info("Voxelisation was stopped");
            cancelled = true;
            return null;
        }catch(NullPointerException ex){
            logger.error("Unknwown exception", ex);
            cancelled = true;
            return null;
        }finally{
            
        }
        

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

        return files;
    }

    public ArrayList<File> voxeliseFromRsp(TLSVoxCfg cfg){

        
        File output = cfg.getOutputFile();
        File input = cfg.getInputFile();
        VoxelParameters parameters = cfg.getVoxelParameters();
        Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
        List<LidarScan> lidarScans = cfg.getLidarScans();
        cfg.setEchoFilter(new RxpEchoFilter(cfg.getEchoFilters()));
        
        if (!Files.isReadable(output.toPath())) {
            logger.error("File " + output.getAbsolutePath() + " not reachable");
        }

        if (!Files.isReadable(input.toPath())) {
            logger.error("File " + input.getAbsolutePath() + " not reachable");
        }

        startTime = System.currentTimeMillis();

        parameters.infos.setType(VoxelSpaceInfos.Type.TLS);
        
        Raster dtm = null;
        if (parameters.getDtmFilteringParams().useDTMCorrection()) {
            dtm = loadDTM(parameters.getDtmFilteringParams().getDtmFile());
        }
        
        List<PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();
        //List<Octree> pointcloudList = null;
        
        if(pointcloudFilters != null){
            
            if(vop == null){ vop = Mat4D.identity();}
            
            //pointcloudList = new ArrayList<>();
            
            if(parameters.isUsePointCloudFilter()){
                for(fr.amap.lidar.amapvox.voxelisation.PointcloudFilter filter : pointcloudFilters){
                    filter.setOctree(loadOctree(filter.getPointcloudFile(), vop));
                    //pointcloudList.add(loadOctree(filter.getPointcloudFile(), vop));
                }
            }
        }
        
        ArrayList<File> files = new ArrayList<>();
        exec = Executors.newFixedThreadPool(coresNumber);
        
        
        try {
            LinkedBlockingQueue<Callable<RxpVoxelisation>>  tasks = new LinkedBlockingQueue<>();

            int count = 1;
            for (LidarScan file : lidarScans) {

                File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() + ".vox");
                tasks.put(new RxpVoxelisation(file.file, outputFile, vop, pop, MatrixUtility.convertMatrix4dToMat4D(file.matrix), parameters, dtm, pointcloudFilters, cfg));
                files.add(outputFile);
                count++;
            }
            
            List<Future<RxpVoxelisation>> futures = exec.invokeAll(tasks);
            
            /*for(Future f : futures){
                try {
                    f.get();
                } catch (InterruptedException ex) {
                    
                } catch (ExecutionException ex) {
                    logger.error(ex.getCause());
                }
            }*/
            
            
            exec.shutdown();
            
            
            
        }catch (InterruptedException ex){
            logger.info("Voxelisation was stopped");
            cancelled = true;
            return null;
        }catch(NullPointerException ex){
            logger.error("Unknwown exception", ex);
            cancelled = true;
            return null;
        }finally{
            
        }
        

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

        return files;
    }

    public void voxeliseFromRxp(TLSVoxCfg cfg) throws Exception {

        startTime = System.currentTimeMillis();
        
        File output = cfg.getOutputFile();
        File input = cfg.getInputFile();
        VoxelParameters parameters = cfg.getVoxelParameters();
        Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
        Mat4D sop = MatrixUtility.convertMatrix4dToMat4D(cfg.getSopMatrix());
        //List<Filter> filters = cfg.getFilters();        
        
        parameters.infos.setType(VoxelSpaceInfos.Type.TLS);

        RxpScan scan = new RxpScan();
        scan.setFile(input);

        fireProgress(output.getAbsolutePath(), 0, 100);

        Raster dtm = null;
        if (parameters.getDtmFilteringParams().useDTMCorrection()) {
            dtm = loadDTM(parameters.getDtmFilteringParams().getDtmFile());
        }
        
        List<PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();
        //List<Octree> pointcloudList = null;
        
        if(pointcloudFilters != null){
            
            if(vop == null){ vop = Mat4D.identity();}
            
            //pointcloudList = new ArrayList<>();
            
            if(parameters.isUsePointCloudFilter()){
                for(PointcloudFilter filter : pointcloudFilters){
                    filter.setOctree(loadOctree(filter.getPointcloudFile(), vop));
                    //pointcloudList.add(loadOctree(filter.getPointcloudFile(), vop));
                }
            }
        }
        
        if(pop == null){ pop = Mat4D.identity();}
        if(sop == null){ sop = Mat4D.identity();}
        if(vop == null){ vop = Mat4D.identity();}
        
        cfg.setEchoFilter(new RxpEchoFilter(cfg.getEchoFilters()));
        RxpVoxelisation voxelisation = new RxpVoxelisation(input, output, vop, pop, sop, parameters, dtm, pointcloudFilters, cfg);
        voxelisation.call();

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

    }

    public void voxeliseFromAls(ALSVoxCfg cfg) throws IOException, Exception {

        startTime = System.currentTimeMillis();

        cfg.getVoxelParameters().infos.setType(VoxelSpaceInfos.Type.ALS);
        
        LasVoxelisation voxelisation = new LasVoxelisation();
        
        voxelisation.addProcessingListener(new ProcessingListener() {

            @Override
            public void processingStepProgress(String progressMsg, long progress, long max) {
                fireProgress(progressMsg, progress, max);
            }

            @Override
            public void processingFinished(float duration) {

            }
        });

        voxelisation.process(cfg);

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

    }
    
    public BoundingBox3d getALSMinAndMax(File file){
        
        LasHeader header = null;

        switch (FileManager.getExtension(file)) {
            case ".las":
                LasReader lasReader = new LasReader();
                try {
                    header = lasReader.readHeader(file);
                } catch (IOException ex) {
                    logger.error(ex);
                }
                break;

            case ".laz":
                LazExtraction laz = new LazExtraction();
                try {
                    laz.openLazFile(file);
                } catch (Exception ex) {
                    logger.error(ex);
                }
                header = laz.getHeader();
                laz.close();
                break;
        }

        if (header != null) {

            double minX = header.getMinX();
            double minY = header.getMinY();
            double minZ = header.getMinZ();

            double maxX = header.getMaxX();
            double maxY = header.getMaxY();
            double maxZ = header.getMaxZ();

            return new BoundingBox3d(new Point3d(minX, minY, minZ), new Point3d(maxX, maxY, maxZ));
        }
        
        return null;
    }
    /**
     *
     * @param pointFile
     * @param resultMatrix
     * @param quick don't use classification filters
     * @param classificationsToDiscard list of point classification to skip during getting bounding box process
     * @return 
     */
    public BoundingBox3d getBoundingBoxOfPoints(File pointFile, Matrix4d resultMatrix, boolean quick, List<Integer> classificationsToDiscard){
        
        BoundingBox3d boundingBox = new BoundingBox3d();
        
        Matrix4d identityMatrix = new Matrix4d();
        identityMatrix.setIdentity();
                
        if (resultMatrix.equals(identityMatrix) && quick) {

            boundingBox= getALSMinAndMax(pointFile);

        } else {

            int count = 0;
            double xMin = 0, yMin = 0, zMin = 0;
            double xMax = 0, yMax = 0, zMax = 0;

            Mat4D mat = MatrixUtility.convertMatrix4dToMat4D(resultMatrix);
            LasHeader lasHeader;


            switch (FileManager.getExtension(pointFile)) {
                case ".las":

                    LasReader lasReader = new LasReader();
                    try {
                        lasReader.open(pointFile);
                    } catch (IOException ex) {
                        logger.error(ex);
                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                    lasHeader = lasReader.getHeader();
                    Iterator<PointDataRecordFormat> iterator = lasReader.iterator();

                    while (iterator.hasNext()) {

                        PointDataRecordFormat point = iterator.next();

                        if(classificationsToDiscard.contains(Integer.valueOf((int)point.getClassification()))){ //skip those

                        }else{
                            Vec4D pt = new Vec4D(((point.getX() * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                (point.getY() * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                (point.getZ() * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                1);

                            pt = Mat4D.multiply(mat, pt);

                            if (count != 0) {

                                if (pt.x < xMin) {
                                    xMin = pt.x;
                                } else if (pt.x > xMax) {
                                    xMax = pt.x;
                                }

                                if (pt.y < yMin) {
                                    yMin = pt.y;
                                } else if (pt.y > yMax) {
                                    yMax = pt.y;
                                }

                                if (pt.z < zMin) {
                                    zMin = pt.z;
                                } else if (pt.z > zMax) {
                                    zMax = pt.z;
                                }

                            } else {

                                xMin = pt.x;
                                yMin = pt.y;
                                zMin = pt.z;

                                xMax = pt.x;
                                yMax = pt.y;
                                zMax = pt.z;

                                count++;
                            }
                        }                                                
                    }
                    
                    boundingBox.min = new Point3d(xMin, yMin, zMin);
                    boundingBox.max = new Point3d(xMax, yMax, zMax);

                    break;

                case ".laz":
                    LazExtraction lazReader = new LazExtraction();
                    try {
                        lazReader.openLazFile(pointFile);
                    } catch (Exception ex) {
                        logger.error(ex);
                    }

                    lasHeader = lazReader.getHeader();
                    
                    Iterator<LasPoint> it = lazReader.iterator();

                    while (it.hasNext()) {

                        LasPoint point = it.next();

                        if(classificationsToDiscard.contains(Integer.valueOf(point.classification))){ //skip those

                        }else{
                            Vec4D pt = new Vec4D(((point.x * lasHeader.getxScaleFactor()) + lasHeader.getxOffset()),
                                (point.y * lasHeader.getyScaleFactor()) + lasHeader.getyOffset(),
                                (point.z * lasHeader.getzScaleFactor()) + lasHeader.getzOffset(),
                                1);

                            pt = Mat4D.multiply(mat, pt);

                            if (count != 0) {

                                if (pt.x < xMin) {
                                    xMin = pt.x;
                                } else if (pt.x > xMax) {
                                    xMax = pt.x;
                                }

                                if (pt.y < yMin) {
                                    yMin = pt.y;
                                } else if (pt.y > yMax) {
                                    yMax = pt.y;
                                }

                                if (pt.z < zMin) {
                                    zMin = pt.z;
                                } else if (pt.z > zMax) {
                                    zMax = pt.z;
                                }

                            } else {

                                xMin = pt.x;
                                yMin = pt.y;
                                zMin = pt.z;

                                xMax = pt.x;
                                yMax = pt.y;
                                zMax = pt.z;

                                count++;
                            }
                        }

                    }

                    boundingBox.min = new Point3d(xMin, yMin, zMin);
                    boundingBox.max = new Point3d(xMax, yMax, zMax);

                    lazReader.close();

                    break;
            }

        }
        
        return boundingBox;
    }    
    
    public void mergeVoxelFiles(VoxMergingCfg cfg) throws Exception {
                
        cancelled = false;
        
        startTime = System.currentTimeMillis();
        Mode[] toMerge;
        int size;
        VoxelSpaceInfos voxelSpaceHeader = new VoxelSpaceInfos();

        float[][] nbSamplingMultiplyAngleMean;
        float[][] resultingFile;
        int columnNumber;
        
        int padBVTotalColumnIndex = -1;
        int angleMeanColumnIndex = -1;
        int bvEnteringColumnIndex = -1;
        int bvInterceptedColumnIndex = -1;
        int lMeanTotalColumnIndex = -1;
        int lgTotalColumnIndex = -1;
        int nbEchosColumnIndex = -1;
        int nbSamplingColumnIndex = -1;
        int transmittanceColumnIndex = -1;
        
        if(cfg.getFiles().size() > 0){
            
            try {
                voxelSpaceHeader.readFromVoxelFile(cfg.getFiles().get(0));
            } catch (Exception ex) {
                throw ex;
            }
            size = voxelSpaceHeader.getSplit().x * voxelSpaceHeader.getSplit().y * voxelSpaceHeader.getSplit().z;
            columnNumber = voxelSpaceHeader.getColumnNamesList().size();
            resultingFile = new float[size][columnNumber];
            toMerge = new Mode[columnNumber];
            
            for(int i=0;i<toMerge.length;i++){
                
                String columnName = voxelSpaceHeader.getColumnNamesList().get(i);
                                
                switch(columnName){
                    case "i":
                    case "j":
                    case "k":
                        toMerge[i] = Mode.DISCARD;
                        break;
                    case "ground_distance":
                        toMerge[i] = Mode.DISCARD;
                        break;
                    //discard but recalculate after
                    case "PadBVTotal":
                        padBVTotalColumnIndex = i;
                        toMerge[i] = Mode.DISCARD;
                        break;
                    case "angleMean":
                        angleMeanColumnIndex = i;
                        toMerge[i] = Mode.DISCARD;
                        break;
                    case "lMeanTotal":
                        lMeanTotalColumnIndex = i;
                        toMerge[i] = Mode.DISCARD;
                        break;
                    case "transmittance":
                        transmittanceColumnIndex = i;
                        toMerge[i] = Mode.DISCARD;
                        break;

                    case "nbSampling":
                        nbSamplingColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;
                    case "nbEchos":
                        nbEchosColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;
                    case "lgTotal":
                        lgTotalColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;
                    case "bvEntering":
                        bvEnteringColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;
                    case "bvIntercepted":
                        bvInterceptedColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;

                    default:
                        toMerge[i] = Mode.DISCARD;
                }
            }
            
            nbSamplingMultiplyAngleMean = new float[cfg.getFiles().size()][size];
            
            
            
        }else{
            logger.info("No file to merge");
            return;
        }

        for (int i = 0; i < cfg.getFiles().size(); i++) {

            if (cancelled) {
                return;
            }

            String msg = "Merging in progress, file " + (i + 1) + " : " + cfg.getFiles().size();
            logger.info(msg);
            fireProgress(msg, (i+1), cfg.getFiles().size());
            
            try (BufferedReader reader = new BufferedReader(new FileReader(cfg.getFiles().get(i)))){
                
                
                int count = 0;
                FileManager.skipLines(reader, 6);
                
                String currentFileLine;
                while((currentFileLine = reader.readLine()) != null){
                    
                    String[] lineSplittedFile = currentFileLine.split(" ");
                    
                    if(lineSplittedFile.length != columnNumber){
                        logger.error("Columns number doesn't match!");
                        return;
                    }
                    
                    float[] voxelLine = new float[columnNumber];
                    float nbSampling = 0;
                    float angleMean = 0;
                    
                    for(int j=0;j<lineSplittedFile.length;j++){
                        
                        float currentValue = Float.valueOf(lineSplittedFile[j]);
                        float resultValue;
                        
                        if(i == 0){
                            resultValue = currentValue;
                        }else{
                            resultValue = resultingFile[count][j];
                            switch(toMerge[j]){
                                case SUM:
                                    if(Float.isNaN(resultValue)){
                                        resultValue = Float.valueOf(lineSplittedFile[j]);
                                    }else if(!Float.isNaN(currentValue)){
                                        resultValue += currentValue;
                                    }
                                    break;
                                default:
                                    resultValue = currentValue;
                            }
                        } 
                        
                        if(j == nbSamplingColumnIndex){
                            nbSampling =  Float.valueOf(lineSplittedFile[j]);
                        }else if(j == angleMeanColumnIndex){
                            angleMean =  Float.valueOf(lineSplittedFile[j]);
                        }
                        
                        voxelLine[j] = resultValue;
                    }
                    
                    resultingFile[count] = voxelLine;
                    nbSamplingMultiplyAngleMean[i][count] = nbSampling * angleMean;
                    
                    count++;
                }
                
            } catch (FileNotFoundException ex) {
                logger.error(ex);
            } catch (IOException ex) {
                logger.error(ex);
            }
        }

        logger.info("Compute angleMean");
        if(nbSamplingColumnIndex != -1 && angleMeanColumnIndex !=-1){
            
            for (int i = 0; i < size; i++) {
                
                float sum = 0;
                
                for (int j = 0; j < cfg.getFiles().size(); j++) {
                    if (!Float.isNaN(nbSamplingMultiplyAngleMean[j][i])) {
                        sum += nbSamplingMultiplyAngleMean[j][i];
                    } 
                }

                resultingFile[i][angleMeanColumnIndex] = sum/(resultingFile[i][nbSamplingColumnIndex]);
            }

                
            
        }else{
            logger.error("nbSampling or angleMean columns are missing, cannot re-compute angleMean");
        }
        
        logger.info("Compute lMeanTotal");
        for (int i = 0; i < size; i++) {
            
            resultingFile[i][lMeanTotalColumnIndex] = resultingFile[i][lgTotalColumnIndex] / resultingFile[i][nbSamplingColumnIndex];
        }
        
        logger.info("Compute transmittance and PAD");
        
        //LeafAngleDistribution distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLANOPHILE);
        LADParams ladParameters = cfg.getVoxelParameters().getLadParams();
        if(ladParameters == null){
            ladParameters = new LADParams();
        }
        LeafAngleDistribution distribution = new LeafAngleDistribution(ladParameters.getLadType(), 
                ladParameters.getLadBetaFunctionAlphaParameter(),
                ladParameters.getLadBetaFunctionBetaParameter());
        
        DirectionalTransmittance direcTransmittance = new DirectionalTransmittance(distribution);
        
        VoxelAnalysis voxelAnalysis = new VoxelAnalysis();
        voxelAnalysis.init(cfg.getVoxelParameters(), null);
        
        logger.info("Building transmittance functions table");
        direcTransmittance.buildTable(DirectionalTransmittance.DEFAULT_STEP_NUMBER);
        logger.info("Transmittance functions table is built");
        
        for (int i = 0; i < size; i++) {

            resultingFile[i][transmittanceColumnIndex] = voxelAnalysis.computeTransmittance(resultingFile[i][bvEnteringColumnIndex], resultingFile[i][bvInterceptedColumnIndex]);
            resultingFile[i][transmittanceColumnIndex] =  voxelAnalysis.computeNormTransmittance(resultingFile[i][transmittanceColumnIndex], resultingFile[i][lMeanTotalColumnIndex]);
            resultingFile[i][padBVTotalColumnIndex] = voxelAnalysis.computePADFromNormTransmittance(resultingFile[i][transmittanceColumnIndex], resultingFile[i][angleMeanColumnIndex]);
        }
        
        logger.info("writing output file: " + cfg.getOutputFile().getAbsolutePath());
        long start_time = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cfg.getOutputFile()))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + (float) voxelSpaceHeader.getMinCorner().x + " " + (float) voxelSpaceHeader.getMinCorner().y + " " + (float) voxelSpaceHeader.getMinCorner().z + "\n");
            writer.write("#max_corner: " + (float) voxelSpaceHeader.getMaxCorner().x + " " + (float) voxelSpaceHeader.getMaxCorner().y + " " + (float) voxelSpaceHeader.getMaxCorner().z + "\n");
            writer.write("#split: " + voxelSpaceHeader.getSplit().x + " " + voxelSpaceHeader.getSplit().y + " " + voxelSpaceHeader.getSplit().z + "\n");

            writer.write("#type: TLS" + " #res: "+voxelSpaceHeader.getResolution()+" "+"#MAX_PAD: "+cfg.getVoxelParameters().infos.getMaxPAD()+"\n");

            String header = "";
            
            for (String columnName : voxelSpaceHeader.getColumnNamesList()) {
                header += columnName + " ";
            }
            header = header.trim();
            writer.write(header + "\n");

            for (int i = 0; i < size; i++) {
                
                StringBuilder voxel = new StringBuilder();
                
                for (int j = 0;j<columnNumber;j++){
                    
                    if (j < 3) {
                        voxel.append((int)resultingFile[i][j]);
                    }else{
                        voxel.append(resultingFile[i][j]);
                    }
                    
                    if(j < columnNumber-1){
                        voxel.append(" ");
                    }
                }

                writer.write(voxel.toString() + "\n");

            }
            
            logger.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

        } catch (IOException ex) {
            logger.error(ex);
        }
        
        

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
    }
    
    public void multiVoxelisation(MultiVoxCfg configuration) throws Exception{
        
        startTime = System.currentTimeMillis();
        configuration.getVoxelParameters().infos.setType(VoxelSpaceInfos.Type.ALS);
        
        List<Input> inputs = configuration.getMultiProcessInputs();
        
        if(inputs.isEmpty()){
            return;
        }
        
        Raster terrain = null;
        Mat4D vopMatrix = MatrixUtility.convertMatrix4dToMat4D(configuration.getVopMatrix());
        if(vopMatrix == null){
            vopMatrix = Mat4D.identity();
        }
        
        if(inputs.get(0).dtmFile != null){
            
            
            
        }else if(configuration.getVoxelParameters().getDtmFilteringParams().getDtmFile() != null && 
                configuration.getVoxelParameters().getDtmFilteringParams().useDTMCorrection() ){
            
            fireProgress("Reading DTM file", 0, 100);
            
            try {
                terrain = AsciiGridHelper.readFromAscFile(configuration.getVoxelParameters().getDtmFilteringParams().getDtmFile());
                terrain.setTransformationMatrix(vopMatrix);
                
            } catch (Exception ex) {
                logger.error(ex);
            }
        } 
        
        
        if(configuration.getTrajectoryFile() == null){
            logger.error("Trajectory file is null");
            return;
        }
        
        VoxelParameters params = configuration.getVoxelParameters();
        
        int count = 1;
        for(Input input : inputs){
            
            if(cancelled){
                return;
            }
            
            fireProgress("Processing file "+count+"/"+inputs.size()+" : "+input.inputFile.getAbsolutePath(), 0, 100);
            
            params.infos.setMinCorner(input.voxelParameters.infos.getMinCorner());
            params.infos.setMaxCorner(input.voxelParameters.infos.getMaxCorner());
            params.infos.setSplit(input.voxelParameters.infos.getSplit());
            params.infos.setResolution(input.voxelParameters.infos.getResolution());
            
            configuration.setInputFile(input.inputFile);
            configuration.setOutputFile(input.outputFile);
            
            LasVoxelisation voxelisation = new LasVoxelisation();
            
            voxelisation.process(configuration);
            
            count++;
        }
        
        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
    }
    /*
    public void calculateTransmittance(TransmittanceCfg cfg){
        
        Parameters parameters = cfg.getParameters();
        
        TransmittanceSim padTransmittance = new TransmittanceSim(parameters);
        padTransmittance.process();
        
        if(parameters.isGenerateTextFile()){
            padTransmittance.writeTransmittance();
        }
        
        if(parameters.isGenerateBitmapFile()){
            padTransmittance.writeBitmaps();
        }
    }*/

    public void setCoresNumber(int coresNumber) {
        this.coresNumber = coresNumber;
    }
}
