/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.loading.shader;

import com.jogamp.opengl.GL3;

/**
 *
 * @author calcul
 */
public class InstanceShader extends Shader{
    
    
    public InstanceShader(String name){
        
        super(name);
        
        vertexShaderStreamPath = "shaders/InstanceVertexShader.txt";
        fragmentShaderStreamPath = "shaders/InstanceFragmentShader.txt";
        attributes = new String[] {"position", "instance_position", "instance_color"};
        //uniforms = new String[]{"viewMatrix","projMatrix"};
    }
    
    public InstanceShader(GL3 m_gl, String name){
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
    }
}
