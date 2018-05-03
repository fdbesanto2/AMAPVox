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
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Scans {
    
    private String name;
    private File rxpLiteFile;
    private File rxpFullFile;
    private String fold;
    private Matrix4d sopMatrix;
    private RxpScan scanLite;
    private RxpScan scanFull;
    
    
    public Scans(){
        sopMatrix = new Matrix4d();
        sopMatrix.setIdentity();
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

    public void setRxpLiteFile(File rxpLiteFile) {
        this.rxpLiteFile = rxpLiteFile;
    }

    public void setRxpFullFile(File rxpFullFile) {
        this.rxpFullFile = rxpFullFile;
    }
    
    
    public void setSopMatrix(Matrix4d sopMatrix) {
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

    public Matrix4d getSopMatrix() {
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
