package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

import fr.ird.jeeb.workspace.archimedes.geometry.LineElement;
import fr.ird.jeeb.workspace.archimedes.geometry.LineSegment;
import fr.ird.jeeb.lib.structure.geometry.util.BoundingBox3f;
import fr.ird.jeeb.workspace.archimedes.raytracing.voxel.VoxelManager.VoxelCrossingContext;

public class VoxelAnalysis {

    private VoxelSpace voxSpace;
    private TLSVoxel voxels[][][] = null;
    private VoxelManager voxelManager;
    private BoundingBox3f bbox = new BoundingBox3f();
    private Point3f origin = new Point3f();
    
    float[][] weighting;
    
    private VoxelParameters parameters;

    public class TLSVoxel {

        int nbSampling = 0;			// number of rays crossing the voxel
        float sampledVolume = 0;		// 
        float interceptions = 0;
        float hiddenVolume = 0;
        float lgTraversant = 0;
        float lgInterception = 0;
        float pathLength = 0;			// total path length of rays in voxel
        float PAD = 0;
    }
    
    public VoxelAnalysis(){
        
    }
    
    public void init(VoxelParameters parameters){
        
        this.parameters = parameters;
        
        switch(parameters.getWeighting()){
            
            case VoxelParameters.WEIGHTING_ECHOS_NUMBER:
                
                weighting = new float[][]{{1.00f,0.00f,0.00f,0.00f,0.00f,0.00f,0.00f},
                                        {0.62f,0.38f,0.00f,0.00f,0.00f,0.00f,0.00f},
                                        {0.40f,0.35f,0.25f,0.00f,0.00f,0.00f,0.00f},
                                        {0.28f,0.29f,0.24f,0.19f,0.00f,0.00f,0.00f},
                                        {0.21f,0.24f,0.21f,0.19f,0.15f,0.00f,0.00f},
                                        {0.16f,0.21f,0.19f,0.18f,0.14f,0.12f,0.00f},
                                        {0.15f,0.17f,0.15f,0.16f,0.12f,0.19f,0.06f}};
                
                break;
                
            case VoxelParameters.WEIGHTING_NONE:
                
                weighting = new float[][]{{1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f},
                                        {1.00f,1.00f,1.00f,1.00f,1.00f,1.00f,1.00f}};
                
                break;
                
            case VoxelParameters.WEIGHTING_FILE:
                
                //read file
                File weightingFile = parameters.getWeightingFile();
                
                break;
            
        }
        
        createVoxelSpace();
        
    }
    
    public void voxelise(Shot shot){
        
        shot.direction.normalize();
        origin = new Point3f(shot.origin);
        
        if(parameters.getWeighting() == VoxelParameters.WEIGHTING_ECHOS_NUMBER){
            
        }
        
        if (shot.nbEchos == 0) {
            
            origin = new Point3f(shot.origin);
            //origin = new Point3f();
            
            LineSegment seg = new LineSegment(shot.origin, shot.direction, 999999);
            //LineSegment seg = new LineSegment(new Point3f(), shot.direction, 999999);

            Point3f echo = seg.getEnd();
            propagate(origin, echo);
            
        } else {
            
            for (int i = 0; i < shot.nbEchos; i++) {
                
                LineSegment seg = new LineSegment(shot.origin, shot.direction, shot.ranges[i]);
                //LineSegment seg = new LineSegment(new Point3f(), shot.direction, shot.ranges[i]);
                Point3f echo = seg.getEnd();
                
                bbox.update(echo);
                
                // propagate
                propagate(origin, echo);
                origin = new Point3f(echo);
            }
        }
    }
    
    private void propagate(Point3f origin, Point3f shot) {

        LineElement lineElement = new LineSegment(origin, shot);

        VoxelCrossingContext context = voxelManager.getFirstVoxel(lineElement);

        float distanceToHit = shot.distance(origin);
        
        while ((context != null) && (context.indices != null)) {

            float d1 = context.length;// distance to the point where the ray enter the voxel
            Point3i indices = context.indices;
            context = voxelManager.CrossVoxel(lineElement, context.indices);
            
            float d2 = context.length;// maximal path length of the ray within the voxel

            TLSVoxel vox = voxels[indices.x][indices.y][indices.z];

            // voxel sampled without hit
            if (d2 < distanceToHit) {
                
                vox.lgTraversant += (d2 - d1);
                vox.pathLength += (d2 - d1);
                vox.nbSampling++;
                
            }else if (d1>distanceToHit) {
                //no comprende
            }else {
                
                /*si plusieurs échos issus du même tir dans le voxel, 
                on incrémente et le nombre de tirs entrants (nbsampling) 
                et le nombre d'interception (interceptions) 
                et la longueur parcourue(lgInterception)*/
                
                vox.nbSampling++;
                vox.pathLength += distanceToHit - d1;
                vox.interceptions ++;
                
                vox.lgInterception += distanceToHit - d1;
                //vox.pathLength += distanceToHit - d1;
                
                Point3f echo = new Point3f(lineElement.getDirection());
                echo.scale(distanceToHit);
                       
            }
            
            
        }
    }
    
