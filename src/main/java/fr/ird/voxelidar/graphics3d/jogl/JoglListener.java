/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.jogl;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.frame.JFrameSettingUp;
import fr.ird.voxelidar.frame.JFrameTools;
import fr.ird.voxelidar.frame.JProgressLoadingFile;
import fr.ird.voxelidar.listener.EventManager;
import fr.ird.voxelidar.graphics3d.mesh.MeshFactory;
import fr.ird.voxelidar.graphics3d.object.camera.CameraAdapter;
import fr.ird.voxelidar.graphics3d.object.camera.TrackballCamera;
import fr.ird.voxelidar.graphics3d.object.scene.Scene;
import fr.ird.voxelidar.graphics3d.object.scene.SceneObject;
import fr.ird.voxelidar.graphics3d.object.terrain.Terrain;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace.Format;
import fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpaceAdapter;
import fr.ird.voxelidar.graphics3d.shader.Shader;
import fr.ird.voxelidar.math.matrix.Mat4F;
import fr.ird.voxelidar.math.vector.Vec3F;
import fr.ird.voxelidar.util.Settings;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.Map.Entry;
import javax.media.opengl.GL3;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien
 */
public class JoglListener implements GLEventListener {
    
    private Shader simpleShader;
    private Mat4F modelViewMatrix;
    private Mat4F projectionMatrix;
    private VoxelSpace voxelSpace;
    private EventManager eventListener;
    TrackballCamera camera;
    public final JFrameSettingUp parent;
    private Scene scene;
    private Vec3F worldColor;
    public int width;
    public int height;
    public JFrameTools toolBox;
    private Terrain terrain;
    private Settings settings;
    private boolean isFpsInit = false;
    final static Logger logger = Logger.getLogger(JoglListener.class);

    public Settings getSettings() {
        return settings;
    }

    public Terrain getTerrain() {
        return terrain;
    }
    
    public Scene getScene() {
        return scene;
    }

    public Vec3F getWorldColor() {
        return worldColor;
    }

    public void setWorldColor(Vec3F worldColor) {
        this.worldColor = worldColor;
    }

    public TrackballCamera getCamera() {
        return camera;
    }
    
    public void attachToolBox(JFrameTools toolbox){
        
        this.toolBox = toolbox;
    }
    
    public JoglListener(JFrameSettingUp parent, Terrain terrain, Settings settings){
        
        this.terrain = terrain;
        this.settings = settings;
        this.parent = parent;
        worldColor = new Vec3F(200.0f/255.0f, 200.0f/255.0f, 200.0f/255.0f);
    }
    
    public void attachEventListener(EventManager eventListener){
        
        this.eventListener = eventListener;
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        
        GL3 gl = drawable.getGL().getGL3();
        
        //calculate fps every 61 frames (wait render speed to stabilize)
        drawable.getAnimator().setUpdateFPSFrames(61, null);
        
        //this.width = drawable.getWidth();
        //this.height = drawable.getHeight();
        
        Vec3F eye = new Vec3F(22.75f+45.5f, 72.25f, 195.25f-390.5f);
        Vec3F target = new Vec3F(22.75f, 72.25f, 195.25f);
        Vec3F up = new Vec3F(0.0f, 1.0f, 0.0f);
        
        
        
        camera = new TrackballCamera();
        camera.init(eye, target, up);
        
        initScene(gl);
        
        //camera.setPerspective(60.0f, (1.0f*drawable.getWidth())/drawable.getHeight(), 0.1f, 1000.0f);
        
        
        
        projectionMatrix = new Mat4F();
        modelViewMatrix = new Mat4F();
        
        
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA );
        
        gl.glDepthFunc(GL3.GL_LEQUAL);
        
        gl.glEnable(GL3.GL_DEPTH_TEST);
        
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        
        //gl.glPolygonMode(GL3.GL_FRONT_AND_BACK, GL3.GL_LINE);
        //gl.glDisable(GL3.GL_CULL_FACE);
        
        gl.glHint(GL3.GL_LINE_SMOOTH_HINT, GL3.GL_NICEST);
        gl.glHint(GL3.GL_GENERATE_MIPMAP_HINT, GL3.GL_NICEST);
        gl.glHint(GL3.GL_POLYGON_SMOOTH_HINT, GL3.GL_NICEST);
        gl.glHint(GL3.GL_FRAGMENT_SHADER_DERIVATIVE_HINT, GL3.GL_NICEST);
        
