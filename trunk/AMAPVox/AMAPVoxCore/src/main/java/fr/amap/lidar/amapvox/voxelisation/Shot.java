/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class Shot {
    
    public Point3d origin;
    public Vector3d direction;
    public double ranges[];
    private boolean[] mask;
    private double angle = Double.NaN;

    public Shot(Point3d origin, Vector3d direction, double ranges[]) {
        this.origin = origin;
        this.direction = direction;
        this.ranges = ranges;
    }
    
    public void setMask(boolean[] mask){
        this.mask = mask;
    }
    
    public int getEchoesNumber(){
        return ranges == null ? 0 : ranges.length;
    }
    
    private void calculateAngle() {
        
        this.angle = Math.toDegrees(Math.acos(Math.abs(direction.z)));
    }

    public double getAngle() {
        
        if(Double.isNaN(angle)){
            calculateAngle();
        }
        
        return angle;
    }

    public boolean[] getMask() {
        return mask;
    }
}
