/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.multires;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.ALSVoxel;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.TLSVoxel;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class ProcessingMultiRes {
    
    private final static Logger logger = Logger.getLogger(ProcessingMultiRes.class);
    
    private float maxPAD;
    
    public final static float DEFAULT_MAX_PAD_1M = 3.536958f;
    public final static float DEFAULT_MAX_PAD_2M = 2.262798f;
    public final static float DEFAULT_MAX_PAD_3M = 1.749859f;
    public final static float DEFAULT_MAX_PAD_4M = 1.3882959f;
    
    private float max_pad_1m = 3.536958f;
    private float max_pad_2m = 2.262798f;
    private float max_pad_3m = 1.749859f;
    private float max_pad_4m = 1.3882959f;
    
    public ProcessingMultiRes(float[] padLimits){
        
        max_pad_1m = padLimits[0];
        max_pad_2m = padLimits[1];
        max_pad_3m = padLimits[2];
        max_pad_4m = padLimits[3];
    }
    
    public void process(File outputFile, List<File> elements) {

        final Map<Double, VoxelSpaceLoader> voxelSpaces = new TreeMap<>();

        int count = 0;
        Point3d resolution;
        double minResolution = 0;
        
        for(File f : elements){

            VoxelSpaceLoader voxelSpace = new VoxelSpaceLoader(f);
            voxelSpace.load();

            resolution = voxelSpace.data.resolution;
            try {
                voxelSpaces.put(resolution.x, voxelSpace);
            } catch (Exception e) {
                logger.error(e);
            }

            if (count == 0) {
                minResolution = resolution.x;
            } else if (minResolution > resolution.x) {
                minResolution = resolution.x;
            }

            count++;

        }

        Iterator<Map.Entry<Double, VoxelSpaceLoader>> entries = voxelSpaces.entrySet().iterator();
        VoxelSpaceLoader vs = entries.next().getValue();

        int correctValues = 0;
        int correctedValues = 0;
        int setToDefault = 0;
        int totalValues = vs.data.voxels.size();

        /**
         * Calcul de la valeur moyenne de Pad de la scène (sous canopée et au
         * dessus du sol)*
         */
        int nb = 0;
        float sumPad = 0;

        int[][] tabTemp = new int[vs.data.split.x][vs.data.split.y];

        //initialisation
        for (int[] tabTemp1 : tabTemp) {
            for (int j = 0; j < tabTemp1.length; j++) {
                tabTemp1[j] = -1;
            }
        }

        //on cherche les voxels non vide les plus haut
        for (int n = 0; n < vs.data.voxels.size(); n++) {

            Voxel v = vs.data.voxels.get(n);
            int k = v.$k;

            if (v.nbSampling > 0 && v.nbEchos > 0 && k > tabTemp[v.$i][v.$j]) {
                tabTemp[v.$i][v.$j] = k;
            }
        }

    //on calcule la valeur moyenne de chaque couche
        //calcul des intervalles
        float[] padMeanZ = new float[vs.data.split.z];
        int[] padMeanZCount = new int[vs.data.split.z];

        for (int i = 0; i < vs.data.split.x; i++) {
            for (int j = 0; j < vs.data.split.y; j++) {
                for (int k = 0; k < vs.data.split.z; k++) {

                    //on vérifie qu'on est sous la canopée
                    if (k <= tabTemp[i][j]) {

                        Voxel vox = vs.data.getVoxel(i, j, k);

                        for (int t = 0; t < vs.data.split.z; t++) {
                            int min = t;
                            int max = t + 1;

                            if (vox.ground_distance >= min && vox.ground_distance < max && vox.nbSampling > 1) {
                                double pad = ((ALSVoxel) vox).PadBVTotal;
                                if (!Double.isNaN(pad) && pad < 3) {
                                    padMeanZ[t] += pad;
                                    padMeanZCount[t]++;
                                }

                                break;
                            }

                        }
                    }
                }
            }
        }

        for (int x = 0; x < padMeanZ.length; x++) {
            padMeanZ[x] = padMeanZ[x] / padMeanZCount[x];
        }

        for (int n = 0; n < vs.data.voxels.size(); n++) {

            double currentResolution = vs.data.resolution.x;

            Voxel voxel = vs.data.voxels.get(n);
            calculatePAD(voxel, currentResolution);

            entries = voxelSpaces.entrySet().iterator();
            entries.next().getValue();

            float currentNbSampling = voxel.nbSampling;
            double currentTransmittance = voxel.transmittance;

            boolean outOfResolutions = false;
            boolean uncorrectValue = false;

            VoxelSpaceLoader vsTemp;
            Voxel voxTemp = null;

            //while(currentNbSampling < Math.pow(currentResolution, 2)+1 || currentTransmittance == 0){
            //while (currentNbSampling < Math.pow(currentResolution, 2) * 2 + 1 || currentTransmittance == 0) {
            while(currentNbSampling == 0 || currentTransmittance == 0){  
                
                uncorrectValue = true;

                if (!entries.hasNext()) {
                    outOfResolutions = true;
                    break;
                }

                Map.Entry<Double, VoxelSpaceLoader> entry = entries.next();
                vsTemp = entry.getValue();
                currentResolution = entry.getKey();

                //il faudra utiliser la vraie position 
                Point3i indices = getIndicesFromIndices(new Point3i(voxel.$i, voxel.$j, voxel.$k), currentResolution);
                voxTemp = vsTemp.data.getVoxel(indices.x, indices.y, indices.z);

                if(voxTemp != null){
                    calculatePAD(voxTemp, currentResolution);

                    currentNbSampling = voxTemp.nbSampling;
                    currentTransmittance = voxTemp.transmittance;
                }
            }

            if (outOfResolutions) {
                //on met les valeurs par défaut

                if (((ALSVoxel) voxel).ground_distance > 0) {
                    currentResolution = 0;
                    ((ALSVoxel) voxel).PadBVTotal = padMeanZ[(int) voxel.ground_distance];
                } else {
                    currentResolution = Double.NaN;
                }

                setToDefault++;

            } else if (uncorrectValue) {
                //on applique la nouvelle valeur de Pad

                double oldValue = ((ALSVoxel) voxel).PadBVTotal;
                double newValue = ((ALSVoxel) voxTemp).PadBVTotal;

                ((ALSVoxel) voxel).PadBVTotal = newValue;

                correctedValues++;

            } else {
                correctValues++;
            }

            ExtendedALSVoxel eV = new ExtendedALSVoxel((ALSVoxel) voxel);

            eV.resolution = currentResolution;
            vs.data.voxels.set(n, eV);
        }
        
        logger.info("Nombre de valeurs correctes: " + correctValues + "/" + totalValues);
        logger.info("Nombre de valeurs corrigées: " + correctedValues + "/" + totalValues);
        logger.info("Nombre de valeurs mises à défaut: " + setToDefault + "/" + totalValues);
        
        vs.write(outputFile);

    }
    
    private void calculatePAD(Voxel vox, double resolution) {
        
        if(resolution <= 1.0){
            maxPAD= max_pad_1m;
        }else if(resolution <= 2.0){
            maxPAD= max_pad_2m;
        }else if(resolution <= 3.0){
            maxPAD= max_pad_3m;
        }else if(resolution <= 4.0){
            maxPAD=max_pad_4m;
        }else{
            maxPAD= max_pad_4m;
        }
        
        if (vox.nbSampling >= vox.nbEchos) {
            
            vox.lMeanTotal = vox.lgTotal / (vox.nbSampling);

        }
        
        float pad;

        if (vox instanceof TLSVoxel) {

            TLSVoxel tlsVox = (TLSVoxel) vox;

            /**
             * *PADBV**
             */
            if (tlsVox.bflEntering <= 0) {

                pad = Float.NaN;
                tlsVox.transmittance = Float.NaN;

            } else if (tlsVox.bflIntercepted > tlsVox.bflEntering) {

                tlsVox.transmittance = Float.NaN;
                pad = Float.NaN;

            } else {

                tlsVox.transmittance = (tlsVox.bflEntering - tlsVox.bflIntercepted) / tlsVox.bflEntering;

                if (tlsVox.nbSampling > 1 && tlsVox.transmittance == 0) {

                    pad = maxPAD;

                } else if (tlsVox.nbSampling <= 2 && tlsVox.transmittance == 0) {

                    pad = Float.NaN;

                } else {
                    
                    pad = (float) (Math.log(tlsVox.transmittance) / (-0.5 * tlsVox.lMeanTotal));
                    
                    if (Float.isNaN(pad)) {
                        pad = Float.NaN;
                    } else if (pad > maxPAD || Float.isInfinite(pad)) {
                        pad = maxPAD;
                    }
                }

            }
            
            tlsVox.PadBflTotal = pad + 0.0f; //set +0.0f to avoid -0.0f

        } else {

            ALSVoxel alsVox = (ALSVoxel) vox;

            if (alsVox.bvEntering <= 0) {
                
                pad = Float.NaN;
                alsVox.transmittance = Float.NaN;

            } else if (alsVox.bvIntercepted > alsVox.bvEntering) {

                pad = Float.NaN;
                alsVox.transmittance = Float.NaN;

            } else {
                
                alsVox.transmittance = (alsVox.bvEntering - alsVox.bvIntercepted) / alsVox.bvEntering;

                if (alsVox.nbSampling > 1 && alsVox.transmittance == 0) {

                    pad = maxPAD;

                } else if (alsVox.nbSampling < 2 && alsVox.transmittance == 0) {

                    pad = Float.NaN;

                } else {

                    pad = (float) (Math.log(alsVox.transmittance) / (-0.5 * alsVox.lMeanTotal));

                    if (Float.isNaN(pad)) {
                        pad = Float.NaN;
                    } else if (pad > maxPAD || Float.isInfinite(pad)) {
                        pad = maxPAD;
                    }
                }

            }
            alsVox.PadBVTotal = pad + 0.0f; //set +0.0f to avoid -0.0f
        }

    }
    
    private Point3i getIndicesFromIndices(Point3i indices, double resolution){
        
        return new Point3i((int)(indices.x/resolution), (int)(indices.y/resolution), (int)(indices.z/resolution));
    }

}
