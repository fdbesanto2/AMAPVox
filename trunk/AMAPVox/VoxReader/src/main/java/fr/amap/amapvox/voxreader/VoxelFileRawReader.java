/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxreader;

import fr.amap.amapvox.voxcommons.RawVoxel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class VoxelFileRawReader extends AbstractReader implements Iterable<RawVoxel>{

    private final static Logger logger = Logger.getLogger(VoxelFileRawReader.class);
    
    public VoxelFileRawReader(File voxelFile, boolean keepInMemory) throws Exception {
        super(voxelFile, keepInMemory);
    }

    @Override
    public Iterator<RawVoxel> iterator() {
        
        currentVoxelIndex = -1;
        
        if((!wasRead && keepInMemory) || !keepInMemory){
            
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
        }
        
        Iterator<RawVoxel> it = new Iterator<RawVoxel>() {

            @Override
            public boolean hasNext() {
                
                if((!wasRead && keepInMemory) || !keepInMemory){
                    try {
                        boolean isNextExist = ((currentLine = reader.readLine()) != null);

                        if(!isNextExist){
                            wasRead = true;
                        }

                        return isNextExist;
                    } catch (IOException ex) {
                        return false;
                    }
                    
                }else{
                    return voxelSpace.voxels != null && currentVoxelIndex+1 < voxelSpace.voxels.size();     
                }
            }

            @Override
            public RawVoxel next() {
                
                //on parse la ligne
                currentVoxelIndex++;
                
                RawVoxel voxel;
                
                if(wasRead){
                    if(keepInMemory){
                        return (RawVoxel) voxelSpace.voxels.get(currentVoxelIndex);
                    }else{
                        voxel = parseVoxelFileLine(currentLine);
                    }
                }else{
                    voxel = parseVoxelFileLine(currentLine);
                    
                    if(voxel == null){
                        logger.error("Error parsing line: "+(currentVoxelIndex+7));
                    }else{
                        if(keepInMemory){
                            voxelSpace.voxels.add(voxel);
                        }
                    }
                    
                }
                
                return voxel;
            }
        };
        
        return it;
    }

    private RawVoxel parseVoxelFileLine(String line){
        
        try{
            String[] voxelLine = line.split(" ");
                    
            Point3i indice = new Point3i(Integer.valueOf(voxelLine[0]), 
                    Integer.valueOf(voxelLine[1]),
                    Integer.valueOf(voxelLine[2]));


            RawVoxel vox = new RawVoxel(indice.x, indice.y, indice.z);
            vox.attributs = new float[voxelLine.length - 3];

            for(int i=3;i<voxelLine.length;i++){
                vox.attributs[i-3] = Float.valueOf(voxelLine[i]);
            }
            
            return vox;
            
        }catch(Exception e){
            return null;
        }
        
        
    }
}
