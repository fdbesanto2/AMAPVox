/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.buffer;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;

/**
 *
 * @author Julien
 */
public class MeshBuffer {
    
    public static final int FLOAT_SIZE = Buffers.SIZEOF_FLOAT;
    public static final int SHORT_SIZE = Buffers.SIZEOF_SHORT;
    
    long offset = 0;
    long totalBuffersSize;
    
    /**
     * Vertex Buffer Object identifier
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Vertex_Buffer_Object">https://www.opengl.org/wiki/Vertex_Specification#Vertex_Buffer_Object</a>
     */
    private final int vboId;

    /**
     * Index Buffer Object Identifier
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Index_buffers">https://www.opengl.org/wiki/Vertex_Specification#Index_buffers</a>
     */
    private final int iboId;
    
    private final ArrayList<Long> offsets;
    
    /**
     *
     * @param gl opengl context
     */
    public MeshBuffer(GL3 gl){
        
        totalBuffersSize = 0;
        offset = 0;
        offsets = new ArrayList<>();
        offsets.add(0l);
        
        IntBuffer tmp = IntBuffer.allocate(2);
        gl.glGenBuffers(2, tmp);
        vboId=tmp.get(0);
        iboId=tmp.get(1);
    }
    
    /**
     *
     * @param gl opengl context
     * @param indexBuffer Short buffer containing indices to link vertices, faces, texture coordinates
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Index_buffers">https://www.opengl.org/wiki/Vertex_Specification#Index_buffers</a>
     * @param floatBuffers Float buffer to set to the GPU, can be vertices positions, texture coordinates, color values
     */
    public void initBuffers(GL3 gl, ShortBuffer indexBuffer, FloatBuffer... floatBuffers){
        
        bindBuffer(gl);
        
        for (FloatBuffer buffer : floatBuffers) {
            totalBuffersSize += buffer.capacity()*FLOAT_SIZE;
        }
        
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
        for (FloatBuffer buffer : floatBuffers) {
            addSubBuffer(gl, buffer);
        }
        
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
            gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()*SHORT_SIZE, indexBuffer, GL3.GL_STATIC_DRAW);
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        unbindBuffer(gl);
    }
    
    /**
     *
     * @param gl opengl context
     */
    public void bindBuffer(GL3 gl){
        
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, vboId);
    }
    
    /**
     * Add buffer into current global buffer
     * @param gl opengl context
     * @param buffer buffer to set
     */
    private void addSubBuffer(GL3 gl, FloatBuffer buffer){
        
        long bufferSize = buffer.capacity()*FLOAT_SIZE;
        gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, offset, bufferSize, buffer);
        offset += bufferSize;
        offsets.add(offset);
    }
    
    /**
     * Update buffer linked to the specified index
     * @param gl opengl context
     * @param index subbuffer index
     * @param buffer buffer to update
     */
    public void updateBuffer(GL3 gl, int index, FloatBuffer buffer){
        
        bindBuffer(gl);
            
            gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, offsets.get(index), buffer.capacity()*FLOAT_SIZE, buffer);
        
        unbindBuffer(gl);
    }

    /**
     *
     * @param gl opengl context
     */
    
    public void unbindBuffer(GL3 gl){
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
    }

    /**
     *
     * @return Vertex Buffer Object identifier
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Vertex_Buffer_Object">https://www.opengl.org/wiki/Vertex_Specification#Vertex_Buffer_Object</a>
     */
    public int getVboId() {
        return vboId;
    }

    /**
     *
     * @return Index Buffer Object Identifier
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Index_buffers">https://www.opengl.org/wiki/Vertex_Specification#Index_buffers</a>
     */
    public int getIboId() {
        return iboId;
    }
}
