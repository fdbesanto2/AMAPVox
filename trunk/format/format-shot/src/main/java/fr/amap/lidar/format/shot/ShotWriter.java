/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.format.shot;

import fr.amap.amapvox.io.tls.rxp.Shot;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 *
 * @author Julien Heurtebize
 */
public class ShotWriter {
    
    private final static String SIGNATURE = "LASER SHOT";
    
    private final DataOutputStream dos;
    
    private final short nbEchoesAttributes;
    private final String[] echoesAttributes;
    
    private final File file;
    
    private long shotNumber;
    private int offsetToShotNumberBytes;
    
    /**
     * 
     * @param file
     * @param attributes echoes attributes names
     * @throws FileNotFoundException
     * @throws IOException 
     */
    public ShotWriter(File file, String... attributes) throws FileNotFoundException, IOException{
        
        dos = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
        
        dos.writeUTF(SIGNATURE);
        offsetToShotNumberBytes = dos.size();
        dos.writeLong(-1); //number of shots, to update at the end
        dos.writeShort(attributes.length);
        
        nbEchoesAttributes = (short) attributes.length;
        echoesAttributes = attributes;
        
        for(String attribute : echoesAttributes){
            dos.writeUTF(attribute);
        }
        
        this.file = file;
        shotNumber = 0;
        
        
        
    }
    
    /**
     * 
     * @param shot
     * @param attributes Optional echoes attributes
     * @throws java.io.IOException if an I/O error occurs
     */
    public void write(Shot shot, double[]... attributes) throws IOException, Exception{
        
        dos.writeByte(shot.nbEchos);
        
        //write shot origin
        dos.writeDouble(shot.origin.x);
        dos.writeDouble(shot.origin.y);
        dos.writeDouble(shot.origin.z);
        
        //write direction
        dos.writeDouble(shot.direction.x);
        dos.writeDouble(shot.direction.y);
        dos.writeDouble(shot.direction.z);
        
        //write distances to source
        for(int i = 0;i<shot.nbEchos;i++){
            dos.writeDouble(shot.ranges[i]);
        }
        
        //write optional attributes
        
        if(attributes != null){
            
            if(attributes.length != nbEchoesAttributes){
                throw new Exception("The number of attributes is different from the number of attributes declared at the initialization.");
            }
            
            for (int j = 0; j < attributes.length; j++) {
                for (int i = 0; i < shot.nbEchos; i++) {

                    if(attributes[j].length != shot.nbEchos){
                        throw new ArrayIndexOutOfBoundsException("The number of values of the attribute is different from the number of echoes");
                    }

                    dos.writeDouble(attributes[j][i]);
                }
            }
        }
        
        shotNumber++;
    }
    
    public void close() throws IOException{
        
        dos.close();
        
        try ( //write the shot number
            RandomAccessFile raf = new RandomAccessFile(file, "rw")) {
            raf.seek(offsetToShotNumberBytes);
            raf.writeLong(shotNumber);
        }
    }
}
