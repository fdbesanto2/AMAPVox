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

package fr.amap.lidar.amapvox.voxviewer.object.lighting;

import fr.amap.commons.math.point.Point3F;
import fr.amap.commons.math.vector.Vec3F;


/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class Light {
    
    public Point3F position;
    public Vec3F ambient;
    public Vec3F diffuse;
    public Vec3F specular;
    
    public Light(){
        
        ambient = new Vec3F(1.0f, 1.0f, 1.0f);
        diffuse = new Vec3F(1.0f, 1.0f, 1.0f);
        specular = new Vec3F(1.0f, 1.0f, 1.0f);
        position = new Point3F(0, 0, 100);
    }
}
