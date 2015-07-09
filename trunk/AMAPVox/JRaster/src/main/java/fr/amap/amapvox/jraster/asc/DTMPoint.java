
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

package fr.amap.amapvox.jraster.asc;

import fr.amap.amapvox.commons.math.point.Point3F;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */


public class DTMPoint extends Point3F{
    
    /**
     * Référence vers les faces composée de ce point
     */
    public List<Integer> faces;
    
    public DTMPoint(float x, float y, float z) {
        
        super(x, y, z);
        faces = new ArrayList<>();
    }
}
