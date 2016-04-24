/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar.rxptotxt;

import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.commons.util.SphericalCoordinates;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import javax.vecmath.Vector3d;

/**
 *
 * @author calcul
 */
public class RxpScanConversion {

    
    public RxpScanConversion() {
        
    }
    
    public void toTxt(SimpleScan scan, File outputDirectory,
            boolean exportReflectance, boolean exportAmplitude, boolean exportDeviation) throws IOException, Exception{
        
        /***Convert rxp to txt***/

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {
            
            RxpExtraction extraction = new RxpExtraction();
            
            extraction.openRxpFile(scan.file, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.REFLECTANCE);
            
            Iterator<Shot> iterator = extraction.iterator();
            
            /**Transformation**/
            Mat4D popMatrix = scan.popMatrix;
            Mat4D sopMatrix = scan.sopMatrix;
            
            Mat4D transfMatrix = Mat4D.multiply(sopMatrix, popMatrix);

            Mat3D rotation = new Mat3D();
            rotation.mat = new double[]{
                transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
                transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
                transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
            };   
            
            DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.getDefault());
            otherSymbols.setDecimalSeparator('.');
            otherSymbols.setGroupingSeparator('.'); 
            DecimalFormat strictFormat = new DecimalFormat("###.##", otherSymbols);
            
            String header = "shotID x y z directionX directionY directionZ distance nbEchos rangEcho";
            
            if(exportReflectance){
                header += " reflectance";
            }

            if(exportAmplitude){
                header += " amplitude";
            }

            if(exportDeviation){
                header += " deviation";
            }
            
            header += "\n";
            
            writer.write(header);
            
            int shotID = 0;
            while(iterator.hasNext()){
                
                Shot shot = iterator.next();
               
                Vec4D origin = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
                Vec3D direction = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));
                direction = Vec3D.normalize(direction);
                
                SphericalCoordinates sc = new SphericalCoordinates();
                sc.toSpherical(new Vector3d(direction.x, direction.y, direction.z));
                
                for(int i=0;i<shot.nbEchos;i++){
                    
                    double x = origin.x + direction.x * shot.ranges[i];
                    double y = origin.y + direction.y * shot.ranges[i];
                    double z = origin.z + direction.z * shot.ranges[i];
                    
                    String echo = shotID + " " + x + " " + y + " " + z + " " + direction.x + " " + direction.y+ " " + direction.z + " " + shot.ranges[i]+" "+shot.nbEchos+" "+i;
                    
                    if(exportReflectance){
                        echo += " " + strictFormat.format(shot.reflectances[i]);
                    }
                    
                    if(exportAmplitude){
                        echo += " " + strictFormat.format(shot.amplitudes[i]);
                    }
                    
                    if(exportDeviation){
                        echo += " " + strictFormat.format(shot.deviations[i]);
                    }
                    
                    echo += "\n";
                    
                    writer.write(echo);
                }
                
                shotID++;
            }
            
            extraction.close();
        }
        
    }
}
