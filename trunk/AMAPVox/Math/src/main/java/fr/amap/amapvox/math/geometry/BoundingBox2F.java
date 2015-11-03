
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

package fr.amap.amapvox.math.geometry;

import fr.amap.amapvox.math.point.Point2F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class BoundingBox2F {
    
    public Point2F min;
    public Point2F max;
    
    public BoundingBox2F(){
        min = new Point2F();
        max = new Point2F();
    }

    public BoundingBox2F(Point2F min, Point2F max) {
        this.min = min;
        this.max = max;
    }
    
    
}
