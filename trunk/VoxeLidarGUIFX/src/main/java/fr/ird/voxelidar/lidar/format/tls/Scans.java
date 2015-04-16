/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.tls;

import fr.ird.voxelidar.engine3d.math.matrix.Mat4D;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Scans {
    
    private String name;
    private File rxpLiteFile;
    private File rxpFullFile;
    private String fold;
    private Mat4D sopMatrix;
    private RxpScan scanLite;
    private RxpScan scanFull;
    
    
    public Scans(){
        sopMatrix = Mat4D.identity();
    }

    public RxpScan getScanLite() {
        return scanLite;
    }

    public void setScanLite(RxpScan scanLite) {
        this.scanLite = scanLite;
    }

    public RxpScan getScanFull() {
        return scanFull;
    }

    public void setScanFull(RxpScan scanFull) {
        this.scanFull = scanFull;
    }
    
    private Map<Integer, RxpScan> scanList;

    public void setFold(String fold) {
        this.fold = fold;
    }

    public File getRxpLiteFile() {
        return rxpLiteFile;
    }

    public void setRxpLiteFile(File rxpLiteFile) {
        this.rxpLiteFile = rxpLiteFile;
    }

    public File getRxpFullFile() {
        return rxpFullFile;
    }

    public void setRxpFullFile(File rxpFullFile) {
        this.rxpFullFile = rxpFullFile;
    }
    
    
    public void setSopMatrix(Mat4D sopMatrix) {
        this.sopMatrix = sopMatrix;
    }
    
    public void setName(String name) {
        this.name = name;
    }

    public void setScanList(Map<Integer,RxpScan> scanList) {
        this.scanList = scanList;
    }

    public String getName() {
        return name;
    }

    public String getFold() {
        return fold;
    }

    public Mat4D getSopMatrix() {
        return sopMatrix;
    }

    public Map<Integer, RxpScan> getScanList() {
        return scanList;
    }
    
    
    public Map<Integer, RxpScan> getScanListFiltered(boolean lite) {
        
        Map<Integer, RxpScan> result = new HashMap<>();
        
        for(Entry entry:scanList.entrySet()){
            
            if(scanList.get((int)entry.getKey()).getAbsolutePath().contains(".mon") && lite){
                
                result.put((int)entry.getKey(), scanList.get((int)entry.getKey()));
                
            }else if(!scanList.get((int)entry.getKey()).getAbsolutePath().contains(".mon") && !lite){
                result.put((int)entry.getKey(), scanList.get((int)entry.getKey()));
            }
        }
        
        return result;
    }
    
    
}
