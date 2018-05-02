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
package fr.amap.viewer3d.loading.shader;

import fr.amap.commons.math.vector.Vec3F;

/**
 *
 * @author Julien Heurtebize
 */
public class BillboardPCLShader extends Shader{
    
    private final Uniform3F billboardCenter;
    private final Uniform1f billboardSize;
    
    public BillboardPCLShader(){
        
        super();
        
        setVertexShaderCode(loadCodeFromInputStream(getStream("shaders/instanceBillboardVertexShader.txt")));
        setFragmentShaderCode(loadCodeFromInputStream(getStream("shaders/InstanceFragmentShader.txt")));
        
        billboardCenter = new Uniform3F("billboardCenter");
        billboardSize = new Uniform1f("billboardSize");
    }
    
    public void setBillboardCenter(Vec3F center){
        billboardCenter.setValue(center);
        //notifyDirty(billboardCenter);
    }
    
    public void setBillboardSize(float size){
        billboardSize.setValue(size);
        //notifyDirty(billboardSize);
    }
}
