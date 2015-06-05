package fr.ird.voxelidar.gui;
/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */


import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpaceAdapter;
import fr.ird.voxelidar.lidar.format.dart.DartWriter;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.controlsfx.dialog.ProgressDialog;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class DartExporterFrameController implements Initializable {

    private Stage stage;
    private FileChooser fileChooserSaveMaketFile;
    private FileChooser fileChooserSaveTrianglesFile;
    private FileChooser fileChooserOpenDTMFile;
    
    private File voxelFile;
    
    @FXML
    private CheckBox checkboxGenerateTrianglesFile;
    @FXML
    private Button buttonOpenDTMFile;
    @FXML
    private Button buttonOpenTriangleFile;
    @FXML
    private Button buttonOpenMaketFile;
    @FXML
    private TextField textfieldTrianglesFilePath;
    @FXML
    private TextField textfieldDTMFilePath;
    @FXML
    private TextField textfieldMaketFilePath;
    @FXML
    private AnchorPane anchorpaneTrianglesGeneration;
    @FXML
    private Button buttonExportMaket;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        EventHandler<DragEvent> dragOverEvent = new EventHandler<DragEvent>() {

            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                if (db.hasFiles()) {
                    event.acceptTransferModes(TransferMode.COPY);
                } else {
                    event.consume();
                }
            }
        };
        
        textfieldDTMFilePath.setOnDragOver(dragOverEvent);
        textfieldMaketFilePath.setOnDragOver(dragOverEvent);
        textfieldTrianglesFilePath.setOnDragOver(dragOverEvent);
        
        setDragDroppedSingleFileEvent(textfieldDTMFilePath);
        setDragDroppedSingleFileEvent(textfieldMaketFilePath);
        setDragDroppedSingleFileEvent(textfieldTrianglesFilePath);
        
        fileChooserSaveMaketFile = new FileChooser();
        fileChooserSaveMaketFile.setTitle("Save maket file");
        fileChooserSaveMaketFile.setInitialFileName("maket.txt");
        
        fileChooserSaveTrianglesFile = new FileChooser();
        fileChooserSaveTrianglesFile.setTitle("Save triangle file");
        fileChooserSaveTrianglesFile.setInitialFileName("triangle.txt");
        
        fileChooserOpenDTMFile = new FileChooser();
        fileChooserOpenDTMFile.setTitle("Choose DTM file");
        fileChooserOpenDTMFile.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Files", "*"),
                new FileChooser.ExtensionFilter("DTM Files", "*.asc"));
        
        checkboxGenerateTrianglesFile.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                anchorpaneTrianglesGeneration.setDisable(!newValue);
            }
        });
    }    

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setVoxelFile(File voxelFile) {
        this.voxelFile = voxelFile;
        
        textfieldMaketFilePath.setText(voxelFile.getParent()+"/"+"maket.txt");
        textfieldTrianglesFilePath.setText(voxelFile.getParent()+"/"+"triangle.txt");
    }

    @FXML
    private void onActionButtonOpenDTMFile(ActionEvent event) {
        
        File dtmFile = fileChooserOpenDTMFile.showOpenDialog(stage);
        
        if(dtmFile != null){
            textfieldDTMFilePath.setText(dtmFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenTriangleFile(ActionEvent event) {
        
        File triangleFile = fileChooserSaveTrianglesFile.showSaveDialog(stage);
        
        if(triangleFile != null){
            textfieldTrianglesFilePath.setText(triangleFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonOpenMaketFile(ActionEvent event) {
        
        File maketFile = fileChooserSaveMaketFile.showSaveDialog(stage);
        
        if(maketFile != null){
            textfieldMaketFilePath.setText(maketFile.getAbsolutePath());
        }
    }
    
    private void setDragDroppedSingleFileEvent(final TextField textField) {

        textField.setOnDragDropped(new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasFiles() && db.getFiles().size() == 1) {
                    success = true;
                    for (File file : db.getFiles()) {
                        if (file != null) {
                            textField.setText(file.getAbsolutePath());
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            }
        });
    }

    @FXML
    private void onActionButtonExportMaket(ActionEvent event) {
        
        final File voxelFileToProcess = voxelFile;
        final File maketFile = new File(textfieldMaketFilePath.getText());
        final File dtmFile = new File(textfieldDTMFilePath.getText());
        final File triangleFile = new File(textfieldTrianglesFilePath.getText());
        final boolean generateTriangleFile = checkboxGenerateTrianglesFile.isSelected();
        
        ProgressDialog d;
        Service<Void> service;

        service = new Service<Void>() {

            @Override
            protected Task<Void> createTask() {
                return new Task<Void>() {
                    @Override
                    protected Void call() throws InterruptedException {
                        final VoxelSpace voxelSpace = new VoxelSpace();
                        voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {
                            @Override
                            public void voxelSpaceCreationFinished() {
                                DartWriter dartWriter = new DartWriter();
                                dartWriter.setDtmFile(dtmFile);
                                
                                Mat4D transfMatrix = new Mat4D();
                                transfMatrix.mat=new double[]{0.9540688863574789, 0.29958731629459895, 0.0, -448120.0441687209,
                                                                -0.29958731629459895, 0.9540688863574789, 0.0, -470918.3928060016,
                                                                0.0, 0.0, 1.0, 0.0,
                                                                0.0, 0.0, 0.0, 1.0};
                                
                                dartWriter.setTransfMatrix(transfMatrix);
                                dartWriter.setTrianglesFile(triangleFile);
                                dartWriter.setGenerateTrianglesFile(generateTriangleFile);
                                dartWriter.writeFromVoxelSpace(voxelSpace.data, maketFile);
                            }
                        });

                        voxelSpace.loadFromFile(voxelFileToProcess);
                        
                        return null;
                    }
                };
            }
        };

        d = new ProgressDialog(service);
        d.initOwner(stage);
        d.setResizable(true);
        d.show();

        service.start();
    }
}