package fr.amap.lidar.amapvox.gui.viewer3d;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.util.ColorGradient;
import fr.amap.commons.util.filter.CombinedFloatFilter;
import fr.amap.commons.util.filter.FloatFilter;
import fr.amap.viewer3d.object.camera.TrackballCamera;
import fr.amap.viewer3d.renderer.JoglListener;
import fr.amap.commons.util.filter.CombinedFilterItem;
import fr.amap.lidar.amapvox.gui.Util;
import fr.amap.viewer3d.loading.shader.InstanceLightedShader;
import fr.amap.viewer3d.loading.shader.InstanceShader;
import fr.amap.viewer3d.object.scene.Scene;
import java.awt.Color;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import org.apache.log4j.Logger;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class ToolBoxFrameController implements Initializable {
    
    private final static Logger LOGGER = Logger.getLogger(ToolBoxFrameController.class);
    
    private JoglListener joglContext;
    
    
    private Stage stage;
    
    private double originalStageWidth;
    private double originalContentPaneWidth;
    private boolean isHidden;
    
    public double maxHeight;
    
    private VoxelSpaceSceneObject voxelSpace;
    
    @FXML
    private ComboBox<String> comboBoxAttributeToShow;
    @FXML
    private ComboBox<String> comboboxGradient;
    @FXML
    private TextField textFieldVoxelSize;
    @FXML
    private Button buttonApplyVoxelSize;
    @FXML
    private TextField textFieldMinValue;
    @FXML
    private TextField textFieldMaxValue;
    @FXML
    private Button buttonResetMinMax;
    /*@FXML
    private TextField textFieldFilterValues;*/
    private CheckBox comboboxStretched;
    @FXML
    private CheckBox checkboxStretched;
    /*@FXML
    private RadioButton radiobuttonDontDisplayValues;*/
    /*@FXML
    private RadioButton radiobuttonDisplayValues;*/
    //private Tooltip tooltipTextfieldFilter;
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
    private Button buttonViewIsometric;
    /*@FXML
    private Button buttonOKValidationFilter;*/
    @FXML
    private ColorPicker colorpickerLightingAmbientColor;
    @FXML
    private ColorPicker colorpickerLightingDiffuseColor;
    @FXML
    private ColorPicker colorpickerLightingSpecularColor;
    @FXML
    private ColorPicker colorPickerBackgroundColor;
    @FXML
    private CheckBox checkboxEnableLighting;
    @FXML
    private Button buttonIncreaseCutting;
    @FXML
    private Button buttonDecreaseCutting;
    @FXML
    private TextField textfieldIncrementValue;
    @FXML
    private Button buttonViewBack;
    @FXML
    private ComboBox<String> comboBoxScalarField;
    @FXML
    private Tooltip tooltipTextfieldFilter1;
    @FXML
    private ListView<CombinedFilterItem> listviewFilters;
    @FXML
    private RadioButton radiobuttonDisplay;
    @FXML
    private RadioButton radiobuttonDontDisplay;
    @FXML
    private TextField textfieldFilteringRange;
    
    
    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        
        comboboxGradient.getItems().addAll(Util.AVAILABLE_GRADIENT_COLOR_NAMES);
        comboboxGradient.getSelectionModel().select("HEAT");
        
        isHidden = false;
        
        colorPickerBackgroundColor.setValue(new javafx.scene.paint.Color(0.8, 0.8, 0.8, 1));
        colorpickerLightingAmbientColor.setValue(new javafx.scene.paint.Color(1.0, 1.0, 1.0, 1));
        colorpickerLightingDiffuseColor.setValue(new javafx.scene.paint.Color(1.0, 1.0, 1.0, 1));
        colorpickerLightingSpecularColor.setValue(new javafx.scene.paint.Color(1.0, 1.0, 1.0, 1));
               
                
        comboboxGradient.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                
                String gradient = newValue;
                Color[] gradientColor = ColorGradient.GRADIENT_RAINBOW;

                for (int i = 0;i<Util.AVAILABLE_GRADIENT_COLORS.size();i++) {

                    if(Util.AVAILABLE_GRADIENT_COLOR_NAMES.get(i).equals(gradient)){
                        gradientColor = Util.AVAILABLE_GRADIENT_COLORS.get(i);
                        i = Util.AVAILABLE_GRADIENT_COLOR_NAMES.size() - 1;
                    }
                }

                //recalculate voxel color with the new gradient
                voxelSpace.updateColorValue(gradientColor);

                //update instance color buffer to gpu
                voxelSpace.updateInstanceColorBuffer();

                joglContext.refresh();
        
            }
        });
        
        colorPickerBackgroundColor.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {

            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) {
                
                joglContext.setWorldColor(new Vec3F((float)newValue.getRed(), (float)newValue.getGreen(), (float)newValue.getBlue()));
                joglContext.refresh();
            }
        });
        
        colorpickerLightingAmbientColor.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {

            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) {
                joglContext.getScene().setLightAmbientValue(new Vec3F((float)newValue.getRed(), (float)newValue.getGreen(), (float)newValue.getBlue()));
                joglContext.refresh();
            }
        });
        
        colorpickerLightingDiffuseColor.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {

            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) {
                joglContext.getScene().setLightDiffuseValue(new Vec3F((float)newValue.getRed(), (float)newValue.getGreen(), (float)newValue.getBlue()));
                joglContext.refresh();
            }
        });
        
        colorpickerLightingSpecularColor.valueProperty().addListener(new ChangeListener<javafx.scene.paint.Color>() {

            @Override
            public void changed(ObservableValue<? extends javafx.scene.paint.Color> observable, javafx.scene.paint.Color oldValue, javafx.scene.paint.Color newValue) {
                joglContext.getScene().setLightSpecularValue(new Vec3F((float)newValue.getRed(), (float)newValue.getGreen(), (float)newValue.getBlue()));
                joglContext.refresh();
            }
        });
        
        comboBoxAttributeToShow.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    voxelSpace.resetAttributValueRange();
                    voxelSpace.changeCurrentAttribut(newValue);
                    voxelSpace.updateVao();
                    voxelSpace.updateInstanceColorBuffer();
                    joglContext.refresh();
                    textFieldMinValue.setText(String.valueOf(voxelSpace.getRealAttributValueMin()));
                    textFieldMaxValue.setText(String.valueOf(voxelSpace.getRealAttributValueMax()));
                    
                    
                }catch(Exception e){}
                
            }
        });
        
        final InstanceLightedShader ils = new InstanceLightedShader();
        final InstanceShader is = new InstanceShader();
        
        checkboxEnableLighting.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                if(newValue){
                    voxelSpace.setShader(ils);
                }else{
                    voxelSpace.setShader(is);
                }
                
                joglContext.refresh();
            }
        });
        
        /*textFieldFilterValues.setOnKeyReleased(new EventHandler<KeyEvent>() {

            @Override
            public void handle(KeyEvent event) {
                
                if(event.getCode() == KeyCode.ENTER){
                    updateValuesFilter();
                }
            }
        });*/
        
        checkboxStretched.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue){
                    voxelSpace.setStretched(true);
                    voxelSpace.updateValue();
                    voxelSpace.updateInstanceColorBuffer();
                    
                    
                    joglContext.refresh();
                }else{
                    voxelSpace.setStretched(false);
                    voxelSpace.updateValue();
                    voxelSpace.updateInstanceColorBuffer();
                    
                    
                    joglContext.refresh();
                }
                
            }
        });
        
        /*ToggleGroup groupDisplayValues = new ToggleGroup();
        radiobuttonDisplayValues.setToggleGroup(groupDisplayValues);
        radiobuttonDontDisplayValues.setToggleGroup(groupDisplayValues);
        
        radiobuttonDontDisplayValues.selectedProperty().addListener(new ChangeListener<Boolean>() {

            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                
                updateValuesFilter();
            }
        });*/
        
        
        
        
        //tooltipTextfieldFilter.setText("Syntax: value1, value2\nvalue can be a floating point number\nor a value range [1.0->2.0[");
        
        textfieldCameraFOV.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float fov = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getScene().getCamera();
                    joglContext.getScene().getCamera().setPerspective(fov, camera.getAspect(), camera.getNearPersp(), camera.getFarPersp());
                    joglContext.refresh();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraNear.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float near = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getScene().getCamera();
                    if(radiobuttonOrthographicCamera.isSelected()){
                        joglContext.getScene().getCamera().setOrthographic(camera.getLeft(), camera.getRight(), camera.getTop(), camera.getBottom(), near, camera.getFarOrtho());
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }else{
                        joglContext.getScene().getCamera().setPerspective(camera.getFovy(), camera.getAspect(), near, camera.getFarPersp());
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }
                    
                    joglContext.refresh();
                    
                }catch(Exception e){}
            }
        });
        
        textfieldCameraFar.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try{
                    float far = Float.valueOf(newValue);
                    TrackballCamera camera = joglContext.getScene().getCamera();
                    if(radiobuttonOrthographicCamera.isSelected()){
                        camera.setOrthographic(camera.getLeft(), camera.getRight(), camera.getTop(), camera.getBottom(), camera.getNearOrtho(), far);
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }else{
                        camera.setPerspective(camera.getFovy(), camera.getAspect(), camera.getNearPersp(), far);
                        joglContext.updateCamera();
                        joglContext.refresh();
                    }
                    
                    joglContext.refresh();
                    
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
                        joglContext.getScene().getCamera().setViewToOrthographic(near, far, far, far, near, far);
                        joglContext.updateCamera();
                        joglContext.refresh();
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

                        joglContext.getScene().getCamera().setViewToPerspective(fov, near, far);
                        joglContext.updateCamera();
                        joglContext.refresh();
                        
                    }catch(Exception e){}
                    
                }
            }
        });
        
        comboBoxScalarField.setItems(comboBoxAttributeToShow.getItems());
        comboBoxScalarField.getItems().addListener(new ListChangeListener<String>() {
            @Override
            public void onChanged(ListChangeListener.Change<? extends String> c) {
                if(c.getList().size() > 0){
                    comboBoxScalarField.getSelectionModel().selectFirst();
                }
            }
        });
        
        ToggleGroup group = new ToggleGroup();
        radiobuttonDisplay.setToggleGroup(group);
        radiobuttonDontDisplay.setToggleGroup(group);
        
        CombinedFloatFilter combFilter1 = new CombinedFloatFilter(new FloatFilter("x", 0.0f, FloatFilter.EQUAL),  null, CombinedFloatFilter.AND);
        CombinedFloatFilter combFilter2 = new CombinedFloatFilter(new FloatFilter("x", Float.NaN, FloatFilter.EQUAL),  null, CombinedFloatFilter.AND);
        
        listviewFilters.getItems().add(new CombinedFilterItem("PadBVTotal", false, 
                        combFilter1.getFilter1(), combFilter1.getFilter2(), combFilter1.getType()));
        
        listviewFilters.getItems().add(new CombinedFilterItem("PadBVTotal", false, 
                        combFilter2.getFilter1(), combFilter2.getFilter2(), combFilter2.getType()));
        
    }
    
    public void setStage(Stage stage){
        
        this.stage = stage;
        maxHeight = stage.getHeight();
        
    }
    
    public void initContent(final VoxelSpaceSceneObject voxelSpace){
        
        this.voxelSpace = voxelSpace;
        
        Platform.runLater(new Runnable() {
            
            @Override
            public void run() {
                textFieldVoxelSize.setText(String.valueOf(voxelSpace.getCubeSize()));
                textFieldMinValue.setText(String.valueOf(voxelSpace.getRealAttributValueMin()));
                textFieldMaxValue.setText(String.valueOf(voxelSpace.getRealAttributValueMax()));
            }
        });
    }
    
    /*private void updateValuesFilter(){
        
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

                voxelSpace.setFilterValues(filterValues, radiobuttonDisplayValues.isSelected());
                voxelSpace.updateColorValue(voxelSpace.getGradient());
                voxelSpace.updateVao();
                joglContext.refresh();
                
                return null;
            }
        };
        
        new Thread(task).start();
        
    }*/
    
    public void setJoglListener(JoglListener joglContext){
        this.joglContext = joglContext;
    }
    
    public void setAttributes(String attributeToVisualize, String[] attributes){
                
        if(attributeToVisualize == null && attributes.length> 0){
            attributeToVisualize = attributes[0];
        }
        
        comboBoxAttributeToShow.getItems().addAll(attributes);
        comboBoxAttributeToShow.getSelectionModel().select(attributeToVisualize);
    }
    /*
    public void setAttributes(List<String> attributes){
        comboBoxAttributeToShow.getItems().addAll(attributes);
        comboBoxAttributeToShow.getSelectionModel().select(joglContext.getSettings().attributeToVisualize);
    }*/
    

    @FXML
    private void onActionButtonApplyVoxelSize(ActionEvent event) {
        
        Task task = new Task() {

            @Override
            protected Object call() throws Exception {
                
                try{
                    Float voxelSize = Float.valueOf(textFieldVoxelSize.getText());

                    voxelSpace.updateCubeSize(null, voxelSize);
                    joglContext.refresh();

                }catch(NumberFormatException e){
                    LOGGER.error("Cannot parse string value to float", e);
                }catch(Exception e){
                    LOGGER.error("Unknown exception", e);
                }
                
                return null;
            }
        };
        
        new Thread(task).start();
    }

    @FXML
    private void onActionButtonResetMinMax(ActionEvent event) {
        
        textFieldMinValue.setText(String.valueOf(voxelSpace.getRealAttributValueMin()));
        textFieldMaxValue.setText(String.valueOf(voxelSpace.getRealAttributValueMax()));
        
        voxelSpace.resetAttributValueRange();
        voxelSpace.updateValue();
        voxelSpace.updateColorValue(voxelSpace.getGradient());
        voxelSpace.updateInstanceColorBuffer();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonApplyMinMax(ActionEvent event) {
        
        try{
            float min = Float.valueOf(textFieldMinValue.getText());
            float max = Float.valueOf(textFieldMaxValue.getText());
            
            voxelSpace.setAttributValueRange(min, max);
            voxelSpace.updateValue();
            voxelSpace.updateColorValue(voxelSpace.getGradient());
            voxelSpace.updateInstanceColorBuffer();
            joglContext.refresh();
            
        }catch(NumberFormatException e){
            LOGGER.error("Cannot parse string value to float", e);
        }catch(Exception e){
            LOGGER.error("Unknown exception", e);
        }
    }

    @FXML
    private void onActionButtonHideToolBox(ActionEvent event) {
        
        if(!isHidden){
            
            originalStageWidth = stage.getWidth();
            originalContentPaneWidth = tabpaneContent.getWidth();
            
            tabpaneContent.setPrefWidth(0);
            
            joglContext.setStartX(0);
            
            stage.setWidth(14);
            imageViewArrowHiddingPane.setRotate(180);
            isHidden = true;
            
            
        }else{
            tabpaneContent.setPrefWidth(originalContentPaneWidth);
            stage.setWidth(originalStageWidth);
            
            joglContext.setStartX(((int)stage.getWidth()));
            
            
            imageViewArrowHiddingPane.setRotate(0);
            isHidden = false;
        }
        
        joglContext.updateCamera();
        joglContext.refresh();
        
    }

    @FXML
    private void onActionButtonViewTop(ActionEvent event) {
        
        joglContext.getScene().getCamera().setViewToTop();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonViewRight(ActionEvent event) {
        
        joglContext.getScene().getCamera().setViewToRight();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonViewBottom(ActionEvent event) {
                
        joglContext.getScene().getCamera().setViewToBottom();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonViewLeft(ActionEvent event) {
        
        joglContext.getScene().getCamera().setViewToLeft();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonViewFront(ActionEvent event) {
        
        joglContext.getScene().getCamera().setViewToFront();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonViewBack(ActionEvent event) {
        
        joglContext.getScene().getCamera().setViewToBack();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonViewIsometric(ActionEvent event) {
    }
    /*
    private void resetMouseLocation(){
        
        joglContext.getEventListener().mouseXOldLocation = joglContext.getEventListener().mouseXCurrentLocation;
        joglContext.getEventListener().mouseYOldLocation = joglContext.getEventListener().mouseYCurrentLocation;
    }*/

    /*@FXML
    private void onActionButtonOKValidationFilter(ActionEvent event) {
        updateValuesFilter();
    }*/

    @FXML
    private void onActionButtonIncreaseCutting(ActionEvent event) {
        
        cutting(true);
    }
    @FXML
    private void onActionButtonDecreaseCutting(ActionEvent event) {
        
        cutting(false);
    }

    @FXML
    private void onActionButtonResetCuttingPlane(ActionEvent event) {
        
        voxelSpace.resetCuttingPlane();
        joglContext.refresh();
    }
    
    private void cutting(boolean increase){
        
        voxelSpace.setCuttingIncrementFactor(Float.valueOf(textfieldIncrementValue.getText()));
        voxelSpace.setCuttingPlane(increase, 
                joglContext.getScene().getCamera().getForwardVector(),
                joglContext.getScene().getCamera().getRightVector(), 
                joglContext.getScene().getCamera().getUpVector(),
                joglContext.getScene().getCamera().getLocation());
        
        
        joglContext.refresh();
    }
    

    @FXML
    private void onActionButtonAddFilterToList(ActionEvent event) {
        
        String selectedItem = comboBoxScalarField.getSelectionModel().getSelectedItem();
        
        if(selectedItem != null){
            
            final String[] valuesArray = textfieldFilteringRange.getText().replace(" ", "").split(",");
            
            Set<CombinedFloatFilter> filterValues = new HashSet<>();
            
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
                                    firstInequalityID = FloatFilter.GREATER_THAN;
                                    break;
                                case '[':
                                    firstInequalityID = FloatFilter.GREATER_THAN_OR_EQUAL;
                                    break;
                                default:
                                    firstInequalityID = FloatFilter.GREATER_THAN_OR_EQUAL;
                            }

                            int secondInequalityID;
                            switch(secondInequality){
                                case ']':
                                    secondInequalityID = FloatFilter.LESS_THAN_OR_EQUAL;
                                    break;
                                case '[':
                                    secondInequalityID = FloatFilter.LESS_THAN;
                                    break;
                                default:
                                    secondInequalityID = FloatFilter.LESS_THAN_OR_EQUAL;
                            }


                            filterValues.add(new CombinedFloatFilter(
                                    new FloatFilter("x", firstValue, firstInequalityID), 
                                    new FloatFilter("x", secondValue, secondInequalityID), CombinedFloatFilter.AND));
                        }

                    }else{
                        filterValues.add(new CombinedFloatFilter(
                                    new FloatFilter("x", Float.valueOf(valuesArray[i]), FloatFilter.EQUAL), 
                                    null, CombinedFloatFilter.AND));
                    }

                }catch(Exception e){}
            }
            
            for(CombinedFloatFilter combinedFilter : filterValues){
                
                ObservableList<CombinedFilterItem> items = listviewFilters.getItems();
                
                CombinedFilterItem combinedFilterItem = new CombinedFilterItem(comboBoxScalarField.getSelectionModel().getSelectedItem(), radiobuttonDisplay.isSelected(), 
                        combinedFilter.getFilter1(), combinedFilter.getFilter2(), combinedFilter.getType());
                
                String newFilter = combinedFilterItem.toString();
                
                boolean addFilter = true;
                
                for(CombinedFilterItem item : items){
                    if(item.toString().equals(newFilter)){
                        addFilter = false;
                    }
                }
                
                if(addFilter){
                    listviewFilters.getItems().add(combinedFilterItem);
                }
            }
            
            updateScene();
        }
    }
    
    private void updateScene(){
        voxelSpace.setFilters(listviewFilters.getItems());
        voxelSpace.updateColorValue(voxelSpace.getGradient());
        voxelSpace.updateVao();
        joglContext.refresh();
    }

    @FXML
    private void onActionButtonRemoveFilterFromList(ActionEvent event) {
        
        ObservableList<CombinedFilterItem> selectedItems = listviewFilters.getSelectionModel().getSelectedItems();
        listviewFilters.getItems().removeAll(selectedItems);
        updateScene();
    }
    
}