    public void calculatePAD(float threshold){
        
        for (int x = 0; x < parameters.split.x; x++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int z = 0; z < parameters.split.z; z++) {
                        
                        TLSVoxel vox = voxels[x][y][z];
                        
                        float PAD;
                        
                        if(vox.nbSampling <= threshold){
                            
                            PAD = -1;
                            
                        }else if(vox.interceptions >= vox.nbSampling){
                            
                            PAD = 10;
                            
                        }else{
                            PAD = (float) ((Math.log((vox.nbSampling - vox.interceptions)/vox.nbSampling))/(-0.5*vox.lgTraversant/(vox.nbSampling-vox.interceptions)));
                            
                            if(PAD > 10){
                                PAD = 10;
                            }
                        }
                        
                        
                        vox.PAD = PAD+0.0f;
                    }
                }
        }
        
    }
    
    private void createVoxelSpace() {

        voxSpace = new VoxelSpace(new BoundingBox3f(parameters.bottomCorner, parameters.topCorner), parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY);

        // allocate voxels
        System.out.println("allocate!!!!!!!!");
        
        voxels = new TLSVoxel[parameters.split.x][parameters.split.y][parameters.split.z];
        
        for (int x = 0; x < parameters.split.x; x++) {
            for (int y = 0; y < parameters.split.y; y++) {
                for (int z = 0; z < parameters.split.z; z++) {
                    
                    voxels[x][y][z] = new TLSVoxel();
                }
            }
        }

        Scene scene = new Scene();
        scene.setBoundingBox(new BoundingBox3f(parameters.bottomCorner, parameters.topCorner));

        voxelManager = new VoxelManager(scene, new VoxelManagerSettings(parameters.split, VoxelManagerSettings.NON_TORIC_FINITE_BOX_TOPOLOGY));

        voxelManager.showInformations();
    }

    public void writeOutput(File outputFile) {

        Point3f dimVox = voxSpace.getVoxelSize();
        float voxelVolume = (dimVox.x * dimVox.y * dimVox.z);

        try {
            // Create file 
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile));
            
            writer.write("VOXEL SPACE"+"\n");
            writer.write("#min_corner: "+voxSpace.getBoundingBox().min.x+" "+voxSpace.getBoundingBox().min.y+" "+voxSpace.getBoundingBox().min.z+"\n");
            writer.write("#max_corner: "+voxSpace.getBoundingBox().max.x+" "+voxSpace.getBoundingBox().max.y+" "+voxSpace.getBoundingBox().max.z+"\n");
            writer.write("#split: "+voxSpace.getSplitting().x+" "+voxSpace.getSplitting().y+" "+voxSpace.getSplitting().z+"\n");
            
            Point3f offset = new Point3f();
            writer.write("#offset: "+offset.x+" "+offset.y+" "+offset.z+"\n");
            
            //writer.write("i j k shots path_length BFintercepted BFentering BSintercepted BSentering PAD"+"\n");
            writer.write("i j k nbSampling interceptions path_length lgTraversant lgInterception PAD"+"\n");
            
            for (int x = 0; x < parameters.split.x; x++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int z = 0; z < parameters.split.z; z++) {
                        
                        TLSVoxel vox = voxels[x][y][z];
                        
                        float BFIntercepted = 0.0f;
                        float BFentering = 0.0f;
                        float BSintercepted = 0.0f;
                        float BSentering = 0.0f;
                        float PAD = 0.0f;
                        
                        //writer.write(x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.pathLength+" "+BFIntercepted+" "+BFentering+" "+BSintercepted+" "+BSentering+" "+PAD+"\n");
                        writer.write(x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.interceptions+" "+vox.pathLength+" "+vox.lgTraversant+" "+vox.lgInterception+" "+vox.PAD+"\n");
                        
                    }
                }
            }

            writer.close();
        } catch (Exception e) {	//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

}
