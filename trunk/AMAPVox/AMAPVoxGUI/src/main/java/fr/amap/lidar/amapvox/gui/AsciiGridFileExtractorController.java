/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.javafx.io.FileChooserContext;
import fr.amap.commons.javafx.matrix.TransformationFrameController;
import fr.amap.lidar.amapvox.gui.viewer3d.VoxelSpaceSceneObject;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javax.vecmath.Matrix4d;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class AsciiGridFileExtractorController implements Initializable {

    private final static Logger LOGGER = Logger.getLogger(AsciiGridFileExtractorController.class);
    
    private FileChooserContext fcRaster;
    private Stage stage;
    private TransformationFrameController transformationFrameController;
    private Stage transformationFrame;
    private Matrix4d rasterTransfMatrix;
    private boolean wasHidden;
    
    @FXML
    private TextField textfieldRasterFilePath;
    @FXML
    private CheckBox checkboxFitRasterToVoxelSpace;
    @FXML
    private ComboBox<VoxelSpaceSceneObject> comboboxSelectVoxelSpaceToFitTo;
    @FXML
    private TextField textfieldRasterFittingMargin;
    @FXML
    private HBox hboxFitToVoxelSpaceParams;
    @FXML
    private CheckBox checkboxUseTransformationMatrix;
    @FXML
    private Button buttonSetTransformationMatrix;
    @FXML
    private CheckBox checkboxBuildOctree;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        fcRaster = new FileChooserContext();
        hboxFitToVoxelSpaceParams.disableProperty().bind(checkboxFitRasterToVoxelSpace.selectedProperty().not());
        
        try {
            transformationFrameController = TransformationFrameController.getInstance();
            transformationFrame = transformationFrameController.getStage();
        } catch (Exception ex) {
            LOGGER.error("Cannot load fxml file", ex);
        }
    }
    
    public static AsciiGridFileExtractorController getInstance() throws IOException, Exception{
        
        try {
            FXMLLoader loader = new FXMLLoader(AsciiGridFileExtractorController.class.getResource("/fxml/AsciiGridFileExtractor.fxml"));
            Parent root = loader.load();
            Stage stage = new Stage();
            AsciiGridFileExtractorController controller = loader.getController();
            stage.setScene(new Scene(root));
            controller.setStage(stage);
            return controller;
            
        } catch (IOException ex) {
            throw ex;
        }catch (Exception ex) {
            throw ex;
        }
    }

    public void init(File ascFile, List<VoxelSpaceSceneObject> vsSceneObjectsList){
        
        textfieldRasterFilePath.setText(ascFile.getAbsolutePath());
        wasHidden = false;
        
        if(vsSceneObjectsList != null && vsSceneObjectsList.size() > 0){
            comboboxSelectVoxelSpaceToFitTo.getItems().setAll(vsSceneObjectsList);
            checkboxFitRasterToVoxelSpace.setDisable(false);
        }else{
            checkboxFitRasterToVoxelSpace.setDisable(true);
        }
    }
    
    public void setStage(Stage stage){
        this.stage = stage;
    }

    public Stage getStage() {
        return stage;
    }

    public boolean wasHidden() {
        return wasHidden;
    }

    @FXML
    private void onActionButtonSelectRasterFile(ActionEvent event) {
        
        File selectedFile = fcRaster.showOpenDialog(stage);
        
        if(selectedFile != null){
            textfieldRasterFilePath.setText(selectedFile.getAbsolutePath());
        }
    }

    @FXML
    private void onActionButtonSetTransformationMatrix(ActionEvent event) {
        
        transformationFrameController.reset();
        
        transformationFrame.setOnHidden(new EventHandler<WindowEvent>() {

            @Override
            public void handle(WindowEvent event) {
                
                if(transformationFrameController.isConfirmed()){
                    
                    rasterTransfMatrix = transformationFrameController.getMatrix();
                
                    if (rasterTransfMatrix == null) {
                        rasterTransfMatrix = new Matrix4d();
                        rasterTransfMatrix.setIdentity();
                    }
                }
                
            }
        });
        
        transformationFrame.show();
    }

    @FXML
    private void onActionCheckboxUseTransformationMatrix(ActionEvent event) {
        
    }

    @FXML
    private void onActionButtonImport(ActionEvent event) {
        
        wasHidden = true;
        stage.close();
    }
    
    public File getRasterFile(){
        return new File(textfieldRasterFilePath.getText());
    }

    public Matrix4d getRasterTransfMatrix() {
        return rasterTransfMatrix;
    }
    
    public boolean isTransfMatrixEnabled(){
        
        return checkboxUseTransformationMatrix.isSelected();
    }
    
    public boolean isFittingToVoxelSpaceEnabled(){
        return checkboxFitRasterToVoxelSpace.isSelected();
    }
    
    public VoxelSpaceSceneObject getVoxelSpaceToFitTo(){
        return comboboxSelectVoxelSpaceToFitTo.getSelectionModel().getSelectedItem();
    }
    
    public int getFittingMargin(){
        return Integer.valueOf(textfieldRasterFittingMargin.getText());
    }
    
    public boolean isOctreeWanted(){
        return checkboxBuildOctree.isSelected();
    }
    
}
