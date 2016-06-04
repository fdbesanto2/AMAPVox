/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.loading.shader;

/**
 * 
 * @author Julien Heurtebize
 */
public class TextureShaderProj extends Shader{
    
    public TextureShaderProj(){
        
        super();
        
        setVertexShaderCode(loadCodeFromInputStream(getStream("shaders/TextureVertexShader.txt")));
        setFragmentShaderCode(loadCodeFromInputStream(getStream("shaders/TextureFragmentShader.txt")));
    }
}
