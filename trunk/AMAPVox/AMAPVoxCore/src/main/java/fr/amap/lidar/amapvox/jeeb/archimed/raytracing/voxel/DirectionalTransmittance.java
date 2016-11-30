/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.jeeb.archimed.raytracing.voxel;

import fr.amap.lidar.amapvox.commons.LeafAngleDistribution;
import fr.amap.lidar.amapvox.jeeb.archimed.raytracing.util.BoundingBox3d;
import fr.amap.lidar.amapvox.commons.Voxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3d;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

/**
 * Get ray transmittance from a position to a direction into a sampled voxel space.
 * @author main :Jean Dauzat ; co: Julien Heurtebize
 */
public class DirectionalTransmittance {
    
    private final static Logger logger = Logger.getLogger(DirectionalTransmittance.class);
    
    private final Point3d min;
    private final Point3d max;
    
    private final Point3d voxSize;
    private final Point3i splitting;
    
    private final VoxelSpaceInfos infos;
    private final VoxelSpace voxSpace;
    
    private TLSVoxel voxels[][][];
    private float mnt[][];
    
    private fr.amap.lidar.amapvox.commons.DirectionalTransmittance direcTrans;
    
    private boolean toricity = false;
    private final static double EPSILON = 0.001;
    
    private class TLSVoxel {

        float padBV;
    }

    /**
     * 
     * @param inputFile voxel file
     * @throws Exception 
     */
    public DirectionalTransmittance(File inputFile) throws Exception {
        
        VoxelFileReader reader = new VoxelFileReader(inputFile);
        infos = reader.getVoxelSpaceInfos();
        
        min = infos.getMinCorner();
        max = infos.getMaxCorner();
        splitting = infos.getSplit();
        
        voxSize = new Point3d();
        voxSize.x = (max.x - min.x) / (double) splitting.x;
        voxSize.y = (max.y - min.y) / (double) splitting.y;
        voxSize.z = (max.z - min.z) / (double) splitting.z;
        
        logger.info(infos.toString()+"\n");
        
        direcTrans = new fr.amap.lidar.amapvox.commons.DirectionalTransmittance(new LeafAngleDistribution(infos.getLadType(), infos.getLadParams()));
        direcTrans.buildTable(180);
        
        voxSpace = new VoxelSpace(new BoundingBox3d(min, max), splitting, 0);
        
        createVoxelTable();
        allocateMNT();
        
        Iterator<Voxel> iterator = reader.iterator();
        
        while(iterator.hasNext()){
            
            Voxel voxel = iterator.next();
            
            if(voxel != null){
                if (voxel.$k == 0) {
                    mnt[voxel.$i][voxel.$j] = (float) (/*min.z - */voxel.ground_distance);
                }

                /*if (Float.isNaN(voxel.PadBVTotal)) {
                    voxels[voxel.$i][voxel.$j][voxel.$k].padBV = 0;
                } else {*/
                    voxels[voxel.$i][voxel.$j][voxel.$k].padBV = voxel.PadBVTotal;
                //}
            }else{
                logger.warn("Voxel null");
            }
        }
        
    }
    
    private List<Double> distToVoxelWallsV2(Point3d origin, Vector3d direction) {

        // point where the ray exits from the top of the bounding box
        
        double distToTop = (max.z - origin.z) / direction.z;

        List<Double> distances = new ArrayList<>();
        
        distances.add(distToTop);

        // voxel walls in X
       
        if(direction.x != 0){
            double deltaX = min.x - origin.x;
            deltaX -= voxSize.x * ((int)(deltaX/voxSize.x));

            if(direction.x > 0){
                deltaX = voxSize.x - deltaX;
            }

            double dist = Math.abs(deltaX / direction.x);
            distances.add(dist);
            
            double dX = Math.abs(voxSize.x / direction.x);
            while(dist < distToTop){
                //current distance
                dist += dX;
                distances.add(dist);
            }
        }
        
        // voyel walls in Y
       
        if(direction.y != 0){
            double deltaY = min.y - origin.y;
            deltaY -= voxSize.y * ((int)(deltaY/voxSize.y));

            if(direction.y > 0){
                deltaY = voxSize.y - deltaY;
            }

            double dist = Math.abs(deltaY / direction.y);
            distances.add(dist);
            
            double dY = Math.abs(voxSize.y / direction.y);
            while(dist < distToTop){
                //current distance
                dist += dY;
                distances.add(dist);
            }
        }
        
        // vozel walls in Z
       
        if(direction.z != 0){
            double deltaZ = min.z - origin.z;
            deltaZ -= voxSize.z * ((int)(deltaZ/voxSize.z));

            if(direction.z > 0){
                deltaZ = voxSize.z - deltaZ;
            }

            double dist = Math.abs(deltaZ / direction.z);
            distances.add(dist);
            
            double dZ = Math.abs(voxSize.z / direction.z);
            while(dist < distToTop){
                //current distance
                dist += dZ;
                distances.add(dist);
            }
        }

        Collections.sort(distances);

        return distances;
    }
 
