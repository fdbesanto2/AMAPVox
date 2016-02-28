/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.math.point.Point3D;
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
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class PointCloudSceneObject extends ScalarSceneObject{

    private final static Logger LOGGER = Logger.getLogger(PointCloudSceneObject.class);
            
    private final Statistic xPositionStatistic;
    private final Statistic yPositionStatistic;
    private final Statistic zPositionStatistic;
    
    private final TFloatArrayList vertexDataList;
    private final TFloatArrayList lodVertexDataList;
    
    private Octree octree;
    
    /*
     * TODO : implement LOD
     * just load inside the list a point for each 100 points loaded
     * Switch point cloud draw to LOD when user interacts with the scene (camera translation, zoom)
     */
    //LOD
    private boolean generateLOD;
    private float percentageLOD;
    
    //keep one point for x points
    private int onePointOf = 10;
    
    private int nbPointsAdded = 0;
    
    public PointCloudSceneObject(){
        
        //this.generateLOD = true;
        
        vertexDataList = new TFloatArrayList();
        lodVertexDataList = new TFloatArrayList();
        
        xPositionStatistic = new Statistic();
        yPositionStatistic = new Statistic();
        zPositionStatistic = new Statistic();
    }
    
    public void addPoint(float x, float y, float z){
        
        if(nbPointsAdded == onePointOf && generateLOD){
            lodVertexDataList.add(x);
            lodVertexDataList.add(y);
            lodVertexDataList.add(z);
            nbPointsAdded = 0;
        }else{
            vertexDataList.add(x);
            vertexDataList.add(y);
            vertexDataList.add(z);
        }
        
        nbPointsAdded++;
        
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
    
    @Override
    public void addValue(String index, float value){
        
        if(!scalarFieldsList.containsKey(index)){
            
            scalarFieldsList.put(index, new ScalarField(index));
        }
        
        scalarFieldsList.get(index).addValue(value);
    }
    
    @Override
    public void addValue(String index, float value, boolean gradient){
        
        
        if(!scalarFieldsList.containsKey(index)){
            ScalarField scalarField = new ScalarField(index);
            scalarField.hasColorGradient = gradient;
            scalarFieldsList.put(index, scalarField);
        }
        
        scalarFieldsList.get(index).addValue(value);
    }
    
    public void switchToLOD(){
        
        if(generateLOD){
            ((PointCloudGLMesh)mesh).vertexCount = lodVertexDataList.size()/3;
        }
    }
    
    public void switchToMaxDetail(){
        
        if(generateLOD){
            ((PointCloudGLMesh)mesh).vertexCount = (lodVertexDataList.size()+vertexDataList.size())/3;
        }
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
        
        if(generateLOD){
            float[] lodPoints = lodVertexDataList.toArray();
            points = ArrayUtils.addAll(lodPoints, points);
        }
        
        mesh = GLMeshFactory.createPointCloud(points, updateColor(scalarField));
        
        currentAttribut = scalarField.getName();
        
        if(mousePickable){
            octree = new Octree(50);
            octree.setPoints(points);
            try {
                octree.build();
            } catch (Exception ex) {
                LOGGER.error("The octree build failed.");
            }
        }
        
    }
    
    public int getNumberOfPoints(){
        
        if(vertexDataList == null){
            return mesh.vertexCount;
        }
        return vertexDataList.size()/3;
    }

    /**
     * When picking a pointcloud scene object, the element returned is the nearest point to the ray.
     * @param mousePicker The current mouse picker.
     * @return The nearest point or null if elements were not closed enough
     */
    @Override
    public Point3F doPicking(MousePicker mousePicker) {
        
        
        Point3F startPoint = MousePicker.getPointOnray(mousePicker.getCamPosition(), mousePicker.getCurrentRay(), 0);
        Point3F endPoint = MousePicker.getPointOnray(mousePicker.getCamPosition(), mousePicker.getCurrentRay(), 600);
        
        int closestElement = octree.getClosestElement(new Point3D(startPoint.x, startPoint.y, startPoint.z),
                new Point3D(endPoint.x, endPoint.y, endPoint.z), 0.1f);
        
        if(closestElement > 0){
            
            Point3D point = octree.getPoints()[closestElement];
            return new Point3F((float)point.x, (float)point.y, (float)point.z);
        }
        
        return null;
    }

    public Octree getOctree() {
        return octree;
    }

    public boolean isGenerateLOD() {
        return generateLOD;
    }

    public void setGenerateLOD(boolean generateLOD) {
        this.generateLOD = generateLOD;
    }

    public float getPercentageLOD() {
        return percentageLOD;
    }

    public void setPercentageLOD(float percentageLOD) {
        
        this.percentageLOD = percentageLOD;
        
        percentageLOD = Float.min(percentageLOD, 100);
        percentageLOD = Float.max(percentageLOD, 0);
        
        onePointOf = (int) (100/percentageLOD);
    }
}
