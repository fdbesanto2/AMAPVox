/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.postproc;

import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.raster.multiband.BCommon;
import fr.amap.commons.raster.multiband.BHeader;
import fr.amap.commons.raster.multiband.BSQ;
import fr.amap.commons.util.Statistic;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

/**
 * Stores voxel space PAD values as a multi-band raster, which each z layer (relative to a DTM or not) is a raster band.
 * @author Julien Heurtebize
 */
public class MultiBandRaster {
    
    private final static Logger LOGGER = Logger.getLogger(MultiBandRaster.class);
    
    public static BSQ computeRaster(float startingHeight, float step, int bandNumber, int resolution, VoxelSpaceInfos infos, Voxel[][][] voxels, Raster dtm){
        
        float[] altitudes = new float[bandNumber];
        for (int i = 0; i < bandNumber; i++) {
            altitudes[i] = startingHeight + (i * step);
        }

        float scale = (float) (resolution / infos.getResolution());

        int rasterXSize = (int) (Math.ceil(infos.getSplit().x / scale));
        int rasterYSize = (int) (Math.ceil(infos.getSplit().y / scale));

        BHeader header = new BHeader(rasterXSize, rasterYSize, altitudes.length, BCommon.NumberOfBits.N_BITS_32);
        header.setUlxmap(infos.getMinCorner().x + (resolution / 2.0f));
        header.setUlymap(infos.getMinCorner().y - (infos.getResolution() / 2.0f) + (infos.getSplit().y * infos.getResolution()));
        header.setXdim(resolution);
        header.setYdim(resolution);

        BSQ raster = new BSQ(null, header);

        Statistic[][][] padMean = new Statistic[rasterXSize][rasterYSize][altitudes.length];

        if (dtm != null) {

            if (altitudes.length > 0) {

                float altitudeMin = altitudes[0];

                for (int i = 0; i < infos.getSplit().x; i++) {
                    for (int j = infos.getSplit().y - 1; j >= 0; j--) {
                        for (int k = 0; k < infos.getSplit().z; k++) {

                            Voxel vox = voxels[i][j][k];

                            //on calcule l'indice de la couche auquel appartient le voxel
                            if (vox != null && vox.ground_distance > altitudeMin) {
                                int layer = (int) ((vox.ground_distance - altitudeMin) / step);

                                if (layer < altitudes.length) {

                                    int indiceI = (int) (i / scale);
                                    int indiceJ = (int) (j / scale);

                                    if (padMean[indiceI][indiceJ][layer] == null) {
                                        padMean[indiceI][indiceJ][layer] = new Statistic();
                                    }

                                    if (!Double.isNaN(vox.PadBVTotal)) {
                                        padMean[indiceI][indiceJ][layer].addValue(vox.PadBVTotal);
                                    }
                                }
                            }
                        }
                    }
                }
                
                long l = 4294967295L;

                try {
                    //on écrit la moyenne
                    for (int i = 0; i < rasterXSize; i++) {
                        for (int j = rasterYSize - 1, j2 = 0; j >= 0; j--, j2++) {
                            for (int k = 0; k < altitudes.length; k++) {

                                if (padMean[i][j][k] != null) {

                                    double meanPAD = padMean[i][j][k].getMean();
                                    //float value = (meanOfPAD-0)/(MAX_PAD-0);

                                    long value = (long) ((meanPAD / (double) infos.getMaxPAD()) * (l));
                                    String binaryString = Long.toBinaryString(value);
                                    byte[] bval = new BigInteger(binaryString, 2).toByteArray();
                                    ArrayUtils.reverse(bval);
                                    byte b0 = 0x0, b1 = 0x0, b2 = 0x0, b3 = 0x0;
                                    if (bval.length > 0) {
                                        b0 = bval[0];
                                    }
                                    if (bval.length > 1) {
                                        b1 = bval[1];
                                    }
                                    if (bval.length > 2) {
                                        b2 = bval[2];
                                    }
                                    if (bval.length > 3) {
                                        b3 = bval[3];
                                    }
                                    raster.setPixel(i, j2, k, b0, b1, b2, b3);

                                }
                            }
                        }
                    }

                } catch (Exception ex) {
                    LOGGER.error(ex);
                }
            }

        }
        
        return raster;
    }
    
    public static void writeRaster(File outputFile, BSQ raster) throws IOException{
        
        raster.setOutputFile(outputFile);
        
        raster.writeImage();
        raster.writeHeader();
    }
}
