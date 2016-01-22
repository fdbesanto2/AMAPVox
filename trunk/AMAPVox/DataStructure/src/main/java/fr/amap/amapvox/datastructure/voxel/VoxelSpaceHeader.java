
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.amap.amapvox.datastructure.voxel;

import fr.amap.commons.util.io.file.FileManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class VoxelSpaceHeader {
        
    public VoxelSpaceData.Type type;
    public Point3i split;
    public Point3d resolution;
    public Point3d bottomCorner;
    public Point3d topCorner;
    public float res;
    public float maxPad = 5.0f;
    public List<String> attributsNames;

    public VoxelSpaceHeader() {
               
        attributsNames = new ArrayList<>();
        split = new Point3i();
        resolution = new Point3d();
        bottomCorner = new Point3d();
        topCorner = new Point3d();
    }
    
    public static VoxelSpaceHeader readVoxelFileHeader(File f){
        
        String header = FileManager.readHeader(f.getAbsolutePath());
        VoxelSpaceHeader voxelSpaceHeader;
        
        if(header.equals("VOXEL SPACE")){
            
            voxelSpaceHeader = new VoxelSpaceHeader();
            
            try(BufferedReader reader = new BufferedReader(new FileReader(f))) {
                
                //header
                reader.readLine();
                
                
                String[] minC = reader.readLine().split(" ");
                voxelSpaceHeader.bottomCorner.x =  Double.valueOf(minC[1]);
                voxelSpaceHeader.bottomCorner.y =  Double.valueOf(minC[2]);
                voxelSpaceHeader.bottomCorner.z =  Double.valueOf(minC[3]);
                
                String[] maxC = reader.readLine().split(" ");
                voxelSpaceHeader.topCorner.x =  Double.valueOf(maxC[1]);
                voxelSpaceHeader.topCorner.y =  Double.valueOf(maxC[2]);
                voxelSpaceHeader.topCorner.z =  Double.valueOf(maxC[3]);
                
                String[] split = reader.readLine().split(" ");
                
                voxelSpaceHeader.split = new Point3i(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
                                
                voxelSpaceHeader.resolution.x = (voxelSpaceHeader.topCorner.x - voxelSpaceHeader.bottomCorner.x) / voxelSpaceHeader.split.x;
                voxelSpaceHeader.resolution.y = (voxelSpaceHeader.topCorner.y - voxelSpaceHeader.bottomCorner.y) / voxelSpaceHeader.split.y;
                voxelSpaceHeader.resolution.z = (voxelSpaceHeader.topCorner.z - voxelSpaceHeader.bottomCorner.z) / voxelSpaceHeader.split.z;
                
                
                String[] metadatas = reader.readLine().split(" ");
                String type = metadatas[1];
                
                if(type.equals("ALS")){
                    voxelSpaceHeader.type = VoxelSpaceData.Type.ALS;
                }else{
                    voxelSpaceHeader.type = VoxelSpaceData.Type.TLS;
                }
                
                if(metadatas.length > 3){
                    
                    voxelSpaceHeader.res = Float.valueOf(metadatas[3]);
                    
                    if(metadatas.length > 5){
                        voxelSpaceHeader.maxPad = Float.valueOf(metadatas[5]);
                    }
                    
                }
                
                String[] columnsNames = reader.readLine().split(" ");
                
                
                voxelSpaceHeader.attributsNames.addAll(Arrays.asList(columnsNames));
                
                return voxelSpaceHeader;
                
            }catch(Exception e){
                return null;
            }
        }
        
        return null;
    }
    
}
