package fr.amap.lidar.amapvox.voxviewer;

import com.jogamp.nativewindow.util.Point;
import com.jogamp.newt.event.WindowListener;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.util.FPSAnimator;
import fr.amap.lidar.amapvox.voxviewer.event.BasicEvent;
import fr.amap.lidar.amapvox.voxviewer.event.EventManager;
import fr.amap.lidar.amapvox.voxviewer.input.InputKeyListener;
import fr.amap.lidar.amapvox.voxviewer.input.InputMouseAdapter;
import fr.amap.lidar.amapvox.voxviewer.renderer.GLRenderFrame;
import fr.amap.lidar.amapvox.voxviewer.renderer.MinimalWindowAdapter;
import fr.amap.lidar.amapvox.voxviewer.renderer.JoglListener;
import fr.amap.lidar.amapvox.voxviewer.renderer.MinimalKeyAdapter;
import fr.amap.lidar.amapvox.voxviewer.renderer.MinimalMouseAdapter;
import java.io.File;
import java.util.List;

public class Viewer3D {

    private File voxelFile;
    private String attributeToView;
    static double SCREEN_WIDTH;
    static double SCREEN_HEIGHT;
    private List<String> parameters;
    private final GLRenderFrame renderFrame;
    private final JoglListener joglContext;
    private final FPSAnimator animator;  
    
    private boolean focused;
    
    private final int width;
    private final int height;
    
    private boolean dynamicDraw = false;
    private final MinimalMouseAdapter minimalMouseAdapter;
    private final EventManager minimalEventMgr;
    
    private final BasicEvent basicEvents;
    
    public Viewer3D(int posX, int posY, int width, int height, String title) throws GLException{
        
        GLProfile glp = GLProfile.getMaximum(false);
        GLCapabilities caps = new GLCapabilities(glp);
        caps.setDoubleBuffered(true);

        this.width = width;
        this.height = height;

        renderFrame = new GLRenderFrame(caps, posX, posY, width, height, title);
        //renderFrame = GLRenderFrame.create(caps, posX, posY, width, height, title);
        animator = new FPSAnimator(60);

        /* From doc : 
        An Animator can be attached to one or more GLAutoDrawables to drive their display() methods in a loop.
        The Animator class creates a background thread in which the calls to display() are performed.
        After each drawable has been redrawn, a brief pause is performed to avoid swamping the CPU,
        unless setRunAsFastAsPossible(boolean) has been called.
        */
        animator.add(renderFrame);

        joglContext = new JoglListener(animator);
        joglContext.getScene().setWidth(width);
        joglContext.getScene().setHeight(height);

        renderFrame.addGLEventListener(joglContext);
        renderFrame.addWindowListener(new MinimalWindowAdapter(animator));


        //basic input adapters for waking up animator if necessary
        MinimalKeyAdapter minimalKeyAdapter = new MinimalKeyAdapter(animator);
        renderFrame.addKeyListener(minimalKeyAdapter);

        minimalMouseAdapter = new MinimalMouseAdapter(animator, dynamicDraw);
        renderFrame.addMouseListener(minimalMouseAdapter);

        InputKeyListener inputKeyListener = new InputKeyListener();
        InputMouseAdapter inputMouseAdapter = new InputMouseAdapter();
        
        minimalEventMgr = new EventManager(inputMouseAdapter, inputKeyListener) {
            @Override
            public void updateEvents() {
                
                if(!animator.isPaused() && !joglContext.isDynamicDraw() &&
                        !mouse.isButtonDown(InputMouseAdapter.Button.LEFT) && !mouse.isButtonDown(InputMouseAdapter.Button.RIGHT)){

                    //this function cost time, it should not be called at each updateEvents method call
                    animator.pause();
                }
            }
        };
        
        addEventListener(minimalEventMgr);
        
        basicEvents = new BasicEvent(joglContext, inputMouseAdapter, inputKeyListener);
        addEventListener(basicEvents);
    }
    
    public final void addEventListener(EventManager eventManager){
        
        renderFrame.addKeyListener(eventManager.getKeyboard());
        renderFrame.addMouseListener(eventManager.getMouse());
        joglContext.addEventListener(eventManager);
    }
    
    /**
     * Remove the default action listener who is handling mouse and keyboard events.
     * You can call this method if you want to custom events.
     */
    public void removeDefaultEventManager(){
        renderFrame.removeKeyListener(basicEvents.getKeyboard());
        renderFrame.removeMouseListener(basicEvents.getMouse());
        joglContext.removeEventListener(basicEvents);
    }
    
    public fr.amap.lidar.amapvox.voxviewer.object.scene.Scene getScene(){
        return getJoglContext().getScene();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public GLRenderFrame getRenderFrame() {
        return renderFrame;
    }
    
    
    public Point getPosition(){
        Point locationOnScreen = renderFrame.getLocationOnScreen(null);
        return new Point(locationOnScreen.getX(), locationOnScreen.getY());
    }
    
    public void show(){
        this.setOnTop();
        animator.start();
        renderFrame.setVisible(true);
    }
    
    public void setOnTop(){
        renderFrame.setAlwaysOnTop(true);
        renderFrame.setAlwaysOnTop(false);
    }

    public JoglListener getJoglContext() {
        return joglContext;
    }

    public FPSAnimator getAnimator() {
        return animator;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setIsFocused(boolean focused) {
        this.focused = focused;
    }

    public boolean isDynamicDraw() {
        return dynamicDraw;
    }

    public void setDynamicDraw(boolean dynamicDraw) {
        this.dynamicDraw = dynamicDraw;
        joglContext.setDynamicDraw(dynamicDraw);
        minimalMouseAdapter.setDynamicDraw(dynamicDraw);
    }

}
