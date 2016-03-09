/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.amapvox.jleica.LDoublePoint;
import fr.amap.amapvox.jleica.LFloatPoint;
import fr.amap.amapvox.jleica.LPoint;
import fr.amap.amapvox.jleica.ptg.PTGHeader;
import fr.amap.amapvox.jleica.ptg.PTGReader;
import fr.amap.amapvox.jleica.ptg.PTGScan;
import fr.amap.amapvox.jleica.ptx.PTXHeader;
import fr.amap.amapvox.jleica.ptx.PTXScan;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.util.LidarScan;
import fr.amap.lidar.amapvox.gui.AttributsImporterFrameController;
import static fr.amap.lidar.amapvox.gui.viewer3d.SceneObjectImportTask.LOGGER;
import fr.amap.lidar.amapvox.voxelisation.configuration.PTXLidarScan;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author calcul
 */
public class PtxImportTask extends SceneObjectImportTask{

    private final List<PTXLidarScan> scans;
    private Stage attributsImporterFrame;
    private AttributsImporterFrameController attributsImporterFrameController;
    private List<String> selectedAttributs;
    
    public PtxImportTask(File file, List<PTXLidarScan> scans) {
        super(file);
        
        this.scans = scans;
        
        try {
            attributsImporterFrame = new Stage();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AttributsImporterFrame.fxml"));
            Parent root = loader.load();
            attributsImporterFrameController = loader.getController();
            attributsImporterFrame.setScene(new Scene(root));
            attributsImporterFrameController.setStage(attributsImporterFrame);
        } catch (IOException ex) {
            LOGGER.error("Cannot load fxml file", ex);
        }
    }

    @Override
    public void showImportFrame(Stage stage) throws Exception {
        
        attributsImporterFrameController.setAttributsList("intensity", "RGB");

        attributsImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                if (attributsImporterFrameController.getSelectedAttributs() != null
                        && !attributsImporterFrameController.getSelectedAttributs().isEmpty()) {

                    selectedAttributs = attributsImporterFrameController.getSelectedAttributs();
                }
            }
        });
        
        attributsImporterFrame.showAndWait();
    }

    @Override
    protected SceneObject call() throws Exception {
        
        updateProgress(0, 100);
        
        boolean importReflectance = false, importRGB = false;

        if (selectedAttributs.contains("intensity")) {
            importReflectance = true;
        }
        if (selectedAttributs.contains("RGB")) {
            importRGB = true;
        }

        final PointCloudSceneObject pointCloud = new PointCloudSceneObject();
        
        updateMessage("Loading data");
        
        for(PTXLidarScan scan : scans){
            
            PTXScan ptxScan = scan.getScan();
            PTXHeader ptxHeader = ptxScan.getHeader();
            
            Mat4D transfMatrix = ptxHeader.getTransfMatrix();
            
            Iterator<LPoint> iterator = ptxScan.iterator();
            
            while(iterator.hasNext()){

                LPoint point = iterator.next();

                float x, y, z;
                if(ptxHeader.isPointInFloatFormat()){
                    x = ((LFloatPoint)point).x;
                    y = ((LFloatPoint)point).y;
                    z = ((LFloatPoint)point).z;
                }else{
                    x = (float) ((LDoublePoint)point).x;
                    y = (float) ((LDoublePoint)point).y;
                    z = (float) ((LDoublePoint)point).z;
                }

                Vec4D transformedPoint = Mat4D.multiply(transfMatrix, new Vec4D(x, y, z, 1));
                pointCloud.addPoint((float) transformedPoint.x, (float) transformedPoint.y, (float) transformedPoint.z);
                
                if(importReflectance){
                    pointCloud.addValue("intensity", point.intensity);
                }
                
                if(importRGB && ptxHeader.isPointContainsRGB()){
                    
                    pointCloud.addValue("RGB color", point.red, false);
                    pointCloud.addValue("RGB color", point.green, false);
                    pointCloud.addValue("RGB color", point.blue, false);
                }
            }
        }
        
        updateProgress(99, 100);
        updateMessage("Build 3d scene object");
        
        pointCloud.initMesh();
        pointCloud.setShader(fr.amap.lidar.amapvox.voxviewer.object.scene.Scene.colorShader);
        
        updateProgress(100, 100);

        return pointCloud;
    }
    
}
