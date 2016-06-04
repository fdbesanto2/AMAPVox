/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.export;

import fr.amap.commons.util.ColorGradient;
import fr.amap.commons.util.Statistic;
import fr.amap.lidar.amapvox.commons.RawVoxel;
import fr.amap.lidar.amapvox.commons.VoxelSpaceInfos;
import fr.amap.lidar.amapvox.gui.DialogHelper;
import fr.amap.lidar.amapvox.gui.FileChooserContext;
import fr.amap.lidar.amapvox.gui.Util;
import fr.amap.lidar.amapvox.voxreader.VoxelFileRawReader;
import fr.amap.lidar.amapvox.voxreader.VoxelFileReader;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javax.vecmath.Point3d;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 * FXML Controller class
 *
 * @author Julien Heurtebize
 */
public class ObjExporterDialogController implements Initializable {

    private FileChooserContext fcVoxelFile = new FileChooserContext();
    private FileChooserContext fcOutputFile = new FileChooserContext();
    
    private Stage stage;
    
    private List<Point3d> cubeVertices;
    private List<Point3i> faces;
    private Map<String, Point3f> materials;
    private List<String> materialsKeys;
    
    @FXML
    private TextField textfieldInputVoxelFile;
    @FXML
    private CheckBox checkboxSizeFunctionOfPAD;
    @FXML
    private CheckBox checkboxMaterial;
    @FXML
    private ComboBox<String> comboboxAttribute;
    @FXML
    private ComboBox<String> comboboxGradient;
    @FXML
    private VBox vboxMaterialParameters;
    @FXML
    private HBox hboxSizeFunctionOfPADParameters;
    @FXML
    private TextField textfieldPADMax;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        
        fcVoxelFile = new FileChooserContext();
        comboboxGradient.getItems().addAll(Util.AVAILABLE_GRADIENT_COLOR_NAMES);
        comboboxGradient.getSelectionModel().selectFirst();
        comboboxAttribute.getSelectionModel().selectFirst();
        
