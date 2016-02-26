/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.las.LasReader;
import fr.amap.amapvox.als.las.PointDataRecordFormat;
import fr.amap.amapvox.als.las.PointDataRecordFormat2;
import fr.amap.commons.math.point.Point3F;
import fr.amap.lidar.amapvox.gui.AttributsImporterFrameController;
import fr.amap.lidar.amapvox.gui.LasAttributs;
import fr.amap.lidar.amapvox.gui.MainFrameController;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author Julien Heurtebize
 */
public class LasImportTask extends SceneObjectImportTask{

    private Stage attributsImporterFrame;
    private AttributsImporterFrameController attributsImporterFrameController;
    private LasAttributs lasAttributs;
            
    public LasImportTask(File file){
        
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
        
        //select attributs to import in the attributs importer frame
        

        LasReader reader = new LasReader();
        reader.open(file);
        LasHeader header = reader.getHeader();

        lasAttributs = new LasAttributs(header.getPointDataFormatID());


        attributsImporterFrameController.setAttributsList(lasAttributs.getAttributsNames());

        //create scene object and set attributs colors
        attributsImporterFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                if (attributsImporterFrameController.getSelectedAttributs() != null 
                        && !attributsImporterFrameController.getSelectedAttributs().isEmpty()) {
                    
                    lasAttributs.processList(attributsImporterFrameController.getSelectedAttributs());
                }
            }
        });
        
        attributsImporterFrame.showAndWait();
    }
    
    @Override
    protected SceneObject call() throws Exception {
                
        PointCloudSceneObject sceneObject = new PointCloudSceneObject();

        LasReader reader = new LasReader();

        try {
            reader.open(file);

            LasHeader header = reader.getHeader();

            sceneObject.setGravityCenter(new Point3F((float) (header.getMinX() + (header.getMaxX() - header.getMinX()) / 2.0),
                    (float) (header.getMinY() + (header.getMaxY() - header.getMinY()) / 2.0),
                    (float) (header.getMinZ() + (header.getMaxZ() - header.getMinZ()) / 2.0)));

            long pointNumber = header.getNumberOfPointrecords();

            Iterator<PointDataRecordFormat> lasIterator = reader.iterator();

            long count = 0;

            lasIterator = reader.iterator();

            while (lasIterator.hasNext()) {
                
                updateProgress(count, pointNumber+1);

                PointDataRecordFormat point = lasIterator.next();

                sceneObject.addPoint((float) ((point.getX() * header.getxScaleFactor()) + header.getxOffset()),
                        (float) ((point.getY() * header.getyScaleFactor()) + header.getyOffset()),
                        (float) ((point.getZ() * header.getzScaleFactor()) + header.getzOffset()));

                if (lasAttributs.isExportClassification()) {
                    sceneObject.addValue("classification", (float) point.getClassification());
                }

                if (lasAttributs.isExportIntensity()) {
                    sceneObject.addValue("intensity", (float) point.getIntensity());
                }

                if (lasAttributs.isExportNumberOfReturns()) {
                    sceneObject.addValue("number of returns", (float) point.getNumberOfReturns());
                }

                if (lasAttributs.isExportReturnNumber()) {
                    sceneObject.addValue("return number", (float) point.getReturnNumber());
                }

                if (lasAttributs.isExportTime()) {
                    sceneObject.addValue("GPS time", (float) point.getGpsTime());
                }

                if (header.getPointDataFormatID() == 2 || header.getPointDataFormatID() == 3) {

                    if (lasAttributs.isExportRed() || lasAttributs.isExportGreen() || lasAttributs.isExportBlue()) {

                        int red = 0;
                        int green = 0;
                        int blue = 0;
                        if (lasAttributs.isExportRed()) {
                            red = ((PointDataRecordFormat2) point).getRed();
                        }
                        if (lasAttributs.isExportGreen()) {
                            green = ((PointDataRecordFormat2) point).getGreen();
                        }
                        if (lasAttributs.isExportBlue()) {
                            blue = ((PointDataRecordFormat2) point).getBlue();
                        }

                        sceneObject.addValue("RGB color", red, false);
                        sceneObject.addValue("RGB color", green, false);
                        sceneObject.addValue("RGB color", blue, false);
                    }

                }

                count++;
            }

            sceneObject.initMesh();
            sceneObject.setShader(fr.amap.lidar.amapvox.voxviewer.object.scene.Scene.colorShader);
            
            updateProgress(pointNumber+1, pointNumber+1);
            
            return sceneObject;

        } catch (Exception ex) {
            throw ex;
        }
    }

    
    
}
