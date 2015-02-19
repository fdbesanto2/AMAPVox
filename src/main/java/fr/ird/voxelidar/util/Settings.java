/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import fr.ird.voxelidar.swing.JFrameSettingUp;
import fr.ird.voxelidar.swing.ListAdapterComboboxModel;
import fr.ird.voxelidar.engine3d.object.mesh.Attribut;
import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
    public Map<String, Attribut> extendedMapAttributs;
    public File voxelSpaceFile;
    
    public Settings(){
        
        drawAxis = false;
        drawNullVoxel = false;
        drawVoxelUnderground = false;
        drawTerrain = false;
        extendedMapAttributs = new TreeMap<>();
    }
    
    public void addAttribut(Attribut attribut){
        extendedMapAttributs.put(attribut.getName(), attribut);
    }
    
    public Settings(JFrameSettingUp jframeSettingUp){
        
        drawAxis = jframeSettingUp.getjCheckBoxDrawAxis().isSelected();
        drawNullVoxel = jframeSettingUp.getjCheckBoxDrawNullVoxel().isSelected();
        drawVoxelUnderground = jframeSettingUp.getjCheckBoxDrawUndergroundVoxel().isSelected();
        drawTerrain = jframeSettingUp.getjCheckBoxDrawTerrain().isSelected();
        
        
        
        
        //voxelSpaceFile = new File(jframeSettingUp.getjListOutputFiles().getSelectedValue().toString());
        
        
        
        
        //voxelSpaceFile = new File(jframeSettingUp.getjListOutputFiles().getSelectedValue().toString());
    }
}
