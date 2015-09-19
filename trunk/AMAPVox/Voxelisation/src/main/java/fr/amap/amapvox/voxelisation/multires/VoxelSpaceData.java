/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxelisation.multires;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.vecmath.Point2f;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelSpaceData{
    
    public enum Type{
        ALS(1),
        TLS(2);
        
        private final int type;
        Type(int type){
            this.type = type;
        }
    }
    
    private Map<String, Point2f> minMax;
    
    public float minY, maxY;
    public Point3i split;
    public Point3d resolution;
    public float res;
    public Point3d bottomCorner;
    public Point3d topCorner;
    public Type type;
    
    public float maxPad = 5.0f;
    
    public ArrayList<ExtendedALSVoxel> voxels;
    
    public ArrayList<String> attributsNames;
    
    public VoxelSpaceData(){
        
        voxels = new ArrayList<>();
        minMax = new HashMap<>();
        attributsNames = new ArrayList<>();
        split = new Point3i();
        resolution = new Point3d();
        bottomCorner = new Point3d();
        topCorner = new Point3d();
    }

    public Map<String, Point2f> getMinMax() {
        return minMax;
    }

    public void setMinMax(Map<String, Point2f> minMax) {
        this.minMax = minMax;
    }
    
    public ExtendedALSVoxel getVoxel(int i, int j, int k){
        
        if(i > split.x -1 || j > split.y -1 || k > split.z -1){
            return null;
        }
        
        int index = get1DFrom3D(i, j, k);
        
        if(index>voxels.size()-1){
            return null;
        }
        
        return voxels.get(index);
    }
    
    public void setVoxel(int i, int j, int k, ExtendedALSVoxel voxel){
        
        int index = get1DFrom3D(i, j, k);
        
        if(index>voxels.size()-1){
            
        }else{
            voxels.set(index, voxel);
        }
    }
    
    private int get1DFrom3D(int i, int j, int k){
        
        return (i*split.y*split.z) + (j*split.z) +  k;
    }
}
