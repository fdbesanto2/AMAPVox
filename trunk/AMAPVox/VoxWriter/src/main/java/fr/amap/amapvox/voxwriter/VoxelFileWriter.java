/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxwriter;

import fr.amap.amapvox.voxcommons.Voxel;
import fr.amap.amapvox.voxcommons.VoxelSpace;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 *
 * @author calcul
 */
public class VoxelFileWriter {
    
    public static void write(VoxelSpace voxelspace, File outputFile) throws IOException{
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(voxelspace.toString()+"\n");
            
            for (Iterator it = voxelspace.voxels.iterator(); it.hasNext();) {
                Voxel voxel = (Voxel) it.next();
                writer.write(voxel+"\n");
            }
        }
    }
}
