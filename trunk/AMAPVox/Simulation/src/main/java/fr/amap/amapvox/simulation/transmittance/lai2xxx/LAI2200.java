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

package fr.amap.amapvox.simulation.transmittance.lai2xxx;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author calcul
 */


public class LAI2200 extends LAI2xxx{

    public LAI2200(int shotNumber, ViewCap viewCap){
        
         super(shotNumber, viewCap, new Ring(12.3f, 0),
                                    new Ring(28.6f, 16.7f),
                                    new Ring(43.4f, 32.4f),
                                    new Ring(58.1f, 47.3f),
                                    new Ring(74.1f, 62.3f));
     
        //le lai2200 a 5 plages angulaires
        
       
    }

    @Override
    public void writeOutput(File outputFile) {
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))){
            
            /*****write header*****/
                writer.write("LAI_File\t"+outputFile.getName()+"\n");
                writer.write("Version\t"+"1.0.0"+"\n");

                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
                writer.write("Date\t"+dateFormat.format(Calendar.getInstance().getTime())+"\n");

                computeValues();

                //leaf area index
                writer.write("LAI\t"+LAI+"\n");

                //standard error lai
                writer.write("SEL\t"+Float.NaN+"\n");

                //apparent clumping factor
                writer.write("ACF\t"+acf+"\n");

                //diffuse non-interceptance
                writer.write("DIFN\t"+Float.NaN+"\n");

                //mean tilt angle
                writer.write("MTA\t"+Float.NaN+"\n");

                //standard error MTA
                writer.write("SEM\t"+Float.NaN+"\n");

                //number of pairs of above and below observations that were included in the calculation
                writer.write("SMP\t"+Float.NaN+"\n");
            
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
                avgTransLine += avgTransByRing[i]+"\t";
                acfsLine += acfsByRing[i]+"\t";
                cntcLine += contactNumberByRing[i]+"\t";
                stddevLine += stdevByRing[i]+"\t";
                distsLine += rings[i].getDist()+"\t";
                gapsLine += gapsByRing[i]+"\t";
            }

            String statistics = "MASK"+     "\t"+   maskLine+       "\n"+
                                "ANGLES"+   "\t"+   anglesLine+     "\n"+
                                "AVGTRANS"+ "\t"+   avgTransLine+   "\n"+
                                "ACFS"+     "\t"+   acfsLine+       "\n"+
                                "CNTCT#"+   "\t"+   cntcLine+       "\n"+
                                "STDDEV"+   "\t"+   stddevLine+     "\n"+
                                "DISTS"+    "\t"+   distsLine+      "\n"+
                                "GAPS"+     "\t"+   gapsLine+       "\n";

            writer.write(statistics);
                
            //write sensor information
            writer.write("### Contributing Sensors\n");
            writer.write("Sensor\tW1\tPCH2516\t3978\t1244\t1000\t1004\t1289\n"); //fake values
                
            //write observations
            writer.write("### Data\n");
                 
            
        } catch (IOException ex) {
            //logger.error("Cannot write LAI2200 output file", ex);
        }
    }
    
}
