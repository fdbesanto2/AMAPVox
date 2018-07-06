/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.shot;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class Shot {
    
    public final int index;
    public Point3d origin;
    public Vector3d direction;
    public double ranges[];
    private boolean[] mask;
    private double angle = Double.NaN;

    public Shot(int index, Point3d origin, Vector3d direction, double ranges[]) {
        this.index = index;
        this.origin = origin;
        this.direction = direction;
        this.ranges = ranges;
    }
    
    /**
     * Copy constructor
     * @param shot the shot to copy
     */
    public Shot(Shot shot) {
        this.index = shot.index;
        this.origin = new Point3d(shot.origin);
        this.direction = new Vector3d(shot.direction);
        this.angle = shot.angle;
        
        if(shot.ranges != null){
            this.ranges = new double[shot.ranges.length];

            System.arraycopy(shot.ranges, 0, this.ranges, 0, shot.ranges.length);
        }
        
        if(shot.mask != null){
            this.mask = new boolean[shot.mask.length];

            System.arraycopy(shot.mask, 0, this.mask, 0, shot.mask.length);
        }
        
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
    
    public boolean isEmpty(){
        
        if(ranges == null){
            return true;
        }else{
            return ranges.length == 0;
        }
    }
    
    public double getFirstRange(){
        if(isEmpty()){
            return Double.NaN;
        }else{
            return ranges[0];
        }
    }
}
