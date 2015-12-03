/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.amap.amapvox.mavenproject6;

import fr.amap.amapvox.io.tls.rsp.Rsp;
import fr.amap.amapvox.io.tls.rsp.Scans;
import fr.amap.amapvox.io.tls.rxp.RxpExtraction;
import fr.amap.amapvox.io.tls.rxp.Shot;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;

/**
 *
 * @author calcul
 */
public class Main {
    
    public static void main(String[] args) {
        
        try {
            Rsp rsp = new Rsp();
            rsp.read(new File("/media/forestview01/BDLidar/TLS/Test_scanners/Riegl/TestScanners.RiSCAN/project.rsp"));
            
            ArrayList<Scans> rxpList = rsp.getRxpList();
            
            for(Scans scans : rxpList){
                
                RxpExtraction extraction = new RxpExtraction();
                extraction.openRxpFile(scans.getScanLite().getFile());
                Iterator<Shot> iterator = extraction.iterator();

                BufferedWriter writer = new BufferedWriter(new FileWriter(new File("/media/forestview01/partageLidar/Voxelisation/test_amapVOX/test_first_echo_vs_normal/tirs_mon/"+scans.getScanLite().getFile().getName())));

                writer.write("nb_echos origin_x origin_y origin_z direction_x direction_y direction_z r1 r2 r3 r4 r5 r6 r7\n");

                while(iterator.hasNext()){
                    Shot shot = iterator.next();

                    String line = shot.nbEchos+" "+shot.origin.x+" "+shot.origin.y+" "+shot.origin.z
                                +" "+shot.direction.x+" "+shot.direction.y+" "+shot.direction.z;
                        
                        
                    for(int i=0;i<shot.nbEchos;i++){
                        line += " "+shot.ranges[i];
                    }
                    
                    writer.write(line+"\n");
                }

                extraction.close();

                writer.close();
            }
            
            
            
        } catch (Exception ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
