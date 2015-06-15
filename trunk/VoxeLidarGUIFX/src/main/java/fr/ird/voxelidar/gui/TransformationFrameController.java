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


import fr.ird.voxelidar.util.MatrixFileParser;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javax.vecmath.Matrix4d;

/**
 * FXML Controller class
 *
 * @author calcul
 */


public class TransformationFrameController implements Initializable {
    @FXML
    private TextField labelAxisRotationPhi;
    @FXML
    private TextField labelAxisRotationTheta;
    @FXML
    private TextField labelAxisRotationPsi;
    @FXML
    private TextField labelAxisRotationAngle;
    @FXML
    private TextField labelEulerRotationX;
    @FXML
    private TextField labelEulerRotationY;
    @FXML
    private TextField labelEulerRotationZ;
    @FXML
    private TextField labelTranslationX;
    @FXML
    private TextField labelTranslationY;
    @FXML
    private TextField labelTranslationZ;
    @FXML
    private TextField matrixM00;
    @FXML
    private TextField matrixM01;
    @FXML
    private TextField matrixM02;
    @FXML
    private TextField matrixM03;
    @FXML
    private TextField matrixM10;
    @FXML
    private TextField matrixM11;
    @FXML
    private TextField matrixM12;
    @FXML
    private TextField matrixM13;
    @FXML
    private TextField matrixM20;
    @FXML
    private TextField matrixM21;
    @FXML
    private TextField matrixM22;
    @FXML
    private TextField matrixM23;
    @FXML
    private TextField matrixM30;
    @FXML
    private TextField matrixM31;
    @FXML
    private TextField matrixM32;
    @FXML
    private TextField matrixM33;
    
    @FXML
    private CheckBox checkboxInverseTransformation;
    @FXML
    private Button buttonSetIdentity;
    @FXML
    private Button buttonOpenMatrixFile;

    
    private Stage stage;
    private MainFrameController parent;
    
    private FileChooser fileChooserOpenMatrixFile;
    private File lastMatrixFile;
    @FXML
    private Button buttonConfirm;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        setToIdentity();
        
        labelTranslationX.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                try{
                    double value = Double.valueOf(newValue);
                    matrixM03.setText(String.valueOf(value));
                    
                }catch(Exception e){}
            }
        });
        
        labelTranslationY.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                try{
                    double value = Double.valueOf(newValue);
                    matrixM13.setText(String.valueOf(value));
                    
                }catch(Exception e){}
            }
        });
        
        labelTranslationZ.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                try{
                    double value = Double.valueOf(newValue);
                    matrixM23.setText(String.valueOf(value));
                    
                }catch(Exception e){}
            }
        });
        
        checkboxInverseTransformation.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                Matrix4d matrix = getMatrix();
                matrix.invert();
                fillMatrix(matrix);
            }
        });
        
        fileChooserOpenMatrixFile = new FileChooser();
        fileChooserOpenMatrixFile.setTitle("Choose Matrix file");
    }
    
    public void setStage(Stage stage) {
        this.stage = stage;
    }
    
    public void setParent(MainFrameController controller){
        this.parent = controller;
    }
    
    private void fillMatrix(Matrix4d matrix){
        
        matrixM00.setText(String.valueOf(matrix.m00));
        matrixM01.setText(String.valueOf(matrix.m01));
        matrixM02.setText(String.valueOf(matrix.m02));
        matrixM03.setText(String.valueOf(matrix.m03));
        matrixM10.setText(String.valueOf(matrix.m10));
        matrixM11.setText(String.valueOf(matrix.m11));
        matrixM12.setText(String.valueOf(matrix.m12));
        matrixM13.setText(String.valueOf(matrix.m13));
        matrixM20.setText(String.valueOf(matrix.m20));
        matrixM21.setText(String.valueOf(matrix.m21));
        matrixM22.setText(String.valueOf(matrix.m22));
        matrixM23.setText(String.valueOf(matrix.m23));
        matrixM30.setText(String.valueOf(matrix.m30));
        matrixM31.setText(String.valueOf(matrix.m31));
        matrixM32.setText(String.valueOf(matrix.m32));
        matrixM33.setText(String.valueOf(matrix.m33));
    }
    
    public Matrix4d getMatrix(){
        
        Matrix4d matrix;
        
        try{
            matrix = new Matrix4d(
                Double.valueOf(matrixM00.getText()),
                Double.valueOf(matrixM01.getText()), 
                Double.valueOf(matrixM02.getText()), 
                Double.valueOf(matrixM03.getText()), 
                Double.valueOf(matrixM10.getText()),
                Double.valueOf(matrixM11.getText()),
                Double.valueOf(matrixM12.getText()), 
                Double.valueOf(matrixM13.getText()),
                Double.valueOf(matrixM20.getText()),
                Double.valueOf(matrixM21.getText()), 
                Double.valueOf(matrixM22.getText()),
                Double.valueOf(matrixM23.getText()),
                Double.valueOf(matrixM30.getText()),
                Double.valueOf(matrixM31.getText()),
                Double.valueOf(matrixM32.getText()),
                Double.valueOf(matrixM33.getText()));
            
            return matrix;
            
        }catch(Exception e){}
        
        matrix = new Matrix4d();
        matrix.setIdentity();
        
        return matrix;
        
    }
    
    private void setToIdentity(){
        
        Matrix4d matrix = new Matrix4d();
        matrix.setIdentity();
        fillMatrix(matrix);
    }

    @FXML
    private void onActionButtonSetIdentity(ActionEvent event) {
        setToIdentity();
    }

    @FXML
    private void onActionButtonOpenMatrixFile(ActionEvent event) {
        
        if(lastMatrixFile != null){
            fileChooserOpenMatrixFile.setInitialDirectory(lastMatrixFile.getParentFile());
        }
        
        File selectedFile = fileChooserOpenMatrixFile.showOpenDialog(stage);
        
        if (selectedFile != null) {

            lastMatrixFile = selectedFile;
            
            Matrix4d mat = MatrixFileParser.getMatrixFromFile(selectedFile);
            if (mat != null) {
                fillMatrix(mat);
            } else {
                parent.showMatrixFormatErrorDialog();
            }
        }
    }

    @FXML
    private void onActionButtonConfirm(ActionEvent event) {
    }
}
