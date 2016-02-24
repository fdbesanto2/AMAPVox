/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author calcul
 */
public class ScalarSceneObject extends SimpleSceneObject{
    
    protected final Map<String, ScalarField> scalarFieldsList;
    protected String currentAttribut;
    
    public ScalarSceneObject() {
        this.scalarFieldsList = new HashMap<>();
    }
    
    public ScalarSceneObject(GLMesh mesh, boolean isAlphaRequired) {
        
        super(mesh, isAlphaRequired);
        
        this.scalarFieldsList = new HashMap<>();
        
    }
    
    protected final void init(){
                
        Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            iterator.next().getValue().buildHistogram();
        }
        
        ScalarField scalarField = scalarFieldsList.entrySet().iterator().next().getValue();
        
        currentAttribut = scalarField.getName();
        
    }
    
    public void switchToNextColor(){
        
        Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            String key = iterator.next().getKey();
            if(key.equals(currentAttribut)){
                
                if(iterator.hasNext()){
                    switchColor(iterator.next().getKey());
                }else{
                    switchColor(scalarFieldsList.entrySet().iterator().next().getKey());
                }
            }
        }
    }
    
    public void switchColor(String colorAttributIndex){
        
        
        if(mesh == null){
            
            //mesh = new PointCloudGLMesh();
            //initMesh();
        }
        
        if(scalarFieldsList.containsKey(colorAttributIndex)){
            
            ScalarField scalarField = scalarFieldsList.get(colorAttributIndex);
            
            currentAttribut = scalarField.getName();
            updateColor();
        }
    }
    
    public float[] updateColor(ScalarField scalarField){
        
        int nbValues;
        float[] colorDataArray;
        
        if(scalarField.hasColorGradient){
            
            nbValues = scalarField.getNbValues()*3;
            colorDataArray = new float[nbValues];
            
            for(int i=0, j=0;i<scalarField.getNbValues();i++, j+=3){

                colorDataArray[j] = scalarField.getColor(i).getRed()/255.0f;
                colorDataArray[j+1] = scalarField.getColor(i).getGreen()/255.0f;
                colorDataArray[j+2] = scalarField.getColor(i).getBlue()/255.0f;
            }
        }else{
            
            nbValues = scalarField.getNbValues();
            colorDataArray = new float[nbValues];
            
            for(int i=0;i<scalarField.getNbValues();i++){
                colorDataArray[i] = scalarField.getValue(i)/255.0f;
            }
        }
        
        return colorDataArray;
    }
    
    public void updateColor(){
        
        ScalarField scalarField = scalarFieldsList.get(currentAttribut);
        
        int nbValues;
        float[] colorDataArray;
        
        if(scalarField.hasColorGradient){
            
            nbValues = scalarField.getNbValues()*3;
            colorDataArray = new float[nbValues];
            
            for(int i=0, j=0;i<scalarField.getNbValues();i++, j+=3){

                colorDataArray[j] = scalarField.getColor(i).getRed()/255.0f;
                colorDataArray[j+1] = scalarField.getColor(i).getGreen()/255.0f;
                colorDataArray[j+2] = scalarField.getColor(i).getBlue()/255.0f;
            }
        }else{
            
            nbValues = scalarField.getNbValues();
            colorDataArray = new float[nbValues];
            
            for(int i=0;i<scalarField.getNbValues();i++){
                colorDataArray[i] = scalarField.getValue(i)/255.0f;
            }
        }

        mesh.setColorData(colorDataArray);
        colorNeedUpdate = true;
    }
    
    public Map<String, ScalarField> getScalarFieldsList() {
        return scalarFieldsList;
    }
    
    public void addValue(String index, float value){
        
        if(!scalarFieldsList.containsKey(index)){
            
            scalarFieldsList.put(index, new ScalarField(index));
        }
        
        scalarFieldsList.get(index).addValue(value);
    }
    
    public void addValue(String index, float value, boolean gradient){
        
        
        if(!scalarFieldsList.containsKey(index)){
            ScalarField scalarField = new ScalarField(index);
            scalarField.hasColorGradient = gradient;
            scalarFieldsList.put(index, scalarField);
        }
        
        scalarFieldsList.get(index).addValue(value);
    }
}
