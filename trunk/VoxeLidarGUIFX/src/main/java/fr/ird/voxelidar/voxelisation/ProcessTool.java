/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import fr.ird.voxelidar.configuration.VoxelisationConfiguration;
import fr.ird.voxelidar.configuration.Input;
import fr.ird.voxelidar.configuration.MatrixAndFile;
import fr.ird.voxelidar.util.ProcessingListener;
import fr.ird.voxelidar.voxelisation.tls.RxpVoxelisation;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec2D;
import fr.ird.voxelidar.engine3d.math.vector.Vec4D;
import fr.ird.voxelidar.engine3d.misc.Attribut;
import fr.ird.voxelidar.lidar.format.dtm.RegularDtm;
import fr.ird.voxelidar.lidar.format.dtm.DtmLoader;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceHeader;
import fr.ird.voxelidar.io.file.FileManager;
import fr.amap.lidar.als.LasHeader;
import fr.amap.lidar.als.las.LasReader;
import fr.amap.lidar.als.las.PointDataRecordFormat;
import fr.ird.voxelidar.multires.ProcessingMultiRes;
import fr.ird.voxelidar.octree.Octree;
import fr.ird.voxelidar.octree.OctreeFactory;
import fr.ird.voxelidar.transmittance.TransmittanceSim;
import fr.ird.voxelidar.transmittance.Parameters;
import fr.ird.voxelidar.util.Cancellable;
import fr.ird.voxelidar.util.DataSet;
import fr.ird.voxelidar.util.DataSet.Mode;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.MatrixConverter;
import fr.ird.voxelidar.util.TimeCounter;
import fr.amap.lidar.als.LasPoint;
import fr.ird.voxelidar.voxelisation.als.LasVoxelisation;
import fr.ird.voxelidar.voxelisation.als.Trajectory;
import fr.amap.lidar.als.laz.LazExtraction;
import fr.ird.voxelidar.configuration.Configuration;
import fr.ird.voxelidar.configuration.MultiVoxCfg;
import fr.ird.voxelidar.configuration.TransmittanceCfg;
import fr.ird.voxelidar.configuration.VoxMergingCfg;
import fr.ird.voxelidar.voxelisation.extraction.tls.RxpExtraction;
import fr.ird.voxelidar.voxelisation.raytracing.util.BoundingBox3d;
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
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix4d;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class ProcessTool implements Cancellable{
    
    
    final static Logger logger = Logger.getLogger(ProcessTool.class);

    private VoxelParameters parameters;
    private final EventListenerList listeners;
    private long startTime;
    private RegularDtm dtm;
    private List<Octree> pointcloudList;
    //private PointCloud pointcloud;
    private boolean cancelled;
    private ExecutorService exec;

    public ProcessTool() {
        listeners = new EventListenerList();
        cancelled = false;
    }

    public void addVoxelisationToolListener(VoxelisationToolListener listener) {
        listeners.add(VoxelisationToolListener.class, listener);
    }

    public void fireProgress(String progress, int ratio) {
        for (VoxelisationToolListener voxelisationToolListener : listeners.getListeners(VoxelisationToolListener.class)) {
            voxelisationToolListener.voxelisationProgress(progress, ratio);
        }
    }

    public void fireFinished(float duration) {

        for (VoxelisationToolListener voxelisationToolListener : listeners.getListeners(VoxelisationToolListener.class)) {
            voxelisationToolListener.voxelisationFinished(duration);
        }
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

    private RegularDtm loadDTM(File dtmFile) {

        RegularDtm terrain = null;

        if (dtmFile != null && parameters.useDTMCorrection()) {

            try {
                terrain = DtmLoader.readFromAscFile(dtmFile);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return terrain;
    }
    
    private Octree loadOctree(File pointcloudFile, Mat4D vopMatrix) {

        Octree octree = null;
        
        if (pointcloudFile != null && parameters.isUsePointCloudFilter()) {

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
    
    private PointCloud loadPointcloud(File pointcloudFile, Mat4D transfMatrix) {

        PointCloud ptCloud = null;

        if (pointcloudFile != null && parameters.isUsePointCloudFilter()) {

            try {
                ptCloud = new PointCloud();
                
                logger.info("Loading point cloud file...");
                ptCloud.readFromFile(pointcloudFile, transfMatrix);
                logger.info("Point cloud file loaded");
                
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return ptCloud;
    }

    public ArrayList<File> voxeliseFromRsp(File output, File input, VoxelParameters parameters, Mat4D vop, Mat4D pop, List<MatrixAndFile> matricesAndFiles, List<Filter> filters, int coresNumber){

        if (!Files.isReadable(output.toPath())) {
            logger.error("File " + output.getAbsolutePath() + " not reachable");
        }

        if (!Files.isReadable(input.toPath())) {
            logger.error("File " + input.getAbsolutePath() + " not reachable");
        }

        startTime = System.currentTimeMillis();

        this.parameters = parameters;
        this.parameters.setTLS(true);
        
        dtm = loadDTM(parameters.getDtmFile());
        
        List<PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();
        
        if(pointcloudFilters != null){
            
            if(vop == null){ vop = Mat4D.identity();}
            
            pointcloudList = new ArrayList<>();
            for(PointcloudFilter filter : pointcloudFilters){
                pointcloudList.add(loadOctree(filter.getPointcloudFile(), vop));
            }
        }
        
        //pointcloud = loadPointcloud(parameters.getPointcloudFile());
        
        ArrayList<File> files = new ArrayList<>();
        exec = Executors.newFixedThreadPool(coresNumber);
        
        
        try {
            LinkedBlockingQueue<Callable<RxpVoxelisation>>  tasks = new LinkedBlockingQueue<>();

            int count = 1;
            for (MatrixAndFile file : matricesAndFiles) {

                File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() + ".vox");
                tasks.put(new RxpVoxelisation(file.file, outputFile, vop, pop, MatrixConverter.convertMatrix4dToMat4D(file.matrix), this.parameters, dtm, pointcloudList, filters));
                files.add(outputFile);
                count++;
            }
            
            //test pour savoir si le chargemen tardif de la librairie est en cause
            RxpExtraction rxpExtraction = new RxpExtraction();
            rxpExtraction = null;
            
            /*List<Future<RxpVoxelisation>> results = */exec.invokeAll(tasks);
            
            exec.shutdown();
            /*
            if(parameters.isUsePointCloudFilter() && pointcloudList != null){
                
                int filteredPointsCount = 0;
                for (Future f : results) {
                    VoxelAnalysisData resultData = (VoxelAnalysisData) f.get();
                    filteredPointsCount += resultData.filteredPointsCount;
                }
                
                //logger.info("Number of echos filtered : "+filteredPointsCount);
                //logger.info("Number of points in point cloud: "+pointcloudList.getPoints().length);
            }*/
            
            
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

    public void voxeliseFromRxp(File output, File input, File dtmFile, VoxelParameters parameters, Mat4D vop, Mat4D pop, Mat4D sop, List<Filter> filters) {

        startTime = System.currentTimeMillis();

        this.parameters = parameters;
        this.parameters.setTLS(true);

        RxpScan scan = new RxpScan();
        scan.setFile(input);

        fireProgress(output.getAbsolutePath(), 1);

        if (dtm == null && dtmFile != null) {
            dtm = loadDTM(dtmFile);
        }
        
        if(pop == null){ pop = Mat4D.identity();}
        if(sop == null){ sop = Mat4D.identity();}
        if(vop == null){ vop = Mat4D.identity();}

        RxpVoxelisation voxelisation = new RxpVoxelisation(input, output, vop, pop, sop, parameters, dtm, pointcloudList, filters);
        voxelisation.call();

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

    }

    public void voxeliseFromAls(File output, File input, File trajectoryFile, VoxelParameters parameters, Mat4D vop, List<Filter> filters, List<Integer> classifiedPointsToDiscard) {

        startTime = System.currentTimeMillis();

        this.parameters = parameters;
        this.parameters.setTLS(false);

        if (vop == null) {
            vop = Mat4D.identity();
        }

        
        
        RegularDtm terrain = null;
        
        if(parameters.getDtmFile() != null && parameters.useDTMCorrection() ){
            
            fireProgress("Reading DTM file", 0);
            
            try {
                terrain = DtmLoader.readFromAscFile(parameters.getDtmFile());
                terrain.setTransformationMatrix(vop);
            } catch (Exception ex) {
                logger.error(ex);
            }
        } 
        
        fireProgress("Reading trajectory file", 0);
        
        List<Trajectory> trajectoryList = new ArrayList<>();
        
        try {            
            
            BufferedReader reader = new BufferedReader(new FileReader(trajectoryFile));

            String line;

            //skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                
                line = line.replaceAll(",", " ");
                String[] lineSplit = line.split(" ");
                
                Double time = Double.valueOf(lineSplit[3]);
                
                Trajectory traj = new Trajectory(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                        Double.valueOf(lineSplit[2]), time);

                //troncate unused values
                //if(time >= minTime-0.01 && time <= maxTime+0.01){
                    trajectoryList.add(traj);
                //}
            }

        } catch (FileNotFoundException ex) {
            logger.error(ex);
            return;
        } catch (IOException ex) {
            logger.error(ex);
            return;
        }
        
        LasVoxelisation voxelisation = new LasVoxelisation(input, output, vop, parameters, filters, classifiedPointsToDiscard, terrain, trajectoryList);
        
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
    
    public BoundingBox3d getALSMinAndMax(File file){
        
        LasHeader header = null;

        switch (FileManager.getExtension(file)) {
            case ".las":
                LasReader lasReader = new LasReader();
                header = lasReader.readHeader(file);
                break;

            case ".laz":
                LazExtraction laz = new LazExtraction();
                laz.openLazFile(file);
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

            Mat4D mat = MatrixConverter.convertMatrix4dToMat4D(resultMatrix);
            LasHeader lasHeader;


            switch (FileManager.getExtension(pointFile)) {
                case ".las":

                    LasReader lasReader = new LasReader();
                    lasReader.open(pointFile);

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
                    lazReader.openLazFile(pointFile);

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
    
    public void mergeVoxelFiles(VoxMergingCfg cfg/*List<File> filesList, File output, int transmittanceMode, float maxPAD*/) {
                
        cancelled = false;
        
        startTime = System.currentTimeMillis();
        Mode[] toMerge;
        int size;
        VoxelSpaceHeader voxelSpaceHeader;

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
            
            voxelSpaceHeader = VoxelSpaceHeader.readVoxelFileHeader(cfg.getFiles().get(0));
            size = voxelSpaceHeader.split.x * voxelSpaceHeader.split.y * voxelSpaceHeader.split.z;
            columnNumber = voxelSpaceHeader.attributsNames.size();
            resultingFile = new float[size][columnNumber];
            toMerge = new Mode[columnNumber];
            
            for(int i=0;i<toMerge.length;i++){
                
                String columnName = voxelSpaceHeader.attributsNames.get(i);
                                
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
            fireProgress(msg, i);
            
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
        for (int i = 0; i < size; i++) {
            
            resultingFile[i][transmittanceColumnIndex] = (resultingFile[i][bvEnteringColumnIndex] - resultingFile[i][bvInterceptedColumnIndex])/
                                                        resultingFile[i][bvEnteringColumnIndex];

            float pad1;

            if (resultingFile[i][bvEnteringColumnIndex] <= 0) {

                pad1 = Float.NaN;
                resultingFile[i][transmittanceColumnIndex] = Float.NaN;

            } else if (resultingFile[i][bvInterceptedColumnIndex] > resultingFile[i][bvEnteringColumnIndex]) {

                logger.error("BFInterceptes > BFEntering, NaN assigné");

                pad1 = Float.NaN;
                resultingFile[i][transmittanceColumnIndex] = Float.NaN;

            } else {

                if (resultingFile[i][nbSamplingColumnIndex] > 1 && resultingFile[i][transmittanceColumnIndex] == 0 && Objects.equals(resultingFile[i][nbSamplingColumnIndex], resultingFile[i][nbEchosColumnIndex])) {

                    pad1 = cfg.getVoxelParameters().getMaxPAD();

                } else if (resultingFile[i][nbSamplingColumnIndex] <= 2 && resultingFile[i][transmittanceColumnIndex] == 0 && Objects.equals(resultingFile[i][nbSamplingColumnIndex], resultingFile[i][nbEchosColumnIndex])) {

                    pad1 = Float.NaN;

                } else {

                    pad1 = (float) (Math.log(resultingFile[i][transmittanceColumnIndex]) / (-0.5 * resultingFile[i][lMeanTotalColumnIndex]));

                    if (Float.isNaN(pad1)) {
                        pad1 = Float.NaN;
                    } else if (pad1 > cfg.getVoxelParameters().getMaxPAD() || Float.isInfinite(pad1)) {
                        pad1 = cfg.getVoxelParameters().getMaxPAD();
                    }
                }
            }

            resultingFile[i][padBVTotalColumnIndex] = pad1 + 0.0f;
        }
        
        logger.info("writing output file: " + cfg.getOutputFile().getAbsolutePath());
        long start_time = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cfg.getOutputFile()))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + (float) voxelSpaceHeader.bottomCorner.x + " " + (float) voxelSpaceHeader.bottomCorner.y + " " + (float) voxelSpaceHeader.bottomCorner.z + "\n");
            writer.write("#max_corner: " + (float) voxelSpaceHeader.topCorner.x + " " + (float) voxelSpaceHeader.topCorner.y + " " + (float) voxelSpaceHeader.topCorner.z + "\n");
            writer.write("#split: " + voxelSpaceHeader.split.x + " " + voxelSpaceHeader.split.y + " " + voxelSpaceHeader.split.z + "\n");

            writer.write("#type: TLS" + " #res: "+voxelSpaceHeader.res+" "+"#MAX_PAD: "+cfg.getVoxelParameters().getMaxPAD()+"\n");

            String header = "";
            
            for (String columnName : voxelSpaceHeader.attributsNames) {
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
    
    public void multiVoxelisation(MultiVoxCfg configuration){
        
        startTime = System.currentTimeMillis();
        configuration.getVoxelParameters().setTLS(false);
        
        List<Input> inputs = configuration.getMultiProcessInputs();
        
        if(inputs.isEmpty()){
            return;
        }
        
        RegularDtm terrain = null;
        Mat4D vopMatrix = MatrixConverter.convertMatrix4dToMat4D(configuration.getVopMatrix());
        if(vopMatrix == null){
            vopMatrix = Mat4D.identity();
        }
        
        if(inputs.get(0).dtmFile != null){
            
            
            
        }else if(configuration.getVoxelParameters().getDtmFile() != null && configuration.getVoxelParameters().useDTMCorrection() ){
            
            fireProgress("Reading DTM file", 0);
            
            try {
                terrain = DtmLoader.readFromAscFile(configuration.getVoxelParameters().getDtmFile());
                terrain.setTransformationMatrix(vopMatrix);
                
            } catch (Exception ex) {
                logger.error(ex);
            }
        } 
        
        fireProgress("Reading trajectory file", 0);
        
        if(configuration.getTrajectoryFile() == null){
            logger.error("Trajectory file is null");
            return;
        }
        
        List<Trajectory> trajectoryList = new ArrayList<>();
        
        try {
            //maxIterations = FileManager.getLineNumber(trajectoryFile.getAbsolutePath());
            //step = (int) (maxIterations/10);
            //iterations = 0;
            
            BufferedReader reader = new BufferedReader(new FileReader(configuration.getTrajectoryFile()));

            String line;

            //skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                /*
                if(iterations % step == 0){
                    fireProgress("Reading trajectory file", (int) ((iterations*100)/(float)maxIterations));
                }*/
                
                line = line.replaceAll(",", " ");
                String[] lineSplit = line.split(" ");
                
                Double time = Double.valueOf(lineSplit[3]);
                
                Trajectory traj = new Trajectory(Double.valueOf(lineSplit[0]), Double.valueOf(lineSplit[1]),
                        Double.valueOf(lineSplit[2]), time);

                //troncate unused values
                //if(time >= minTime-0.01 && time <= maxTime+0.01){
                    trajectoryList.add(traj);
                    //trajectoryMap.put(time, traj);
                //}
                
                //iterations++;
            }

        } catch (FileNotFoundException ex) {
            logger.error(ex);
            return;
        } catch (IOException ex) {
            logger.error(ex);
            return;
        }
        
        VoxelParameters params = configuration.getVoxelParameters();
        
        int count = 1;
        for(Input input : inputs){
            
            if(cancelled){
                return;
            }
            
            if(input.dtmFile != null){
                
                try {
                    fireProgress("Reading DTM file : "+input.dtmFile.getAbsolutePath(), 0);
                    terrain = DtmLoader.readFromAscFile(input.dtmFile);
                    terrain.setTransformationMatrix(vopMatrix);
                } catch (Exception ex) {
                    logger.error(ex);
                    return;
                }                
            }
            
            List<Input> multiResInputs = input.multiResList;
            
            fireProgress("Processing file "+count+"/"+inputs.size()+" : "+input.inputFile.getAbsolutePath(), 0);
            
            params.setBottomCorner(input.voxelParameters.getBottomCorner());
            params.setTopCorner(input.voxelParameters.getTopCorner());
            params.setSplit(input.voxelParameters.getSplit());
            params.setResolution(input.voxelParameters.getResolution());
            
            LasVoxelisation voxelisation = new LasVoxelisation(input.inputFile, input.outputFile, vopMatrix, params, configuration.getFilters(), configuration.getClassifiedPointsToDiscard(), terrain, trajectoryList);
            
            voxelisation.process();
            
            List<File> files = new ArrayList<>();
            files.add(input.outputFile);
            
            if(multiResInputs != null){
                
                for(Input multiResInput : multiResInputs){
                    
                    params.setSplit(multiResInput.voxelParameters.getSplit());
                    params.setResolution(multiResInput.voxelParameters.getResolution());
                    voxelisation.setOutputFile(multiResInput.outputFile);
                    voxelisation.setParameters(params);
                    voxelisation.setUpdateALS(false);
                    
                    voxelisation.process();
                    
                    files.add(multiResInput.outputFile);
                }
                
                ProcessingMultiRes processingMultiRes = new ProcessingMultiRes(configuration.getMultiResPadMax(), false);
                processingMultiRes.process(files);
                processingMultiRes.write(input.outputFileMultiRes);
            }
            
            count++;
        }
        
        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
    }
    
    public void calculateTransmittance(Parameters parameters){
        
        TransmittanceSim padTransmittance = new TransmittanceSim(parameters);
        padTransmittance.process();
        
        if(parameters.isGenerateTextFile()){
            padTransmittance.writeTransmittance();
        }
        
        if(parameters.isGenerateBitmapFile()){
            padTransmittance.writeBitmaps();
        }
    }

    public static Matrix4d getMatrixTransformation(Vector3d point1, Vector3d point2) {

        if ((point1.x == point2.x) && (point1.y == point2.y) && (point1.z == point2.z)) {

            return new Matrix4d();
        }
        Vec2D v = new Vec2D(point1.x - point2.x, point1.y - point2.y);
        double rho = (double) Math.atan(v.x / v.y);

        Vector3d trans = new Vector3d(-point2.x, -point2.y, -point2.z);
        trans.z = 0; //no vertical translation

        Matrix4d mat4x4Rotation = new Matrix4d();
        Matrix4d mat4x4Translation = new Matrix4d();

        //rotation autour de l'axe z
        mat4x4Rotation.set(new double[]{
            (double) Math.cos(rho), (double) -Math.sin(rho), 0, 0,
            (double) Math.sin(rho), (double) Math.cos(rho), 0, 0,
            0, 0, 1, 0,
            0, 0, 0, 1
        });

        mat4x4Translation.set(new double[]{
            1, 0, 0, trans.x,
            0, 1, 0, trans.y,
            0, 0, 1, trans.z,
            0, 0, 0, 1
        });

        mat4x4Rotation.mul(mat4x4Translation);
        return mat4x4Rotation;
    }

    public void addVoxelisationToolListener(ProcessingListener processingListener) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /*
    public void executeTask(File configurationFile){
        
        String type = Configuration.readType(configurationFile);
        
                    
        switch(type){
            case "transmittance":

                TransmittanceCfg cfg = new TransmittanceCfg();
                cfg.readConfiguration(configurationFile);
                voxTool.calculateTransmittance(cfg.getParameters());

                break;

            default:


                final int coreNumberToUse = (int) sliderRSPCoresToUse.getValue();

                final long start_time = System.currentTimeMillis();

                String processMode = Configuration.readType(file);

                                final String msgTask = "Task " + taskID + "/" + taskNumber + " :" + file.getAbsolutePath();

                                switch (processMode) {

                                    case "merging":
                                        final VoxMergingCfg voxMergingCfg = new VoxMergingCfg();
                                        voxMergingCfg.readConfiguration(file);

                                        voxTool.mergeVoxelsFileV2(voxMergingCfg.getFiles(), voxMergingCfg.getOutputFile(), 0, voxMergingCfg.getVoxelParameters().getMaxPAD());

                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                addFileToVoxelList(voxMergingCfg.getOutputFile());
                                                setOnSucceeded(null);
                                            }
                                        });

                                        break;

                                    case "voxelisation-ALS":

                                        final ALSVoxCfg aLSVoxCfg = new ALSVoxCfg();
                                        aLSVoxCfg.readConfiguration(file);

                                        voxTool.addVoxelisationToolListener(new VoxelisationToolListener() {

                                            @Override
                                            public void voxelisationProgress(String progress, int ratio) {
                                                Platform.runLater(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        updateMessage(msgTask + "\n" + progress);
                                                    }
                                                });

                                            }

                                            @Override
                                            public void voxelisationFinished(float duration) {

                                                logger.info("las voxelisation finished in " + TimeCounter.getElapsedStringTimeInSeconds(start_time));
                                            }
                                        });

                                        voxTool.voxeliseFromAls(aLSVoxCfg.getOutputFile(), aLSVoxCfg.getInputFile(), aLSVoxCfg.getTrajectoryFile(), aLSVoxCfg.getVoxelParameters(), MatrixConverter.convertMatrix4dToMat4D(aLSVoxCfg.getVopMatrix()), aLSVoxCfg.getFilters(), aLSVoxCfg.getClassifiedPointsToDiscard());

                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {

                                                addFileToVoxelList(aLSVoxCfg.getOutputFile());
                                            }
                                        });

                                        break;

                                    case "voxelisation-TLS":

                                        final TLSVoxCfg cfg = new TLSVoxCfg();
                                        cfg.readConfiguration(file);
                                        //final VoxelisationConfiguration cfg1 = new VoxelisationConfiguration();
                                        //cfg1.readConfiguration(file);

                                        switch (cfg.getInputType()) {

                                            case RSP_PROJECT:

                                                try {
                                                    ArrayList<File> outputFiles = voxTool.voxeliseFromRsp(cfg.getOutputFile(), cfg.getInputFile(), cfg.getVoxelParameters(),
                                                            MatrixConverter.convertMatrix4dToMat4D(cfg.getVopMatrix()),
                                                            MatrixConverter.convertMatrix4dToMat4D(cfg.getPopMatrix()),
                                                            cfg.getMatricesAndFiles(), cfg.getFilters(), coreNumberToUse);

                                                    if (cfg.getVoxelParameters().isMergingAfter()) {

                                                        //if(!voxTool.isCancelled()){
                                                            mergeVoxelsFileV2(outputFiles, cfg.getVoxelParameters().getMergedFile(), cfg.getVoxelParameters().getTransmittanceMode(), cfg.getVoxelParameters().getMaxPAD());
                                                        //}

                                                    }


                                                }catch (Exception e) {

                                                }

                                                break;

                                            case RXP_SCAN:

                                                voxTool.voxeliseFromRxp(cfg.getOutputFile(), cfg.getInputFile(),
                                                        cfg.getVoxelParameters().getDtmFile(),
                                                        cfg.getVoxelParameters(),
                                                        MatrixConverter.convertMatrix4dToMat4D(cfg.getVopMatrix()),
                                                        MatrixConverter.convertMatrix4dToMat4D(cfg.getPopMatrix()),
                                                        MatrixConverter.convertMatrix4dToMat4D(cfg.getSopMatrix()),
                                                        cfg.getFilters());

                                                Platform.runLater(new Runnable() {

                                                    @Override
                                                    public void run() {

                                                        addFileToVoxelList(cfg.getOutputFile());
                                                    }
                                                });

                                                break;
                                        }

                                        break;

                                    case "multi-resolutions":

                                        final MultiResCfg multiResCfg = new MultiResCfg();
                                        multiResCfg.readConfiguration(file);

                                        ProcessingMultiRes process = new ProcessingMultiRes(multiResCfg.getMultiResPadMax(), multiResCfg.isMultiResUseDefaultMaxPad());

                                        process.process(multiResCfg.getFiles());
                                        process.write(multiResCfg.getOutputFile());

                                        break;

                                    case "multi-voxelisation":

                                        MultiVoxCfg multiVoxCfg = new MultiVoxCfg();
                                        multiVoxCfg.readConfiguration(file);
                                        voxTool.multiVoxelisation(multiVoxCfg);

                                        break;

                                }
                            }
                        };
                    }
                };

                break;
        }
    }*/

}
