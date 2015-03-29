/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.renderer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.util.FPSAnimator;
import fr.ird.voxelidar.engine3d.object.mesh.Mesh;
import fr.ird.voxelidar.engine3d.event.BasicEvent;
import fr.ird.voxelidar.engine3d.loading.mesh.MeshFactory;
import fr.ird.voxelidar.engine3d.object.camera.CameraAdapter;
import fr.ird.voxelidar.engine3d.object.camera.TrackballCamera;
import fr.ird.voxelidar.engine3d.object.scene.Scene;
import fr.ird.voxelidar.engine3d.object.scene.SceneObject;
import fr.ird.voxelidar.engine3d.object.scene.Dtm;
import fr.ird.voxelidar.engine3d.object.scene.VoxelSpace;
import fr.ird.voxelidar.engine3d.loading.shader.Shader;
import fr.ird.voxelidar.engine3d.loading.shader.ShaderGenerator;
import fr.ird.voxelidar.engine3d.loading.shader.ShaderGenerator.Flag;
import fr.ird.voxelidar.engine3d.loading.texture.Texture;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4F;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import fr.ird.voxelidar.engine3d.object.scene.SimpleSceneObject;
import fr.ird.voxelidar.util.Settings;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Map.Entry;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 *
 * @author Julien
 */
public class JoglListener implements GLEventListener {
    
    private VoxelSpace voxelSpace;
    private BasicEvent eventListener;
    TrackballCamera camera;
    private Scene scene;
    private Vec3F worldColor;
    public int width;
    public int height;
    private Dtm terrain;
    private Settings settings;
    private boolean isFpsInit = false;
    final static Logger logger = Logger.getLogger(JoglListener.class);
    private boolean justOnce = false;
    private FPSAnimator animator;
    private boolean viewMatrixChanged = false;
    private boolean projectionMatrixChanged = false;
    private boolean isInit;

    
    public Settings getSettings() {
        return settings;
    }

