/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.rxptolaz;

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
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author calcul
 */
public class RxpScanConversion {

    
    public RxpScanConversion() {
        
    }
    
    private String getOSName() throws UnsupportedOperationException{
        
        String osArch = System.getProperty("os.arch");
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.startsWith("win")) {
            if (osArch.equalsIgnoreCase("x86")) {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            } else {
                return "windows";
            }
        } else if (osName.startsWith("linux")) {
            if (osArch.equalsIgnoreCase("amd64")) {
                return "linux";
            } else if (osArch.equalsIgnoreCase("ia64")) {
                return "linux";
            } else if (osArch.equalsIgnoreCase("i386")) {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            } else {
                throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
            }
        } else {
            throw new UnsupportedOperationException("Platform " + osName + ":" + osArch + " not supported");
        }
    }
    
    private int reflectanceToIntensity(float reflectance){
        
        float min = -50;
        float max = 50;
        
        return (int) (65535 * ((reflectance - min) / (max - min)));
    }
    
    public void toLaz(SimpleScan scan, File outputDirectory) throws IOException, InterruptedException, UnsupportedOperationException, Exception{
        
        /***Convert rxp to txt***/
        
        Mat4D transfMatrix = Mat4D.multiply(scan.sopMatrix, scan.popMatrix);
        
        Mat3D rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile));

        RxpExtraction extraction = new RxpExtraction();

        extraction.openRxpFile(scan.file, RxpExtraction.REFLECTANCE);

        Iterator<Shot> iterator = extraction.iterator();

        while(iterator.hasNext()){

            Shot shot = iterator.next();
            
            Vec4D origin = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
            Vec3D direction = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

            for(int i=0;i<shot.nbEchos;i++){

                double x = origin.x + direction.x * shot.ranges[i];
                double y = origin.y + direction.y * shot.ranges[i];
                double z = origin.z + direction.z * shot.ranges[i];

                writer.write(x+" "+y+" "+z+" "+(i+1)+" "+shot.nbEchos+" "+reflectanceToIntensity(shot.reflectances[i])+"\n");
            }

        }

        extraction.close();
        writer.close();

        /***Convert txt to laz***/
        String propertyValue = System.getProperty("user.dir");
        System.out.println("Current jar directory : "+propertyValue);

        String txtToLasPath;

        String osName = getOSName();

        switch(osName){
            case "windows":
            case "linux":
                txtToLasPath = propertyValue+File.separator+"LASTools"+File.separator+osName+File.separator+"txt2las";
                break;
            default:
                throw new UnsupportedOperationException("Os architecture not supported");
        }

        if(osName.equals("windows")){
            txtToLasPath = txtToLasPath+".exe";
        }

        File outputLazFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".las");

        String[] commandLine = new String[]{txtToLasPath, "-i", outputTxtFile.getAbsolutePath(),
                                                                         "-o", outputLazFile.getAbsolutePath(),
                                                                         "-parse", "xyzrni"};

        System.out.println("Command line : "+ArrayUtils.toString(commandLine).replaceAll(",", " ").replaceAll("}", "").replace("{", ""));

        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process p = pb.start();

        p.waitFor();   
        
    }
}
