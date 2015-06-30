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

package fr.ird.voxelidar.transmittance.lai2xxx;

import static fr.ird.voxelidar.transmittance.lai2xxx.LAI2xxx.logger;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 *
 * @author calcul
 */


public class LAI2000 extends LAI2xxx{
    
    
    public LAI2000(int shotNumber, ViewCap viewCap){
        
        super(shotNumber, viewCap, new Ring(13, 0),
                                    new Ring(28, 16),
                                    new Ring(43, 32),
                                    new Ring(58, 47),
                                    new Ring(74, 61));
        
    }

    @Override
    public void writeOutput(File outputFile) {
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            
            //write header
            //write statistics
            //write sensor information
            //write observations
            
        } catch (IOException ex) {
            logger.error("Cannot write LAI2000 output file", ex);
        }
    }
    
}
