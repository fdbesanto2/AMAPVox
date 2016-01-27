/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui;

import fr.amap.amapvox.commons.util.ColorGradient;
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
                
                if(((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().containsKey(newValue)){
                    
                    updateValues(((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().get(newValue));
                    
                    ((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).switchColor(newValue);
                }
               
            }
        });
        
        comboboxActiveGradient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<ColorString>() {

            @Override
            public void changed(ObservableValue<? extends ColorString> observable, ColorString oldValue, ColorString newValue) {
                
                if(((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()) instanceof PointCloudSceneObject){
                    ((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).getScalarFieldsList().get(comboboxActiveScalarField.getSelectionModel().getSelectedItem()).setGradientColor(newValue.color);
                    ((PointCloudSceneObject)sceneObjectWrapper.getSceneObject()).updateColor();
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
        
        comboboxActiveScalarField.getSelectionModel().selectFirst();
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
