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
public class InstanceLightedShader extends Shader{

    private static final String vertexShaderStreamPath = "shaders/InstanceLightedVertexShader.txt";
    private static final String fragmentShaderStreamPath = "shaders/InstanceLightedFragmentShader.txt";
    
    private static final String[] attributes = {"position", "instance_position", "instance_color"};
    private static final String[] uniforms = {"viewMatrix","projMatrix", "lightPosition", "lambient", "ldiffuse", "lspecular"};
    
    public InstanceLightedShader(GL3 m_gl, String name) throws Exception {
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
        
    }
    
}
