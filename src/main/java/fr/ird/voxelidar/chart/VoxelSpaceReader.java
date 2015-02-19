/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.chart;

import fr.ird.voxelidar.engine3d.math.point.Point2F;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceData;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceRawData;
import fr.ird.voxelidar.io.file.FileManager;
import fr.ird.voxelidar.util.DataSet;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import javax.vecmath.Point3i;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class VoxelSpaceReader {
    
    private final static Logger logger = Logger.getLogger(VoxelSpaceReader.class);
    
    public static VoxelSpaceRawData read(File file){
        
        String header = FileManager.readHeader(file.getAbsolutePath());
        VoxelSpaceRawData data = new VoxelSpaceRawData();
        
        if(header.equals("VOXEL SPACE")){
            

            /******read file*****/

            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(file));
                
                //header
                reader.readLine();
                
                String[] minC = reader.readLine().split(" ");
                data.bottomCorner.x =  Double.valueOf(minC[1]);
                data.bottomCorner.y =  Double.valueOf(minC[2]);
                data.bottomCorner.z =  Double.valueOf(minC[3]);
                
                String[] maxC = reader.readLine().split(" ");
                data.topCorner.x =  Double.valueOf(maxC[1]);
                data.topCorner.y =  Double.valueOf(maxC[2]);
                data.topCorner.z =  Double.valueOf(maxC[3]);
                
                String[] split = reader.readLine().split(" ");
                
                data.split = new Point3i(Integer.valueOf(split[1]), Integer.valueOf(split[2]), Integer.valueOf(split[3]));
                
                data.resolution.x = (data.topCorner.x - data.bottomCorner.x) / data.split.x;
                data.resolution.y = (data.topCorner.y - data.bottomCorner.y) / data.split.y;
                data.resolution.z = (data.topCorner.z - data.bottomCorner.z) / data.split.z;
                
                //skip offset (to be suppressed)
                reader.readLine();
                
                String[] columnsNames = reader.readLine().split(" ");
                
                int voxelNumber = data.split.x * data.split.y * data.split.z;
                int lineNumber = 0;
                
                Map<String, Float[]> voxels = new LinkedHashMap<>();
                
                for (String columnName : columnsNames) {
                    
                    voxels.put(columnName, new Float[voxelNumber]);
                }
                
                String line;                
                
                //start reading voxels
                while ((line = reader.readLine())!= null) {

                    String[] voxelLine = line.split(" ");                    
                    
                    for (int i=0;i<voxelLine.length;i++) {
                        
                        float value = Float.valueOf(voxelLine[i]);
                        
                        voxels.get(columnsNames[i])[lineNumber] = value;
                    }

                    lineNumber++;
                }
                
                reader.close();
                data.setVoxelsFromMap(voxels);
                //data.voxels = voxels;

            } catch (FileNotFoundException ex) {
                logger.error(null, ex);
            } catch (IOException ex) {
                logger.error(null, ex);
            }
        }
        
        return data;
    }
    
    public static void main(String args[]) {
        
        VoxelSpaceRawData data1 = VoxelSpaceReader.read(new File("C:\\Users\\Julien\\Desktop\\Sortie voxels\\130403_105816.mon.rxp.vox"));
        VoxelSpaceRawData data2 = VoxelSpaceReader.read(new File("C:\\Users\\Julien\\Desktop\\Sortie voxels\\130403_104825.mon.rxp.vox"));
        VoxelSpaceRawData data3 = VoxelSpaceReader.read(new File("C:\\Users\\Julien\\Desktop\\Sortie voxels\\130403_103444.mon.rxp.vox"));
        
        ArrayList<Map<String, Float[]>> datasets = new ArrayList<>();
        datasets.add(data1.getVoxelMap());
        datasets.add(data2.getVoxelMap());
        datasets.add(data3.getVoxelMap());
        
        Map<String, Float[]> mergeAll = DataSet.mergeMultipleDataSet(datasets, 
                new boolean[]{false, false, false, true, true, true, true, true, true, true, true, true, true, true});
        
        Map<String, Float[]> merge = DataSet.mergeTwoDataSet(data1.getVoxelMap(), data2.getVoxelMap(), 
                new boolean[]{false, false, false, true, true, true, true, true, true, true, true, true, true, true});
        
        VoxelSpaceRawData data4 = new VoxelSpaceRawData(data1);
        data4.setVoxelsFromMap(mergeAll);
        
        data4.write(new File("c:\\Users\\Julien\\Desktop\\merge.vox"));
        
        System.out.println("test");
    }
}


