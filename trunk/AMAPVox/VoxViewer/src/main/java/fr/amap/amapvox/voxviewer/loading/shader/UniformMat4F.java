/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.loading.shader;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import fr.amap.amapvox.commons.math.matrix.Mat4F;
import java.nio.FloatBuffer;

/**
 *
 * @author calcul
 */
public class UniformMat4F extends Uniform{

    private FloatBuffer value;
    
    public UniformMat4F(String name) {
        super(name);
    }
    
    public UniformMat4F(String name, Shader shader, int location) {
        super(name, shader, location);
    }

    @Override
    public void update(GL3 gl, int location) {
        
        gl.glUniformMatrix4fv(location, 1, false, value);
    }

    public void setValue(Mat4F value) {
        
        this.value = Buffers.newDirectFloatBuffer(value.mat);
        notifyOwners();
    }
    
}
