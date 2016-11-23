/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar;

import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.util.SphericalCoordinates;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.lidar.format.jleica.LPointShotExtractor;
import fr.amap.lidar.format.jleica.LShot;
import fr.amap.lidar.format.jleica.ptg.PTGScan;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Iterator;
import java.util.Locale;
import javax.vecmath.Vector3d;
import org.apache.commons.lang3.ArrayUtils;

/**
 *
 * @author Julien Heurtebize
 */
public class PtgScanConversion {

    public PtgScanConversion() {
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
    
    public void toLaz(SimpleScan scan, File outputDirectory, boolean laz, boolean exportIntensity) throws IOException, InterruptedException, UnsupportedOperationException, Exception{
        
        /***Convert rxp to txt***/
        
        Mat4D transfMatrix = Mat4D.multiply(scan.sopMatrix, scan.popMatrix);
        
        Mat3D rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {
            PTGScan ptgScan = new PTGScan();
            ptgScan.openScanFile(scan.file);
            
            LPointShotExtractor shots = new LPointShotExtractor(ptgScan);
            Iterator<LShot> iterator = shots.iterator();
            
            
            while(iterator.hasNext()){
                
                LShot shot = iterator.next();
                shot.direction.normalize();
                
                Vec4D origin = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
                Vec3D direction = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));
                
                for(int i=0;i<shot.ranges.length;i++){
                    
                    double x = origin.x + direction.x * shot.ranges[i];
                    double y = origin.y + direction.y * shot.ranges[i];
                    double z = origin.z + direction.z * shot.ranges[i];
                    
                    if(exportIntensity){
                        writer.write(x+" "+y+" "+z+" "+(i+1)+" "+shot.ranges.length+" "+shot.point.intensity+"\n");
                    }else{
                        writer.write(x+" "+y+" "+z+" "+(i+1)+" "+shot.ranges.length+"\n");
                    }
                    
                }
                
            }
        }

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

        File outputLazFile;
        if(laz){
            outputLazFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".laz");
        }else{
            outputLazFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".las");
        }
        
        

        String[] commandLine;
        
        if(exportIntensity){
           commandLine = new String[]{txtToLasPath, "-i", outputTxtFile.getAbsolutePath(),
                                                                         "-o", outputLazFile.getAbsolutePath(),
                                                                         "-parse", "xyzrni"};
        }else{
            commandLine = new String[]{txtToLasPath, "-i", outputTxtFile.getAbsolutePath(),
                                                                         "-o", outputLazFile.getAbsolutePath(),
                                                                         "-parse", "xyzrn"};
        }
        

        System.out.println("Command line : "+ArrayUtils.toString(commandLine).replaceAll(",", " ").replaceAll("}", "").replace("{", ""));

        ProcessBuilder pb = new ProcessBuilder(commandLine);
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);

        Process p = pb.start();

        p.waitFor();   
        
    }
    
    public void toTxt(SimpleScan scan, File outputDirectory,
            boolean exportRGB, boolean exportIntensity) throws IOException, Exception{
        
        /***Convert rxp to txt***/

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {
            
            
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
            
            String header = "directionX directionY directionZ x y z empty";
            
            if(exportIntensity){
                header += " intensity";
            }

            if(exportRGB){
                header += " red green blue";
            }
            
            header += "\n";
            
            writer.write(header);
            
            PTGScan ptgScan = new PTGScan();
            ptgScan.openScanFile(scan.file);
                
            LPointShotExtractor shots = new LPointShotExtractor(ptgScan);
            Iterator<LShot> iterator = shots.iterator();
            
            int shotID = 0;
             
            while(iterator.hasNext()){
                
                LShot shot = iterator.next();
                shot.direction.normalize();
                
                Vec4D origin = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
                Vec3D direction = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));
                direction = Vec3D.normalize(direction);
                
                SphericalCoordinates sc = new SphericalCoordinates();
                sc.toSpherical(new Vector3d(direction.x, direction.y, direction.z));
                
                short empty = 1;
                
                if(shot.ranges.length > 0){
                    empty = 0;
                }
                
                double x = origin.x + direction.x * 100;
                double y = origin.y + direction.y * 100;
                double z = origin.z + direction.z * 100;
                    
                writer.write(direction.x+" "+direction.y+" "+direction.z+" "+x+" "+y+" "+z+" "+empty+"\n");
                
//                for(int i=0;i<shot.ranges.length;i++){
//                    
//                    double x = origin.x + direction.x * shot.ranges[i];
//                    double y = origin.y + direction.y * shot.ranges[i];
//                    double z = origin.z + direction.z * shot.ranges[i];
//                    
//                    String echo = shotID + " " + x + " " + y + " " + z + " " + direction.x + " " + direction.y+ " " + direction.z + " " + shot.ranges[i]+" "+shot.ranges.length+" "+i;
//                    
//                    if(exportIntensity){
//                        echo += " " + strictFormat.format(shot.point.intensity);
//                    }
//                    
//                    if(exportRGB){
//                        echo += " " + strictFormat.format(shot.point.red);
//                        echo += " " + strictFormat.format(shot.point.green);
//                        echo += " " + strictFormat.format(shot.point.blue);
//                    }
//                    
//                    echo += "\n";
//                    
//                    writer.write(echo);
//                }
                
                shotID++;
            }
        }catch(Exception ex){
            System.err.println(ex);
        }
    }
}
