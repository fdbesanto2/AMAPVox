package fr.ird.voxelidar;

import fr.ird.voxelidar.extraction.LasExtraction;
import fr.ird.voxelidar.extraction.LasExtractionListener;
import fr.ird.voxelidar.extraction.LasPoint;
import fr.ird.voxelidar.extraction.RxpExtraction;
import fr.ird.voxelidar.extraction.RxpExtractionListener;
import fr.ird.voxelidar.extraction.Shot;
import fr.ird.voxelidar.frame.JFrameSettingUp;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
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
        
        //new TestJNI1().afficherBonjour();
        
        //SimpleDLL sdll = INSTANCE;
        //SimpleDLL sdll = SimpleDLL.INSTANCE;
        
        //String res = sdll.simpleCall("test");
        
        //ShotResultCallBack call = new ShotResultCallBack();
        
        //sdll.registerCallback(call);
        /*
        ShotExtractor shotExtractor = new ShotExtractor();
        
        final ArrayList<Shot> shots = new ArrayList<>();
        
        shotExtractor.extractFromRxp(new File("\\\\forestview01\\BDLidar\\TLS\\Puechabon2013\\PuechabonAvril\\PuechabonAvril2013.RiSCAN\\SCANS\\ScanPos001\\SINGLESCANS\\130403_091135.rxp"), 1000000, new AbstractCallback() {

            @Override
            public void callback(String param1, int size, Shot s) {
                
                System.out.println(param1 +" " + size);
                
                //Structure[] str = (Structure[])Array.newInstance(s.getClass(), size);
                
                shots.addAll(Arrays.asList((Shot[])s.toArray(size)));
            }
        });
        */
        //sdll.simpleConnection("C:\\Users\\Julien\\Documents\\Visual Studio 2012\\Projects\\TLSRivLib\\testmtd.rxp", 10000);        
        //sdll.simpleConnection("C:\\Users\\Julien\\Documents\\Visual Studio 2012\\Projects\\TLSRivLib\\testmtd.rxp", 100000);
        /*
        final BlockingQueue<LasPoint> queue = new LinkedBlockingQueue<>();
        LasExtraction extraction = new LasExtraction(queue, "\\\\forestview01\\BDLidar\\ALS\\Paracou\\2013\\Dalles_semis_points\\Paracou000005.las");
            
        extraction.addLasExtractionListener(new LasExtractionListener() {

            @Override
            public void isFinished() {
                System.out.println("test");
            }
        });
        

        //runnable to do the extraction
        new Thread(extraction).start();
        */
        
        JFrameSettingUp mainJFrame = new JFrameSettingUp();
        mainJFrame.setVisible(true);
        
    }
    
    
}
