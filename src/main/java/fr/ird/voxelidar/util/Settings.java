/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import fr.ird.voxelidar.frame.JFrameSettingUp;
import fr.ird.voxelidar.frame.ListAdapterComboboxModel;
import fr.ird.voxelidar.graphics3d.mesh.Attribut;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Julien
 */
public class Settings {
    
    public boolean drawTerrain;
    public boolean drawVoxelUnderground;
    public boolean drawNullVoxel;
    public boolean drawAxis;
    public String attributeToVisualize;
    public Attribut attribut;
    public Map<String, Attribut> mapAttributs;
    public File voxelSpaceFile;
    
    public Settings(JFrameSettingUp jframeSettingUp){
        
        drawAxis = jframeSettingUp.getjCheckBoxDrawAxis().isSelected();
        drawNullVoxel = jframeSettingUp.getjCheckBoxDrawNullVoxel().isSelected();
        drawVoxelUnderground = jframeSettingUp.getjCheckBoxDrawUndergroundVoxel().isSelected();
        drawTerrain = jframeSettingUp.getjCheckBoxDrawTerrain().isSelected();
         
        try{
            attributeToVisualize = jframeSettingUp.getjComboBoxAttributeToVisualize().getSelectedItem().toString();
            mapAttributs = new HashMap<>();
            ListAdapterComboboxModel mainAttributeModelAdapter = jframeSettingUp.getMainAttributeModelAdapter();
            
            Set<String> variablesNames = new HashSet<>();
            for(int i=0;i<mainAttributeModelAdapter.getSize();i++){
                
                variablesNames.add(mainAttributeModelAdapter.getElementAt(i).toString());
            }
            
            for(int i=0;i<mainAttributeModelAdapter.getSize();i++){
                
                Attribut a = new Attribut(mainAttributeModelAdapter.getElementAt(i).toString(), mainAttributeModelAdapter.getValue(i), variablesNames);
                mapAttributs.put(mainAttributeModelAdapter.getElementAt(i).toString(), a);
            }            
            
            attribut = mapAttributs.get(attributeToVisualize);
            
        }catch(Exception e){
            attributeToVisualize = null;
            mapAttributs = null;
                    
        }
        
        
        
        
        voxelSpaceFile = new File(jframeSettingUp.getjListOutputFiles().getSelectedValue().toString());
    }
}
