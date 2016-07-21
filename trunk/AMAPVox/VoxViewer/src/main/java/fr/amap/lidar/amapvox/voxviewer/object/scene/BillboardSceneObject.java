/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.scene;

import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.lidar.amapvox.voxviewer.loading.shader.BillboardShader;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMeshFactory;

/**
 *
 * @author Julien Heurtebize
 */
public class BillboardSceneObject extends SimpleSceneObject2{

    public BillboardSceneObject(Point3F billboardCenter, float billboardSize, Vec3F color) {
        
        super(GLMeshFactory.createPlane(new Vec3F(), billboardSize, billboardSize), false);
        
        this.shader = new BillboardShader();
        ((BillboardShader)this.shader).setBillboardCenter(new Vec3F(billboardCenter.x, billboardCenter.y, billboardCenter.z));
        ((BillboardShader)this.shader).setBillboardSize(billboardSize);
        ((BillboardShader)this.shader).setBillboardColor(color);
    }
}
