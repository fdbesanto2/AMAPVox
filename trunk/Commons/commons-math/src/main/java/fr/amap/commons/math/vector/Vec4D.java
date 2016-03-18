/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.math.vector;

import fr.amap.commons.math.point.Point4D;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class Vec4D extends Point4D{
        
    public Vec4D(){
        
        this.x = 0;
        this.y = 0;
        this.z = 0;
        this.w = 0;
    }
    
    public Vec4D(double x, double y, double z, double w){
        
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
}
