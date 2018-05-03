/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package scripts;

import fr.amap.commons.javafx.io.TextFileParserFrameController;
import fr.amap.commons.math.geometry.BoundingBox2F;
import fr.amap.commons.math.point.Point2F;
import fr.amap.commons.raster.asc.AsciiGridHelper;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.math.util.BoundingBox3d;
import fr.amap.commons.util.io.file.CSVFile;
import fr.amap.lidar.amapvox.commons.LADParams;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.gui.EditableListViewController;
import fr.amap.lidar.amapvox.gui.FileChooserTextFieldController;
import fr.amap.lidar.amapvox.gui.FxmlContext;
import fr.amap.lidar.amapvox.gui.Global;
import fr.amap.lidar.amapvox.gui.MainFrameController;
import fr.amap.lidar.amapvox.gui.Util;
import fr.amap.lidar.amapvox.voxelisation.postproc.VoxelFileMerging;
import fr.amap.lidar.amapvox.voxelisation.configuration.ALSVoxCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.VoxelAnalysisCfg;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.DTMFilteringParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.EchoesWeightParams;
import fr.amap.lidar.amapvox.voxelisation.configuration.params.VoxelParameters;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien Heurtebize
 */
public class DanielScript extends Script{
    
    private final List<Node> pages = new ArrayList<>();

