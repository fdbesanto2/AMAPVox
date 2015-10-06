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
public class Uniform1I extends Uniform{

    private int value;
    
    public Uniform1I(String name) {
        super(name);
    }
    
    public Uniform1I(String name, Shader shader, int location) {
        super(name, shader, location);
    }

    @Override
    public void update(GL3 gl, int location) {
        gl.glUniform1i(location, value);
    }
    
    public void setValue(int value) {
        this.value = value;
        notifyOwners();
    }
    
}
