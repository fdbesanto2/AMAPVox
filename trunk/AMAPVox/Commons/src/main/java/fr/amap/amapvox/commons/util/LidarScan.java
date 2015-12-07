/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.util;

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
    public List<Filter> filters;

    public LidarScan(File file, Matrix4d matrix) {
        this.file = file;
        this.matrix = matrix;
        filters = new ArrayList<>();
    }
    
    @Override
    public String toString(){
        return file.getAbsolutePath();
    }
}
