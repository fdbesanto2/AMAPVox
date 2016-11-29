/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.util.Statistic;
import fr.amap.commons.structure.octree.Octree;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.BillboardPCLShader;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import static fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh.FLOAT_SIZE;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.lidar.amapvox.voxviewer.mesh.InstancedGLMesh;
import fr.amap.lidar.amapvox.voxviewer.mesh.PointCloudGLMesh;
import gnu.trove.list.array.TFloatArrayList;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.Iterator;
import java.util.Map;
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
    
    private TFloatArrayList vertexDataList;
    
    private Octree octree;
    
    private int pointNumber;
    private FloatBuffer vertices;
    private int drawEveryNPoints = -1;
    private int stride = 0;
    
    public PointCloudSceneObject(){
        
        //this.generateLOD = true;
        vertexDataList = new TFloatArrayList();
        
        xPositionStatistic = new Statistic();
        yPositionStatistic = new Statistic();
        zPositionStatistic = new Statistic();
    }
    
//    public PointCloudSceneObject(int numberOfPoints){
//        
//        //this.generateLOD = true;
//        vertices = FloatBuffer.allocate(numberOfPoints*3 + 6000 /*add for safety*/);
//        //vertexDataList = new TFloatArrayList();
//        
//        xPositionStatistic = new Statistic();
//        yPositionStatistic = new Statistic();
//        zPositionStatistic = new Statistic();
//    }
    
    public void addPoint(float x, float y, float z){
        
        /*vertices.put(x);
        vertices.put(y);
        vertices.put(z);*/
        pointNumber++;
        
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
    
    public void setPointSize(int size){
        
        if(size == 0){ //view pointcloud as default with 1 pixel size
            
        }else{ //draw pointcloud as billboards
            mesh = new InstancedGLMesh(GLMeshFactory.createPlane(new Vec3F(), size, size), vertexDataList.size()/3);
            
            ((InstancedGLMesh)mesh).instancePositionsBuffer = Buffers.newDirectFloatBuffer(vertexDataList.toArray());
            
            ScalarField scalarField = scalarFieldsList.get(currentAttribut);
            ((InstancedGLMesh)mesh).instanceColorsBuffer = Buffers.newDirectFloatBuffer(updateColor(scalarField));
            ((InstancedGLMesh)mesh).setInstanceNumber(getNumberOfPoints());
            this.setShader(new BillboardPCLShader());
            
            this.resetIds();
        }
    }
    
    @Override
    public void initBuffers(GL3 gl){
        
        if(mesh instanceof InstancedGLMesh){
            int maxSize = (mesh.getVertexBuffer().capacity()*GLMesh.FLOAT_SIZE)+(getNumberOfPoints()*3*GLMesh.FLOAT_SIZE)+(getNumberOfPoints()*4*GLMesh.FLOAT_SIZE);
            mesh.initBuffers(gl, maxSize);

            ((InstancedGLMesh)mesh).setInstanceNumber(getNumberOfPoints());
        }else{
            super.initBuffers(gl);
        }
    }
    
    @Override
    public void initVao(GL3 gl){
        
        if (mesh != null) {
            //generate vao
            IntBuffer tmp = IntBuffer.allocate(1);
            gl.glGenVertexArrays(1, tmp);
            vaoId = tmp.get(0);

            gl.glBindVertexArray(vaoId);

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, mesh.getVboId());

            gl.glEnableVertexAttribArray(shader.attributeMap.get("position"));
            gl.glVertexAttribPointer(shader.attributeMap.get("position"), mesh.dimensions, GL3.GL_FLOAT, false, stride, 0);

            if (mesh.colorBuffer != null) {
                try {
                    gl.glEnableVertexAttribArray(shader.attributeMap.get("color"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("color"), mesh.dimensions, GL3.GL_FLOAT, false, stride, mesh.getVertexBuffer().capacity() * FLOAT_SIZE);

                } catch (Exception e) {
                }
                try {
                    gl.glEnableVertexAttribArray(shader.attributeMap.get("normal"));
                    gl.glVertexAttribPointer(shader.attributeMap.get("normal"), mesh.dimensions, GL3.GL_FLOAT, false, stride, mesh.getVertexBuffer().capacity() * FLOAT_SIZE + mesh.normalBuffer.capacity() * FLOAT_SIZE);
                } catch (Exception e) {
                }
            }

            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, mesh.getIboId());

            gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);

            gl.glBindVertexArray(0);
        }
        
    }
    
    public void initMesh(){
        
        setGravityCenter(new Point3F((float)(xPositionStatistic.getMean()),
                                    (float)(yPositionStatistic.getMean()),
                                    (float)(zPositionStatistic.getMean())));
        
        Iterator<Map.Entry<String, ScalarField>> iterator = scalarFieldsList.entrySet().iterator();
        
        while(iterator.hasNext()){
            
            iterator.next().getValue().buildHistogram();
        }
        
        ScalarField scalarField = scalarFieldsList.entrySet().iterator().next().getValue();
        
        
        float[] points = vertexDataList.toArray();
        vertexDataList = null;
        
        mesh = GLMeshFactory.createPointCloud(points, updateColor(scalarField));
        /*mesh = new PointCloudGLMesh();
        vertices.compact();
        mesh.setVertexBuffer(vertices);
        mesh.vertexCount = pointNumber;
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(updateColor(scalarField));*/
        
        currentAttribut = scalarField.getName();
        
        if(mousePickable){
            octree = new Octree(50);
            //octree.setPoints(points);
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

    public void setDrawEveryNPoints(int drawEveryNPoints) {
        this.drawEveryNPoints = drawEveryNPoints;
        stride = 3 * GLMesh.FLOAT_SIZE * drawEveryNPoints;
    }

    public int getDrawEveryNPoints() {
        return drawEveryNPoints;
    }
}
