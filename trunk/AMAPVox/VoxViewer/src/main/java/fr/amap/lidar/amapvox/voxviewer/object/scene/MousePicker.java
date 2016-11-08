/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.math.util.MatrixUtility;
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
    
    private Mat4F invertedProjectionMatrix;
    private Mat4F invertedViewMatrix;
    private Camera camera;
    private Vec4F eyeWorldCoords;
    
    private final static boolean DEBUG = false;

    public MousePicker(Camera camera) {
        currentRay = new Vec3F();
        invertedProjectionMatrix = Mat4F.identity();
        invertedViewMatrix = Mat4F.identity();
        this.camera = camera;
    }
    
    public void update(float mouseX, float mouseY, int startX, int startY, float displayWidth, float displayHeight){
        
        currentRay = calculateMouseRay(mouseX, mouseY, startX, startY, displayWidth, displayHeight);
    }
    
    public Vec3F getCurrentRay(){
        return currentRay;
    }
    
    /**
     * For ray picking, the difference between a perspective and orthogonal projection is that for perspective,
     * all rays have the same initial position (the camera position), and their directions depend on the mouse location.
     * In the orthographic case, all rays have the same direction (the camera direction) 
     * but have initial positions dependent on the mouse position.
     * @param distance
     * @return 
     */
    public Point3F getPointOnray(float distance){
        
        
        if(!camera.isPerspective()){
            
            Vec3F start = new Vec3F(eyeWorldCoords.x, eyeWorldCoords.y, eyeWorldCoords.z);
            
            Vec3F point = Vec3F.add(start, camera.getTarget());
            
            Vec3F dir = Vec3F.substract(camera.getTarget(), camera.getLocation());
            dir = Vec3F.normalize(dir);
            
            dir = Vec3F.multiply(dir, distance);
            point = Vec3F.add(point, dir);
            
            return new Point3F(point.x, point.y, point.z);
            
        }else{
            Vec3F start = new Vec3F(camera.getLocation().x, camera.getLocation().y, camera.getLocation().z);
            Vec3F scaledRay = new Vec3F(currentRay.x * distance, currentRay.y * distance, currentRay.z * distance);
            Vec3F point = Vec3F.add(start, scaledRay);
            
            return new Point3F(point.x, point.y, point.z);
        }
        
    }
    
    
    public static Point3F getPointOnray(Point3F camPosition, Vec3F ray, float distance){
        
        Vec3F start = new Vec3F(camPosition.x, camPosition.y, camPosition.z);
        Vec3F scaledRay = new Vec3F(ray.x * distance, ray.y * distance, ray.z * distance);
        Vec3F tmp = Vec3F.add(start, scaledRay);
        
        return new Point3F(tmp.x, tmp.y, tmp.z);
    }
    
    public Vec3F calculateMouseRay(float mouseX, float mouseY, int startX, int startY, float displayWidth, float displayHeight){
        
        Vec2F normalizedCoords = getNormalizedDeviceCoords(mouseX, mouseY, startX, startY, displayWidth, displayHeight);
        
        if(DEBUG){
            System.out.println("Normalized device coord : "+normalizedCoords.x+"\t"+normalizedCoords.y);
        }
        
        
        Vec4F clipCoords = new Vec4F(normalizedCoords.x,  normalizedCoords.y, -1, 1f);
        Vec4F eyeCoords = toEyeCoords(clipCoords);
        Vec3F worldRay = toWorldCoords(eyeCoords);
        return new Vec3F(worldRay.x, worldRay.y, worldRay.z);
    }
    
    public Vec3F toWorldCoords(Vec4F eyeCoords){
           
        Vec4F rayWorld = Mat4F.multiply(invertedViewMatrix, eyeCoords);
        this.eyeWorldCoords = new Vec4F(rayWorld.x, rayWorld.y, rayWorld.z, rayWorld.w);
        rayWorld = Vec4F.normalize(rayWorld);
        Vec3F mouseRay  = new Vec3F(rayWorld.x, rayWorld.y, rayWorld.z);
        mouseRay = Vec3F.normalize(mouseRay);
        
        return mouseRay;
    }
    
    public Vec4F toEyeCoords(Vec4F clipCoords){
        
        Vec4F eyeCoords = Mat4F.multiply(invertedProjectionMatrix, clipCoords);
        return new Vec4F(eyeCoords.x, eyeCoords.y, -1, 0);
    }   
    
    /**
     * Get the normalized device coordinates of the mouse from -1 to 1.
     * The left lower corner of the screen is -1 and the right upper corner of the screen is 1 on both axis.
     * @param mouseX mouse location x in screen coordinates
     * @param mouseY mouse location y in screen coordinates
     * @param startX viewport start x
     * @param startY viewport start y
     * @param displayWidth viewport width
     * @param displayHeight viewport height
     * @return the normalized device coordinates (NDC)
     */
    public Vec2F getNormalizedDeviceCoords(float mouseX, float mouseY, int startX, int startY, float displayWidth, float displayHeight){
        
        float x = (2f*(mouseX - startX)) / displayWidth - 1 ;
        float y = -((2f*(mouseY - startY)) / displayHeight - 1);
        
        return new Vec2F(x, y);
    }

    public void setProjectionMatrix(Mat4F projectionMatrix) {
        this.invertedProjectionMatrix = Mat4F.inverse(Mat4F.transpose(projectionMatrix));
    }

    public void setViewMatrix(Mat4F viewMatrix) {
        this.invertedViewMatrix = Mat4F.inverse(Mat4F.transpose(viewMatrix));
    }

    public Point3F getCamPosition() {
        
        Vec3F location = camera.getLocation();
        
        return new Point3F(location.x, location.y, location.z);
    }

    public Camera getCamera() {
        return camera;
    }   

    public Vec4F getEyeCoords() {
        return eyeWorldCoords;
    }
}