    public Dtm getTerrain() {
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
    
    public JoglListener(VoxelSpace voxelSpace, Settings settings, FPSAnimator animator){
        
        this.voxelSpace = voxelSpace;
        this.settings = settings;
        this.animator = animator;
        worldColor = new Vec3F(200.0f/255.0f, 200.0f/255.0f, 200.0f/255.0f);
    }
    /*
    public JoglListener(JFrameSettingUp parent, Dtm terrain, Settings settings, FPSAnimator animator){
        
        this.terrain = terrain;
        this.settings = settings;
        this.parent = parent;
        worldColor = new Vec3F(200.0f/255.0f, 200.0f/255.0f, 200.0f/255.0f);
        this.animator = animator;
    }
    */
    public void attachEventListener(BasicEvent eventListener){
        
        this.eventListener = eventListener;
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        
        isInit = true;
                
        GL3 gl = drawable.getGL().getGL3();
        
        //calculate fps every 61 frames (wait render speed to stabilize)
        //drawable.getAnimator().setUpdateFPSFrames(61, null);
        
        //this.width = drawable.getWidth();
        //this.height = drawable.getHeight();
        
        Vec3F eye = new Vec3F(22.75f+45.5f, 72.25f, 195.25f-390.5f);
        Vec3F target = new Vec3F(22.75f, 72.25f, 195.25f);
        Vec3F up = new Vec3F(0.0f, 1.0f, 0.0f);
        
        
        
        camera = new TrackballCamera();
        camera.init(eye, target, up);
        
        initScene(gl);
        
        gl.glBlendFunc(GL3.GL_SRC_ALPHA, GL3.GL_ONE_MINUS_SRC_ALPHA );
                
        gl.glEnable(GL3.GL_DEPTH_TEST);
        gl.glDepthFunc(GL3.GL_LEQUAL);
        gl.glClearDepthf(1.0f);
        
        gl.glEnable(GL3.GL_LINE_SMOOTH);
        gl.glEnable(GL3.GL_POLYGON_SMOOTH);
        
        //gl.glPolygonMode(GL3.GL_FRONT, GL3.GL_LINE);
        //gl.glPolygonMode(GL3.GL_BACK, GL3.GL_LINE);
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
    
    public void drawNextFrame(){
        justOnce = true;
        animator.resume();
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        
        GL3 gl=drawable.getGL().getGL3();
        gl.glViewport(0, 0, width, height);
        this.width = width;
        this.height = height;
        
        scene.setWidth(width);
        scene.setHeight(height);
        
        camera.setPerspective(60.0f, (1.0f*width)/height, 0.1f, 1000.0f);
    }
    
    private void render(GLAutoDrawable drawable) {
        
        GL3 gl = drawable.getGL().getGL3();
        
        gl.glViewport(0, 0, width, height);
        gl.glClear(GL3.GL_DEPTH_BUFFER_BIT|GL3.GL_COLOR_BUFFER_BIT);
        gl.glClearColor(worldColor.x, worldColor.y, worldColor.z, 1.0f);
        
        gl.glDisable(GL3.GL_BLEND );
        
        eventListener.updateEvents(); 
        
        if(viewMatrixChanged || isInit){
            
            FloatBuffer viewMatrixBuffer = Buffers.newDirectFloatBuffer(camera.getViewMatrix().mat);
                    
            for(Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {

                if(!shader.getValue().isOrtho){
                    gl.glUseProgram(shader.getKey());
                        gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("viewMatrix"), 1, false, viewMatrixBuffer);
                    gl.glUseProgram(0);
                }
            }
            
            viewMatrixChanged = false;
            
        }
        
        if(projectionMatrixChanged || isInit){
            
            FloatBuffer projMatrixBuffer = Buffers.newDirectFloatBuffer(camera.getProjectionMatrix().mat);

            for(Entry<Integer, Shader> shader : scene.getShadersList().entrySet()) {

                if(!shader.getValue().isOrtho){
                    String threadName = Thread.currentThread().getName();
                    gl.glUseProgram(shader.getKey());
                        gl.glUniformMatrix4fv(shader.getValue().uniformMap.get("projMatrix"), 1, false, projMatrixBuffer);
                    gl.glUseProgram(0);
                }
            }
            
            projectionMatrixChanged = false;
        }
        
        isInit = false;
        
        scene.draw(gl, camera);
        
        if(justOnce){
            animator.pause();
            justOnce = false;
        }
    }
    
    public void loadScene(GL3 gl, File sceneFile){
        
        Document document;
        Element root;
        SAXBuilder sxb =new SAXBuilder();
        
        //avoid loading of dtd file
        sxb.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        
        try {
            document = sxb.build(new FileInputStream(sceneFile));
            root = document.getRootElement();
            Element objects = root.getChild("objects");
            List<Element> objectsElement = objects.getChildren("object");
            
            for(Element object:objectsElement){
                
            }
            
        } catch (FileNotFoundException ex) {
            logger.error(ex);
        } catch (JDOMException | IOException ex) {
            logger.error(ex);
        }
        
    }
    
    private void initScene(final GL3 gl){
        
        scene = new Scene();
        
        
        try{
            //set shaders
            //InputStreamReader simpleVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/SimpleVertexShader.txt"));
            //InputStreamReader simpleFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/SimpleFragmentShader.txt"));
            ShaderGenerator shadGenerator = new ShaderGenerator();
            Shader basicShader = shadGenerator.generateShader(gl, EnumSet.of(Flag.COLORED), "basicShader");
            //Shader basicShader = new Shader(gl, simpleFragmentShader, simpleVertexShader, "basicShader");
            //basicShader.setUniformLocations(new String[]{"viewMatrix","projMatrix"});
            //basicShader.setAttributeLocations(new String[]{"position","color"});
            
            logger.debug("shader compiled: "+basicShader.name);
            /*
            InputStreamReader aoVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/AOVertexShader.txt"));
            InputStreamReader aoFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/AOFragmentShader.txt"));
            Shader aoShader = new Shader(gl, aoFragmentShader, aoVertexShader, "aoShader");
            aoShader.setUniformLocations(new String[]{"viewMatrix","projMatrix"});
            aoShader.setAttributeLocations(new String[]{"position", "instance_position", "instance_color","ambient_occlusion"});
            
            logger.debug("shader compiled: "+basicShader.name);
                */
            
            InputStreamReader noTranslationVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/NoTranslationVertexShader.txt"));
            InputStreamReader noTranslationFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/NoTranslationFragmentShader.txt"));
            Shader noTranslationShader = new Shader(gl, noTranslationFragmentShader, noTranslationVertexShader, "noTranslationShader");
            noTranslationShader.setUniformLocations(new String[]{"viewMatrix","projMatrix"});
            noTranslationShader.setAttributeLocations(new String[]{"position","color"});
            
            logger.debug("shader compiled: "+noTranslationShader.name);
            
            InputStreamReader instanceVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/InstanceVertexShader.txt"));
            InputStreamReader instanceFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/InstanceFragmentShader.txt"));
            //Shader instanceShader = shadGenerator.generateShader(gl, EnumSet.of(Flag.INSTANCED, Flag.TEXTURED), "instanceShader");
            Shader instanceShader = new Shader(gl, instanceFragmentShader, instanceVertexShader, "instanceShader");
            instanceShader.setUniformLocations(new String[]{"viewMatrix","projMatrix"});
            instanceShader.setAttributeLocations(new String[]{"position", "instance_position", "instance_color"});
            
            logger.debug("shader compiled: "+instanceShader.name);
            
            //InputStreamReader textureVertexShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/TextureVertexShader.txt"));
            //InputStreamReader textureFragmentShader = new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("shaders/TextureFragmentShader.txt"));
            Shader texturedShader = shadGenerator.generateShader(gl, EnumSet.of(Flag.TEXTURED), "textureShader");
            //Shader texturedShader = new Shader(gl, textureFragmentShader, textureVertexShader, "textureShader");
            //texturedShader.setUniformLocations(new String[]{"viewMatrix","projMatrix","texture"});
            //texturedShader.setAttributeLocations(new String[]{"position","textureCoordinates"});
            texturedShader.isOrtho = true;

            logger.debug("shader compiled: "+texturedShader.name);
            
            //scene.addShader(aoShader);
            scene.addShader(noTranslationShader);
            scene.addShader(instanceShader);
            scene.addShader(basicShader);
            scene.addShader(texturedShader);
            
            Mesh axisMesh = MeshFactory.createMeshFromObj(
                    new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("mesh/axis.obj")), 
                    new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("mesh/axis.mtl")));
            
            SceneObject axis = new SimpleSceneObject(axisMesh, noTranslationShader.getProgramId(), false);
            axis.setDrawType(GL3.GL_TRIANGLES);
            scene.addObject(axis, gl);
            
            if(settings.drawAxis){
                SceneObject sceneObject = new SimpleSceneObject(MeshFactory.createLandmark(-1000, 1000), basicShader.getProgramId(), false);
                sceneObject.setDrawType(GL3.GL_LINES);
                scene.addObject(sceneObject, gl);
            }
            
            if(settings.drawDtm){
                SceneObject terrainSceneObject = new SimpleSceneObject(MeshFactory.createMesh(terrain.getPoints(), terrain.getIndices()), basicShader.getProgramId(), true);
                terrainSceneObject.setDrawType(GL3.GL_TRIANGLES);
                scene.addObject(terrainSceneObject, gl);
            }
            
            camera.addCameraListener(new CameraAdapter() {
                
                
                @Override
                public void locationChanged(final Vec3F location) {
                    
                    //toolBox.jTextFieldXCameraPosition.setText(String.valueOf(location.x));
                    //toolBox.jTextFieldYCameraPosition.setText(String.valueOf(location.y));
                    //toolBox.jTextFieldZCameraPosition.setText(String.valueOf(location.z));
                }

                @Override
                public void targetChanged(final Vec3F target) {
                    //toolBox.jTextFieldXCameraTarget.setText(String.valueOf(target.x));
                    //toolBox.jTextFieldYCameraTarget.setText(String.valueOf(target.y));
                    //toolBox.jTextFieldZCameraTarget.setText(String.valueOf(target.z));
                    
                }

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
            
            voxelSpace.setSettings(settings);
            voxelSpace.setShaderId(instanceShader.getProgramId());
            
            
            //voxelSpace.attachTexture(Texture.createFromFile(gl, new File("/home/calcul/Documents/Julien/Blends/uv_texture.png")));
            
            //voxelSpace = new VoxelSpace(gl, aoShader.getProgramId(), settings);
            //voxelSpace.setAttributToVisualize(settings.attributeToVisualize);

            //final JProgressLoadingFile progress = new JProgressLoadingFile(parent);
            //progress.setVisible(true);
            /*
            voxelSpace.addVoxelSpaceListener(new VoxelSpaceAdapter() {

                @Override
                public void voxelSpaceCreationProgress(int progression){

                    //progress.jProgressBar1.setValue(progression);
                }

                
                @Override
                public void voxelSpaceCreationFinished(){
                    //progress.dispose();

                    scene.canDraw = true;
                    
                    drawNextFrame();
                }
            });
            */
            /*
            try{
                voxelSpace.loadFromFile(settings.voxelSpaceFile);
                //voxelSpace.loadFromFile(settings.voxelSpaceFile, settings.mapAttributs, terrain, false);
            }catch(Exception e){
                logger.error("cannot load voxel space from file", e);
            }
            */


            scene.setVoxelSpace(voxelSpace);
            
            scene.canDraw = true;
            
        }catch(Exception e){
            logger.error("error in scene initialization", e);
        }
        
    }
    
}