    private List<Double> distToVoxelWalls(Point3d origin, Vector3d direction) {

        // point where the ray exits from the top of the bounding box
        
        Point3d exit = new Point3d(direction);
        double dist = (max.z - origin.z) / direction.z;
        exit.scale(dist);
        exit.add(origin);

        Point3i originVoxel = new Point3i((int) ((origin.x - min.x) / voxSize.x), (int) ((origin.y - min.y) / voxSize.y), (int) ((origin.z - min.z) / voxSize.z));
        Point3i exitVoxel = new Point3i((int) ((exit.x - min.x) / voxSize.x), (int) ((exit.y - min.y) / voxSize.y), (int) ((exit.z - min.z) / voxSize.z));

        List<Double> distances = new ArrayList<>();

        Vector3d oe = new Vector3d(exit);
        oe.sub(origin);
        distances.add(0.0);
        distances.add(oe.length());

        // voxel walls in X
        int minX = Math.min(originVoxel.x, exitVoxel.x);
        int maxX = Math.max(originVoxel.x, exitVoxel.x);
        for (int m = minX; m < maxX; m++) {
            double dx = (m + 1) * voxSize.x;
            dx += min.x - origin.x;
            dx /= direction.x;
            distances.add(dx);
        }

        // voxel walls in Y
        int minY = Math.min(originVoxel.y, exitVoxel.y);
        int maxY = Math.max(originVoxel.y, exitVoxel.y);
        for (int m = minY; m < maxY; m++) {
            double dy = (m + 1) * voxSize.y;
            dy += min.y - origin.y;
            dy /= direction.y;
            distances.add(dy);
        }

        // voxel walls in Z
        int minZ = Math.min(originVoxel.z, exitVoxel.z);
        int maxZ = Math.max(originVoxel.z, exitVoxel.z);
        for (int m = minZ; m < maxZ; m++) {
            double dz = (m + 1) * voxSize.z;
            dz += min.z - origin.z;
            dz /= direction.z;
            distances.add(dz);
        }

        Collections.sort(distances);

        return distances;
    }
    
    public double directionalTransmittance(Point3d origin, Vector3d direction) {
        
        List<Double> distances = distToVoxelWallsV2(origin, direction);
        
        //we can optimize this by storing the angle value to avoid repeating this for each position
        double directionAngle = FastMath.toDegrees(FastMath.acos(direction.z)); 
        
        double dMoy;
        Point3d pMoy;

        double d1 = 0;
        double transmitted = 1;
        for (Double d2 : distances) {
            double pathLength = d2 - d1;
            dMoy = (d1 + d2) / 2.0;
            pMoy = new Point3d(direction);
            pMoy.scale(dMoy);
            pMoy.add(origin);
            pMoy.sub(min);
            
            int i = (int) Math.floor(pMoy.x / voxSize.x);
            int j = (int) Math.floor(pMoy.y / voxSize.y);
            int k = (int) Math.floor(pMoy.z / voxSize.z);
            
            if (i < 0 || j < 0 || k < 0 || i >= splitting.x || j >= splitting.y || k >= splitting.z) {
                
                if(toricity){
                    
                    while(i < 0){i += splitting.x;}
                    while(j < 0){j += splitting.y; }
                    while (i >= splitting.x) { i -= splitting.x;}
                    while (j >= splitting.y) { j -= splitting.y;}
                    
                    if(k < 0 || k>= splitting.z){
                        break;
                    }
                    
                }else{
                    break;
                }
            }
            
            // Test if current voxel is below the ground level
            if (pMoy.z < mnt[i][j]) {
                transmitted = 0;
            } else {
                if(Float.isNaN(voxels[i][j][k].padBV)){
                    //test
                    //voxels[i][j][k].padBV = 3.536958f;
                    return Double.NaN;
                }
                
                float coefficientGTheta = (float) direcTrans.getTransmittanceFromAngle(directionAngle, true);
                
                //input transmittance
                //double transmittedBefore = transmitted;
                
                transmitted *= Math.exp(-coefficientGTheta * voxels[i][j][k].padBV * pathLength);
                
                //output transmittance
                //double transmittedAfter = transmitted;
                
                //intercepted transmittance
                //double interceptedTrans = transmittedAfter - transmittedBefore;
                
                //transmitted *= Math.exp(-0.5 * voxels[i][j][k].padBV * pathLength)/*(default coeff)*/;
            }
            
            if(transmitted <= EPSILON && toricity){
                break;
            }
            
            d1 = d2;
        }

        return transmitted;
    }
    
    private void allocateMNT() {

        // allocate MNT
        logger.info("allocate MNT");
        mnt = new float[splitting.x][];
        for (int x = 0; x < splitting.x; x++) {
            mnt[x] = new float[splitting.y];
            for (int y = 0; y < splitting.y; y++) {
                mnt[x][y] = (float) min.z;
            }
        }
    }
    
    private void createVoxelTable() {

        // allocate voxels
        logger.info("allocate Voxels");
        voxels = new TLSVoxel[splitting.x][][];
        for (int x = 0; x < splitting.x; x++) {
            voxels[x] = new TLSVoxel[splitting.y][];
            for (int y = 0; y < splitting.y; y++) {
                voxels[x][y] = new TLSVoxel[splitting.z];
                for (int z = 0; z < splitting.z; z++) {
                    voxels[x][y][z] = new TLSVoxel();
                }
            }
        }
    }

    public VoxelSpace getVoxSpace() {
        return voxSpace;
    }

    public float[][] getMnt() {
        return mnt;
    }  

    public VoxelSpaceInfos getInfos() {
        return infos;
    }

    public boolean isToricity() {
        return toricity;
    }

    public void setToricity(boolean toricity) {
        this.toricity = toricity;
    }
}
