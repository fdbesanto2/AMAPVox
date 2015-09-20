/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.mesh;

import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class InstancedGLMesh extends GLMesh{
    
    public FloatBuffer instancePositionsBuffer;
    public FloatBuffer instanceColorsBuffer;
    private int instanceNumber;
    
    
    public InstancedGLMesh(int instanceNumber){
        
        super();
        this.instanceNumber = instanceNumber;
    }
    
    public InstancedGLMesh(GL3 gl, GLMesh glMesh , int instanceNumber){
        
        super(gl);
        
        this.vertexBuffer = glMesh.vertexBuffer;
        this.indexBuffer = glMesh.indexBuffer;
        this.colorBuffer = glMesh.colorBuffer;
        this.normalBuffer = glMesh.normalBuffer;
        this.vertexCount = glMesh.vertexCount;
    }

    @Override
    public void draw(GL3 gl) {
        gl.glDrawElementsInstanced(GL3.GL_TRIANGLES, vertexCount, GL3.GL_UNSIGNED_INT, 0, instanceNumber);
    }

    @Override
    public void initBuffers(GL3 gl, long maximumTotalBufferSize) {
        
        if(getVboId() <= 0 || getIboId() <= 0){
            initVBOAndIBO(gl);
        }
        
        
        FloatBuffer[] floatBuffers = new FloatBuffer[]{vertexBuffer};
        
        bindBuffer(gl);
        
        totalBuffersSize = maximumTotalBufferSize;
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
        for (FloatBuffer buffer : floatBuffers) {
            addSubBuffer(gl, buffer);
        }
        
        sendIBOData(gl);
        
        unbindBuffer(gl);
    }

    public int getInstanceNumber() {
        return instanceNumber;
    }

    public void setInstanceNumber(int instanceNumber) {
        this.instanceNumber = instanceNumber;
    }
    
}
