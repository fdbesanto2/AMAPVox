/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.loading.shader;

import com.jogamp.opengl.GL3;
import fr.amap.commons.math.vector.Vec3F;

/**
 *
 * @author calcul
 */
public class SimpleShader extends Shader{
    
    private Uniform3F colorUniform;
    
    public SimpleShader(String name){
        
        super(name);
        
        vertexShaderStreamPath = "shaders/SimpleVertexShader.txt";
        fragmentShaderStreamPath = "shaders/SimpleFragmentShader.txt";
        attributes = new String[] {"position"};
        //uniforms = new String[]{"viewMatrix","projMatrix", "color"};
        
        colorUniform = new Uniform3F("color");
    }
    
    public SimpleShader(GL3 m_gl, String name){
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
    }
    
    public void setColor(Vec3F color){
        colorUniform.setValue(color);
        notifyDirty(colorUniform);
    }
}
