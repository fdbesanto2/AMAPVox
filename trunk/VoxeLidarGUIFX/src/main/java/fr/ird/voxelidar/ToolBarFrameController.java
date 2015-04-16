package fr.ird.voxelidar;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import fr.ird.voxelidar.engine3d.renderer.JoglListener;
import fr.ird.voxelidar.util.ColorGradient;
import java.awt.Color;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.TreeSet;
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
        
        ToggleGroup group = new ToggleGroup();
        radiobuttonDisplayValues.setToggleGroup(group);
        radiobuttonDontDisplayValues.setToggleGroup(group);
        
        radiobuttonDontDisplayValues.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                updateValuesFilter();
            }
        });
        
    }
    
    private void updateValuesFilter(){
        
        String[] valuesArray = textFieldFilterValues.getText().replace(" ", "").split(",");
        Set<Float> values = new TreeSet<>();
        for(int i=0;i<valuesArray.length;i++){
            try{
                values.add(Float.valueOf(valuesArray[i]));
            }catch(Exception e){}
        }

        joglContext.getScene().getVoxelSpace().setFilterValues(values, radiobuttonDisplayValues.isSelected());
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
    }
    
}
