package fr.ird.voxelidar.voxelisation.extraction.tls;


import fr.ird.voxelidar.voxelisation.extraction.Shot;
import fr.ird.voxelidar.util.NativeLoader;
import java.io.File;
import java.util.Iterator;
import org.apache.log4j.Logger;


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
    
    public final static Logger logger = Logger.getLogger(RxpExtraction.class);
    private final static String NATIVE_LIBRARY_NAME = "RivLibLibrary";
    
    private native void afficherBonjour();
    private native long instantiate();
    private native void delete(long pointer);
    private native int open(long pointer, String file_name);
    private native void closeConnexion(long pointer);
    private native Shot getNextShot(long pointer);
    private native boolean hasShot(long pointer);
    
    private long rxpPointer;
    
    static {
        
        NativeLoader loader = new NativeLoader();
        loader.loadLibrary(NATIVE_LIBRARY_NAME);
        
    }
    
    public int openRxpFile(File file){
        
        Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        
        rxpPointer = instantiate();

        switch ((int) rxpPointer) {
            case -1:
                break;
            case -2:
                break;
            default:
                int result = open(rxpPointer, file.getAbsolutePath());

                switch (result) {
                    case -1:
                        logger.error("Rxp file " + file.getAbsolutePath() + " cannot be open");
                        break;

                    case 0:
                        logger.info("Rxp file " + file.getAbsolutePath() + " is open");
                        break;

                    default:
                        logger.error("Rxp file " + file.getAbsolutePath() + " reading error");
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
