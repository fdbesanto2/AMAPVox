/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.voxviewer.object.mesh;

import com.jogamp.opengl.GL3;
import fr.amap.lidar.amapvox.voxviewer.mesh.GLMesh;

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
