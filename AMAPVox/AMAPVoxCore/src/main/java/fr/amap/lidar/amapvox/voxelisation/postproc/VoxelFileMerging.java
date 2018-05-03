/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.postproc;

import fr.amap.commons.util.io.file.FileManager;
import fr.amap.commons.util.DataSet.Mode;
import static fr.amap.commons.util.DataSet.Mode.SUM;
import fr.amap.commons.util.TimeCounter;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.commons.util.Process;
import fr.amap.lidar.amapvox.commons.GTheta;
import fr.amap.lidar.amapvox.commons.LADParams;
import fr.amap.lidar.amapvox.commons.LeafAngleDistribution;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxMergingCfg;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.log4j.Logger;
import fr.amap.commons.util.Cancellable;
import fr.amap.lidar.amapvox.voxelisation.VoxelAnalysis;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class VoxelFileMerging extends Process implements Cancellable{
    
    private final static Logger LOGGER = Logger.getLogger(VoxelFileMerging.class);
    
    private long startTime;
    private boolean cancelled;

    public VoxelFileMerging() {
        cancelled = false;
    }
    
    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }
    
    
    public void mergeVoxelFiles(VoxMergingCfg cfg) throws Exception {
                
        cancelled = false;
        
        startTime = System.currentTimeMillis();
        Mode[] toMerge;
        int size;
        VoxelSpaceInfos infos = new VoxelSpaceInfos();

        float[][] nbSamplingMultiplyAngleMean;
        float[][] resultingFile;
        int columnNumber;
        
        int padBVTotalColumnIndex = -1;
        int angleMeanColumnIndex = -1;
        int bvEnteringColumnIndex = -1;
        int bvInterceptedColumnIndex = -1;
        int sumSurfMulLengthColumnIndex = -1;
        int sumSurfMulLengthMulEntColumnIndex = -1;
        int transmittance_tmpColumnIndex = -1;
        int lMeanTotalColumnIndex = -1;
        int lgTotalColumnIndex = -1;
        int nbEchosColumnIndex = -1;
        int nbSamplingColumnIndex = -1;
        int transmittanceColumnIndex = -1;
        
        
        if(cfg.getFiles().size() > 0){
            
            try {
                infos.readFromVoxelFile(cfg.getFiles().get(0));
                LADParams ladParams = new LADParams();
                ladParams.setLadType(infos.getLadType());
                
                if(infos.getLadParams() != null){
                    ladParams.setLadBetaFunctionAlphaParameter((float)infos.getLadParams()[0]);
                    ladParams.setLadBetaFunctionBetaParameter((float)infos.getLadParams()[1]);
                }
                
                cfg.getVoxelParameters().setLadParams(ladParams);
                cfg.getVoxelParameters().setTransmittanceMode(infos.getTransmittanceMode());
                cfg.getVoxelParameters().setPathLengthMode(infos.getPathLengthMode());
                
            } catch (Exception ex) {
                throw ex;
            }
            size = infos.getSplit().x * infos.getSplit().y * infos.getSplit().z;
            columnNumber = infos.getColumnNamesList().size();
            resultingFile = new float[size][columnNumber];
            toMerge = new Mode[columnNumber];
            
            for(int i=0;i<toMerge.length;i++){
                
                String columnName = infos.getColumnNamesList().get(i);
                                
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
                    case "sumSurfMulLength":
                        sumSurfMulLengthColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;
                    case "sumSurfMulLengthMulEnt":
                        sumSurfMulLengthMulEntColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;
                    case "transmittance_tmp":
                        transmittance_tmpColumnIndex = i;
                        toMerge[i] = Mode.SUM;
                        break;

                    default:
                        toMerge[i] = Mode.DISCARD;
                }
            }
            
            nbSamplingMultiplyAngleMean = new float[cfg.getFiles().size()][size];
            
            
            
        }else{
            LOGGER.info("No file to merge");
            return;
        }

        for (int i = 0; i < cfg.getFiles().size(); i++) {

            if (cancelled) {
                return;
            }

            String msg = "Merging in progress, file " + (i + 1) + " : " + cfg.getFiles().size();
            LOGGER.info(msg);
            fireProgress(msg, (i+1), cfg.getFiles().size());
            
            try (BufferedReader reader = new BufferedReader(new FileReader(cfg.getFiles().get(i)))){
                
                
                int count = 0;
                FileManager.skipLines(reader, 6);
                
                String currentFileLine;
                while((currentFileLine = reader.readLine()) != null){
                    
                    String[] lineSplittedFile = currentFileLine.split(" ");
                    
                    if(lineSplittedFile.length != columnNumber){
                        LOGGER.error("Columns number doesn't match!");
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
                LOGGER.error(ex);
            } catch (IOException ex) {
                LOGGER.error(ex);
            }
        }

        LOGGER.info("Compute angleMean");
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
            LOGGER.error("nbSampling or angleMean columns are missing, cannot re-compute angleMean");
        }
        
        LOGGER.info("Compute lMeanTotal");
        for (int i = 0; i < size; i++) {
            
            resultingFile[i][lMeanTotalColumnIndex] = resultingFile[i][lgTotalColumnIndex] / resultingFile[i][nbSamplingColumnIndex];
        }
        
        LOGGER.info("Compute transmittance and PAD");
        
        //LeafAngleDistribution distribution = new LeafAngleDistribution(LeafAngleDistribution.Type.PLANOPHILE);
        LADParams ladParameters = cfg.getVoxelParameters().getLadParams();
        if(ladParameters == null){
            ladParameters = new LADParams();
        }
        LeafAngleDistribution distribution = new LeafAngleDistribution(ladParameters.getLadType(), 
                ladParameters.getLadBetaFunctionAlphaParameter(),
                ladParameters.getLadBetaFunctionBetaParameter());
        
        GTheta direcTransmittance = new GTheta(distribution);
        
        LOGGER.info("Building transmittance functions table");
        direcTransmittance.buildTable(GTheta.DEFAULT_STEP_NUMBER);
        LOGGER.info("Transmittance functions table is built");
        
        int transMode = cfg.getVoxelParameters().getTransmittanceMode();
        
        for (int i = 0; i < size; i++) {
            
            switch (transMode) {
                
                case 2:
                    resultingFile[i][transmittanceColumnIndex] =  VoxelAnalysis.computeNormTransmittanceMode2(resultingFile[i][transmittance_tmpColumnIndex], resultingFile[i][sumSurfMulLengthColumnIndex], resultingFile[i][lMeanTotalColumnIndex]);
                    break;
                case 3:
                    resultingFile[i][transmittanceColumnIndex] =  VoxelAnalysis.computeNormTransmittanceMode3(resultingFile[i][transmittance_tmpColumnIndex], resultingFile[i][sumSurfMulLengthMulEntColumnIndex]);
                    break;
                case 1:
                default:
                    resultingFile[i][transmittanceColumnIndex] = VoxelAnalysis.computeTransmittance(resultingFile[i][bvEnteringColumnIndex], resultingFile[i][bvInterceptedColumnIndex]);
                    resultingFile[i][transmittanceColumnIndex] =  VoxelAnalysis.computeNormTransmittance(resultingFile[i][transmittanceColumnIndex], resultingFile[i][lMeanTotalColumnIndex]);
                    break;
            }
            
            resultingFile[i][padBVTotalColumnIndex] = VoxelAnalysis.computePADFromNormTransmittance(resultingFile[i][transmittanceColumnIndex], resultingFile[i][angleMeanColumnIndex], cfg.getVoxelParameters().infos.getMaxPAD(), direcTransmittance);
        }
        
        LOGGER.info("writing output file: " + cfg.getOutputFile().getAbsolutePath());
        long start_time = System.currentTimeMillis();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(cfg.getOutputFile()))) {

            writer.write("VOXEL SPACE" + "\n");
            writer.write("#min_corner: " + (float) infos.getMinCorner().x + " " + (float) infos.getMinCorner().y + " " + (float) infos.getMinCorner().z + "\n");
            writer.write("#max_corner: " + (float) infos.getMaxCorner().x + " " + (float) infos.getMaxCorner().y + " " + (float) infos.getMaxCorner().z + "\n");
            writer.write("#split: " + infos.getSplit().x + " " + infos.getSplit().y + " " + infos.getSplit().z + "\n");

            writer.write("#type: TLS" + " #res: "+infos.getResolution()+" "+"#MAX_PAD: "+cfg.getVoxelParameters().infos.getMaxPAD()+"\n");

            String header = "";
            
            for (String columnName : infos.getColumnNamesList()) {
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
            
            LOGGER.info("file written ( " + TimeCounter.getElapsedStringTimeInSeconds(start_time) + " )");

        } catch (IOException ex) {
            LOGGER.error(ex);
        }
        
        

        fireFinished(TimeCounter.getElapsedTimeInSeconds(startTime));
    }

}
