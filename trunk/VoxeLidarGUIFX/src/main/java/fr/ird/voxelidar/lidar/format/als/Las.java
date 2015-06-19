/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.als;

import java.util.ArrayList;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Las {
    
    private LasHeader header;
    private ArrayList<VariableLengthRecord> variableLengthRecords;
    private ArrayList<? extends PointDataRecordFormat> pointDataRecords;

    public Las(LasHeader header, ArrayList<VariableLengthRecord> variableLengthRecords, ArrayList<? extends PointDataRecordFormat> pointDataRecords) {
        this.header = header;
        this.variableLengthRecords = variableLengthRecords;
        this.pointDataRecords = pointDataRecords;
    }

    public LasHeader getHeader() {
        return header;
    }

    public ArrayList<? extends PointDataRecordFormat> getPointDataRecords() {
        return pointDataRecords;
    }

    public ArrayList<VariableLengthRecord> getVariableLengthRecords() {
        return variableLengthRecords;
    }
    
    public double getTransformedX(int index){
        
        double transformedX = (pointDataRecords.get(index).getX() * header.getxScaleFactor()) + header.getxOffset();
        
        return transformedX;
    }
    
    public double getTransformedY(int index){
        
        double transformedY = (pointDataRecords.get(index).getY() * header.getyScaleFactor()) + header.getyOffset();
        
        return transformedY;
    }
    
    public double getTransformedZ(int index){
        
        double transformedZ = (pointDataRecords.get(index).getZ() * header.getzScaleFactor()) + header.getzOffset();
        
        return transformedZ;
    }
    
    
    
}
