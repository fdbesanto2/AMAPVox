/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.voxviewer.object.mesh;

import fr.amap.commons.math.vector.Vec2F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Line {
    
    float m, p;
    
    public static Line createLineFromPoints(Vec2F point1, Vec2F point2){
        
        Line line = new Line();
        
        line.m = (point2.y - point1.y)/(point2.x - point1.x);
        line.p = point1.y - (line.m * point1.x);
        
        return line;
    }
}
