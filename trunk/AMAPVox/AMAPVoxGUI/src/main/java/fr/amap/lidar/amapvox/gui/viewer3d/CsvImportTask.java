/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import fr.amap.commons.javafx.io.TextFileParserFrameController;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.util.io.file.FileManager;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.ColorShader;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

/**
 *
 * @author calcul
 */
public class CsvImportTask extends SceneObjectImportTask{

    private TextFileParserFrameController textFileParserFrameController;
    private int finalSkipNumber;
    private int finalXIndex;
    private int finalYIndex;
    private int finalZIndex;
    private int numberOfLines;
    private String separator;
    private List<Integer> otherFieldsIndices;
    
    public CsvImportTask(File file) {
        
        super(file);
        
        try {
            textFileParserFrameController = TextFileParserFrameController.getInstance();
        } catch (Exception ex) {
            LOGGER.error("Cannot load fxml file", ex);
        }
    }

    @Override
    public void showImportFrame(Stage stage) throws Exception {

        textFileParserFrameController.setColumnAssignment(true);
        textFileParserFrameController.setColumnAssignmentValues("Ignore", "X", "Y", "Z", "Red", "Green", "Blue", "Scalar field");

        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 1);
        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 2);
        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 3);
        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(3, 4);

        textFileParserFrameController.setTextFile(file);

        Stage textFileParserFrame = textFileParserFrameController.getStage();

        textFileParserFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {

                separator = textFileParserFrameController.getSeparator();
                List<String> columnAssignment = textFileParserFrameController.getAssignedColumnsItems();
                numberOfLines = textFileParserFrameController.getNumberOfLines();

                final int headerIndex = textFileParserFrameController.getHeaderIndex();
                int skipNumber = textFileParserFrameController.getSkipLinesNumber();

                int xIndex = -1, yIndex = -1, zIndex = -1;
                otherFieldsIndices = new ArrayList<>();

                for (int i = 0; i < columnAssignment.size(); i++) {

                    String item = columnAssignment.get(i);

                    if (item != null) {
                        switch (item) {
                            case "X":
                                xIndex = i;
                                break;
                            case "Y":
                                yIndex = i;
                                break;
                            case "Z":
                                zIndex = i;
                                break;
                            case "Scalar field":
                                otherFieldsIndices.add(i);
                                break;
                        }
                    }
                }

                if (headerIndex != -1) {
                    skipNumber++;
                }

                finalSkipNumber = skipNumber;
                finalXIndex = xIndex;
                finalYIndex = yIndex;
                finalZIndex = zIndex;
            }
        });
        
        textFileParserFrame.showAndWait();
    }

    @Override
    protected SceneObject call() throws Exception {
        
        updateMessage("Loading data");
        
        int lineNumber = FileManager.getLineNumber(file.getAbsolutePath());
        
        lineNumber = Integer.min(lineNumber, numberOfLines);
        
        PointCloudSceneObject sceneObject = new PointCloudSceneObject();
        sceneObject.setGravityCenter(new Point3F(0, 0, 0));

        BufferedReader reader;
        try {
            FileReader fileReader = new FileReader(file);
            reader = new BufferedReader(fileReader);

            String line;
            int count = 0;

            for (int i = 0; i < finalSkipNumber; i++) {
                reader.readLine();
            }

            while ((line = reader.readLine()) != null) {

                if (count == numberOfLines) {
                    break;
                }
                
                updateProgress(count, lineNumber+1);

                String[] lineSplitted = line.split(separator);

                float x = 0, y = 0, z = 0;
                if (finalXIndex != -1) {
                    x = Float.valueOf(lineSplitted[finalXIndex]);
                }
                if (finalYIndex != -1) {
                    y = Float.valueOf(lineSplitted[finalYIndex]);
                }
                if (finalZIndex != -1) {
                    z = Float.valueOf(lineSplitted[finalZIndex]);
                }

                sceneObject.addPoint(x, y, z);

                int scalarFieldIndex = 0;

                if (!otherFieldsIndices.isEmpty()) {
                    
                    for (Integer i : otherFieldsIndices) {
                        try{
                            sceneObject.addValue("Scalar field " + scalarFieldIndex, Float.valueOf(lineSplitted[i]));
                            scalarFieldIndex++;
                        }catch(NumberFormatException ex){}
                        
                    }
                } else {
                    sceneObject.addValue("default", count);
                }

                count++;
            }

            reader.close();
            
            updateMessage("Build 3d scene object");
            
            sceneObject.initMesh();
            sceneObject.setShader(new ColorShader());

            updateProgress(100, 100);

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (IOException ex) {
            throw ex;
        }
        
        return sceneObject;
    }
    
}
