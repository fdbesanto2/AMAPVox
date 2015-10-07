/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.loading.shader;

import com.jogamp.opengl.GL3;
import fr.amap.amapvox.commons.math.vector.Vec3F;

/**
 *
 * @author calcul
 */
public class Uniform3F extends Uniform{

    private Vec3F value;
    
    public Uniform3F(String name) {
        super(name);
    }

    public Uniform3F(String name, Shader shader, int location) {
        super(name, shader, location);
    }

    @Override
    public void update(GL3 gl, int location) {
        
        gl.glUniform3f(location, value.x, value.y, value.z);
    }

    public void setValue(Vec3F value) {
        this.value = value;
        notifyOwners();
    }
    
}
