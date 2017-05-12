/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.lidar;

import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import fr.amap.commons.math.matrix.Mat3D;
import fr.amap.commons.math.matrix.Mat4D;
import fr.amap.commons.math.util.SphericalCoordinates;
import fr.amap.commons.math.vector.Vec3D;
import fr.amap.commons.math.vector.Vec4D;
import fr.amap.lidar.format.shot.Column;
import fr.amap.lidar.format.shot.Echo;
import fr.amap.lidar.format.shot.ShotFileContext;
import fr.amap.lidar.format.shot.ShotWriter;
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
    
        
    public void toShots2(SimpleScan scan, File outputDirectory,  boolean exportReflectance, boolean exportAmplitude, boolean exportDeviation, boolean exportTime, boolean exportXYZ) throws IOException, InterruptedException, UnsupportedOperationException, Exception{
        
        /***Convert rxp to txt***/
        /*** Includes Shot timeStamp ***/
        
        /*** 
         * SOP: pour chaque scan --> syst Projet 
         * POP: transformation globale du Projet (en particulier vers coordonnées géographiques)
         * 
         ***/
        Mat4D transfMatrix = Mat4D.multiply(scan.sopMatrix, scan.popMatrix);
        
        Mat3D rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        File outputMDFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".md");
        BufferedWriter writerMD = new BufferedWriter(new FileWriter(outputMDFile));
        writerMD.write("shotID shotTime nbEchos\n");
      
        
        int nbExtraAttributes = 0;
        if(exportReflectance){nbExtraAttributes++;}
        if(exportDeviation){nbExtraAttributes++;}
        if(exportAmplitude){nbExtraAttributes++;}
        if(exportTime){nbExtraAttributes++;}
        if(exportXYZ){nbExtraAttributes+=3;}
        
        Column[] extraColumns = new Column[nbExtraAttributes];
        int index = 0;
        if(exportReflectance){
            extraColumns[index] = new Column("reflectance", Column.Type.FLOAT);
            index++;
        }
        if(exportDeviation){
            extraColumns[index] = new Column("deviation", Column.Type.FLOAT);
            index++;
        }
        if(exportAmplitude){
            extraColumns[index] = new Column("amplitude", Column.Type.FLOAT);
            index++;
        }
        if(exportTime){
            extraColumns[index] = new Column("time", Column.Type.DOUBLE);
            index++;
        }
        if(exportXYZ){
            extraColumns[index] = new Column("x", Column.Type.DOUBLE);
            index++;
            extraColumns[index] = new Column("y", Column.Type.DOUBLE);
            index++;
            extraColumns[index] = new Column("z", Column.Type.DOUBLE);
            index++;            
        }
        
        ShotFileContext context = new ShotFileContext(extraColumns);
                
        ShotWriter2 writer = new ShotWriter2(context, outputTxtFile);

        RxpExtraction extraction = new RxpExtraction();

        extraction.openRxpFile(scan.file, RxpExtraction.REFLECTANCE, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.TIME);

        Iterator<Shot> iterator = extraction.iterator();

        int shotID = 0;
        
        while(iterator.hasNext()){

            Shot shot = iterator.next();
            double shotTime = shot.time;
            
            Vec4D origin = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
            Vec3D direction = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

            writerMD.write(shotID + " " + shotTime + " " + shot.nbEchos + "\n");
            if(shot.nbEchos == 0){
                writer.write(new fr.amap.lidar.format.shot.Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z), shotTime);
            }else{
                
                Echo[] echoes = new Echo[shot.nbEchos];
                
                for(int i=0;i<shot.nbEchos;i++){
                    
                    Object[] extra = new Object[nbExtraAttributes];
                    
                    index = 0;
                    if(exportReflectance){
                        extra[index] = shot.reflectances[i];
                        index++;
                    }
                    if(exportDeviation){
                        extra[index] = shot.deviations[i];
                        index++;
                    }
                    if(exportAmplitude){
                        extra[index] = shot.amplitudes[i];
                        index++;
                    }
                    if(exportTime){
                        extra[index] = shot.times[i];
                        index++;
                    }
                    if(exportXYZ){
                        extra[index] = shot.origin.x + shot.direction.x * shot.ranges[i];
                        index++;
                        extra[index] = shot.origin.y + shot.direction.y * shot.ranges[i];
                        index++;
                        extra[index] = shot.origin.z + shot.direction.z * shot.ranges[i];
                        index++;
                    }
                
                    echoes[i] = new Echo(shot.ranges[i], extra);
                }
                
                writer.write(new fr.amap.lidar.format.shot.Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, echoes),shotTime);
                
            }
            
            
            
            shotID++;

        }

        extraction.close();
        writer.close();
        writerMD.close();
    }
    
    
    
    
    public void toShots(SimpleScan scan, File outputDirectory,  boolean exportReflectance, boolean exportAmplitude, boolean exportDeviation, boolean exportTime, boolean exportXYZ) throws IOException, InterruptedException, UnsupportedOperationException, Exception{
        
        /***Convert rxp to txt***/
        
        Mat4D transfMatrix = Mat4D.multiply(scan.sopMatrix, scan.popMatrix);
        
        Mat3D rotation = new Mat3D();
        rotation.mat = new double[]{
            transfMatrix.mat[0],transfMatrix.mat[1],transfMatrix.mat[2],
            transfMatrix.mat[4],transfMatrix.mat[5],transfMatrix.mat[6],
            transfMatrix.mat[8],transfMatrix.mat[9],transfMatrix.mat[10]
        };

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        
        int nbExtraAttributes = 0;
        if(exportReflectance){nbExtraAttributes++;}
        if(exportDeviation){nbExtraAttributes++;}
        if(exportAmplitude){nbExtraAttributes++;}
        if(exportTime){nbExtraAttributes++;}
        if(exportXYZ){nbExtraAttributes+=3;}
        
        Column[] extraColumns = new Column[nbExtraAttributes];
        int index = 0;
        if(exportReflectance){
            extraColumns[index] = new Column("reflectance", Column.Type.FLOAT);
            index++;
        }
        if(exportDeviation){
            extraColumns[index] = new Column("deviation", Column.Type.FLOAT);
            index++;
        }
        if(exportAmplitude){
            extraColumns[index] = new Column("amplitude", Column.Type.FLOAT);
            index++;
        }
        if(exportTime){
            extraColumns[index] = new Column("time", Column.Type.DOUBLE);
            index++;
        }
        if(exportXYZ){
            extraColumns[index] = new Column("x", Column.Type.DOUBLE);
            index++;
            extraColumns[index] = new Column("y", Column.Type.DOUBLE);
            index++;
            extraColumns[index] = new Column("z", Column.Type.DOUBLE);
            index++;            
        }
        
        ShotFileContext context = new ShotFileContext(extraColumns);
                
        ShotWriter writer = new ShotWriter(context, outputTxtFile);

        RxpExtraction extraction = new RxpExtraction();

        extraction.openRxpFile(scan.file, RxpExtraction.REFLECTANCE, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.TIME);

        Iterator<Shot> iterator = extraction.iterator();

        int shotID = 0;
        
        while(iterator.hasNext()){

            Shot shot = iterator.next();
            
            Vec4D origin = Mat4D.multiply(transfMatrix, new Vec4D(shot.origin.x, shot.origin.y, shot.origin.z, 1.0d));
            Vec3D direction = Mat3D.multiply(rotation, new Vec3D(shot.direction.x, shot.direction.y, shot.direction.z));

            if(shot.nbEchos == 0){
                writer.write(new fr.amap.lidar.format.shot.Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z));
            }else{
                
                Echo[] echoes = new Echo[shot.nbEchos];
                
                for(int i=0;i<shot.nbEchos;i++){
                    
                    Object[] extra = new Object[nbExtraAttributes];
                    
                    index = 0;
                    if(exportReflectance){
                        extra[index] = shot.reflectances[i];
                        index++;
                    }
                    if(exportDeviation){
                        extra[index] = shot.deviations[i];
                        index++;
                    }
                    if(exportAmplitude){
                        extra[index] = shot.amplitudes[i];
                        index++;
                    }
                    if(exportTime){
                        extra[index] = shot.times[i];
                        index++;
                    }
                    if(exportXYZ){
                        extra[index] = shot.origin.x + shot.direction.x * shot.ranges[i];
                        index++;
                        extra[index] = shot.origin.y + shot.direction.y * shot.ranges[i];
                        index++;
                        extra[index] = shot.origin.z + shot.direction.z * shot.ranges[i];
                        index++;
                    }
                
                    echoes[i] = new Echo(shot.ranges[i], extra);
                }
                
                writer.write(new fr.amap.lidar.format.shot.Shot(shotID, origin.x, origin.y, origin.z, direction.x, direction.y, direction.z, echoes));
            }
            
            
            shotID++;

        }

        extraction.close();
        writer.close();
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

                if(exportIntensity){
                    writer.write(x+" "+y+" "+z+" "+(i+1)+" "+shot.nbEchos+" "+reflectanceToIntensity(shot.reflectances[i])+"\n");
                }else{
                    writer.write(x+" "+y+" "+z+" "+(i+1)+" "+shot.nbEchos+"\n");
                }
                
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
            boolean exportReflectance, boolean exportAmplitude, boolean exportDeviation, boolean exportTime) throws IOException, Exception{
        
        /***Convert rxp to txt***/

        File outputTxtFile = new File(outputDirectory.getAbsolutePath()+File.separator+scan.file.getName()+".txt");
        
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputTxtFile))) {
            
            RxpExtraction extraction = new RxpExtraction();
            
            extraction.openRxpFile(scan.file, RxpExtraction.AMPLITUDE, RxpExtraction.DEVIATION, RxpExtraction.REFLECTANCE, RxpExtraction.TIME);
            
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
            
            if(exportTime){
                header += " time";
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
                    
                    if(exportTime){
                        echo += " " + shot.times[i];
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
