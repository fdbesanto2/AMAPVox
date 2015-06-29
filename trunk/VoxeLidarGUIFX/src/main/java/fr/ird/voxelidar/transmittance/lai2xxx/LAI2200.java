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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author calcul
 */


public class LAI2200 extends LAI2xxx{

    public LAI2200(int shotNumber, ViewCap viewCap){
        
        super(shotNumber, viewCap);
    }
    
    @Override
    protected void initRings() {
        
        //le lai2200 a 5 plages angulaires
        rings = new Ring[5];
        
        rings[0] = new Ring(12.3f, 0);
        rings[1] = new Ring(28.6f, 16.7f);
        rings[2] = new Ring(43.4f, 32.4f);
        rings[3] = new Ring(58.1f, 47.3f);
        rings[4] = new Ring(74.1f, 62.3f);
    }

    @Override
    protected void writeOutput(File outputFile) {
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            
            /*****write header*****/
                writer.write("LAI_File\t"+outputFile.getName()+"\n");
                writer.write("Version\t"+"1.0.0"+"\n");

                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                writer.write("Date\t"+dateFormat.format(Calendar.getInstance().getTime())+"\n");

                float lai = 0.0f;

                //leaf area index
                writer.write("LAI\t"+lai+"\n");

                //standard error lai
                writer.write("SEL\t"+lai+"\n");

                //apparent clumping factor
                writer.write("ACF\t"+lai+"\n");

                //diffuse non-interceptance
                writer.write("DIFN\t"+lai+"\n");

                //mean tilt angle
                writer.write("MTA\t"+lai+"\n");

                //standard error MTA
                writer.write("SEM\t"+lai+"\n");

                //number of pairs of above and below observations that were included in the calculation
                writer.write("SMP\t"+lai+"\n");
            
            /*****write statistics*****/
                
                String maskLine = "",
                       anglesLine = "",
                       avgTransLine = "",
                       acfsLine = "",
                       cntcLine = "",
                       stddevLine = "",
                       distsLine = "",
                       gapsLine = "";
                
                for(int i=0;i<rings.length;i++){
                    
                    maskLine += "1"+"\t";
                    anglesLine += rings[i].getMeanAngle()+"\t";
                    avgTransLine += rings[i].getAvgtrans()+"\t";
                    acfsLine += rings[i].getAcfs()+"\t";
                    cntcLine += rings[i].getCntct()+"\t";
                    stddevLine += rings[i].getStdev()+"\t";
                    distsLine += rings[i].getDist()+"\t";
                    gapsLine += rings[i].getGap()+"\t";
                }
                
                writer.write("MASK\t"+maskLine+"\n");
                writer.write("ANGLES\t"+anglesLine+"\n");
                writer.write("AVGTRANS\t"+avgTransLine+"\n");
                writer.write("ACFS\t"+acfsLine+"\n");
                writer.write("CNTCT#\t"+cntcLine+"\n");
                writer.write("STDDEV\t"+stddevLine+"\n");
                writer.write("DISTS\t"+distsLine+"\n");
                writer.write("GAPS\t"+gapsLine+"\n");
                
            //write sensor information
                writer.write("### Contributing Sensors\n");
                writer.write("Sensor\tW1\tPCH2516\t3978\t1244\t1000\t1004\t1289\n"); //fake values
                
            //write observations
                 writer.write("### Data\n");
                 
            
        } catch (IOException ex) {
            logger.error("Cannot write LAI2200 output file", ex);
        }
    }
    
}