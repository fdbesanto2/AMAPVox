/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.loading.shader;

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
