/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.amap.amapvox.io.tls.rsp;

import java.io.File;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RxpScan {
    
    private String fileName;
    private String name;
    private File file;
    private String absolutePath;
    private Matrix4d sopMatrix;
    
    public RxpScan(){
        sopMatrix = new Matrix4d();
        sopMatrix.setIdentity();
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public Matrix4d getSopMatrix() {
        return sopMatrix;
    }

    public void setSopMatrix(Matrix4d sopMatrix) {
        this.sopMatrix = sopMatrix;
    }
    
    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFileName() {
        return fileName;
    }

    public String getName() {
        return name;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }
    
    
    
    
    
}
