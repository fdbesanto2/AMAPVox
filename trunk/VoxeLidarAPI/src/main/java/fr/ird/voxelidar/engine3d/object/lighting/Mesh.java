/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.lighting;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author calcul
 */
public interface Mesh {
    
    public int vertexCount = 0;
    
    public FloatBuffer vertexBuffer = null;
    public ShortBuffer indexBuffer = null;
    
    public FloatBuffer colorBuffer = null;
    
    
    
    public void attachMaterial(Material material);
}
