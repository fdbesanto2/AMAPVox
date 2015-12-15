/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.gui;

import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.voxcommons.VoxelSpaceInfos;
import fr.amap.amapvox.voxreader.VoxelFileReader;
import fr.amap.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javax.vecmath.Point3d;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Julien
 */
public class PositionImporterFrameController implements Initializable {
    
    private FileChooserContext fileChooserOpenVoxelFile;
    private FileChooserContext fileChooserOpenPositionFile;
    private Stage stage;
    private TextFileParserFrameController textFileParserFrameController;
    
    private final static Logger logger = Logger.getLogger(PositionImporterFrameController.class);
    private float mnt[][];
    
    @FXML
    private Button onActionButtonImportFromFile;
    @FXML
    private TextField textfieldScannerPosCenterX;
    @FXML
    private TextField textfieldScannerPosCenterY;
    @FXML
    private TextField textfieldScannerPosCenterZ;
    @FXML
    private TextField textfieldScannerWidthArea;
    @FXML
    private TextField textfieldScannerStepArea;
    @FXML
    private TextField textfieldVoxelFile;
    @FXML
    private ListView<Point3d> listViewCanopyAnalyzerSensorPositions;
    @FXML
    private MenuItem menuItemSelectionNone1;
    @FXML
    private Button onActionButtonImportFromFile1;
    @FXML
    private TextField textFieldXPosition;
    @FXML
    private TextField textFieldYPosition;
    @FXML
    private TextField textFieldZPosition;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        fileChooserOpenVoxelFile = new FileChooserContext();
        fileChooserOpenPositionFile = new FileChooserContext();
        
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        try {
            textFileParserFrameController = TextFileParserFrameController.getInstance();
        } catch (Exception ex) {
            logger.error("Cannot load fxml file", ex);
        }
    }    

    @FXML
    private void onActionButtonOpenVoxelFile(ActionEvent event) {
        
        File selectedFile = fileChooserOpenVoxelFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldVoxelFile.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionMenuItemPositionsSelectionAll(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().selectAll();
    }

    @FXML
    private void onActionMenuItemPositionsSelectionNone(ActionEvent event) {
        listViewCanopyAnalyzerSensorPositions.getSelectionModel().clearSelection();
    }    
            
    @FXML
    private void onActionImportPositionsFromFile(ActionEvent event) {
       
        final File selectedFile = fileChooserOpenPositionFile.showOpenDialog(stage);
        
        if(selectedFile != null){
            textFileParserFrameController.setColumnAssignment(true);
            textFileParserFrameController.setColumnAssignmentValues("X", "Y", "Z");
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 1);
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 2);
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 3);


            try {
                textFileParserFrameController.setTextFile(selectedFile);
                Stage textFileParserFrame = textFileParserFrameController.getStage();
                textFileParserFrame.show();

                textFileParserFrame.setOnHidden(new  EventHandler<WindowEvent>() {

                    @Override
                    public void handle(WindowEvent event) {
                        
                        String separator = textFileParserFrameController.getSeparator();
                        List<String> columnAssignment = textFileParserFrameController.getAssignedColumnsItems();
                        final int numberOfLines = textFileParserFrameController.getNumberOfLines();
                        
                        final int headerIndex = textFileParserFrameController.getHeaderIndex();
                        int skipNumber = textFileParserFrameController.getSkipLinesNumber();
                        
                        int xIndex = -1, yIndex = -1, zIndex = -1;
                        
                        for(int i =0;i<columnAssignment.size();i++){
                            
                            String item = columnAssignment.get(i);
                            
                            if(item != null){
                                switch(item){
                                    case "X":
                                        xIndex = i;
                                        break;
                                    case "Y":
                                        yIndex = i;
                                        break;
                                    case "Z":
                                        zIndex = i;
                                        break;
                                    default:
                                } 
                            }
                        }
                        
                        if(headerIndex != -1){
                            skipNumber++;
                        }
                        
                        final int finalSkipNumber = skipNumber;
                        final int finalXIndex = xIndex;
                        final int finalYIndex = yIndex;
                        final int finalZIndex = zIndex;
                        
                        Service service = new Service() {

                            @Override
                            protected Task createTask() {
                                return new Task() {

                                    @Override
                                    protected Object call() throws Exception {
                                        
                                        final List<Point3d> positions = new ArrayList<>();
                                        
                                        BufferedReader reader;
                                        try {
                                            reader = new BufferedReader(new FileReader(selectedFile));

                                            String line;
                                            int count = 0;

                                            for(int i=0;i<finalSkipNumber;i++){
                                                reader.readLine();
                                            }

                                            while((line = reader.readLine()) != null){

                                                if(count == numberOfLines){
                                                    break;
                                                }

                                                String[] lineSplitted = line.split(separator);
                                                
                                                double x = 0, y = 0, z = 0;
                                                if(finalXIndex != -1){
                                                    x = Double.valueOf(lineSplitted[finalXIndex]);
                                                }
                                                if(finalYIndex != -1){
                                                    y = Double.valueOf(lineSplitted[finalYIndex]);
                                                }
                                                if(finalZIndex != -1){
                                                    z = Double.valueOf(lineSplitted[finalZIndex]);
                                                }
                                                
                                                positions.add(new Point3d(x, y, z));

                                                count++;
                                            }

                                            reader.close();                            

                                        } catch (FileNotFoundException ex) {
                                            logger.error("Cannot load point file", ex);
                                        } catch (IOException ex) {
                                            logger.error("Cannot load point file", ex);
                                        }
                                        
                                        Platform.runLater(new Runnable() {

                                            @Override
                                            public void run() {
                                                listViewCanopyAnalyzerSensorPositions.getItems().addAll(positions);
                                            }
                                        });
                                        
                                        return null;
                                    }
                                };
                            }
                        };
                        
                        service.start();
                        
                    }
                });
            } catch (IOException ex) {
                logger.error(ex);
            }
        }
        
    }

    @FXML
    private void onActionButtonRemovePosition(ActionEvent event) {
        ObservableList<Point3d> selectedItems = listViewCanopyAnalyzerSensorPositions.getSelectionModel().getSelectedItems();
        listViewCanopyAnalyzerSensorPositions.getItems().removeAll(selectedItems);
    }

    @FXML
    private void onActionButtonImportPositions(ActionEvent event) {
        stage.close();
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }

    @FXML
    private void onActionButtonGenerateGridPosition(ActionEvent event) {

        Service s = new Service() {

            @Override
            protected Task createTask() {
                return new Task() {

                    @Override
                    protected Object call() throws Exception {
                        
                        int size = Integer.valueOf(textfieldScannerWidthArea.getText());

                        int middleX = (int) Integer.valueOf(textfieldScannerPosCenterX.getText());
                        int middleY = (int) Integer.valueOf(textfieldScannerPosCenterY.getText());
                        float zOffset = Float.valueOf(textfieldScannerPosCenterZ.getText());
                        
                        final List<Point3d> positions = new ArrayList<>();

                        VoxelFileReader reader = new VoxelFileReader(new File(textfieldVoxelFile.getText()));
                        VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();
                        Point3d voxSize = new Point3d();
                        voxSize.x = (infos.getMaxCorner().x - infos.getMinCorner().x) / (double) infos.getSplit().x;
                        voxSize.y = (infos.getMaxCorner().y - infos.getMinCorner().y) / (double) infos.getSplit().y;
                        voxSize.z = (infos.getMaxCorner().z - infos.getMinCorner().z) / (double) infos.getSplit().z;

                        // allocate MNT
                        logger.info("allocate MNT");
                        mnt = new float[infos.getSplit().x][];
                        for (int x = 0; x < infos.getSplit().x; x++) {
                            mnt[x] = new float[infos.getSplit().y];
                            for (int y = 0; y < infos.getSplit().y; y++) {
                                mnt[x][y] = (float) infos.getMinCorner().z;
                            }
                        }

                        int xMin = middleX - size;
                        int yMin = middleY - size;

                        int xMax = middleX + size;
                        int yMax = middleY + size;

                        xMin = Integer.max(xMin, 0);
                        yMin = Integer.max(yMin, 0);

                        xMax = Integer.min(xMax, infos.getSplit().x - 1);
                        yMax = Integer.min(yMax, infos.getSplit().y - 1);

                        for (int i = xMin; i < xMax; i++) {

                            double tx = (0.5f + (double) i) * voxSize.x;

                            for (int j = yMin; j < yMax; j++) {

                                double ty = (0.5f + (double) j) * voxSize.y;
                                Point3d pos = new Point3d(infos.getMinCorner());
                                pos.add(new Point3d(tx, ty, mnt[i][j] + zOffset));
                                positions.add(pos);
                            }
                        }

                        Platform.runLater(new Runnable() {

                            @Override
                            public void run() {
                                listViewCanopyAnalyzerSensorPositions.getItems().addAll(positions);
                            }
                        });
                        
                        

                        return null;
                    }
                };
            }
        };
        
        s.start();
        
        
        
        s.setOnFailed(new EventHandler() {
            @Override
            public void handle(Event event) {
                ErrorDialog.show(new Exception(s.getException()));
            }
        });

    }
    
    public List<Point3d> getPositions(){
        
        return listViewCanopyAnalyzerSensorPositions.getItems();
    }

    @FXML
    private void onActionButtonAddSinglePosition(ActionEvent event) {
        
        try{
            Point3d position = new Point3d(Double.valueOf(textFieldXPosition.getText()),
                                        Double.valueOf(textFieldYPosition.getText()),
                                        Double.valueOf(textFieldZPosition.getText()));
        
            listViewCanopyAnalyzerSensorPositions.getItems().add(position);
            
        }catch(Exception e){
            ErrorDialog.show(e);
        }
    }
    
}
