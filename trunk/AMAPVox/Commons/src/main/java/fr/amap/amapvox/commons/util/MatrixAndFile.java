/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.util;

import java.io.File;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class MatrixAndFile{
    
    public File file;
    public Matrix4d matrix;

    public MatrixAndFile(File file, Matrix4d matrix) {
        this.file = file;
        this.matrix = matrix;
    }
    
    @Override
    public String toString(){
        return file.getAbsolutePath();
    }
}
