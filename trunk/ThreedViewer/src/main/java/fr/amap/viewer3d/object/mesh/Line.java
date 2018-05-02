/*
 * Copyright (C) 2016 UMR AMAP (botAnique et Modélisation de l'Architecture des Plantes et des végétations.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 */
package fr.amap.viewer3d.object.mesh;

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
