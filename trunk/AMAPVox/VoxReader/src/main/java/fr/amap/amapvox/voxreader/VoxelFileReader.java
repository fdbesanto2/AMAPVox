/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxreader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class VoxelFileReader implements Iterable<Voxel>{

    private final static Logger logger = Logger.getLogger(VoxelFileReader.class);
    
    private File voxelFile;
    private BufferedReader reader;
    private String currentLine = null;
    
    private final VoxelSpaceInfos voxelSpaceInfos;
    
    public VoxelFileReader(File voxelFile){
        
        this.voxelFile = voxelFile;
        
        voxelSpaceInfos= new VoxelSpaceInfos();
        voxelSpaceInfos.readFromVoxelFile(voxelFile);
    }
    
    @Override
    public Iterator<Voxel> iterator() {
        
        try {
            reader = new BufferedReader(new FileReader(voxelFile));
            
            for(int i=0;i<6;i++){
                reader.readLine();
            }
            
        } catch (FileNotFoundException ex) {
            logger.error("Cannot find file", ex);
        } catch (IOException ex) {
            logger.error("Cannot read file", ex);
        }
        
        
        
        Iterator<Voxel> it;
        
        it = new Iterator<Voxel>() {

            @Override
            public boolean hasNext() {
                
                try {
                    return ((currentLine = reader.readLine()) != null);
                } catch (IOException ex) {
                    return false;
                }
            }

            @Override
            public Voxel next() {
                
                //on parse la ligne
                return parseVoxelFileLine(currentLine);
            }
        };
        
        return it;
    }
    
    public Voxel parseVoxelFileLine(String line){
        
        String[] voxelLine = line.split(" ");
                    
        Point3i indice = new Point3i(Integer.valueOf(voxelLine[0]), 
                Integer.valueOf(voxelLine[1]),
                Integer.valueOf(voxelLine[2]));

        float[] mapAttrs = new float[voxelSpaceInfos.getColumnNames().length];

        for (int i=0;i<voxelLine.length;i++) {

            float value = Float.valueOf(voxelLine[i]);

            mapAttrs[i] = value;
        }

        Voxel vox = new Voxel(indice.x, indice.y, indice.z, ExtendedALSVoxel.class);

        for(int i=3;i<mapAttrs.length;i++){
            try {
                vox.setFieldValue(vox.getClass(), voxelSpaceInfos.getColumnNames()[i], vox, mapAttrs[i]);
            } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException ex) {
               logger.error("Cannot set field value",ex);
            }
        }
        
        return vox;
    }

    public static Logger getLogger() {
        return logger;
    }

    public File getVoxelFile() {
        return voxelFile;
    }

    public BufferedReader getReader() {
        return reader;
    }

    public String getCurrentLine() {
        return currentLine;
    }

    public VoxelSpaceInfos getVoxelSpaceInfos() {
        return voxelSpaceInfos;
    }
}
