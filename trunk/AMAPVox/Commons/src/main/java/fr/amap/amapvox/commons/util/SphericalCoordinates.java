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

package fr.amap.amapvox.commons.util;

import javax.vecmath.Vector3f;

/**
 *
 * @author calcul
 */


public class SphericalCoordinates {
    
    private float azimuth;
    private float elevation;

    public SphericalCoordinates(float azimuth, float elevation) {
        this.azimuth = azimuth;
        this.elevation = elevation;
    }

    public float getAzimuth() {
        return azimuth;
    }

    public float getElevation() {
        return elevation;
    }
    
    public Vector3f toCartesian(){
        
        float radius = 1;
        
        return new Vector3f(radius * (float)Math.sin(elevation) * (float)Math.cos(azimuth), 
                            radius * (float)Math.sin(elevation) * (float)Math.sin(azimuth), 
                            radius * (float)Math.cos((double)elevation));
    }
}
