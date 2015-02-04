/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.mesh;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 *
 * @author Julien
 */
public class Mesh {
    
    public FloatBuffer vertexBuffer;
    //public FloatBuffer aoBuffer;
    public ShortBuffer indexBuffer;
    public FloatBuffer colorBuffer;
    public FloatBuffer textureCoordinatesBuffer;
    //protected MeshBuffer meshBuffer;
    public int vertexCount;
    
    
}
