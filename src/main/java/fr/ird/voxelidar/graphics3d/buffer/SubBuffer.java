/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.buffer;

import java.nio.Buffer;

/**
 *
 * @author Julien
 */
public class SubBuffer {
    
    public int type;
    public long size;
    public long offset;
    public Buffer buffer;

    public SubBuffer(int type, long size, long offset, Buffer buffer) {
        this.type = type;
        this.size = size;
        this.offset = offset;
        this.buffer = buffer;
    }
    
}
