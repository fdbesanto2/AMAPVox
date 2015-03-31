/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.voxelisation;

import fr.ird.voxelidar.MatrixAndFile;
import fr.ird.voxelidar.util.ProcessingListener;
import fr.ird.voxelidar.voxelisation.als.LasVoxelisation;
import fr.ird.voxelidar.voxelisation.tls.RxpVoxelisation;
import fr.ird.voxelidar.lidar.format.tls.RxpScan;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.math.vector.Vec2D;
import fr.ird.voxelidar.engine3d.object.mesh.Attribut;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.engine3d.object.scene.DtmLoader;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.lidar.format.tls.Rsp;
import fr.ird.voxelidar.lidar.format.tls.Scans;
import fr.ird.voxelidar.util.DataSet;
import fr.ird.voxelidar.util.DataSet.Mode;
import fr.ird.voxelidar.util.MatrixConverter;
import fr.ird.voxelidar.util.TimeCounter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import javax.vecmath.Matrix4d;
import javax.vecmath.Vector3d;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class VoxelisationTool {

    final static Logger logger = Logger.getLogger(VoxelisationTool.class);

    private VoxelParameters parameters;
    private final EventListenerList listeners;
    private long startTime;
    private Dtm dtm;
    
    public VoxelisationTool() {
        listeners = new EventListenerList();
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

    private Dtm loadDTM(File dtmFile) {

        Dtm terrain = null;

        if (dtmFile != null && parameters.useDTMCorrection()) {

            try {
                terrain = DtmLoader.readFromAscFile(dtmFile, null);

            } catch (Exception ex) {
                logger.error(ex);
            }
        }

        return terrain;
    }

    public ArrayList<File> generateVoxelsFromRsp(File output, File input, VoxelParameters parameters, Mat4D vop, Mat4D pop, List<MatrixAndFile> matricesAndFiles) {

        startTime = System.currentTimeMillis();

        this.parameters = parameters;
        this.parameters.setTLS(true);

        Rsp rsp = new Rsp();
        rsp.read(input);
        
        ArrayList<File> files = new ArrayList<>();

        ExecutorService exec = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ArrayList<Callable<RxpVoxelisation>> tasks = new ArrayList<>();

        dtm = loadDTM(parameters.getDtmFile());

        int count = 1;
        for (MatrixAndFile file : matricesAndFiles) {

            File outputFile = new File(output.getAbsolutePath() + "/" + file.file.getName() + ".vox");
            tasks.add(new RxpVoxelisation(file.file, outputFile, vop, pop, MatrixConverter.convertMatrix4dToMat4D(file.matrix), this.parameters, dtm));
            files.add(outputFile);
            count++;
        }

        try {
            List<Future> results = (List) exec.invokeAll(tasks);
            exec.shutdown();
            for (Future f : results) {
                f.get();
            }
        } catch (InterruptedException | RejectedExecutionException | NullPointerException | ExecutionException ex) {
            logger.error(ex.getMessage());
        }

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

        return files;
    }

    public void generateVoxelsFromRxp(File output, File input, File dtmFile, VoxelParameters parameters, Mat4D vop, Mat4D pop, Mat4D sop) {

        startTime = System.currentTimeMillis();

        this.parameters = parameters;
        this.parameters.setTLS(true);

        RxpScan scan = new RxpScan();
        scan.setFile(input);

        File outputFile = new File(output.getAbsolutePath() + "/" + scan.getFile().getName() + ".vox");

        fireProgress(outputFile.getAbsolutePath(), 1);
        
        if(dtm == null){
            dtm = loadDTM(dtmFile);
        }
        

        RxpVoxelisation voxelisation = new RxpVoxelisation(input, outputFile, vop, pop, sop, parameters, dtm);
        voxelisation.call();

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));

    }

    public void generateVoxelsFromLas(File output, File input, File trajectoryFile, VoxelParameters parameters, Mat4D vop) {

        startTime = System.currentTimeMillis();

        this.parameters = parameters;
        this.parameters.setTLS(false);

        LasVoxelisation voxelisation = new LasVoxelisation(input, output, vop, trajectoryFile, parameters);

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

    public void mergeVoxelsFile(List<File> filesList, File output) throws NullPointerException {

        startTime = System.currentTimeMillis();
        Mode[] toMerge = null;
        Map<String, Float[]> map1, map2, result = null;
        int size = 0;
        double bottomCornerX = 0, bottomCornerY = 0, bottomCornerZ = 0;
        double topCornerX = 0, topCornerY = 0, topCornerZ = 0;
        int splitX = 0, splitY = 0, splitZ = 0;
        
        float[][] nbSamplingMultiplyAngleMean = null;

        for (int i = 0; i < filesList.size(); i++) {

            logger.info("Merging in progress, file " + (i + 1) + " : " + filesList.size());

            VoxelSpace voxelSpace1 = new VoxelSpace(filesList.get(i));
            voxelSpace1.load();
            size = voxelSpace1.data.split.x * voxelSpace1.data.split.y * voxelSpace1.data.split.z;
            map1 = voxelSpace1.data.getVoxelMap();
            
            

            bottomCornerX = voxelSpace1.data.bottomCorner.x;
            bottomCornerY = voxelSpace1.data.bottomCorner.y;
            bottomCornerZ = voxelSpace1.data.bottomCorner.z;

            topCornerX = voxelSpace1.data.topCorner.x;
            topCornerY = voxelSpace1.data.topCorner.y;
            topCornerZ = voxelSpace1.data.topCorner.z;

            splitX = voxelSpace1.data.split.x;
            splitY = voxelSpace1.data.split.y;
            splitZ = voxelSpace1.data.split.z;

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
                        //case "PadBVOutgoing":
                        //case "PadBVNoInterceptions":
                        case "PadBVTotal":
                        case "angleMean":
                        //case "lMeanOutgoing":
                        case "lMeanTotal":
                        case "transmittance":
                        //case "LMean_NoInterception":

                            m = Mode.DISCARD;
                            break;

                        case "nbSampling":
                        //case "nbOutgoing":
                        case "nbEchos":
                        //case "lgNoInterception":
                        //case "lgOutgoing":
                        case "lgTotal":
                        //case "bvOutgoing":
                        case "bvEntering":
                        case "bvIntercepted":
                        case "bflEntering":
                        case "bflIntercepted":

                            m = Mode.SUM;
                            break;
                        
                        case "bfEntering":
                        case "bfIntercepted":
                        case "bsEntering":
                        case "bsIntercepted":

                            m = Mode.REMOVE;
                            break;

                        default:
                            m = Mode.DISCARD;
                    }

                    toMerge[count] = m;

                    count++;
                }

                VoxelSpace voxelSpace2 = new VoxelSpace(filesList.get(i));
                voxelSpace2.load();
                map2 = voxelSpace2.data.getVoxelMap();
                
                nbSamplingMultiplyAngleMean = new float[filesList.size()][size];

            } else {
                map2 = result;
            }
            
            Float[] nbTemp1 = map1.get("nbSampling");
            Float[] nbTemp2 = map1.get("angleMean");
            
            for(int j=0;j<nbTemp1.length;j++){
                nbSamplingMultiplyAngleMean[i][j] = nbTemp1[j] * nbTemp2[j];
            }
            
            result = DataSet.mergeTwoDataSet(map1, map2, toMerge);

        }
        
        
        logger.info("Recalculate PadBVOutgoing, angleMean, lMeanOutgoing, LMean_NoInterception");
        /*recalculate PadBF, angleMean, LMean_Exiting, LMean_NoInterception*/
        if (result != null) {
            
            //remove unused variables
            //result.remove("bfEntering");
            //result.remove("bfIntercepted");
            //result.remove("bsEntering");
            //result.remove("bsIntercepted");
            //result.remove("transmittance");
            
            
            /*recalculate lMeanOutgoing, LMean_NoInterception*/
            Float[] nbSamplingArray = result.get("nbSampling");
            Float[] nbEchosArray = result.get("nbEchos");
            //Float[] lgOutgoingArray = result.get("lgOutgoing");
            Float[] lgTotalArray = result.get("lgTotal");
            //Float[] nbOutgoingArray = result.get("nbOutgoing");
            //Float[] LMean_OutgoingArray = result.get("lMeanOutgoing");
            Float[] lMeanTotalArray = result.get("lMeanTotal");
            //Float[] lMeanNoInterceptionArray = result.get("LMean_NoInterception");
            //Float[] Lg_NoInterceptionArray  = result.get("lgNoInterception");
            
            for (int i = 0; i < lMeanTotalArray.length; i++) {
                
                //lMeanNoInterceptionArray[i] = Lg_NoInterceptionArray[i] / (nbSamplingArray[i]-nbEchosArray[i]);
                //LMean_OutgoingArray[i] = lgOutgoingArray[i] / nbOutgoingArray[i];
                lMeanTotalArray[i] = lgTotalArray[i] / nbSamplingArray[i];
                
            }
            
            /*recalculate Pad*/
            //Float[] padBVOutgoingArray = result.get("PadBVOutgoing");
            //Float[] PadBVNoInterceptionsArray = result.get("PadBVNoInterceptions");
            Float[] PadBVTotalArray = result.get("PadBVTotal");
            Float[] transmittanceArray = result.get("transmittance");
            
            if(PadBVTotalArray == null){
                
                Float[] PadBflTotalArray = result.get("PadBflTotal");
                Float[] bflEnteringArray = result.get("bflEntering");
                Float[] bflInterceptedArray = result.get("bflIntercepted");
                
                if (nbSamplingArray != null && nbEchosArray != null) {

                    for (int i = 0; i < PadBflTotalArray.length; i++) {
                        transmittanceArray[i] = (bflEnteringArray[i] - bflInterceptedArray[i]) / bflEnteringArray[i];
                        float pad3;

                        if (bflEnteringArray[i] <= 0) {

                            pad3 = Float.NaN;

                        } else if (bflInterceptedArray[i] > bflEnteringArray[i]) {

                            logger.error("BFInterceptes > BFEntering, NaN assigné");
                            pad3 = Float.NaN;

                        } else {

                            if (nbSamplingArray[i] > 1 && transmittanceArray[i] == 0 && Objects.equals(nbSamplingArray[i], nbEchosArray[i])) {

                                pad3 = 3;

                            } else if (nbSamplingArray[i] <= 2 && transmittanceArray[i] == 0 && Objects.equals(nbSamplingArray[i], nbEchosArray[i])) {

                                pad3 = Float.NaN;

                            } else {

                                //pad1 = (float) (Math.log(transmittance) / (-0.5 * LMean_OutgoingArray[i]));
                                //pad2 = (float) (Math.log(transmittance) / (-0.5 * lMeanNoInterceptionArray[i]));
                                pad3 = (float) (Math.log(transmittanceArray[i]) / (-0.5 * lMeanTotalArray[i]));

                                if (Float.isNaN(pad3)) {
                                    pad3 = Float.NaN;
                                } else if (pad3 > 3 || Float.isInfinite(pad3)) {
                                    pad3 = 3;
                                }
                            }
                        }


                        //padBVOutgoingArray[i] = pad1;
                        //PadBVNoInterceptionsArray[i] = pad2;
                        PadBflTotalArray[i] = pad3+0.0f;
                    }
                }
                
            }else{
                
                Float[] bvEnteringArray = result.get("bvEntering");
                Float[] bvInterceptedArray = result.get("bvIntercepted");
            
                if (nbSamplingArray != null && nbEchosArray != null) {

                    for (int i = 0; i < PadBVTotalArray.length; i++) {
                        transmittanceArray[i] = (bvEnteringArray[i] - bvInterceptedArray[i]) / bvEnteringArray[i];
                        float pad3;

                        if (bvEnteringArray[i] <= 0) {

                            pad3 = Float.NaN;

                        } else if (bvInterceptedArray[i] > bvEnteringArray[i]) {

                            logger.error("BFInterceptes > BFEntering, NaN assigné");
                            pad3 = Float.NaN;

                        } else {

                            if (nbSamplingArray[i] > 1 && transmittanceArray[i] == 0 && Objects.equals(nbSamplingArray[i], nbEchosArray[i])) {

                                pad3 = 3;

                            } else if (nbSamplingArray[i] <= 2 && transmittanceArray[i] == 0 && Objects.equals(nbSamplingArray[i], nbEchosArray[i])) {

                                pad3 = Float.NaN;

                            } else {

                                //pad1 = (float) (Math.log(transmittance) / (-0.5 * LMean_OutgoingArray[i]));
                                //pad2 = (float) (Math.log(transmittance) / (-0.5 * lMeanNoInterceptionArray[i]));
                                pad3 = (float) (Math.log(transmittanceArray[i]) / (-0.5 * lMeanTotalArray[i]));

                                if (Float.isNaN(pad3)) {
                                    pad3 = Float.NaN;
                                } else if (pad3 > 3 || Float.isInfinite(pad3)) {
                                    pad3 = 3;
                                }
                            }
                        }


                        //padBVOutgoingArray[i] = pad1;
                        //PadBVNoInterceptionsArray[i] = pad2;
                        PadBVTotalArray[i] = pad3 + 0.0f;
                    }
                }
            }
            
            
            
            /*recalculate angleMean*/
            Float[] angleMeanArray = result.get("angleMean");
            
            for(int i=0;i<size;i++){
                
                float sum = 0;
                for(int j=0;j<filesList.size();j++){
                    
                    if(!Float.isNaN(nbSamplingMultiplyAngleMean[j][i])){
                        sum += nbSamplingMultiplyAngleMean[j][i];
                    }
                    
                }
                
                angleMeanArray[i] = sum/nbSamplingArray[i];
            }
            
            logger.info("writing output file: "+output.getAbsolutePath());
            
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(output))) {

                writer.write("VOXEL SPACE" + "\n");
                writer.write("#min_corner: " + (float) bottomCornerX + " " + (float) bottomCornerY + " " + (float) bottomCornerZ + "\n");
                writer.write("#max_corner: " + (float) topCornerX + " " + (float) topCornerY + " " + (float) topCornerZ + "\n");
                writer.write("#split: " + splitX + " " + splitY + " " + splitZ + "\n");

                writer.write("#offset: " + (float) bottomCornerX + " " + (float) bottomCornerY + " " + (float) bottomCornerZ + "\n");

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
