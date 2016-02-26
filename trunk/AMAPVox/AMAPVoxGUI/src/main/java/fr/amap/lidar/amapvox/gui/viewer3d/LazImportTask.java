/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.als.laz.LazExtraction;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.util.ColorGradient;
import fr.amap.lidar.amapvox.gui.AttributsImporterFrameController;
import fr.amap.lidar.amapvox.gui.LazAttributs;
import static fr.amap.lidar.amapvox.gui.viewer3d.SceneObjectImportTask.LOGGER;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
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
public class LazImportTask extends SceneObjectImportTask{

    private Stage attributsImporterFrame;
    private AttributsImporterFrameController attributsImporterFrameController;
    private final LazAttributs lazAttributs;
    
    public LazImportTask(File file) {
        
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
        
        lazAttributs = new LazAttributs();
                
    }

    @Override
    public void showImportFrame(Stage stage) throws Exception {
        
        attributsImporterFrameController.setAttributsList(lazAttributs.getAttributsNames());
        
        attributsImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                if (attributsImporterFrameController.getSelectedAttributs() != null
                        && !attributsImporterFrameController.getSelectedAttributs().isEmpty()) {

                    lazAttributs.processList(attributsImporterFrameController.getSelectedAttributs());
                }
            }
        });
        
        attributsImporterFrame.showAndWait();
    }

    @Override
    protected SceneObject call() throws Exception {
        
        PointCloudSceneObject sceneObject = new PointCloudSceneObject();

        LazExtraction lazExtraction = new LazExtraction();
        try {
            lazExtraction.openLazFile(file);

            LasHeader header = lazExtraction.getHeader();

            sceneObject.setGravityCenter(new Point3F((float) (header.getMinX() + (header.getMaxX() - header.getMinX()) / 2.0),
                    (float) (header.getMinY() + (header.getMaxY() - header.getMinY()) / 2.0),
                    (float) (header.getMinZ() + (header.getMaxZ() - header.getMinZ()) / 2.0)));

            long pointNumber = header.getNumberOfPointrecords();

            ColorGradient cg = new ColorGradient(0, 1);
            cg.setGradientColor(ColorGradient.GRADIENT_RAINBOW3);
            Iterator<LasPoint> lazIterator = lazExtraction.iterator();

            long count = 0;
            
            while (lazIterator.hasNext()) {
                
                updateProgress(count, pointNumber+1);

                LasPoint point = lazIterator.next();

                point.x = (point.x * header.getxScaleFactor()) + header.getxOffset();
                point.y = (point.y * header.getyScaleFactor()) + header.getyOffset();
                point.z = (point.z * header.getzScaleFactor()) + header.getzOffset();

                sceneObject.addPoint((float) point.x, (float) point.y, (float) point.z);

                if (lazAttributs.isExportClassification()) {
                    sceneObject.addValue("classification", (float) point.classification);
                }

                if (lazAttributs.isExportIntensity()) {
                    sceneObject.addValue("intensity", (float) point.i);
                }

                if (lazAttributs.isExportNumberOfReturns()) {
                    sceneObject.addValue("number of returns", (float) point.n);
                }

                if (lazAttributs.isExportReturnNumber()) {
                    sceneObject.addValue("number of returns", (float) point.r);
                }

                if (lazAttributs.isExportTime()) {
                    sceneObject.addValue("GPS time", (float) point.t);
                }

                count++;
            }

            lazExtraction.close();

            sceneObject.initMesh();
            sceneObject.setShader(fr.amap.lidar.amapvox.voxviewer.object.scene.Scene.colorShader);
            
            updateProgress(pointNumber+1, pointNumber+1);
            
            return sceneObject;

        } catch (Exception ex) {
            throw ex;
        }
        
    }
    
}
