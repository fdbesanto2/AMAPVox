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
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ToolBoxFrameController implements Initializable {
    
    private final static Logger logger = Logger.getLogger(ToolBoxFrameController.class);
    
    private JoglListener joglContext;
    private ArrayList<String> gradientColorNames;
    private ArrayList<Color[]> gradientColors;
    
    private Stage stage;
    
    private double originalStageWidth;
    private double originalContentPaneWidth;
    private boolean isHidden;
    
    public double maxHeight;
    
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
    private TextField textfieldCameraFar;
    @FXML
    private TextField textfieldCameraNear;
    @FXML
    private RadioButton radiobuttonPerspectiveCamera;
    @FXML
    private RadioButton radiobuttonOrthographicCamera;
    @FXML
    private Button buttonHideToolBox;
    @FXML
    private TabPane tabpaneContent;
    @FXML
    private ImageView imageViewArrowHiddingPane;
    @FXML
    private Button buttonViewTop;
    @FXML
    private Button buttonViewRight;
    @FXML
    private Button buttonViewBottom;
    @FXML
    private Button buttonViewLeft;
    @FXML
    private Button buttonViewFront;
    @FXML
    private Button buttonViewBack;
    @FXML
    private Button buttonViewIsometric;
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        Class c = ColorGradient.class;
        Field[] fields = c.getFields();
        
        gradientColorNames = new ArrayList<>();
        gradientColors = new ArrayList<>();
        
        isHidden = false;
        
        //initContent();
        
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
        
        Task colorChangingTask = new Task() {

            @Override
            protected Object call() throws Exception {
                
                Vec3F worldColor = joglContext.getWorldColor();
                joglContext.setWorldColor(new Vec3F((float) (sliderColorRed.getValue()/255.0), worldColor.y,worldColor.z));
                try{
                    joglContext.drawNextFrame();
                }catch(Exception e){}
                
                return null;
            }
        };
        
        Service s = new Service() {

            @Override
            protected Task createTask() {
                return colorChangingTask;
            }
        };
        
        
        
        sliderColorRed.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                sliderColorRed.setTooltip(new Tooltip(String.valueOf(sliderColorRed.getValue())));
                  
                s.restart();
                
            }
        });
        
        sliderColorGreen.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                sliderColorGreen.setTooltip(new Tooltip(String.valueOf(sliderColorGreen.getValue())));
                
                Vec3F worldColor = joglContext.getWorldColor();
                joglContext.setWorldColor(new Vec3F(worldColor.x, (float) (sliderColorGreen.getValue()/255.0), worldColor.z));
                
                try{
                    joglContext.drawNextFrame();
                }catch(Exception e){}
            }
        });
        
        sliderColorBlue.valueProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                
                sliderColorBlue.setTooltip(new Tooltip(String.valueOf(sliderColorBlue.getValue())));
                
                Vec3F worldColor = joglContext.getWorldColor();
                joglContext.setWorldColor(new Vec3F(worldColor.x,worldColor.y, (float) (sliderColorBlue.getValue()/255.0)));
                
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
                        float near = Float.valueOf(textfieldCameraNear.getText());
                        float far = Float.valueOf(textfieldCameraFar.getText());
                        joglContext.getCamera().setOrthographic(near, far);
                        joglContext.updateCamera();
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
    
    public void setStage(Stage stage){
        this.stage = stage;
        maxHeight = stage.getHeight();
    }
    
    public void initContent(){
        
        Platform.runLater(new Runnable() {
            
            @Override
            public void run() {
                textFieldVoxelSize.setText(String.valueOf(joglContext.getScene().getVoxelSpace().getCubeSize()));
                textFieldMinValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMin));
                textFieldMaxValue.setText(String.valueOf(joglContext.getScene().getVoxelSpace().attributValueMax));
            }
        });
    }
    
    private void updateValuesFilter(){
        
        final String[] valuesArray = textFieldFilterValues.getText().replace(" ", "").split(",");
        
        Task task = new Task() {

            @Override
            protected Object call() throws Exception {
                
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
                
                return null;
            }
        };
        
        new Thread(task).start();
        
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
        
        Task task = new Task() {

            @Override
            protected Object call() throws Exception {
                
                try{
                    Float voxelSize = Float.valueOf(textFieldVoxelSize.getText());

                    joglContext.getScene().getVoxelSpace().updateCubeSize(null, voxelSize);
                    joglContext.drawNextFrame();

                }catch(NumberFormatException e){
                    logger.error("Cannot parse string value to float", e);
                }catch(Exception e){
                    logger.error("Unknown exception", e);
                }
                
                return null;
            }
        };
        
        new Thread(task).start();
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
            
        }catch(NumberFormatException e){
            logger.error("Cannot parse string value to float", e);
        }catch(Exception e){
            logger.error("Unknown exception", e);
        }
    }

    @FXML
    private void onActionButtonHideToolBox(ActionEvent event) {
        
        if(!isHidden){
            
            originalStageWidth = stage.getWidth();
            originalContentPaneWidth = tabpaneContent.getWidth();
            
            tabpaneContent.setPrefWidth(0);
            
            joglContext.startX = 0;
            
            stage.setWidth(14);
            imageViewArrowHiddingPane.setRotate(180);
            isHidden = true;
            
            
        }else{
            tabpaneContent.setPrefWidth(originalContentPaneWidth);
            stage.setWidth(originalStageWidth);
            
            joglContext.startX = ((int)stage.getWidth());
            
            imageViewArrowHiddingPane.setRotate(0);
            isHidden = false;
        }
        
        joglContext.updateCamera();
        joglContext.drawNextFrame();
        
    }

    @FXML
    private void onActionButtonViewTop(ActionEvent event) {
        
        
        joglContext.getCamera().setLocation(new Vec3F(joglContext.getScene().getVoxelSpace().getCenterX()+0.0001f, 
                                                      joglContext.getScene().getVoxelSpace().getCenterY()+0.0001f,
                                                      joglContext.getScene().getVoxelSpace().getCenterZ()+150));
        
        joglContext.getCamera().updateViewMatrix();
        joglContext.getCamera().setRotation(new Vec3F(0, 0, 1), 0);
        joglContext.getEventListener().mouseXOldLocation = joglContext.getEventListener().mouseXCurrentLocation;
        joglContext.getEventListener().mouseYOldLocation = joglContext.getEventListener().mouseYCurrentLocation;
        joglContext.getCamera().notifyViewMatrixChanged();
        joglContext.drawNextFrame();
    }

    @FXML
    private void onActionButtonViewRight(ActionEvent event) {
    }

    @FXML
    private void onActionButtonViewBottom(ActionEvent event) {
    }

    @FXML
    private void onActionButtonViewLeft(ActionEvent event) {
    }

    @FXML
    private void onActionButtonViewFront(ActionEvent event) {
    }

    @FXML
    private void onActionButtonViewBack(ActionEvent event) {
    }

    @FXML
    private void onActionButtonViewIsometric(ActionEvent event) {
    }
    
}
