/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.als.laz;

import fr.amap.amapvox.als.LasHeader;
import fr.amap.amapvox.als.LasPoint;
import fr.amap.amapvox.commons.util.NativeLoader;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LazExtraction implements Iterable<LasPoint>{
    
    private final static String NATIVE_LIBRARY_NAME = "LasZipLibrary";
    
    private final static Logger logger = Logger.getLogger(LazExtraction.class);
    
    private native void afficherBonjour();
    private native long instantiateLasZip();
    private native void deleteLasZip(long pointer);
    private native int open(long pointer, String file_name);
    private native void readAllPoints(long pointer);
    private native LasPoint getNextPoint(long pointer);
    private native LasHeader getBasicHeader(long pointer);
    
    private long lasZipPointer;
    private LasHeader header;
    
    public LazExtraction(){
        
    }
    
    static {
        
        NativeLoader loader = new NativeLoader();
        try {
            loader.loadLibrary(NATIVE_LIBRARY_NAME, LazExtraction.class);
        } catch (IOException ex) {
            logger.error("Cannot load "+NATIVE_LIBRARY_NAME+" library", ex);
        }
        
    }
    
    public void openLazFile(File file) throws IOException, Exception{
        
        try{
            lasZipPointer = instantiateLasZip();
        }catch(Exception e){
            throw new Exception("Cannot initialize laszip pointer", e);
        }
        
        
        int result = open(lasZipPointer, file.getAbsolutePath());
            
        switch(result){
            case -1:
                throw new IOException("Laz file "+file.getAbsolutePath()+" cannot be open");
            case 0:
                logger.info("Laz file "+file.getAbsolutePath()+" is opened");
                break;

            default:
                throw new Exception("Laz file "+file.getAbsolutePath()+" reading error");
        }
        
        try{
            header = getBasicHeader(lasZipPointer);
        }catch(Exception e){
            throw new Exception("Cannot get laz header", e);
        }
        
        if(header == null){
            throw new Exception("Cannot get laz header");
        }
        
    }
    
    public void close(){
        deleteLasZip(lasZipPointer);
        logger.info("Laz file is closed");
    }

    public LasHeader getHeader() {
        return header;
    }

    @Override
    public Iterator<LasPoint> iterator() {
        
        long size = header.getNumberOfPointrecords();
        
        Iterator<LasPoint> it = new Iterator<LasPoint>() {
            
            long count = 0;
            
            @Override
            public boolean hasNext() {
                return count<size;
            }

            @Override
            public LasPoint next() {
                count++;
                return getNextPoint(lasZipPointer);
            }
        };
        
        return it;
    }
}
