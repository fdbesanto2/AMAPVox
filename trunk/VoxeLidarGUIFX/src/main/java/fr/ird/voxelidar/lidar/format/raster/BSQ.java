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

package fr.ird.voxelidar.lidar.format.raster;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author calcul
 */


public class BSQ extends BRaster{

    public BSQ(File outputFile, BHeader header) {
        super(outputFile, header);
    }

    @Override
    public void writeImage() {
        
        try (DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)))){
            
            for(Band band : bands){
                
                for(int y=0;y<header.getNrows();y++){
                    byte[] bytes = band.getRow(y);
                    writer.write(bytes);
                }
            }
            
        }catch (FileNotFoundException ex) {
            logger.error("File "+outputFile.getAbsolutePath()+" not found", ex);
        } catch (IOException ex) {
            logger.error("An error occured during writing of file "+outputFile.getAbsolutePath(), ex);
        }
    }

    @Override
    public void writeColorFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void writeStatisticsFile() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
