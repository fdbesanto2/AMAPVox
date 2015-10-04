/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.als.laz;

import fr.amap.lidar.als.LasHeader;
import fr.ird.voxelidar.util.NativeLoader;
import fr.amap.lidar.als.LasPoint;
import java.io.File;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 *
 * @author Julien Heurtebize (julienhtbe@gmail.com)
 */
public class LazExtraction implements Iterable<LasPoint>{
    
    public final static Logger logger = Logger.getLogger(LazExtraction.class);
    private final static String NATIVE_LIBRARY_NAME = "LasZipLibrary";
    
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
        loader.loadLibrary(NATIVE_LIBRARY_NAME);
        
    }
    
    public void openLazFile(File file){
        
        try{
            lasZipPointer = instantiateLasZip();
        }catch(Exception e){
            logger.error("Cannot initialize laszip pointer");
        }
        
        
        int result = open(lasZipPointer, file.getAbsolutePath());
            
        switch(result){
            case -1:
                logger.error("Laz file "+file.getAbsolutePath()+" cannot be open");
                break;

            case 0:
                logger.info("Laz file "+file.getAbsolutePath()+" is opened");
                break;

            default:
                logger.error("Laz file "+file.getAbsolutePath()+" reading error");
        }
        
        try{
            header = getBasicHeader(lasZipPointer);
        }catch(Exception e){
            logger.error("Cannot get laz header");
        }
        
        if(header == null){
            logger.error("Cannot get laz header");
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
