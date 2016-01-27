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
public class TextureShader extends Shader{
    
    public TextureShader(String name){
        
        super(name);
        
        vertexShaderStreamPath = "shaders/billboardVertexShader.txt";
        fragmentShaderStreamPath = "shaders/TextureFragmentShader.txt";
        attributes = new String[] {"position", "textureCoordinates"};
        //uniforms = new String[]{"viewMatrix","projMatrix", "texture", "eye"};
    }
    
    public TextureShader(GL3 m_gl, String name) throws Exception {
        
        super(m_gl, name);
        
        load(vertexShaderStreamPath, fragmentShaderStreamPath);
        setAttributeLocations(attributes);
        setUniformLocations(uniforms);
    }
}
