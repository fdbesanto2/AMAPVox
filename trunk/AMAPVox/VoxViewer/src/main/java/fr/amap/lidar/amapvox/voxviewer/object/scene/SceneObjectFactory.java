/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import fr.amap.commons.format.mesh3d.Obj;
import fr.amap.commons.format.mesh3d.ObjHelper;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.SimpleShader;
import fr.amap.lidar.amapvox.voxviewer.loading.texture.Texture;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;
import static fr.amap.lidar.amapvox.voxviewer.object.scene.Scene.colorShader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class SceneObjectFactory {
    
    private final static Logger logger = Logger.getLogger(SceneObjectFactory.class);
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, int width, int height, Texture texture){
        
        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, width, height), true);
        
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
    
    public static SceneObject createTexturedPlane(Vec3F startPoint, Texture texture, int shaderId){
        
        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createPlaneFromTexture(startPoint, texture, texture.getWidth(), texture.getHeight()), true);
        sceneObject.attachTexture(texture);
        
        return sceneObject;
    }
    
    public static SceneObject createGizmo(){
        
        SceneObject sceneObject = new SimpleSceneObject(GLMeshFactory.createLandmark(-5, 5), false);
        sceneObject.setPosition(new Point3F());
        
        SimpleShader colorShader = new SimpleShader();
        colorShader.setColor(new Vec3F(0, 0, 1));
        
        sceneObject.setShader(colorShader);
        sceneObject.setDrawType(GLMesh.DrawType.LINES);
        return sceneObject;
    }
    
    public static SimpleSceneObject createFlag() throws IOException{
        
        InputStream flag = SceneObjectFactory.class.getResourceAsStream("/mesh/flag.obj");
        InputStreamReader isr = new InputStreamReader(flag);
        
        Obj obj = ObjHelper.readObj(isr);
                
        GLMesh mesh = GLMeshFactory.createMesh(obj.getPoints(), obj.getNormals(), obj.get1DFaces());
        
        int nbPoints = obj.getPoints().length;
        float colorData[] = new float[nbPoints * 3];
        
        for(int i=0, j=0;i<nbPoints;i++,j+=3){
            
            colorData[j] = 0;
            colorData[j+1] = 0;
            colorData[j+2] = 0;
        }
        
        mesh.colorBuffer = Buffers.newDirectFloatBuffer(colorData);
        
        SimpleSceneObject sceneObjectFlag = new SimpleSceneObject(mesh, false);
        
        return sceneObjectFlag;
    }
    
}
