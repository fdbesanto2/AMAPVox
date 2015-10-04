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
public class LightedShader extends Shader{
    
    private static final String vertexShaderStreamPath = "shaders/LightVertexShader.txt";
    private static final String fragmentShaderStreamPath = "shaders/LightFragmentShader.txt";
    
    private static final String[] attributes = {"position", "color", "normal"};
    private static final String[] uniforms = {"viewMatrix","projMatrix", "normalMatrix", "Material", "Light", "eyeCoordinates", "lightPosition"};
    
    public LightedShader(GL3 m_gl, String name){
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
    }
}
