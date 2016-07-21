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
public class Uniform1f extends Uniform{

    private float value;
    
    public Uniform1f(String name) {
        super(name);
    }
    
    public Uniform1f(String name, Shader shader) {
        super(name, shader);
    }

    @Override
    public void update(GL3 gl, int location) {
        gl.glUniform1f(location, value);
    }
    
    public void setValue(float value) {
        this.value = value;
        notifyOwners();
    }
    
}
