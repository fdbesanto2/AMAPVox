/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.als;

import java.util.ArrayList;

/**
 *
 * @author Julien
 */
public class Las {
    
    private LasHeader header;
    private ArrayList<VariableLengthRecord> variableLengthRecords;
    private ArrayList<? extends PointDataRecordFormat0> pointDataRecords;

    public Las(LasHeader header, ArrayList<VariableLengthRecord> variableLengthRecords, ArrayList<? extends PointDataRecordFormat0> pointDataRecords) {
        this.header = header;
        this.variableLengthRecords = variableLengthRecords;
        this.pointDataRecords = pointDataRecords;
    }

    public LasHeader getHeader() {
        return header;
    }

    public ArrayList<? extends PointDataRecordFormat0> getPointDataRecords() {
        return pointDataRecords;
    }

    public ArrayList<VariableLengthRecord> getVariableLengthRecords() {
        return variableLengthRecords;
    }
    
    
    
}
