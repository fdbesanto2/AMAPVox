package fr.amap.amapvox.io.tls.rxp;


import fr.amap.commons.util.NativeLoader;
import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandle;
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
    private native int open(long pointer, String file_name, int[] shotTypes);
    private native void closeConnexion(long pointer);
    private native Shot getNextShot(long pointer);
    private native boolean hasShot(long pointer);
    
    public final static int REFLECTANCE = 2;
    public final static int DEVIATION = 3;
    public final static int AMPLITUDE = 4;
    public final static int TIME = 5;
    
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
     * @param shotTypes Echoes attributes to import, variable parameters, can be {@link #REFLECTANCE},
     * {@link #DEVIATION}, {@link #AMPLITUDE}, {@link #TIME}.
     * Import only attributes you need.
     * @return -1 if an exception has occured, 0 if everything if fine and other value for unknown exception
     * @throws IOException if path of the file is invalid
     * @throws Exception if an unknown exception occured when trying to open the file 
     */
    public int openRxpFile(File file, int... shotTypes) throws IOException, Exception{
                
        rxpPointer = instantiate();

        switch ((int) rxpPointer) {
            case -1:
                break;
            case -2:
                break;
            default:
                int result = open(rxpPointer, file.getAbsolutePath(), shotTypes);

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
    
    public static void main(String[] args) {
        
        RxpExtraction extraction = new RxpExtraction();
        try {
            extraction.openRxpFile(new File("/home/julien/Documents/SINGLESCANS/750213_173552.mon.rxp"), RxpExtraction.REFLECTANCE, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION);
            
            Iterator<Shot> iterator = extraction.iterator();
            
            /*while(iterator.hasNext()){
                Shot shot = iterator.next();
                System.out.println(shot.origin.x+" "+shot.origin.y+" "+shot.origin.z);
            }*/
            
            Shot shot ;
            do{
                shot = iterator.next();
                if(shot != null){
                    System.out.println(shot.origin.x+" "+shot.origin.y+" "+shot.origin.z);
                }
                
            }while(shot != null);
            
            System.err.println("opened");
        } catch (Exception ex) {
            Logger.getLogger(RxpExtraction.class.getName()).log(Level.SEVERE, null, ex);
        }finally{
            extraction.close();
        }
    }

    @Override
    public Iterator<Shot> iterator() {
        
        Iterator<Shot> it = new Iterator<Shot>() {
                        
            private Shot shot = null;
            private boolean hasNextCalled = false;
            int nbShotsFailed = 0;
            
            private Shot nextShot(){
                
                if(hasShot(rxpPointer)){
                    
                    Object o = getNextShot(rxpPointer);
                    if(o instanceof Shot){
                        shot = (fr.amap.amapvox.io.tls.rxp.Shot) o;
                    }else{
                        nbShotsFailed++;
                        return nextShot();
                    }
                                        
                    return shot;
                    
                }else{
                    return null;
                }
            }
             
            @Override
            public boolean hasNext() {
                
                hasNextCalled = true;
                
                shot = nextShot();
                
                return shot != null;
            }

            @Override
            public Shot next() {
                
                if(hasNextCalled){
                    hasNextCalled = false;
                    return shot;
                }else{
                    return nextShot();
                }
                
            }
        };
        
        return it;
    }
    
}
