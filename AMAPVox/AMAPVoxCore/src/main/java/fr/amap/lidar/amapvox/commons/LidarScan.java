/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.amapvox.commons;

import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.util.filter.FloatFilter;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LidarScan{
    
    public File file;
    public Matrix4d matrix;
    public List<FloatFilter> filters;
    public String name;

    public LidarScan(File file, Matrix4d matrix, String name) {
        this.file = file;
        this.matrix = matrix;
        this.name = name;
        filters = new ArrayList<>();
    }
    
    @Override
    public String toString(){
        return name;
    }

    public String getName() {
        return name;
    }

    public Matrix4d getMatrix() {
        return matrix;
    }
}
