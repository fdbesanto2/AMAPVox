/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.structure.octree;

import fr.amap.commons.math.point.Point3D;

/**
 *
 * @author Julien Heurtebize
 */
public class Element {
    
    public Point3D position;

    public Element(Point3D position) {
        this.position = position;
    }    
}
