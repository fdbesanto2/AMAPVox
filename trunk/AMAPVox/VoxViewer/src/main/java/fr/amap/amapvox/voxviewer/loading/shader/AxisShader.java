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
public class AxisShader extends Shader{

    private static final String vertexShaderStreamPath = "shaders/NoTranslationVertexShader.txt";
    private static final String fragmentShaderStreamPath = "shaders/NoTranslationFragmentShader.txt";
    
    private static final String[] attributes = {"position", "normal"};
    private static final String[] uniforms = {"viewMatrix","projMatrix", "normalMatrix", "Material", "Light", "eye"};
    
    
    public AxisShader(GL3 m_gl, String name) throws Exception {
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
    }
    
}
