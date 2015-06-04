/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.projection;

import fr.ird.voxelidar.lidar.format.dtm.RegularDtm;
import fr.ird.voxelidar.engine3d.object.scene.VoxelObject;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData;
import fr.ird.voxelidar.util.ColorGradient;
import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.awt.Color;
import java.awt.image.BufferedImage;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Projection {
    
    final static Logger logger = Logger.getLogger(Projection.class);
    
    private final VoxelSpaceData data;
    private final RegularDtm terrain;
    private float minValue;
    private float maxValue;
    
    public static final short PAI = 1;
    public static final short TRANSMITTANCE = 2;

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }
    
    public Projection(VoxelSpaceData voxelSpace, RegularDtm terrain){
        
        this.data = voxelSpace;
        this.terrain = terrain;
    }
    
    public BufferedImage generateMap(int type){
        
        MultiKeyMap map = new MultiKeyMap();
        
        if(terrain != null){
            
            MultiKeyMap mapTerrainXY = terrain.getXYStructure();
                   

            for(VoxelObject voxel : data.voxels){
                
                
                float value = 0;
                float[] attributs = voxel.getAttributs();
                
                switch(type){
                    case Projection.PAI:
                        value = generatePAI(attributs[data.header.attributsNames.indexOf("interceptions")], attributs[data.header.attributsNames.indexOf("nbSampling")]);
                        break;
                    case Projection.TRANSMITTANCE:
                        value = generateTransmittanceMap(attributs[data.header.attributsNames.indexOf("interceptions")], attributs[data.header.attributsNames.indexOf("nbSampling")]);
                        break;
                }

                int x = voxel.$i;
                int y = voxel.$k;

                if(Float.isNaN(value)){
                    value = 0;
                }

                float hauteurTerrainXY = 0;
                try{
                    hauteurTerrainXY = (float) mapTerrainXY.get(voxel.position.x, voxel.position.z);
                }catch(Exception e){
                    logger.error("voxelisation failed", e);
                }

                if(voxel.position.y > hauteurTerrainXY){

                    if(map.containsKey(x, y)){

                        map.put(x, y, (float)map.get(x, y)+value);
                    }else{
                        map.put(x, y, value);
                    }
                }
            }
        
        }else{
            /*
            int count = 0;
            for(Voxel voxel : data.voxels){

                float value = 0;
                
                switch(type){
                    case Projection.PAI:
                        value = generatePAI(data.getVoxelValue("interceptions", count), data.getVoxelValue("nbSampling", count));
                        break;
                    case Projection.TRANSMITTANCE:
                        value = generateTransmittanceMap(data.getVoxelValue("interceptions", count), data.getVoxelValue("nbSampling", count));
                        break;
                }

                int x = voxel.$i;
                int y = voxel.$k;

                if(Float.isNaN(value)){
                    value = 0;
                }

                if(map.containsKey(x, y)){

                    map.put(x, y, (float)map.get(x, y)+value);
                }else{
                    map.put(x, y, value);
                }
                
                count++;
            }
            */
        }
        
        
        
        int index = 0;
        
        MapIterator it = map.mapIterator();
        
        while (it.hasNext()) {
            
            it.next();
            float value = (float) it.getValue();
            
            
            
            if(index == 0){
                
                minValue = value;
                maxValue = value;
                
            }else{
                
                if(value<minValue){
                    minValue = value;
                }
                if(value>maxValue){
                    maxValue = value;
                }
            }
            index++;
        }
        
        ColorGradient gradient = new ColorGradient(minValue, maxValue);
        gradient.setGradientColor(ColorGradient.GRADIENT_HEAT);
        Color[][] texture = new Color[data.header.split.x][data.header.split.y];
        
        it = map.mapIterator();

        while (it.hasNext()) {
            it.next();
            float value = (float) it.getValue();
            
            MultiKey mk = (MultiKey) it.getKey();
            
            int indiceX = (int)mk.getKey(0);
            int indiceY = (int)mk.getKey(1);
            
            texture[indiceX][indiceY] = gradient.getColor(value);
        }
        
        
        
        BufferedImage bi = new BufferedImage(data.header.split.x, data.header.split.y, BufferedImage.TYPE_INT_RGB);
        
        for (int i = 0; i < data.header.split.x; i++) {
            for (int j = 0; j < data.header.split.y; j++) {

                bi.setRGB(i, j, texture[i][j].getRGB());
            }
        }
        
        return bi;
    }
    
    
    private float generatePAI(float interceptions, float nbSampling){
        
        float densitePn = interceptions/(nbSampling+interceptions);
        float transmittance = 1 - densitePn;

        float pad = (float) ((Math.log(transmittance)*(-2))/1);
        if(Float.isNaN(pad) || Float.isInfinite(pad)){
            pad = 0;
        }
        
        return pad;
    }
    
    

    private float generateTransmittanceMap(float interceptions, float nbSampling){
        
        float densitePn = interceptions/(nbSampling+interceptions);
        
        float transmittance = 1 - densitePn;
        
        return transmittance;
    }
    
}
