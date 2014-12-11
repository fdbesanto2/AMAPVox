/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.buffer;

import static fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace.FLOAT_SIZE;
import static fr.ird.voxelidar.graphics3d.object.voxelspace.VoxelSpace.SHORT_SIZE;
import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import javax.media.opengl.GL3;

/**
 *
 * @author Julien
 */
public class MeshBuffer {
    
    public final static int VERTEX_BUFFER = 1;
    public final static int COLOR_BUFFER = 2;
    public final static int INDEX_BUFFER = 3;
    public final static int TEXTURE_COORDINATES_BUFFER = 4;
    
    private int bufferId, iboId, vaoId;
    private long totalBuffersSize;
    private ArrayList<SubBuffer> subBuffersList;
    private int indexBuffer;
    
    public void MeshBuffer(){
        
        totalBuffersSize = 0;
        subBuffersList = new ArrayList<>();
    }
    public void setBuffer(int type, Buffer buffer){
        
        SubBuffer subBuffer = null;
        
        switch(type){
            
            case VERTEX_BUFFER:
                subBuffer = new SubBuffer(type, buffer.capacity()*FLOAT_SIZE, totalBuffersSize, buffer);
                totalBuffersSize+=buffer.capacity()*FLOAT_SIZE;
                break;
            case COLOR_BUFFER:
                subBuffer = new SubBuffer(type, buffer.capacity()*FLOAT_SIZE, totalBuffersSize, buffer);
                totalBuffersSize+=buffer.capacity()*FLOAT_SIZE;
                break;
            case INDEX_BUFFER:
                subBuffer = new SubBuffer(type, buffer.capacity()*SHORT_SIZE, totalBuffersSize, buffer);
                totalBuffersSize+=buffer.capacity()*SHORT_SIZE;
                indexBuffer = subBuffersList.size();
                break;
            case TEXTURE_COORDINATES_BUFFER:
                subBuffer = new SubBuffer(type, buffer.capacity()*FLOAT_SIZE, totalBuffersSize, buffer);
                totalBuffersSize+=buffer.capacity()*FLOAT_SIZE;
                break;
        }
        
        subBuffersList.add(subBuffer);
    }
    
    public void initBuffer(GL3 gl){
        
        IntBuffer tmp = IntBuffer.allocate(2);
        gl.glGenBuffers(2, tmp);
        
        bufferId=tmp.get(0);
        iboId=tmp.get(1);
        
        
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, bufferId);
        
            //allocate total memory
            gl.glBufferData(GL3.GL_ARRAY_BUFFER, totalBuffersSize, null, GL3.GL_STATIC_DRAW);
            
            for (SubBuffer subBuffer : subBuffersList) {
                
                if(subBuffer.type != MeshBuffer.INDEX_BUFFER){
                    gl.glBufferSubData(GL3.GL_ARRAY_BUFFER, subBuffer.offset, subBuffer.size, subBuffer.buffer);
                }
            }
            SubBuffer indexSubBuffer = subBuffersList.get(indexBuffer);
            
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, iboId);
                gl.glBufferData(GL3.GL_ELEMENT_ARRAY_BUFFER, indexSubBuffer.size, indexSubBuffer.buffer, GL3.GL_STATIC_DRAW);
            gl.glBindBuffer(GL3.GL_ELEMENT_ARRAY_BUFFER, 0);
        
        gl.glBindBuffer(GL3.GL_ARRAY_BUFFER, 0);
    }
    
    public void updateBuffer(){
        
        
    }
}
