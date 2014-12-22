package fr.ird.voxelidar;

import fr.ird.voxelidar.frame.JFrameSettingUp;
import java.io.IOException;
import org.apache.log4j.Logger;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author Julien
 */
public class Principal {
    
    final static Logger logger = Logger.getLogger(Principal.class);
    
    public static void main(String args[]) throws IOException {
        //Las read = LasReader.read("F:\\Las files\\1.3\\LAS-1.3-waveform-terrapoint.las");
        //LasToTxt.writeTxt(read, "F:\\Las files\\1.1\\LAS-1.3-waveform-terrapoint.txt", "xyzirndeca");
        /*
        AlsPreprocessing als = new AlsPreprocessing();
        als.preprocessAlsForVoxelisation("C:\\Users\\Julien\\Desktop\\Test Als preprocess\\CoordonnéesP15.csv",
        "C:\\Users\\Julien\\Desktop\\Test Als preprocess\\ALSbuf_xyzirncapt.txt",
        "C:\\Users\\Julien\\Desktop\\Test Als preprocess\\sbet_250913_01.txt");
         */
        //float det = Vec2.determinant(new Vec2(0.0f,0.0f), new Vec2(3.0f,6.0f));
        
        
        
        JFrameSettingUp mainJFrame = new JFrameSettingUp();
        mainJFrame.setVisible(true);
        
        
        
        
    }
}
