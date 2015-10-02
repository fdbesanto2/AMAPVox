package fr.amap.amapvox.io.tls.rxp;


import fr.amap.amapvox.commons.util.NativeLoader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 * <p>This class is an utility to open and read rxp Riegl proprietary format</p>
 * <p>The idea of this reader is to get an iterator on those files and getting shot + echos (with reflectance)</p>
 * <p>This class call a native JNI library using Riegl RivLib library.</p>
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class RxpExtraction implements Iterable<Shot>{
    
    private final static String NATIVE_LIBRARY_NAME = "RivLibLibrary";
    
    private native void afficherBonjour();
    private native long instantiate();
    private native void delete(long pointer);
    private native int open(long pointer, String file_name, int shotType);
    private native void closeConnexion(long pointer);
    private native Shot getNextShot(long pointer);
    private native boolean hasShot(long pointer);
    
    public final static short SIMPLE_SHOT = 1;
    public final static short SHOT_WITH_REFLECTANCE = 2;
    
    private long rxpPointer;
    
    static {
        
        NativeLoader loader = new NativeLoader();
        try {
            loader.loadLibrary(NATIVE_LIBRARY_NAME, RxpExtraction.class);
        } catch (IOException ex) {
            Logger.getLogger(RxpExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    /**
     * Open a rxp file and instantiate a pointer
     * @param file Rxp file to read
     * @param shotType <p>Type of shot, can be RxpExtraction.SIMPLE_SHOT or RxpExtraction.SHOT_WITH_REFLECTANCE</p>
     * <p>One having simple informations and the other having reflectance value for each echo</p>
     * @return -1 if an exception has occured, 0 if everything if fine and other value for unknown exception
     * @throws IOException if path of the file is invalid
     * @throws Exception if an unknown exception occured when trying to open the file 
     */
    public int openRxpFile(File file, int shotType) throws IOException, Exception{
                
        rxpPointer = instantiate();

        switch ((int) rxpPointer) {
            case -1:
                break;
            case -2:
                break;
            default:
                int result = open(rxpPointer, file.getAbsolutePath(), shotType);

                switch (result) {
                    case -1:
                        throw new IOException("Rxp file " + file.getAbsolutePath() + " cannot be open");

                    case 0:
                        //logger.info("Rxp file " + file.getAbsolutePath() + " is open");
                        break;

                    default:
                        throw new Exception("Rxp file " + file.getAbsolutePath() + " reading error");
                }
            
                return result;
        }
        
        return -1;
        
    }
    
    /**
     * Close rxp file and delete pointer
     */
    public void close(){
        closeConnexion(rxpPointer);
        delete(rxpPointer);
    }

    @Override
    public Iterator<Shot> iterator() {
        
        
        
        Iterator<Shot> it = new Iterator<Shot>() {
            
            Shot shot;
            
            @Override
            public boolean hasNext() {
                
                if(hasShot(rxpPointer)){
                    
                    shot = getNextShot(rxpPointer);
                    return shot != null;
                    
                }else{
                    return false;
                }
            }

            @Override
            public Shot next() {
                
                return shot;
            }
        };
        
        return it;
    }
    
}