        //drawable.setAutoSwapBufferMode(true);
        
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        
        
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        
        update();
        render(drawable);
    }
    
    private void update() {
        
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        
        GL3 gl=drawable.getGL().getGL3();
        gl.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        
        camera.setPerspective(60.0f, (1.0f*width)/height, 0.1f, 1000.0f);
    }
    
    private void render(GLAutoDrawable drawable) {
        
        
        
        GL3 gl=drawable.getGL().getGL3();
        gl.glViewport(0, 0, width, height);
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT|GL3.GL_COLOR_BUFFER_BIT);
        gl.glClearColor(worldColor.x, worldColor.y, worldColor.z, 1.0f);
        
        gl.glDisable(GL3.GL_BLEND );
        
        eventListener.updateEvents(this); 
        
        scene.draw(gl, camera);
        
        /*optimize interaction by reducing fps
        cause: the fps animator use all cpu to render the scene 
        involving that the window event thread is not quickly called
        WARNING: this optimization is not proper and cause bad render performance
        */
        
        if(!isFpsInit && (int)drawable.getAnimator().getLastFPS()>0){
            
            //keep performance for events
            int offset = 8;
            
            drawable.getAnimator().stop();
            ((FPSAnimator)drawable.getAnimator()).setFPS((int)((FPSAnimator)drawable.getAnimator()).getLastFPS()-offset);
            isFpsInit = true;
            drawable.getAnimator().start();
        }
           
        //System.out.println((int)((FPSAnimator)drawable.getAnimator()).getLastFPS());
        //System.out.println((int)((FPSAnimator)drawable.getAnimator()).getFPS());
    }
    
    private void initScene(final GL3 gl){
        
        scene = new Scene();
        
        try{
            //set shaders
            InputStreamReader simpleVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/SimpleVertexShader.txt"));
            InputStreamReader simpleFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/SimpleFragmentShader.txt"));
            Shader basicShader = new Shader(gl, simpleFragmentShader, simpleVertexShader, "basicShader");
            basicShader.setUniformLocations(new String[]{"viewMatrix","projMatrix"});
            basicShader.setAttributeLocations(new String[]{"position","color"});
            
            logger.debug("shader compiled: "+basicShader.name);

            InputStreamReader instanceVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/InstanceVertexShader.txt"));
            InputStreamReader instanceFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/InstanceFragmentShader.txt"));
            Shader instanceShader = new Shader(gl, instanceFragmentShader, instanceVertexShader, "instanceShader");
            instanceShader.setUniformLocations(new String[]{"viewMatrix","projMatrix"});
            instanceShader.setAttributeLocations(new String[]{"position", "instance_position", "instance_color"});
            
            logger.debug("shader compiled: "+instanceShader.name);
            
            InputStreamReader textureVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/TextureVertexShader.txt"));
            InputStreamReader textureFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/TextureFragmentShader.txt"));
            Shader orthoShader = new Shader(gl, textureFragmentShader, textureVertexShader, "textureShader");
            orthoShader.setUniformLocations(new String[]{"viewMatrix","projMatrix","texture"});
            orthoShader.setAttributeLocations(new String[]{"position","textureCoordinates"});
            orthoShader.isOrtho = true;

            logger.debug("shader compiled: "+orthoShader.name);

            scene.addShader(instanceShader);
            scene.addShader(basicShader);
            scene.addShader(orthoShader);
            
            if(settings.drawAxis){
                SceneObject sceneObject = new SceneObject(MeshFactory.createLandmark(-1000, 1000), basicShader.getProgramId(), false);
                sceneObject.setDrawType(GL3.GL_LINES);
                scene.addObject(sceneObject, gl);
            }
            
            if(settings.drawTerrain){
                SceneObject terrainSceneObject = new SceneObject(MeshFactory.createMesh(terrain.getPoints(), terrain.getIndices()), basicShader.getProgramId(), true);
                terrainSceneObject.setDrawType(GL3.GL_TRIANGLES);
                scene.addObject(terrainSceneObject, gl);
            }
            
            camera.addCameraListener(new CameraAdapter() {
                
                
                @Override
                public void locationChanged(final Vec3F location) {
                    
                    toolBox.jTextFieldXCameraPosition.setText(String.valueOf(location.x));
                    toolBox.jTextFieldYCameraPosition.setText(String.valueOf(location.y));
                    toolBox.jTextFieldZCameraPosition.setText(String.valueOf(location.z));
                }

                @Override
                public void targetChanged(final Vec3F target) {
                    toolBox.jTextFieldXCameraTarget.setText(String.valueOf(target.x));
                    toolBox.jTextFieldYCameraTarget.setText(String.valueOf(target.y));
                    toolBox.jTextFieldZCameraTarget.setText(String.valueOf(target.z));
                    
                }

                @Override
                public void viewMatrixChanged(Mat4F viewMatrix) {

                    FloatBuffer viewMatrixBuffer = Buffers.newDirectFloatBuffer(viewMatrix.mat);
                    
                    for(Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {

                        if(!shader.getValue().isOrtho){
                            gl.glUseProgram(shader.getKey());
                                gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("viewMatrix"), 1, false, viewMatrixBuffer);
                            gl.glUseProgram(0);
                        }
                    }
                }

                @Override
                public void projMatrixChanged(final Mat4F projMatrix) {
                        
                        
                        FloatBuffer projMatrixBuffer = Buffers.newDirectFloatBuffer(projMatrix.mat);

                        for(Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {
                            
                            if(!shader.getValue().isOrtho){
                                String threadName = Thread.currentThread().getName();
                                gl.glUseProgram(shader.getKey());
                                    gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("projMatrix"), 1, false, projMatrixBuffer);
                                gl.glUseProgram(0);
                            }
                        }
                }
            });
            
            voxelSpace = new VoxelSpace(gl, instanceShader.getProgramId(), settings);
            voxelSpace.setAttributToVisualize(settings.attributeToVisualize);

            final JProgressLoadingFile progress = new JProgressLoadingFile(parent);
            progress.setVisible(true);

            voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                @Override
                public void voxelSpaceCreationProgress(int progression){

                    progress.jProgressBar1.setValue(progression);
                }

                
                public void voxelSpaceCreationFinished(){
                    progress.dispose();

                    scene.canDraw = true;
                }
            });
            
            try{
                voxelSpace.loadFromFile(settings.voxelSpaceFile, Format.VOXELSPACE_FORMAT2, settings.mapAttributs);
                //voxelSpace.loadFromFile(settings.voxelSpaceFile, settings.mapAttributs, terrain, false);
            }catch(Exception e){
                logger.error("cannot load voxel space from file", e);
            }
            


            scene.setVoxelSpace(voxelSpace);
        }catch(Exception e){
            logger.error("error in scene initialization", e);
        }
        
    }
    
}
