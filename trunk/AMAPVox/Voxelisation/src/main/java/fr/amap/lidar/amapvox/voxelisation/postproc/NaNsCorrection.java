/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.postproc;

import fr.amap.commons.util.Statistic;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import fr.amap.commons.util.Cancellable;

/**
 *
 * @author calcul
 */
public class NaNsCorrection implements Cancellable{
    
    private final static Logger LOGGER = Logger.getLogger(NaNsCorrection.class);
    
    private boolean cancelled;
    
    public void correct(VoxelParameters parameters, Voxel voxels[][][]){
        
        /**A faire : corriger de manière parallèle**/
        
        int xSplit = parameters.infos.getSplit().x;
        int ySplit = parameters.infos.getSplit().y;
        int zSplit = parameters.infos.getSplit().z;
        
        int[][] canopeeArray = new int[xSplit][ySplit];
        for (int x = 0; x < xSplit; x++) {
            for (int y = 0; y < ySplit; y++) {
                for (int z = zSplit-1; z >= 0; z--) {
                    
                    Voxel voxel = voxels[x][y][z];
                    
                    if (voxel.nbSampling > 0 && voxel.nbEchos > 0) {
                        canopeeArray[x][y] = z;
                        break;
                    }
                }
            }
        }
        
        int passLimit = Integer.max(Integer.max(xSplit, ySplit), zSplit);
        
        int passMax = 0;
        
        long startTime = System.currentTimeMillis();
        
        for (int x = 0; x < xSplit; x++) {
            for (int y = 0; y < ySplit; y++) {
                for (int z = 0; z < zSplit; z++) {
                    
                    if(cancelled){
                        return;
                    }
                    
                    Voxel voxel = voxels[x][y][z];
                    
                    if(voxel.ground_distance >= (parameters.infos.getResolution() / 2.0f)){
                        
                        float currentNbSampling = voxel.nbSampling;
                        float currentTransmittance = voxel.transmittance;

                        List<Voxel> neighbours = new ArrayList<>();
                        int nbRemovedNeighbors = 0;

                        int passID = 1;

                        //testloop:
                        while(currentNbSampling <= parameters.getNaNsCorrectionParams().getNbSamplingThreshold() || currentTransmittance == 0 ){
                            
                                  
                            if(cancelled){
                                return;
                            }
                            
                            if(passID > passLimit){
                                break;
                            }
                            
                            int minX = Integer.max(x-passID, 0);
                            int minY = Integer.max(y-passID, 0);
                            int minZ = Integer.max(z-passID, 0);
                            
                            int maxX = Integer.min(x+passID, xSplit-1);
                            int maxY = Integer.min(y+passID, ySplit-1);
                            int maxZ = Integer.min(z+passID, zSplit-1);
                            
                            //get neighbors
                            for(int i = minX ; i<= maxX ; i++){
                                for(int j = minY ; j<= maxY ; j++){
                                    for(int k = minZ ; k<= maxZ ; k++){
                                        
                                        if (cancelled) {
                                            return;
                                        }

                                            //on n'ajoute pas les voxels de la passe précédente
                                        if(passID != 1 && (i >= x-(passID-1) && i <= x+(passID-1))
                                                && (j >= y-(passID-1) && j <= y+(passID-1))
                                                && (k >= z-(passID-1) && k <= z+(passID-1))){

                                        } else {

                                            if (i == x && j == y && k == z) {

                                            } else {
                                                if (k <= canopeeArray[i][j]) {

                                                    Voxel neighbour = voxels[i][j][k];

                                                    if (neighbour.ground_distance >= -(parameters.infos.getResolution() / 2.0f)) {
                                                        neighbours.add(neighbour);
                                                    }else{
                                                        nbRemovedNeighbors++;
                                                    }
                                                }else{
                                                    nbRemovedNeighbors++;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Statistic nbSamplingStat = new Statistic();
                            float meanTransmittance = 0;
                            
                            float sumBVEntering = 0;
                            float sumBVIntercepted = 0;
                            float sumLgTotal = 0;
                            
                            
                            int count = 0;

                            for(Voxel neighbour : neighbours){
                                
                                if (cancelled) {
                                    return;
                                }
                                
                                /*les voxels de transmittance nulle sont traités comme étant non échantillonné,
                                tous les voisins sont considérés indépendamment de l'échantillonnage*/
                                
                                if(!Float.isNaN(neighbour.transmittance) /*&& neighbour.transmittance != 0*/){
                                    
                                    sumBVEntering += neighbour.bvEntering;
                                    sumBVIntercepted += neighbour.bvIntercepted;
                                    sumLgTotal += neighbour.lgTotal;
                                    nbSamplingStat.addValue(neighbour.nbSampling);

                                    count++;
                                }
                            }

                            if(count > 0){
                                
                                meanTransmittance = (float) Math.pow((sumBVEntering-sumBVIntercepted)/sumBVEntering, nbSamplingStat.getSum()/sumLgTotal);
                                                                
                                currentNbSampling = (float) nbSamplingStat.getMean();
                                currentTransmittance = meanTransmittance;

                            
                            }else{
                                currentNbSampling = 0;
                            }
                            
                            passID++;
                            
                        }

                        if(passID > passMax){
                            passMax = passID;
                            LOGGER.info("Maximum neighborhood range : "+passMax);
                        }

                        if(neighbours.size() > 0){

                            Statistic PADStatistic = new Statistic();
                            
                            for(Voxel neighbour : neighbours){
                                
                                if (cancelled) {
                                    return;
                                }

                                if(!Float.isNaN(neighbour.transmittance) && neighbour.transmittance != 0){
                                    PADStatistic.addValue(neighbour.PadBVTotal);
                                }
                            }
                            
                            if((PADStatistic.getNbValues()) != 0){
                                voxels[x][y][z].neighboursNumber = neighbours.size();
                                voxels[x][y][z].passNumber = passID;
                                voxels[x][y][z].PadBVTotal = (float)PADStatistic.getMean();
                                voxels[x][y][z].nbSampling = (int)currentNbSampling;
                                voxels[x][y][z].transmittance = currentTransmittance;
                            }                           
                            
                        }
                    }
                    
                }
            }
        }
        
        long endTime = System.currentTimeMillis();
        long time = endTime - startTime;
        LOGGER.info("Time : "+time+" ms");
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
