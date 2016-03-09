/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.lidar.amapvox.commons.RawVoxel;
import java.awt.Color;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class VoxelObject extends RawVoxel{
    
    /*public int $i;
    public int $j;
    public int $k;
    
    public float[] attributs;*/
    
    public int type;
    
    public float attributValue;
    public boolean isHidden;
    
    private Color color;
    

    public float[] getAttributs() {
        return attributs;
    }
    
    public VoxelObject(Point3i indice, float[] attributs, float alpha){
        
        this.$i = indice.x;
        this.$j = indice.y;
        this.$k = indice.z;
        
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
