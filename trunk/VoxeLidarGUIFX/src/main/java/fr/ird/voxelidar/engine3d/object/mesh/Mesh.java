/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.mesh;

import com.jogamp.common.nio.Buffers;
import fr.ird.voxelidar.engine3d.math.vector.Vec3F;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Mesh {
    
    public FloatBuffer vertexBuffer;
    public FloatBuffer normalBuffer;
    //public FloatBuffer aoBuffer;
    public ShortBuffer indexBuffer;
    public FloatBuffer colorBuffer;
    //protected MeshBuffer meshBuffer;
    public int vertexCount;
    
    public void setGlobalScale(float scale){
        
        float[] tab = new float[vertexBuffer.capacity()];
        vertexBuffer.get(tab);
        
        for(int i=0;i<tab.length;i++){
            tab[i] *= scale;
        }
        
        vertexBuffer = Buffers.newDirectFloatBuffer(tab);
    }    
    
}
