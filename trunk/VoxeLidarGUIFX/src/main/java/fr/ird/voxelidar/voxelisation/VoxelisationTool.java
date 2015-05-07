/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import fr.ird.voxelidar.MatrixAndFile;
import fr.ird.voxelidar.util.ProcessingListener;
import fr.ird.voxelidar.voxelisation.tls.RxpVoxelisation;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec2D;
import fr.ird.voxelidar.engine3d.object.mesh.Attribut;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.engine3d.object.scene.DtmLoader;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceHeader;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.util.DataSet;
import fr.ird.voxelidar.util.DataSet.Mode;
import fr.ird.voxelidar.util.Filter;
import fr.ird.voxelidar.util.MatrixConverter;
import fr.ird.voxelidar.util.TimeCounter;
import fr.ird.voxelidar.voxelisation.als.LasVoxelisation;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class VoxelisationTool {
    
    
    final static Logger logger = Logger.getLogger(VoxelisationTool.class);

    private VoxelParameters parameters;
    private final EventListenerList listeners;
    private long startTime;
    private Dtm dtm;
    private boolean cancelled;
    private ExecutorService exec;

    public VoxelisationTool() {
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

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        
        if(exec != null){
            exec.shutdownNow();
        }
    }

    private Dtm loadDTM(File dtmFile) {

        Dtm terrain = null;

        if (dtmFile != null && parameters.useDTMCorrection()) {

            try {
                terrain = DtmLoader.readFromAscFile(dtmFile);
            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return terrain;
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
        
        ArrayList<File> files = new ArrayList<>();
        exec = Executors.newFixedThreadPool(coresNumber);

        try {
            LinkedBlockingQueue<Callable<RxpVoxelisation>>  tasks = new LinkedBlockingQueue<>(matricesAndFiles.size());

            int count = 1;
            for (MatrixAndFile file : matricesAndFiles) {

                File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() + ".vox");
                tasks.add(new RxpVoxelisation(file.file, outputFile, vop, pop, MatrixConverter.convertMatrix4dToMat4D(file.matrix), this.parameters, dtm, filters));
                files.add(outputFile);
                count++;
            }
            exec.invokeAll(tasks);
            
            /*
            for (Future f : results) {
                f.get();
            }
            */
        } catch (InterruptedException | RejectedExecutionException | NullPointerException ex) {
            logger.error(ex.getMessage());
        }finally{
            exec.shutdown();
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

        RxpVoxelisation voxelisation = new RxpVoxelisation(input, output, vop, pop, sop, parameters, dtm, filters);
        voxelisation.call();

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

    }

    public void voxeliseFromAls(File output, File input, File trajectoryFile, VoxelParameters parameters, Mat4D vop, List<Filter> filters, boolean filterLowPoints) {

        startTime = System.currentTimeMillis();

        this.parameters = parameters;
        this.parameters.setTLS(false);

        if (vop == null) {
            vop = Mat4D.identity();
        }

        LasVoxelisation voxelisation = new LasVoxelisation(input, output, vop, trajectoryFile, parameters, filters, filterLowPoints);

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
    
    
    public void mergeVoxelsFileV2(List<File> filesList, File output, int transmittanceMode, float maxPAD) {
        
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
        
        if(filesList.size() > 0){
            
            voxelSpaceHeader = VoxelSpaceHeader.readVoxelFileHeader(filesList.get(0));
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
            
            nbSamplingMultiplyAngleMean = new float[filesList.size()][size];
            
            
            
        }else{
            logger.info("No file to merge");
            return;
        }

        for (int i = 0; i < filesList.size(); i++) {

            if (cancelled) {
                return;
            }

            String msg = "Merging in progress, file " + (i + 1) + " : " + filesList.size();
            logger.info(msg);
            fireProgress(msg, i);
            
            try (BufferedReader reader = new BufferedReader(new FileReader(filesList.get(i)))){
                
                
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
                
                for (int j = 0; j < filesList.size(); j++) {
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

                    pad1 = maxPAD;

                } else if (resultingFile[i][nbSamplingColumnIndex] <= 2 && resultingFile[i][transmittanceColumnIndex] == 0 && Objects.equals(resultingFile[i][nbSamplingColumnIndex], resultingFile[i][nbEchosColumnIndex])) {

                    pad1 = Float.NaN;

                } else {

                    pad1 = (float) (Math.log(resultingFile[i][transmittanceColumnIndex]) / (-0.5 * resultingFile[i][lMeanTotalColumnIndex]));

                    if (Float.isNaN(pad1)) {
                        pad1 = Float.NaN;
                    } else if (pad1 > maxPAD || Float.isInfinite(pad1)) {
                        pad1 = maxPAD;
                    }
                }
            }

            resultingFile[i][padBVTotalColumnIndex] = pad1 + 0.0f;
        }
        
        logger.info("writing output file: " + output.getAbsolutePath());
        long start_time = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + (float) voxelSpaceHeader.bottomCorner.x + " " + (float) voxelSpaceHeader.bottomCorner.y + " " + (float) voxelSpaceHeader.bottomCorner.z + "\n");
            writer.write("#max_corner: " + (float) voxelSpaceHeader.topCorner.x + " " + (float) voxelSpaceHeader.topCorner.y + " " + (float) voxelSpaceHeader.topCorner.z + "\n");
            writer.write("#split: " + voxelSpaceHeader.split.x + " " + voxelSpaceHeader.split.y + " " + voxelSpaceHeader.split.z + "\n");

            writer.write("#type: TLS" + " #res: "+voxelSpaceHeader.res+" "+"#MAX_PAD: "+maxPAD+"\n");

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

    public void mergeVoxelsFile(List<File> filesList, File output, int transmittanceMode, float maxPAD) {

        startTime = System.currentTimeMillis();
        Mode[] toMerge = null;
        Map<String, Float[]> map1, map2 = null, result = null;
        int size = 0;
        double bottomCornerX = 0, bottomCornerY = 0, bottomCornerZ = 0;
        double topCornerX = 0, topCornerY = 0, topCornerZ = 0;
        int splitX = 0, splitY = 0, splitZ = 0;
        float resolution = 0;

        float[][] nbSamplingMultiplyAngleMean = null;
        //float[] sumTransmittanceMultiplyLgTotal = null;

        for (int i = 0; i < filesList.size(); i++) {

            if (cancelled) {
                return;
            }

            String msg = "Merging in progress, file " + (i + 1) + " : " + filesList.size();
            logger.info(msg);
            fireProgress(msg, i);

            VoxelSpace voxelSpace1 = new VoxelSpace(filesList.get(i));
            voxelSpace1.load();

            size = voxelSpace1.data.header.split.x * voxelSpace1.data.header.split.y * voxelSpace1.data.header.split.z;
            map1 = voxelSpace1.data.getVoxelMap();

            bottomCornerX = voxelSpace1.data.header.bottomCorner.x;
            bottomCornerY = voxelSpace1.data.header.bottomCorner.y;
            bottomCornerZ = voxelSpace1.data.header.bottomCorner.z;

            topCornerX = voxelSpace1.data.header.topCorner.x;
            topCornerY = voxelSpace1.data.header.topCorner.y;
            topCornerZ = voxelSpace1.data.header.topCorner.z;

            splitX = voxelSpace1.data.header.split.x;
            splitY = voxelSpace1.data.header.split.y;
            splitZ = voxelSpace1.data.header.split.z;
            
            resolution = voxelSpace1.data.header.res;
            

            if (i == 0) {
                Map<String, Attribut> mapAttributs = voxelSpace1.getMapAttributs();
                toMerge = new Mode[mapAttributs.size()];

                int count = 0;
                for (Entry entry : mapAttributs.entrySet()) {

                    String key = entry.getKey().toString();
                    Mode m;

                    switch (key) {

                        case "i":
                        case "j":
                        case "k":
                        case "ground_distance":

                        //discard but recalculate after
                        case "PadBVTotal":
                        case "PadBVTotal_V2":
                        case "angleMean":
                        case "lMeanTotal":
                        case "transmittance":
                        case "transmittance_v2":

                            m = Mode.DISCARD;
                            break;

                        case "nbSampling":
                        case "nbEchos":
                        case "lgTotal":
                        case "bvEntering":
                        case "bvIntercepted":

                            m = Mode.SUM;
                            break;

                        default:
                            m = Mode.DISCARD;
                    }

                    toMerge[count] = m;

                    count++;
                }
                
                nbSamplingMultiplyAngleMean = new float[filesList.size()][size];
                //sumTransmittanceMultiplyLgTotal = new float[size];

                result = map1;
                map2 = result;

            } else {

                result = DataSet.mergeTwoDataSet(map1, map2, toMerge);
                map2 = result;
            }
            
            Float[] nbTemp1 = map1.get("nbSampling");
            Float[] nbTemp2 = map1.get("angleMean");
            //Float[] nbTemp3 = map1.get("transmittance_v2");
            //Float[] nbTemp4 = map1.get("lgTotal");
            
            for(int j=0;j<nbTemp1.length;j++){
                nbSamplingMultiplyAngleMean[i][j] = nbTemp1[j] * nbTemp2[j];
                /*
                if(!Float.isNaN(nbTemp3[j]) && !Float.isNaN(nbTemp4[j])){
                    sumTransmittanceMultiplyLgTotal[j] += (nbTemp3[j] * nbTemp4[j]);
                }
                */
            }
            
        }

        logger.info("Recalculate lMeanTotal, angleMean, transmittance, PadBVTotal");
        /*recalculate PadBF, angleMean, LMean_Exiting, LMean_NoInterception*/
        if (result != null) {
            
            try{
                result.remove("PadBVTotal_V2");
                result.remove("transmittance_v2");
            }catch(Exception e){}
            

            /*recalculate lMeanOutgoing, LMean_NoInterception*/
            Float[] nbSamplingArray = result.get("nbSampling");
            Float[] nbEchosArray = result.get("nbEchos");
            Float[] lgTotalArray = result.get("lgTotal");
            Float[] lMeanTotalArray = result.get("lMeanTotal");

            for (int i = 0; i < lMeanTotalArray.length; i++) {

                lMeanTotalArray[i] = lgTotalArray[i] / nbSamplingArray[i];
            }

            /*recalculate Pad*/
            Float[] transmittanceArray = result.get("transmittance");
            //Float[] transmittance2Array = result.get("transmittance_v2");
            Float[] PadBVTotalArray = result.get("PadBVTotal");
            //Float[] PadBVTotal2Array = result.get("PadBVTotal_V2");
            Float[] bVEnteringArray = result.get("bvEntering");
            Float[] bVInterceptedArray = result.get("bvIntercepted");

            if (transmittanceArray == null /*|| transmittance2Array == null*/ || PadBVTotalArray == null
                    /*|| PadBVTotal2Array == null*/ || bVEnteringArray == null || bVInterceptedArray == null
                    || nbSamplingArray == null || nbEchosArray == null) {

                logger.error("Arguments are missing");
                return;
            }

            for (int i = 0; i < PadBVTotalArray.length; i++) {

                transmittanceArray[i] = (bVEnteringArray[i] - bVInterceptedArray[i]) / bVEnteringArray[i];
                //transmittance2Array[i] = sumTransmittanceMultiplyLgTotal[i]/lgTotalArray[i];
                
                float pad1/*, pad2*/;

                if (bVEnteringArray[i] <= 0) {

                    pad1 = Float.NaN;
                    //pad2 = pad1;
                    
                    transmittanceArray[i] = Float.NaN;
                    //transmittance2Array[i] = Float.NaN;

                } else if (bVInterceptedArray[i] > bVEnteringArray[i]) {

                    logger.error("BFInterceptes > BFEntering, NaN assigné");
                    
                    pad1 = Float.NaN;
                    //pad2 = pad1;
                    
                    transmittanceArray[i] = Float.NaN;
                    //transmittance2Array[i] = Float.NaN;

                } else {

                    if (nbSamplingArray[i] > 1 && transmittanceArray[i] == 0 && Objects.equals(nbSamplingArray[i], nbEchosArray[i])) {

                        pad1 = maxPAD;
                        //pad2 = pad1;

                    } else if (nbSamplingArray[i] <= 2 && transmittanceArray[i] == 0 && Objects.equals(nbSamplingArray[i], nbEchosArray[i])) {

                        pad1 = Float.NaN;
                        //pad2 = pad1;

                    } else {

                        pad1 = (float) (Math.log(transmittanceArray[i]) / (-0.5 * lMeanTotalArray[i]));
                        //pad2 = (float) (Math.log(transmittance2Array[i]) / (-0.5 * lMeanTotalArray[i]));

                        if (Float.isNaN(pad1)) {
                            pad1 = Float.NaN;
                        } else if (pad1 > maxPAD || Float.isInfinite(pad1)) {
                            pad1 = maxPAD;
                        }
                        /*
                        if (Float.isNaN(pad2)) {
                            pad2 = Float.NaN;
                        } else if (pad2 > maxPAD || Float.isInfinite(pad2)) {
                            pad2 = maxPAD;
                        }*/
                    }
                }

                PadBVTotalArray[i] = pad1 + 0.0f;
                //PadBVTotal2Array[i] = pad2 + 0.0f;
            }

            /*recalculate angleMean*/
            Float[] angleMeanArray = result.get("angleMean");

            if (angleMeanArray == null) {
                logger.error("Argument angleMean is missing");
                return;
            }

            for (int i = 0; i < size; i++) {

                float sum = 0;
                for (int j = 0; j < filesList.size(); j++) {

                    if (!Float.isNaN(nbSamplingMultiplyAngleMean[j][i])) {
                        sum += nbSamplingMultiplyAngleMean[j][i];
                    }

                }

                angleMeanArray[i] = sum / nbSamplingArray[i];
            }

            logger.info("writing output file: " + output.getAbsolutePath());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {

                writer.write("VOXEL SPACE" + "\n");
                writer.write("#min_corner: " + (float) bottomCornerX + " " + (float) bottomCornerY + " " + (float) bottomCornerZ + "\n");
                writer.write("#max_corner: " + (float) topCornerX + " " + (float) topCornerY + " " + (float) topCornerZ + "\n");
                writer.write("#split: " + splitX + " " + splitY + " " + splitZ + "\n");

                writer.write("#type: TLS" + " #res: "+resolution+" "+"#MAX_PAD: "+maxPAD+"\n");

                String header = "";

                for (Entry entry : result.entrySet()) {
                    String columnName = (String) entry.getKey();
                    
                    header += columnName + " ";
                }
                header = header.trim();
                writer.write(header + "\n");

                for (int i = 0; i < size; i++) {

                    String voxel = "";

                    int count = 0;
                    for (Entry entry : result.entrySet()) {

                        Float[] values = (Float[]) entry.getValue();

                        if (count < 3) {

                            voxel += values[i].intValue() + " ";
                        } else {
                            voxel += values[i] + " ";
                        }

                        count++;
                    }

                    voxel = voxel.trim();

                    writer.write(voxel + "\n");

                }

            } catch (IOException ex) {
                logger.error(ex);
            }
            
            
        }

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
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

}
