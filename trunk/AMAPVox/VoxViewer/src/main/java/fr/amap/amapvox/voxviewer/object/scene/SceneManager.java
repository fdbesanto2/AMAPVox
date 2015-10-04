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

package fr.amap.amapvox.voxviewer.object.scene;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import fr.amap.amapvox.commons.math.matrix.Mat4F;
import fr.amap.amapvox.voxviewer.loading.shader.Shader;
import fr.amap.amapvox.voxviewer.mesh.GLMesh;
import fr.amap.amapvox.voxviewer.mesh.GLMeshFactory;
import fr.amap.amapvox.voxviewer.object.camera.Camera;
import fr.amap.amapvox.voxviewer.object.camera.CameraAdapter;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
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
    private int currentCameraID = -1;
    
    private List<Integer> itemsNotInitialized;
    
    private boolean viewMatrixChanged;
    private boolean projectionMatrixChanged;
    private boolean isInit;
    
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
    
    public void addShader(Shader shader) {
        
        shadersList.put(shader.getProgramId(),shader);
    }
    
    public void addCamera(Camera camera){
        scene.cameraList.add(camera);
    }
    
    public void setLastCameraCurrent(){
        
        if(scene.cameraList.size() > 0){
            currentCameraID = scene.cameraList.size();
        }
    }
    
    public void setCurrentCamera(Camera camera){
        
        currentCameraID = scene.cameraList.indexOf(camera);
    }
    
    public void setCurrentCamera(int id){
        
        if(id >= 0 && scene.cameraList.size() < id){
            currentCameraID = id;
        }
    }
    
    public Camera getCurrentCamera(){
        return scene.cameraList.get(currentCameraID);
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
        
        if(viewMatrixChanged || isInit){
            
            Mat4F normalMatrix = Mat4F.transpose(getCurrentCamera().getViewMatrix());
            FloatBuffer normalMatrixBuffer = Buffers.newDirectFloatBuffer(normalMatrix.mat);
            int id = scene.getShaderByName("noTranslationShader");
            Shader s = scene.getShadersList().get(id);
            gl.glUseProgram(id);
                gl.glUniformMatrix4fv(s.uniformMap.get("normalMatrix"), 1, false, normalMatrixBuffer);
            gl.glUseProgram(0);
            
            FloatBuffer viewMatrixBuffer = Buffers.newDirectFloatBuffer(getCurrentCamera().getViewMatrix().mat);
                    
            for(Map.Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {

                if(!shader.getValue().isOrtho){
                    gl.glUseProgram(shader.getKey());
                        gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("viewMatrix"), 1, false, viewMatrixBuffer);
                    gl.glUseProgram(0);
                }
            }
            
            viewMatrixChanged = false;
            
        }
        
        if(projectionMatrixChanged || isInit){
            
            FloatBuffer projMatrixBuffer = Buffers.newDirectFloatBuffer(getCurrentCamera().getProjectionMatrix().mat);

            for(Map.Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {

                if(!shader.getValue().isOrtho){
                    gl.glUseProgram(shader.getKey());
                        gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("projMatrix"), 1, false, projMatrixBuffer);
                    gl.glUseProgram(0);
                }
            }
            
            projectionMatrixChanged = false;
        }
        
        gl.glEnable(GL3.GL_BLEND);
        
        for(SceneObject sceneObject : scene.objectsList){
            sceneObject.draw(gl);
        }
    }
    
    public void initScene(GL3 gl) throws Exception{
                
        try{
                    /*    
            InputStreamReader noTranslationVertexShader = new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("shaders/NoTranslationVertexShader.txt"));
            InputStreamReader noTranslationFragmentShader = new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("shaders/NoTranslationFragmentShader.txt"));
            Shader noTranslationShader = new Shader(gl, noTranslationFragmentShader, noTranslationVertexShader, "noTranslationShader");
            noTranslationShader.setAttributeLocations(Shader.composeShaderAttributes(Shader.MINIMAL_SHADER_ATTRIBUTES, Shader.LIGHT_SHADER_ATTRIBUTES));
            noTranslationShader.setUniformLocations(Shader.composeShaderUniforms(Shader.MINIMAL_SHADER_UNIFORMS, Shader.LIGHT_SHADER_UNIFORMS));
            
            //logger.debug("shader compiled: "+noTranslationShader.name);
            
            InputStreamReader instanceVertexShader = new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("shaders/InstanceVertexShader.txt"));
            InputStreamReader instanceFragmentShader = new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("shaders/InstanceFragmentShader.txt"));
            Shader instanceShader = new Shader(gl, instanceFragmentShader, instanceVertexShader, "instanceShader");
            instanceShader.setUniformLocations(Shader.composeShaderUniforms(Shader.MINIMAL_SHADER_UNIFORMS));
            instanceShader.setAttributeLocations(Shader.composeShaderAttributes(new String[]{"position"}, Shader.INSTANCE_SHADER_ATTRIBUTES));
            
            //logger.debug("shader compiled: "+instanceShader.name);
            
            InputStreamReader textureVertexShader = new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("shaders/TextureVertexShader.txt"));
            InputStreamReader textureFragmentShader = new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("shaders/TextureFragmentShader.txt"));
            Shader texturedShader = new Shader(gl, textureFragmentShader, textureVertexShader, "textureShader");
            texturedShader.setUniformLocations(Shader.composeShaderUniforms(Shader.MINIMAL_SHADER_UNIFORMS, Shader.TEXTURE_SHADER_UNIFORMS));
            texturedShader.setAttributeLocations(Shader.composeShaderAttributes(new String[]{"position"}, Shader.TEXTURE_SHADER_ATTRIBUTES));
            texturedShader.isOrtho = true;

            //logger.debug("shader compiled: "+texturedShader.name);
            
            scene.addShader(noTranslationShader);
            scene.addShader(instanceShader);
            scene.addShader(texturedShader);
            
            GLMesh axisMesh = GLMeshFactory.createMeshFromObj(new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("mesh/axis.obj")),
                                            new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("mesh/axis.mtl")));
            
            //GLMesh axisMesh = GLMeshFactory.createMeshFromX3D(new InputStreamReader(SceneManager.class.getClassLoader().getResourceAsStream("mesh/axis.x3d")));
            axisMesh.setGlobalScale(0.03f);
            
            SceneObject axis = new SimpleSceneObject(axisMesh, noTranslationShader.getProgramId(), false);
            
            axis.setDrawType(GL3.GL_TRIANGLES);
            scene.addObject(axis, gl);
            
            getCurrentCamera().addCameraListener(new CameraAdapter() {
                
                @Override
                public void viewMatrixChanged(Mat4F viewMatrix) {
                    //must be updated inside the thread loop
                    viewMatrixChanged = true;
                }

                @Override
                public void projMatrixChanged(final Mat4F projMatrix) {
                    //must be updated inside the thread loop
                    projectionMatrixChanged = true;
                }
            });
            /*
            scene.addObject(voxelSpace, gl);
            
            voxelSpace.setSettings(settings);
            voxelSpace.setShaderId(instanceShader.getProgramId());

            scene.setVoxelSpace(voxelSpace);
            
            scene.canDraw = true;
            
            isInit = true;
            
            fireSceneInitialized();*/
            
        }catch(Exception e){
            throw new Exception("error in scene initialization", e);
        }
    }
}
