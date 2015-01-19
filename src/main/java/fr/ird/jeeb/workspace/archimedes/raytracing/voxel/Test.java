/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.jeeb.workspace.archimedes.raytracing.voxel;

import fr.ird.voxelidar.io.file.FileManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3f;
import javax.vecmath.Point3i;
import javax.vecmath.Vector3f;
import org.apache.commons.lang3.ArrayUtils;


/**
 *
 * @author Julien
 */
public class Test{
    
    public static void main(String args[]) {
        
        int nbAvailableCore = Runtime.getRuntime().availableProcessors();
        //int lineNumber = FileManager.getLineNumber("C:\\Users\\Julien\\Documents\\Jean process vox\\TLS_format_Fred.txt");
        
        
        VoxelAnalysis voxelAnalysis = new VoxelAnalysis();
        voxelAnalysis.init(
                new VoxelParameters(new Point3f(-10, -10, -2), new Point3f(10, 10, 6), new Point3i(20, 20, 8)));
        
        try {
            BufferedReader reader = new BufferedReader(new FileReader(new File("test_echos")));
            
            String line;
            
            //header
            //reader.readLine();
            
            while((line = reader.readLine()) != null){
                
                String[] lineSplitted = line.split(" ");
                lineSplitted = ArrayUtils.remove(lineSplitted, 0);
                
                int nbEchos = Integer.valueOf(lineSplitted[0]);
                
                Point3f origin = new Point3f(Float.valueOf(lineSplitted[1]), Float.valueOf(lineSplitted[2]), Float.valueOf(lineSplitted[3]));
                Vector3f direction = new Vector3f(Float.valueOf(lineSplitted[4]), Float.valueOf(lineSplitted[5]), Float.valueOf(lineSplitted[6]));
                float[] ranges = new float[nbEchos];
                
                for(int i=0;i<nbEchos;i++){
                    ranges[i] = Float.valueOf(lineSplitted[i+7]);
                }
                
                if(nbEchos != lineSplitted.length-7){
                    System.err.println("erreur");
                }
                
                Shot shot = new Shot(nbEchos, origin, direction, ranges);
                
                voxelAnalysis.voxelise(shot);
            }
            
            voxelAnalysis.writeOutput(new File("test_vox_jean"));
            
            reader.close();
            
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Test.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
}

