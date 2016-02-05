/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.camera;

import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.object.scene.SceneObject;
import javax.swing.event.EventListenerList;
import org.apache.commons.math3.geometry.euclidean.threed.SphericalCoordinates;
import org.apache.commons.math3.geometry.euclidean.threed.Vector3D;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class TrackballCamera extends Camera{
    
    private Vec3F forwardVec;
    private Vec3F rightVec;
    private Vec3F upVec;
    
    private float viewportWidth;
    private float viewportHeight;
    
    private float width;
    private float height;
    
    private boolean inverseY = false;
    
    
    private SceneObject pivot;

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
        location = new Vec3F();
        target = new Vec3F();
        up = new Vec3F(0, 0, 1);
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
        
        this.perspective = true;
        
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
        
        this.perspective = false;
        
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
    
    public void setPivot(SceneObject sceneObject){
        
        this.target = new Vec3F(sceneObject.getGravityCenter().x, sceneObject.getGravityCenter().y, sceneObject.getGravityCenter().z);
        this.pivot = sceneObject;
    }
    
    public void setPivot(Vec3F pivot){
        
        this.target = pivot;
    }
    
    private float normalizeTheta(float theta){
        
        //normalize between 0 and 2pi
        while(theta < 0){
            theta += Math.PI * 2;
        }
        while(theta > (Math.PI * 2)){
            theta -= (Math.PI  * 2);
        } 
        
        return theta;
    }
    
    private float normalizePhi(float phi){
        
        //lock between 0 excluded and pi exluded
        
        while(theta < 0){
            theta += Math.PI * 2;
        }
        while(theta > (Math.PI * 2)){
            theta -= (Math.PI  * 2);
        } 
        
        return theta;
    }
        
    public void rotateFromOrientationV2(Vec3F axis, float offsetX, float offsetY){
        
        
        //get current theta and phi
        SphericalCoordinates sc1 = new SphericalCoordinates(new Vector3D(location.x - target.x, location.y - target.y, location.z - target.z));
        
        float oldTheta = (float) sc1.getTheta();
        float oldPhi = (float) sc1.getPhi();
    
        //theta doit être compris entre 0 et 2pi
        //phi doit être compris entre entre ]0 et pi[
        float theta = 0, phi = 0;
        
        forwardVec = getForwardVector();
        float radius = Vec3F.length(forwardVec);
        
        float thetaStep = (float) Math.toRadians(Math.abs(offsetX));
        float phiStep = (float) Math.toRadians(Math.abs(offsetY/2.0f));
        
        //float thetaStep = (float) ((Math.PI * 2)/360.0f); //1° step
        //float phiStep = (float) ((Math.PI * 2)/360.0f); //1° step
        
        if(offsetX > 0){
            theta -= thetaStep;
        }else if(offsetX < 0){
            theta += thetaStep;
        }
        
        theta += oldTheta;        
        phi += oldPhi;
        
        theta = normalizeTheta(theta);
        
        if(offsetY > 0){
            phi -= phiStep;
            if(phi < 0){
                phi += phiStep;
            }
        }else if(offsetY < 0){
            phi += phiStep;
            if(phi > Math.PI){
                phi -= phiStep;
            }
        }
        
        SphericalCoordinates sc = new SphericalCoordinates(radius, theta, phi);
        Vector3D cartesian = sc.getCartesian();
        
        location.x =  target.x + (float) cartesian.getX();
        location.y =  target.y + (float) cartesian.getY();
        location.z =  target.z + (float) cartesian.getZ();
        
        updateViewMatrix();
    }
    
    public void rotateFromOrientation(Vec3F axis, Vec3F center, float angle){
        
        forwardVec = getForwardVector();
        
        float radius = Vec3F.length(forwardVec);
        
        forwardVec = Vec3F.normalize(forwardVec);
        
        rightVec = Vec3F.cross(forwardVec, up);
        
        if(Vec3F.length(rightVec) == 0){
            rightVec.y = 1;
        }

        upVec = getUpVector();
        
        //forwardVec = Vec3F.normalize(forwardVec);
        rightVec = Vec3F.normalize(rightVec);
        upVec = Vec3F.normalize(upVec);

        if(axis.x != 0){

            //équation du cercle:
            //M = C + A* r * cos(t) + B * r *sin(t) avec M = position sur le cercle, C = centre du cercle, A = vecteur du cercle, B = vecteur du cercle orthogonal à A, r = rayon du cercle, t = angle
            //position = Vec3.add(pivot, Vec3.add(Vec3.multiply(viewXAxis, r* (float)Math.cos(angleX)), Vec3.multiply(viewYAxis, r* (float)Math.sin(angleX))));
            
            //pondération de l'angle par l'inclinaison
            float n = Vec3F.dot(forwardVec, up);
            float d = Vec3F.length(forwardVec)*Vec3F.length(up);

            float tilt = (float) (Math.acos(Math.abs(n/d)));
            /*
            if(tilt == 0){
                tilt = 0.18f;
            }*/

            angle *= (tilt/(Math.PI/2.0d));
        
            angle = -angle;
            float angleSinus = (float)Math.sin(angle);
            float angleCosinus = (float)Math.cos(angle);

            location.x = target.x + (-forwardVec.x * radius * angleCosinus) + (rightVec.x * radius * angleSinus);
            location.y = target.y + (-forwardVec.y * radius * angleCosinus) + (rightVec.y * radius * angleSinus);
            location.z = target.z + (-forwardVec.z * radius * angleCosinus) + (rightVec.z * radius * angleSinus);

        }
        if(axis.y != 0){
                        
            float angleSinus = (float)Math.sin(angle);
            float angleCosinus = (float)Math.cos(angle);
            
            //copy
            Vec3F oldLocation = new Vec3F(location.x, location.y, location.z);
            
            location.x = target.x + (-forwardVec.x * radius * angleCosinus) + (upVec.x * radius * angleSinus);
            location.y = target.y + (-forwardVec.y * radius * angleCosinus) + (upVec.y * radius * angleSinus);
            location.z = target.z + (-forwardVec.z * radius * angleCosinus) + (upVec.z * radius * angleSinus);
            
            Vec3F newForwardVec = getForwardVector();
            newForwardVec = Vec3F.normalize(newForwardVec);
            
            Vec3F newRightVec = Vec3F.cross(newForwardVec, up);
            
            if((newRightVec.y < 0 && rightVec.y> 0)|| (newRightVec.y > 0 && rightVec.y < 0)){
                location = oldLocation;
            }
        }

        //target = pivot;

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
        
        forwardVec = getForwardVector();
        
        forwardVec = Vec3F.normalize(forwardVec);
        
        if(translation.x != 0.0f){
            
            Vec3F sideShift = Vec3F.normalize(Vec3F.cross(up, forwardVec));
            location = Vec3F.add(location, Vec3F.multiply(sideShift, translation.x));
            //target = Vec3F.add(target, Vec3F.multiply(sideShift, translation.x));
        }
        if(translation.y != 0.0f){
            
            Vec3F verticalShift = up;
            location = Vec3F.add(location, Vec3F.multiply(verticalShift, translation.y));
            //target = Vec3F.add(target, Vec3F.multiply(verticalShift, translation.y));
        }
        if(translation.z !=0.0f){
            
            if(perspective){
                
                //copy old location
                Vec3F oldForwardVector = getForwardVector();
                Vec3F oldLocation = location;
                
                //test translation effect
                location = Vec3F.add(location, Vec3F.multiply(forwardVec, translation.z));
                Vec3F newForwardVector = getForwardVector();
                
                //if translation is not good, get back to the original location (equivalent to not move)
                if((newForwardVector.z < 0 && oldForwardVector.z > 0) || (newForwardVector.z > 0 && oldForwardVector.z < 0)){
                    location = oldLocation;
                }
                //target = Vec3F.add(target, Vec3F.multiply(orientation, translation.z)); //use for not reaching the target
                //setPerspective(70.0f, (1.0f*640)/480, near-translation.z, far-translation.z);
            }else{
                
                if((left < -5.0f && right > 5.0f) || translation.z < 0){
                    
                    float widthCoeff = viewportWidth/1000.0f;
                    float heightCoeff = viewportHeight/1000.0f;
                    
                    left = left+translation.z*(widthCoeff);
                    right = right-translation.z*(widthCoeff);
                    bottom = bottom+translation.z*(heightCoeff);
                    top = top-translation.z*(heightCoeff);
                }
                
                
                updateProjMatrix();
            }
            
        }
        
        updateViewMatrix();
    }

    public void translateV2(Vec3F translation){
                
        forwardVec = getForwardVector();
        
        //slow down relatively from the length of the forward vector
        if(perspective){
            translation = Vec3F.multiply(translation, (Vec3F.length(forwardVec)/(float)Math.tan(fovy))*0.001f);
        }else{
            translation = Vec3F.multiply(translation, Vec3F.length(forwardVec)*0.0025f);
        }
        
        forwardVec = Vec3F.normalize(forwardVec);
        
        rightVec = getRightVector();
        upVec = getUpVector();
        
        Vec3F xTranslation = Vec3F.multiply(rightVec, -translation.x);
        Vec3F yTranslation = Vec3F.multiply(upVec, translation.y);
        
        Vec3F translationVec = Vec3F.add(xTranslation, yTranslation);
        
        location = Vec3F.add(translationVec, location);
        target = Vec3F.add(translationVec,  target);
        
        updateViewMatrix();
    }
    
    @Override
    public void updateViewMatrix(){
        
        Mat4F oldValue = viewMatrix;
        forwardVec = getForwardVector();
        viewMatrix = Mat4F.lookAt(location, target, up);
        notifyViewMatrixChanged();
        props.firePropertyChange("viewMatrix", oldValue, viewMatrix);
        
    }
    
    public void notifyViewMatrixChanged(){
        
        fireLocationChanged(location);
        fireTargetChanged(target);
        fireViewMatrixChanged(viewMatrix);
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

    public boolean isPerspective() {
        return perspective;
    }

    public void setOrthographic(float near, float far) {
        
        this.nearOrtho = near;
        this.farOrtho = far;
        
        this.perspective = false;
        
        updateProjMatrix();
    }
    
    private float getTargetDistance(){
        
        Vec3F center = new Vec3F(getPivot().getGravityCenter().x, 
                                getPivot().getGravityCenter().y,
                                getPivot().getGravityCenter().z);
        
        return Vec3F.length(Vec3F.substract(location, center));
    }
    
    public void setViewToBack(){
        
        project(
                        new Vec3F(getPivot().getGravityCenter().x,
                                 getPivot().getGravityCenter().y + getTargetDistance(),
                                 getPivot().getGravityCenter().z),
                        new Vec3F(getPivot().getGravityCenter().x,
                                 getPivot().getGravityCenter().y,
                                 getPivot().getGravityCenter().z));
        
        updateViewMatrix();
    }
    
    public void setViewToFront(){
        
        project(
                        new Vec3F(getPivot().getGravityCenter().x, 
                                getPivot().getGravityCenter().y-getTargetDistance(),
                                getPivot().getGravityCenter().z), 
                
                        new Vec3F(getPivot().getGravityCenter().x, 
                                getPivot().getGravityCenter().y,
                                getPivot().getGravityCenter().z));
        
        updateViewMatrix();
    }
    
    public void setViewToLeft(){
        
        setLocation(new Vec3F(
                getPivot().getGravityCenter().x-getTargetDistance(), 
                getPivot().getGravityCenter().y, 
                getPivot().getGravityCenter().z));
        
        setTarget(new Vec3F(getPivot().getGravityCenter().x, 
                                            getLocation().y,
                                            getLocation().z));
        
        updateViewMatrix();
    }
    
        public void setViewToRight(){
        
        setLocation(new Vec3F(
                getPivot().getGravityCenter().x+getTargetDistance(),
                getPivot().getGravityCenter().y,
                getPivot().getGravityCenter().z));
        
        setTarget(new Vec3F(getPivot().getGravityCenter().x, 
                                                      getLocation().y,
                                                      getLocation().z));
        
        updateViewMatrix();
    }
    
    public void setViewToBottom(){
        
        project(new Vec3F(getPivot().getGravityCenter().x, 
                                    getPivot().getGravityCenter().y,
                                    getPivot().getGravityCenter().z-getTargetDistance()), 
                      new Vec3F(getPivot().getGravityCenter().x, 
                                    getPivot().getGravityCenter().y,
                                    getPivot().getGravityCenter().z));
        
        updateViewMatrix();
    }
    
    public void setViewToTop(){
        
        project(new Vec3F(getPivot().getGravityCenter().x, 
                                                      getPivot().getGravityCenter().y,
                                                      getPivot().getGravityCenter().z+getTargetDistance()), 
                                        new Vec3F(getPivot().getGravityCenter().x, 
                                                      getPivot().getGravityCenter().y,
                                                      getPivot().getGravityCenter().z));
        
        updateViewMatrix();
    }
    
    public void setViewToOrthographic(){
        
        /*float objectDepth = Vec3F.dot(
                Vec3F.substract(
                        new Vec3F(voxelSpace.getCenterX(), voxelSpace.getCenterY(), voxelSpace.getCenterZ()),
                        camera.getLocation()),
                camera.getForwardVector());

        float cameraWidth = (2.0f / camera.getProjectionMatrix().mat[0]) * objectDepth;
        float cameraHeight = (2.0f / camera.getProjectionMatrix().mat[5]) * objectDepth;
            
        float ymax = (float) Math.tan(camera.getFovy() * Math.PI / 360.0f);
        float xmax = ymax * camera.getAspect();
        cameraWidth = objectDepth * xmax;
        cameraHeight = objectDepth * ymax;
        
        camera.setWidth(width);
        camera.setHeight(height);*/
        setOrthographic(getLeft(), getRight(), getTop(), getBottom(), getNearOrtho(), getFarOrtho());
    }
    
    public void setViewToOrthographic(float left, float right, float top, float bottom, float near, float far){
        
        setOrthographic(left, right, top, bottom, near, far);
    }
    
    public void setViewToPerspective(){
        
        setPerspective(getFovy(), getAspect(), getNearPersp(), getFarPersp());
    }
    
    public void setViewToPerspective(float fov, float near, float far){
        
        setPerspective(fov, getAspect(), near, far);
    }
    
    public void switchPerspective(){
        
        if(isPerspective()){
            setViewToOrthographic();
        }else{
            setViewToPerspective();
        }
    }
    
    public Vec3F getForwardVector(){
        return Vec3F.substract(target, location);
    }
    
    public Vec3F getUpVector(){
        
        return Vec3F.cross(rightVec, forwardVec);
    }
    
    public Vec3F getRightVector(){
        
        Vec3F result = Vec3F.cross(forwardVec, up);
        
        if(Vec3F.length(result) == 0){
            result.x = 1;
        }
        
        rightVec = result;
        
        return result;
    }

    @Override
    public Vec3F getTarget() {
        return target;
    }

    @Override
    public Vec3F getLocation() {
        return location;
    }

    public void setViewportWidth(float viewportWidth) {
        this.viewportWidth = viewportWidth;
    }

    public void setViewportHeight(float viewportHeight) {
        this.viewportHeight = viewportHeight;
    } 

    public float getViewportWidth() {
        return viewportWidth;
    }

    public float getViewportHeight() {
        return viewportHeight;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public SceneObject getPivot() {
        return pivot;
    }
}
