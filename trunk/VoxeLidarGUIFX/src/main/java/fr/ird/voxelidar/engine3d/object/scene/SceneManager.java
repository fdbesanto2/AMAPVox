/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.ird.voxelidar.engine3d.object.scene;

import com.jogamp.opengl.GL3;
import fr.ird.voxelidar.engine3d.loading.shader.Shader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class SceneManager {
    
    private final Scene scene;
    public final Map<Integer, Shader> shadersList;
    
    private List<Integer> itemsNotInitialized;
    
    public SceneManager(){
        this.scene = new Scene();
        itemsNotInitialized = new ArrayList<>();
        shadersList = new HashMap<>();
    }
    
    public SceneManager(Scene scene){
        this.scene = scene;
        itemsNotInitialized = new ArrayList<>();
        shadersList = new HashMap<>();
    }
    
    public void addObject(SceneObject sceneObject){
        scene.objectsList.add(sceneObject);
        sceneObject.setId(scene.objectsList.size());
    }
    
    public void removeObject(SceneObject sceneObject){
        
    }
    
    public void removeObject(int sceneObjectID){
        
    }
    
    public void draw(GL3 gl){
        
        if(itemsNotInitialized.size() > 0){
            
            for(int i=0;i<itemsNotInitialized.size();i++){
                
                SceneObject sceneObject = scene.objectsList.get(i);
                sceneObject.initBuffers(gl);
                sceneObject.initVao(gl, shadersList.get(sceneObject.getShaderId()));
            }
            
            itemsNotInitialized = new ArrayList<>();
        }
    }
}
