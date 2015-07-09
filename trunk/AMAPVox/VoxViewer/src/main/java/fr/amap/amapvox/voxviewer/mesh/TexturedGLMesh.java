/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.mesh;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class TexturedGLMesh extends GLMesh{
    
    public FloatBuffer textureCoordinatesBuffer;

    public TexturedGLMesh() {
    }
    
    public TexturedGLMesh(GL3 gl) {
        super(gl);
    }

    @Override
    public void initBuffers(GL3 gl, long maximumTotalBufferSize) {
        
        if(textureCoordinatesBuffer != null){
            
            initVBOAndIBO(gl);

            bindBuffer(gl);

            FloatBuffer[] floatBuffers = new FloatBuffer[]{vertexBuffer, textureCoordinatesBuffer};

            if(maximumTotalBufferSize == DEFAULT_SIZE){
                for (FloatBuffer buffer : floatBuffers) {
                    totalBuffersSize += buffer.capacity()*FLOAT_SIZE;
                }
            }else{
                totalBuffersSize = maximumTotalBufferSize;
            }

            gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);

            for (FloatBuffer buffer : floatBuffers) {
                addSubBuffer(gl, buffer);
            }

            sendIBOData(gl);

            unbindBuffer(gl);
        }
    }

    @Override
    public void draw(GL3 gl) {
                
        gl.glDrawElements(GL3.GL_TRIANGLES, vertexCount, GL3.GL_UNSIGNED_INT, 0);
    }
}
