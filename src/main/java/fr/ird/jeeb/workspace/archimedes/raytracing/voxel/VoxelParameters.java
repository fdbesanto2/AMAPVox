/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import java.io.File;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;

/**
 *
 * @author Julien
 */
public class VoxelParameters {
    
    File inputFile;
    Point3f bottomCorner;
    Point3f topCorner;
    Point3i split;

    public VoxelParameters(File inputFile, Point3f bottomCorner, Point3f topCorner, Point3i split) {
        this.inputFile = inputFile;
        this.bottomCorner = bottomCorner;
        this.topCorner = topCorner;
        this.split = split;
    }
    
}
