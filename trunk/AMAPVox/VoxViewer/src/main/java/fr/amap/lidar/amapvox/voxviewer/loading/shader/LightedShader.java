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
public class LightedShader extends Shader{

    
    public LightedShader(String name){
        
        super(name);
        
        vertexShaderStreamPath = "shaders/LightVertexShader.txt";
        fragmentShaderStreamPath = "shaders/LightFragmentShader.txt";
        attributes = new String[] {"position", "color", "normal"};
        //uniforms = new String[]{"viewMatrix","projMatrix", "normalMatrix", "Material", "Light", "eyeCoordinates", "lightPosition"};
    }
    
    public LightedShader(GL3 m_gl, String name){
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
    }
}
