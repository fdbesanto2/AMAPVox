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
public class InstanceLightedShader extends Shader{

    
    public InstanceLightedShader(String name){
        
        super(name);
        
        setVertexShaderCode(loadCodeFromInputStream(getStream("shaders/InstanceLightedVertexShader.txt")));
        setFragmentShaderCode(loadCodeFromInputStream(getStream("shaders/InstanceLightedFragmentShader.txt")));
    }
    
}
