/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.io.tls.rsp;

import fr.amap.amapvox.commons.math.matrix.Mat4D;
import java.io.File;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RxpScan {
    
    private String fileName;
    private String name;
    private File file;
    private String absolutePath;
    private Mat4D sopMatrix;
    
    public RxpScan(){
        sopMatrix = Mat4D.identity();
    }
    
    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }
    
    public Mat4D getSopMatrix() {
        return sopMatrix;
    }

    public void setSopMatrix(Mat4D sopMatrix) {
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
