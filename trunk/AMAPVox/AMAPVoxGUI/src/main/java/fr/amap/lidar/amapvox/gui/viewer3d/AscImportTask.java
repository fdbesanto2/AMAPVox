/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.commons.math.geometry.BoundingBox2F;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.util.MatrixUtility;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.gui.AsciiGridFileExtractorController;
import fr.amap.lidar.amapvox.gui.SceneObjectWrapper;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.lidar.amapvox.voxviewer.object.scene.RasterSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.VoxelSpaceSceneObject;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Matrix4d;

/**
 *
 * @author calcul
 */
public class AscImportTask extends SceneObjectImportTask{

    private final List<VoxelSpaceSceneObject> vsScObjects;
    private AsciiGridFileExtractorController ascExtractor;
    
    private boolean transform;
    private boolean fitDTMToVoxelSpace;
    private boolean buildOctree;
    private int mntFittingMargin;
    private File voxelFileToFitTo; 
    
    public AscImportTask(File file, List<SceneObjectWrapper> vsSceneObjectsList) {
        
        super(file);
        
        vsScObjects = new ArrayList<>();
                
        for(SceneObjectWrapper sc : vsSceneObjectsList){
            if(sc.getSceneObject() instanceof VoxelSpaceSceneObject){
                vsScObjects.add((VoxelSpaceSceneObject)sc.getSceneObject());
            }
        }
        
        try {
            ascExtractor = AsciiGridFileExtractorController.getInstance();
        } catch (Exception ex) {
            LOGGER.error("Cannot load fxml file", ex);
        }
    }

    @Override
    public void showImportFrame(Stage stage) throws Exception {
        
        ascExtractor.init(file, vsScObjects);
        
        ascExtractor.getStage().setOnHidden(new EventHandler<WindowEvent>() {
                    
                @Override
                public void handle(WindowEvent event) {
                    
                    if(ascExtractor.wasHidden()){
                        
                        transform = ascExtractor.isTransfMatrixEnabled();
                        
                        transfMatrix = MatrixUtility.convertMatrix4dToMat4D(ascExtractor.getRasterTransfMatrix());
                        if(transfMatrix == null){
                            transfMatrix = Mat4D.identity();
                        }
                        
                        fitDTMToVoxelSpace = ascExtractor.isFittingToVoxelSpaceEnabled();
                        buildOctree = ascExtractor.isOctreeWanted();
                        mntFittingMargin = ascExtractor.getFittingMargin();
                        
                        if(ascExtractor.getVoxelSpaceToFitTo() != null){
                            voxelFileToFitTo = ascExtractor.getVoxelSpaceToFitTo().getVoxelFile();
                        }
                    }
                }
        });
        
        ascExtractor.getStage().showAndWait();
    }
    
    
    @Override
    protected SceneObject call() throws Exception {
        
        if(file != null){

            updateProgress(0, 100);
            updateMessage("Loading data");
            
            Raster dtm = AsciiGridHelper.readFromAscFile(file);

            if (transform && transfMatrix != null) {
                dtm.setTransformationMatrix(transfMatrix);
            }

            if (fitDTMToVoxelSpace) {

                VoxelFileReader reader = new VoxelFileReader(voxelFileToFitTo);
                VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();

                dtm.setLimits(new BoundingBox2F(new Point2F((float) infos.getMinCorner().x, (float) infos.getMinCorner().y),
                        new Point2F((float) infos.getMaxCorner().x, (float) infos.getMaxCorner().y)), mntFittingMargin);
            }

            updateMessage("Build mesh");
            dtm.buildMesh();

            updateMessage("Build 3d scene object");
            GLMesh dtmMesh = GLMeshFactory.createMeshAndComputeNormalesFromDTM(dtm);

            //SceneObject dtmSceneObject = new SimpleSceneObject(dtmMesh, false);
            

            RasterSceneObject dtmSceneObject = new RasterSceneObject(dtmMesh, false);
            dtmSceneObject.setShader(fr.amap.lidar.amapvox.voxviewer.object.scene.Scene.phongShader);

            dtmSceneObject.setMousePickable(buildOctree);
            
            updateProgress(100, 100);
            
            
            return dtmSceneObject;
        }
        
        return null;
    }

    
}
