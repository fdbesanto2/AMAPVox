/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.commons.math.geometry;

import fr.amap.amapvox.commons.math.point.Point3F;

/**
 *
 * @author calcul
 */
public class Rectangle {
    
    private final Point3F point1;
    private final Point3F point2;
    private final Point3F point3;
    private final Point3F point4;

    public Rectangle(Point3F point1, Point3F point2, Point3F point3, Point3F point4) {
        this.point1 = point1;
        this.point2 = point2;
        this.point3 = point3;
        this.point4 = point4;
    }

    public Point3F getPoint1() {
        return point1;
    }

    public Point3F getPoint2() {
        return point2;
    }

    public Point3F getPoint3() {
        return point3;
    }

    public Point3F getPoint4() {
        return point4;
    }
}
