/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.loading.shader;

import com.jogamp.opengl.GL3;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author calcul
 */
public abstract class Uniform {
    
    
    protected final String name;
    protected final List<Shader> owners;
    protected final List<Integer> locations;
    
    public Uniform(String name){
        
        locations = new ArrayList<>();
        owners = new ArrayList<>();
        
        this.name = name;
    }
    
    public Uniform(String name, Shader shader, int location){
        
        locations = new ArrayList<>();
        owners = new ArrayList<>();
        
        this.name = name;
        
        owners.add(shader);
        locations.add(location);
        
    }
    
    public void addOwner(Shader shader, int location){
        owners.add(shader);
        locations.add(location);
    }
    
    public void notifyOwners(){
        for(int i=0;i<owners.size();i++){
            owners.get(i).notifyDirty(this, locations.get(i));
        }
    }

    public String getName() {
        return name;
    }
    
    public abstract void update(GL3 gl, int location); 
}
