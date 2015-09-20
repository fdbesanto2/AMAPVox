/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.als.las;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class PointDataRecordFormat2 extends PointDataRecordFormat{
    
    public static final short LENGTH = 26;
    
    private int red;
    private int green;
    private int blue;

    public void setBlue(int blue) {
        this.blue = blue;
    }

    public void setGreen(int green) {
        this.green = green;
    }

    public void setRed(int red) {
        this.red = red;
    }

    public int getBlue() {
        return blue;
    }

    public int getGreen() {
        return green;
    }

    public int getRed() {
        return red;
    }
    
    
}