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
 *
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
    
    public void close(){
        closeConnexion(rxpPointer);
        delete(rxpPointer);
    }

    @Override
    public Iterator<Shot> iterator() {
        
        Iterator<Shot> it = new Iterator<Shot>() {
            
            @Override
            public boolean hasNext() {
                return hasShot(rxpPointer);
            }

            @Override
            public Shot next() {
                return getNextShot(rxpPointer);
            }
        };
        
        return it;
    }
    
}
