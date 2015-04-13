/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import fr.ird.voxelidar.engine3d.object.mesh.Attribut;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author Julien
 */
public class Settings {
    
    public boolean drawDtm;
    public boolean drawUndergroundVoxel;
    public boolean drawNullVoxel;
    public boolean drawAxis;
    public String attributeToVisualize;
    public Attribut attribut;
    public Map<String, Attribut> extendedMapAttributs;
    
    public Settings(){
        
        drawAxis = false;
        drawNullVoxel = false;
        drawUndergroundVoxel = false;
        drawDtm = false;
        extendedMapAttributs = new TreeMap<>();
    }
    
    public void addAttribut(Attribut attribut){
        extendedMapAttributs.put(attribut.getName(), attribut);
    }

    public Settings(boolean drawDtm, boolean drawUndergroundVoxel, boolean drawNullVoxel, boolean drawAxis, String attributeToVisualize) {
        this.drawDtm = drawDtm;
        this.drawUndergroundVoxel = drawUndergroundVoxel;
        this.drawNullVoxel = drawNullVoxel;
        this.drawAxis = drawAxis;
        this.attributeToVisualize = attributeToVisualize;
    }
    
}
