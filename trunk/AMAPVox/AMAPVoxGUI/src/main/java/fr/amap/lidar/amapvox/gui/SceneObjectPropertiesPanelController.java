/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.util.ColorGradient;
import fr.amap.lidar.amapvox.voxviewer.object.scene.PointCloudSceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.ScalarField;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import fr.amap.lidar.amapvox.voxviewer.object.scene.VoxelSpaceSceneObject;
import java.awt.Color;
import java.lang.reflect.Field;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.ResourceBundle;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author calcul
 */
public class SceneObjectPropertiesPanelController implements Initializable {
    @FXML
    private ComboBox<String> comboboxActiveScalarField;
    @FXML
    private ComboBox<ColorString> comboboxActiveGradient;
    @FXML
    private Button buttonConfigureGradient;
    @FXML
    private Spinner<Double> spinnerMinimumDisplayValue;
    @FXML
    private Spinner<Double> spinnerMaximumDisplayValue;
    @FXML
    private Button buttonConfigureMatrix;

    private ArrayList<String> gradientColorNames;
    private ArrayList<Color[]> gradientColors;
    private SceneObjectWrapper sceneObjectWrapper;
    private MainFrameController context;
    
    private final static Logger logger = Logger.getLogger(SceneObjectPropertiesPanelController.class);
    @FXML
    private AreaChart<Number, Number> areaChartScalarFieldValues;
    @FXML
    private TextField textFieldM10;
    @FXML
    private TextField textFieldM00;
    @FXML
    private TextField textFieldM01;
    @FXML
    private TextField textFieldM02;
    @FXML
    private TextField textFieldM03;
    @FXML
    private TextField textFieldM21;
    @FXML
    private TextField textFieldM31;
    @FXML
    private TextField textFieldM13;
    @FXML
    private TextField textFieldM30;
    @FXML
    private TextField textFieldM20;
    @FXML
    private TextField textFieldM23;
    @FXML
    private TextField textFieldM11;
    @FXML
    private TextField textFieldM32;
    @FXML
    private TextField textFieldM33;
    @FXML
    private TextField textFieldM12;
    @FXML
    private TextField textFieldM22;
    
    private class ColorString{
        
        public Color[] color;
        public String name;

        public ColorString(String name, Color[] color) {
            this.name = name;
            this.color = color;
        }
        
        @Override
        public String toString(){
            return name;
        }
    }
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        gradientColorNames = new ArrayList<>();
        gradientColors = new ArrayList<>();
        
        Class c = ColorGradient.class;
        Field[] fields = c.getFields();
        
        try {
            
            for (Field field : fields) {
                
                String type = field.getType().getSimpleName();
                if (type.equals("Color[]")) {
                    gradientColorNames.add(field.getName());
                    gradientColors.add((Color[])field.get(c));
                    comboboxActiveGradient.getItems().add(new ColorString(field.getName(), (Color[])field.get(c)));
                }
                
                
            }
            
            comboboxActiveGradient.getSelectionModel().selectFirst();
            
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.error(ex);
        }
        
        comboboxActiveScalarField.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                if(sceneObjectWrapper.getSceneObject() instanceof PointCloudSceneObject){
                    if(((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().containsKey(newValue)){
                    
                        updateValues(((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().get(newValue));

                        ((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).switchColor(newValue);
                    }
                }else if(sceneObjectWrapper.getSceneObject() instanceof VoxelSpaceSceneObject){
                    
                    //updateValues(((VoxelSpaceSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().get(newValue));
                    //((VoxelSpaceSceneObject)sceneObjectWrapper.getSceneObject()).switchColor(newValue);
                }
               
            }
        });
        
        comboboxActiveGradient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ColorString>() {

            @Override
            public void changed(ObservableValue<? extends ColorString> observable, ColorString oldValue, ColorString newValue) {
                
                if(sceneObjectWrapper.getSceneObject() instanceof PointCloudSceneObject){
                    ((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().get(comboboxActiveScalarField.getSelectionModel().getSelectedItem()).setGradientColor(newValue.color);
                    ((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).updateColor();
                }else if(sceneObjectWrapper.getSceneObject() instanceof SceneObject){
                    
                }
            }
        });
    }
    

    public void setSceneObjectWrapper(final SceneObjectWrapper selectedItem) {
        
        sceneObjectWrapper = selectedItem;
        
        if(sceneObjectWrapper.getSceneObject() instanceof PointCloudSceneObject){
            
            comboboxActiveScalarField.getItems().setAll(((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().keySet());
            
            if(!((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().isEmpty()){
                updateValues(((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().get(comboboxActiveScalarField.getItems().get(0)));
            }
            
        }else if(sceneObjectWrapper.getSceneObject() instanceof VoxelSpaceSceneObject){
            
            comboboxActiveScalarField.getItems().setAll(((VoxelSpaceSceneObject)sceneObjectWrapper.getSceneObject()).getVariables());
            
        }
        
        setTransfMatrix(selectedItem.getTransfMatrix());
        
        comboboxActiveScalarField.getSelectionModel().selectFirst();
    }
    
    private void setTransfMatrix(Mat4D matrix){
        
        if(matrix == null){
            matrix = Mat4D.identity();
        }
        
        textFieldM00.setText(String.valueOf(matrix.mat[0]));
        textFieldM01.setText(String.valueOf(matrix.mat[1]));
        textFieldM02.setText(String.valueOf(matrix.mat[2]));
        textFieldM03.setText(String.valueOf(matrix.mat[3]));
        textFieldM10.setText(String.valueOf(matrix.mat[4]));
        textFieldM11.setText(String.valueOf(matrix.mat[5]));
        textFieldM12.setText(String.valueOf(matrix.mat[6]));
        textFieldM13.setText(String.valueOf(matrix.mat[7]));
        textFieldM20.setText(String.valueOf(matrix.mat[8]));
        textFieldM21.setText(String.valueOf(matrix.mat[9]));
        textFieldM22.setText(String.valueOf(matrix.mat[10]));
        textFieldM23.setText(String.valueOf(matrix.mat[11]));
        textFieldM30.setText(String.valueOf(matrix.mat[12]));
        textFieldM31.setText(String.valueOf(matrix.mat[13]));
        textFieldM32.setText(String.valueOf(matrix.mat[14]));
        textFieldM33.setText(String.valueOf(matrix.mat[15]));
    }
    
    private void updateValues(ScalarField scalarField){
                        
        double minValue = scalarField.getStatistic().getMinValue();
        double maxValue = scalarField.getStatistic().getMaxValue();

        SpinnerValueFactory.DoubleSpinnerValueFactory factory = new SpinnerValueFactory.DoubleSpinnerValueFactory(minValue, maxValue, 0.0d);

        spinnerMinimumDisplayValue.setValueFactory(factory);
        spinnerMaximumDisplayValue.setValueFactory(factory);

        spinnerMinimumDisplayValue.getEditor().setText(String.valueOf(minValue));
        spinnerMaximumDisplayValue.getEditor().setText(String.valueOf(maxValue));
        
        XYChart.Series seriesValues= new XYChart.Series();
        seriesValues.setName("Values");
        
        for(int i=0;i<scalarField.histogramFrequencyCount.length;i++){
            seriesValues.getData().add(new XYChart.Data(scalarField.histogramValue[i], scalarField.histogramFrequencyCount[i]));
        }
        
        areaChartScalarFieldValues.getData().setAll(seriesValues);
    }
    
}
