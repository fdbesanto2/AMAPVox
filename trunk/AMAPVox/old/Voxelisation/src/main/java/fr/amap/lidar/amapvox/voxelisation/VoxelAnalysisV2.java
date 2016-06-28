/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxelisation;

import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineElement;
import fr.amap.lidar.amapvox.jeeb.raytracing.geometry.LineSegment;
import fr.amap.lidar.amapvox.jeeb.raytracing.util.BoundingBox3d;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.Scene;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManager;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManager.VoxelCrossingContext;
import fr.amap.lidar.amapvox.jeeb.raytracing.voxel.VoxelManagerSettings;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import javax.vecmath.Point3d;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien
 */
public class VoxelAnalysisV2 implements IVoxelAnalysis{

    private final VoxelAnalysisCfg cfg;
    private final VoxelManager voxelManager;
    private final VoxelParameters parameters;

    public VoxelAnalysisV2(VoxelAnalysisCfg cfg) {
        
        parameters = cfg.getVoxelParameters();
        this.cfg = cfg;
        
        Scene scene = new Scene();
        scene.setBoundingBox(new BoundingBox3d(parameters.infos.getMinCorner(), parameters.infos.getMaxCorner()));

        voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.infos.getSplit(), VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));
    }
    
    @Override
    public void processOneShot(Shot shot) {
        
        if(shot.nbEchos > 0){
            
            LineElement lineElement = new LineSegment(shot.origin, getEchoLocation(shot, 0));
            
            VoxelCrossingContext context = voxelManager.getFirstVoxel(lineElement);
            if(context == null){
                return;
            }
            
            //cumulated distance
            double cumulatedDist = 0;
            Point3i oldVoxel;
            Point3i currentVoxel = null;
            double currentLength = 0;
            double oldDist;
                
            for(int currentEcho=0;currentEcho<shot.ranges.length;currentEcho++){ //for each echo
                
                //distance from source to current echo
                double distanceToHit = shot.ranges[currentEcho]; 
                
                while(context.indices != null && cumulatedDist < distanceToHit){
                    
                    currentVoxel = context.indices;
                    
                    oldDist = context.length;
                    context = voxelManager.CrossVoxel(shot.origin, shot.direction, currentVoxel);
                    
                    cumulatedDist = context.length;
                    
                    double currentDist = cumulatedDist - oldDist;
                    currentLength = currentDist - (cumulatedDist - distanceToHit);
                    
                    if(currentLength < 0){
                        currentLength = currentDist;
                    }
                }
                
                System.out.println("Echo N° : "+currentEcho+"\tCurrent voxel : "+currentVoxel.x+" "+currentVoxel.y+" "+currentVoxel.z);
            }
            
        }else{
            
        }
        
    }
    
    private Point3d getEchoLocation(Shot shot, int indice) {

        LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[indice]);
        return seg.getEnd();
    }
    
}
