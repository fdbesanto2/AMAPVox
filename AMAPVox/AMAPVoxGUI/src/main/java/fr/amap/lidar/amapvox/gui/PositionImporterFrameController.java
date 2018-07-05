/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.javafx.io.FileChooserContext;
import fr.amap.commons.math.point.Point3F;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import fr.amap.viewer3d.object.scene.PointCloudSceneObject;
import fr.amap.commons.javafx.io.TextFileParserFrameController;
import fr.amap.lidar.amapvox.commons.Voxel;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
import javafx.scene.image.ImageView;
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
    private TextField textfieldVoxelFile;
    @FXML
    private ListView<Point3d> listViewCanopyAnalyzerSensorPositions;
    @FXML
    private TextField textFieldXPosition;
    @FXML
    private TextField textFieldYPosition;
    @FXML
    private TextField textFieldZPosition;
    @FXML
    private TextField textfieldScannerHeightOffset;
    @FXML
    private TextField textfieldScannerSeedPosition;
    @FXML
    private Button buttonGenerateGridPosition;
    @FXML
    private ImageView imageViewLoading;

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
            textFileParserFrameController.setColumnAssignmentValues("X", "Y", "Z", "Ignore");
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 0);
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 1);
            textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 2);
            textFileParserFrameController.setHeaderExtractionEnabled(false);


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
                                                
                                                try {

                                                    if (finalXIndex != -1) {
                                                        x = Double.valueOf(lineSplitted[finalXIndex]);
                                                    }
                                                    if (finalYIndex != -1) {
                                                        y = Double.valueOf(lineSplitted[finalYIndex]);
                                                    }
                                                    if (finalZIndex != -1) {
                                                        z = Double.valueOf(lineSplitted[finalZIndex]);
                                                    }

                                                    positions.add(new Point3d(x, y, z));
                                                } catch (Exception e) {
                                                    logger.error("Cannot parse line "+(count+1));
                                                }
                                                
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
        
        if(selectedItems.size() == listViewCanopyAnalyzerSensorPositions.getItems().size()){
            listViewCanopyAnalyzerSensorPositions.getItems().clear();
        }else{
            listViewCanopyAnalyzerSensorPositions.getItems().removeAll(selectedItems);
        }
    }

    @FXML
    private void onActionButtonImportPositions(ActionEvent event) {
        stage.close();
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }
    
    private List<Point3d> generateGridPositions() throws Exception{
        
        float step = Float.valueOf(textfieldScannerSeedPosition.getText());
        float zOffset = Float.valueOf(textfieldScannerHeightOffset.getText());

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
                mnt[x][y] = 999999999;
            }
        }
        
        Voxel[][][] voxels = new Voxel[infos.getSplit().x][infos.getSplit().y][infos.getSplit().z];
        
        Iterator<Voxel> iterator = reader.iterator();
        while(iterator.hasNext()){
            Voxel voxel = iterator.next();
            voxels[voxel.i][voxel.j][voxel.k] = voxel;
        }
        
        for(int i=0;i<infos.getSplit().x;i++){

            for(int j=0;j<infos.getSplit().y;j++){
                
                for(int k = infos.getSplit().z-1 ; k >= 0 ; k--){
                    
                    if(voxels[i][j][k].ground_distance > 0){

                        double posZ = infos.getMinCorner().z + (infos.getResolution() / 2.0d) + (k * infos.getResolution());
                        double diff = voxels[i][j][k].ground_distance;
                        mnt[i][j] = (float) (posZ - diff + (infos.getResolution() / 2.0d));
                        
                    }else if(voxels[i][j][k].ground_distance < 0){
                        k = 0;
                    }
                }
                
            }
        }
        

        double middleX = infos.getMinCorner().x + (infos.getMaxCorner().x - infos.getMinCorner().x);
        double middleY = infos.getMinCorner().y + (infos.getMaxCorner().y - infos.getMinCorner().y);

        //calcul du x min
        double xWidth = (infos.getMaxCorner().x - infos.getMinCorner().x) / 2.0;
        int nbPossibleXStep = (int) (xWidth / step) * 2;
        double xStart = middleX - (nbPossibleXStep * step);

        double yWidth = (infos.getMaxCorner().y - infos.getMinCorner().y) / 2.0;
        int nbPossibleYStep = (int) (yWidth / step) * 2;
        double yStart = middleY - (nbPossibleYStep * step);



        for(int i=0;i <= nbPossibleXStep;i++){

            for(int j=0;j <= nbPossibleYStep;j++){

                double posX = xStart + i*step; 
                double posY = yStart + j*step;

                int indiceX = (int)((posX-infos.getMinCorner().x)/infos.getResolution());
                int indiceY = (int)((posY-infos.getMinCorner().y)/infos.getResolution());
                
                if(indiceX == infos.getSplit().x){
                    indiceX--;
                }
                
                if(indiceY == infos.getSplit().y){
                    indiceY--;
                }

                positions.add(new Point3d(posX, posY, mnt[indiceX][indiceY] + zOffset));
            }
        }
        
        return positions;
    }

    @FXML
    private void onActionButtonGenerateGridPosition(ActionEvent event) {

        buttonGenerateGridPosition.setDisable(true);
        imageViewLoading.setVisible(true);
        
        Service s = new Service() {

            @Override
            protected Task createTask() {
                return new Task() {

                    @Override
                    protected Object call() throws Exception {

                        try {

                            List<Point3d> positions = generateGridPositions();

                            Platform.runLater(new Runnable() {

                                @Override
                                public void run() {
                                    listViewCanopyAnalyzerSensorPositions.getItems().addAll(positions);
                                    buttonGenerateGridPosition.setDisable(false);
                                    imageViewLoading.setVisible(false);
                                }
                            });

                        } catch (Exception e) {
                            logger.error(e);
                        }
                        
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
    
    public void setInitialVoxelFile(File file){
        textfieldVoxelFile.setText(file.getAbsolutePath());
    }
    
}
