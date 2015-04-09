/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import fr.ird.voxelidar.voxelisation.raytracing.voxel.Voxel;
import java.awt.Color;
import java.util.Map;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author calcul
 */
public class VoxelObject extends Voxel{
    
    public final Point3f position;
    public float[] attributs;
    
    public int type;
    
    public float attributValue;
    
    private Color color;
    public Map<String, Point2f> minMax;

    public float[] getAttributs() {
        return attributs;
    }
    
    public VoxelObject(Point3i indice, Point3f position, float attributValue){
        
        
        this.position = position;
        this.color = new Color(0, 0, 0, 1.0f);
        
        this.attributValue = attributValue;
        
        this.type = 6;
    }
    
    public VoxelObject(Point3i indice, Point3f position, float[] attributs, float alpha){
        
        this.position = position;
        
        this.color = new Color(0, 0, 0, 1.0f);
        this.attributs = attributs;
        
        this.type = 6;
    }
    
    public void setAlpha(int alpha){
        
        color = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }
    
    public void setColor(int red, int green, int blue){
        this.color = new Color(red, green, blue, color.getAlpha());
    }
    
    public float getAlpha(){
        return color.getAlpha()/255.0f;
    }
    
    public float getRed(){
        return color.getRed()/255.0f;
    }
    public float getGreen(){
        return color.getGreen()/255.0f;
    }
    
    public float getBlue(){
        return color.getBlue()/255.0f;
    }
}
