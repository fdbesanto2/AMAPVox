/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.util.Statistic;
import fr.amap.commons.structure.octree.Octree;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.lidar.amapvox.voxviewer.mesh.PointCloudGLMesh;
import gnu.trove.list.array.TFloatArrayList;
import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author calcul
 */
public class PointCloudSceneObject extends ScalarSceneObject{

    private Statistic xPositionStatistic;
    private Statistic yPositionStatistic;
    private Statistic zPositionStatistic;
    
    private TFloatArrayList vertexDataList;
    
    private Octree octree;
    
    /*
     * TODO : implement LOD
     * just load inside the list a point for each 100 points loaded
     * Switch point cloud draw to LOD when user interacts with the scene (camera translation, zoom)
     */
            
    
    public PointCloudSceneObject(){
        
        vertexDataList = new TFloatArrayList();
        
        xPositionStatistic = new Statistic();
        yPositionStatistic = new Statistic();
        zPositionStatistic = new Statistic();
        
        this.mousePickable = false;
        
        if(mousePickable){
            octree = new Octree(50);
        }
    }
    
    public void addPoint(float x, float y, float z){
        
        vertexDataList.add(x);
        vertexDataList.add(y);
        vertexDataList.add(z);
        
        xPositionStatistic.addValue(x);
        yPositionStatistic.addValue(y);
        zPositionStatistic.addValue(z);
    }
    
    @Override
    public void switchColor(String colorAttributIndex){
        
        if(mesh == null){
            
            mesh = new PointCloudGLMesh();
            initMesh();
        }
        
        super.switchColor(colorAttributIndex);
    }
    
    public void initMesh(){
        
        this.gravityCenter = new Point3F((float)(xPositionStatistic.getMean()),
                                    (float)(yPositionStatistic.getMean()),
                                    (float)(zPositionStatistic.getMean()));
        
        Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            iterator.next().getValue().buildHistogram();
        }
        
        ScalarField scalarField = scalarFieldsList.entrySet().iterator().next().getValue();
        
        
        
        float[] points = vertexDataList.toArray();
        mesh = GLMeshFactory.createPointCloud(points, updateColor(scalarField));
        
        currentAttribut = scalarField.getName();
        
        if(octree != null){
            octree.setPoints(points);
        }
        
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

    @Override
    public String doPicking(MousePicker mousePicker) {
        
        /*Vec3F currentRay = mousePicker.getCurrentRay();
        
        Point3F closestPoint = mousePicker.getPointOnray(camPosition, currentRay, 1);
        Point3F farestPoint = mousePicker.getPointOnray(camPosition, currentRay, 99999);*/
        
        
        return ("");
    }

    public Octree getOctree() {
        return octree;
    }
}
