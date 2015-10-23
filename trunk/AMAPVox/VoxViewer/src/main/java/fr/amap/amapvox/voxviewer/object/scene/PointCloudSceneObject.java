/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import fr.amap.amapvox.math.point.Point3F;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.mesh.PointCloudGLMesh;
import gnu.trove.list.TFloatList;
import gnu.trove.list.array.TFloatArrayList;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author calcul
 */
public class PointCloudSceneObject extends SimpleSceneObject{

    private TFloatArrayList vertexDataList;
    private TFloatArrayList[] colorDataList;
    
    private float[][] colorDataArray;
    
    private int colorAttributsNumber;
            
    public PointCloudSceneObject(PointCloudGLMesh mesh, boolean isAlphaRequired){
        super(mesh, isAlphaRequired, new Point3F());
    }
    
    public PointCloudSceneObject(int colorAttributsNumber){
        
        vertexDataList = new TFloatArrayList();
        colorDataList = new TFloatArrayList[colorAttributsNumber];
        this.colorAttributsNumber = colorAttributsNumber;
        
        for(int i=0 ; i < colorDataList.length;i++){
            
            colorDataList[i] = new TFloatArrayList();
        }
        
        
    }
    
    public void addPoint(float x, float y, float z){
        
        vertexDataList.add(x);
        vertexDataList.add(y);
        vertexDataList.add(z);
    }
    
    public void addColor(int index, float red, float green, float blue){
        
        if(index < colorDataList.length){
            colorDataList[index].add(red);
            colorDataList[index].add(green);
            colorDataList[index].add(blue);
        }
    }
    
    public void switchColor(int colorAttributIndex){
        
        
        if(mesh == null){
            
            mesh = new PointCloudGLMesh();
            initMesh();
        }
        
        if(colorAttributIndex < colorDataArray.length){
            mesh.setColorData(colorDataArray[colorAttributIndex]);
        }
        
        colorNeedUpdate = true;
    }
    
    public void initMesh(){
        
        if(colorDataList.length > 0){
            
            colorDataArray = new float[colorAttributsNumber][colorDataList[0].size()];
            
            for(int i=0;i<colorDataArray.length;i++){
                colorDataArray[i] = colorDataList[i].toArray();
                colorDataList[i] = new TFloatArrayList();
            }
            
            colorDataList = null;
            
            mesh = GLMeshFactory.createPointCloud(vertexDataList.toArray(), colorDataArray[0]);
        }
    }
    
    public int getNumberOfPoints(){
        
        if(vertexDataList == null){
            return mesh.vertexCount;
        }
        return vertexDataList.size()/3;
    }
}
