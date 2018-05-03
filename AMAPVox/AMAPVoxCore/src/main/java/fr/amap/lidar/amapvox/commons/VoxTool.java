/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.commons;

import fr.amap.commons.math.point.Point3I;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to perform different things on voxels 
 * @author calcul
 */
public class VoxTool {
    
    /**
     * <p>This method get the number of neighbors of a given indice in the axis.</p>
     * <p>Indices are bounded from 0 to split number minus 1 </p>
     * @param passID moore neighborhood range
     * @param voxelID axis indice
     * @param splitNumber voxel number in the axis, is a maximum/limit
     * @return the number of neighbours in the axis
     */
    public static int getNeighboursNumberForAxis(int passID, int voxelID, int splitNumber){
        
        /**number of possible voxel including borders**/
       int nLeft = Integer.min(passID, voxelID);
       int nRight = Integer.min(passID, splitNumber - voxelID - 1);
       
       int width = nLeft+nRight+1;
       
       return width;
    }
    
    /**
     * <p>This method get the number of neighbors of a given voxel.</p>
     * <p>Neighbours are bounded from 0 to split number minus 1 </p>
     * @param passID moore neighborhood range
     * @param voxelXId voxel indice in x axis
     * @param voxelYId voxel indice in y axis
     * @param voxelZId voxel indice in z axis
     * @param splitX voxel number in x axis
     * @param splitY voxel number in y axis
     * @param splitZ voxel number in z axis
     * @return the number of neighbours of the given voxel
     */
    public static int getNeighboursNumber(int passID, int voxelXId, int voxelYId, int voxelZId, int splitX, int splitY, int splitZ){
        
        int neighboursNumberInXAxis = getNeighboursNumberForAxis(passID, voxelXId, splitX);
        int neighboursNumberInYAxis = getNeighboursNumberForAxis(passID, voxelYId, splitY);
        int neighboursNumberInZAxis = getNeighboursNumberForAxis(passID, voxelZId, splitZ);
        
        int neighboursNumber = (neighboursNumberInXAxis * neighboursNumberInYAxis * neighboursNumberInZAxis) - 1;
        
        return neighboursNumber;
    }
    
    private List<Point3I> getNeighboursList(int passID, int voxelXId, int voxelYId, int voxelZId, int splitX, int splitY, int splitZ){
        
        List<Point3I> neighbours = new ArrayList<>();
        
        int xMin = Integer.max(voxelXId-passID, 0);
        int xMax = Integer.min(voxelXId+passID, splitX-1);
        
        int yMin = Integer.max(voxelYId-passID, 0);
        int yMax = Integer.min(voxelYId+passID, splitY-1);
        
        int zMin = Integer.max(voxelZId-passID, 0);
        int zMax = Integer.min(voxelZId+passID, splitZ-1);
        
        int previousxMin = Integer.max(voxelXId-(passID-1), 0);
        int previousxMax = Integer.min(voxelXId+(passID-1), splitX-1);
        
        int previousyMin = Integer.max(voxelYId-(passID-1), 0);
        int previousyMax = Integer.min(voxelYId+(passID-1), splitY-1);
        
        int previouszMin = Integer.max(voxelZId-(passID-1), 0);
        int previouszMax = Integer.min(voxelZId+(passID-1), splitZ-1);
        
        //couches horizontales
        for (int x=xMin;x<=xMax;x++){
            for (int y=yMin;y<=yMax;y++){
                
                if(zMin != previouszMin){
                    neighbours.add(new Point3I(x, y, zMin));
                }
                
                if(zMax != previouszMax){
                    neighbours.add(new Point3I(x, y, zMax));
                }
            }
        }
        
        if(previouszMax != zMax){
            zMax -= 1;
        }
        
        if(previouszMin != zMin){
            zMin += 1;
        }
        
        //couches verticales
        for (int y=yMin;y<=yMax;y++){
            
            for (int z=zMin;z<=zMax;z++){
                
                if(xMin != previousxMin){
                    neighbours.add(new Point3I(xMin, y, z));
                }
                
                if(xMax != previousxMax){
                    neighbours.add(new Point3I(xMax, y, z));
                }
                
            }
        }
        
        if(previousxMin != xMin){
            xMin += 1;
        }
        
        if(previousxMax != xMax){
            xMax -= 1;
        }
        
        //couches latérales
        for (int x=xMin;x<=xMax;x++){
            for (int z=zMin;z<=zMax;z++){
                
                if(yMin != previousyMin){
                    neighbours.add(new Point3I(x, yMin, z));
                }

                if(yMax != previousyMax){
                    neighbours.add(new Point3I(x, yMax, z));
                }
            }
        }
        
        return neighbours;
    }
    
    /**
     *
     * @param voxel
     * @param passID
     * @param splitX
     * @param splitY
     * @param splitZ
     * @return
     */
//    public static Iterator getMooreNeighboroodIterator(Voxel voxel, int passID, int splitX, int splitY, int splitZ){
//        
//        return new Iterator() {
//
//            int neighboursNumber = getNeighboursNumber(passID, voxel.$i, voxel.$j, voxel.$k, splitX, splitY, splitZ)-
//                                   getNeighboursNumber(passID-1, voxel.$i, voxel.$j, voxel.$k, splitX, splitY, splitZ);
//            
//            int xMin = Integer.max(voxel.$i-passID, 0);
//            int xMax = Integer.min(voxel.$i+passID, splitX-1);
//
//            int yMin = Integer.max(voxel.$j-passID, 0);
//            int yMax = Integer.min(voxel.$j+passID, splitY-1);
//
//            int zMin = Integer.max(voxel.$k-passID, 0);
//            int zMax = Integer.min(voxel.$k+passID, splitZ-1);
//
//            int previousxMin = Integer.max(voxel.$i-(passID-1), 0);
//            int previousxMax = Integer.min(voxel.$i+(passID-1), splitX-1);
//
//            int previousyMin = Integer.max(voxel.$j-(passID-1), 0);
//            int previousyMax = Integer.min(voxel.$j+(passID-1), splitY-1);
//
//            int previouszMin = Integer.max(voxel.$k-(passID-1), 0);
//            int previouszMax = Integer.min(voxel.$k+(passID-1), splitZ-1);
//            
//            @Override
//            public boolean hasNext() {
//                
//            }
//
//            @Override
//            public Voxel next() {
//                
//            }
//        };
//    }
}
