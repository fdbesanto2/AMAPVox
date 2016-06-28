/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.format.shot;

import fr.amap.amapvox.io.tls.rxp.Shot;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author Julien Heurtebize
 */
public class ShotReader implements Iterable<Shot>{

    private final DataInputStream dis;
    
    private final short nbEchoesAttributes;
    private final String[] echoesAttributes;
    
    private final long shotNumber;
    
    public ShotReader(File file) throws FileNotFoundException, IOException, Exception {
        
        dis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        
        String signature = dis.readUTF();
        
        if(!signature.equals("LASER SHOT")){
            throw new Exception("File format is unknown !");
        }
        
        shotNumber = dis.readLong();
        
        nbEchoesAttributes = dis.readShort();
        echoesAttributes = new String[nbEchoesAttributes];
        
        for (short s = 0; s < nbEchoesAttributes; s++) {
            echoesAttributes[s] = dis.readUTF();
        }
    }

    public long getShotNumber() {
        return shotNumber;
    }
    
    public static boolean isFileAShotFile(File file) throws FileNotFoundException, IOException{
        
        String signature;
        try (DataInputStream tmpDis = new DataInputStream(new BufferedInputStream(new FileInputStream(file)))) {
            signature = tmpDis.readUTF();
        }catch(Exception ex){
            return false;
        }
        
        return signature.equals("LASER SHOT");        
    }

    @Override
    public Iterator<Shot> iterator() {
        
        return new Iterator<Shot>() {
            
            private Shot currentShot = null;
            private boolean hasNextCalled = false;
            
            private Shot getNextShot(){
                
                try {
                    Shot shot = new Shot();
                    
                    shot.nbEchos = dis.readByte();
                    
                    //read shot origin
                    shot.origin = new Point3d();
                    shot.origin.x = dis.readDouble();
                    shot.origin.y = dis.readDouble();
                    shot.origin.z = dis.readDouble();
                    
                    //read direction
                    shot.direction = new Vector3d();
                    shot.direction.x = dis.readDouble();
                    shot.direction.y = dis.readDouble();
                    shot.direction.z = dis.readDouble();
                    
                    //read distances to source
                    shot.ranges = new double[shot.nbEchos];
                    for(short i = 0;i<shot.nbEchos;i++){
                        
                        shot.ranges[i] = dis.readDouble();
                    }
                    
                    //read optional attributes
                    
                    for (int j = 0; j < echoesAttributes.length; j++) {
                        for (int i = 0; i < shot.nbEchos; i++) {
                            
                            double echoeAttribute = dis.readDouble();
                            
                            switch (echoesAttributes[j]) {
                                case "intensity":
                                    
                                    if(shot.intensities == null){
                                        shot.intensities = new float[shot.nbEchos];
                                    }
                                    
                                    shot.intensities[i] = (float) echoeAttribute;
                                    
                                    break;
                                case "reflectance":
                                    
                                    if(shot.reflectances == null){
                                        shot.reflectances = new float[shot.nbEchos];
                                    }
                                    
                                    shot.reflectances[i] = (float) echoeAttribute;
                                    
                                    break;
                                case "amplitude":
                                    
                                    if(shot.amplitudes == null){
                                        shot.amplitudes = new float[shot.nbEchos];
                                    }
                                    
                                    shot.amplitudes[i] = (float) echoeAttribute;
                                    
                                    break;
                                case "deviation":
                                    
                                    if(shot.deviations == null){
                                        shot.deviations = new float[shot.nbEchos];
                                    }
                                    
                                    shot.deviations[i] = (float) echoeAttribute;
                                    
                                    break;
                                default:
                                    
                                    if(shot.echoesAttributes == null){
                                        shot.echoesAttributes = new double[echoesAttributes.length][shot.nbEchos];
                                    }
                                    
                                    shot.echoesAttributes[j][i] = echoeAttribute;
                                    
                                    break;
                            }
                        }
                    }
                    
                    return shot;
                } catch (IOException ex) {
                    return null;
                }
            }
            
            @Override
            public boolean hasNext() {
                
                hasNextCalled = true;
                
                currentShot = getNextShot();
                
                return currentShot != null;
            }

            @Override
            public Shot next() {
                
                if(hasNextCalled){
                    hasNextCalled = false;
                    return currentShot;
                }else{
                    return getNextShot();
                }
            }
        };
    }
    
    public void close() throws IOException{
        dis.close();
    }
}
