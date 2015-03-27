/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.lighting;

import java.util.Set;
import javax.vecmath.Point3f;
import javax.vecmath.Quat4f;

/**
 *
 * @author calcul
 */
public class SceneObject {
    
    private Mesh mesh;
    private Set<Material> material;
    private Set<Integer> materialOffsets;
    private Point3f position;
    private Quat4f rotation;
    
    private boolean instantiable;
    
    public SceneObject(){
        
    }
    
    public void setInstantiable(){
        
    }

    public boolean isInstantiable() {
        return instantiable;
    }

    public void setInstantiable(boolean instantiable) {
        this.instantiable = instantiable;
    }
    
}
