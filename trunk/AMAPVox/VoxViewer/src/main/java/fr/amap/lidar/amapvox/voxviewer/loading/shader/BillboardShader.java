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
public class BillboardShader extends Shader{
    
    private final Uniform3F billboardCenter;
    private final Uniform1f billboardSize;
    private final Uniform3F billboardColor;
    
    public BillboardShader(){
        
        super();
        
        setVertexShaderCode(loadCodeFromInputStream(getStream("shaders/billboardVertexShader.txt")));
        setFragmentShaderCode(loadCodeFromInputStream(getStream("shaders/SimpleFragmentShader.txt")));
        
        billboardCenter = new Uniform3F("billboardCenter");
        billboardSize = new Uniform1f("billboardSize");
        billboardColor = new Uniform3F("color");
    }
    
    public void setBillboardCenter(Vec3F center){
        billboardCenter.setValue(center);
        //notifyDirty(billboardCenter);
    }
    
    public void setBillboardSize(float size){
        billboardSize.setValue(size);
        //notifyDirty(billboardSize);
    }
    
    public void setBillboardColor(Vec3F color){
        billboardColor.setValue(color);
        notifyDirty(billboardColor);
    }
}
