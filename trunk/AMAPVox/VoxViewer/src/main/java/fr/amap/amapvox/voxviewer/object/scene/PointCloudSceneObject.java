/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import fr.amap.amapvox.commons.util.Statistic;
import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.mesh.PointCloudGLMesh;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author calcul
 */
public class PointCloudSceneObject extends SimpleSceneObject{

    private Statistic xPositionStatistic;
    private Statistic yPositionStatistic;
    private Statistic zPositionStatistic;
    
    private TFloatArrayList vertexDataList;
    private Map<String, ScalarField> scalarFieldsList;
    private String currentAttribut;
            
    public PointCloudSceneObject(PointCloudGLMesh mesh, boolean isAlphaRequired){
        super(mesh, isAlphaRequired, new Point3F());
    }
    
    public PointCloudSceneObject(){
        
        vertexDataList = new TFloatArrayList();
        scalarFieldsList = new HashMap<>();
        
        xPositionStatistic = new Statistic();
        yPositionStatistic = new Statistic();
        zPositionStatistic = new Statistic();
    }
    
    public void addPoint(float x, float y, float z){
        
        vertexDataList.add(x);
        vertexDataList.add(y);
        vertexDataList.add(z);
        
        xPositionStatistic.addValue(x);
        yPositionStatistic.addValue(y);
        zPositionStatistic.addValue(z);
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
            
            mesh = new PointCloudGLMesh();
            initMesh();
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
    
    public void initMesh(){
        
        this.position = new Point3F((float)(xPositionStatistic.getMean()),
                                    (float)(yPositionStatistic.getMean()),
                                    (float)(zPositionStatistic.getMean()));
        
        Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            iterator.next().getValue().buildHistogram();
        }
        
        ScalarField scalarField = scalarFieldsList.entrySet().iterator().next().getValue();
        
        
        

        mesh = GLMeshFactory.createPointCloud(vertexDataList.toArray(), updateColor(scalarField));
        
        currentAttribut = scalarField.getName();
    }
    
    public int getNumberOfPoints(){
        
        if(vertexDataList == null){
            return mesh.vertexCount;
        }
        return vertexDataList.size()/3;
    }
    
    @Override
    public void load(File file) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Map<String, ScalarField> getScalarFieldsList() {
        return scalarFieldsList;
    }
}
