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
package fr.amap.viewer3d.object.scene;

import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;
import fr.amap.viewer3d.loading.shader.BillboardShader;
import fr.amap.viewer3d.mesh.GLMeshFactory;

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
