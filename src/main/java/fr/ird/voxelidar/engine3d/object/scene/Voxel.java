/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.scene;

import java.util.Map;
import javax.vecmath.Point2f;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;

/**
 *
 * @author Julien
 */
public class Voxel extends RawVoxel{
    
    public final Point3f position;
    
    public int type;
    
    public float attributValue;
    public Vector3f color;
    public float alpha;
    public Map<String, Point2f> minMax;

    public Float[] getAttributs() {
        return attributs;
    }
    
    public Voxel(Point3i indice, Point3f position, float attributValue){
        
        this.indice = indice;
        
        this.position = position;
        
        this.alpha = 1.0f;
        
        this.attributValue = attributValue;
        
        this.type = 6;
    }
    
    public Voxel(Point3i indice, Point3f position, Float[] attributs, float alpha){
        
        this.indice = indice;
        
        this.position = position;
        
        this.alpha = alpha;
        this.attributs = attributs;
        
        this.type = 6;
    }
    
}
