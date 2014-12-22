/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics2d.image;

import fr.ird.voxelidar.graphics3d.object.terrain.Terrain;
import fr.ird.voxelidar.graphics3d.object.voxelspace.Voxel;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace;
import fr.ird.voxelidar.util.ColorGradient;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Map;
import org.apache.commons.collections.MapIterator;
import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.collections.map.MultiKeyMap;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class Projection {
    
    final static Logger logger = Logger.getLogger(Projection.class);
    
    private final VoxelSpace voxelSpace;
    private final Terrain terrain;
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
    
    public Projection(VoxelSpace voxelSpace, Terrain terrain){
        
        this.voxelSpace = voxelSpace;
        this.terrain = terrain;
    }
    
    public BufferedImage generateMap(int type){
        
        MultiKeyMap map = new MultiKeyMap();
        
        if(terrain != null){
            
            MultiKeyMap mapTerrainXY = terrain.getXYStructure();
        
            ArrayList<Voxel> voxelList = voxelSpace.getVoxelList();
            

            for(Voxel voxel : voxelList){

                float value = 0;
                switch(type){
                    case Projection.PAI:
                        value = generatePAI(voxel.getAttributs());
                        break;
                    case Projection.TRANSMITTANCE:
                        value = generateTransmittanceMap(voxel.getAttributs());
                        break;
                }

                int x = voxel.indiceX;
                int y = voxel.indiceZ;

                if(Float.isNaN(value)){
                    value = 0;
                }

                float hauteurTerrainXY = 0;
                try{
                    hauteurTerrainXY = (float) mapTerrainXY.get(voxel.x, voxel.z);
                }catch(Exception e){
                    logger.error("voxelisation failed", e);
                }

                if(voxel.y > hauteurTerrainXY){

                    if(map.containsKey(x, y)){

                        map.put(x, y, (float)map.get(x, y)+value);
                    }else{
                        map.put(x, y, value);
                    }
                }
            }
        
        }else{
        
            ArrayList<Voxel> voxelList = voxelSpace.getVoxelList();
            

            for(Voxel voxel : voxelList){

                float value = 0;
                switch(type){
                    case Projection.PAI:
                        value = generatePAI(voxel.getAttributs());
                        break;
                    case Projection.TRANSMITTANCE:
                        value = generateTransmittanceMap(voxel.getAttributs());
                        break;
                }

                int x = voxel.indiceX;
                int y = voxel.indiceZ;

                if(Float.isNaN(value)){
                    value = 0;
                }

                if(map.containsKey(x, y)){

                    map.put(x, y, (float)map.get(x, y)+value);
                }else{
                    map.put(x, y, value);
                }
            }
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
        Color[][] texture = new Color[voxelSpace.nX][voxelSpace.nY];
        
        it = map.mapIterator();

        while (it.hasNext()) {
            it.next();
            float value = (float) it.getValue();
            
            MultiKey mk = (MultiKey) it.getKey();
            
            int indiceX = (int)mk.getKey(0);
            int indiceY = (int)mk.getKey(1);
            
            texture[indiceX][indiceY] = gradient.getColor(value);
        }
        
        
        
        BufferedImage bi = new BufferedImage(voxelSpace.nX, voxelSpace.nY, BufferedImage.TYPE_INT_RGB);
        
        for (int i = 0; i < voxelSpace.nX; i++) {
            for (int j = 0; j < voxelSpace.nY; j++) {

                bi.setRGB(i, j, texture[i][j].getRGB());
            }
        }
        
        return bi;
    }
    
    private float generatePAI(Map<String, Float> attributs){
        
        Float nInterceptes = attributs.get("n_interceptes");
        Float nApres = attributs.get("n_apres");


        float densitePn = nInterceptes/(nApres+nInterceptes);
        float transmittance = 1 - densitePn;

        float pad = (float) ((Math.log(transmittance)*(-2))/1);
        if(Float.isNaN(pad) || Float.isInfinite(pad)){
            pad = 0;
        }
        
        return pad;
    }
    
    

    private float generateTransmittanceMap(Map<String, Float> attributs){
        
        Float nInterceptes = attributs.get("n_interceptes");
        Float nApres = attributs.get("n_apres");


        float densitePn = nInterceptes/(nApres+nInterceptes);
        
        float transmittance = 1 - densitePn;
        
        return transmittance;
    }
    
}
