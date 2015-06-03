/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.camera;

import com.jogamp.opengl.math.Quaternion;
import fr.ird.voxelidar.engine3d.math.matrix.Mat4F;
import fr.ird.voxelidar.engine3d.math.point.Point3F;
import fr.ird.voxelidar.engine3d.math.quaternion.QuaternionUtility;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class TrackballCamera extends Camera{

    public boolean needToInverse;
    

    public float getAngleX() {
        return angleX;
    }

    public float getAngleY() {
        return angleY;
    }

    public float getAngleZ() {
        return angleZ;
    }
    
    
    public TrackballCamera(){
        
        listeners = new EventListenerList();
        viewMatrix = Mat4F.identity();
        
    }
    
    @Override
    public void init(Vec3F location, Vec3F target, Vec3F up) {
        
        /*
        this.location =location;
        this.target = target;
        */
        this.up = up;
        /*
        viewMatrix = Mat4F.identity();
        viewMatrix = Mat4F.lookAt(location, target, up);
        
        orientation = Vec3F.substract(target, location);
        orientation = Vec3F.normalize(orientation);
        */
        
        
        
        //updateViewMatrix();
        
    }
    
    public void initOrtho(float left, float right, float top, float bottom, float near, float far){
        
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.nearOrtho = near;
        this.farOrtho = far;
    }
    
    @Override
    public void setPerspective(float fovy, float aspect, float near, float far){
        
        this.fovy = fovy;
        this.aspect = aspect;
        this.nearPersp = near;
        this.farPersp = far;
        
        this.isPerspective = true;
        
        updateProjMatrix();
        
    }
    
    @Override
    public void setOrthographic(float left, float right, float top, float bottom, float near, float far){
        
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
        this.nearOrtho = near;
        this.farOrtho = far;
        
        this.isPerspective = false;
        
        updateProjMatrix();
        
    }

    @Override
    public void rotateAroundPoint(Vec3F axis, Vec3F pivot, float angle) {
        
        
        
        if(axis.x != 0){
            
            //this.angleX = angle;
            this.angleX =this.angleX + angle;
            
            
            float r = (float) Math.sqrt(Math.pow(pivot.z - location.z, 2)+Math.pow(pivot.y - location.y, 2));
        
            float z = (float) (pivot.z + r * Math.cos(this.angleX));
            float y = (float) (pivot.y + r * Math.sin(this.angleX));

            location.z = z;
            location.y = y;
            
        }else if(axis.y != 0){
            
            //this.angleY = angle;
            this.angleY =this.angleY + angle;
            
            /*
            if(Math.toDegrees(angleY)>360){
                
                angleY = (float) Math.toRadians(Math.toDegrees(angleY) - 360);
            }else if(Math.toDegrees(angleY)<0){
                angleY = (float) Math.toRadians(360 - Math.toDegrees(-angleY));
            }
            */
            
            
            float r = (float) Math.sqrt(Math.pow(pivot.x - location.x, 2)+Math.pow(pivot.z - location.z, 2));
        
            float x = (float) (pivot.x + r * Math.cos(this.angleY));
            float z = (float) (pivot.z + r * Math.sin(this.angleY));

            location.x = x;
            location.z = z;
            
        }else if(axis.z != 0){
            
            //this.angleZ = angle;
            this.angleZ =this.angleZ + angle;
            
            float r = (float) Math.sqrt(Math.pow(pivot.x - location.x, 2)+Math.pow(pivot.y - location.y, 2));
        
            float x = (float) (pivot.x + r * Math.cos(this.angleZ));
            float y = (float) (pivot.y + r * Math.sin(this.angleZ));

            location.x = x;
            location.y = y;
        }
        
        target = pivot;
        
        updateViewMatrix();
    }
    
    public void rotateFromOrientation(Vec3F axis, Vec3F pivot, float angle){
        
        orientation = Vec3F.substract(target, location);
        float r = Vec3F.length(orientation);
        
        orientation = Vec3F.normalize(orientation);

        Vec3F viewXAxis = new Vec3F(orientation.x, orientation.y, orientation.z);
        Vec3F viewUpAxis = up;
        Vec3F viewYAxis = Vec3F.cross(orientation, viewUpAxis);
        
        if(Vec3F.length(viewYAxis) == 0){
            viewYAxis.x = 1;
        }


        boolean skipY = false;
        
        if(Vec3F.length(viewYAxis) < 0.2f) {

            if(orientation.y < 0 && angle > 0){
                skipY = true;  
            }else if(orientation.y > 0 && angle < 0){
                skipY = true;   
            }
        }

        Vec3F viewZAxis = Vec3F.cross(viewYAxis, viewXAxis);

        viewXAxis = Vec3F.normalize(viewXAxis);
        viewYAxis = Vec3F.normalize(viewYAxis);
        viewZAxis = Vec3F.normalize(viewZAxis);

        float x=0, y=0, z=0;
        

        if(axis.x != 0){

            //équation du cercle:
            //M = C + A* r * cos(t) + B * r *sin(t) avec M = position sur le cercle, C = centre du cercle, A = vecteur du cercle, B = vecteur du cercle orthogonal à A, r = rayon du cercle, t = angle
            //position = Vec3.add(pivot, Vec3.add(Vec3.multiply(viewXAxis, r* (float)Math.cos(angleX)), Vec3.multiply(viewYAxis, r* (float)Math.sin(angleX))));
            angle = -angle;
            float angleSinus = (float)Math.sin(angle);
            float angleCosinus = (float)Math.cos(angle);

            x = pivot.x + (-viewXAxis.x * r * angleCosinus) + (viewYAxis.x * r * angleSinus);
            y = pivot.y + (-viewXAxis.y * r * angleCosinus) + (viewYAxis.y * r * angleSinus);
            z = pivot.z + (-viewXAxis.z * r * angleCosinus) + (viewYAxis.z * r * angleSinus);

        }else if(axis.y != 0){
            //position = Vec3.add(pivot, Vec3.add(Vec3.multiply(viewXAxis, r* (float)Math.cos(angleY)), Vec3.multiply(viewZAxis, r* (float)Math.sin(angleY))));
            if(skipY)
                return;
            
            float angleSinus = (float)Math.sin(angle);
            float angleCosinus = (float)Math.cos(angle);
            
            x = pivot.x + (-viewXAxis.x * r * angleCosinus) + (viewZAxis.x * r * angleSinus);
            y = pivot.y + (-viewXAxis.y * r * angleCosinus) + (viewZAxis.y * r * angleSinus);
            z = pivot.z + (-viewXAxis.z * r * angleCosinus) + (viewZAxis.z * r * angleSinus);

        }else if(axis.z != 0){
            //position = Vec3.add(pivot, Vec3.add(Vec3.multiply(viewXAxis, r* (float)Math.cos(angleZ)), Vec3.multiply(viewYAxis, r* (float)Math.sin(angleZ))));
        }


        location.x = x;    
        location.y = y;
        location.z = z;


        target = pivot;
        
        if(skipY){
            needToInverse = true;
        }

        updateViewMatrix();
        
    }
    
    public void rotateX(Vec3F center, float angle){
        
        rotateAroundPoint(new Vec3F(1.0f, 0.0f, 0.0f), center, angle);
    }
    
    public void rotateY(Vec3F center, float angle){
        
        rotateAroundPoint(new Vec3F(0.0f, 1.0f, 0.0f), center, angle);
    }
    
    public void rotateZ(Vec3F center, float angle){
        
        rotateAroundPoint(new Vec3F(0.0f, 0.0f, 1.0f), center, angle);
    }
    

    /**
     *
     * @param translation translation vector of camera
     */
    @Override
    public void translate(Vec3F translation) {
        
        orientation = Vec3F.substract(target, location);
        orientation = Vec3F.normalize(orientation);
        
        if(translation.x != 0.0f){
            
            Vec3F sideShift = Vec3F.normalize(Vec3F.cross(up, orientation));
            location = Vec3F.add(location, Vec3F.multiply(sideShift, translation.x));
            //target = Vec3F.add(target, Vec3F.multiply(sideShift, translation.x));
        }
        if(translation.y != 0.0f){
            
            Vec3F verticalShift = up;
            location = Vec3F.add(location, Vec3F.multiply(verticalShift, translation.y));
            //target = Vec3F.add(target, Vec3F.multiply(verticalShift, translation.y));
        }
        if(translation.z !=0.0f){
            
            if(isPerspective){
                location = Vec3F.add(location, Vec3F.multiply(orientation, translation.z));
                //target = Vec3F.add(target, Vec3F.multiply(orientation, translation.z)); //use for not reaching the target
                //setPerspective(70.0f, (1.0f*640)/480, near-translation.z, far-translation.z);
            }else{
                
                if((left < -20.0f && right > 20.0f) || translation.z < 0){
                    left = left+translation.z*2;
                    right = right-translation.z*2;
                    bottom = bottom+translation.z*1.5f;
                    top = top-translation.z*1.5f;
                }
                
                updateProjMatrix();
            }
            
        }
        
        updateViewMatrix();
    }

    
    @Override
    public void updateViewMatrix(){
        
        viewMatrix = Mat4F.lookAt(location, target, up);
        /*
        if(needToInverse){
            
            Vec3F backward = Vec3F.multiply(Vec3F.normalize(Vec3F.substract(location, target)), -1);
            
            viewMatrix.mat[2] = -viewMatrix.mat[2];
            viewMatrix.mat[6] = -viewMatrix.mat[6];
            viewMatrix.mat[10] = -viewMatrix.mat[10];
            
            viewMatrix.mat[0] = -viewMatrix.mat[0];
            viewMatrix.mat[4] = -viewMatrix.mat[4];
            viewMatrix.mat[8] = -viewMatrix.mat[8];
            
            needToInverse = false;
        }*/
        
        //System.out.println(location.x+" "+location.y+" "+location.z);
        /*
        Point3F eyePoint = new Point3F(location.x, location.y, location.z);
        Point3F targetPoint = new Point3F(target.x, target.y, target.z);
        
        
        viewMatrix = QuaternionUtility.lookRotation(eyePoint, targetPoint, up);
        */
        notifyViewMatrixChanged();
        
    }
    
    public void notifyViewMatrixChanged(){
        
        fireLocationChanged(location);
        fireTargetChanged(target);
        fireViewMatrixChanged(viewMatrix);
    }
    
    public void updateProjMatrix(){
        
        if(isPerspective){
            
            projectionMatrix = Mat4F.perspective(fovy, aspect, nearPersp, farPersp);
            
        }else{
            
            projectionMatrix = Mat4F.ortho(left, right, bottom, top, nearOrtho, farOrtho);
        }
        
        fireProjectionMatrixChanged(projectionMatrix);
    }

    @Override
    public void addCameraListener(CameraListener listener) {
        listeners.add(CameraListener.class, listener);
    }

    @Override
    public void fireLocationChanged(Vec3F location) {
        
        for(CameraListener listener :listeners.getListeners(CameraListener.class)){
            
            listener.locationChanged(location);
        }
    }

    @Override
    public void fireTargetChanged(Vec3F target) {
        
        for(CameraListener listener :listeners.getListeners(CameraListener.class)){
            
            listener.targetChanged(target);
        }
    }

    @Override
    public void setTarget(Vec3F target) {
        this.target = target;
    }

    @Override
    public void setLocation(Vec3F location) {
        this.location = location;
    }

    @Override
    public void fireViewMatrixChanged(Mat4F viewMatrix) {
        
        for(CameraListener listener :listeners.getListeners(CameraListener.class)){
            
            listener.viewMatrixChanged(viewMatrix);
        }
    }

    @Override
    public void fireProjectionMatrixChanged(Mat4F projMatrix) {
        
        for(CameraListener listener :listeners.getListeners(CameraListener.class)){
            
            try{
                listener.projMatrixChanged(projMatrix);
            }catch(RuntimeException  e){
                listeners.remove(CameraListener.class, listener);
            }
            
        }
    }

    public boolean isIsPerspective() {
        return isPerspective;
    }

    public void setOrthographic(float near, float far) {
        
        this.nearOrtho = near;
        this.farOrtho = far;
        
        this.isPerspective = false;
        
        updateProjMatrix();
    }
    
}
