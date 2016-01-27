/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.loading.shader;

import com.jogamp.opengl.GL3;

/**
 *
 * @author calcul
 */
public class AxisShader extends Shader{
    
    public AxisShader(String name){
        
        super(name);
        
        vertexShaderStreamPath = "shaders/NoTranslationVertexShader.txt";
        fragmentShaderStreamPath = "shaders/NoTranslationFragmentShader.txt";
        attributes = new String[] {"position", "normal"};
        //uniforms = new String[]{"viewMatrix","projMatrix", "normalMatrix", "Material", "Light", "eye"};
    }
    
    public AxisShader(GL3 m_gl, String name) throws Exception {
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
    }
    
}
