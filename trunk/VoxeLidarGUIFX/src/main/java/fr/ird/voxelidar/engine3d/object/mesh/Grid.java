/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.engine3d.object.mesh;

import com.jogamp.opengl.GL3;
import fr.ird.voxelidar.engine3d.mesh.GLMesh;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Grid extends GLMesh{
    
    public Grid(){
        
    }

    @Override
    public void draw(GL3 gl) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void initBuffers(GL3 gl, long maximumTotalBufferSize) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
