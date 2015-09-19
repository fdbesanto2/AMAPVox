
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

package fr.amap.amapvox.als;

/**
 *
 * @author calcul
 */



public class LasHeader12 extends LasHeader11 {
    
    private int globalEncoding;

    @Override
    public int getGlobalEncoding() {
        return globalEncoding;
    }

    @Override
    public void setGlobalEncoding(int globalEncoding) {
        this.globalEncoding = globalEncoding;
    }
}
