/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.als.laz.LazExtraction;
import fr.amap.amapvox.commons.io.file.FileManager;
import fr.amap.amapvox.commons.math.matrix.Mat4D;
import fr.amap.amapvox.commons.math.vector.Vec2D;
import fr.amap.amapvox.commons.math.vector.Vec4D;
import fr.amap.amapvox.commons.util.BoundingBox3d;
import fr.amap.amapvox.commons.util.Cancellable;
import fr.amap.amapvox.commons.util.DataSet.Mode;
import static fr.amap.amapvox.commons.util.DataSet.Mode.SUM;
import fr.amap.amapvox.commons.util.Filter;
import fr.amap.amapvox.commons.util.MatrixAndFile;
import fr.amap.amapvox.commons.util.MatrixUtility;
import fr.amap.amapvox.commons.util.ProcessingListener;
import fr.amap.amapvox.commons.util.TimeCounter;
import fr.amap.amapvox.datastructure.octree.Octree;
import fr.amap.amapvox.datastructure.octree.OctreeFactory;
import fr.amap.amapvox.datastructure.voxel.VoxelSpaceHeader;
import fr.amap.amapvox.io.tls.rsp.RxpScan;
import fr.amap.amapvox.jraster.asc.DtmLoader;
import fr.amap.amapvox.jraster.asc.RegularDtm;
import fr.amap.amapvox.voxelisation.als.LasVoxelisation;
import fr.amap.amapvox.voxelisation.als.Trajectory;
import fr.amap.amapvox.voxelisation.configuration.ALSVoxCfg;
import fr.amap.amapvox.voxelisation.configuration.Input;
import fr.amap.amapvox.voxelisation.configuration.MultiVoxCfg;
import fr.amap.amapvox.voxelisation.configuration.TLSVoxCfg;
import fr.amap.amapvox.voxelisation.configuration.VoxMergingCfg;
import fr.amap.amapvox.voxelisation.configuration.VoxelParameters;
import fr.amap.amapvox.voxelisation.multires.ProcessingMultiRes;
import fr.amap.amapvox.voxelisation.tls.RxpVoxelisation;
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
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
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
    
    private final static Logger logger = Logger.getLogger(ProcessTool.class);
    
    private final EventListenerList listeners;
    private long startTime;
    private boolean cancelled;
    private ExecutorService exec;
    private int coresNumber;

    public ProcessTool() {
        listeners = new EventListenerList();
        cancelled = false;
    }

    public void addProcessToolListener(ProcessToolListener listener) {
        listeners.add(ProcessToolListener.class, listener);
    }

    public void fireProgress(String progress, int ratio) {
        for (ProcessToolListener processToolListener : listeners.getListeners(ProcessToolListener.class)) {
            processToolListener.processProgress(progress, ratio);
        }
    }

    public void fireFinished(float duration) {

        for (ProcessToolListener processToolListener : listeners.getListeners(ProcessToolListener.class)) {
            processToolListener.processFinished(duration);
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

        if (dtmFile != null) {

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

    public ArrayList<File> voxeliseFromRsp(TLSVoxCfg cfg){

        
        File output = cfg.getOutputFile();
        File input = cfg.getInputFile();
        VoxelParameters parameters = cfg.getVoxelParameters();
        Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
        List<MatrixAndFile> matricesAndFiles = cfg.getMatricesAndFiles();
        List<Filter> filters = cfg.getFilters();
        
        
        if (!Files.isReadable(output.toPath())) {
            logger.error("File " + output.getAbsolutePath() + " not reachable");
        }

        if (!Files.isReadable(input.toPath())) {
            logger.error("File " + input.getAbsolutePath() + " not reachable");
        }

        startTime = System.currentTimeMillis();

        parameters.setTLS(true);
        
        RegularDtm dtm = null;
        if (parameters.useDTMCorrection()) {
            dtm = loadDTM(parameters.getDtmFile());
        }
        
        List<fr.amap.amapvox.commons.util.PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();
        List<Octree> pointcloudList = null;
        
        if(pointcloudFilters != null){
            
            if(vop == null){ vop = Mat4D.identity();}
            
            pointcloudList = new ArrayList<>();
            
            if(parameters.isUsePointCloudFilter()){
                for(fr.amap.amapvox.commons.util.PointcloudFilter filter : pointcloudFilters){
                    pointcloudList.add(loadOctree(filter.getPointcloudFile(), vop));
                }
            }
        }
        
        ArrayList<File> files = new ArrayList<>();
        exec = Executors.newFixedThreadPool(coresNumber);
        
        
        try {
            LinkedBlockingQueue<Callable<RxpVoxelisation>>  tasks = new LinkedBlockingQueue<>();

            int count = 1;
            for (MatrixAndFile file : matricesAndFiles) {

                File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() + ".vox");
                tasks.put(new RxpVoxelisation(file.file, outputFile, vop, pop, MatrixUtility.convertMatrix4dToMat4D(file.matrix), parameters, dtm, pointcloudList, filters));
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

    public void voxeliseFromRxp(TLSVoxCfg cfg) {

        startTime = System.currentTimeMillis();
        
        File output = cfg.getOutputFile();
        File input = cfg.getInputFile();
        VoxelParameters parameters = cfg.getVoxelParameters();
        Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        Mat4D pop = MatrixUtility.convertMatrix4dToMat4D(cfg.getPopMatrix());
        Mat4D sop = MatrixUtility.convertMatrix4dToMat4D(cfg.getSopMatrix());
        List<Filter> filters = cfg.getFilters();        
        
        parameters.setTLS(true);

        RxpScan scan = new RxpScan();
        scan.setFile(input);

        fireProgress(output.getAbsolutePath(), 1);

        RegularDtm dtm = null;
        if (parameters.useDTMCorrection()) {
            dtm = loadDTM(parameters.getDtmFile());
        }
        
        List<fr.amap.amapvox.commons.util.PointcloudFilter> pointcloudFilters = parameters.getPointcloudFilters();
        List<Octree> pointcloudList = null;
        
        if(pointcloudFilters != null){
            
            if(vop == null){ vop = Mat4D.identity();}
            
            pointcloudList = new ArrayList<>();
            
            if(parameters.isUsePointCloudFilter()){
                for(fr.amap.amapvox.commons.util.PointcloudFilter filter : pointcloudFilters){
                    pointcloudList.add(loadOctree(filter.getPointcloudFile(), vop));
                }
            }
        }
        
        if(pop == null){ pop = Mat4D.identity();}
        if(sop == null){ sop = Mat4D.identity();}
        if(vop == null){ vop = Mat4D.identity();}

        RxpVoxelisation voxelisation = new RxpVoxelisation(input, output, vop, pop, sop, parameters, dtm, pointcloudList, filters);
        voxelisation.call();

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

    }

    public void voxeliseFromAls(ALSVoxCfg cfg) throws IOException, Exception {

        File output = cfg.getOutputFile();
        File input = cfg.getInputFile();
        File trajectoryFile = cfg.getTrajectoryFile();
        VoxelParameters parameters = cfg.getVoxelParameters();
        Mat4D vop = MatrixUtility.convertMatrix4dToMat4D(cfg.getVopMatrix());
        List<Filter> filters = cfg.getFilters();
        List<Integer> classifiedPointsToDiscard = cfg.getClassifiedPointsToDiscard();
                
        startTime = System.currentTimeMillis();

        parameters.setTLS(false);

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
                throw ex;
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
            throw ex;
        } catch (IOException ex) {
            throw ex;
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
    
    public void mergeVoxelFiles(VoxMergingCfg cfg) {
                
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
        
        //LeafAngleDistribution distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLANOPHILE);
        LeafAngleDistribution distribution = new LeafAngleDistribution(cfg.getVoxelParameters().getLadType());
        DirectionalTransmittance direcTransmittance = new DirectionalTransmittance(distribution);
        
        logger.info("Building transmittance functions table");
        direcTransmittance.buildTable(DirectionalTransmittance.DEFAULT_STEP_NUMBER);
        logger.info("Transmittance functions table is built");
        
        for (int i = 0; i < size; i++) {
            
            resultingFile[i][transmittanceColumnIndex] = (resultingFile[i][bvEnteringColumnIndex] - resultingFile[i][bvInterceptedColumnIndex])/
                                                        resultingFile[i][bvEnteringColumnIndex];

            resultingFile[i][transmittanceColumnIndex] = (float) Math.pow(resultingFile[i][transmittanceColumnIndex], 1 / resultingFile[i][lMeanTotalColumnIndex]);
            
            float pad1;

            if (resultingFile[i][nbSamplingColumnIndex] == 0) {

                pad1 = Float.NaN;
                resultingFile[i][transmittanceColumnIndex] = Float.NaN;

            } else if (resultingFile[i][bvInterceptedColumnIndex] > resultingFile[i][bvEnteringColumnIndex]) {

                logger.error("BFInterceptes > BFEntering, NaN assigné");

                pad1 = Float.NaN;
                resultingFile[i][transmittanceColumnIndex] = Float.NaN;

            } else {

                if (/*resultingFile[i][nbSamplingColumnIndex] > 1 && */resultingFile[i][transmittanceColumnIndex] == 0) {

                    pad1 = cfg.getVoxelParameters().getMaxPAD();

                }/* else if (resultingFile[i][nbSamplingColumnIndex] <= 2 && resultingFile[i][transmittanceColumnIndex] == 0 && Objects.equals(resultingFile[i][nbSamplingColumnIndex], resultingFile[i][nbEchosColumnIndex])) {

                    pad1 = Float.NaN;

                } */else {

                    double coefficientGTheta = direcTransmittance.getTransmittanceFromAngle(resultingFile[i][angleMeanColumnIndex], true);
                    
                    if(coefficientGTheta == 0){
                        logger.error("Voxel : " + resultingFile[i][0] + " " + resultingFile[i][1] + " " + resultingFile[i][2] + " -> coefficient GTheta nul, angle = "+resultingFile[i][angleMeanColumnIndex]);
                    }
                    
                    pad1 = (float) (Math.log(resultingFile[i][transmittanceColumnIndex]) / (-coefficientGTheta));

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
        Mat4D vopMatrix = MatrixUtility.convertMatrix4dToMat4D(configuration.getVopMatrix());
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

    public void setCoresNumber(int coresNumber) {
        this.coresNumber = coresNumber;
    }
}
