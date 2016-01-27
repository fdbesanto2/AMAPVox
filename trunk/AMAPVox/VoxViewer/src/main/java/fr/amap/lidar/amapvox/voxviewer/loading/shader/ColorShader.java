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

    public ColorShader(String name) {
        
        super(name);
        
        vertexShaderStreamPath = "shaders/ColorVertexShader.txt";
        fragmentShaderStreamPath = "shaders/ColorFragmentShader.txt";
        attributes = new String[] {"position","color"};
    }
    
}