        vboxMaterialParameters.disableProperty().bind(checkboxMaterial.selectedProperty().not());
    }    

    @FXML
    private void onActionButtonOpenVoxelFile(ActionEvent event) throws Exception {
        
        File selectedFile = fcVoxelFile.showOpenDialog(stage);
        
        setVoxelFile(selectedFile);
    }

    @FXML
    private void onActionButtonExport(ActionEvent event) {
        
        
        File outputFile = fcOutputFile.showSaveDialog(stage);
        if(outputFile == null){
            return;
        }
        
        cubeVertices = new ArrayList<>();
        faces = new ArrayList<>();
        
        VoxelFileRawReader reader;
        
        try {
            reader = new VoxelFileRawReader(new File(textfieldInputVoxelFile.getText()), true);
            VoxelSpaceInfos infos = reader.getVoxelSpaceInfos();
            
            int padAttributeIndex = comboboxAttribute.getItems().indexOf("PadBVTotal")-3;
            int selectedAttributeIndex = comboboxAttribute.getSelectionModel().getSelectedIndex()-3;
            float padMax = Float.valueOf(textfieldPADMax.getText());
            
            ColorGradient gradient = new ColorGradient(0, 0);
            
            if(checkboxMaterial.isSelected()){
                //find min and max
                Iterator<RawVoxel> iterator = reader.iterator();
                Statistic attributeStat = new Statistic();

                while(iterator.hasNext()){

                    RawVoxel voxel = iterator.next();
                    
                    switch (selectedAttributeIndex) {
                        case -3:
                            attributeStat.addValue(voxel.$i);
                            break;
                        case -2:
                            attributeStat.addValue(voxel.$j);
                            break;
                        case -1:
                            attributeStat.addValue(voxel.$k);
                            break;
                        default:
                            attributeStat.addValue(voxel.attributs[selectedAttributeIndex]);
                            break;
                    }
                    
                }

                gradient = new ColorGradient((float)attributeStat.getMinValue(), (float)attributeStat.getMaxValue());
                gradient.setGradientColor(Util.AVAILABLE_GRADIENT_COLORS.get(comboboxGradient.getSelectionModel().getSelectedIndex()));
                
                materials = new HashMap<>();
                materialsKeys = new ArrayList<>();
            }
            
            Iterator<RawVoxel> iterator = reader.iterator();
            
            while(iterator.hasNext()){
                
                RawVoxel voxel = iterator.next();
                
                if(!Float.isNaN(voxel.attributs[padAttributeIndex]) && voxel.attributs[padAttributeIndex] != 0){
                    
                    Point3d position = getPosition(new Point3i(voxel.$i, voxel.$j, voxel.$k), infos);
                    
                    
                    if(checkboxMaterial.isSelected()){
                        
                        Color c;
                        
                        switch (selectedAttributeIndex) {
                            case -3:
                                c = gradient.getColor(voxel.$i);
                                break;
                            case -2:
                                c = gradient.getColor(voxel.$j);
                                break;
                            case -1:
                                c = gradient.getColor(voxel.$k);
                                break;
                            default:
                                c = gradient.getColor(voxel.attributs[selectedAttributeIndex]);
                                break;
                        }                        

                        float red = c.getRed() / 255.0f;
                        float green = c.getGreen() / 255.0f;
                        float blue = c.getBlue() / 255.0f;

                        String key = String.valueOf(red)+"_"+String.valueOf(green)+"_"+String.valueOf(blue);
                        materials.put(key, new Point3f(red, green, blue));
                        materialsKeys.add(key);
                    }

                    float voxelSize;
                    if(checkboxSizeFunctionOfPAD.isSelected()){
                        
                        float padValue = voxel.attributs[padAttributeIndex];
                        voxelSize = (float) (infos.getResolution() * Math.pow(padValue/padMax, 1/3.0));
                        
                    }else{
                        voxelSize = infos.getResolution();
                    }
                    
                    createCube(voxelSize, position);
                }

            }
        
        } catch (Exception ex) {
            DialogHelper.showErrorDialog(stage, ex);
            return;
        }
        
        int extensionBeginIndex = outputFile.getAbsolutePath().lastIndexOf(".");
        File materialFile = new File(outputFile.getAbsolutePath().substring(0, extensionBeginIndex)+".mtl");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            
            if(checkboxMaterial.isSelected()){
                writer.write("mtllib "+materialFile.getName()+"\n");
            }
            
            for (Point3d point : cubeVertices) {
                writer.write("v " + point.x + " " + point.y + " " + point.z + "\n");
            }

            int count = 0;
            int matIndex = 0;

            for (Point3i face : faces) {

                if(checkboxMaterial.isSelected()){
                    if (count == 0) {

                        writer.write("usemtl Material." + getValueIndex(materialsKeys.get(matIndex)) + "\n");
                        matIndex++;
                        count = 12;
                    }
                    
                    count--;
                }
                
                writer.write("f " + face.x + " " + face.y + " " + face.z + "\n");
            }
        } catch (IOException ex) {
            DialogHelper.showErrorDialog(stage, new IOException("Cannot write obj file", ex));
            return;
        }
        
        if(checkboxMaterial.isSelected()){
                
            try{
                
                
                BufferedWriter materialWriter = new BufferedWriter(new FileWriter(materialFile));
                int count = 0;
                Iterator<Map.Entry<String, Point3f>> iterator = materials.entrySet().iterator();
                while(iterator.hasNext()){

                    Map.Entry<String, Point3f> next = iterator.next();
                    Point3f material = next.getValue();

                    materialWriter.write("newmtl Material." + count + "\n"
                            + "Ns 96.078431\n"
                            + "Ka 0 0 0\n"
                            + "Kd " + material.x + " " + material.y + " " + material.z + "\n"
                            + "Ks 0.500000 0.500000 0.500000\n"
                            + "Ni 1.000000\n"
                            + "d 1.000000\n"
                            + "illum 2\n");

                    count++;
                }
                
                materialWriter.close();
                
            }catch(IOException ex){
                DialogHelper.showErrorDialog(stage, new IOException("Cannot write material file", ex));
            }

        }
    }
        
    private void createCube(float size, Point3d translation){
        
        cubeVertices.add(new Point3d(size/2.0f+translation.x, size/2.0f+translation.y, -size/2.0f+translation.z));
        cubeVertices.add(new Point3d(size/2.0f+translation.x, -size/2.0f+translation.y, -size/2.0f+translation.z));
        cubeVertices.add(new Point3d(-size/2.0f+translation.x, -size/2.0f+translation.y, -size/2.0f+translation.z));
        cubeVertices.add(new Point3d(-size/2.0f+translation.x, size/2.0f+translation.y, -size/2.0f+translation.z));
        cubeVertices.add(new Point3d(size/2.0f+translation.x, size/2.0f+translation.y, size/2.0f+translation.z));
        cubeVertices.add(new Point3d(size/2.0f+translation.x, -size/2.0f+translation.y, size/2.0f+translation.z));
        cubeVertices.add(new Point3d(-size/2.0f+translation.x, -size/2.0f+translation.y, size/2.0f+translation.z));
        cubeVertices.add(new Point3d(-size/2.0f+translation.x, size/2.0f+translation.y, size/2.0f+translation.z));
        
        int currentOffset = cubeVertices.size() - 7;
        
        faces.add(new Point3i(currentOffset+0, currentOffset+1, currentOffset+2));
        faces.add(new Point3i(currentOffset+4, currentOffset+7, currentOffset+6));
        faces.add(new Point3i(currentOffset+0, currentOffset+4, currentOffset+5));
        faces.add(new Point3i(currentOffset+1, currentOffset+5, currentOffset+6));
        faces.add(new Point3i(currentOffset+2, currentOffset+6, currentOffset+7));
        faces.add(new Point3i(currentOffset+4, currentOffset+0, currentOffset+3));
        faces.add(new Point3i(currentOffset+3, currentOffset+0, currentOffset+2));
        faces.add(new Point3i(currentOffset+5, currentOffset+4, currentOffset+6));
        faces.add(new Point3i(currentOffset+1, currentOffset+0, currentOffset+5));
        faces.add(new Point3i(currentOffset+2, currentOffset+1, currentOffset+6));
        faces.add(new Point3i(currentOffset+3, currentOffset+2, currentOffset+7));
        faces.add(new Point3i(currentOffset+7, currentOffset+4, currentOffset+3));
        
    }
    
    private Point3d getPosition(Point3i indices, VoxelSpaceInfos infos) {
        
        double posX = infos.getMinCorner().x + (infos.getResolution() / 2.0d) + (indices.x * infos.getResolution());
        double posY = infos.getMinCorner().y + (infos.getResolution() / 2.0d) + (indices.y * infos.getResolution());
        double posZ = infos.getMinCorner().z + (infos.getResolution() / 2.0d) + (indices.z * infos.getResolution());

        return new Point3d(posX, posY, posZ);
    }
    
    private int getValueIndex(String key){
        
        Iterator<Map.Entry<String, Point3f>> iterator1 = materials.entrySet().iterator();
        
        int count = 0;
        
        while(iterator1.hasNext()){
            Map.Entry<String, Point3f> next = iterator1.next();
            if(next.getKey().equals(key)){
                return count;
            }
            
            count++;
        }
        
        return -1;
    }
    
    public void setStage(Stage stage){
        this.stage =stage;
    }

    public Stage getStage() {
        return stage;
    }
    
    public void setVoxelFile(File voxelFile) throws Exception{
        
        textfieldInputVoxelFile.setText(voxelFile.getAbsolutePath());
        try {
            VoxelFileReader reader = new VoxelFileReader(voxelFile);
            String[] columnNames = reader.getVoxelSpaceInfos().getColumnNames();
            comboboxAttribute.getItems().setAll(columnNames);
            comboboxAttribute.getSelectionModel().selectFirst();
            textfieldPADMax.setText(String.valueOf(reader.getVoxelSpaceInfos().getMaxPAD()));
        } catch (Exception ex) {
            throw ex;
        }
    }
}
