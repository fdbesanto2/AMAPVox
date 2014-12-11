/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.tls;

/**
 *
 * @author Julien
 */
public class Scan {
    
    private String fileName;
    private String name;
    private String absolutePath;

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
