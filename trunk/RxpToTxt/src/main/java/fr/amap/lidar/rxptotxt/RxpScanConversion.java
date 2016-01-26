/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.rxptotxt;

import fr.amap.amapvox.commons.util.SphericalCoordinates;
import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author calcul
 */
public class RxpScanConversion {

    
    public RxpScanConversion() {
        
    }
    
    public void toTxt(SimpleScan scan, File outputDirectory) throws IOException, Exception{
        
        /***Convert rxp to txt***/

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {
            
            RxpExtraction extraction = new RxpExtraction();
            
            extraction.openRxpFile(scan.file);
            
            Iterator<Shot> iterator = extraction.iterator();
            
            writer.write("phi theta r1 r2 r3 r4 r5 r6 r7\n");
            
            while(iterator.hasNext()){
                
                Shot shot = iterator.next();
                
                SphericalCoordinates sc = new SphericalCoordinates();
                sc.toSpherical(shot.direction);
                
                String line = Math.toDegrees(sc.getAzimuth())+" "+Math.toDegrees(sc.getElevation());
                
                for(int i=0;i<shot.nbEchos;i++){
                    
                    line += " "+shot.ranges[i];
                }
                
                writer.write(line+"\n");
            }
            
            extraction.close();
        }
        
    }
}
