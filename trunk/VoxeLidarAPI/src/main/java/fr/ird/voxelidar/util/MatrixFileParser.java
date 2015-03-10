/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.voxelidar.util;

import fr.ird.voxelidar.io.file.FileManager;
import java.io.File;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Matrix4d;

/**
 *
 * @author Julien
 */
public class MatrixFileParser {
    
    public static Matrix4d getMatrixFromFile(File matrixFile){
        
        FileManager fileManager = new FileManager();
        ArrayList<String> lines = fileManager.readAllLines(matrixFile);
        
        if(lines.size() != 4){
            return null;
        }
        
        Matrix4d matrix = new Matrix4d();
        
        for(int j=0;j<lines.size();j++){
            
            StringTokenizer parser = new StringTokenizer(lines.get(j));
            int count = 0;
            while(parser.hasMoreTokens()){
                String val = parser.nextToken();
                double v1 = Double.valueOf(val);
                matrix.setElement(j, count, v1);
                count++;
            }
        }
        
        return matrix;
    }
}
