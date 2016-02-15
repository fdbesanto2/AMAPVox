/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.postproc;

import fr.amap.commons.math.point.Point3I;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class ButterflyRemover {
    
    private final static Logger LOGGER = Logger.getLogger(ButterflyRemover.class);
    
    /**
     * Clean a voxel space of isolated voxels (butterflies, artifacts...) from a voxel file.
     * @see #clean(fr.amap.lidar.amapvox.commons.Voxel[][][]) 
     * @param voxelFile
     * @throws Exception 
     */
    public static void clean(File voxelFile, File outputFile) throws Exception{
        
        Voxel[][][] voxels;
        
        VoxelFileReader reader = new VoxelFileReader(voxelFile);
        VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

        voxels = new Voxel[infos.getSplit().x][infos.getSplit().y][infos.getSplit().z];

        Iterator<fr.amap.lidar.amapvox.commons.Voxel> iterator = reader.iterator();


        while(iterator.hasNext()){
            Voxel voxel = iterator.next();

            voxels[voxel.$i][voxel.$j][voxel.$k] = voxel;                
        }
        
        Voxel[][][] voxelSpaceCleaned = clean(voxels);
        
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
        
        writer.write(infos.toString()+"\n");
        
        for (int i = 0; i < infos.getSplit().x; i++) {
            for (int j = 0; j < infos.getSplit().y; j++) {
                for (int k = 0; k < infos.getSplit().z; k++) {
                    writer.write(voxelSpaceCleaned[i][j][k].toString()+"\n");
                }
            }
        }
        
        writer.close();
    }
    
    /**
     * <p>Clean a voxel space of isolated voxels (butterflies, artifacts...)
     * For each voxel we count the number of empty neighbors voxels in a Moore neighborhood of rank 1.</p>
     * If the neighborhood is completely empty (based on the echo number), then the voxel is cleaned 
     * (its attributes are changed: nbEchos = 0, bvIntercepted = 0, PadBVTotal = 0, transmittance = 1).
     * @param voxels Three-dimensional array of voxels representing the voxel space
     * @return The cleaned voxel space
     * @throws Exception 
     */
    public static Voxel[][][] clean(Voxel[][][] voxels) throws Exception{
        
        int neighboursRange = 1;
        
        List<Point3I> badVoxels = new ArrayList<>();

        if(voxels.length == 0 || voxels[0].length == 0 || voxels[0][0].length == 0){
            throw new Exception("Voxel space has an invalid number of voxels on one axis.");
        }
        
        int xSplit = voxels.length;
        int ySplit = voxels[0].length;
        int zSplit = voxels[0][0].length;
        
        for (int i = 0; i < voxels.length; i++) {
            for (int j = 0; j < voxels[i].length; j++) {
                for (int k = 0; k < voxels[i][j].length; k++) {
                    
                    if(voxels[i][j][k].nbEchos >= 1){
                        badVoxels.add(new Point3I(i, j, k));
                    }
                }
            }
        }

        int count = 0;

        for (Point3I badVoxel : badVoxels) {

            int minX = Integer.max(badVoxel.x-neighboursRange, 0);
            int minY = Integer.max(badVoxel.y-neighboursRange, 0);
            int minZ = Integer.max(badVoxel.z-neighboursRange, 0);

            int maxX = Integer.min(badVoxel.x+neighboursRange, xSplit-1);
            int maxY = Integer.min(badVoxel.y+neighboursRange, ySplit-1);
            int maxZ = Integer.min(badVoxel.z+neighboursRange, zSplit-1);

            int emptyNeighborsNumber = 0;
            int nbNeighbors = 0;

            for(int i = minX ; i<= maxX ; i++){
                for(int j = minY ; j<= maxY ; j++){
                    for(int k = minZ ; k<= maxZ ; k++){

                        if (!(i == badVoxel.x && j == badVoxel.y && k == badVoxel.z)) {
                            if(voxels[i][j][k].nbEchos == 0){
                                emptyNeighborsNumber++;
                            }

                            nbNeighbors++;
                        }
                    }
                }
            }

            float ratio = emptyNeighborsNumber / (float)nbNeighbors;

            if(emptyNeighborsNumber == nbNeighbors){

                count++;
                
                voxels[badVoxel.x][badVoxel.y][badVoxel.z].nbEchos = 0;
                voxels[badVoxel.x][badVoxel.y][badVoxel.z].bvIntercepted = 0;
                voxels[badVoxel.x][badVoxel.y][badVoxel.z].PadBVTotal = 0;
                voxels[badVoxel.x][badVoxel.y][badVoxel.z].transmittance = 1;
            }
        }

        LOGGER.info("Nb butterflies : "+count);
        
        return voxels;
    }
    
    //moore neighborhood
//    public List<Point3I> getIndicesFromPassID(Point3I middleID, int passID, Point3I minLimit, Point3I maxLimit){
//        
//        
//        List<Point3I> indices = new ArrayList<>();
//        
//        int minI = Math.max(middleID.x - passID, minLimit.x);
//        int maxI = Math.min(middleID.x + passID, maxLimit.x);
//        int minJ = Math.max(middleID.y - passID, minLimit.y);
//        int maxJ = Math.min(middleID.y + passID, maxLimit.y);
//        int minK = Math.max(middleID.z - passID, minLimit.z);
//        int maxK = Math.min(middleID.z + passID, maxLimit.z);
//        
//        for (int i = minI; i <= maxI; i++) {
//            for (int j = minJ; j <= maxJ; j++) {
//                indices.add(new Point3I(i, j, minK));
//                indices.add(new Point3I(i, j, maxK));
//            }
//        }
//
//        for (int k = minK; k <= maxK; k++) {
//            for (int j = minJ+1; j <= maxJ-1; j++) {
//
//                indices.add(new Point3I(minI, j, k));
//                indices.add(new Point3I(maxI, j, k));
//            }
//        }
//
//        for (int k = minK+1; k <= maxK-1; k++) {
//            for (int i = minI + 1; i <= maxI - 1; i++) {
//                indices.add(new Point3I(i, minJ, k));
//                indices.add(new Point3I(i, maxJ, k));
//            }
//        }
//        
//        for (int k = minK+1; k <= maxK-1; k++) {
//            for (int i = minI; i <= maxI; i++) {
//                for (int j = minJ+1; j <= maxJ-1; j++) {
//                   indices.add(new Point3I(i, j, k));
//                   indices.add(new Point3I(i, j, k));
//                }
//            }
//        }
//        
//        return indices;
//    }
//    
//    //moore neighborhood
//    public List<Point3I> getIndicesFromPassIDV2(Point3I middleID, int passID, Point3I minLimit, Point3I maxLimit){
//        
//        
//        List<Point3I> indices = new ArrayList<>();
//        
//        int minI = Math.max(middleID.x - passID, minLimit.x);
//        int maxI = Math.min(middleID.x + passID, maxLimit.x);
//        int minJ = Math.max(middleID.y - passID, minLimit.y);
//        int maxJ = Math.min(middleID.y + passID, maxLimit.y);
//        int minK = Math.max(middleID.z - passID, minLimit.z);
//        int maxK = Math.min(middleID.z + passID, maxLimit.z);
//                
//        for (int x = -passID; x <= passID; ++x) {
//            int r_x = passID - Math.abs(x);
//            for (int y = -r_x; y <= r_x; ++y) {
//                int r_y = r_x - Math.abs(y);
//                for (int z = -r_y; z <= r_y; ++z) {
//                    System.out.println(x + " " + y + " " + z);
//                }
//            }
//        }
//        
//        return indices;
//    }  
}
