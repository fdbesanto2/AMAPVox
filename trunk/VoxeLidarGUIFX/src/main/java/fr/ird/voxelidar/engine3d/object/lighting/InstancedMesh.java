/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.lighting;

import java.nio.FloatBuffer;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public interface InstancedMesh {
    
    public FloatBuffer instancePositionsBuffer = null;
    public FloatBuffer instanceColorsBuffer= null;
    public int instanceNumber = 0;
    
    
}
