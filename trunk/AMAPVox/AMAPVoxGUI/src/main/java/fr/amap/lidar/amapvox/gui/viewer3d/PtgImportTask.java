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
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.lidar.amapvox.gui.AttributsImporterFrameController;
import static fr.amap.lidar.amapvox.gui.viewer3d.SceneObjectImportTask.LOGGER;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author calcul
 */
public class PtgImportTask extends SceneObjectImportTask{

    private Stage attributsImporterFrame;
    private AttributsImporterFrameController attributsImporterFrameController;
    private List<String> selectedAttributs;
    
    public PtgImportTask(File file) {
        
        super(file);
        
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
        
        boolean importReflectance = false, importRGB = false;

        if (selectedAttributs.contains("intensity")) {
            importReflectance = true;
        }
        if (selectedAttributs.contains("RGB")) {
            importRGB = true;
        }

        final PointCloudSceneObject pointCloud = new PointCloudSceneObject();

        PTGReader ptgReader = new PTGReader();
        ptgReader.openPTGFile(file);

        if(ptgReader.isBinaryFile()){

            PTGScan ptgScan = new PTGScan();
            ptgScan.openScanFile(file);

            PTGHeader ptgHeader = ptgScan.getHeader();
            transfMatrix = ptgHeader.getTransfMatrix();

            Iterator<LPoint> iterator = ptgScan.iterator();

            while(iterator.hasNext()){
                
                updateProgress(ptgScan.getCurrentColIndex(), ptgHeader.getNumCols()+1);

                LPoint point = iterator.next();

                float x, y, z;
                if(ptgHeader.isPointInFloatFormat()){
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
                
                if(importRGB && ptgHeader.isPointContainsRGB()){
                    
                    pointCloud.addValue("RGB color", point.red, false);
                    pointCloud.addValue("RGB color", point.green, false);
                    pointCloud.addValue("RGB color", point.blue, false);
                }
            }
            
            pointCloud.initMesh();
            pointCloud.setShader(fr.amap.lidar.amapvox.voxviewer.object.scene.Scene.colorShader);

            updateProgress(ptgScan.getCurrentColIndex()+1, ptgHeader.getNumCols()+1);
            
            return pointCloud;
        }

        return null;
    }
    
}
