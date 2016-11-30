/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.amap.viewer3d.object.camera;

import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.vector.Vec3F;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class Camera {
    
    protected final PropertyChangeSupport props = new PropertyChangeSupport(this);
    
    
    protected Mat4F projectionMatrix;
    protected Mat4F viewMatrix;
    protected float fovy = 60.0f;
    protected float aspect;
    protected float nearPersp = 10;
    protected float farPersp = 1000;
    protected float nearOrtho = 0.1f;
    protected float farOrtho = 1000;
    protected float left = Float.NaN;
    protected float right = Float.NaN;
    protected float bottom = Float.NaN;
    protected float top = Float.NaN;
    protected Vec3F location;
    protected Vec3F target;
    protected Vec3F up;
    protected boolean isUpdated;
    protected float phi,theta;
    protected boolean perspective = true;
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
    public abstract Vec3F getTarget();
    public abstract Vec3F getLocation();
    
    public void addPropertyChangeListener(String propName, PropertyChangeListener l) {
        props.addPropertyChangeListener(propName, l);
    }
    
    
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
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(location.x, location.y, location.z));
    }
    public void projectFront(){
        
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(location.x, location.y, location.z-1));
    }
    public void projectBack(){
        project(new Vec3F(location.x, location.y, location.z), new Vec3F(location.x, location.y, location.z+1));
    }
    public void projectIsometric(){
        
    }

    public float getFovy() {
        return fovy;
    }

    public float getNearPersp() {
        return nearPersp;
    }

    public float getNearOrtho() {
        return nearOrtho;
    }

    public float getLeft() {
        return left;
    }

    public float getRight() {
        return right;
    }

    public float getBottom() {
        return bottom;
    }

    public float getTop() {
        return top;
    }

    public Vec3F getUp() {
        return up;
    }

    public float getFarPersp() {
        return farPersp;
    }

    public float getFarOrtho() {
        return farOrtho;
    }

    public float getAspect() {
        return aspect;
    }

    public void setIsPerspective(boolean isPerspective) {
        this.perspective = isPerspective;
    }
    
    public void setRotation(Vec3F axis, float angle){
        
        viewMatrix = Mat4F.setRotation(viewMatrix, axis, angle);
    }
    
    public void updateProjMatrix(){
        
        Mat4F oldValue = projectionMatrix;
        
        if(perspective){
            
            projectionMatrix = Mat4F.perspective(fovy, aspect, nearPersp, farPersp);
            
        }else{
            
            projectionMatrix = Mat4F.ortho(left, right, bottom, top, nearOrtho, farOrtho);
        }
        
        fireProjectionMatrixChanged(projectionMatrix);
        props.firePropertyChange("projMatrix", oldValue, projectionMatrix);
    }

    public boolean isPerspective() {
        return perspective;
    }

    public void setNearPersp(float nearPersp) {
        this.nearPersp = nearPersp;
        updateProjMatrix();
    }

    public void setFarPersp(float farPersp) {
        this.farPersp = farPersp;
        updateProjMatrix();
    }
    
    /**
     *
     * @param translation translation vector of camera
     */
    public abstract void translate(Vec3F translation);
}
