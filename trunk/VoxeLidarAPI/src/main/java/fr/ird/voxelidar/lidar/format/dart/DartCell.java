/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.lidar.format.dart;

/**
 *
 * @author Julien
 */
public class DartCell {
    
    public static final int CELL_TYPE_EMPTY = 0;
    public static final int CELL_TYPE_OPAQUE_AIR = 1;
    public static final int CELL_TYPE_OPAQUE_GROUND = 2;
    public static final int CELL_TYPE_OPAQUE_WATER = 3;
    public static final int CELL_TYPE_OPAQUE_ROAD = 4;
    public static final int CELL_TYPE_TURBID_CROWN = 6;
    public static final int CELL_TYPE_TURBID_TWIG = 15;
    
    private int type;
    private int nbFigures;
    private int[] figures;
    private int nbTurbids;
    private Turbid[] turbids;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getNbFigures() {
        return nbFigures;
    }

    public void setNbFigures(int nbFigures) {
        
        this.nbFigures = nbFigures;
        figures = new int[nbFigures];
    }

    public int[] getFigureIndex() {
        return figures;
    }

    public void setFigureIndex(int[] figureIndex) {
        this.figures = figureIndex;
    }

    public long getNbTurbids() {
        return nbTurbids;
    }

    public void setNbTurbids(int nbTurbids) {
        
        this.nbTurbids = nbTurbids;
        turbids = new Turbid[nbTurbids];
    }

    public Turbid[] getTurbids() {
        return turbids;
    }

    public void setTurbids(Turbid[] turbids) {
        this.turbids = turbids;
    }    
    
}
