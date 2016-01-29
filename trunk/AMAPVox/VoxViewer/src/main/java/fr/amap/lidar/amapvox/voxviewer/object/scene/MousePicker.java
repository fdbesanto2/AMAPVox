/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.util.MatrixUtility;
import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec2F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.commons.math.vector.Vec4F;
import fr.amap.lidar.amapvox.voxviewer.object.camera.Camera;
import javax.vecmath.Matrix4f;

/**
 *
 * @author Julien Heurtebize
 */
public class MousePicker {
    
    private Vec3F currentRay;
    
    private Mat4F projectionMatrix;
    private Mat4F viewMatrix;
    private Camera camera;

    public MousePicker(Camera camera) {
        currentRay = new Vec3F();
        projectionMatrix = Mat4F.identity();
        viewMatrix = Mat4F.identity();
        this.camera = camera;
    }
    
    public void update(float mouseX, float mouseY, float displayWidth, float displayHeight){
        currentRay = calculateMouseRay(mouseX, mouseY, displayWidth, displayHeight);
    }
    
    public Vec3F getCurrentRay(){
        return currentRay;
    }
    
    public Point3F getPointOnray(Point3F camPosition, Vec3F ray, float distance){
        
        Vec3F start = new Vec3F(camPosition.x, camPosition.y, camPosition.z);
        Vec3F scaledRay = new Vec3F(ray.x * distance, ray.y * distance, ray.z * distance);
        Vec3F tmp = Vec3F.add(start, scaledRay);
        
        return new Point3F(tmp.x, tmp.y, tmp.z);
    }
    
    public Vec3F calculateMouseRay(float mouseX, float mouseY, float displayWidth, float displayHeight){
        
        Vec2F normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY, displayWidth, displayHeight);
        Vec4F clipCoords = new Vec4F(normalizedCoords.x,  normalizedCoords.y, -1, 1f);
        Vec4F eyeCoords = toEyeCoords(clipCoords);
        Vec3F worldRay = toWorldCoords(eyeCoords);
        return new Vec3F(worldRay.x, worldRay.z, worldRay.y);
    }
    
    public Vec3F toWorldCoords(Vec4F eyeCoords){
        
        Mat4F invertedView = Mat4F.inverse(viewMatrix);        
        Vec4F rayWorld = Mat4F.multiply(invertedView, eyeCoords);
        rayWorld = Vec4F.normalize(rayWorld);
        Vec3F mouseRay  = new Vec3F(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay = Vec3F.normalize(mouseRay);
        return mouseRay;
    }
    
    public Vec4F toEyeCoords(Vec4F clipCoords){
        
        Mat4F invertedProjection = Mat4F.inverse(projectionMatrix);
        Vec4F eyeCoords = Mat4F.multiply(invertedProjection, clipCoords);
        return new Vec4F(eyeCoords.x, eyeCoords.z, eyeCoords.y, 0);
    }   
    
    public Vec2F getNormalizedDeviceCoords(float mouseX, float mouseY, float displayWidth, float displayHeight){
        
        float x = (2f*mouseX) / displayWidth - 1;
        float y = (2f*mouseY) / displayHeight - 1;
        
        return new Vec2F(x, y);
    }

    public void setProjectionMatrix(Mat4F projectionMatrix) {
        this.projectionMatrix = projectionMatrix;
    }

    public void setViewMatrix(Mat4F viewMatrix) {
        this.viewMatrix = viewMatrix;
    }

    public Point3F getCamPosition() {
        
        Vec3F location = camera.getLocation();
        
        return new Point3F(location.x, location.y, location.z);
    }

    public Camera getCamera() {
        return camera;
    }    
}
