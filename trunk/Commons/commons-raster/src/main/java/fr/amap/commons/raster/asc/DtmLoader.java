/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.commons.raster.asc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class DtmLoader {

    
    public static RegularDtm readFromAscFile(File ascFile) throws Exception{
        
        final String pathFile = ascFile.getAbsolutePath();
        
        
        String line;
        
        try (BufferedReader reader = new BufferedReader(new FileReader(ascFile))) {
            
            int nbCols = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            int nbRows = Integer.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float xLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float yLeftCorner = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float step = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            float noDataValue = Float.valueOf(reader.readLine().split(" ", 2)[1].trim());
            
            float[][] zArray = new float[nbCols][nbRows];
            
            float z;
            
            int yIndex = 0;
            
            while((line = reader.readLine()) != null){
                
                line = line.trim();
                String[] values = line.split(" ", -1);
                if(values.length != nbCols){
                    throw new Exception("nb columns different from ncols header value");
                }
                
                for(int xIndex=0;xIndex<values.length;xIndex++){
                    
                    z = Float.valueOf(values[xIndex]);
                    if(z == noDataValue){
                        z = Float.NaN;
                    }
                    zArray[xIndex][yIndex] = z;
                    
                }
                
                yIndex++;
            }
            
            RegularDtm terrain = new RegularDtm(pathFile, zArray, xLeftCorner, yLeftCorner, step, nbCols, nbRows);
        
            return terrain;
            
        } catch (IOException ex) {
            throw ex;
        }
    }
   
}
