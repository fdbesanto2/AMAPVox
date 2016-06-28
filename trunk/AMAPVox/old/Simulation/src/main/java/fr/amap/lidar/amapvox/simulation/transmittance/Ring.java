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

package fr.amap.lidar.amapvox.simulation.transmittance;

/**
 *
 * @author calcul
 */


public class Ring {
    
    private float lowerZenithalAngle;
    private float upperZenithalAngle;

    public Ring(float lowerZenithalAngle, float upperZenithalAngle) {
        this.lowerZenithalAngle = lowerZenithalAngle;
        this.upperZenithalAngle = upperZenithalAngle;
    }    
}
