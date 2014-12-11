/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.graphics3d.mesh;

/**
 *
 * @author Julien
 */
public class Face {
    
    private int point1, point2, point3;

    public int getPoint1() {
        return point1;
    }

    public int getPoint2() {
        return point2;
    }

    public int getPoint3() {
        return point3;
    }
    
    
    public Face(int point1, int point2, int point3){
        
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
    }
}
