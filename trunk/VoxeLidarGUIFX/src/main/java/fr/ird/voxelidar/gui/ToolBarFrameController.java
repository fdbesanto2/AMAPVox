package fr.ird.voxelidar.gui;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import fr.ird.voxelidar.engine3d.object.camera.TrackballCamera;
import fr.ird.voxelidar.engine3d.renderer.JoglListener;
import fr.ird.voxelidar.util.ColorGradient;
import fr.ird.voxelidar.util.CombinedFilter;
import fr.ird.voxelidar.util.Filter;
import java.awt.Color;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ToolBarFrameController implements Initializable {
    
    private final static Logger logger = Logger.getLogger(ToolBarFrameController.class);
    
    private JoglListener joglContext;
    private ArrayList<String> gradientColorNames;
    private ArrayList<Color[]> gradientColors;
    
    @FXML
    private ComboBox<String> comboBoxAttributeToShow;
    @FXML
    private ComboBox<String> comboboxGradient;
    @FXML
    private TextField textFieldVoxelSize;
    @FXML
    private Button buttonApplyVoxelSize;
    @FXML
    private Slider sliderColorRed;
    @FXML
    private Slider sliderColorGreen;
    @FXML
    private Slider sliderColorBlue;
    @FXML
    private TextField textFieldMinValue;
    @FXML
    private TextField textFieldMaxValue;
    @FXML
    private Button buttonResetMinMax;
    @FXML
    private TextField textFieldFilterValues;
    private CheckBox comboboxStretched;
    @FXML
    private CheckBox checkboxStretched;
    @FXML
    private RadioButton radiobuttonDontDisplayValues;
    @FXML
    private RadioButton radiobuttonDisplayValues;
    @FXML
    private Tooltip tooltipTextfieldFilter;
    @FXML
    private Button buttonApplyMinMax;
    @FXML
    private TextField textfieldCameraFOV;
    @FXML
    private TextField textfieldCameraLeft;
    @FXML
    private TextField textfieldCameraRight;
    @FXML
    private TextField textfieldCameraTop;
    @FXML
    private TextField textfieldCameraBottom;
    @FXML
    private TextField textfieldCameraFar;
    @FXML
    private TextField textfieldCameraNear;
    @FXML
    private RadioButton radiobuttonPerspectiveCamera;
    @FXML
    private RadioButton radiobuttonOrthographicCamera;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Class c = ColorGradient.class;
        Field[] fields = c.getFields();
        
        gradientColorNames = new ArrayList<>();
        gradientColors = new ArrayList<>();
        
        try {
            
            for (Field field : fields) {
                
                String type = field.getType().getSimpleName();
                if (type.equals("Color[]")) {
                    gradientColorNames.add(field.getName());
                    gradientColors.add((Color[])field.get(c));
                }
            }
            comboboxGradient.getItems().addAll(gradientColorNames);
            comboboxGradient.getSelectionModel().select("HEAT");
            
        } catch (IllegalArgumentException | IllegalAccessException ex) {
            logger.error(ex);
        }
        
        comboboxGradient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                String gradient = newValue;
                Color[] gradientColor = ColorGradient.GRADIENT_RAINBOW;

                for (int i = 0;i<gradientColorNames.size();i++) {

                    if(gradientColorNames.get(i).equals(gradient)){
                        gradientColor = gradientColors.get(i);
                        i = gradientColorNames.size() - 1;
                    }
                }

                //recalculate voxel color with the new gradient
                joglContext.getScene().getVoxelSpace().updateColorValue(gradientColor);

                //update instance color buffer to gpu
                joglContext.getScene().getVoxelSpace().updateInstanceColorBuffer();

                joglContext.drawNextFrame();
        
            }
        });
        
        sliderColorRed.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Vec3F worldColor = joglContext.getWorldColor();
                joglContext.setWorldColor(new Vec3F((float) (sliderColorRed.getValue()/255.0), worldColor.y,worldColor.z));
                sliderColorRed.setTooltip(new Tooltip(String.valueOf(sliderColorRed.getValue())));
                try{
                    joglContext.drawNextFrame();
                }catch(Exception e){}
                
            }
        });
        
        sliderColorGreen.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Vec3F worldColor = joglContext.getWorldColor();
                joglContext.setWorldColor(new Vec3F(worldColor.x, (float) (sliderColorGreen.getValue()/255.0), worldColor.z));
                sliderColorGreen.setTooltip(new Tooltip(String.valueOf(sliderColorGreen.getValue())));
                try{
                    joglContext.drawNextFrame();
                }catch(Exception e){}
            }
        });
        
        sliderColorBlue.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                Vec3F worldColor = joglContext.getWorldColor();
                joglContext.setWorldColor(new Vec3F(worldColor.x,worldColor.y, (float) (sliderColorBlue.getValue()/255.0)));
                sliderColorBlue.setTooltip(new Tooltip(String.valueOf(sliderColorBlue.getValue())));
                try{
                    joglContext.drawNextFrame();
                }catch(Exception e){}
            }
        });
        comboBoxAttributeToShow.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    joglContext.getScene().getVoxelSpace().resetAttributValueRange();
                    joglContext.getScene().getVoxelSpace().changeCurrentAttribut(newValue);
                    joglContext.getScene().getVoxelSpace().updateInstanceColorBuffer();
                    joglContext.drawNextFrame();
                    textFieldMinValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMin));
                    textFieldMaxValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMax));
                    
                }catch(Exception e){}
                
            }
        });
        
        textFieldFilterValues.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                updateValuesFilter();
            }
        });
        
        checkboxStretched.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    joglContext.getScene().getVoxelSpace().setStretched(true);
                    joglContext.getScene().getVoxelSpace().updateValue();
                    joglContext.getScene().getVoxelSpace().updateInstanceColorBuffer();
                    
                    
                    joglContext.drawNextFrame();
                }else{
                    joglContext.getScene().getVoxelSpace().setStretched(false);
                    joglContext.getScene().getVoxelSpace().updateValue();
                    joglContext.getScene().getVoxelSpace().updateInstanceColorBuffer();
                    
                    
                    joglContext.drawNextFrame();
                }
                
            }
        });
        
        ToggleGroup groupDisplayValues = new ToggleGroup();
        radiobuttonDisplayValues.setToggleGroup(groupDisplayValues);
        radiobuttonDontDisplayValues.setToggleGroup(groupDisplayValues);
        
        radiobuttonDontDisplayValues.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                updateValuesFilter();
            }
        });
        
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                textFieldVoxelSize.setText(String.valueOf(joglContext.getScene().getVoxelSpace().getCubeSize()));
                textFieldMinValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMin));
                textFieldMaxValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMax));
            }
        });
        
        
        tooltipTextfieldFilter.setText("Syntax: value1, value2\nvalue can be a floating point number\nor a value range [1.0->2.0[");
        
        textfieldCameraFOV.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float fov = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getCamera();
                    joglContext.getCamera().setPerspective(fov, camera.getAspect(), camera.getNearPersp(), camera.getFarPersp());
                    joglContext.drawNextFrame();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraTop.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float top = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getCamera();
                    joglContext.getCamera().setOrthographic(camera.getLeft(), camera.getRight(), top, camera.getBottom(), camera.getNearOrtho(), camera.getFarOrtho());
                    joglContext.drawNextFrame();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraBottom.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float bottom = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getCamera();
                    joglContext.getCamera().setOrthographic(camera.getLeft(), camera.getRight(), camera.getTop(), bottom, camera.getNearOrtho(), camera.getFarOrtho());
                    joglContext.drawNextFrame();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraLeft.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float left = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getCamera();
                    joglContext.getCamera().setOrthographic(left, camera.getRight(), camera.getTop(), camera.getBottom(), camera.getNearOrtho(), camera.getFarOrtho());
                    joglContext.drawNextFrame();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraRight.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float right = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getCamera();
                    joglContext.getCamera().setOrthographic(camera.getLeft(), right, camera.getTop(), camera.getBottom(), camera.getNearOrtho(), camera.getFarOrtho());
                    joglContext.drawNextFrame();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraNear.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float near = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getCamera();
                    if(radiobuttonOrthographicCamera.isSelected()){
                        joglContext.getCamera().setOrthographic(camera.getLeft(), camera.getRight(), camera.getTop(), camera.getBottom(), near, camera.getFarOrtho());
                    }else{
                        joglContext.getCamera().setPerspective(camera.getFovy(), camera.getAspect(), near, camera.getFarPersp());
                    }
                    
                    joglContext.drawNextFrame();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraFar.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float far = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getCamera();
                    if(radiobuttonOrthographicCamera.isSelected()){
                        joglContext.getCamera().setOrthographic(camera.getLeft(), camera.getRight(), camera.getTop(), camera.getBottom(), camera.getNearOrtho(), far);
                    }else{
                        joglContext.getCamera().setPerspective(camera.getFovy(), camera.getAspect(), camera.getNearPersp(), far);
                    }
                    
                    joglContext.drawNextFrame();
                    
                }catch(Exception e){}
            }
        });
        
        ToggleGroup groupCameraMod = new ToggleGroup();
        radiobuttonPerspectiveCamera.setToggleGroup(groupCameraMod);
        radiobuttonOrthographicCamera.setToggleGroup(groupCameraMod);
        
        radiobuttonOrthographicCamera.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    
                    try{
                        float left = Float.valueOf(textfieldCameraLeft.getText());
                        float right = Float.valueOf(textfieldCameraRight.getText());
                        float top = Float.valueOf(textfieldCameraTop.getText());
                        float bottom = Float.valueOf(textfieldCameraBottom.getText());
                        float near = Float.valueOf(textfieldCameraNear.getText());
                        float far = Float.valueOf(textfieldCameraFar.getText());

                        joglContext.getCamera().setOrthographic(left, right, top, bottom, near, far);
                        joglContext.drawNextFrame();
                    }catch(Exception e){}
                    
                }
            }
        });
        
        radiobuttonPerspectiveCamera.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    
                    try{
                        float fov = Float.valueOf(textfieldCameraFOV.getText());
                        float near = Float.valueOf(textfieldCameraNear.getText());
                        float far = Float.valueOf(textfieldCameraFar.getText());

                        TrackballCamera camera = joglContext.getCamera();
                        joglContext.getCamera().setPerspective(fov, camera.getAspect(), near, far);
                        joglContext.drawNextFrame();
                        
                    }catch(Exception e){}
                    
                }
            }
        });
    }
    
    private void updateValuesFilter(){
        
        String[] valuesArray = textFieldFilterValues.getText().replace(" ", "").split(",");
        Set<CombinedFilter> filterValues = new HashSet<>();
        for(int i=0;i<valuesArray.length;i++){
            try{
                if(valuesArray[i].contains("[") || valuesArray[i].contains("]") ){
                    int index = valuesArray[i].indexOf("->");
                    
                    if(index != -1){
                        char firstInequality = valuesArray[i].charAt(0);
                        char secondInequality = valuesArray[i].charAt(valuesArray[i].length()-1);
                        
                        
                        float firstValue = Float.valueOf(valuesArray[i].substring(1, index));
                        float secondValue = Float.valueOf(valuesArray[i].substring(index+2, valuesArray[i].length()-1));
                        
                        int firstInequalityID;
                        switch(firstInequality){
                            case ']':
                                firstInequalityID = Filter.GREATER_THAN;
                                break;
                            case '[':
                                firstInequalityID = Filter.GREATER_THAN_OR_EQUAL;
                                break;
                            default:
                                firstInequalityID = Filter.GREATER_THAN_OR_EQUAL;
                        }
                        
                        int secondInequalityID;
                        switch(secondInequality){
                            case ']':
                                secondInequalityID = Filter.LESS_THAN_OR_EQUAL;
                                break;
                            case '[':
                                secondInequalityID = Filter.LESS_THAN;
                                break;
                            default:
                                secondInequalityID = Filter.LESS_THAN_OR_EQUAL;
                        }
                        
                        
                        filterValues.add(new CombinedFilter(
                                new Filter("x", firstValue, firstInequalityID), 
                                new Filter("x", secondValue, secondInequalityID), CombinedFilter.AND));
                    }
                    
                }else{
                    filterValues.add(new CombinedFilter(
                                new Filter("x", Float.valueOf(valuesArray[i]), Filter.EQUAL), 
                                null, CombinedFilter.AND));
                }
                
            }catch(Exception e){}
        }

        joglContext.getScene().getVoxelSpace().setFilterValues(filterValues, radiobuttonDisplayValues.isSelected());
        joglContext.getScene().getVoxelSpace().updateColorValue(joglContext.getScene().getVoxelSpace().getGradient());
        joglContext.getScene().getVoxelSpace().updateInstanceColorBuffer();
        joglContext.drawNextFrame();
    }
    
    public void setJoglListener(JoglListener joglContext){
        this.joglContext = joglContext;
    }
    
    public void setAttributes(List<String> attributes){
        comboBoxAttributeToShow.getItems().addAll(attributes);
        comboBoxAttributeToShow.getSelectionModel().select(joglContext.getSettings().attributeToVisualize);
    }

    @FXML
    private void onActionButtonApplyVoxelSize(ActionEvent event) {
        
        Float voxelSize = Float.valueOf(textFieldVoxelSize.getText());
        
        joglContext.getScene().getVoxelSpace().updateCubeSize(null, voxelSize);
        joglContext.drawNextFrame();
    }

    @FXML
    private void onActionButtonResetMinMax(ActionEvent event) {
        
        textFieldMinValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMin));
        textFieldMaxValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMax));
        
        joglContext.getScene().getVoxelSpace().resetAttributValueRange();
        joglContext.getScene().getVoxelSpace().updateValue();
        joglContext.getScene().getVoxelSpace().updateColorValue(joglContext.getScene().getVoxelSpace().getGradient());
        joglContext.getScene().getVoxelSpace().updateInstanceColorBuffer();
        joglContext.drawNextFrame();
    }

    @FXML
    private void onActionButtonApplyMinMax(ActionEvent event) {
        
        try{
            float min = Float.valueOf(textFieldMinValue.getText());
            float max = Float.valueOf(textFieldMaxValue.getText());
            
            joglContext.getScene().getVoxelSpace().setAttributValueRange(min, max);
            joglContext.getScene().getVoxelSpace().updateValue();
            joglContext.getScene().getVoxelSpace().updateColorValue(joglContext.getScene().getVoxelSpace().getGradient());
            joglContext.getScene().getVoxelSpace().updateInstanceColorBuffer();
            joglContext.drawNextFrame();
            
        }catch(Exception e){
            logger.error("error");
        }
        
        
    }
    
}