    public DanielScript(MainFrameController controller){
        
        super(controller);
        
        try {
            //page 1
            FxmlContext fxmlContext = Util.loadFxml("/fxml/EditableListView.fxml");
            EditableListViewController<File> listController = (EditableListViewController) fxmlContext.getController();
            
            listController.getButtonAddItemToListView().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    FileChooser fcLas = new FileChooser();
                    fcLas.setTitle("Choose *.las or *.laz input files");
                    ExtensionFilter extensionFilter = new FileChooser.ExtensionFilter("Las files", "*.las", "*.laz");
                    fcLas.getExtensionFilters().setAll(extensionFilter);
                    fcLas.setSelectedExtensionFilter(extensionFilter);
                    
                    final List<File> selectedFiles = fcLas.showOpenMultipleDialog(controller.getStage());
                    listController.getListViewItems().getItems().addAll(selectedFiles);
                }
            });
            
            pages.add(new VBox(new Label("Choose *.las or *.laz input files"), fxmlContext.getRoot()));
            
            //page 2
            FxmlContext fxmlContext2 = Util.loadFxml("/fxml/FileChooserTextField.fxml");
            FileChooserTextFieldController<CSVFile> trajController = (FileChooserTextFieldController) fxmlContext2.getController();
            trajController.getLabel().setText("Trajectory file");
            
            trajController.getButtonOpenFile().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    File selectedFile = trajController.getFileChooser().showOpenDialog(new Stage());
                    
                    if (selectedFile != null) {
                        TextFileParserFrameController textFileParserFrameController;
                        
                        try {
                            textFileParserFrameController = TextFileParserFrameController.getInstance();
                        } catch (Exception ex) {
                            controller.showErrorDialog(ex);
                            return;
                        }
                        
                        textFileParserFrameController.setColumnAssignment(true);
                        textFileParserFrameController.setColumnAssignmentValues("Ignore", "Easting", "Northing", "Elevation", "Time");
                        
                        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(0, 1);
                        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(1, 2);
                        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(2, 3);
                        textFileParserFrameController.setColumnAssignmentDefaultSelectedIndex(3, 4);
                        
                        textFileParserFrameController.setHeaderExtractionEnabled(true);
                        textFileParserFrameController.setSeparator(",");
                        
                        try {
                            textFileParserFrameController.setTextFile(selectedFile);
                        } catch (IOException ex) {
                            controller.showErrorDialog(ex);
                            return;
                        }
                        
                        Stage textFileParserFrame = textFileParserFrameController.getStage();
                        textFileParserFrame.show();
                        
                        textFileParserFrame.setOnHidden(new EventHandler<WindowEvent>() {
                            
                            @Override
                            public void handle(WindowEvent event) {
                                
                                CSVFile trajectoryFile = new CSVFile(selectedFile.getAbsolutePath());
                                trajectoryFile.setColumnSeparator(textFileParserFrameController.getSeparator());
                                trajectoryFile.setColumnAssignment(textFileParserFrameController.getAssignedColumnsItemsMap());
                                trajectoryFile.setNbOfLinesToRead(textFileParserFrameController.getNumberOfLines());
                                trajectoryFile.setNbOfLinesToSkip(textFileParserFrameController.getSkipLinesNumber());
                                trajectoryFile.setContainsHeader(textFileParserFrameController.getHeaderIndex() != -1);
                                trajectoryFile.setHeaderIndex(textFileParserFrameController.getHeaderIndex());
                                
                                trajController.setSelectedObject(trajectoryFile);
                                
                            }
                        });
                        
                    }
                }
            });
            
            pages.add(fxmlContext2.getRoot());
            
            //page 3
            FxmlContext fxmlContext3 = Util.loadFxml("/fxml/FileChooserTextField.fxml");
            FileChooserTextFieldController<File> outputController = (FileChooserTextFieldController) fxmlContext3.getController();
            outputController.getLabel().setText("Output directory");
            
            outputController.getButtonOpenFile().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    DirectoryChooser dc = new DirectoryChooser();
                    File selectedDir = dc.showDialog(new Stage());
                    
                    if (selectedDir != null) {
                        outputController.setSelectedObject(selectedDir);
                    }
                }
            });
            
            pages.add(fxmlContext3.getRoot());
            
            //page 4
            FxmlContext fxmlContext4 = Util.loadFxml("/fxml/FileChooserTextField.fxml");
            FileChooserTextFieldController<File> dtmController = (FileChooserTextFieldController) fxmlContext4.getController();
            dtmController.getLabel().setText("DTM file");
            
            dtmController.getButtonOpenFile().setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    
                    File selectedFile = dtmController.getFileChooser().showOpenDialog(new Stage());
                    
                    if (selectedFile != null) {
                        dtmController.setSelectedObject(selectedFile);
                    }
                }
            });
            
            pages.add(fxmlContext4.getRoot());
            
            //page 5
            
            TextField tfResolution = new TextField("5");
            HBox hboxRes = new HBox(new Label("Resolution"), tfResolution);
            hboxRes.setAlignment(Pos.CENTER_LEFT);
            hboxRes.setSpacing(5);
            
            Button executeButton = new Button("Execute");
            executeButton.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    try {
                        List<File> cfgFiles = executeScript(
                                listController.getListViewItems().getItems(),
                                trajController.getSelectedObject(),
                                dtmController.getSelectedObject(),
                                outputController.getSelectedObject(),
                                Float.valueOf(tfResolution.getText()));
                        
                        controller.addTasksToTaskList(cfgFiles.toArray(new File[cfgFiles.size()]));
                        
                    } catch (Exception ex) {
                        controller.showErrorDialog(ex);
                    }
                }
            });
            
            HBox hBox = new HBox(executeButton);
            hBox.setAlignment(Pos.CENTER_RIGHT);
            
            VBox vboxParams = new VBox(hboxRes);
            VBox.setVgrow(vboxParams, Priority.ALWAYS);
            
            VBox vbox = new VBox(vboxParams, hBox);
            vbox.setPadding(new Insets(5));
            VBox.setMargin(vbox, new Insets(5));
            
            pages.add(vbox);
            
        } catch (Exception ex) {
            controller.showErrorDialog(ex);
        }
    }
    
    @Override
    public void launch() {
        
        Pagination pagination = new Pagination(5);
        
        pagination.setPageFactory(new Callback<Integer, Node>() {
            @Override
            public Node call(Integer param) {
                return pages.get(param);
            }
        });
        
        Stage stage = new Stage();
        stage.setScene(new Scene(pagination));
        stage.showAndWait();
    }

    protected List<File> executeScript(List<File> lasFiles, CSVFile trajectoryFile, File dtmFile, File outputDirectory, float resolution) throws Exception {
        
        List<File> cfgFiles = new ArrayList<>();
        
        Raster dtm = null;

        if(dtmFile != null){
            
            try {
                dtm = AsciiGridHelper.readFromAscFile(dtmFile);
            } catch (Exception ex) {
                controller.showErrorDialog(ex);
                return null;
            }
        }
        
        List<Integer> classToIgnore = new ArrayList<>();
        /*classToIgnore.add(0);
        classToIgnore.add(7);
        classToIgnore.add(12);*/
        
        Matrix4d transfMatrix = new Matrix4d();
        transfMatrix.setIdentity();
        
        for(File lasFile : lasFiles){
            
            BoundingBox3d boundingBox = fr.amap.lidar.amapvox.util.Util.getBoundingBoxOfPoints(lasFile, transfMatrix, true, classToIgnore);
            
            Raster dtmSubset = dtm.subset(new BoundingBox2F(
                    new Point2F((float) boundingBox.min.x, (float) boundingBox.min.y), 
                    new Point2F((float) boundingBox.max.x, (float) boundingBox.max.y)), 0);

            File subsetDTMFile = new File(outputDirectory, lasFile.getName() +".asc");
            try {
                AsciiGridHelper.write(subsetDTMFile, dtmSubset, false);
            } catch (IOException ex) {
                controller.showErrorDialog(ex);
            }

            ALSVoxCfg cfg = new ALSVoxCfg();
            cfg.setInputFile(lasFile);
            cfg.setOutputFile(new File(outputDirectory, lasFile.getName()+".vox"), VoxelAnalysisCfg.VoxelsFormat.RASTER);
            cfg.setTrajectoryFile(trajectoryFile);
            EchoesWeightParams echoesWeightParams = new EchoesWeightParams();
            echoesWeightParams.setWeightingMode(EchoesWeightParams.WEIGHTING_ECHOS_NUMBER);
            echoesWeightParams.setWeightingData(EchoesWeightParams.DEFAULT_ALS_WEIGHTING);
            

            VoxelParameters voxelParameters = new VoxelParameters.Builder(
                    boundingBox.min, boundingBox.max, resolution, VoxelSpaceInfos.Type.ALS)
                    .ladParams(new LADParams())
                    .dtmFilteringParams(new DTMFilteringParams(subsetDTMFile, 1.0f))
                    .echoesWeightParams(echoesWeightParams)
                    .padMAX(5.0f)
                    .build();

            cfg.setVoxelParameters(voxelParameters);
            File cfgFile = new File(outputDirectory, lasFile.getName()+".cfg.xml");
            
            cfg.writeConfiguration(cfgFile, Global.buildVersion);
            
            cfgFiles.add(cfgFile);
        }
        
        return cfgFiles;
    }
    
}
