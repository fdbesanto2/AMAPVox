/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.object.camera;

import fr.ird.voxelidar.math.matrix.Mat4F;
import fr.ird.voxelidar.math.vector.Vec3F;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien
 */
public abstract class Camera {
    
    protected Mat4F projectionMatrix;
    protected Mat4F viewMatrix;
    protected float fovy;
    protected float aspect;
    protected float nearPersp;
    protected float farPersp;
    protected float nearOrtho;
    protected float farOrtho;
    protected float left;
    protected float right;
    protected float bottom;
    protected float top;
    public Vec3F location;
    public Vec3F target;
    protected Vec3F up;
    public Vec3F orientation;
    protected boolean isUpdated;
    protected float phi,theta;
    protected boolean isPerspective;
    protected float angleX = 0.0f;
    protected float angleY = 0.0f;
    protected float angleZ = 0.0f;
    protected EventListenerList listeners;
    public boolean isInit = false;
    
    //set lookat matrix (position, lookat)
    public abstract void init(Vec3F location, Vec3F target, Vec3F up);
    public abstract void setPerspective(float fovy, float aspect, float near, float far);
    public abstract void setOrthographic(float left, float right, float top, float bottom, float near, float far);
    public abstract void rotateAroundPoint(Vec3F axis, Vec3F pivot, float angle);
    public abstract void updateViewMatrix();
    public abstract void addCameraListener(CameraListener listener);
    public abstract void fireLocationChanged(Vec3F location);
    public abstract void fireTargetChanged(Vec3F target);
    public abstract void fireViewMatrixChanged(Mat4F viewMatrix);
    public abstract void fireProjectionMatrixChanged(Mat4F projMatrix);
    public abstract void setTarget(Vec3F target);
    public abstract void setLocation(Vec3F location);
    
    public void project(Vec3F location, Vec3F target){
        
        this.location.x = location.x;
        this.location.y = location.y;
        this.location.z = location.z;
        
        this.target.x = target.x;
        this.target.y = target.y;
        this.target.z = target.z;
        
        updateViewMatrix();
    }

    public Mat4F getProjectionMatrix() {
        return projectionMatrix;
    }

    public Mat4F getViewMatrix() {
        return viewMatrix;
    }
    
    public void projectTop(){
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(location.x, location.y-1, location.z));
    }
    public void projectBottom(){
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(location.x, location.y+1, location.z));
    }
    public void projectLeft(){
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(-1, 0, 0));
    }
    public void projectRight(){
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(1, 0, 0));
    }
    public void projectFront(){
        
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(location.x, location.y, location.z-1));
    }
    public void projectBack(){
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(location.x, location.y, location.z+1));
    }
    public void projectIsometric(){
        
    }
    
    /**
     *
     * @param translation translation vector of camera
     */
    public abstract void translate(Vec3F translation);
}
