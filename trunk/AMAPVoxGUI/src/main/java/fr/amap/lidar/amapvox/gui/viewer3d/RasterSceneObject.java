/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.gui.viewer3d;

import com.jogamp.common.nio.Buffers;
import fr.amap.commons.math.point.Point3D;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.raster.asc.Point;
import fr.amap.commons.raster.asc.Face;
import fr.amap.commons.raster.asc.Raster;
import fr.amap.commons.structure.octree.Octree;
import fr.amap.commons.util.ColorGradient;
import fr.amap.viewer3d.loading.shader.PhongShader;
import fr.amap.viewer3d.mesh.GLMesh;
import fr.amap.viewer3d.mesh.SimpleGLMesh;
import fr.amap.viewer3d.object.scene.MousePicker;
import fr.amap.viewer3d.object.scene.ScalarSceneObject;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */
public class RasterSceneObject extends ScalarSceneObject{
    
    private final static Logger LOGGER = Logger.getLogger(RasterSceneObject.class);
    
    private Octree octree;
    
    public RasterSceneObject(Raster raster){
        
        super();
        
        mesh = new SimpleGLMesh();
        
        if(!raster.isBuilt()){
            raster.buildMesh();
        }
        
        initMesh(raster);
        initColors(raster);
        computeNormales(raster);
        
        setGravityCenter(mesh.getGravityCenter());
        
        super.setShader(new PhongShader());
    }
    
    public RasterSceneObject(GLMesh mesh) {
        
        super(mesh, false);
        
        getElevationsFromMesh(mesh);
        init();
    }
    
    private void initOctree(){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        if(vertexBuffer != null){
            
            octree = new Octree(50);
            
            float[] array = new float[vertexBuffer.capacity()];

            for(int i = 0; i<vertexBuffer.capacity(); i++){
                
                array[i] = vertexBuffer.get(i);
            }

            octree.setPoints(array);
            try {
                octree.build();
            } catch (Exception ex) {
                LOGGER.error("The octree build failed.");
            }
        }
        
    }
    
    public Point3F getVertex(int index){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        if(vertexBuffer != null){
            
            int x = index * 3;
            int y = x+1;
            int z = x+2;
            
            if(z < vertexBuffer.capacity()){
                return new Point3F(vertexBuffer.get(x), vertexBuffer.get(y), vertexBuffer.get(z));
            }            
        }
        
        return null;
    }
    
    private void initMesh(Raster raster){
        
        List<Point> points = raster.getPoints();
        List<Face> faces = raster.getFaces();
        
        float[] vertexData = new float[points.size()*3];
        
        for(int i=0,j=0 ; i<points.size(); i++, j+=3){
            
            vertexData[j] = points.get(i).x;
            vertexData[j+1] = points.get(i).y;
            vertexData[j+2] = points.get(i).z;
        }
        
        int indexData[] = new int[faces.size()*3];
        for(int i=0, j=0 ; i<faces.size(); i++, j+=3){
            
            indexData[j] = faces.get(i).getPoint1();
            indexData[j+1] = faces.get(i).getPoint2();
            indexData[j+2] = faces.get(i).getPoint3();
        }
        
        mesh.setVertexBuffer(Buffers.newDirectFloatBuffer(vertexData));
        mesh.indexBuffer = Buffers.newDirectIntBuffer(indexData);
        mesh.vertexCount = indexData.length;
    }
    
    private void initColors(Raster raster){
        
        getElevationsFromMesh(mesh);
        
        init();
        switchToNextColor();
        
        /*List<DTMPoint> points = raster.getPoints();
        
        ColorGradient gradient = new ColorGradient(raster.getzMin(), raster.getzMax());
        gradient.setGradientColor(ColorGradient.GRADIENT_RAINBOW3);

        float colorData[] = new float[points.size()*3];
        for(int i=0, j=0;i<points.size();i++,j+=3){

            Color color = gradient.getColor(points.get(i).z);
            colorData[j] = color.getRed()/255.0f;
            colorData[j+1] = color.getGreen()/255.0f;
            colorData[j+2] =  color.getBlue()/255.0f;

        }

        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);*/
    }
    
    private void computeNormales(Raster raster){
        
        List<Face> faces = raster.getFaces();
        List<Point> points = raster.getPoints();
        
        float[] normalData = new float[points.size()*3];
        for(int i=0,j=0 ; i<points.size(); i++, j+=3){
            
            Vec3F meanNormale = new Vec3F(0, 0, 0);
            
            for(Integer faceIndex : points.get(i).faces){
                
                Face face = faces.get(faceIndex);
                
                Point point1 = points.get(face.getPoint1());
                Point point2 = points.get(face.getPoint2());
                Point point3 = points.get(face.getPoint3());
                
                Vec3F vec1 = Vec3F.substract(new Vec3F(point2.x, point2.y, point2.z), new Vec3F(point1.x, point1.y, point1.z));
                Vec3F vec2 = Vec3F.substract(new Vec3F(point3.x, point3.y, point3.z), new Vec3F(point1.x, point1.y, point1.z));
                
                meanNormale = Vec3F.add(meanNormale, Vec3F.normalize(Vec3F.cross(vec2, vec1)));
                
            }
            
            meanNormale = Vec3F.normalize(meanNormale);
            
            normalData[j] = meanNormale.x;
            normalData[j+1] = meanNormale.y;
            normalData[j+2] = meanNormale.z;
        }
        
        mesh.normalBuffer = Buffers.newDirectFloatBuffer(normalData);
    }
    
    private void getElevationsFromMesh(GLMesh mesh){
        
        FloatBuffer vertexBuffer = mesh.getVertexBuffer();
        
        if(vertexBuffer != null){
            
            for(int j = 0 ; j<vertexBuffer.capacity(); j+=3){
                float z = vertexBuffer.get(j+2);
                addValue("Elevation", z, true);
            }
        }
    }
    
    /**
     * When picking a raster scene object, the element returned is the nearest point to the ray.
     * @param mousePicker The current mouse picker.
     * @return The nearest point or null if elements were not closed enough
     */
    @Override
    public Integer doPicking(MousePicker mousePicker) {
        
        
        Point3F startPoint = MousePicker.getPointOnray(mousePicker.getCamPosition(), mousePicker.getCurrentRay(), 0);
        Point3F endPoint = MousePicker.getPointOnray(mousePicker.getCamPosition(), mousePicker.getCurrentRay(), 600);
        
        int closestElement = octree.getClosestElement(new Point3D(startPoint.x, startPoint.y, startPoint.z),
                new Point3D(endPoint.x, endPoint.y, endPoint.z), 1.0f);
        
        if(closestElement > 0){
            
            return closestElement;
        }
        
        return null;
    }
    
    @Override
    public void setMousePickable(boolean isPickable){
        
        super.setMousePickable(isPickable);
        
        if(isPickable){
            initOctree();
        }
    }
    
    
}
