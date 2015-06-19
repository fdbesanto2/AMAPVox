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

import fr.ird.voxelidar.lidar.format.raster.BCommon.NumberOfBits;
import static fr.ird.voxelidar.lidar.format.raster.BCommon.getBooleanBitsArray;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author calcul
 */


public abstract class BRaster {
    
    protected final static Logger logger = Logger.getLogger(BRaster.class);
    
    protected BHeader header;
    protected File outputFile;
    protected List<Band> bands;
    
    public abstract void writeImage();
    
    public BRaster(File outputFile, BHeader header){
        bands = new ArrayList<>();
        this.header = header;
        this.outputFile = outputFile;
        
        if(!outputFile.getName().endsWith(".bsq")){
            this.outputFile = new File(outputFile.getAbsolutePath()+".bsq");
        }
        
        for(int i=0;i<header.getNbands();i++){
            bands.add(new Band(header.getNcols(), header.getNrows(), header.getNbits()));
        }
    }
    
    public void writeHeader(){
        
        String rasterPath = outputFile.getAbsolutePath();
        File headerFile = new File(rasterPath.substring(0, rasterPath.length()-4)+".hdr");
        
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(headerFile))) {
            writer.write(header.toString());
        } catch (IOException ex) {
            logger.error("Cannot write header file "+headerFile.getAbsolutePath(), ex);
        }
    }
    
    
    
    public void setPixel(int posX, int posY, int bandID, Color color){
        
        if(bandID > bands.size()){
            logger.warn("Cannot set pixel to band "+bandID+" ; band doesn't exist");
            return;
        }
        
        boolean[] bits = new boolean[header.getNbits().getNumberOfBits()];
        
        switch(header.getNbits()){
            case N_BITS_1: //black or white
                bits[0] = (color.getRed()/255) != 0;
                break;
            case N_BITS_4: //16 differents colors
                break;
            case N_BITS_8: //256 differents colors
                bits = getBooleanBitsArray(color.getRed(), 8);
                break;
            case N_BITS_16: //65536 colors
                
                break;
            case N_BITS_32:
                
                boolean[] bitsRed = getBooleanBitsArray(color.getRed(), 8);
                boolean[] bitsGreen = getBooleanBitsArray(color.getGreen(), 8);
                boolean[] bitsBlue = getBooleanBitsArray(color.getBlue(), 8);
                boolean[] bitsAlpha = getBooleanBitsArray(color.getAlpha(), 8);
                
                concatenateArrays(bits, bitsRed, bitsGreen, bitsBlue, bitsAlpha);
                
                break;
        }
        
        
        
        bands.get(bandID).setPixel(posX, posY, bits);
    }
    
    private void concatenateArrays(boolean[] dest, boolean[]... srcs){
        
        int count = 0;
        for(int j=0;j<srcs.length;j++){
            for(int k=0;k<srcs[j].length;k++){
                dest[count] = srcs[j][k];
                count++;
            }
        }
    }
    
    
    
    public abstract void writeColorFile();
    public abstract void writeStatisticsFile();
    
}
