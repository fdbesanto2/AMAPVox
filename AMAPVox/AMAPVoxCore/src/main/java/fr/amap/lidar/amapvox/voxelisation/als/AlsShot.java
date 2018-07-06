/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation.als;

import fr.amap.lidar.amapvox.shot.Shot;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class AlsShot extends Shot{
    
    public float intensities[];
    public int classifications[];
    public double time;
    
    public AlsShot(int index, Point3d origin, Vector3d direction, double[] ranges) {
        super(index, origin, direction, ranges);
    }
    
    public AlsShot(int index, Point3d origin, Vector3d direction, double[] ranges, int[] classifications, float[] intensities) {
        super(index, origin, direction, ranges);
        this.classifications = classifications;
        this.intensities = intensities;
    }
}
