/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.jdart;

import fr.amap.amapvox.commons.math.point.Point3F;
import fr.amap.amapvox.commons.math.point.Point3I;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Dart {
    
    private Point3I sceneDimension;
    private Point3F cellDimension;
    private long cellsNumberByLayer;
    public DartCell[][][] cells;
    
    public Dart() {
        
        this.sceneDimension = new Point3I();
        this.cellDimension = new Point3F();
        this.cellsNumberByLayer = 0;
        this.cells = new DartCell[0][0][0];
    }
    
    public Dart(Point3I sceneDimension, Point3F cellDimension, long cellsNumberByLayer) {
        
        this.sceneDimension = sceneDimension;
        this.cellDimension = cellDimension;
        this.cellsNumberByLayer = cellsNumberByLayer;
        this.cells = new DartCell[sceneDimension.x][sceneDimension.y][sceneDimension.z];
    }

    public Point3I getSceneDimension() {
        return sceneDimension;
    }

    public void setSceneDimension(Point3I sceneDimension) {
        this.sceneDimension = sceneDimension;
    }

    public Point3F getCellDimension() {
        return cellDimension;
    }

    public void setCellDimension(Point3F cellDimension) {
        this.cellDimension = cellDimension;
    }

    public long getCellsNumberByLayer() {
        return cellsNumberByLayer;
    }

    public void setCellsNumberByLayer(long cellsNumberByLayer) {
        this.cellsNumberByLayer = cellsNumberByLayer;
    }
    
}
