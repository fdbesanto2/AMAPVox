/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.lidar.amapvox.gui.AttributsImporterFrameController;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.ColorShader;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javafx.application.Platform;
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
public class RxpImportTask extends SceneObjectImportTask{

    private Stage attributsImporterFrame;
    private AttributsImporterFrameController attributsImporterFrameController;
    private List<String> selectedAttributs;
    
    public RxpImportTask(File file, Mat4D transfMatrix) {
        
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
        
        this.transfMatrix = transfMatrix;
    }

    @Override
    public void showImportFrame(Stage stage) throws Exception {
        
        attributsImporterFrameController.setAttributsList("reflectance", "deviation", "amplitude", "time");

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
        
        boolean importReflectance = false, importDeviation = false, importAmplitude = false, importTime = false;
        List<Integer> typeList = new ArrayList<>();

        if (selectedAttributs.contains("reflectance")) {
            importReflectance = true;
            typeList.add(RxpExtraction.REFLECTANCE);
        }
        if (selectedAttributs.contains("deviation")) {
            importDeviation = true;
            typeList.add(RxpExtraction.DEVIATION);
        }
        if (selectedAttributs.contains("amplitude")) {
            importAmplitude = true;
            typeList.add(RxpExtraction.AMPLITUDE);
        }
        if (selectedAttributs.contains("time")) {
            importTime = true;
            typeList.add(RxpExtraction.TIME);
        }

        final PointCloudSceneObject pointCloud = new PointCloudSceneObject();

        RxpExtraction reader = new RxpExtraction();
        int[] typeListNative = new int[typeList.size()];

        for (int i = 0; i < typeList.size(); i++) {
            typeListNative[i] = typeList.get(i);
        }
        
        updateProgress(0, 100);

        updateMessage("Loading data");
        
        reader.openRxpFile(file, typeListNative);

        final Iterator<Shot> iterator = reader.iterator();

        while (iterator.hasNext()) {

            Shot shot = iterator.next();

            for (int i = 0; i < shot.ranges.length; i++) {

                double range = shot.ranges[i];

                float x = (float) (shot.origin.x + shot.direction.x * range);
                float y = (float) (shot.origin.y + shot.direction.y * range);
                float z = (float) (shot.origin.z + shot.direction.z * range);

                Vec4D transformedPoint = Mat4D.multiply(transfMatrix, new Vec4D(x, y, z, 1));
                pointCloud.addPoint((float) transformedPoint.x, (float) transformedPoint.y, (float) transformedPoint.z);

                if (importReflectance) {
                    float reflectance = shot.reflectances[i];
                    pointCloud.addValue("reflectance", reflectance);
                }

                if (importDeviation) {
                    float deviation = shot.deviations[i];
                    pointCloud.addValue("deviation", deviation);
                }

                if (importAmplitude) {
                    float amplitude = shot.amplitudes[i];
                    pointCloud.addValue("amplitude", amplitude);
                }
                
                if (importTime) {
                    float time = (float) shot.times[i];
                    pointCloud.addValue("time", time);
                }
            }

        }
        
        updateMessage("Build 3d scene object");

        pointCloud.setDrawEveryNPoints(100);
        pointCloud.initMesh();
        pointCloud.setShader(new ColorShader());

        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                updateProgress(100, 100);
            }
        });
        
        return pointCloud;
    }
    
}
