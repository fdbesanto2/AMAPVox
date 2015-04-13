package fr.ird.voxelidar.voxelisation.extraction;

import java.util.ArrayList;
import javax.swing.event.EventListenerList;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Vector3d;
import javax.vecmath.Vector3f;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Julien
 */
public class Shots {
    
    public final String addShotMethodSignature = "(IDDDDDD[D)V";
    private final EventListenerList listeners;
    
    
    public Shots(){
        listeners = new EventListenerList();
    }
    
    public void initialize(){
    }
    
    public void addShotsListener(ShotsListener listener){
        listeners.add(ShotsListener.class, listener);
    }
    
    public void fireShotAdded(Shot shot){
        
        for(ShotsListener listener :listeners.getListeners(ShotsListener.class)){
            
            listener.shotExtracted(shot);
        }
    }
    
    public void addShot(int nbShots, double beam_origin_x, double beam_origin_y, double beam_origin_z,
                        double beam_direction_x,double beam_direction_y,double beam_direction_z, double[] echos){
        
        /*
        Shot shot = new Shot();
        
        shot.nbEchos = nbShots;
        shot.origin = new Point3d(beam_origin_x, beam_origin_y, beam_origin_z);
        shot.direction = new Vector3d(beam_direction_x, beam_direction_y, beam_direction_z);
        
        shot.ranges = echos;
        */
        fireShotAdded(new Shot(nbShots, beam_origin_x, beam_origin_y, beam_origin_z, beam_direction_x, beam_direction_y, beam_direction_z, echos));
        
        //shotList.add(shot);
        
    }
    
    public void addShot(Shot shot){
        
        fireShotAdded(shot);
        //shotList.add(shot);
    }
        
}