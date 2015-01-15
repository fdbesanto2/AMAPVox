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
    
    private VoxelParameters parameters;

    public class TLSVoxel {

        int nbSampling = 0;			// number of rays crossing the voxel
        float sampledVolume = 0;		// 
        float interceptions = 0;
        float hiddenVolume = 0;
        float pathLength = 0;			// total path length of rays in voxel
    }
    
    public VoxelAnalysis(){
        
    }
    
    public void init(VoxelParameters parameters){
        
        this.parameters = parameters;
                
        createVoxelSpace();
        
    }
    
    public void voxelise(Shot shot){
        
        shot.direction.normalize();
        origin = new Point3f(shot.origin);
        
        
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
    
    public void propagate(Point3f origin, Point3f shot) {

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
                
                vox.pathLength += d2 - d1;
                vox.nbSampling++;
                
            }else if (d1>distanceToHit) {
                
            }else {
                
                vox.nbSampling++;
                vox.pathLength += distanceToHit - d1;
                vox.interceptions += 1;
                
                Point3f echo = new Point3f(lineElement.getDirection());
                echo.scale(distanceToHit);
            }
        }
    }
    
    private void createVoxelSpace() {

        voxSpace = new VoxelSpace(new BoundingBox3f(parameters.bottomCorner, parameters.topCorner), parameters.split, 0);

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
            
            writer.write("i j k shots path_length BFintercepted BFentering BSintercepted BSentering PAD"+"\n");
            
            for (int x = 0; x < parameters.split.x; x++) {
                for (int y = 0; y < parameters.split.y; y++) {
                    for (int z = 0; z < parameters.split.z; z++) {
                        
                        TLSVoxel vox = voxels[x][y][z];
                        
                        float BFIntercepted = 0.0f;
                        float BFentering = 0.0f;
                        float BSintercepted = 0.0f;
                        float BSentering = 0.0f;
                        float PAD = 0.0f;
                        
                        writer.write(x+" "+y+" "+z+" "+vox.nbSampling+" "+vox.pathLength+" "+BFIntercepted+" "+BFentering+" "+BSintercepted+" "+BSentering+" "+PAD+"\n");
                    }
                }
            }

            writer.close();
        } catch (Exception e) {	//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }

    }

}
