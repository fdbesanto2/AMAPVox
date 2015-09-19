/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.mesh;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL3;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public abstract class GLMesh {
    
    public static final int FLOAT_SIZE = Buffers.SIZEOF_FLOAT;
    public static final int INTEGER_SIZE = Buffers.SIZEOF_INT;
    //public static final int SHORT_SIZE = Buffers.SIZEOF_SHORT;
    public static final int DEFAULT_SIZE = -1;
    
    long offset = 0;
    long totalBuffersSize;
    public int drawType = GL3.GL_TRIANGLES;
    
    public FloatBuffer vertexBuffer;
    public FloatBuffer normalBuffer;
    public IntBuffer indexBuffer;
    public FloatBuffer colorBuffer;
    public int vertexCount;
    public int dimensions = 3;
    
    
    /**
     * Vertex Buffer Object identifier
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Vertex_Buffer_Object">https://www.opengl.org/wiki/Vertex_Specification#Vertex_Buffer_Object</a>
     */
    private int vboId;

    /**
     * Index Buffer Object Identifier
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Index_buffers">https://www.opengl.org/wiki/Vertex_Specification#Index_buffers</a>
     */
    private int iboId;
    
    protected List<Long> offsets;
    protected List<Long> buffersSizes;
    
    public GLMesh(){
        
        totalBuffersSize = 0;
        offset = 0;
        offsets = new ArrayList<>();
        buffersSizes = new ArrayList<>();
    }
    
    /**
     *
     * @param gl opengl context
     */
    public GLMesh(GL3 gl){
        
        totalBuffersSize = 0;
        offset = 0;
        offsets = new ArrayList<>();
        buffersSizes = new ArrayList<>();
        
        //offsets.add(0l);
        
        IntBuffer tmp = IntBuffer.allocate(2);
        gl.glGenBuffers(2, tmp);
        
        vboId=tmp.get(0);
        iboId=tmp.get(1);
    }
    
    /**
     *
     * @param gl opengl context
     * @param maxSize size to reserve to the gpu
     * @param indexBuffer Short buffer containing indices to link vertices, faces, texture coordinates
     * @see <a href="https://www.opengl.org/wiki/Vertex_Specification#Index_buffers">https://www.opengl.org/wiki/Vertex_Specification#Index_buffers</a>
     * @param floatBuffers Float buffer to set to the GPU, can be vertices positions, texture coordinates, color values
     */
    public abstract void draw(GL3 gl);
    public abstract void initBuffers(GL3 gl, long maximumTotalBufferSize);
    /*
    public void initBuffers(GL3 gl, int maxSize, ShortBuffer indexBuffer, FloatBuffer... floatBuffers){
        
        bindBuffer(gl);
        
        if(maxSize == DEFAULT_SIZE){
            for (FloatBuffer buffer : floatBuffers) {
                totalBuffersSize += buffer.capacity()*FLOAT_SIZE;
            }
        }else{
            totalBuffersSize = maxSize;
        }
        
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
        for (FloatBuffer buffer : floatBuffers) {
            addSubBuffer(gl, buffer);
        }
        
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
            gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()*SHORT_SIZE, indexBuffer, GL3.GL_STATIC_DRAW);
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        unbindBuffer(gl);
    }*/
    /*
    public void initBuffersV2(GL3 gl, int maxSize, ShortBuffer indexBuffer, FloatBuffer... floatBuffers){
        
        bindBuffer(gl);
        
        totalBuffersSize = maxSize;
        gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
        for (FloatBuffer buffer : floatBuffers) {
            addSubBuffer(gl, buffer);
        }
        
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
            gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()*SHORT_SIZE, indexBuffer, GL3.GL_STATIC_DRAW);
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        unbindBuffer(gl);
    }*/
    
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
    protected void addSubBuffer(GL3 gl, FloatBuffer buffer){
        
        long bufferSize = buffer.capacity()*FLOAT_SIZE;
        gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, offset, bufferSize, buffer);
        offsets.add(offset);
        offset += bufferSize;
        buffersSizes.add(bufferSize);
    }
    
    /**
     * Update buffer linked to the specified index
     * @param gl opengl context
     * @param index subbuffer index
     * @param buffer buffer to update
     */
    public void updateBuffer(GL3 gl, int index, FloatBuffer buffer){
        
        bindBuffer(gl);
            
            long bufferSize = buffer.capacity()*FLOAT_SIZE;
            
            if(index >= buffersSizes.size()){
                buffersSizes.add(bufferSize);
            }
            
            if(index >= offsets.size()){
                offsets.add(computeOffset(index));
            }
            
            long difference = bufferSize-buffersSizes.get(index);
            
            totalBuffersSize += difference;
            //gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
        
            gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, offsets.get(index), bufferSize, buffer);
            
            long oldBufferSize = buffersSizes.get(index);
            
            if(bufferSize != oldBufferSize){
                
                buffersSizes.set(index, bufferSize);
                
                if((index+1) < offsets.size()){
                    
                    offsets.set(index+1, computeOffset(index+1));
                }
            }
        
        unbindBuffer(gl);
    }
    
    private long computeOffset(int index){
        
        long offsetTot = 0;
        
        if(index <= buffersSizes.size()){
            
            for(int i=0;i<index;i++) {
                offsetTot+=buffersSizes.get(i);
            }
        }
        
        return offsetTot;
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
    
    
    protected void initVBOAndIBO(GL3 gl){
        
        IntBuffer tmp = IntBuffer.allocate(2);
        gl.glGenBuffers(2, tmp);
        vboId=tmp.get(0);
        iboId=tmp.get(1);
    }
    
    protected void sendIBOData(GL3 gl){
        
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
            gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexBuffer.capacity()*INTEGER_SIZE, indexBuffer, GL3.GL_STATIC_DRAW);
        gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
    }
    
    public void setGlobalScale(float scale){
        
        if(vertexBuffer != null){
            
            float[] tab = new float[vertexBuffer.capacity()];
            vertexBuffer.get(tab);

            for(int i=0;i<tab.length;i++){
                tab[i] *= scale;
            }

            vertexBuffer = Buffers.newDirectFloatBuffer(tab);
        }
    }   
}
