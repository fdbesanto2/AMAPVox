/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.mesh;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author calcul
 */
public class PointCloudGLMesh extends GLMesh{

    @Override
    public void draw(GL3 gl) {
        gl.glDrawArrays(GL3.GL_POINTS, 0, vertexCount);
    }

    @Override
    public void initBuffers(GL3 gl, long maximumTotalBufferSize) {
        
        initVBOAndIBO(gl);
        
        bindBuffer(gl);
        
        List<FloatBuffer> floatBuffers = new ArrayList<>();
        floatBuffers.add(vertexBuffer);
        if(colorBuffer != null){
            floatBuffers.add(colorBuffer);
        }
        
        if(maximumTotalBufferSize == DEFAULT_SIZE){
            for (FloatBuffer buffer : floatBuffers) {
                
                if(buffer != null){
                    totalBuffersSize += buffer.capacity()*FLOAT_SIZE;
                }
                
            }
        }else{
            totalBuffersSize = maximumTotalBufferSize;
        }
        
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
        for (FloatBuffer buffer : floatBuffers) {
            addSubBuffer(gl, buffer);
        }
        
        unbindBuffer(gl);
    }
    
}
