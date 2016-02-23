/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.loading.shader;

import fr.amap.commons.math.matrix.Mat4F;
import fr.amap.commons.math.vector.Vec3F;

/**
 *
 * @author calcul
 */
public class SimpleShader extends Shader{
    
    private final Uniform3F colorUniform;
    private final UniformMat4F transformationUniform;
    
    public SimpleShader(String name){
        
        super(name);
        
        setVertexShaderCode(loadCodeFromInputStream(getStream("shaders/SimpleVertexShader.txt")));
        setFragmentShaderCode(loadCodeFromInputStream(getStream("shaders/SimpleFragmentShader.txt")));
        
        colorUniform = new Uniform3F("color");
        transformationUniform = new UniformMat4F("transformation");
        transformationUniform.setValue(Mat4F.identity());
        
        notifyDirty(transformationUniform);
    }
    
    public void setColor(Vec3F color){
        colorUniform.setValue(color);
        notifyDirty(colorUniform);
    }
}
