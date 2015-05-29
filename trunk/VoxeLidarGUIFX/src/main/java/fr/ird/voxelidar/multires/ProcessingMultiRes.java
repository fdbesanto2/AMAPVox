/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.multires;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.ALSVoxel;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ProcessingMultiRes {
    
    private final static Logger logger = Logger.getLogger(ProcessingMultiRes.class);
    
    private float maxPAD;
    private boolean useDefaultMaxPad = false;
    
    private VoxelSpaceLoader vs;
    
    public final static float DEFAULT_MAX_PAD_1M = 3.536958f;
    public final static float DEFAULT_MAX_PAD_2M = 2.262798f;
    public final static float DEFAULT_MAX_PAD_3M = 1.749859f;
    public final static float DEFAULT_MAX_PAD_4M = 1.3882959f;
    public final static float DEFAULT_MAX_PAD_5M = 1.0848f;
    
    private float max_pad_1m = DEFAULT_MAX_PAD_1M;
    private float max_pad_2m = DEFAULT_MAX_PAD_2M;
    private float max_pad_3m = DEFAULT_MAX_PAD_3M;
    private float max_pad_4m = DEFAULT_MAX_PAD_4M;
    private float max_pad_5m = DEFAULT_MAX_PAD_5M;
    
    public ProcessingMultiRes(float[] padLimits, boolean useDefaultMaxPad){
        
        max_pad_1m = padLimits[0];
        max_pad_2m = padLimits[1];
        max_pad_3m = padLimits[2];
        max_pad_4m = padLimits[3];
        max_pad_5m = padLimits[4];
        
        this.useDefaultMaxPad = useDefaultMaxPad;
    }
    
    public void process(List<File> elements) {

        final Map<Float, VoxelSpaceLoader> voxelSpaces = new TreeMap<>();

        int count = 0;
        float resolution;
        double minResolution = 0;
        
        for(File f : elements){

            VoxelSpaceLoader voxelSpace = new VoxelSpaceLoader(f);
            voxelSpace.load();

            resolution = voxelSpace.data.res;
            try {
                voxelSpaces.put(resolution, voxelSpace);
            } catch (Exception e) {
                logger.error(e);
            }

            if (count == 0) {
                minResolution = resolution;
            } else if (minResolution > resolution) {
                minResolution = resolution;
            }

            count++;

        }

        Iterator<Map.Entry<Float, VoxelSpaceLoader>> entries = voxelSpaces.entrySet().iterator();
        vs = entries.next().getValue();

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
        
        for (int i = 0; i < vs.data.split.x; i++) {
            for (int j = 0; j < vs.data.split.y; j++) {
                for (int k = vs.data.split.z-1; k >= 0; k--) {
                    
                    Voxel v= vs.data.getVoxel(i, j, k);
                    
                    if (v.nbSampling > 0 && v.nbEchos > 0 && k > tabTemp[v.$i][v.$j]) {
                        tabTemp[v.$i][v.$j] = k;
                        break;
                    }
                }
            }
        }
        /*
        for (Voxel v : vs.data.voxels) {
            
            int k = v.$k;

            if (v.nbSampling > 0 && v.nbEchos > 0 && k > tabTemp[v.$i][v.$j]) {
                tabTemp[v.$i][v.$j] = k;
            }
        }
        */
        
        //on calcule la valeur moyenne de chaque couche
        //calcul des intervalles
        float[] padMeanZ = new float[vs.data.split.z];
        int[] padMeanZCount = new int[vs.data.split.z];

        for (int i = 0; i < vs.data.split.x; i++) {
            for (int j = 0; j < vs.data.split.y; j++) {
                
                int indiceMaxZ = tabTemp[i][j];
                
                /*on calcul le PAD moyen par couche
                    pour cela on parcours chaque couche
                */
                for (int k = indiceMaxZ; k >= 0; k--) {
                    
                    Voxel vox = vs.data.getVoxel(i, j, k);
                    double pad = ((ALSVoxel) vox).PadBVTotal;
                    
                    if (!Double.isNaN(pad)) {
                        padMeanZ[k] += pad;
                        padMeanZCount[k]++;
                    }
                }
            }
        }
        /*
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
        */
        for (int x = 0; x < padMeanZ.length; x++) {
            padMeanZ[x] = padMeanZ[x] / padMeanZCount[x];
        }
        

        for (int n = 0; n < vs.data.voxels.size(); n++) {

            float currentResolution = vs.data.res;

            ExtendedALSVoxel voxel = vs.data.voxels.get(n);
            calculatePAD(voxel, currentResolution, useDefaultMaxPad, vs.data.maxPad);

            entries = voxelSpaces.entrySet().iterator();
            entries.next().getValue();

            float currentNbSampling = voxel.nbSampling;
            double currentTransmittance = voxel.transmittance;

            boolean outOfResolutions = false;
            boolean uncorrectValue = false;

            VoxelSpaceLoader vsTemp;
            ExtendedALSVoxel voxTemp = null;

            //while(currentNbSampling < Math.pow(currentResolution, 2)+1 || currentTransmittance == 0){
            //while (currentNbSampling < Math.pow(currentResolution, 2) * 2 + 1 || currentTransmittance == 0) {
            while(currentNbSampling == 0 || currentTransmittance == 0 || Double.isNaN(currentTransmittance)){  
                
                uncorrectValue = true;

                if (!entries.hasNext()) {
                    outOfResolutions = true;
                    break;
                }

                Map.Entry<Float, VoxelSpaceLoader> entry = entries.next();
                vsTemp = entry.getValue();
                currentResolution = entry.getKey();

                //il faudra utiliser la vraie position 
                Point3i indices = getIndicesFromIndices(new Point3i(voxel.$i, voxel.$j, voxel.$k), currentResolution);
                voxTemp = vsTemp.data.getVoxel(indices.x, indices.y, indices.z);
                
                if(voxTemp != null  && voxTemp.$i == indices.x && voxTemp.$j == indices.y && voxTemp.$k == indices.z){
                    
                    if(voxTemp.ground_distance > 1){
                        calculatePAD(voxTemp, currentResolution, useDefaultMaxPad, vs.data.maxPad);

                        currentNbSampling = voxTemp.nbSampling;
                        currentTransmittance = voxTemp.transmittance;
                    }
                }
                
            }

            if (outOfResolutions) {
                //on met les valeurs par défaut

                if (voxel.ground_distance > 1) {
                    currentResolution = 0;
                    int indice = vs.data.split.z - tabTemp[voxel.$i][voxel.$j] + voxel.$k;
                    
                    if(indice >= padMeanZ.length){
                        voxel.PadBVTotal = 0;
                    }else if(indice < 0){
                        voxel.PadBVTotal = Float.NaN;
                    }else{
                        if(useDefaultMaxPad && padMeanZ[indice]>vs.data.maxPad){
                            voxel.PadBVTotal = vs.data.maxPad;
                        }else{
                            voxel.PadBVTotal = padMeanZ[indice];
                        }
                        
                    }
                    
                } else {
                    currentResolution = Float.NaN;
                }

                setToDefault++;

            } else if (uncorrectValue) {
                //on applique la nouvelle valeur de Pad

                //float oldValue = voxel.PadBVTotal;
                float newValue = voxTemp.PadBVTotal;
                
                if(Double.isNaN(newValue)){
                    logger.error("incorrect Pad value");
                }

                voxel.PadBVTotal = newValue;

                correctedValues++;

            } else {
                correctValues++;
            }


            voxel.resolution = currentResolution;
            
            
            vs.data.voxels.set(n, voxel);
        }
        
        
        logger.info("Nombre de valeurs correctes: " + correctValues + "/" + totalValues);
        logger.info("Nombre de valeurs corrigées: " + correctedValues + "/" + totalValues);
        logger.info("Nombre de valeurs mises à défaut: " + setToDefault + "/" + totalValues);
        
        

    }
    
    public void write(File outputFile){
        vs.write(outputFile);
    }
    
    private void calculatePAD(ExtendedALSVoxel vox, double resolution, boolean useDefault, float defaultMaxPad) {
        
        if(useDefault){
            maxPAD = defaultMaxPad;
        }else{
            if(resolution <= 1.0){
                maxPAD = max_pad_1m;
            }else if(resolution <= 2.0){
                maxPAD = max_pad_2m;
            }else if(resolution <= 3.0){
                maxPAD = max_pad_3m;
            }else if(resolution <= 4.0){
                maxPAD = max_pad_4m;
            }else if(resolution <= 5.0){
                maxPAD = max_pad_5m;
            }else{
                maxPAD = max_pad_5m;
            }
        }
        
        
        if (vox.nbSampling >= vox.nbEchos) {
            
            vox.lMeanTotal = vox.lgTotal / (vox.nbSampling);

        }
        
        float pad;

        if (vox.bvEntering <= 0) {

            pad = Float.NaN;
            vox.transmittance = Float.NaN;

        } else if (vox.bvIntercepted > vox.bvEntering) {

            pad = Float.NaN;
            vox.transmittance = Float.NaN;

        } else {

            vox.transmittance = (vox.bvEntering - vox.bvIntercepted) / vox.bvEntering;

            if (vox.nbSampling > 1 && vox.transmittance == 0) {

                pad = maxPAD;

            } else if (vox.nbSampling < 2 && vox.transmittance == 0) {

                pad = Float.NaN;

            } else {

                pad = (float) (Math.log(vox.transmittance) / (-0.5 * vox.lMeanTotal));

                if (Float.isNaN(pad)) {
                    pad = Float.NaN;
                } else if (pad > maxPAD || Float.isInfinite(pad)) {
                    pad = maxPAD;
                }
            }

        }
        
        vox.PadBVTotal = pad + 0.0f; //set +0.0f to avoid -0.0f

    }
    
    private Point3i getIndicesFromIndices(Point3i indices, double resolution){
        
        return new Point3i((int)(indices.x/resolution), (int)(indices.y/resolution), (int)(indices.z/resolution));
    }

}
