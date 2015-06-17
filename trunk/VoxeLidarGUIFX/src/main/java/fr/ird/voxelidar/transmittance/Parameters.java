/*
This software is distributed WITHOUT ANY WARRANTY and without even the
implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

This program is open-source LGPL 3 (see copying.txt).
Authors:
    Gregoire Vincent    gregoire.vincent@ird.fr
    Julien Heurtebize   julienhtbe@gmail.com
    Jean Dauzat         jean.dauzat@cirad.fr
    Rémi Cresson        cresson.r@gmail.com

For further information, please contact Gregoire Vincent.
 */

package fr.ird.voxelidar.transmittance;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3f;

/**
 *
 * @author calcul
 */


public class Parameters {
    
    private File inputFile;
    private int directionsNumber;
    
    //scanner positions
    private boolean useScanPositionsFile;
    private Point3f centerPoint;
    private float width;
    private float step;
    private File pointsPositionsFile;
    
    private float latitudeRadians;
    
    private List<SimulationPeriod> simulationPeriods;
    private boolean generateTextFile;
    private boolean generateBitmapFile;
    private File textFile;
    private File bitmapFile;

    public Parameters(){
        simulationPeriods = new ArrayList<>();
    }
    
    public File getInputFile() {
        return inputFile;
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public int getDirectionsNumber() {
        return directionsNumber;
    }

    public void setDirectionsNumber(int directionsNumber) {
        this.directionsNumber = directionsNumber;
    }

    public boolean isUseScanPositionsFile() {
        return useScanPositionsFile;
    }

    public void setUseScanPositionsFile(boolean useScanPositionsFile) {
        this.useScanPositionsFile = useScanPositionsFile;
    }

    public Point3f getCenterPoint() {
        return centerPoint;
    }

    public void setCenterPoint(Point3f centerPoint) {
        this.centerPoint = centerPoint;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getStep() {
        return step;
    }

    public void setStep(float step) {
        this.step = step;
    }

    public File getPointsPositionsFile() {
        return pointsPositionsFile;
    }

    public void setPointsPositionsFile(File pointsPositionsFile) {
        this.pointsPositionsFile = pointsPositionsFile;
    }

    public float getLatitudeRadians() {
        return latitudeRadians;
    }

    public void setLatitudeRadians(float latitudeRadians) {
        this.latitudeRadians = latitudeRadians;
    }

    public List<SimulationPeriod> getSimulationPeriods() {
        return simulationPeriods;
    }

    public void setSimulationPeriods(List<SimulationPeriod> simulationPeriods) {
        this.simulationPeriods = simulationPeriods;
    }

    public boolean isGenerateTextFile() {
        return generateTextFile;
    }

    public void setGenerateTextFile(boolean generateTextFile) {
        this.generateTextFile = generateTextFile;
    }

    public boolean isGenerateBitmapFile() {
        return generateBitmapFile;
    }

    public void setGenerateBitmapFile(boolean generateBitmapFile) {
        this.generateBitmapFile = generateBitmapFile;
    }

    public File getTextFile() {
        return textFile;
    }

    public void setTextFile(File textFile) {
        this.textFile = textFile;
    }

    public File getBitmapFile() {
        return bitmapFile;
    }

    public void setBitmapFile(File bitmapFile) {
        this.bitmapFile = bitmapFile;
    }
    
}
