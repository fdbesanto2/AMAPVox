/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.loading.shader;

/**
 *
 * @author calcul
 */
public class ColorShader extends Shader{

    public ColorShader() {
        
        super();
        
        setVertexShaderCode(loadCodeFromInputStream(getStream("shaders/ColorVertexShader.txt")));
        setFragmentShaderCode(loadCodeFromInputStream(getStream("shaders/ColorFragmentShader.txt")));
    }
    
}
